package com.pimpedpixel.games.systems.gameplay;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.dongbat.jbump.World;
import com.pimpedpixel.games.Bridge2FarGame;
import com.pimpedpixel.games.DesignResolution;
import com.pimpedpixel.games.gameplay.*;
import com.pimpedpixel.games.systems.characters.JbumpMapInitializationSystem;

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

    // Character dimensions for Harry (needed for Jbump collider setup)
    private final float harryOffsetX;
    private final float harryWidth;
    private final float harryHeight;

    // Systems that need to be notified of level changes
    private HarryLevelStartSystem levelStartSystem;
    private HarryDeathSystem deathSystem;
    private JbumpMapInitializationSystem jbumpMapInitSystem;

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

    @Override
    protected void processSystem() {
        System.out.println("Ok?");
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
        }

        if (deathSystem != null) {
            deathSystem.setCurrentLevelIndex(levelIndex);
        }

        // Load the TMX map for this level
        String mapName = "bridgefall_" + (levelIndex + 1); // Levels are 1-indexed in filenames
        TiledMap newTileMap = loadBridgeFallMap(mapName);

        if (newTileMap == null) {
            Gdx.app.error("LevelLoadingSystem", "Failed to load tilemap for level " + levelIndex);
            return;
        }

        // Clean up old Jbump world
        cleanupCurrentLevel();

        // Store new tilemap
        this.currentTileMap = newTileMap;

        // Reinitialize Jbump world for the new level
        initializeJbumpWorld(newTileMap);

        // Apply scenario-specific modifications to the tilemap
        applyScenarioModifications(newTileMap, scenarioIndex);

        // Notify systems of the level change
        notifySystemsOfLevelChange();

        System.out.println("Level " + levelIndex + ", scenario " + scenarioIndex + " loaded successfully");
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
        for (int x = 0; x < cellStates.size(); x++) {
            if (x >= groundTileLayer.getWidth()) break;

            int cellState = cellStates.get(x);
            CollisionType collisionType = CollisionType.fromValue(cellState);

            if (collisionType == CollisionType.HOLE) {
                groundTileLayer.setCell(x, actualRow, null);
                changesMade++;
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
