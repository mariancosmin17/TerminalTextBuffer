package com.terminal;

public class TerminalBuffer {
    private final int width;
    private final int height;
    private final int maxScrollback;

    private int cursorCol;
    private int cursorRow;
    private Attributes currentAttributes;

    public TerminalBuffer(int width, int height, int maxScrollback) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive.");
        }
        if (maxScrollback < 0) {
            throw new IllegalArgumentException("Scrollback size cannot be negative.");
        }

        this.width = width;
        this.height = height;
        this.maxScrollback = maxScrollback;
        this.cursorCol = 0;
        this.cursorRow = 0;
        this.currentAttributes = Attributes.createDefault();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getMaxScrollback() { return maxScrollback; }
    public Attributes getCurrentAttributes() {
        return currentAttributes;
    }
    public void setCurrentAttributes(Attributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("Attributes cannot be null");
        }
        this.currentAttributes = attributes;
    }
    public int getCursorCol() { return cursorCol; }
    public int getCursorRow() { return cursorRow; }
    public void setCursorPosition(int col, int row) {
        this.cursorCol = Math.max(0, Math.min(width - 1, col));
        this.cursorRow = Math.max(0, Math.min(height - 1, row));
    }
    public void moveCursorUp(int n) {
        setCursorPosition(cursorCol, cursorRow - n);
    }

    public void moveCursorDown(int n) {
        setCursorPosition(cursorCol, cursorRow + n);
    }

    public void moveCursorLeft(int n) {
        setCursorPosition(cursorCol - n, cursorRow);
    }

    public void moveCursorRight(int n) {
        setCursorPosition(cursorCol + n, cursorRow);
    }
}