package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.pimpedpixel.games.gameplay.LevelLoader;
import com.pimpedpixel.games.systems.characters.HarryState;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;
import com.pimpedpixel.games.systems.hud.TimerComponent;

/**
 * System that handles level transitions and applies level start logic.
 * At the start of each level, this system will:
 * 1. Decrement the timer by a specified amount
 * 2. Set Harry's state to DYING
 */
public class LevelTransitionSystem extends IteratingSystem {

    private ComponentMapper<HarryStateComponent> mHarryState;
    private ComponentMapper<TimerComponent> mTimer;

    private final LevelLoader.LevelContainer levelContainer;
    private int currentLevelIndex = 0;
    private boolean levelTransitionPending = false;
    private float timerDecrementAmount = 5f; // Decrement timer by 5 seconds at level start

    public LevelTransitionSystem(LevelLoader.LevelContainer levelContainer) {
        super(Aspect.all(HarryStateComponent.class, TimerComponent.class));
        this.levelContainer = levelContainer;
    }

    @Override
    protected void initialize() {
        Gdx.app.log("LevelTransitionSystem", "Initialized level transition system");

        // Start with first level
        if (levelContainer != null && levelContainer.getLevels().length > 0) {
            currentLevelIndex = 0;
            Gdx.app.log("LevelTransitionSystem", "Starting with level " + (currentLevelIndex + 1));
        }
    }

    @Override
    protected void process(int entityId) {
        // Handle level transition if pending
        if (levelTransitionPending) {
            handleLevelStart(entityId);
            levelTransitionPending = false;
        }
    }

    /**
     * Trigger a level transition to the next level.
     */
    public void transitionToNextLevel() {
        if (levelContainer == null || levelContainer.getLevels().length == 0) {
            Gdx.app.log("LevelTransitionSystem", "No levels available for transition");
            return;
        }

        currentLevelIndex++;

        if (currentLevelIndex >= levelContainer.getLevels().length) {
            Gdx.app.log("LevelTransitionSystem", "All levels completed!");
            currentLevelIndex = levelContainer.getLevels().length - 1; // Stay on last level
            return;
        }

        Gdx.app.log("LevelTransitionSystem", "Transitioning to level " + (currentLevelIndex + 1));
        levelTransitionPending = true;
    }

    /**
     * Handle the start of a new level.
     * Applies timer decrement and sets Harry to DYING state.
     */
    private void handleLevelStart(int entityId) {
        HarryStateComponent harryState = mHarryState.get(entityId);
        TimerComponent timer = mTimer.get(entityId);

        if (harryState == null || timer == null) {
            Gdx.app.error("LevelTransitionSystem", "Harry or Timer component not found!");
            return;
        }

        // 1. Decrement the timer
        timer.remainingTime = Math.max(0, timer.remainingTime - timerDecrementAmount);

        // 2. Set Harry to DYING state
        harryState.state = HarryState.DYING;

        Gdx.app.log("LevelTransitionSystem",
            "Level " + (currentLevelIndex + 1) + " started: Timer decremented by " + timerDecrementAmount +
            " seconds, Harry state set to DYING");

        // Log the new timer value
        Gdx.app.log("LevelTransitionSystem",
            "Remaining time: " + timer.getFormattedTime());
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

    /**
     * Check if a level transition is pending.
     */
    public boolean isLevelTransitionPending() {
        return levelTransitionPending;
    }
}
