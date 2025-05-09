package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.InputConstants;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.interfaces.KeyInputListener;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

//TODO: Multiline InputField
public class InputField extends Component implements KeyInputListener, MouseInputListener {
    public static final double HEIGHT = 11;
    public static String FILTER_ALL = "[0-9a-zA-Z!?,.:;\\-{}()/&%$\"<>' ]";
    public static String FILTER_NUMBERS = "[0-9.,\\-!]";
    public static String FILTER_HEX = "[#0-9a-fA-F]";
    public static String FILTER_FILENAME = "[^</*?\"\\\\>:|]";
    public boolean numbersOnly;
    public String content;
    public ContentProvider onContentChange = null;
    public String name = null;
    public Color normalColor = new Color(31, 31, 31, 150);
    public Color edgeColor = new Color(255, 255, 255, 95);
    public Color cursorColor = new Color(255, 255, 255, 150);
    public Color highlightColor = new Color(255, 255, 255, 175);
    private boolean isFocused = false;
    private int cursorPos = 0;
    private int highlightStart = 0;
    private int highlightEnd = 0;
    private String customFilter = null;

    public InputField(Vector2D pos, double width) {
        this("", pos, width, false);
    }

    public InputField(String content, Vector2D pos, double width, boolean numbersOnly) {
        this.setPos(pos);
        this.setSize(new Vector2D(width, HEIGHT));
        this.content = content;
        this.numbersOnly = numbersOnly;
    }

    public InputField(String content, Vector2D pos, double width) {
        this(content, pos, width, false);
    }

    public void focus() {
        isFocused = true;
    }

    public InputField setName(String name) {
        this.name = name;
        return this;
    }

    public InputField setOnContentChange(ContentProvider onContentChange) {
        this.onContentChange = onContentChange;
        return this;
    }

    @Override
    public void render(Vector2D mouse) {
        Vector2D nameSize = name == null ? Vector2D.ZERO : FontRenderer.getStringSize(name);
        Vector2D rectPos = getDisplayedPos().add(nameSize.getX(), 0);
        Vector2D rectSize = getDisplayedSize().sub(nameSize.getX(), 0);

        if (name != null) {
            FontRenderer.drawCenteredString(
                    name,
                    getDisplayedPos().add(nameSize.getX() / 2D, getDisplayedSize().getY() / 2D + 1),
                    Color.WHITE,
                    false
            );
        }

        Renderer2D.drawRectWithEdge(rectPos, rectSize, 1, normalColor, edgeColor);

        cursorPos = MathUtil.constrain(cursorPos, 0, content.length());
        highlightStart = MathUtil.constrain(highlightStart, 0, content.length());
        highlightEnd = MathUtil.constrain(highlightEnd, 0, content.length());

        FontRenderer.drawString(
                content,
                rectPos.add(2, 2),
                Color.WHITE, false
        );
        if (highlightStart != highlightEnd) {
            Renderer2D.drawRect(
                    rectPos.add(2 + FontRenderer.getStringSize(content.substring(0, highlightStart)).getX(), 2),
                    new Vector2D(FontRenderer.getStringSize(content.substring(highlightStart, highlightEnd)).getX(), rectSize.getY() - 4),
                    highlightColor
            );
            FontRenderer.drawString(
                    content.substring(highlightStart, highlightEnd),
                    rectPos.add(2 + FontRenderer.getStringSize(content.substring(0, highlightStart)).getX(), 2),
                    Color.BLACK, false
            );
        }

        if (isFocused && highlightStart == highlightEnd)
            Renderer2D.drawRect(
                    new Vector2D(rectPos.getX() + getCursorX(), rectPos.getY() + 1),
                    new Vector2D(1, rectSize.getY() - 2),
                    cursorColor
            );
    }

    private double getCursorX() {
        return 2 + FontRenderer.getStringSize(content.substring(0, cursorPos)).getX();
    }

    @Override
    public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        if (!isFocused) return false;
        boolean inputPerformed = true;

