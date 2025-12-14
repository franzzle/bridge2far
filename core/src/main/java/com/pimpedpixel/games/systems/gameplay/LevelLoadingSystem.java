package com.pimpedpixel.games.systems.gameplay;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.dongbat.jbump.World;
import com.pimpedpixel.games.DesignResolution;
import com.pimpedpixel.games.gameplay.*;
import com.pimpedpixel.games.systems.characters.JbumpMapInitializationSystem;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;
import com.pimpedpixel.games.systems.characters.JbumpItemComponent;
import com.pimpedpixel.games.systems.characters.PhysicsComponent;
import com.pimpedpixel.games.systems.characters.TransformComponent;
import com.pimpedpixel.games.systems.characters.DisabledJbumpColliderComponent;
import com.pimpedpixel.games.systems.hud.TimerSystem;
import com.pimpedpixel.games.systems.playfield.MapBackgroundRenderSystem;
import com.pimpedpixel.games.systems.playfield.MapForegroundRenderSystem;

import java.util.*;

/**
 * System for loading and managing levels, including Jbump world and TMX map reloading.
 * Handles the complete level transition process with proper cleanup and initialization.
 */
public class LevelLoadingSystem extends BaseSystem {

    private TiledMap currentTileMap;
    private World<Object> currentJbumpWorld;
    private final com.dongbat.jbump.World<Object> jbumpWorld;
    private final LevelLoader.LevelContainer levelContainer;
    private com.badlogic.gdx.assets.AssetManager assetManager;
    private com.artemis.World artemisWorld;
    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<JbumpItemComponent> mJbumpItem;
    private ComponentMapper<HarryStateComponent> mHarryState;
    private ComponentMapper<PhysicsComponent> mPhysics;
    private ComponentMapper<DisabledJbumpColliderComponent> mDisabledCollider;

    // Character dimensions for Harry (needed for Jbump collider setup)
    private final float harryOffsetX;
    private final float harryWidth;
    private final float harryHeight;

    // Systems that need to be notified of level changes
    private HarryLevelStartSystem levelStartSystem;
    private HarryDeathSystem deathSystem;
    private JbumpMapInitializationSystem jbumpMapInitSystem;
    private TimerSystem timerSystem;

    // Current level state
    private int currentLevelIndex = 0;
    private int currentScenarioIndex = 0;

    public LevelLoadingSystem(com.dongbat.jbump.World<Object> jbumpWorld,
                           LevelLoader.LevelContainer levelContainer,
                           float harryOffsetX, float harryWidth, float harryHeight) {
        this.jbumpWorld = jbumpWorld;
        this.levelContainer = levelContainer;
        this.harryOffsetX = harryOffsetX;
        this.harryWidth = harryWidth;
        this.harryHeight = harryHeight;
    }

