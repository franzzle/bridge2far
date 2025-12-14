package com.pimpedpixel.games.screens;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pimpedpixel.games.Bridge2FarGame;
import com.pimpedpixel.games.DesignResolution;
import com.pimpedpixel.games.GameInfo;
import com.pimpedpixel.games.assets.AssetLoadingImpl;
import com.pimpedpixel.games.config.CharacterConfig;
import com.pimpedpixel.games.config.DebugConfig;
import com.pimpedpixel.games.gameplay.*;
import com.pimpedpixel.games.gameprogress.GameProgress;
import com.pimpedpixel.games.gameprogress.PasswordCodec;
import com.pimpedpixel.games.systems.characters.*;
import com.pimpedpixel.games.systems.debug.ZebraDebugSystem;
import com.pimpedpixel.games.systems.gameplay.*;
import com.pimpedpixel.games.systems.hud.TimerSystem;
import com.pimpedpixel.games.systems.playfield.MapBackgroundRenderSystem;
import com.pimpedpixel.games.systems.playfield.MapForegroundRenderSystem;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.pimpedpixel.games.DesignResolution.ASSET_SCALE;

public class Bridge2FarGameplayScreen implements Screen {

    private final Bridge2FarGame game;
    private final AssetManager assetManager;
    private final GameInfo gameInfo;
    private final GameProgress resumeProgress;
    private int startingLevelIndex = 0;

    private Stage stage;
    private World artemisWorld;
    private OrthographicCamera camera;
    private SpriteBatch spriteBatch;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Viewport viewport;
    private LevelLoader.LevelContainer levelContainer;

    // --- NEW: Single Jbump World for all objects ---
    private com.dongbat.jbump.World<Object> jbumpWorld;

    // Harry factory for creating Harry entities
    private HarryFactory harryFactory;

    // Zebra factory for creating Zebra entities
    private ZebraFactory zebraFactory;

    // Character dimensions for Harry (used by multiple systems)
    private float harryOffsetX = 22f * ASSET_SCALE;
    private float harryWidth = 20f;
    private float harryHeight = 64f;

    private InputMultiplexer inputMultiplexer;
    private InputAdapter gameplayInputProcessor;
    private boolean showingPasswordPopup;
    private String resumePassword = "";
    private BitmapFont passwordFont;
    private GlyphLayout glyphLayout = new GlyphLayout();
    private ShapeRenderer overlayRenderer;

    public Bridge2FarGameplayScreen(Bridge2FarGame game, GameProgress resumeProgress) {
        this.game = game;
        this.assetManager = game.getAssetManager();
        this.gameInfo = game.getGameInfo();
        this.resumeProgress = resumeProgress;
        initializeGameWorld();
    }

    @Override
    public void show() {
        if (resumeProgress != null) {
            Gdx.app.log("Bridge2FarGameplayScreen", "Resume progress applied: " + resumeProgress);
        }
        if (inputMultiplexer != null) {
            Gdx.input.setInputProcessor(inputMultiplexer);
        }
    }

