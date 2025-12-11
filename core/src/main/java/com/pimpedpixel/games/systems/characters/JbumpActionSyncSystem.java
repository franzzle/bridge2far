package com.pimpedpixel.games.systems.characters;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.dongbat.jbump.World;
import com.dongbat.jbump.Item;

/**
 * System to synchronize Jbump colliders with entity positions for entities that move via actions.
 * This is specifically needed for zebras and other entities that use LibGDX actions for movement
 * instead of the physics-based movement system.
 */
public class JbumpActionSyncSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<JbumpItemComponent> mJbumpItem;
    private ComponentMapper<ActionComponent> mActions;
    private ComponentMapper<ZebraStateComponent> mZebraState;
    
    private final World<Object> jbumpWorld;
    
    public JbumpActionSyncSystem(World<Object> jbumpWorld) {
        super(Aspect.all(
            TransformComponent.class,
            JbumpItemComponent.class,
            ActionComponent.class
        ).one(
            ZebraStateComponent.class  // Optional - for zebra direction updates
        ));
        this.jbumpWorld = jbumpWorld;
    }
    
    @Override
    protected void process(int entityId) {
        TransformComponent transform = mTransform.get(entityId);
        JbumpItemComponent jbumpItem = mJbumpItem.get(entityId);
        ActionComponent actions = mActions.get(entityId);
        ZebraStateComponent zebraState = mZebraState.get(entityId);
        
        // Only update if the entity has actions (is moving via actions)
        if (actions.hasActions()) {
            // Get the current jbump item position
            float currentX = jbumpWorld.getRect(jbumpItem.item).x;
            float currentY = jbumpWorld.getRect(jbumpItem.item).y;
            
            // Check if the transform position has changed from the jbump position
            // This indicates that the action system has moved the entity
            if (Math.abs(transform.x - currentX) > 0.01f || Math.abs(transform.y - currentY) > 0.01f) {
                // Update the jbump collider to match the new transform position
                // We need to maintain the original width and height of the collider
                float width = jbumpWorld.getRect(jbumpItem.item).w;
                float height = jbumpWorld.getRect(jbumpItem.item).h;
                
                jbumpWorld.update(jbumpItem.item, transform.x, transform.y, width, height);
                
                // Update zebra direction based on movement (if this is a zebra)
                if (zebraState != null) {
                    // Determine direction based on movement
                    float movementDelta = transform.x - currentX;
                    if (Math.abs(movementDelta) > 0.1f) { // Significant horizontal movement
                        zebraState.dir = movementDelta > 0 ? Direction.RIGHT : Direction.LEFT;
                    }
                }
                
                // Optional: Log for debugging (can be removed in production)
                // System.out.println("JbumpActionSyncSystem: Updated collider for entity " + entityId + 
                //                   " to position (" + transform.x + ", " + transform.y + ")");
            }
        }
    }
}