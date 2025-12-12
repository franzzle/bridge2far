package com.pimpedpixel.games.gameplay;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

public class LevelLoader {
    private final Json json;

    public LevelLoader() {
        json = new Json();
    }

    public LevelContainer loadLevels(String filePath) {
        FileHandle file = Gdx.files.internal(filePath);
        return json.fromJson(LevelContainer.class, file);
    }

    // Helper class to match the JSON structure
    public static class LevelContainer {
        private Level[] levels;
        private SystemDefaults systemdefaults;

        public Level[] getLevels() {
            return levels;
        }

        public void setLevels(Level[] levels) {
            this.levels = levels;
        }

        public SystemDefaults getSystemdefaults() {
            return systemdefaults;
        }

        public void setSystemdefaults(SystemDefaults systemdefaults) {
            this.systemdefaults = systemdefaults;
        }
    }

    // Class to represent system defaults
    public static class SystemDefaults {
        private int moveSpeed;
        private int jumpSpeed;
        private int gravity;
        private int spawnX;
        private int spawnY;
        private int timeLimit;
        private int dyingDuration;
        private int diedDuration;
        private int timerDecrementAmount;

        // Getters and setters
        public int getMoveSpeed() {
            return moveSpeed;
        }

        public void setMoveSpeed(int moveSpeed) {
            this.moveSpeed = moveSpeed;
        }

        public int getJumpSpeed() {
            return jumpSpeed;
        }

        public void setJumpSpeed(int jumpSpeed) {
            this.jumpSpeed = jumpSpeed;
        }

        public int getGravity() {
            return gravity;
        }

        public void setGravity(int gravity) {
            this.gravity = gravity;
        }

        public int getSpawnX() {
            return spawnX;
        }

        public void setSpawnX(int spawnX) {
            this.spawnX = spawnX;
        }

        public int getSpawnY() {
            return spawnY;
        }

        public void setSpawnY(int spawnY) {
            this.spawnY = spawnY;
        }

        public int getTimeLimit() {
            return timeLimit;
        }

        public void setTimeLimit(int timeLimit) {
            this.timeLimit = timeLimit;
        }

        public int getDyingDuration() {
            return dyingDuration;
        }

        public void setDyingDuration(int dyingDuration) {
            this.dyingDuration = dyingDuration;
        }

        public int getDiedDuration() {
            return diedDuration;
        }

        public void setDiedDuration(int diedDuration) {
            this.diedDuration = diedDuration;
        }

        public int getTimerDecrementAmount() {
            return timerDecrementAmount;
        }

        public void setTimerDecrementAmount(int timerDecrementAmount) {
            this.timerDecrementAmount = timerDecrementAmount;
        }
    }
}
