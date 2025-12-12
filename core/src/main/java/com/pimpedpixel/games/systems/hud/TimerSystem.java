package com.pimpedpixel.games.systems.hud;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.pimpedpixel.games.DesignResolution;
import com.pimpedpixel.games.gameplay.Level;
import com.pimpedpixel.games.gameplay.LevelLoader;
import com.pimpedpixel.games.gameplay.Scenario;
import com.pimpedpixel.games.systems.characters.HarryState;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;

/**
 * System that manages the game timer and displays it in the HUD using C64 font.
 */
public class TimerSystem extends IteratingSystem {

    private ComponentMapper<TimerComponent> timerMapper;
    private ComponentMapper<HarryStateComponent> harryStateMapper;
    private final AssetManager assetManager;
    private final Stage stage;
    private final LevelLoader.LevelContainer levelContainer;

    private Label timerLabel;
    private Label scenarioTitleLabel;
    private BitmapFont c64Font;
    private int currentEntity = -1;
    private EntitySubscription harrySubscription;

    private static final String TIMER_PREFIX = "TIME: ";
    private static final float SCENARIO_TITLE_DISPLAY_TIME = 2.0f; // 2 seconds
    private static final float SCENARIO_TITLE_FADE_TIME = 1.0f; // 1 second fade

    public TimerSystem(AssetManager assetManager,
                       Stage stage,
                       LevelLoader.LevelContainer levelContainer) {
        super(Aspect.all(TimerComponent.class));
        this.assetManager = assetManager;
        this.stage = stage;
        this.levelContainer = levelContainer;
    }

    @Override
    protected void initialize() {
        // Load C64 font
        c64Font = assetManager.get("font/c64.fnt", BitmapFont.class);

        // Create timer label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = c64Font;

        timerLabel = new Label(TIMER_PREFIX + "00:00", labelStyle);
        float timerX = 20f;
        float timerY = DesignResolution.HEIGHT - timerLabel.getHeight();
        timerLabel.setPosition(timerX, timerY);
        timerLabel.setAlignment(Align.left);

        // Create scenario title label (centered horizontally, 75% from bottom, initially invisible)
        scenarioTitleLabel = new Label("", labelStyle);
        scenarioTitleLabel.setPosition(
            (DesignResolution.WIDTH - scenarioTitleLabel.getWidth()) / 2,
            DesignResolution.HEIGHT * 0.25f  // 25% from bottom = 75% from top
        );
        scenarioTitleLabel.setAlignment(Align.center);
        scenarioTitleLabel.setVisible(false);

        // Add to stage (HUD group)
        stage.addActor(timerLabel);
        stage.addActor(scenarioTitleLabel);

        // Create timer entity
        createTimerEntity();

        // Initialize entity subscription for Harry entities
        harrySubscription = world.getAspectSubscriptionManager().get(Aspect.all(HarryStateComponent.class));
    }

    private void createTimerEntity() {
        if (levelContainer != null && levelContainer.getLevels().length > 0) {
            // Find level 1 (the starting level) to get the correct time limit
            Level level1 = null;
            for (Level level : levelContainer.getLevels()) {
                if (level.getLevelNumber() == 1) {
                    level1 = level;
                    break;
                }
            }

            // If level 1 not found, use the first level as fallback
            if (level1 == null) {
                level1 = levelContainer.getLevels()[0];
                Gdx.app.log("TimerSystem", "Level 1 not found, using first level as fallback");
            }

            // Get time limit from first scenario of the selected level
            Scenario scenario = level1.getScenarios().get(0);
            float timeLimit = scenario.getTimeLimit();

            // Create timer entity
            currentEntity = world.create();
            TimerComponent timer = world.edit(currentEntity).create(TimerComponent.class);
            timer.totalTime = timeLimit;
            timer.remainingTime = timeLimit;
            timer.start();

            Gdx.app.log("TimerSystem", "Created timer with " + timeLimit + " seconds from level " + level1.getLevelNumber());
        } else {
            Gdx.app.error("TimerSystem", "No levels found to get time limit");
        }
    }

