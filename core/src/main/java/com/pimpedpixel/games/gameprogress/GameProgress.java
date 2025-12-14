package com.pimpedpixel.games.gameprogress;

public final class GameProgress {

    private final int level;
    private final int attempts;

    public GameProgress(int level, int attempts) {
        if (attempts < 0 || attempts > 100_000) {
            throw new IllegalArgumentException("Attempts out of range");
        }
        if (level < 0 || level > 255) {
            throw new IllegalArgumentException("Level out of range");
        }
        this.level = level;
        this.attempts = attempts;
    }

    public int getLevel() {
        return level;
    }

    public int getAttempts() {
        return attempts;
    }

    @Override
    public String toString() {
        return "GameProgress[level=" + level + ", attempts=" + attempts + "]";
    }
}
