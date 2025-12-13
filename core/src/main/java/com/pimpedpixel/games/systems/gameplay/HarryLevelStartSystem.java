package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.pimpedpixel.games.gameplay.Level;
import com.pimpedpixel.games.gameplay.LevelLoader;
import com.pimpedpixel.games.gameplay.Scenario;
import com.pimpedpixel.games.gameplay.ScenarioState;
import com.pimpedpixel.games.systems.characters.HarryState;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;
import com.pimpedpixel.games.systems.hud.TimerComponent;
import com.pimpedpixel.games.systems.hud.TimerSystem;

/**
 * System that handles Harry-specific level start logic.
 * At the start of each level, this system will:
 * 1. Show the scenario title with fade-out animation
 * 2. Decrement the timer by a specified amount
 * 3. Set Harry's state to DYING
 */
public class HarryLevelStartSystem extends IteratingSystem {

    private ComponentMapper<HarryStateComponent> mHarryState;

    private final LevelLoader.LevelContainer levelContainer;
    private int currentLevelIndex = 0;
    private int currentScenarioIndex = 0;
    private boolean levelStartPending = false;
    private float timerDecrementAmount = 1f; // Decrement timer by 1 second at level start

    // Reference to timer system to access timer component
    private TimerSystem timerSystem;

    public HarryLevelStartSystem(LevelLoader.LevelContainer levelContainer) {
        super(Aspect.all(HarryStateComponent.class));
        this.levelContainer = levelContainer;
    }

    /**
     * Set the timer system reference after initialization.
     */
    public void setTimerSystem(TimerSystem timerSystem) {
        this.timerSystem = timerSystem;
    }
    
    /**
     * Set the current level index.
     */
    public void setCurrentLevelIndex(int levelIndex) {
        this.currentLevelIndex = levelIndex;
    }

    @Override
    protected void initialize() {
        log("Initialized Harry level start system");

        // Start with first level
        if (levelContainer != null && levelContainer.getLevels() != null && levelContainer.getLevels().length > 0) {
            currentLevelIndex = 0;
            log("Starting with level " + (currentLevelIndex + 1));
        }
    }

    @Override
    protected void process(int entityId) {
        // Handle level start if pending
        if (levelStartPending) {
            handleLevelStart(entityId);
            levelStartPending = false;
        }
    }

    /**
     * Trigger a level start for Harry.
     */
    public void startLevel() {
        if (levelContainer == null || levelContainer.getLevels() == null || levelContainer.getLevels().length == 0) {
            log("No levels available");
            return;
        }

        log("Starting level " + (currentLevelIndex + 1) + " for Harry");
        levelStartPending = true;
    }

    /**
     * Handle the start of a new level for Harry.
     * Applies timer decrement and sets Harry to DYING state.
     */
    private void handleLevelStart(int entityId) {
        HarryStateComponent harryState = mHarryState.get(entityId);

        if (harryState == null) {
            Gdx.app.error("HarryLevelStartSystem", "Harry state component not found!");
            return;
        }

        // 1. Show scenario title (if timer system is available)
        if (timerSystem != null && levelContainer != null && currentLevelIndex >= 0 && currentLevelIndex < levelContainer.getLevels().length) {
            Level currentLevel = levelContainer.getLevels()[currentLevelIndex];
            if (currentLevel != null && !currentLevel.getScenarios().isEmpty()) {
                // Use current scenario index instead of hardcoded 0
                if (currentScenarioIndex >= 0 && currentScenarioIndex < currentLevel.getScenarios().size()) {
                    Scenario scenario = currentLevel.getScenarios().get(currentScenarioIndex);
                    if (scenario != null && scenario.getTitle() != null && !scenario.getTitle().isEmpty()) {
                        log("Showing scenario title: " + scenario.getTitle());
                        timerSystem.showScenarioTitle(scenario.getTitle());
                    } else {
                        log("Scenario title is null or empty");
                    }
                } else {
                    log("Invalid scenario index: " + currentScenarioIndex);
                }
            } else {
                log("No scenarios found for level " + (currentLevelIndex + 1));
            }
        } else {
            log("Cannot show scenario title - missing dependencies");
        }

        // 2. Decrement the timer (if timer system is available)
        if (timerSystem != null) {
            TimerComponent timer = timerSystem.getTimer();
            if (timer != null) {
                float oldTime = timer.remainingTime;
                timer.remainingTime = Math.max(0, timer.remainingTime - timerDecrementAmount);
                float timeDecremented = oldTime - timer.remainingTime;

                log("Timer decremented by " + timeDecremented + " seconds (from " + oldTime + " to " + timer.remainingTime + ")");
            }
        }

        // Harry should start in RESTING state, not DYING
        // The DYING state is for when Harry actually dies during gameplay
        // harryState.state = HarryState.DYING;

        log("Level " + (currentLevelIndex + 1) + " started: Harry state remains RESTING");
    }

    /**
     * Transition to the next level.
     */
    public void transitionToNextLevel() {
        if (levelContainer == null || levelContainer.getLevels() == null || levelContainer.getLevels().length == 0) {
            log("No levels available for transition");
            return;
        }

        currentLevelIndex++;
        currentScenarioIndex = 0; // Reset to first scenario when changing levels

        if (currentLevelIndex >= levelContainer.getLevels().length) {
            log("All levels completed!");
            currentLevelIndex = levelContainer.getLevels().length - 1; // Stay on last level
        } else {
            log("Transitioned to level " + (currentLevelIndex + 1));
        }
    }

    /**
     * Transition to the next scenario in the current level.
     */
    public void transitionToNextScenario() {
        if (levelContainer == null || levelContainer.getLevels() == null || levelContainer.getLevels().length == 0) {
            log("No levels available for scenario transition");
            return;
        }

        if (currentLevelIndex < 0 || currentLevelIndex >= levelContainer.getLevels().length) {
            log("Invalid level index for scenario transition");
            return;
        }

        Level currentLevel = levelContainer.getLevels()[currentLevelIndex];
        if (currentLevel.getScenarios() == null || currentLevel.getScenarios().isEmpty()) {
            log("No scenarios available in current level");
            return;
        }

        currentScenarioIndex++;

        if (currentScenarioIndex >= currentLevel.getScenarios().size()) {
            log("All scenarios completed in level " + (currentLevelIndex + 1));
            currentScenarioIndex = currentLevel.getScenarios().size() - 1; // Stay on last scenario
        } else {
            log("Transitioned to scenario " + (currentScenarioIndex + 1) + " in level " + (currentLevelIndex + 1));
        }
    }

    /**
     * Get the current scenario index.
     */
    public int getCurrentScenarioIndex() {
        return currentScenarioIndex;
    }

    /**
     * Set the current scenario index.
     */
    public void setCurrentScenarioIndex(int scenarioIndex) {
        this.currentScenarioIndex = scenarioIndex;
    }

    /**
     * Safe logging method that works in both game and test environments.
     */
    private void log(String message) {
        if (Gdx.app != null) {
            Gdx.app.log("HarryLevelStartSystem", message);
        } else {
            System.out.println("[HarryLevelStartSystem] " + message);
        }
    }

    /**
     * Get the current level index.
     */
    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    /**
     * Set the timer decrement amount for level starts.
     */
    public void setTimerDecrementAmount(float amount) {
        this.timerDecrementAmount = amount;
    }

    /**
     * Get the timer decrement amount.
     */
    public float getTimerDecrementAmount() {
        return timerDecrementAmount;
    }
}
