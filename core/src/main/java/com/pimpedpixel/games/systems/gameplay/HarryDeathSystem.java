package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.dongbat.jbump.World;
import com.pimpedpixel.games.config.CharacterConfig;
import com.pimpedpixel.games.gameplay.Level;
import com.pimpedpixel.games.gameplay.LevelLoader;
import com.pimpedpixel.games.gameplay.Scenario;
import com.pimpedpixel.games.systems.characters.BloodFactory;
import com.pimpedpixel.games.systems.characters.DisabledJbumpColliderComponent;
import com.pimpedpixel.games.systems.characters.Direction;
import com.pimpedpixel.games.systems.characters.HarryState;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;
import com.pimpedpixel.games.systems.characters.JbumpItemComponent;
import com.pimpedpixel.games.systems.characters.TransformComponent;
import com.pimpedpixel.games.systems.hud.TimerComponent;
import com.pimpedpixel.games.systems.hud.TimerSystem;
import com.pimpedpixel.games.gameplay.ScenarioState;
import com.pimpedpixel.games.gameplay.Level;
import com.pimpedpixel.games.gameplay.Scenario;

public class HarryDeathSystem extends IteratingSystem {
    public static final float DEFAULT_START_POSX = 20f;
    public static final float DEFAULT_START_POSY = 700f;
    ComponentMapper<HarryStateComponent> mHarryState;
    ComponentMapper<TransformComponent> mTransform;
    ComponentMapper<JbumpItemComponent> mJbumpItem;
    ComponentMapper<HarryDeathSequenceComponent> mDeathSequence;
    ComponentMapper<DisabledJbumpColliderComponent> mDisabledCollider;

    private final World<Object> jbumpWorld;
    private TimerSystem timerSystem; // Reference to timer system for resetting timer on revival
    private LevelLoader.LevelContainer levelContainer; // For accessing scenario titles
    private int currentLevelIndex = 0; // Track current level for scenario title display
    private static final float DYING_DURATION = 2.0f; // 2 seconds in DYING state
    private static final float DIED_DURATION = 1.0f;  // 1 second in DIED state before revival

    // Blood factory for creating blood entities when Harry dies
    private BloodFactory bloodFactory;

    // Character data for Harry (loaded from CharacterConfig)
    private float harryOffsetX = 22f; // Default values
    private float harryWidth = DEFAULT_START_POSX;
    private float harryHeight = 64f;
    
    // Track Harry's orientation when he starts falling for accurate blood orientation
    private int fallingHarryEntityId = -1;
    private Direction fallingOrientation = Direction.LEFT;

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

    /**
     * Set the blood factory for creating blood entities.
     *
     * @param bloodFactory The blood factory instance
     */
    public void setBloodFactory(BloodFactory bloodFactory) {
        this.bloodFactory = bloodFactory;
    }

    /**
     * Set character data for Harry from CharacterConfig.
     *
     * @param assetScale The asset scale factor for scaling offsets
     */
    public void setCharacterDataFromConfig(float assetScale) {
        CharacterConfig.CharacterData harryData = CharacterConfig.getInstance().getCharacterByName("harry");
        if (harryData != null) {
            this.harryOffsetX = harryData.getScaledHorizontalOffset(assetScale);
            this.harryWidth = harryData.getWidth();
            this.harryHeight = harryData.getHeight();
            System.out.println("HarryDeathSystem configured with CharacterConfig data: " + harryData);
        } else {
            System.err.println("Harry character data not found in CharacterConfig for HarryDeathSystem");
        }
    }

