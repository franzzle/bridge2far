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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

// Import the new JbumpItemComponent
import com.pimpedpixel.games.assets.AssetLoadingImpl;
import com.pimpedpixel.games.systems.characters.*;
import com.pimpedpixel.games.systems.gameplay.HarryJumpSoundSystem;
import com.pimpedpixel.games.systems.gameplay.HarryDeathSystem;
import com.pimpedpixel.games.systems.gameplay.HarryLevelStartSystem;
import com.pimpedpixel.games.systems.gameplay.SoundManager;
import com.pimpedpixel.games.systems.gameplay.SoundSystem;
import com.pimpedpixel.games.systems.hud.TimerSystem;
import com.pimpedpixel.games.gameplay.*;
import com.pimpedpixel.games.systems.playfield.MapBackgroundRenderSystem;
import com.pimpedpixel.games.systems.playfield.MapForegroundRenderSystem;

import static com.pimpedpixel.games.DesignResolution.ASSET_SCALE;

public class Bridge2FarGame extends ApplicationAdapter {

    private Stage stage;
    private World artemisWorld;
    private OrthographicCamera camera;
    private SpriteBatch spriteBatch;

    private AssetManager assetManager;
    private GameInfo gameInfo;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Viewport viewport;
    private LevelLoader levelLoader;
    private LevelLoader.LevelContainer levelContainer;

    // --- NEW: Single Jbump World for all objects ---
    private com.dongbat.jbump.World<Object> jbumpWorld;

    // Harry factory for creating Harry entities
    private HarryFactory harryFactory;

    // Harry's bounding box size
    // TODO Get the character width and height and multiply with asset scale
    private static final float HARRY_WIDTH  = 20f;
    private static final float HARRY_HEIGHT = 64f;

    public static final float HARRY_OFFSET_X = 22f * ASSET_SCALE; // <- NEW OFFSET


    @Override
    public void create() {
        // --- Assets ---
        assetManager = new AssetManager();
        gameInfo = new GameInfo();
        AssetLoadingImpl assetLoading = new AssetLoadingImpl(assetManager, gameInfo);
        assetLoading.start();
        assetLoading.ready();

        // --- Level Loading ---
        levelLoader = new LevelLoader();
        try {
            levelContainer = levelLoader.loadLevels("assets/gameplay/levelInfo.json");
            System.out.println("Loaded " + levelContainer.getLevels().length + " levels");
        } catch (Exception e) {
            System.err.println("Failed to load levels: " + e.getMessage());
            e.printStackTrace();
        }

        // --- Rendering basics ---
        spriteBatch = new SpriteBatch();

        // Use design resolution for camera/viewport
        camera = new OrthographicCamera(DesignResolution.WIDTH, DesignResolution.HEIGHT);
        viewport = new FitViewport(DesignResolution.WIDTH, DesignResolution.HEIGHT, camera);
        viewport.setScreenSize(DesignResolution.WIDTH, DesignResolution.HEIGHT);

        stage = new Stage(viewport, spriteBatch);

        // --- Load tilemap & create renderer BEFORE world config ---
        int currentLevelNumber = 1; // Default to level 1
        if (levelContainer != null && levelContainer.getLevels().length > 0) {
            Level level = levelContainer.getLevels()[0]; // Get first level for now
            currentLevelNumber = level.getLevelNumber();
        }

        final TiledMap tileMap = loadBridgeFallMap(currentLevelNumber);
        if (tileMap == null) {
            Gdx.app.error("BridgeFallGame", "Failed to load bridgefall_" + currentLevelNumber + " TMX");
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
                new JbumpMapInitializationSystem(tileMap, jbumpWorld,
                    "ground"),

                // 3. Character movement & rendering
                // Use the single jbumpWorld instance
                new CharacterMovementSystem(jbumpWorld),
                new HarryDeathSystem(jbumpWorld),
                new CharacterRenderSystem(spriteBatch, camera),

                // 4. Foreground layers
                new MapForegroundRenderSystem(
                    mapRenderer,
                    camera,
                    "beams","bridge"
                ),
                new CollisionDebugRenderSystem(tileMap, "ground"),
                new JbumpDebugRenderSystem(jbumpWorld,new ShapeRenderer(), camera),
                new HarryJumpSoundSystem(),
                new SoundSystem(new SoundManager(assetManager)),

                // 5. HUD Systems (must run after core systems)
                new TimerSystem(assetManager,
                    stage,
                    levelContainer,
                    artemisWorld),
                // 6. Gameplay Systems
                new HarryLevelStartSystem(levelContainer)
            )
            .build();

        artemisWorld = new World(config);

        // Set up system dependencies after world creation
        setupSystemDependencies();

        // Initialize Harry factory
        harryFactory = new HarryFactory(artemisWorld, jbumpWorld, HARRY_OFFSET_X, HARRY_WIDTH, HARRY_HEIGHT);

        // Create Harry entity with position from level data
        float startX = 0;
        float startY = 700f;

        if (levelContainer != null && levelContainer.getLevels().length > 0) {
            Level level = levelContainer.getLevels()[0];
            if (!level.getScenarios().isEmpty()) {
                Scenario scenario = level.getScenarios().get(0);
                startX = scenario.getStartingPositionX();
                startY = scenario.getStartingPositionY();
                System.out.println("Using level start position: (" + startX + ", " + startY + ")");
            }
        } else {
            System.out.println("Using default start position: (" + startX + ", " + startY + ")");
        }

        harryFactory.createHarry(startX, startY);

        // Start the first level (this will show scenario title and trigger level start logic)
        HarryLevelStartSystem levelStartSystem = artemisWorld.getSystem(HarryLevelStartSystem.class);
        if (levelStartSystem != null) {
            levelStartSystem.startLevel();
            Gdx.app.log("Bridge2FarGame", "Started first level");
        } else {
            Gdx.app.error("Bridge2FarGame", "Level start system not found");
        }

        // If you use Stage input later, you can enable it:
        // Gdx.input.setInputProcessor(stage);
    }

