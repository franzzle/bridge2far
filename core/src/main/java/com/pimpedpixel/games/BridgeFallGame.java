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
import com.dongbat.jbump.Item;
// Import the new JbumpItemComponent
import com.pimpedpixel.games.assets.AssetLoadingImpl;
import com.pimpedpixel.games.systems.characters.*;
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

    // --- NEW: Single Jbump World for all objects ---
    private com.dongbat.jbump.World<Object> jbumpWorld;

    // Harry's bounding box size
    // TODO Get the character width and height and multiply with asset scale
    private static final float HARRY_WIDTH = 64f; // Adjust these based on your art
    private static final float HARRY_HEIGHT = 64f;

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
        final TiledMap tileMap = loadBridgeFallMap();
        if (tileMap == null) {
            Gdx.app.error("BridgeFallGame", "Failed to load bridgefall_1 TMX");
            return; // Exit on failure
        }
        mapRenderer = new OrthogonalTiledMapRenderer(tileMap, ASSET_SCALE, spriteBatch);

        // --- NEW: Initialize the single Jbump World ---
        jbumpWorld = new com.dongbat.jbump.World<>();

        // --- ECS world config ---
        WorldConfiguration config = new WorldConfigurationBuilder()
            .with(
                // 1. Background layers
                new MapBackgroundRenderSystem(
                    mapRenderer,
                    camera,
                    "platform"
                ),

                // 2. Jbump World Initialization (MUST run first to populate collision geometry)
                // Use the single jbumpWorld instance
                new JbumpMapInitializationSystem(tileMap, jbumpWorld,"collision"),

                // 3. Character movement & rendering
                // Use the single jbumpWorld instance
                new CharacterMovementSystem(jbumpWorld),
                new CharacterRenderSystem(spriteBatch, camera),

                // 4. Foreground layers
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
        createHarry(0, 700f); // Start him off the ground for testing

        // If you use Stage input later, you can enable it:
        // Gdx.input.setInputProcessor(stage);
    }

    /**
     * Loads the "bridgefall_1" TMX via AssetManager.
     */
    private TiledMap loadBridgeFallMap() {
        final String path = gameInfo.getTmxFile("bridgefall_1").path();

        if (!assetManager.contains(path)) {
            Gdx.app.error("BridgeFallGame", "AssetManager does not contain TMX: " + path);
            return null;
        }

        assetManager.finishLoadingAsset(path);
        return assetManager.get(path, TiledMap.class);
    }

    private void createHarry(float x, float y) {
        int entityId = artemisWorld.create();

        // Use the entityId as the object type for dynamic items
        Item<Integer> harryItem = new Item<>(entityId);

        // 1. TRANSFORM
        TransformComponent t = artemisWorld.edit(entityId).create(TransformComponent.class);
        t.x = x;
        t.y = y;

        // 2. PHYSICS
        PhysicsComponent p = artemisWorld.edit(entityId).create(PhysicsComponent.class);
        p.vx = 0;
        p.vy = 0;
        p.onGround = false; // Start false, collision loop will fix

        // 3. JUMP ITEM (NEW)
        // Add the component and initialize the Jbump Item at the starting position
        JbumpItemComponent j = artemisWorld.edit(entityId).create(JbumpItemComponent.class);
        j.item = harryItem;

        // Add the item to the Jbump World (x, y, width, height)
        // The type for the Item's user data is Integer, which is compatible with World<Object>
        jbumpWorld.add((Item)harryItem, x, y, HARRY_WIDTH, HARRY_HEIGHT);


        // 4. STATE
        HarryStateComponent s = artemisWorld.edit(entityId).create(HarryStateComponent.class);
        s.state = HarryState.RESTING;
        s.dir = Direction.LEFT;
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