        if (isCharTyped && String.valueOf((char) keyCode).matches(getFilter())) {
            typeContentAtCursor(Character.toString((char) keyCode));
        } else {
            switch (keyCode) {
                case InputConstants.KEY_BACKSPACE:
                    deleteSelection();
                    if (highlightStart == highlightEnd)
                        cursorPos--;
                    else cursorPos = highlightStart;
                    break;
                case InputConstants.KEY_DELETE:
                    if (highlightStart == highlightEnd)
                        cursorPos++;
                    deleteSelection();
                    if (highlightStart == highlightEnd)
                        cursorPos--;
                    break;
                case InputConstants.KEY_LEFT:
                    if (highlightStart == highlightEnd)
                        cursorPos--;
                    else cursorPos = highlightStart;
                    break;
                case InputConstants.KEY_RIGHT:
                    if (highlightStart == highlightEnd)
                        cursorPos++;
                    else cursorPos = highlightEnd;
                    break;
                default:
                    inputPerformed = false;
                    break;
            }
        }

        if (inputPerformed) {
            cursorPos = MathUtil.constrain(cursorPos, 0, content.length());
            highlightStart = cursorPos;
            highlightEnd = cursorPos;
        }

        return true;
    }

    private String getFilter() {
        if (customFilter != null) {
            return customFilter;
        }
        return numbersOnly ? FILTER_NUMBERS : FILTER_ALL;
    }

    public InputField setFilter(String filter) {
        this.customFilter = filter;
        return this;
    }

    public void typeContentAtCursor(String c) {
        updateContent(content.substring(0, highlightStart) + c + content.substring(highlightEnd));
        cursorPos = highlightStart + c.length();
        highlightStart = highlightEnd = cursorPos;
    }

    private void deleteSelection() {
        if (highlightStart == highlightEnd)
            updateContent(content.substring(0, Math.max(cursorPos - 1, 0)) + (cursorPos >= content.length() ? "" : content.substring(cursorPos)));
        else
            updateContent(content.substring(0, highlightStart) + content.substring(highlightEnd));
    }

    private void updateContent(String content) {
        this.content = content;
        if (onContentChange != null)
            onContentChange.apply(new Content(content));
    }

    public void clear() {
        isFocused = false;
        cursorPos = 0;
        highlightStart = 0;
        highlightEnd = 0;
        content = "";
    }

    public void setWidth(double width) {
        this.size.setX(width);
    }

    @Override
    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (button == Mouse.Button.LEFT) {
            switch (state) {
                case DOWN:
                    if (contains(mousePos)) {
                        isFocused = true;
                        cursorPos = getCursorPosFromMousePos(mousePos);
                        highlightStart = cursorPos;
                        highlightEnd = cursorPos;
                        return true;
                    } else {
                        isFocused = false;
                    }
                case DRAG:
                case UP:
                    if (isFocused) {
                        int c = getCursorPosFromMousePos(mousePos);
                        if (c < cursorPos) {
                            highlightStart = c;
                            highlightEnd = cursorPos;
                        } else {
                            highlightStart = cursorPos;
                            highlightEnd = c;
                        }
                        return true;
                    }
            }
        }
        return false;
    }

    private int getCursorPosFromMousePos(Vector2D mouse) {
        Vector2D nameSize = name == null ? Vector2D.ZERO : FontRenderer.getStringSize(name);
        Vector2D rectPos = getDisplayedPos().add(nameSize.getX(), 0);

        double x = mouse.getX() - rectPos.getX() - 2;
        if (x < 0)
            return 0;
        else if (x > FontRenderer.getStringSize(content).getX())
            return content.length();

        for (int i = 1; i <= content.length(); i++) {
            int charWidth = FontRenderer.getStringSize(content.substring(i - 1, i)).getXI();
            if (x < charWidth / 2D)
                return i - 1;
            else if (x < charWidth)
                return i;

            x -= charWidth;
        }
        return content.length();
    }

    @FunctionalInterface
    public interface ContentProvider {
        void apply(Content content);
    }

    public static class Content {
        public String content;

        public Content(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public Double getNumber() {
            try {
                return Double.parseDouble(content);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
