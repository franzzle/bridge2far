package com.pimpedpixel.games;

import com.badlogic.gdx.Gdx;

public class DesignResolution {
    private static final int ASSET_BASE_WIDTH = 640;
    private static final int ASSET_BASE_HEIGHT = 400;
    private static final int FONT_BASE_WIDTH = 1280;

    public enum Preset {
        SD_640x400(640, 400),
        HD_1280x800(1280, 800);

        private final int width;
        private final int height;

        Preset(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    private static final float CHARACTER_PIXEL_RATIO = 2f;

    public static boolean useDesign = true;
    public static int WIDTH = Preset.HD_1280x800.getWidth();
    public static int HEIGHT = Preset.HD_1280x800.getHeight();
    public static float ASSET_SCALE = Math.min(WIDTH / (float) ASSET_BASE_WIDTH, HEIGHT / (float) ASSET_BASE_HEIGHT);
    public static float CHARACTER_SCALE = CHARACTER_PIXEL_RATIO * ASSET_SCALE;
    private static float fontScale = WIDTH / (float) FONT_BASE_WIDTH;

    public static void applyPreset(Preset preset) {
        configure(preset.getWidth(), preset.getHeight());
    }

    public static void configure(int width, int height) {
        WIDTH = width;
        HEIGHT = height;
        ASSET_SCALE = Math.min(WIDTH / (float) ASSET_BASE_WIDTH, HEIGHT / (float) ASSET_BASE_HEIGHT);
        CHARACTER_SCALE = CHARACTER_PIXEL_RATIO * ASSET_SCALE;
        fontScale = Math.max(0.5f, WIDTH / (float) FONT_BASE_WIDTH);
    }

    public static float getFontScale() {
        return fontScale;
    }

    public static int getWidth() {
        return useDesign ? WIDTH : Gdx.graphics.getWidth();
    }

    public static int getHeight() {
        return useDesign ? HEIGHT : Gdx.graphics.getHeight();
    }

    public static float getScaleX() {
        return (float) Gdx.graphics.getWidth() / WIDTH;
    }

    public static float getScaleY() {
        return (float) Gdx.graphics.getHeight() / HEIGHT;
    }

    static {
        applyPreset(Preset.HD_1280x800);
    }
}
