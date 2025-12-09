package com.pimpedpixel.games.gameplay;

import java.util.List;

public class CollisionLayer {
    private int rowNumber;
    private List<Integer> cells; // Uses CollisionType values (0, 1, 2, etc.)

    // Getters and setters
    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public List<Integer> getCells() {
        return cells;
    }

    public void setCells(List<Integer> cells) {
        this.cells = cells;
    }
}
