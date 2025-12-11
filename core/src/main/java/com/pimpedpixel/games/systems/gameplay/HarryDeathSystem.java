package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.dongbat.jbump.World;
import com.pimpedpixel.games.gameplay.Level;
import com.pimpedpixel.games.gameplay.LevelLoader;
import com.pimpedpixel.games.gameplay.Scenario;
import com.pimpedpixel.games.systems.characters.HarryState;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;
import com.pimpedpixel.games.systems.characters.JbumpItemComponent;
import com.pimpedpixel.games.systems.characters.TransformComponent;
import com.pimpedpixel.games.systems.hud.TimerComponent;
import com.pimpedpixel.games.systems.hud.TimerSystem;

public class HarryDeathSystem extends IteratingSystem {
    ComponentMapper<HarryStateComponent> mHarryState;
    ComponentMapper<TransformComponent> mTransform;
    ComponentMapper<JbumpItemComponent> mJbumpItem;

    private final World<Object> jbumpWorld;
    private TimerSystem timerSystem; // Reference to timer system for resetting timer on revival
    private LevelLoader.LevelContainer levelContainer; // For accessing scenario titles
    private int currentLevelIndex = 0; // Track current level for scenario title display
    private static final float DYING_DURATION = 2.0f; // 2 seconds in DYING state
    private static final float DIED_DURATION = 1.0f;  // 1 second in DIED state before revival
    private static final float HARRY_OFFSET_X = 22f; // Same offset as in Bridge2FarGame
    private static final float HARRY_WIDTH = 20f;    // Same dimensions as in Bridge2FarGame
    private static final float HARRY_HEIGHT = 64f;   // Same dimensions as in Bridge2FarGame

    public HarryDeathSystem(World<Object> jbumpWorld) {
        super(Aspect.all(HarryStateComponent.class, TransformComponent.class, JbumpItemComponent.class));
        this.jbumpWorld = jbumpWorld;
    }

    /**
     * Set the timer system reference for resetting timer on revival.
     */
    public void setTimerSystem(TimerSystem timerSystem) {
        this.timerSystem = timerSystem;
    }

    /**
     * Set the level container for accessing scenario titles.
     */
    public void setLevelContainer(LevelLoader.LevelContainer levelContainer) {
        this.levelContainer = levelContainer;
    }

    /**
     * Update the current level index.
     */
    public void setCurrentLevelIndex(int levelIndex) {
        this.currentLevelIndex = levelIndex;
    }

    @Override
    protected void process(int entityId) {
        HarryStateComponent stateComp = mHarryState.get(entityId);
        TransformComponent transformComp = mTransform.get(entityId);
        JbumpItemComponent jbumpItemComp = mJbumpItem.get(entityId);

        // Check if Harry is in DYING state
        if (stateComp.state == HarryState.DYING) {
            // Reset stateTime when first entering DYING state
            if (stateComp.previousState != HarryState.DYING) {
                stateComp.stateTime = 0f;
            }
            
            stateComp.stateTime += world.getDelta();

            // After 2 seconds, transition to DIED state and reset position
            if (stateComp.stateTime >= DYING_DURATION) {
                stateComp.state = HarryState.DIED;
                stateComp.stateTime = 0f; // Reset state time for DIED state tracking

                // Reset Harry's position to starting position (0, 700)
                float newX = 0;
                float newY = 700f;

                transformComp.x = newX;
                transformComp.y = newY;

                // Also update the Jbump item position
                jbumpWorld.update(jbumpItemComp.item, newX + HARRY_OFFSET_X, newY, HARRY_WIDTH, HARRY_HEIGHT);
            }
            
            // Update previousState for next frame
            stateComp.previousState = stateComp.state;
        }
        // Check if Harry is in DIED state
        else if (stateComp.state == HarryState.DIED) {
            // Reset stateTime when first entering DIED state
            if (stateComp.previousState != HarryState.DIED) {
                stateComp.stateTime = 0f;
            }
            
            stateComp.stateTime += world.getDelta();

            // Keep resetting position while in DIED state
            float newX = 0;
            float newY = 700f;

            transformComp.x = newX;
            transformComp.y = newY;

            // Also update the Jbump item position
            jbumpWorld.update(jbumpItemComp.item, newX + HARRY_OFFSET_X, newY, HARRY_WIDTH, HARRY_HEIGHT);

            // After 1 second in DIED state, revive Harry to RESTING state
            if (stateComp.stateTime >= DIED_DURATION) {
                stateComp.state = HarryState.RESTING;
                stateComp.stateTime = 0f; // Reset state time
                
                // Reset timer when Harry revives and apply level start decrement
                if (timerSystem != null) {
                    timerSystem.resetAndStartTimer(); // Reset to full time and start immediately
                    
                    // Apply the same timer decrement as HarryLevelStartSystem (1 second)
                    TimerComponent timer = timerSystem.getTimer();
                    if (timer != null) {
                        float timerDecrementAmount = 1f; // Same as HarryLevelStartSystem
                        timer.remainingTime = Math.max(0, timer.remainingTime - timerDecrementAmount);
                    }
                }
                
                // Show scenario title when Harry revives
                if (timerSystem != null && levelContainer != null && currentLevelIndex >= 0 && currentLevelIndex < levelContainer.getLevels().length) {
                    Level currentLevel = levelContainer.getLevels()[currentLevelIndex];
                    if (currentLevel != null && !currentLevel.getScenarios().isEmpty()) {
                        Scenario scenario = currentLevel.getScenarios().get(0);
                        if (scenario != null && scenario.getTitle() != null && !scenario.getTitle().isEmpty()) {
                            timerSystem.showScenarioTitle(scenario.getTitle());
                        }
                    }
                }
                
                // Position is already set correctly, no need to update again
            }
            
            // Update previousState for next frame
            stateComp.previousState = stateComp.state;
        }
    }
}
