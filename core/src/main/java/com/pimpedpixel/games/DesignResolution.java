package com.pimpedpixel.games;

import com.badlogic.gdx.Gdx;

public class DesignResolution {
    public static boolean useDesign;
    public static int WIDTH = 1280;
    public static int HEIGHT = 800;
    public static float ASSET_SCALE = 2f;
    public static float CHARACTER_SCALE = 2f * ASSET_SCALE;

    public static int getWidth() {
        if (useDesign) {
            return WIDTH;
        } else {
            return Gdx.graphics.getWidth();
        }
    }

    public static int getHeight() {
        if (useDesign) {
            return HEIGHT;
        } else {
            return Gdx.graphics.getHeight();
        }
    }

    public static float getScaleX() {
        return (float) Gdx.graphics.getWidth() / WIDTH;
    }

    public static float getScaleY() {
        return (float) Gdx.graphics.getHeight() / HEIGHT;
    }
}
