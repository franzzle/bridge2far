package com.pimpedpixel.games.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pimpedpixel.games.Bridge2FarGame;
import com.pimpedpixel.games.DesignResolution;
import com.pimpedpixel.games.gameprogress.GameProgress;
import com.pimpedpixel.games.gameprogress.PasswordCodec;

/**
 * Simple menu screen that displays the provided menu art, accepts cheat code input,
 * and starts the gameplay screen via Bridge2FarGame when appropriate.
 */
public class Bridge2FarMenuScreen implements Screen {

    private final Bridge2FarGame game;

    private SpriteBatch spriteBatch;
    private Texture menuTexture;
    private BitmapFont font;
    private GlyphLayout glyphLayout = new GlyphLayout();
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private InputAdapter inputProcessor;
    private final StringBuilder cheatCodeBuffer = new StringBuilder();
    private String statusMessage;
    private float statusMessageTimer;
    private static final float STATUS_MESSAGE_DURATION = 3f;
    private static final float PROMPT_BLINK_INTERVAL = 0.5f;
    private float promptBlinkTimer = 0f;
    private boolean showPrompt = true;

    public Bridge2FarMenuScreen(Bridge2FarGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        spriteBatch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("font/c64.fnt"));
        font.getData().setScale(DesignResolution.getFontScale() * 0.5f);
        menuTexture = new Texture(Gdx.files.internal("menu/menu.png"));
        shapeRenderer = new ShapeRenderer();

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
        game.startGameplay(null);
    }

    private void startGameWithCheat(String cheatCode) {
        try {
            GameProgress progress = PasswordCodec.decode(cheatCode);
            Gdx.app.log("CheatCode", "Decoded progress: " + progress);
            cheatCodeBuffer.setLength(0);
            game.startGameplay(progress);
        } catch (Exception ex) {
            Gdx.app.log("CheatCode", "Invalid code: " + cheatCode + " (" + ex.getMessage() + ")");
            showStatusMessage("Invalid password!");
        }
    }

    private void showStatusMessage(String message) {
        this.statusMessage = message;
        this.statusMessageTimer = STATUS_MESSAGE_DURATION;
    }

    @Override
    public void render(float delta) {
        updateStatus(delta);

        Gdx.gl.glClearColor(0, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);
        drawCheatInputBackground();

        spriteBatch.begin();
        float drawX = 0f;
        float drawY = 0f;
        spriteBatch.draw(menuTexture, drawX, drawY, DesignResolution.getWidth(), DesignResolution.getHeight());
        if (showPrompt) {
            font.draw(spriteBatch, "Press SPACE to start", drawX + 40f, drawY + 80f);
        }
        font.draw(spriteBatch, "Enter cheat code, then press ENTER", drawX + 40f, drawY + 50f);
        drawCheatInputText();
        if (statusMessage != null) {
            font.draw(spriteBatch, statusMessage, drawX + 40f, drawY + 120f);
        }
        spriteBatch.end();
    }

    private void drawCheatInputBackground() {
        if (shapeRenderer == null) {
            return;
        }
        float panelWidth = DesignResolution.getWidth() * 0.6f;
        float panelHeight = 80f * DesignResolution.getFontScale();
        float centerX = DesignResolution.getWidth() / 2f;
        float centerY = DesignResolution.getHeight() * 0.2f; // 80% from top == 20% from bottom
        float panelX = centerX - panelWidth / 2f;
        float panelY = centerY - panelHeight / 2f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.75f);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rectLine(panelX, panelY, panelX + panelWidth, panelY, 2f);
        shapeRenderer.rectLine(panelX, panelY + panelHeight, panelX + panelWidth, panelY + panelHeight, 2f);
        shapeRenderer.rectLine(panelX, panelY, panelX, panelY + panelHeight, 2f);
        shapeRenderer.rectLine(panelX + panelWidth, panelY, panelX + panelWidth, panelY + panelHeight, 2f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawCheatInputText() {
        String label = "Cheatcode : ";
        String cheatValue = cheatCodeBuffer.length() > 0 ? cheatCodeBuffer.toString() : "";
        glyphLayout.setText(font, label);
        float labelWidth = glyphLayout.width;
        float labelHeight = glyphLayout.height;
        glyphLayout.setText(font, cheatValue);
        float valueWidth = glyphLayout.width;

        float centerX = DesignResolution.getWidth() / 2f;
        float centerY = DesignResolution.getHeight() * 0.2f;
        float totalWidth = labelWidth + valueWidth;
        float baseX = centerX - totalWidth / 2f;
        float textY = centerY + labelHeight / 2f;

        font.draw(spriteBatch, label, baseX, textY);
        font.draw(spriteBatch, cheatValue, baseX + labelWidth, textY);
    }

    private void updateStatus(float delta) {
        if (statusMessageTimer > 0f) {
            statusMessageTimer -= delta;
            if (statusMessageTimer <= 0f) {
                statusMessageTimer = 0f;
                statusMessage = null;
            }
        }
        promptBlinkTimer += delta;
        if (promptBlinkTimer >= PROMPT_BLINK_INTERVAL) {
            promptBlinkTimer -= PROMPT_BLINK_INTERVAL;
            showPrompt = !showPrompt;
        }
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
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
