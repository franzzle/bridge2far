package com.pimpedpixel.games.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pimpedpixel.games.Bridge2FarGame;
import com.pimpedpixel.games.DesignResolution;

/**
 * Simple menu screen that displays the provided menu art, accepts cheat code input,
 * and starts the gameplay screen via Bridge2FarGame when appropriate.
 */
public class Bridge2FarMenuScreen implements Screen {

    private final Bridge2FarGame game;

    private SpriteBatch spriteBatch;
    private Texture menuTexture;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private InputAdapter inputProcessor;
    private final StringBuilder cheatCodeBuffer = new StringBuilder();

    public Bridge2FarMenuScreen(Bridge2FarGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(DesignResolution.getFontScale());
        menuTexture = new Texture(Gdx.files.internal("menu/menu.png"));

        camera = new OrthographicCamera(DesignResolution.getWidth(), DesignResolution.getHeight());
        viewport = new FitViewport(DesignResolution.getWidth(), DesignResolution.getHeight(), camera);
        viewport.setScreenSize(DesignResolution.getWidth(), DesignResolution.getHeight());
        viewport.apply(true);
        camera.position.set(DesignResolution.getWidth() / 2f, DesignResolution.getHeight() / 2f, 0f);
        camera.update();

        inputProcessor = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    startGameWithoutCheat();
                    return true;
                }
                if (keycode == Input.Keys.ENTER) {
                    startGameWithCheat(cheatCodeBuffer.toString());
                    return true;
                }
                if (keycode == Input.Keys.BACKSPACE && cheatCodeBuffer.length() > 0) {
                    cheatCodeBuffer.setLength(cheatCodeBuffer.length() - 1);
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                if (character == '\r' || character == '\n') {
                    return false;
                }
                if (character >= 32 && character < 127) {
                    cheatCodeBuffer.append(character);
                    return true;
                }
                return false;
            }
        };

        Gdx.input.setInputProcessor(inputProcessor);
    }

    private void startGameWithoutCheat() {
        game.startGameplay("");
    }

    private void startGameWithCheat(String cheatCode) {
        Gdx.app.log("CheatCode", "Entered: " + (cheatCode == null ? "" : cheatCode));
        game.startGameplay(cheatCode);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();
        float drawX = 0f;
        float drawY = 0f;
        spriteBatch.draw(menuTexture, drawX, drawY, DesignResolution.getWidth(), DesignResolution.getHeight());
        font.draw(spriteBatch, "Press SPACE to start", drawX + 40f, drawY + 80f);
        font.draw(spriteBatch, "Enter cheat code, then press ENTER", drawX + 40f, drawY + 50f);
        if (cheatCodeBuffer.length() > 0) {
            font.draw(spriteBatch, "Cheat: " + cheatCodeBuffer, drawX + 40f, drawY + 20f);
        }
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputProcessor) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        if (menuTexture != null) {
            menuTexture.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }
}
