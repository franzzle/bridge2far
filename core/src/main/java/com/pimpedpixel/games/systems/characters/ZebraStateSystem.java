package com.pimpedpixel.games.systems.characters;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.dongbat.jbump.World;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import com.dongbat.jbump.Collision;
import com.dongbat.jbump.Response;
import com.pimpedpixel.games.DesignResolution;
import java.util.List;

/**
 * System to handle zebra state transitions (alternating between walking and grazing).
 * Zebra moves randomly within 25-75% of available cells bounded by jbump collision blocks.
 */
public class ZebraStateSystem extends IteratingSystem {

    private ComponentMapper<ZebraStateComponent> mState;
    private ComponentMapper<PhysicsComponent> mPhysics;
    private ComponentMapper<ActionComponent> mActions;
    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<JbumpItemComponent> mJbumpItem;
    private ComponentMapper<ZebraOverrideComponent> mOverride;

    private float stateChangeTimer = 0f;
    private float stateChangeInterval = 3f; // Change state every 3 seconds (faster for testing)
    private float movementSpeed = 100f; // Movement speed in pixels per second (faster for testing)
    
    // For deterministic patrolling behavior
    private boolean initializedDirection = false;

    // Cell-based movement parameters
    private static final float CELL_WIDTH = 32f * DesignResolution.ASSET_SCALE; // 64 pixels per cell
    private static final int TOTAL_CELLS = 20; // Map is 20 cells wide
    private static final int MIN_AVAILABLE_CELLS = 14; // Minimum available cells
    
    private World<Object> jbumpWorld;

    public ZebraStateSystem(World<Object> jbumpWorld) {
        super(Aspect.all(
            ZebraStateComponent.class,
            PhysicsComponent.class,
            ActionComponent.class,
            TransformComponent.class,
            JbumpItemComponent.class
        ));
        this.jbumpWorld = jbumpWorld;
    }

    /**
     * Detects available cells that are not blocked by jbump collision blocks.
     * Returns the number of available cells and their positions.
     */
    private int[] detectAvailableCells() {
        // For now, use the minimum available cells as specified in requirements
        // In a real implementation, this would scan the jbump world for collision blocks
        return new int[MIN_AVAILABLE_CELLS];
    }

    /**
     * Calculates the movement range based on available cells.
     * Uses 25-75% of available cells as specified in requirements.
     */
    private float calculateMovementRange(int availableCells) {
        // Calculate 25-75% of available cells
        int minCells = (int) (availableCells * 0.25f);
        int maxCells = (int) (availableCells * 0.75f);
        
        // Randomly choose between min and max cells
        int cellsToUse = minCells + (int) (Math.random() * (maxCells - minCells + 1));
        
        // Convert cells to pixels
        return cellsToUse * CELL_WIDTH;
    }

