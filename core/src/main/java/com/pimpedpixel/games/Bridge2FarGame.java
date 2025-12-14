package com.pimpedpixel.games;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.pimpedpixel.games.DesignResolution.Preset;
import com.pimpedpixel.games.gameprogress.GameProgress;
import com.pimpedpixel.games.screens.Bridge2FarGameplayScreen;
import com.pimpedpixel.games.screens.Bridge2FarMenuScreen;

/**
 * Main LibGDX Game that manages screens (menu and gameplay) and shares
 * core services like the AssetManager across them.
 */
public class Bridge2FarGame extends Game {

    private static final Preset DEFAULT_RESOLUTION = Preset.HD_1280x800;

    private AssetManager assetManager;
    private GameInfo gameInfo;

    @Override
    public void create() {
        applyResolutionPreset();
        assetManager = new AssetManager();
        gameInfo = new GameInfo();
        setScreen(new Bridge2FarMenuScreen(this));
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    /**
     * Transition from the current screen to the gameplay screen, disposing
     * of the previous screen once the switch is complete.
     */
    public void startGameplay(GameProgress resumeProgress) {
        Screen previous = getScreen();
        Bridge2FarGameplayScreen gameplayScreen = new Bridge2FarGameplayScreen(this, resumeProgress);
        setScreen(gameplayScreen);
        if (previous != null) {
            previous.dispose();
        }
    }

    /**
     * Return to the main menu screen, disposing of the previous screen.
     */
    public void returnToMenu() {
        Screen previous = getScreen();
        Bridge2FarMenuScreen menuScreen = new Bridge2FarMenuScreen(this);
        setScreen(menuScreen);
        if (previous != null) {
            previous.dispose();
        }
    }

    @Override
    public void dispose() {
        Screen current = getScreen();
        if (current != null) {
            current.dispose();
        }
        if (assetManager != null) {
            assetManager.dispose();
        }
    }

    private void applyResolutionPreset() {
        Preset preset = DEFAULT_RESOLUTION;
        String override = System.getProperty("bridge2far.resolution");
        if (override != null) {
            for (Preset candidate : Preset.values()) {
                if (candidate.name().equalsIgnoreCase(override)) {
                    preset = candidate;
                    break;
                }
            }
        }
        DesignResolution.applyPreset(preset);
    }
}
