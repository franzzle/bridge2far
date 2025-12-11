package com.pimpedpixel.games.systems.characters;

import com.artemis.Component;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

/**
 * Component for handling LibGDX actions on entities.
 */
public class ActionComponent extends Component {
    public Actor actor = new Actor(); // Actor to hold actions
    
    /**
     * Add an action to this entity.
     */
    public void addAction(Action action) {
        actor.addAction(action);
    }
    
    /**
     * Clear all actions from this entity.
     */
    public void clearActions() {
        actor.clearActions();
    }
    
    /**
     * Check if this entity has any actions.
     */
    public boolean hasActions() {
        return actor.hasActions();
    }
    
    /**
     * Update the actor (should be called by a system).
     */
    public void act(float delta) {
        actor.act(delta);
    }
}