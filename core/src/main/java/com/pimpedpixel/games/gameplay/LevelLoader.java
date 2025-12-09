package com.pimpedpixel.games.gameplay;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.files.FileHandle;

public class LevelLoader {
    private Json json;

    public LevelLoader() {
        json = new Json();
    }

    public Level[] loadLevels(String filePath) {
        FileHandle file = new FileHandle(filePath);
        return json.fromJson(Level[].class, file);
    }
}