    /**
     * Checks if a position would collide with jbump collision blocks using jbump's collision system.
     */
    boolean wouldCollide(float x, float y, float width, float height) {
        // Check if there are any collision items at the target position
        for (Item<Object> item : jbumpWorld.getItems()) {
            if (item.userData.equals("MAP_COLLISION") || item.userData.equals("BOUNDARY_WALL")) {
                Rect itemRect = jbumpWorld.getRect(item);
                
                // Proper AABB collision check - check if rectangles overlap
                boolean xOverlap = x < itemRect.x + itemRect.w && x + width > itemRect.x;
                boolean yOverlap = y < itemRect.y + itemRect.h && y + height > itemRect.y;
                
                if (xOverlap && yOverlap) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a movement path would collide with any jbump collision blocks.
     * This checks multiple points along the path to ensure the zebra doesn't walk through walls.
     */
    boolean wouldPathCollide(float startX, float endX, float y, float width, float height, int steps) {
        // Check collision at multiple points along the path
        float stepSize = (endX - startX) / steps;
        
        for (int i = 0; i <= steps; i++) {
            float currentX = startX + (stepSize * i);
            if (wouldCollide(currentX, y, width, height)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the maximum possible left movement without collision.
     * Uses binary search to efficiently find the farthest left position.
     */
    float findMaximumLeftMovement(float startX, float y, float width, float height, float maxRange) {
        float low = startX - maxRange; // Furthest left attempt
        float high = startX; // Current position (no movement)
        
        // Binary search to find the maximum safe left movement
        for (int i = 0; i < 10; i++) { // 10 iterations for good precision
            float mid = (low + high) / 2f;
            
            if (wouldPathCollide(startX, mid, y, width, height, 5)) {
                // Collision detected, try less movement (more to the right)
                low = mid;
            } else {
                // No collision, try more movement (more to the left)
                high = mid;
            }
        }
        
        // Return the farthest safe position found
        return high;
    }

    /**
     * Finds the maximum possible right movement without collision.
     * Uses binary search to efficiently find the farthest right position.
     */
    float findMaximumRightMovement(float startX, float y, float width, float height, float maxRange) {
        float low = startX; // Current position (no movement)
        float high = startX + maxRange; // Furthest right attempt
        
        // Binary search to find the maximum safe right movement
        for (int i = 0; i < 10; i++) { // 10 iterations for good precision
            float mid = (low + high) / 2f;
            
            if (wouldPathCollide(startX, mid, y, width, height, 5)) {
                // Collision detected, try less movement (more to the left)
                high = mid;
            } else {
                // No collision, try more movement (more to the right)
                low = mid;
            }
        }
        
        // Return the farthest safe position found
        return low;
    }

    @Override
    protected void process(int entityId) {
        ZebraStateComponent state = mState.get(entityId);
        PhysicsComponent physics = mPhysics.get(entityId);
        ActionComponent actions = mActions.get(entityId);
        TransformComponent transform = mTransform.get(entityId);
        JbumpItemComponent jbumpItem = mJbumpItem.get(entityId);

        // Update state time using Artemis delta time
        float deltaTime = world.getDelta();
        state.stateTime += deltaTime;

        if (mOverride.has(entityId) && mOverride.get(entityId).deathSequenceActive) {
            physics.vx = 0;
            physics.vy = 0;
            return;
        }

        // Handle state transitions
        stateChangeTimer += deltaTime;

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

                // Get zebra dimensions from jbump item
                float zebraWidth = jbumpWorld.getRect(jbumpItem.item).w;
                float zebraHeight = jbumpWorld.getRect(jbumpItem.item).h;

                // Initialize direction if not set (start with LEFT for deterministic behavior)
                if (!initializedDirection) {
                    state.dir = Direction.LEFT;
                    initializedDirection = true;
                }

                // Detect available cells and calculate maximum movement range
                int[] availableCells = detectAvailableCells();
                float maxMovementRange = calculateMovementRange(availableCells.length);

                // Calculate target position for movement in current direction
                float startX = transform.x;
                float targetX;
                
                // For deterministic patrolling: try to move as far as possible in current direction
                if (state.dir == Direction.LEFT) {
                    // Try to move left as far as possible
                    targetX = startX - maxMovementRange;
                    
                    // Check if the movement path would collide with walls
                    if (wouldPathCollide(startX, targetX, transform.y, zebraWidth, zebraHeight, 5)) {
                        // If collision when moving left, try to find the maximum possible left movement
                        targetX = findMaximumLeftMovement(startX, transform.y, zebraWidth, zebraHeight, maxMovementRange);
                        
                        // If we can't move left at all, reverse direction to right
                        if (targetX == startX) {
                            state.dir = Direction.RIGHT;
                            targetX = startX + maxMovementRange;
                            
                            // Check right movement
                            if (wouldPathCollide(startX, targetX, transform.y, zebraWidth, zebraHeight, 5)) {
                                targetX = findMaximumRightMovement(startX, transform.y, zebraWidth, zebraHeight, maxMovementRange);
                            }
                        }
                    }
                } else {
                    // Try to move right as far as possible
                    targetX = startX + maxMovementRange;
                    
                    // Check if the movement path would collide with walls
                    if (wouldPathCollide(startX, targetX, transform.y, zebraWidth, zebraHeight, 5)) {
                        // If collision when moving right, try to find the maximum possible right movement
                        targetX = findMaximumRightMovement(startX, transform.y, zebraWidth, zebraHeight, maxMovementRange);
                        
                        // If we can't move right at all, reverse direction to left
                        if (targetX == startX) {
                            state.dir = Direction.LEFT;
                            targetX = startX - maxMovementRange;
                            
                            // Check left movement
                            if (wouldPathCollide(startX, targetX, transform.y, zebraWidth, zebraHeight, 5)) {
                                targetX = findMaximumLeftMovement(startX, transform.y, zebraWidth, zebraHeight, maxMovementRange);
                            }
                        }
                    }
                }

                // Create a simple move-by action for testing
                float moveDistance = Math.abs(targetX - startX);
                float moveDuration = moveDistance / movementSpeed;

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
