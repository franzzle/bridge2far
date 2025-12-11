package com.pimpedpixel.games.systems.characters;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;

/**
 * System for updating LibGDX actions on entities.
 */
public class ActionSystem extends IteratingSystem {

    private ComponentMapper<ActionComponent> mActions;
    private ComponentMapper<TransformComponent> mTransform;

    public ActionSystem() {
        super(Aspect.all(
            ActionComponent.class,
            TransformComponent.class
        ));
    }

    @Override
    protected void process(int entityId) {
        ActionComponent action = mActions.get(entityId);
        TransformComponent transform = mTransform.get(entityId);
        
        // Set the actor's position to match the entity's current transform
        action.actor.setPosition(transform.x, transform.y);
        
        // Update the actions with delta time
        action.act(Gdx.graphics.getDeltaTime());
        
        // Always update the transform from the actor's position
        // This ensures that any movement from actions is applied
        transform.x = action.actor.getX();
        transform.y = action.actor.getY();
    }
}