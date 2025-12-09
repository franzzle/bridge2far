package com.pimpedpixel.games.gameplay;

import java.util.List;

public class Level {
    private int levelNumber;
    private List<Scenario> scenarios;

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
        this.scenarios = scenarios;
    }
}
