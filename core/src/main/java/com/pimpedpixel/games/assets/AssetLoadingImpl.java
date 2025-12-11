package com.pimpedpixel.games.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.pimpedpixel.games.GameInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssetLoadingImpl implements AssetLoading {
    private final AssetManager assetManager;
    private final GameInfo gameInfo;

    public AssetLoadingImpl(AssetManager assetManager, GameInfo gameInfo) {
        this.assetManager = assetManager;
        this.gameInfo = gameInfo;
    }

    @Override
    public void start() {
        initDefaultFont();
        initC64Font();
        initTmxTiles();
        initSoundFx();
        initBackgroundMusic();

        assetManager.finishLoading();
    }

    private void initC64Font() {
        final String fontFile = "font/c64.fnt";
        final FileHandle fontFileHandle = Gdx.files.internal(fontFile);
        if (fontFileHandle.exists()) {
            Gdx.app.log(this.getClass().getSimpleName(), fontFileHandle.path());
            final BitmapFontLoader.BitmapFontParameter bitmapFontParameter = new BitmapFontLoader.BitmapFontParameter();
            bitmapFontParameter.flip = false;
            bitmapFontParameter.minFilter = Texture.TextureFilter.Nearest;
            bitmapFontParameter.magFilter = Texture.TextureFilter.Nearest;
            assetManager.setLoader(BitmapFont.class, new BitmapFontLoader(new InternalFileHandleResolver()));
            assetManager.load(fontFile, BitmapFont.class, bitmapFontParameter);
        } else {
            System.out.println("C64 font file not found: " + fontFileHandle.path());
        }
    }

    private void initSoundFx() {
        final String soundFile = "soundfx/jumping.ogg";
        // Check if sound file exists
        if (Gdx.files.internal(soundFile).exists()) {
            assetManager.setLoader(Sound.class, new SoundLoader(new InternalFileHandleResolver()));
            assetManager.load(soundFile, Sound.class);
        } else {
            System.out.println("Sound file not found: " + soundFile);
        }
    }

    private void initTmxTiles() {
        final List<String> lines = getTmxFileList();
        System.out.println("DEBUG: Found " + lines.size() + " TMX files in files.txt:");
        for (String line : lines) {
            System.out.println("  - " + line);
        }
        
        for (final String line : lines){
            final String expectedPath = gameInfo.getTmxFile(line).path();
            System.out.println("DEBUG: Checking TMX file: " + expectedPath);
            System.out.println("DEBUG: File exists: " + gameInfo.getTmxFile(line).exists());
            
            if (gameInfo.getTmxFile(line).exists()) {
                final String tileMapFileName = gameInfo.getTmxFile(line).path();
                assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
                assetManager.load(tileMapFileName, TiledMap.class);
                System.out.println("DEBUG: Loaded TMX asset: " + tileMapFileName);
            } else {
                System.out.println("TMX file not found: " + expectedPath);
            }
        }
    }

    private List<String> getTmxFileList(){
        FileHandle fileList = gameInfo.getTileMapFileList(); // note: typo is preserved: "geMusicFiles"

        if (!fileList.exists()) {
            Gdx.app.debug("AssetLoading", "Tmx map list file not found: " + fileList.path());
            return new ArrayList<>();
        }

        String content = fileList.readString("UTF-8");
        String[] lines = content.split("\\r?\\n");

        return Arrays.asList(lines);
    }

    private void initDefaultFont() {
        final String fontFile = "font/c64";
        // Check if font file exists
        final FileHandle fontFileHandle = Gdx.files.internal(fontFile);
        if (fontFileHandle.exists()) {
            Gdx.app.log(this.getClass().getSimpleName(), fontFileHandle.path());
            final BitmapFontLoader.BitmapFontParameter bitmapFontParameter = new BitmapFontLoader.BitmapFontParameter();
            bitmapFontParameter.flip = false;
            bitmapFontParameter.minFilter = Texture.TextureFilter.Nearest;
            bitmapFontParameter.magFilter = Texture.TextureFilter.Nearest;
            assetManager.setLoader(BitmapFont.class, new BitmapFontLoader(new InternalFileHandleResolver()));
            assetManager.load(fontFile, BitmapFont.class, bitmapFontParameter);
        } else {
            System.out.println("Font file not found: " + fontFile);
        }
    }

    private void initBackgroundMusic() {
        FileHandle fileList = gameInfo.geMusicFileList(); // note: typo is preserved: "geMusicFiles"

        if (!fileList.exists()) {
            Gdx.app.debug("AssetLoading", "Music list file not found: " + fileList.path());
            return;
        }

        String content = fileList.readString("UTF-8");
        String[] lines = content.split("\\r?\\n");

        for (String line : lines) {
            String filename = line.trim();
            if (filename.isEmpty()) continue;

            FileHandle musicFile = gameInfo.getMusicForOgg(filename);
            if (!musicFile.exists()) {
                Gdx.app.error("AssetLoading", "Music file listed but not found: " + musicFile.path());
                continue;
            }

            if (!assetManager.isLoaded(musicFile.path(), Music.class)) {
                assetManager.load(musicFile.path(), Music.class);
                Gdx.app.log("AssetLoading", "Preloading music: " + musicFile.path());
            }
        }
    }

    @Override
    public boolean ready() {
        return assetManager.update();
    }
}
