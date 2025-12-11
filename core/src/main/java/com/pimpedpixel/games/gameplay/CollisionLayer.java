package com.pimpedpixel.games.gameplay;

import java.util.List;

public class CollisionLayer {
    private int matchingRow;
    private List<Integer> cellStates; // Uses CollisionType values (0, 1, 2, etc.)

    // Getters and setters
    public int getMatchingRow() {
        return matchingRow;
    }

    public void setMatchingRow(int matchingRow) {
        this.matchingRow = matchingRow;
    }

    public List<Integer> getCellStates() {
        return cellStates;
    }

    public void setCellStates(List<Integer> cellStates) {
        this.cellStates = cellStates;
    }
}
