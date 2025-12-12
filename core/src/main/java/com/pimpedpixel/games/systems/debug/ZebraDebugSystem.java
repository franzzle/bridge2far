package com.pimpedpixel.games.systems.debug;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.dongbat.jbump.World;
import com.pimpedpixel.games.systems.characters.*;

/**
 * Debug system to monitor zebra movement and collision synchronization.
 * This helps verify that the zebra's jbump collider is moving correctly with the zebra.
 */
public class ZebraDebugSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<JbumpItemComponent> mJbumpItem;
    private ComponentMapper<ZebraStateComponent> mZebraState;
    private ComponentMapper<ActionComponent> mActions;

    private final World<Object> jbumpWorld;
    private boolean debugEnabled = false;

    public ZebraDebugSystem(World<Object> jbumpWorld) {
        super(Aspect.all(
            TransformComponent.class,
            JbumpItemComponent.class,
            ZebraStateComponent.class
        ).one(
            ActionComponent.class // Optional - not all zebras might have actions
        ));
        this.jbumpWorld = jbumpWorld;
    }

    /**
     * Enable or disable debug logging.
     */
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
        if (enabled) {
            System.out.println("ZebraDebugSystem: Debug logging enabled");
        }
    }

    @Override
    protected void process(int entityId) {
        if (!debugEnabled) return;

        TransformComponent transform = mTransform.get(entityId);
        JbumpItemComponent jbumpItem = mJbumpItem.get(entityId);
        ZebraStateComponent zebraState = mZebraState.get(entityId);
        ActionComponent actions = mActions.get(entityId);

        // Get current jbump collider position
        float colliderX = jbumpWorld.getRect(jbumpItem.item).x;
        float colliderY = jbumpWorld.getRect(jbumpItem.item).y;
        float colliderWidth = jbumpWorld.getRect(jbumpItem.item).w;
        float colliderHeight = jbumpWorld.getRect(jbumpItem.item).h;

        // Calculate position difference
        float dx = Math.abs(transform.x - colliderX);
        float dy = Math.abs(transform.y - colliderY);

        // Log zebra state and position information
//        System.out.printf("ZebraDebug[ID:%d]: State=%s, Dir=%s, Transform=(%.1f,%.1f), Collider=(%.1f,%.1f), " +
//                        "Diff=(%.3f,%.3f), Size=(%.1f,%.1f), HasActions=%s%n",
//            entityId, zebraState.state, zebraState.dir, transform.x, transform.y,
//            colliderX, colliderY, dx, dy, colliderWidth, colliderHeight,
//            actions != null && actions.hasActions());

        // Warn if position difference is significant
        if (dx > 1f || dy > 1f) {
            System.out.println("  WARNING: Significant position difference detected!");
        }
    }
}
