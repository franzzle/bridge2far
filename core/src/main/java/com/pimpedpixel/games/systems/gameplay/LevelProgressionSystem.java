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
                
                // Update level indices
                scenarioState.advanceToNextLevel();
                
                // Reset to first scenario for the new level (not continuing rotation)
                scenarioState.setCurrentScenarioIndex(0);
                
                if (levelStartSystem != null) {
                    levelStartSystem.setCurrentLevelIndex(nextLevelIndex);
                }
                
                if (deathSystem != null) {
                    deathSystem.setCurrentLevelIndex(nextLevelIndex);
                }
                
                // Start the new level
                if (levelStartSystem != null) {
                    levelStartSystem.startLevel();
                }
                
                // Move Harry to the new level's starting position immediately
                // Since this is an IteratingSystem, we can process the entity directly
                HarryStateComponent harryState = mHarryState.get(entityId);
                TransformComponent transformComp = mTransform.get(entityId);
                JbumpItemComponent jbumpItemComp = mJbumpItem.get(entityId);
                
                if (harryState != null && transformComp != null && jbumpItemComp != null) {
                    // Get the starting position for the new level's first scenario
                    float[] startPosition = getScenarioStartPosition(nextLevelIndex, 0);
                    float newX = startPosition[0];
                    float newY = startPosition[1];
                    
                    // Update Harry's position
                    transformComp.x = newX;
                    transformComp.y = newY;
                    
                    // Update Jbump collider position
                    jbumpWorld.update(jbumpItemComp.item, newX + harryOffsetX, newY, harryWidth, harryHeight);
                    
                    System.out.println("Moved Harry to new level " + nextLevelIndex + " starting position: (" + newX + ", " + newY + ")");
                }
                
                // Print statistics
                scenarioState.printDebugState();
            }
        }
    }
}