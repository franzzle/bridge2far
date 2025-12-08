package com.pimpedpixel.games;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pimpedpixel.games.assets.AssetLoadingImpl;
import com.pimpedpixel.games.systems.characters.CharacterMovementSystem;
import com.pimpedpixel.games.systems.characters.CharacterRenderSystem;
import com.pimpedpixel.games.systems.characters.Direction;
import com.pimpedpixel.games.systems.characters.HarryAnimationComponent;
import com.pimpedpixel.games.systems.characters.HarryAnimationsFactory;
import com.pimpedpixel.games.systems.characters.HarryState;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;
import com.pimpedpixel.games.systems.characters.PhysicsComponent;
import com.pimpedpixel.games.systems.characters.TransformComponent;
import com.pimpedpixel.games.systems.gameplay.HarryJumpSoundSystem;
import com.pimpedpixel.games.systems.gameplay.SoundManager;
import com.pimpedpixel.games.systems.gameplay.SoundSystem;
import com.pimpedpixel.games.systems.playfield.MapBackgroundRenderSystem;
import com.pimpedpixel.games.systems.playfield.MapForegroundRenderSystem;

import static com.pimpedpixel.games.DesignResolution.ASSET_SCALE;

public class BridgeFallGame extends ApplicationAdapter {

    private Stage stage;
    private World artemisWorld;
    private OrthographicCamera camera;
    private SpriteBatch spriteBatch;

    private AssetManager assetManager;
    private GameInfo gameInfo;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Viewport viewport;

    @Override
    public void create() {
        // --- Assets ---
        assetManager = new AssetManager();
        gameInfo = new GameInfo();
        AssetLoadingImpl assetLoading = new AssetLoadingImpl(assetManager, gameInfo);
        assetLoading.start();
        assetLoading.ready();

        // --- Rendering basics ---
        spriteBatch = new SpriteBatch();

        // Use design resolution for camera/viewport
        camera = new OrthographicCamera(DesignResolution.WIDTH, DesignResolution.HEIGHT);
        viewport = new FitViewport(DesignResolution.WIDTH, DesignResolution.HEIGHT, camera);
        viewport.setScreenSize(DesignResolution.WIDTH, DesignResolution.HEIGHT);

        stage = new Stage(viewport, spriteBatch);

        // --- Load tilemap & create renderer BEFORE world config ---
        TiledMap tileMap = loadBridgeFallMap();
        if (tileMap == null) {
            // If something goes wrong, better to bail early
            Gdx.app.error("BridgeFallGame", "Failed to load bridgefall_1 TMX");
        }
        mapRenderer = new OrthogonalTiledMapRenderer(tileMap, ASSET_SCALE, spriteBatch);

        // --- ECS world config ---
        WorldConfiguration config = new WorldConfigurationBuilder()
            .with(
                // 1. Background layers (everything *behind* Harry)
                //    change "Background" to your actual background layer name(s) if needed.
                new MapBackgroundRenderSystem(
                    mapRenderer,
                    camera,
                    "platform" // <-- adjust if your map uses another name, or remove if not needed
                ),

                // 2. Character movement & rendering
                new CharacterMovementSystem(12 * 32f),
                new CharacterRenderSystem(spriteBatch, camera),

                // 3. Foreground layers (in front of Harry)
                new MapForegroundRenderSystem(
                    mapRenderer,
                    camera,
                    "bridge"
                ),
                new HarryJumpSoundSystem(),
                new SoundSystem(new SoundManager(assetManager))
            )
            .build();

        artemisWorld = new World(config);

        // Create Harry entity
        createHarry(0f, 0f);

        // If you use Stage input later, you can enable it:
        // Gdx.input.setInputProcessor(stage);
    }

    /**
     * Loads the "bridgefall_1" TMX via AssetManager.
     */
    private TiledMap loadBridgeFallMap() {
        final String path = gameInfo.getTmxFile("bridgefall_1").path();

        if (!assetManager.contains(path)) {
            // In case AssetLoadingImpl didn't queue it for some reason, we can load it here:
            // assetManager.load(path, TiledMap.class);
            // assetManager.finishLoadingAsset(path);
            Gdx.app.error("BridgeFallGame", "AssetManager does not contain TMX: " + path);
            return null;
        }

        // Ensure that specific asset is loaded
        assetManager.finishLoadingAsset(path);
        return assetManager.get(path, TiledMap.class);
    }

    private void createHarry(float x, float y) {
        int entityId = artemisWorld.create();

        TransformComponent t = artemisWorld.edit(entityId).create(TransformComponent.class);
        t.x = x;
        t.y = y;

        PhysicsComponent p = artemisWorld.edit(entityId).create(PhysicsComponent.class);
        p.vx = 0;
        p.vy = 0;
        p.onGround = true;

        HarryStateComponent s = artemisWorld.edit(entityId).create(HarryStateComponent.class);
        s.state = HarryState.RESTING;
        s.dir = Direction.RIGHT;
        s.stateTime = 0f;

        HarryAnimationComponent anim = artemisWorld.edit(entityId).create(HarryAnimationComponent.class);
        HarryAnimationsFactory.initAnimations(anim);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        float deltaTime = Gdx.graphics.getDeltaTime();
        artemisWorld.setDelta(deltaTime);
        artemisWorld.process();

        // If you want Stage UI on top:
        // stage.act(deltaTime);
        // stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        // If you want the stage to handle it:
        // stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        artemisWorld.dispose();
        spriteBatch.dispose();
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (assetManager != null) {
            assetManager.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
    }
}
