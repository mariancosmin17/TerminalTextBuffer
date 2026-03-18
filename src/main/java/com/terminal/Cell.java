package com.terminal;

public class Cell {
    private char character;
    private Attributes attributes;

    public Cell(char character, Attributes attributes) {
        this.character = character;
        this.attributes = attributes;
    }

    public static Cell createEmpty() {
        return new Cell(' ', Attributes.createDefault());
    }

    public char getCharacter() { return character; }
    public void setCharacter(char character) { this.character = character; }

    public Attributes getAttributes() { return attributes; }
    public void setAttributes(Attributes attributes) { this.attributes = attributes; }
}