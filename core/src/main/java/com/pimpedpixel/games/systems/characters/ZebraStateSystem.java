package com.pimpedpixel.games.systems.characters;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;

/**
 * System to handle zebra state transitions (alternating between walking and grazing).
 */
public class ZebraStateSystem extends IteratingSystem {

    private ComponentMapper<ZebraStateComponent> mState;
    private ComponentMapper<PhysicsComponent> mPhysics;
    private ComponentMapper<ActionComponent> mActions;
    private ComponentMapper<TransformComponent> mTransform;

    private float stateChangeTimer = 0f;
    private float stateChangeInterval = 3f; // Change state every 3 seconds (faster for testing)

    public ZebraStateSystem() {
        super(Aspect.all(
            ZebraStateComponent.class,
            PhysicsComponent.class,
            ActionComponent.class,
            TransformComponent.class
        ));
    }

    //TODO Should be removed as zebra should be bounded by the jbumb world
    private float movementRange = 200f; // Movement range in pixels
    private float movementSpeed = 100f; // Movement speed in pixels per second (faster for testing)

    @Override
    protected void process(int entityId) {
        ZebraStateComponent state = mState.get(entityId);
        PhysicsComponent physics = mPhysics.get(entityId);
        ActionComponent actions = mActions.get(entityId);
        TransformComponent transform = mTransform.get(entityId);

        // Update state time
        state.stateTime += Gdx.graphics.getDeltaTime();

        // Handle state transitions
        stateChangeTimer += Gdx.graphics.getDeltaTime();

        // Force immediate movement for testing (remove this later)
        if (state.state == ZebraState.GRAZING && !actions.hasActions()) {
            stateChangeTimer = stateChangeInterval + 0.1f;
        }

        if (stateChangeTimer >= stateChangeInterval) {
            stateChangeTimer = 0f;

            // Clear any existing actions
            actions.clearActions();

            // Alternate between walking and grazing
            if (state.state == ZebraState.GRAZING) {
                state.state = ZebraState.WALKING;
                state.stateTime = 0f;

                // Set a random direction when starting to walk
                state.dir = Math.random() > 0.5f ? Direction.LEFT : Direction.RIGHT;

                // Calculate target position for movement
                float startX = transform.x;
                float targetX = state.dir == Direction.LEFT ?
                    startX - movementRange :
                    startX + movementRange;

                // Create a simple move-by action for testing
                float moveDuration = movementRange / movementSpeed;

                // Add the movement action to the zebra
                actions.addAction(
                    Actions.sequence(
                        Actions.moveTo(targetX, transform.y, moveDuration),
                        Actions.run(() -> {
                            state.state = ZebraState.GRAZING;
                            state.stateTime = 0f;
                        })
                    )
                );

            } else {
                state.state = ZebraState.GRAZING;
                state.stateTime = 0f;
                // No movement when grazing
            }
        }

        // Update physics velocity based on current action
        if (state.state == ZebraState.WALKING && actions.hasActions()) {
            // Get current velocity from the action system
            // The ActionSystem will handle the actual movement
            physics.vx = 0; // Action system handles movement
            physics.vy = 0;
        } else {
            physics.vx = 0;
            physics.vy = 0;
        }
    }
}
