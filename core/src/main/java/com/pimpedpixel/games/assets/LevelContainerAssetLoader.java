package com.pimpedpixel.games.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.pimpedpixel.games.gameplay.LevelLoader;

/**
 * Loads levelInfo.json into a LevelContainer via AssetManager (GWT-friendly).
 */
public class LevelContainerAssetLoader extends SynchronousAssetLoader<LevelLoader.LevelContainer, LevelContainerAssetLoader.LevelContainerParameter> {
    private final Json json = new Json();

    public LevelContainerAssetLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public LevelLoader.LevelContainer load(AssetManager manager, String fileName, FileHandle file, LevelContainerParameter parameter) {
        try {
            String raw = file.readString("UTF-8");
            if (raw == null || raw.isEmpty()) {
                com.badlogic.gdx.Gdx.app.error(getClass().getSimpleName(),
                    "Level file is empty: " + fileName + " (exists=" + file.exists() + ")");
            }
            return json.fromJson(LevelLoader.LevelContainer.class, raw);
        } catch (Exception e) {
            // Log with path and rethrow so GWT console shows the real cause.
            com.badlogic.gdx.Gdx.app.error(getClass().getSimpleName(),
                "Failed to parse level data from " + fileName + " (exists=" + file.exists() + ")", e);
            throw e;
        }
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, LevelContainerParameter parameter) {
        return null;
    }

    public static class LevelContainerParameter extends AssetLoaderParameters<LevelLoader.LevelContainer> {
    }
}
