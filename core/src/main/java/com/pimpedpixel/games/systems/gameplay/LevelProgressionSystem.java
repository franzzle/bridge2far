package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.dongbat.jbump.World;
import com.pimpedpixel.games.gameplay.*;
import com.pimpedpixel.games.systems.characters.HarryState;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;
import com.pimpedpixel.games.systems.characters.JbumpItemComponent;
import com.pimpedpixel.games.systems.characters.TransformComponent;

/**
 * System that handles level progression when treasure is found.
 * When Harry finds treasure, this system advances to the next level or
 * wraps around to the first level if all levels are completed.
 */
public class LevelProgressionSystem extends IteratingSystem {
    
    ComponentMapper<HarryStateComponent> mHarryState;
    ComponentMapper<TransformComponent> mTransform;
    ComponentMapper<JbumpItemComponent> mJbumpItem;
    
    private final LevelLoader.LevelContainer levelContainer;
    private final World<Object> jbumpWorld;
    private final float harryOffsetX;
    private final float harryWidth;
    private final float harryHeight;
    private HarryLevelStartSystem levelStartSystem;
    private HarryDeathSystem deathSystem;
    private LevelLoadingSystem levelLoadingSystem;
    
    public LevelProgressionSystem(LevelLoader.LevelContainer levelContainer, 
                                 World<Object> jbumpWorld, 
                                 float harryOffsetX, float harryWidth, float harryHeight) {
        super(Aspect.all(HarryStateComponent.class, TransformComponent.class, JbumpItemComponent.class));
        this.levelContainer = levelContainer;
        this.jbumpWorld = jbumpWorld;
        this.harryOffsetX = harryOffsetX;
        this.harryWidth = harryWidth;
        this.harryHeight = harryHeight;
    }
    
    /**
     * Set the level start system reference
     */
    public void setLevelStartSystem(HarryLevelStartSystem levelStartSystem) {
        this.levelStartSystem = levelStartSystem;
    }
    
    /**
     * Set the death system reference
     */
    public void setDeathSystem(HarryDeathSystem deathSystem) {
        this.deathSystem = deathSystem;
    }
    
    /**
     * Set the level loading system reference
     */
    public void setLevelLoadingSystem(LevelLoadingSystem levelLoadingSystem) {
        this.levelLoadingSystem = levelLoadingSystem;
    }
    
    /**
     * Get the starting position for a given level and scenario
     */
    private float[] getScenarioStartPosition(int levelIndex, int scenarioIndex) {
        try {
            Level level = levelContainer.getLevels()[levelIndex];
            if (level != null && !level.getScenarios().isEmpty()) {
                // Ensure scenario index is within bounds
                int safeScenarioIndex = Math.min(scenarioIndex, level.getScenarios().size() - 1);
                safeScenarioIndex = Math.max(0, safeScenarioIndex);
                
                Scenario scenario = level.getScenarios().get(safeScenarioIndex);
                return new float[]{scenario.getStartingPositionX(), scenario.getStartingPositionY()};
            }
        } catch (Exception e) {
            System.err.println("Error getting scenario start position: " + e.getMessage());
        }
        return new float[]{20f, 700f}; // Default fallback position
    }
    
    @Override
    protected void process(int entityId) {
        HarryStateComponent stateComp = mHarryState.get(entityId);
        
        // Check if Harry is in RESTING state (after revival or level start)
        // This is where we check if treasure was found and handle level progression
        if (stateComp.state == HarryState.RESTING && stateComp.previousState != HarryState.RESTING) {
            System.out.println("LevelProgressionSystem: Harry is RESTING, checking for level progression...");
            checkForLevelProgression(entityId);
        }
        
        // Update previousState for next frame
        stateComp.previousState = stateComp.state;
    }
    
    /**
     * Check if treasure was found and handle level progression
     */
    private void checkForLevelProgression(int entityId) {
        ScenarioState scenarioState = ScenarioState.getInstance();
        
        System.out.println("LevelProgressionSystem.checkForLevelProgression: Checking if treasure was found...");
        System.out.println("Treasure found this scenario: " + scenarioState.isTreasureFoundThisScenario());
        
        // Check if treasure was found in the current scenario
        if (scenarioState.isTreasureFoundThisScenario()) {
            System.out.println("Treasure found! Advancing to next level...");
            
            // Get current level index
            int currentLevelIndex = scenarioState.getCurrentLevelIndex();
            
            // Check if we need to advance to next level
            if (levelContainer != null && levelContainer.getLevels().length > 0) {
                int nextLevelIndex = currentLevelIndex + 1;
                
                // Wrap around to first level if all levels are completed
                if (nextLevelIndex >= levelContainer.getLevels().length) {
                    nextLevelIndex = 0;
                    System.out.println("All levels completed! Wrapping around to level 1.");
                }
                
                // Load the new level using LevelLoadingSystem (handles everything)
                if (levelLoadingSystem != null) {
                    levelLoadingSystem.loadLevel(nextLevelIndex, 0); // Load level with first scenario
                } else {
                    // Fallback to manual updates if LevelLoadingSystem not available
                    scenarioState.advanceToNextLevel();
                    scenarioState.setCurrentScenarioIndex(0);
                    
                    if (levelStartSystem != null) {
                        levelStartSystem.setCurrentLevelIndex(nextLevelIndex);
                    }
                    
                    if (deathSystem != null) {
                        deathSystem.setCurrentLevelIndex(nextLevelIndex);
                    }
                    
                    if (levelStartSystem != null) {
                        levelStartSystem.startLevel();
                    }
                    
                    // Move Harry to the new level's starting position
                    HarryStateComponent harryState = mHarryState.get(entityId);
                    TransformComponent transformComp = mTransform.get(entityId);
                    JbumpItemComponent jbumpItemComp = mJbumpItem.get(entityId);
                    
                    if (harryState != null && transformComp != null && jbumpItemComp != null) {
                        float[] startPosition = getScenarioStartPosition(nextLevelIndex, 0);
                        float newX = startPosition[0];
                        float newY = startPosition[1];
                        
                        transformComp.x = newX;
                        transformComp.y = newY;
                        jbumpWorld.update(jbumpItemComp.item, newX + harryOffsetX, newY, harryWidth, harryHeight);
                        
                        System.out.println("Moved Harry to new level " + nextLevelIndex + " starting position: (" + newX + ", " + newY + ")");
                    }
                }
                
                // Print statistics
                scenarioState.printDebugState();
            }
        }
    }
}