    /**
     * Get the starting position for the current level and scenario.
     *
     * @return Array containing [startX, startY] positions, or [0, 700] if not found
     */
    private float[] getCurrentScenarioStartPosition() {
        if (levelContainer == null || levelContainer.getLevels().length == 0) {
            System.err.println("No level container available, using default start position");
            return new float[]{DEFAULT_START_POSX, DEFAULT_START_POSY};
        }

        try {
            Level currentLevel = levelContainer.getLevels()[currentLevelIndex];
            if (currentLevel == null || currentLevel.getScenarios().isEmpty()) {
                System.err.println("No scenarios found for level " + currentLevelIndex + ", using default start position");
                return new float[]{DEFAULT_START_POSX, DEFAULT_START_POSY};
            }

            // Get the current scenario index from ScenarioState
            ScenarioState scenarioState = ScenarioState.getInstance();
            int scenarioIndex = scenarioState.getCurrentScenarioIndex();
            
            // Ensure scenario index is within bounds
            if (scenarioIndex >= currentLevel.getScenarios().size()) {
                scenarioIndex = 0; // Fallback to first scenario
                scenarioState.setCurrentScenarioIndex(0);
            }

            Scenario scenario = currentLevel.getScenarios().get(scenarioIndex);
            System.out.println("Using start position from scenario " + scenarioIndex + ": (" + 
                scenario.getStartingPositionX() + ", " + scenario.getStartingPositionY() + ")");
            return new float[]{scenario.getStartingPositionX(), scenario.getStartingPositionY()};
        } catch (Exception e) {
            System.err.println("Error getting scenario start position: " + e.getMessage());
            return new float[]{DEFAULT_START_POSX, DEFAULT_START_POSY};
        }
    }