    /**
     * Loads the "bridgefall_X" TMX via AssetManager where X is the level number.
     */
    private TiledMap loadBridgeFallMap(int levelNumber) {
        final String mapName = "bridgefall_" + levelNumber;
        final String path = gameInfo.getTmxFile(mapName).path();

        System.out.println("DEBUG: Trying to load level " + levelNumber + " with map name: " + mapName);
        System.out.println("DEBUG: Expected path: " + path);
        System.out.println("DEBUG: File exists: " + gameInfo.getTmxFile(mapName).exists());
        System.out.println("DEBUG: AssetManager contains path: " + assetManager.contains(path));

        if (!assetManager.contains(path)) {
            Gdx.app.error("BridgeFallGame", "AssetManager does not contain TMX: " + path);

            // List all loaded assets for debugging
            System.out.println("DEBUG: Available TMX assets in AssetManager:");
            for (String assetName : assetManager.getAssetNames()) {
                if (assetName.contains("bridgefall")) {
                    System.out.println("  - " + assetName);
                }
            }

            return null;
        }

        assetManager.finishLoadingAsset(path);
        return assetManager.get(path, TiledMap.class);
    }

    /**
     * Set up system dependencies after world creation.
     */
    private void setupSystemDependencies() {
        // Get the systems
        HarryLevelStartSystem levelStartSystem = artemisWorld.getSystem(HarryLevelStartSystem.class);
        TimerSystem timerSystem = artemisWorld.getSystem(TimerSystem.class);
        HarryDeathSystem deathSystem = artemisWorld.getSystem(HarryDeathSystem.class);

        if (levelStartSystem != null && timerSystem != null) {
            levelStartSystem.setTimerSystem(timerSystem);
            
            // Set up death system for timer reset and scenario title display on revival
            if (deathSystem != null) {
                deathSystem.setTimerSystem(timerSystem);
                deathSystem.setLevelContainer(levelContainer);
                deathSystem.setCurrentLevelIndex(0); // Start with level 1 (index 0)
            }
            
            Gdx.app.log("Bridge2FarGame", "Set up system dependencies successfully");
        } else {
            Gdx.app.error("Bridge2FarGame", "Failed to set up system dependencies");
        }
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
         stage.act(deltaTime);
         stage.draw();
    }

    @Override
    public void resize(int width, int height) {
         stage.getViewport().update(width, height, true);
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
