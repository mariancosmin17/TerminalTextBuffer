package com.terminal;

public record Attributes(
        TerminalColor foreground,
        TerminalColor background,
        boolean isBold,
        boolean isItalic,
        boolean isUnderline
) {
    public static Attributes createDefault() {
        return new Attributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT, false, false, false);
    }
}