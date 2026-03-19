package com.terminal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBufferTest {

    private TerminalBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new TerminalBuffer(10, 3, 2);
    }

    @Test
    void testInitializationThrowsOnInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(-1, 10, 100));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(10, 0, 100));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(10, 10, -5));
    }

    @Test
    void testCursorClamping() {
        buffer.moveCursorLeft(100);
        buffer.moveCursorUp(100);

        assertEquals(0, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());

        buffer.moveCursorRight(100);
        buffer.moveCursorDown(100);

        assertEquals(9, buffer.getCursorCol());
        assertEquals(2, buffer.getCursorRow());
    }

    @Test
    void testWriteAndLineWrapping() {
        buffer.write("1234567890AB");

        assertEquals("1234567890", buffer.getLineAsString(0));

        assertEquals("AB", buffer.getLineAsString(1));

        assertEquals(2, buffer.getCursorCol());
        assertEquals(1, buffer.getCursorRow());
    }

    @Test
    void testInsertEmptyLineAndScrollbackLimit() {
        buffer.write("Linia 1");

        buffer.setCursorPosition(0, 2);

        buffer.insertEmptyLine();
        buffer.insertEmptyLine();
        buffer.insertEmptyLine();
        buffer.insertEmptyLine();

        assertEquals(5, buffer.getTotalRows());

        assertEquals("", buffer.getLineAsString(0));
        assertEquals("", buffer.getLineAsString(1));
        assertEquals("", buffer.getLineAsString(2));
    }

    @Test
    void testClearScreen() {
        buffer.write("Test");
        buffer.clearScreen();

        assertEquals("", buffer.getLineAsString(0));
        assertEquals(0, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow());
    }

    @Test
    void testResizeTruncationStrategy() {
        buffer.write("1234567890");

        buffer.setCursorPosition(9, 2);

        buffer.resize(5, 2);

        assertEquals(5, buffer.getWidth());
        assertEquals(2, buffer.getHeight());

        assertEquals("12345", buffer.getLineAsString(0));

        assertEquals(4, buffer.getCursorCol());
        assertEquals(1, buffer.getCursorRow());

        buffer.resize(10, 3);

        assertEquals("12345", buffer.getLineAsString(0));
    }
}