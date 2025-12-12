package com.pimpedpixel.games.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.pimpedpixel.games.config.DebugConfig;

/**
 * Loads debugconfig.json into a DebugConfig.DebugConfigData via AssetManager (GWT-friendly).
 */
public class DebugConfigAssetLoader extends SynchronousAssetLoader<DebugConfig.DebugConfigData, DebugConfigAssetLoader.DebugConfigParameter> {
    private final Json json = new Json();

    public DebugConfigAssetLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public DebugConfig.DebugConfigData load(AssetManager manager, String fileName, FileHandle file, DebugConfigParameter parameter) {
        try {
            String raw = file.readString("UTF-8");
            if (raw == null || raw.isEmpty()) {
                com.badlogic.gdx.Gdx.app.error(getClass().getSimpleName(),
                    "Debug config file is empty: " + fileName + " (exists=" + file.exists() + ")");
            }
            return json.fromJson(DebugConfig.DebugConfigData.class, raw);
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error(getClass().getSimpleName(),
                "Failed to parse debug config from " + fileName + " (exists=" + file.exists() + ")", e);
            throw e;
        }
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, DebugConfigParameter parameter) {
        return null;
    }

    public static class DebugConfigParameter extends AssetLoaderParameters<DebugConfig.DebugConfigData> {
    }
}
