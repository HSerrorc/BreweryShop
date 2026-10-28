package com.hserrorc.breweryshop;

import java.util.List;

public class GuiLayout {
    private final String title;
    private final List<String> layout;
    private final int rows;

    public GuiLayout(String title, List<String> layout) {
        this.title = title;
        this.layout = layout;
        this.rows = layout.size();
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return rows * 9;
    }

    public char getSlot(int index) {
        int row = index / 9;
        int col = index % 9;
        if (row >= layout.size()) return ' ';
        String line = layout.get(row);
        if (col >= line.length()) return ' ';
        return line.charAt(col);
    }

    public int getSellButtonSlot() {
        for (int i = 0; i < getSize(); i++) {
            if (getSlot(i) == 'S') return i;
        }
        return -1;
    }
}