    @Override
    protected void process(int entityId) {
        HarryStateComponent stateComp = mHarryState.get(entityId);
        TransformComponent transformComp = mTransform.get(entityId);
        JbumpItemComponent jbumpItemComp = mJbumpItem.get(entityId);

        // Track Harry's orientation when he starts falling
        if (stateComp.state == HarryState.FALLING && stateComp.previousState != HarryState.FALLING) {
            // Harry just started falling - capture his orientation
            fallingHarryEntityId = entityId;
            fallingOrientation = stateComp.dir;
            System.out.println("Harry started falling with orientation: " + fallingOrientation);
        }

        // Check if Harry is in DYING state
        if (stateComp.state == HarryState.DYING) {
            // Reset stateTime when first entering DYING state
            if (stateComp.previousState != HarryState.DYING) {
                stateComp.stateTime = 0f;

                // Use the orientation from when Harry started falling, or current orientation if not tracked
                Direction bloodOrientation = (entityId == fallingHarryEntityId) ? fallingOrientation : stateComp.dir;
                
                // Create blood at Harry's current position when he starts dying unless suppressed
                if (!stateComp.suppressBloodOnDeath) {
                    if (bloodFactory != null) {
                        bloodFactory.createBlood(transformComp.x - 40, transformComp.y + 30, bloodOrientation);
                        System.out.println("Created blood at position: (" + transformComp.x + ", " + transformComp.y + ") with orientation: " + bloodOrientation);
                    } else {
                        System.err.println("Blood factory not set - cannot create blood animation");
                    }
                }
            }

            if (mDeathSequence.has(entityId)) {
                HarryDeathSequenceComponent seq = mDeathSequence.get(entityId);
                if (seq != null && !seq.done) {
                    return;
                }
            }

            stateComp.stateTime += world.getDelta();

            // After 2 seconds, transition to DIED state and reset position
            if (stateComp.stateTime >= DYING_DURATION) {
                stateComp.state = HarryState.DIED;
                stateComp.stateTime = 0f; // Reset state time for DIED state tracking

                // Reset Harry's position to the current scenario's starting position
                float[] startPosition = getCurrentScenarioStartPosition();
                float newX = startPosition[0];
                float newY = startPosition[1];

                transformComp.x = newX;
                transformComp.y = newY;

                // Also update the Jbump item position
                if (mDisabledCollider != null && mDisabledCollider.has(entityId)) {
                    DisabledJbumpColliderComponent disabled = mDisabledCollider.get(entityId);
                    if (disabled != null && disabled.disabled) {
                        disabled.x = newX + harryOffsetX;
                        disabled.y = newY;
                        disabled.w = harryWidth;
                        disabled.h = harryHeight;
                    } else {
                        jbumpWorld.update(jbumpItemComp.item, newX + harryOffsetX, newY, harryWidth, harryHeight);
                    }
                } else {
                    jbumpWorld.update(jbumpItemComp.item, newX + harryOffsetX, newY, harryWidth, harryHeight);
                }

                System.out.println("Harry resurrected at scenario start position: (" + newX + ", " + newY + ")");
            }

        }
        // Check if Harry is in DIED state
        else if (stateComp.state == HarryState.DIED) {
            // Reset stateTime when first entering DIED state
            if (stateComp.previousState != HarryState.DIED) {
                stateComp.stateTime = 0f;
            }

            stateComp.stateTime += world.getDelta();

            // Keep resetting position while in DIED state using scenario start position
            float[] startPosition = getCurrentScenarioStartPosition();
            float newX = startPosition[0];
            float newY = startPosition[1];

            transformComp.x = newX;
            transformComp.y = newY;

            // Also update the Jbump item position
            if (mDisabledCollider != null && mDisabledCollider.has(entityId)) {
                DisabledJbumpColliderComponent disabled = mDisabledCollider.get(entityId);
                if (disabled != null && disabled.disabled) {
                    disabled.x = newX + harryOffsetX;
                    disabled.y = newY;
                    disabled.w = harryWidth;
                    disabled.h = harryHeight;
                } else {
                    jbumpWorld.update(jbumpItemComp.item, newX + harryOffsetX, newY, harryWidth, harryHeight);
                }
            } else {
                jbumpWorld.update(jbumpItemComp.item, newX + harryOffsetX, newY, harryWidth, harryHeight);
            }

            // Debug log for DIED state position updates
            System.out.println("Harry in DIED state - maintaining position at scenario start: (" + newX + ", " + newY + ")");

            // After 1 second in DIED state, revive Harry to RESTING state
            if (stateComp.stateTime >= DIED_DURATION) {
                // Record death location in ScenarioState
                ScenarioState scenarioState = ScenarioState.getInstance();
                scenarioState.recordDeath(transformComp.x, transformComp.y);
                
                // Implement scenario rotation on death
                Level currentLevel = levelContainer.getLevels()[currentLevelIndex];
                if (currentLevel != null && !currentLevel.getScenarios().isEmpty()) {
                    ScenarioState.LevelScenarioData levelData = scenarioState.getCurrentLevelScenarioData();
                    if (levelData != null) {
                        
                        // Rotate to next scenario
                        int currentScenarioRotationIndex = levelData.getCurrentScenarioRotationIndex();
                        int nextScenarioIndex = (currentScenarioRotationIndex + 1) % currentLevel.getScenarios().size();
                        levelData.setCurrentScenarioRotationIndex(nextScenarioIndex);
                        
                        // Update current scenario index
                        scenarioState.setCurrentScenarioIndex(nextScenarioIndex);
                        
                        System.out.println("Rotating to next scenario: " + nextScenarioIndex + "/" + currentLevel.getScenarios().size());
                    }
                }
                
                stateComp.state = HarryState.RESTING;
                stateComp.suppressBloodOnDeath = false;
                stateComp.stateTime = 0f; // Reset state time

                if (mDisabledCollider != null && mDisabledCollider.has(entityId)) {
                    DisabledJbumpColliderComponent disabled = mDisabledCollider.get(entityId);
                    if (disabled != null && disabled.disabled) {
                        jbumpWorld.add((com.dongbat.jbump.Item) jbumpItemComp.item, disabled.x, disabled.y, disabled.w, disabled.h);
                        jbumpWorld.update(jbumpItemComp.item, disabled.x, disabled.y, disabled.w, disabled.h);
                        mDisabledCollider.remove(entityId);
                    }
                }

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

                // Show scenario title when Harry revives (use current scenario from rotation)
                if (timerSystem != null && levelContainer != null && currentLevelIndex >= 0 && currentLevelIndex < levelContainer.getLevels().length) {
                    Level level = levelContainer.getLevels()[currentLevelIndex];
                    if (level != null && !level.getScenarios().isEmpty()) {
                        int scenarioIndex = scenarioState.getCurrentScenarioIndex();
                        Scenario scenario = level.getScenarios().get(scenarioIndex);
                        if (scenario != null && scenario.getTitle() != null && !scenario.getTitle().isEmpty()) {
                            timerSystem.showScenarioTitle(scenario.getTitle());
                            System.out.println("Showing scenario title: " + scenario.getTitle());
                        }
                    }
                }

                // Position is already set correctly, no need to update again
            }

        }
    }
}
