package com.pimpedpixel.games.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.pimpedpixel.games.config.CharacterConfig;

/**
 * Loads characters.json into CharacterConfig.CharacterData[] via AssetManager (GWT-friendly).
 */
public class CharacterConfigAssetLoader extends SynchronousAssetLoader<CharacterConfig.CharacterData[], CharacterConfigAssetLoader.CharacterConfigParameter> {
    private final Json json = new Json();

    public CharacterConfigAssetLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public CharacterConfig.CharacterData[] load(AssetManager manager, String fileName, FileHandle file, CharacterConfigParameter parameter) {
        try {
            String raw = file.readString("UTF-8");
            if (raw == null || raw.isEmpty()) {
                com.badlogic.gdx.Gdx.app.error(getClass().getSimpleName(),
                    "Character config file is empty: " + fileName + " (exists=" + file.exists() + ")");
            }
            return json.fromJson(CharacterConfig.CharacterData[].class, raw);
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error(getClass().getSimpleName(),
                "Failed to parse character config from " + fileName + " (exists=" + file.exists() + ")", e);
            throw e;
        }
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, CharacterConfigParameter parameter) {
        return null;
    }

    public static class CharacterConfigParameter extends AssetLoaderParameters<CharacterConfig.CharacterData[]> {
    }
}
