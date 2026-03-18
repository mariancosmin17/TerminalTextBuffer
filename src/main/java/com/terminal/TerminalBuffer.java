package com.terminal;

import java.util.ArrayDeque;
import java.util.Deque;

public class TerminalBuffer {
    private final int width;
    private final int height;
    private final int maxScrollback;

    private int cursorCol;
    private int cursorRow;
    private Attributes currentAttributes;

    private final Cell[][] screen;

    private final Deque<Cell[]> scrollback;

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
        this.screen = new Cell[height][width];

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                this.screen[r][c] = Cell.createEmpty();
            }
        }

        this.scrollback = new ArrayDeque<>();
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
    public void write(String text)
    {
        if(text==null||text.isEmpty()) return;
        for(char ch:text.toCharArray())
        {
            Cell currentCell=screen[cursorRow][cursorCol];
            currentCell.setCharacter(ch);
            currentCell.setAttributes(currentAttributes);
            if(cursorCol<width-1) {
                cursorCol++;
            }
            else{
                if(cursorRow<height-1){
                    cursorCol=0;
                    cursorRow++;
                }
            }
        }
    }

    public void clearScreen() {
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Cell cell = screen[r][c];
                cell.setCharacter(' ');
                cell.setAttributes(Attributes.createDefault());
            }
        }
        setCursorPosition(0, 0);
    }

    public void insert(String text) {
        if (text == null || text.isEmpty()) return;
        int len = text.length();
        for (int r = height - 1; r >= cursorRow; r--) {
            for (int c = width - 1; c >= 0; c--) {
                if (r == cursorRow && c < cursorCol) continue;
                int flatIndex = r * width + c;
                int newFlatIndex = flatIndex + len;

                if (newFlatIndex < width * height) {
                    int newR = newFlatIndex / width;
                    int newC = newFlatIndex % width;
                    screen[newR][newC].setCharacter(screen[r][c].getCharacter());
                    screen[newR][newC].setAttributes(screen[r][c].getAttributes());
                }
            }
        }
        write(text);
    }

    public void fillLine(char ch) {
        for (int c = 0; c < width; c++) {
            Cell cell = screen[cursorRow][c];
            cell.setCharacter(ch);
            cell.setAttributes(currentAttributes);
        }
    }

    public void insertEmptyLine(){
        if(maxScrollback>0)
        {
            Cell[] topLineCopy=new Cell[width];
            for(int c=0;c<width;c++)
            {
                topLineCopy[c]=new Cell(screen[0][c].getCharacter(),screen[0][c].getAttributes());
            }
            if(scrollback.size()>=maxScrollback)
            {
                scrollback.pollFirst();
            }
            scrollback.addLast(topLineCopy);
        }
        for (int r = 0; r < height - 1; r++) {
            for (int c = 0; c < width; c++) {
                screen[r][c].setCharacter(screen[r+1][c].getCharacter());
                screen[r][c].setAttributes(screen[r+1][c].getAttributes());
            }
        }
        for (int c = 0; c < width; c++) {
            screen[height-1][c].setCharacter(' ');
            screen[height-1][c].setAttributes(Attributes.createDefault());
        }
    }

    public void clearScreenAndScrollback() {
        clearScreen();
        scrollback.clear();
    }

    public int getTotalRows() {
        return scrollback.size() + height;
    }

    private Cell[] getRow(int virtualRow) {
        if (virtualRow < 0 || virtualRow >= getTotalRows()) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + virtualRow);
        }

        if (virtualRow < scrollback.size()) {
            int currentIndex = 0;
            for (Cell[] row : scrollback) {
                if (currentIndex == virtualRow) return row;
                currentIndex++;
            }
        }

        int screenRow = virtualRow - scrollback.size();
        return screen[screenRow];
    }

    public char getCharacterAt(int col, int virtualRow) {
        if (col < 0 || col >= width) throw new IndexOutOfBoundsException("Column out of bounds");
        return getRow(virtualRow)[col].getCharacter();
    }

    public Attributes getAttributesAt(int col, int virtualRow) {
        if (col < 0 || col >= width) throw new IndexOutOfBoundsException("Column out of bounds");
        return getRow(virtualRow)[col].getAttributes();
    }

    public String getLineAsString(int virtualRow) {
        Cell[] row = getRow(virtualRow);
        StringBuilder sb = new StringBuilder(width);

        for (int c = 0; c < width; c++) {
            sb.append(row[c].getCharacter());
        }

        int lastNonSpace = width - 1;
        while (lastNonSpace >= 0 && sb.charAt(lastNonSpace) == ' ') {
            lastNonSpace--;
        }

        return sb.substring(0, lastNonSpace + 1);
    }

    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder();
        int startIndex = scrollback.size();

        for (int r = startIndex; r < getTotalRows(); r++) {
            sb.append(getLineAsString(r));
            if (r < getTotalRows() - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public String getEntireContentAsString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < getTotalRows(); r++) {
            sb.append(getLineAsString(r));
            if (r < getTotalRows() - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}