    /**
     * Set the asset manager for loading tilemaps
     */
    public void setAssetManager(com.badlogic.gdx.assets.AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Set the Artemis world for system management
     */
    public void setArtemisWorld(com.artemis.World artemisWorld) {
        this.artemisWorld = artemisWorld;
    }

    @Override
    protected void processSystem() {
    }

    /**
     * Set system references for level change notifications
     */
    public void setLevelStartSystem(HarryLevelStartSystem levelStartSystem) {
        this.levelStartSystem = levelStartSystem;
    }

    public void setDeathSystem(HarryDeathSystem deathSystem) {
        this.deathSystem = deathSystem;
    }

    public void setJbumpMapInitSystem(JbumpMapInitializationSystem jbumpMapInitSystem) {
        this.jbumpMapInitSystem = jbumpMapInitSystem;
    }

    public void setTimerSystem(TimerSystem timerSystem) {
        this.timerSystem = timerSystem;
    }

    /**
     * Load a level by index, including TMX map and Jbump world setup
     */
    public void loadLevel(int levelIndex) {
        loadLevel(levelIndex, 0); // Default to first scenario
    }

    /**
     * Load a level and scenario by index
     */
    public void loadLevel(int levelIndex, int scenarioIndex) {
        System.out.println("LevelLoadingSystem.loadLevel: Loading level " + levelIndex + ", scenario " + scenarioIndex);

        // Validate level index
        if (levelContainer == null || levelContainer.getLevels().length == 0) {
            Gdx.app.error("LevelLoadingSystem", "No levels available!");
            return;
        }

        if (levelIndex < 0 || levelIndex >= levelContainer.getLevels().length) {
            Gdx.app.error("LevelLoadingSystem", "Invalid level index: " + levelIndex);
            return;
        }

        // Store new level/scenario indices
        this.currentLevelIndex = levelIndex;
        this.currentScenarioIndex = scenarioIndex;

        // Update system references
        if (levelStartSystem != null) {
            levelStartSystem.setCurrentLevelIndex(levelIndex);
            levelStartSystem.setCurrentScenarioIndex(scenarioIndex);
        }

        if (deathSystem != null) {
            deathSystem.setCurrentLevelIndex(levelIndex);
        }

        // PAUSE SYSTEMS THAT DEPEND ON JBUMP WORLD DURING TRANSITION
        pauseDependentSystems();

        try {
            // Load the TMX map for this level
            int levelNumber = levelContainer.getLevels()[levelIndex].getLevelNumber();
            String mapName = "bridgefall_" + levelNumber;
            TiledMap newTileMap = loadBridgeFallMap(mapName);

            if (newTileMap == null) {
                Gdx.app.error("LevelLoadingSystem", "Failed to load tilemap for level " + levelIndex);
                return;
            }

            // Clean up old Jbump world
            cleanupCurrentLevel();

            // Store new tilemap
            this.currentTileMap = newTileMap;

            // Apply scenario-specific modifications to the tilemap BEFORE rebuilding Jbump,
            // so the collision geometry reflects the updated tiles (holes, etc.).
            applyScenarioModifications(newTileMap, scenarioIndex);

            updateSystemsForNewTileMap(newTileMap);

            // Reinitialize Jbump world for the new level based on the modified map
            initializeJbumpWorld(newTileMap);

            // Notify systems of the level change
            notifySystemsOfLevelChange();

            // Move Harry to the new scenario start position
            resetHarryToScenarioStart();

            System.out.println("Level " + levelIndex + ", scenario " + scenarioIndex + " loaded successfully");

            // Trigger level start flow (titles/timer) now that state and world are set up
            if (levelStartSystem != null) {
                levelStartSystem.startLevel();
            }

            // Reset timer to this scenario's limit
            if (timerSystem != null) {
                timerSystem.resetAndStartTimer();
            }

        } finally {
            // RESUME SYSTEMS AFTER TRANSITION COMPLETES
            resumeDependentSystems();
        }
    }

    /**
     * Pause systems that depend on the Jbump world during level transitions
     * This prevents null pointer exceptions when the Jbump world is being reinitialized
     */
    private void pauseDependentSystems() {
        System.out.println("Pausing systems that depend on Jbump world during level transition");

        if (artemisWorld == null) {
            System.out.println("Warning: Artemis world not set, cannot pause systems");
            return;
        }

        try {
            // Get all systems from the Artemis world
            // We'll pause systems that are likely to depend on Jbump world

            // Pause CharacterMovementSystem
            com.pimpedpixel.games.systems.characters.CharacterMovementSystem movementSystem =
                artemisWorld.getSystem(com.pimpedpixel.games.systems.characters.CharacterMovementSystem.class);
            if (movementSystem != null) {
                movementSystem.setEnabled(false);
                System.out.println("Paused CharacterMovementSystem");
            }

            // Pause JbumpActionSyncSystem
            com.pimpedpixel.games.systems.characters.JbumpActionSyncSystem actionSyncSystem =
                artemisWorld.getSystem(com.pimpedpixel.games.systems.characters.JbumpActionSyncSystem.class);
            if (actionSyncSystem != null) {
                actionSyncSystem.setEnabled(false);
                System.out.println("Paused JbumpActionSyncSystem");
            }

            // Pause HarryDeathSystem
            HarryDeathSystem deathSystem = artemisWorld.getSystem(HarryDeathSystem.class);
            if (deathSystem != null) {
                deathSystem.setEnabled(false);
                System.out.println("Paused HarryDeathSystem");
            }

            // Pause RewardCollisionSystem
            com.pimpedpixel.games.systems.gameplay.RewardCollisionSystem rewardSystem =
                artemisWorld.getSystem(com.pimpedpixel.games.systems.gameplay.RewardCollisionSystem.class);
            if (rewardSystem != null) {
                rewardSystem.setEnabled(false);
                System.out.println("Paused RewardCollisionSystem");
            }

            // Pause LevelProgressionSystem to prevent recursive level loading
            LevelProgressionSystem progressionSystem = artemisWorld.getSystem(LevelProgressionSystem.class);
            if (progressionSystem != null) {
                progressionSystem.setEnabled(false);
                System.out.println("Paused LevelProgressionSystem");
            }

        } catch (Exception e) {
            System.err.println("Error pausing systems: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Resume systems that were paused during level transitions
     */
    private void resumeDependentSystems() {
        System.out.println("Resuming systems after level transition completes");

        if (artemisWorld == null) {
            System.out.println("Warning: Artemis world not set, cannot resume systems");
            return;
        }

        try {
            // Resume CharacterMovementSystem
            com.pimpedpixel.games.systems.characters.CharacterMovementSystem movementSystem =
                artemisWorld.getSystem(com.pimpedpixel.games.systems.characters.CharacterMovementSystem.class);
            if (movementSystem != null) {
                movementSystem.setEnabled(true);
                System.out.println("Resumed CharacterMovementSystem");
            }

            // Resume JbumpActionSyncSystem
            com.pimpedpixel.games.systems.characters.JbumpActionSyncSystem actionSyncSystem =
                artemisWorld.getSystem(com.pimpedpixel.games.systems.characters.JbumpActionSyncSystem.class);
            if (actionSyncSystem != null) {
                actionSyncSystem.setEnabled(true);
                System.out.println("Resumed JbumpActionSyncSystem");
            }

            // Resume HarryDeathSystem
            HarryDeathSystem deathSystem = artemisWorld.getSystem(HarryDeathSystem.class);
            if (deathSystem != null) {
                deathSystem.setEnabled(true);
                System.out.println("Resumed HarryDeathSystem");
            }

            // Resume RewardCollisionSystem
            com.pimpedpixel.games.systems.gameplay.RewardCollisionSystem rewardSystem =
                artemisWorld.getSystem(com.pimpedpixel.games.systems.gameplay.RewardCollisionSystem.class);
            if (rewardSystem != null) {
                rewardSystem.setEnabled(true);
                System.out.println("Resumed RewardCollisionSystem");
            }

            // Resume LevelProgressionSystem
            LevelProgressionSystem progressionSystem = artemisWorld.getSystem(LevelProgressionSystem.class);
            if (progressionSystem != null) {
                progressionSystem.setEnabled(true);
                System.out.println("Resumed LevelProgressionSystem");
            }

        } catch (Exception e) {
            System.err.println("Error resuming systems: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clean up current level resources
     */
    private void cleanupCurrentLevel() {
        if (currentJbumpWorld != null) {
            // Clear the Jbump world (remove all items)
            currentJbumpWorld.reset();
            System.out.println("Cleared Jbump world for level cleanup");
        }

        // Tilemap will be garbage collected
        currentTileMap = null;
    }

    /**
     * Initialize Jbump world for the new level
     */
    private void initializeJbumpWorld(TiledMap tileMap) {
        if (jbumpMapInitSystem != null) {
            // Reinitialize the Jbump world with the new tilemap
            jbumpMapInitSystem.reinitializeWithNewMap(tileMap, "ground");
            System.out.println("Reinitialized Jbump world with new tilemap");
        } else {
            Gdx.app.error("LevelLoadingSystem", "JbumpMapInitializationSystem not set!");
        }
    }

    /**
     * Apply scenario-specific modifications to the tilemap
     */
    private void applyScenarioModifications(TiledMap tileMap, int scenarioIndex) {
        try {
            Level currentLevel = levelContainer.getLevels()[currentLevelIndex];
            if (currentLevel != null && !currentLevel.getScenarios().isEmpty()) {
                // Ensure scenario index is within bounds
                int safeScenarioIndex = Math.min(scenarioIndex, currentLevel.getScenarios().size() - 1);
                safeScenarioIndex = Math.max(0, safeScenarioIndex);

                Scenario scenario = currentLevel.getScenarios().get(safeScenarioIndex);
                if (scenario != null) {
                    CollisionLayer groundLayer = scenario.getGroundLayer();
                    if (groundLayer != null) {
                        modifyTileMapBasedOnScenario(tileMap, groundLayer);
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("LevelLoadingSystem", "Error applying scenario modifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Notify other systems about the level change
     */
    private void notifySystemsOfLevelChange() {
        // Update ScenarioState with new level/scenario
        ScenarioState scenarioState = ScenarioState.getInstance();
        scenarioState.initializeLevel(currentLevelIndex);
        scenarioState.setCurrentScenarioIndex(currentScenarioIndex);
        scenarioState.resetTreasureFoundFlag();

        System.out.println("Notified systems of level change to level " + currentLevelIndex + ", scenario " + currentScenarioIndex);
    }

    private void updateSystemsForNewTileMap(TiledMap newTileMap) {
        if (artemisWorld == null || newTileMap == null) {
            return;
        }

        MapBackgroundRenderSystem bg = artemisWorld.getSystem(MapBackgroundRenderSystem.class);
        if (bg != null) {
            bg.setMap(newTileMap);
        }

        MapForegroundRenderSystem fg = artemisWorld.getSystem(MapForegroundRenderSystem.class);
        if (fg != null) {
            fg.setMap(newTileMap);
        }

        RewardCollisionSystem reward = artemisWorld.getSystem(RewardCollisionSystem.class);
        if (reward != null) {
            reward.setTileMap(newTileMap);
        }
    }

    /**
     * Move Harry's transform and jbump collider to the new scenario start position.
     */
    private void resetHarryToScenarioStart() {
        if (artemisWorld == null || levelContainer == null || levelContainer.getLevels().length == 0) {
            System.out.println("LevelLoadingSystem: Cannot reset Harry position (missing world or levels)");
            return;
        }

        Level[] levels = levelContainer.getLevels();
        if (currentLevelIndex < 0 || currentLevelIndex >= levels.length) {
            System.out.println("LevelLoadingSystem: Current level index out of bounds, skipping Harry reset");
            return;
        }

        Level level = levels[currentLevelIndex];
        if (level == null || level.getScenarios().isEmpty()) {
            System.out.println("LevelLoadingSystem: No scenarios for current level, skipping Harry reset");
            return;
        }

        int safeScenarioIndex = Math.max(0, Math.min(currentScenarioIndex, level.getScenarios().size() - 1));
        Scenario scenario = level.getScenarios().get(safeScenarioIndex);
        if (scenario == null) {
            System.out.println("LevelLoadingSystem: Scenario is null, skipping Harry reset");
            return;
        }

        float startX = scenario.getStartingPositionX();
        float startY = scenario.getStartingPositionY();

        EntitySubscription harrySub = artemisWorld.getAspectSubscriptionManager().get(
            com.artemis.Aspect.all(HarryStateComponent.class, TransformComponent.class, JbumpItemComponent.class)
        );

        int[] ids = harrySub.getEntities().getData();
        int size = harrySub.getEntities().size();
        for (int i = 0; i < size; i++) {
            int entityId = ids[i];
            TransformComponent t = mTransform.get(entityId);
            JbumpItemComponent j = mJbumpItem.get(entityId);
            HarryStateComponent h = mHarryState.get(entityId);
            PhysicsComponent p = mPhysics.get(entityId);

            if (t == null || j == null || j.item == null) {
                continue;
            }

            t.x = startX;
            t.y = startY;

            if (p != null) {
                p.vx = 0;
                p.vy = 0;
                p.onGround = false;
            }

            if (mDisabledCollider != null && mDisabledCollider.has(entityId)) {
                DisabledJbumpColliderComponent disabled = mDisabledCollider.get(entityId);
                if (disabled != null && disabled.disabled) {
                    jbumpWorld.add((com.dongbat.jbump.Item) j.item, disabled.x, disabled.y, disabled.w, disabled.h);
                    mDisabledCollider.remove(entityId);
                } else {
                    mDisabledCollider.remove(entityId);
                }
            }

            jbumpWorld.update(j.item, startX + harryOffsetX, startY, harryWidth, harryHeight);

            if (h != null) {
                h.state = com.pimpedpixel.games.systems.characters.HarryState.RESTING;
                h.stateTime = 0f;
                h.justJumped = false;
            }

            System.out.println("LevelLoadingSystem: Reset Harry entity " + entityId +
                " to scenario start (" + startX + ", " + startY + ")");
            break; // Only one Harry expected
        }
    }

    /**
     * Loads the "bridgefall_X" TMX via AssetManager where X is the level number.
     */
    private TiledMap loadBridgeFallMap(String mapName) {
        try {
            System.out.println("Loading tilemap: " + mapName);

            if (assetManager == null) {
                Gdx.app.error("LevelLoadingSystem", "AssetManager not set! Cannot load tilemaps.");
                return null;
            }

            // Construct the path to the tilemap
            String path = "rooms/tiles/" + mapName + ".tmx";

            System.out.println("Looking for tilemap at path: " + path);

            // Check if the asset manager contains the tilemap
            if (!assetManager.contains(path)) {
                Gdx.app.error("LevelLoadingSystem", "Tilemap not found in asset manager: " + path);

                // Debug: List available tilemaps
                System.out.println("Available tilemaps in asset manager:");
                for (String assetName : assetManager.getAssetNames()) {
                    if (assetName.contains("bridgefall")) {
                        System.out.println("  - " + assetName);
                    }
                }

                return null;
            }

            // Load and return the tilemap
            assetManager.finishLoadingAsset(path);
            TiledMap tileMap = assetManager.get(path, TiledMap.class);

            if (tileMap == null) {
                Gdx.app.error("LevelLoadingSystem", "Failed to load tilemap from asset manager: " + path);
                return null;
            }

            System.out.println("Successfully loaded tilemap: " + mapName);
            return tileMap;

        } catch (Exception e) {
            Gdx.app.error("LevelLoadingSystem", "Failed to load tilemap " + mapName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Modify the tilemap based on scenario data (copied from Bridge2FarGame)
     */
    private void modifyTileMapBasedOnScenario(TiledMap tileMap, CollisionLayer groundLayer) {
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
            return;
        }

        // Determine the actual row to use (handle Tiled vs LibGDX coordinate system differences)
        int actualRow = groundTileLayer.getHeight() - 1 - matchingRow;

        if (actualRow < 0 || actualRow >= groundTileLayer.getHeight()) {
            System.out.println("ERROR: Row " + actualRow + " is out of bounds!");
            return;
        }

        // Modify the tiles based on cell states
        int changesMade = 0;

        // Find a template tile to use when we need to fill gaps (first non-null cell)
        TiledMapTileLayer.Cell templateCell = null;
        outer:
        for (int tx = 0; tx < groundTileLayer.getWidth(); tx++) {
            for (int ty = 0; ty < groundTileLayer.getHeight(); ty++) {
                TiledMapTileLayer.Cell candidate = groundTileLayer.getCell(tx, ty);
                if (candidate != null && candidate.getTile() != null) {
                    templateCell = candidate;
                    break outer;
                }
            }
        }

        for (int x = 0; x < cellStates.size(); x++) {
            if (x >= groundTileLayer.getWidth()) break;

            int cellState = cellStates.get(x);
            CollisionType collisionType = CollisionType.fromValue(cellState);

            if (collisionType == CollisionType.HOLE) {
                groundTileLayer.setCell(x, actualRow, null);
                changesMade++;
            } else if (collisionType == CollisionType.SOLID) {
                TiledMapTileLayer.Cell cell = groundTileLayer.getCell(x, actualRow);
                if (cell == null && templateCell != null) {
                    // Fill missing tiles to ensure collision is present where scenario says SOLID.
                    TiledMapTileLayer.Cell newCell = new TiledMapTileLayer.Cell();
                    newCell.setTile(templateCell.getTile());
                    groundTileLayer.setCell(x, actualRow, newCell);
                    changesMade++;
                }
            }
        }

        System.out.println("Tilemap modification completed. " + changesMade + " cells were modified.");
    }

    // Getters for current state
    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public int getCurrentScenarioIndex() {
        return currentScenarioIndex;
    }

    public TiledMap getCurrentTileMap() {
        return currentTileMap;
    }

    public World<Object> getCurrentJbumpWorld() {
        return currentJbumpWorld;
    }


}
