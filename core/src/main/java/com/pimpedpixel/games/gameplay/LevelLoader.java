package com.pimpedpixel.games.gameplay;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.files.FileHandle;

public class LevelLoader {
    private final Json json;

    public LevelLoader() {
        json = new Json();
    }

    public LevelContainer loadLevels(String filePath) {
        FileHandle file = new FileHandle(filePath);
        return json.fromJson(LevelContainer.class, file);
    }

    // Helper class to match the JSON structure
    public static class LevelContainer {
        private Level[] levels;

        public Level[] getLevels() {
            return levels;
        }

        public void setLevels(Level[] levels) {
            this.levels = levels;
        }
    }
}
