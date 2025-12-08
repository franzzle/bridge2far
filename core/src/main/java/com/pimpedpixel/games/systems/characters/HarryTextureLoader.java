package com.pimpedpixel.games.systems.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Loads a texture with FF00FF (magenta) treated as transparent.
 * No scaling is done here: we keep the original pixel size and
 * scale later when rendering via DesignResolution.CHARACTER_SCALE.
 */
public final class HarryTextureLoader {

    private HarryTextureLoader() {
        // utility
    }

    public static TextureRegion loadColorKeyedRegion(String path) {
        // 1) Load original (may be RGB-only)
        Pixmap original = new Pixmap(Gdx.files.internal(path));

        // 2) Copy to RGBA so we can have an alpha channel
        Pixmap rgba = new Pixmap(original.getWidth(), original.getHeight(), Pixmap.Format.RGBA8888);
        rgba.drawPixmap(original, 0, 0);
        original.dispose();

        // 3) Replace #FF00FF with fully transparent pixels
        Color tmp = new Color();
        int width = rgba.getWidth();
        int height = rgba.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = rgba.getPixel(x, y);
                Color.rgba8888ToColor(tmp, pixel);

                // exact magenta: r=1, g=0, b=1
                if (isMagenta(tmp)) {
                    rgba.drawPixel(x, y, 0x00000000); // RGBA = 0, fully transparent
                }
            }
        }

        // 4) Upload to texture with NEAREST filtering (pixel art!)
        Texture texture = new Texture(rgba);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        rgba.dispose();

        return new TextureRegion(texture);
    }

    private static boolean isMagenta(Color c) {
        // tiny tolerance in case PNG compression does something weird
        return Math.abs(c.r - 1f) < 0.001f &&
            Math.abs(c.g - 0f) < 0.001f &&
            Math.abs(c.b - 1f) < 0.001f;
    }
}