    private void initializeGameWorld() {
        // --- Assets ---
        AssetLoadingImpl assetLoading = new AssetLoadingImpl(assetManager, gameInfo);
        assetLoading.start();
        assetLoading.ready();

        // --- Level Loading ---
        try {
            levelContainer = assetManager.get("gameplay/levelInfo.json", LevelLoader.LevelContainer.class);
            if (levelContainer != null) {
                System.out.println("Loaded " + levelContainer.getLevels().length + " levels");
            }
        } catch (Exception e) {
            System.err.println("Failed to load levels: " + e.getMessage());
            e.printStackTrace();
        }
        startingLevelIndex = determineStartingLevelIndex();

        // --- Rendering basics ---
        spriteBatch = new SpriteBatch();

        // Use design resolution for camera/viewport
        camera = new OrthographicCamera(DesignResolution.getWidth(), DesignResolution.getHeight());
        viewport = new FitViewport(DesignResolution.getWidth(), DesignResolution.getHeight(), camera);
        viewport.setScreenSize(DesignResolution.getWidth(), DesignResolution.getHeight());

        stage = new Stage(viewport, spriteBatch);
        overlayRenderer = new ShapeRenderer();
        passwordFont = assetManager.get("font/c64.fnt", BitmapFont.class);
        passwordFont.getData().setScale(DesignResolution.getFontScale());
        setupInputProcessing();

        // --- Load tilemap & create renderer BEFORE world config ---
        int currentLevelNumber = 1; // Default to level 1
        Level currentLevel = null;
        Scenario currentScenario = null;

        if (levelContainer != null && levelContainer.getLevels().length > 0) {
            currentLevel = levelContainer.getLevels()[startingLevelIndex];
            currentLevelNumber = currentLevel.getLevelNumber();

            if (!currentLevel.getScenarios().isEmpty()) {
                currentScenario = currentLevel.getScenarios().get(0);
            }
        }

        final TiledMap tileMap = loadBridgeFallMap(currentLevelNumber);
        if (tileMap == null) {
            Gdx.app.error("BridgeFallGame", "Failed to load bridgefall_" + currentLevelNumber + " TMX");
            return; // Exit on failure
        }

        // Modify the tilemap based on scenario data before creating systems
        if (currentScenario != null) {
            modifyTileMapBasedOnScenario(tileMap, currentScenario);
        }

        mapRenderer = new OrthogonalTiledMapRenderer(tileMap, ASSET_SCALE, spriteBatch);

        // --- Character config (ensure dimensions/offsets are set before systems use them) ---
        CharacterConfig.CharacterData harryData = CharacterConfig.getInstance().getCharacterByName("harry");
        CharacterConfig.CharacterData zebraData = CharacterConfig.getInstance().getCharacterByName("zebra");
        if (harryData != null) {
            this.harryOffsetX = harryData.getScaledHorizontalOffset(ASSET_SCALE);
            this.harryWidth = harryData.getWidth();
            this.harryHeight = harryData.getHeight();
            System.out.println("Preloaded Harry config: " + harryData.getWidth() + "x" + harryData.getHeight() + " offset=" + harryOffsetX);
        } else {
            System.err.println("Harry character data not found in CharacterConfig, using defaults");
        }

        // --- NEW: Initialize the single Jbump World ---
        jbumpWorld = new com.dongbat.jbump.World<>();

        // --- ECS world config ---
        // Create a Set to hold all systems
        Set<BaseSystem> systemSet = new LinkedHashSet<>();

        // 1. Background layers
        systemSet.add(new MapBackgroundRenderSystem(
            mapRenderer,
            camera,
            "platform"
        ));

        // 2. Jbump World Initialization (MUST run first to populate collision geometry)
        // Use the single jbumpWorld instance
        systemSet.add(new JbumpMapInitializationSystem(tileMap, jbumpWorld, "ground"));

        // 3. Character movement & rendering
        // Use the single jbumpWorld instance
        LevelLoader.SystemDefaults systemDefaults = levelContainer != null ? levelContainer.getSystemdefaults() : null;
        systemSet.add(new CharacterMovementSystem(jbumpWorld, systemDefaults));
        systemSet.add(new JbumpActionSyncSystem(jbumpWorld)); // Sync jbump colliders for action-based movement (zebras)
        systemSet.add(new HarryDeathSequenceSystem(jbumpWorld));
        systemSet.add(new HarryDeathSystem(jbumpWorld));
        systemSet.add(new ActionSystem());
        systemSet.add(new ZebraStateSystem(jbumpWorld));
        systemSet.add(new RewardCollisionSystem(jbumpWorld, tileMap, "objects", harryOffsetX, harryWidth, harryHeight, ASSET_SCALE)); // Check reward collisions
        systemSet.add(new LevelProgressionSystem(levelContainer, jbumpWorld, harryOffsetX, harryWidth, harryHeight)); // Handle level progression when treasure found
        systemSet.add(new BloodRenderSystem(spriteBatch, camera)); // Draw blood first (behind characters)
        systemSet.add(new CharacterRenderSystem(spriteBatch, camera));

        // Debug system for zebra movement (disabled by default)
        ZebraDebugSystem zebraDebugSystem = new ZebraDebugSystem(jbumpWorld);
        systemSet.add(zebraDebugSystem);
        // Uncomment to enable debug logging:
        // zebraDebugSystem.setDebugEnabled(true);

        // 4. Foreground layers
        systemSet.add(new MapForegroundRenderSystem(
            mapRenderer,
            camera,
            "beams", "bridge", "rewards"
        ));
        if(DebugConfig.getInstance().isHidegroundlayer()){
            systemSet.add(new CollisionDebugRenderSystem(tileMap, "ground"));
        }
        if(DebugConfig.getInstance().isBoundingboxes()){
            systemSet.add(new JbumpDebugRenderSystem(jbumpWorld, new ShapeRenderer(), camera));
        }
        systemSet.add(new HarryJumpSoundSystem());
        systemSet.add(new ZebraShreddingSoundSystem());
        SoundManager soundManager = new SoundManager(assetManager);
        systemSet.add(new StopSoundSystem(soundManager));
        systemSet.add(new SoundSystem(soundManager));

        // 5. HUD Systems (must run after core systems)
        systemSet.add(new TimerSystem(assetManager, stage, levelContainer));

        // 6. Gameplay Systems
        systemSet.add(new HarryLevelStartSystem(levelContainer));
        systemSet.add(new LevelLoadingSystem(jbumpWorld, levelContainer, harryOffsetX, harryWidth, harryHeight));
        systemSet.add(new StateHistorySystem());

        // Convert Set to array for WorldConfigurationBuilder
        BaseSystem[] baseSystems = systemSet.toArray(new BaseSystem[0]);

        WorldConfiguration config = new WorldConfigurationBuilder()
            .with(baseSystems)
            .build();

        artemisWorld = new World(config);

        // Set up system dependencies after world creation
        setupSystemDependencies();

        // Initialize Harry factory using CharacterConfig (use preloaded values)
        if (harryData != null) {
            harryFactory = new HarryFactory(
                artemisWorld,
                jbumpWorld,
                this.harryOffsetX,
                this.harryWidth,
                this.harryHeight
            );
            System.out.println("Initialized Harry factory with CharacterConfig data: " + harryData);
        } else {
            harryFactory = new HarryFactory(artemisWorld, jbumpWorld,
                this.harryOffsetX,
                this.harryWidth,
                this.harryHeight);
        }

        // Initialize Zebra factory using CharacterConfig
        if (zebraData != null) {
            zebraFactory = new ZebraFactory(
                artemisWorld,
                jbumpWorld,
                zebraData.getScaledHorizontalOffset(ASSET_SCALE),
                zebraData.getWidth(),
                zebraData.getHeight()
            );
            System.out.println("Initialized Zebra factory with CharacterConfig data: " + zebraData);
        } else {
            System.err.println("Zebra character data not found in CharacterConfig, using default values");
            zebraFactory = new ZebraFactory(artemisWorld, jbumpWorld, 15f * ASSET_SCALE, 30f, 40f);
        }

        // Create Harry entity with position from the current level data
        float startX = 0;
        float startY = 700f;

        if (currentLevel != null && !currentLevel.getScenarios().isEmpty()) {
            Scenario scenario = currentLevel.getScenarios().get(0);
            startX = scenario.getStartingPositionX();
            startY = scenario.getStartingPositionY();
            System.out.println("Using level " + currentLevel.getLevelNumber() + " start position: (" + startX + ", " + startY + ")");
        } else if (levelContainer != null && levelContainer.getLevels().length > 0) {
            Level fallbackLevel = levelContainer.getLevels()[0];
            if (!fallbackLevel.getScenarios().isEmpty()) {
                Scenario scenario = fallbackLevel.getScenarios().get(0);
                startX = scenario.getStartingPositionX();
                startY = scenario.getStartingPositionY();
            }
            System.out.println("Using fallback start position: (" + startX + ", " + startY + ")");
        } else {
            System.out.println("Using default start position: (" + startX + ", " + startY + ")");
        }

        harryFactory.createHarry(startX, startY);

        // Create a zebra entity at row 14 (base layer)
        // Tile size is 32x32, row 14 is at Y = 14 * 32 = 448 pixels
        // We position at the bottom of the tile, so add tile height: 448 + 32 = 480
        float zebraX = 400f; // Start at X position 400
        float zebraY = 120f;
        zebraFactory.createZebra(zebraX, zebraY);

        // Initialize ScenarioState for the game
        ScenarioState scenarioState = ScenarioState.getInstance();
        scenarioState.initializeLevel(startingLevelIndex);
        scenarioState.setCurrentScenarioIndex(0);
        scenarioState.resetTreasureFoundFlag();
        if (resumeProgress != null) {
            scenarioState.seedAttemptCount(startingLevelIndex, 0, resumeProgress.getAttempts());
        }
        scenarioState.printDebugState();

        // Start the first level (this will show scenario title and trigger level start logic)
        HarryLevelStartSystem levelStartSystem = artemisWorld.getSystem(HarryLevelStartSystem.class);
        if (levelStartSystem != null) {
            levelStartSystem.startLevel();
            Gdx.app.log("Bridge2FarGame", "Started first level");
        } else {
            Gdx.app.error("Bridge2FarGame", "Level start system not found");
        }

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
     * Modifies the tilemap based on scenario data.
     * For HOLE cells (0), removes the tile. For SOLID cells (1), keeps the tile.
     */
    private void modifyTileMapBasedOnScenario(TiledMap tileMap, Scenario scenario) {
        CollisionLayer groundLayer = scenario.getGroundLayer();
        if (groundLayer == null) {
            System.out.println("No groundLayer defined in scenario, skipping tilemap modification");
            return;
        }

        int matchingRow = groundLayer.getMatchingRow();
        List<Integer> cellStates = groundLayer.getCellStates();

        if (cellStates == null || cellStates.isEmpty()) {
            System.out.println("No cellStates defined in groundLayer, skipping tilemap modification");
            return;
        }

        // Get the ground layer from the tilemap
        TiledMapTileLayer groundTileLayer = (TiledMapTileLayer) tileMap.getLayers().get("ground");
        if (groundTileLayer == null) {
            System.out.println("Ground layer not found in tilemap, skipping modification");

            // Debug: List all available layers
            System.out.println("Available layers in tilemap:");
            for (int i = 0; i < tileMap.getLayers().getCount(); i++) {
                MapLayer layer = tileMap.getLayers().get(i);
                System.out.println("  Layer " + i + ": " + layer.getName() + " (type: " + layer.getClass().getSimpleName() + ")");

                // Try to find ground layer by different names
                if (layer.getName().toLowerCase().contains("ground")) {
                    System.out.println("    -> This layer might be the ground layer!");
                    if (layer instanceof TiledMapTileLayer) {
                        groundTileLayer = (TiledMapTileLayer) layer;
                        System.out.println("    -> Using this layer as ground layer");
                    }
                }
            }

            if (groundTileLayer == null) {
                return;
            }
        }

        System.out.println("Modifying tilemap ground layer based on scenario data:");
        System.out.println("Ground layer dimensions: " + groundTileLayer.getWidth() + "x" + groundTileLayer.getHeight());
        System.out.println("Matching row: " + matchingRow);
        System.out.println("Cell states count: " + cellStates.size());
        System.out.println("Cell states: " + cellStates);

        // Debug: Check if the matching row is within bounds
        if (matchingRow >= groundTileLayer.getHeight()) {
            System.out.println("ERROR: Matching row " + matchingRow + " is out of bounds! Ground layer height is " + groundTileLayer.getHeight());
            return;
        }

        // Debug: Check a few cells in the Tiled row to see if they exist
        // First calculate the actual row for debugging
        int debugRow = groundTileLayer.getHeight() - 1 - matchingRow;
        System.out.println("Checking cells in Tiled row " + matchingRow + " (LibGDX row " + debugRow + ":");
        for (int x = 0; x < Math.min(5, groundTileLayer.getWidth()); x++) {
            TiledMapTileLayer.Cell cell = groundTileLayer.getCell(x, debugRow);
            System.out.println("  Cell (" + x + ", " + debugRow + "): " + (cell == null ? "null" : "exists"));
        }

        // Debug: Try to find which rows actually have cells (using LibGDX coordinates)
        System.out.println("Scanning for non-null cells in ground layer (LibGDX coordinates):");
        boolean foundCells = false;
        for (int y = 0; y < Math.min(10, groundTileLayer.getHeight()); y++) {
            for (int x = 0; x < Math.min(5, groundTileLayer.getWidth()); x++) {
                TiledMapTileLayer.Cell cell = groundTileLayer.getCell(x, y);
                if (cell != null) {
                    int tiledY = groundTileLayer.getHeight() - 1 - y;
                    System.out.println("  Found cell at LibGDX (" + x + ", " + y + ") = Tiled (" + x + ", " + tiledY + ")");
                    foundCells = true;
                }
            }
        }

        if (!foundCells) {
            System.out.println("  No cells found in the first 10 LibGDX rows!");
        }

        // Determine the actual row to use (handle Tiled vs LibGDX coordinate system differences)
        // Tiled: Y=0 at top, Y increases downward
        // LibGDX: Y=0 at bottom, Y increases upward
        // So we need to invert the row number
        int actualRow = groundTileLayer.getHeight() - 1 - matchingRow;

        System.out.println("Tiled row " + matchingRow + " maps to LibGDX row " + actualRow);

        // Safety check for the inverted row
        if (actualRow < 0 || actualRow >= groundTileLayer.getHeight()) {
            System.out.println("ERROR: Inverted row " + actualRow + " is out of bounds! Ground layer height is " + groundTileLayer.getHeight());
            return;
        }

        // Debug: Print initial state of the row before modification
        System.out.println("Initial state of row " + actualRow + " before modification:");
        int initialHoleCount = 0;
        int initialSolidCount = 0;
        for (int x = 0; x < Math.min(10, groundTileLayer.getWidth()); x++) {
            TiledMapTileLayer.Cell initialCell = groundTileLayer.getCell(x, actualRow);
            if (initialCell == null) {
                initialHoleCount++;
                System.out.println("  Cell (" + x + ", " + actualRow + "): null (HOLE)");
            } else {
                initialSolidCount++;
                System.out.println("  Cell (" + x + ", " + actualRow + "): tile present (SOLID/FATAL)");
            }
        }
        System.out.println("Initial state: " + initialHoleCount + " holes, " + initialSolidCount + " solids");

        // Modify the tiles based on cell states
        int changesMade = 0;
        for (int x = 0; x < cellStates.size(); x++) {
            if (x >= groundTileLayer.getWidth()) {
                System.out.println("Cell state index " + x + " exceeds ground layer width " + groundTileLayer.getWidth());
                break;
            }

            int cellState = cellStates.get(x);
            CollisionType collisionType = CollisionType.fromValue(cellState);

            // Only modify cells in the matching row
            TiledMapTileLayer.Cell cell = groundTileLayer.getCell(x, actualRow);

            boolean cellChanged = false;
            if (cell != null) {
                if (collisionType == CollisionType.HOLE) {
                    // Remove the tile for HOLE
                    groundTileLayer.setCell(x, actualRow, null);
                    System.out.println("Removed tile at (" + x + ", " + actualRow + ") - HOLE");
                    cellChanged = true;
                    changesMade++;
                } else if (collisionType == CollisionType.SOLID) {
                    // Keep the tile for SOLID (do nothing, it's already there)
                    System.out.println("Kept tile at (" + x + ", " + actualRow + ") - SOLID");
                } else if (collisionType == CollisionType.FATAL) {
                    // For FATAL, we could add special handling if needed
                    // For now, treat it like SOLID
                    System.out.println("Kept tile at (" + x + ", " + actualRow + ") - FATAL (treated as SOLID)");
                }
            } else {
                // Cell is null, but we might need to add a tile for SOLID or FATAL
                if (collisionType == CollisionType.SOLID || collisionType == CollisionType.FATAL) {
                    // We need to add a tile here, but we don't have a reference to the tile
                    // This is a problem - we can't create new tiles without knowing what tile to use
                    System.out.println("Cell at (" + x + ", " + actualRow + ") is null but should be " + collisionType + " - cannot add tile without tile reference");
                } else {
                    System.out.println("Cell at (" + x + ", " + actualRow + ") is null - no action needed for HOLE");
                }
            }
        }

        // Debug: Print the final state of the modified row
        System.out.println("Final state of modified row " + actualRow + " (" + changesMade + " changes made):");
        int finalHoleCount = 0;
        int finalSolidCount = 0;
        for (int x = 0; x < Math.min(10, groundTileLayer.getWidth()); x++) {
            TiledMapTileLayer.Cell finalCell = groundTileLayer.getCell(x, actualRow);
            if (finalCell == null) {
                finalHoleCount++;
                System.out.println("  Cell (" + x + ", " + actualRow + "): null (HOLE)");
            } else {
                finalSolidCount++;
                System.out.println("  Cell (" + x + ", " + actualRow + "): tile present (SOLID/FATAL)");
            }
        }
        System.out.println("Final state: " + finalHoleCount + " holes, " + finalSolidCount + " solids");

        // Summary of changes
        System.out.println("Tilemap modification completed. " + changesMade + " cells were modified.");
        if (changesMade == 0) {
            System.out.println("WARNING: No cells were modified! This might indicate an issue with the layer or cell states.");
        }
    }

    private void setupInputProcessing() {
        gameplayInputProcessor = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (!showingPasswordPopup) {
                        showPasswordPopup();
                    }
                    return true;
                }
                if (showingPasswordPopup && keycode == Input.Keys.ENTER) {
                    exitToMenu();
                    return true;
                }
                return false;
            }
        };

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(gameplayInputProcessor);
        inputMultiplexer.addProcessor(stage);
    }

    private int determineStartingLevelIndex() {
        if (levelContainer == null || levelContainer.getLevels() == null || levelContainer.getLevels().length == 0) {
            return 0;
        }
        if (resumeProgress == null) {
            return 0;
        }
        Level[] levels = levelContainer.getLevels();
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] != null && levels[i].getLevelNumber() == resumeProgress.getLevel()) {
                return i;
            }
        }
        int fallbackIndex = resumeProgress.getLevel() - 1;
        fallbackIndex = Math.max(0, Math.min(levels.length - 1, fallbackIndex));
        return fallbackIndex;
    }

    /**
     * Set up system dependencies after world creation.
     */
    private void setupSystemDependencies() {
        // Get the systems
        HarryLevelStartSystem levelStartSystem = artemisWorld.getSystem(HarryLevelStartSystem.class);
        TimerSystem timerSystem = artemisWorld.getSystem(TimerSystem.class);
        HarryDeathSystem deathSystem = artemisWorld.getSystem(HarryDeathSystem.class);
        LevelProgressionSystem levelProgressionSystem = artemisWorld.getSystem(LevelProgressionSystem.class);
        LevelLoadingSystem levelLoadingSystem = artemisWorld.getSystem(LevelLoadingSystem.class);
        CharacterRenderSystem renderSystem = artemisWorld.getSystem(CharacterRenderSystem.class);
        BloodRenderSystem bloodRenderSystem = artemisWorld.getSystem(BloodRenderSystem.class);

        if (levelStartSystem != null) {
            if (resumeProgress != null) {
                levelStartSystem.applyResumeProgress(resumeProgress, levelContainer);
            } else {
                levelStartSystem.setCurrentLevelIndex(startingLevelIndex);
            }
        }

        if (levelStartSystem != null && timerSystem != null) {
            levelStartSystem.setTimerSystem(timerSystem);

            // Set up death system for timer reset and scenario title display on revival
            if (deathSystem != null) {
                deathSystem.setTimerSystem(timerSystem);
                deathSystem.setLevelContainer(levelContainer);
                deathSystem.setCurrentLevelIndex(startingLevelIndex);

                // Configure character data from CharacterConfig
                deathSystem.setCharacterDataFromConfig(ASSET_SCALE);

                // Set up blood factory for creating blood animations when Harry dies
                BloodFactory bloodFactory = new BloodFactory(artemisWorld, assetManager);
                deathSystem.setBloodFactory(bloodFactory);
            }

            // Set up level loading system
            if (levelLoadingSystem != null) {
                levelLoadingSystem.setLevelStartSystem(levelStartSystem);
                levelLoadingSystem.setDeathSystem(deathSystem);
                levelLoadingSystem.setAssetManager(assetManager); // Set asset manager for tilemap loading
                levelLoadingSystem.setArtemisWorld(artemisWorld); // Set Artemis world for system management

                // Get JbumpMapInitializationSystem for level loading
                JbumpMapInitializationSystem jbumpMapInitSystem = artemisWorld.getSystem(JbumpMapInitializationSystem.class);
                if (jbumpMapInitSystem != null) {
                    levelLoadingSystem.setJbumpMapInitSystem(jbumpMapInitSystem);
                }
                levelLoadingSystem.setTimerSystem(timerSystem);
            }

            // Set up level progression system
            if (levelProgressionSystem != null) {
                levelProgressionSystem.setLevelStartSystem(levelStartSystem);
                levelProgressionSystem.setDeathSystem(deathSystem);

                // Set the level loading system for level progression
                if (levelLoadingSystem != null) {
                    levelProgressionSystem.setLevelLoadingSystem(levelLoadingSystem);
                }
            }

            // Configure character render system from CharacterConfig
            if (renderSystem != null) {
                renderSystem.configureFromCharacterConfig();
            }

            Gdx.app.log("Bridge2FarGame", "Set up system dependencies successfully");
        } else {
            Gdx.app.error("Bridge2FarGame", "Failed to set up system dependencies");
        }
    }

    private void showPasswordPopup() {
        GameProgress progress = captureCurrentProgress();
        resumePassword = PasswordCodec.encode(progress);
        showingPasswordPopup = true;
        Gdx.app.log("Bridge2FarGameplayScreen", "Generated resume password: " + resumePassword + " for " + progress);
    }

    private GameProgress captureCurrentProgress() {
        ScenarioState scenarioState = ScenarioState.getInstance();
        int levelIndex = Math.max(0, scenarioState.getCurrentLevelIndex());
        int levelNumber = levelIndex + 1;
        if (levelContainer != null && levelIndex < levelContainer.getLevels().length) {
            Level level = levelContainer.getLevels()[levelIndex];
            if (level != null) {
                levelNumber = level.getLevelNumber();
            }
        }

        ScenarioState.ScenarioAttemptData attemptData = scenarioState.getCurrentScenarioAttemptData();
        int attempts = attemptData != null ? attemptData.getAttemptCount() : 0;
        attempts = Math.max(0, Math.min(100_000, attempts));

        levelNumber = Math.max(0, Math.min(255, levelNumber));
        return new GameProgress(levelNumber, attempts);
    }

    private void exitToMenu() {
        showingPasswordPopup = false;
        game.returnToMenu();
    }

    private void drawPasswordPopup() {
        if (overlayRenderer == null || passwordFont == null) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        overlayRenderer.setProjectionMatrix(stage.getCamera().combined);
        overlayRenderer.begin(ShapeRenderer.ShapeType.Filled);
        overlayRenderer.setColor(0f, 0f, 0f, 0.8f);
        overlayRenderer.rect(0f, 0f, DesignResolution.getWidth(), DesignResolution.getHeight());
        overlayRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        spriteBatch.setProjectionMatrix(stage.getCamera().combined);
        spriteBatch.begin();
        String line1 = "Use " + resumePassword + " to resume";
        String line2 = "Good luck next time!";
        glyphLayout.setText(passwordFont, line1);
        float centerY = DesignResolution.getHeight() / 2f;
        float line1X = (DesignResolution.getWidth() - glyphLayout.width) / 2f;
        float line1Y = centerY + glyphLayout.height;
        passwordFont.draw(spriteBatch, line1, line1X, line1Y);

        glyphLayout.setText(passwordFont, line2);
        float line2X = (DesignResolution.getWidth() - glyphLayout.width) / 2f;
        float line2Y = centerY - glyphLayout.height;
        passwordFont.draw(spriteBatch, line2, line2X, line2Y);
        spriteBatch.end();
    }



    @Override
    public void render(float delta) {
        if (artemisWorld == null || stage == null) {
            Gdx.app.error("Bridge2FarGameplayScreen", "Artemis world not initialized, skipping render");
            return;
        }

        Gdx.gl.glClearColor(0, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        float worldDelta = showingPasswordPopup ? 0f : delta;
        artemisWorld.setDelta(worldDelta);
        artemisWorld.process();
        stage.act(showingPasswordPopup ? 0f : delta);
        stage.draw();

        if (showingPasswordPopup) {
            drawPasswordPopup();
        }
    }

    @Override
    public void resize(int width, int height) {
         stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        if (inputMultiplexer != null && Gdx.input.getInputProcessor() == inputMultiplexer) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        artemisWorld.dispose();
        spriteBatch.dispose();
        if (overlayRenderer != null) {
            overlayRenderer.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
    }
}