    @Override
    protected void process(int entityId) {
        TimerComponent timer = timerMapper.get(entityId);

        if (timer == null) return;

        // Update timer
        float delta = world.getDelta();
        boolean expired = timer.update(delta);

        // Update label text
        updateTimerLabel(timer);

        if (expired) {
            Gdx.app.log("TimerSystem", "Timer expired!");

            // Find Harry's entity and set his state to DYING
            findAndKillHarry();
        }
    }

    private void updateTimerLabel(TimerComponent timer) {
        String formattedTime = timer.getFormattedTime();
        timerLabel.setText(TIMER_PREFIX + formattedTime);
    }

    /**
     * Get the current timer component.
     */
    public TimerComponent getTimer() {
        if (currentEntity != -1) {
            return timerMapper.get(currentEntity);
        }
        return null;
    }

    /**
     * Start the timer.
     */
    public void startTimer() {
        if (currentEntity != -1) {
            TimerComponent timer = timerMapper.get(currentEntity);
            timer.start();
        }
    }

    /**
     * Stop the timer.
     */
    public void stopTimer() {
        if (currentEntity != -1) {
            TimerComponent timer = timerMapper.get(currentEntity);
            timer.stop();
        }
    }

    /**
     * Reset the timer.
     */
    public void resetTimer() {
        if (currentEntity != -1) {
            TimerComponent timer = timerMapper.get(currentEntity);
            timer.reset();
            updateTimerLabel(timer);
        }
    }

    /**
     * Reset the timer and start it immediately.
     */
    public void resetAndStartTimer() {
        if (currentEntity != -1) {
            TimerComponent timer = timerMapper.get(currentEntity);
            timer.reset();
            timer.start();
            updateTimerLabel(timer);
        }
    }

    /**
     * Show the scenario title with fade-out animation.
     * @param title The title of the scenario to display
     */
    public void showScenarioTitle(String title) {
        if (title == null || title.isEmpty()) {
            Gdx.app.log("TimerSystem", "Scenario title is null or empty");
            return;
        }

        Gdx.app.log("TimerSystem", "Showing scenario title: " + title);

        // Set the title text
        scenarioTitleLabel.setText(title);

        // Position the label (centered horizontally, 75% from bottom)
        scenarioTitleLabel.setPosition(
            (DesignResolution.WIDTH - scenarioTitleLabel.getWidth()) / 2,
            DesignResolution.HEIGHT * 0.75f
        );

        // Bring to front and make sure it's visible
        scenarioTitleLabel.setVisible(true);
        scenarioTitleLabel.setZIndex(Integer.MAX_VALUE); // Ensure it's on top of other elements

        // Set color to fully opaque white
        scenarioTitleLabel.setColor(1f, 1f, 1f, 1f);

        // Create fade-out animation sequence
        // First, make it fully visible (in case it was faded before)
        scenarioTitleLabel.getColor().a = 1f;

        // Then fade out over SCENARIO_TITLE_FADE_TIME seconds, starting after a delay
        scenarioTitleLabel.addAction(
            Actions.sequence(
                Actions.delay(SCENARIO_TITLE_DISPLAY_TIME - SCENARIO_TITLE_FADE_TIME),
                Actions.fadeOut(SCENARIO_TITLE_FADE_TIME),
                Actions.run(() -> {
                    scenarioTitleLabel.setVisible(false);
                    Gdx.app.log("TimerSystem", "Scenario title faded out");
                })
            )
        );
    }

    /**
     * Find Harry's entity and set his state to DYING when timer expires.
     * Also resets the timer to give Harry a fresh start after revival.
     */
    private void findAndKillHarry() {
        // Use the entity subscription to find Harry entities
        if (harrySubscription != null && !harrySubscription.getEntities().isEmpty()) {
            // Get the first Harry entity (there should be only one)
            int harryEntityId = harrySubscription.getEntities().get(0);
            HarryStateComponent harryState = harryStateMapper.get(harryEntityId);

            if (harryState != null) {
                // Found Harry's entity, set his state to DYING
                harryState.state = HarryState.DYING;
                harryState.previousState = HarryState.RESTING; // Ensure proper state transition
                harryState.stateTime = 0f; // Reset state time for clean death sequence

                // Reset the timer to give Harry a fresh start after revival
                resetAndStartTimer();

                Gdx.app.log("TimerSystem", "Harry found and set to DYING state, timer reset");
                return;
            }
        }

        Gdx.app.error("TimerSystem", "Harry entity not found!");
    }
}
