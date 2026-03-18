package com.terminal;

public class TerminalBuffer {
    private final int width;
    private final int height;
    private final int maxScrollback;

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
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getMaxScrollback() { return maxScrollback; }
}