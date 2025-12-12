package com.pimpedpixel.games.systems.characters;

import com.artemis.World;
import com.badlogic.gdx.assets.AssetManager;

/**
 * Factory class for creating blood entities.
 */
public class BloodFactory {

    private final com.artemis.World artemisWorld;
    private final AssetManager assetManager;

    /**
     * Create a new BloodFactory.
     *
     * @param artemisWorld The Artemis ECS world
     * @param assetManager The asset manager for loading resources
     */
    public BloodFactory(com.artemis.World artemisWorld, AssetManager assetManager) {
        this.artemisWorld = artemisWorld;
        this.assetManager = assetManager;
    }

    /**
     * Create a new blood entity at the specified position.
     *
     * @param x The starting X position
     * @param y The starting Y position (ground level where Harry fell)
     * @return The entity ID of the created blood
     */
    public int createBlood(float x, float y) {
        int entityId = artemisWorld.create();

        // 1. TRANSFORM
        TransformComponent t = artemisWorld.edit(entityId).create(TransformComponent.class);
        t.x = x;
        t.y = y;

        // 2. ANIMATION
        BloodAnimationComponent anim = artemisWorld.edit(entityId).create(BloodAnimationComponent.class);
        BloodAnimationsFactory.initAnimations(anim, assetManager);

        return entityId;
    }
}