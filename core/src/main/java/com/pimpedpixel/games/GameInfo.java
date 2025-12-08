package com.pimpedpixel.games;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class GameInfo {
    public static final String ASSETS_ADVENTURES_LOSTADVENTURES_ROOM = "rooms";

    public FileHandle getTmxFile(String name) {
        String path =  ASSETS_ADVENTURES_LOSTADVENTURES_ROOM + "/tiles/" + name + ".tmx";
        return Gdx.files.internal(path);
    }

    public FileHandle getMusicForOgg(String filenameWithoutExtension) {
        String path = "music/wavs/" + filenameWithoutExtension + ".ogg";
        return Gdx.files.internal(path);
    }

    public FileHandle geMusicFileList() {
        String path = "music/files.txt";
        return Gdx.files.internal(path);
    }

    public FileHandle getTileMapFileList() {
        String path = "rooms/tiles/files.txt";
        return Gdx.files.internal(path);
    }
}
