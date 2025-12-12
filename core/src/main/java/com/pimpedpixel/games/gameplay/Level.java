package com.pimpedpixel.games.gameplay;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private int levelNumber;
    private ArrayList<Scenario> scenarios;

    // Getters and setters
    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<Scenario> scenarios) {
        if (scenarios instanceof ArrayList) {
            this.scenarios = (ArrayList<Scenario>) scenarios;
        } else {
            this.scenarios = new ArrayList<>(scenarios);
        }
    }
}
