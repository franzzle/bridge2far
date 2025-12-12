package com.pimpedpixel.games.gameplay;

import java.util.ArrayList;
import java.util.List;

public class CollisionLayer {
    private int matchingRow;
    private ArrayList<Integer> cellStates; // Uses CollisionType values (0, 1, 2, etc.)

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
        if (cellStates instanceof ArrayList) {
            this.cellStates = (ArrayList<Integer>) cellStates;
        } else {
            this.cellStates = new ArrayList<>(cellStates);
        }
    }
}
