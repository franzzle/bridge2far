package com.pimpedpixel.games.systems.characters;

import com.artemis.World;
import com.dongbat.jbump.Item;

/**
 * Factory class for creating Zebra entities.
 * Encapsulates all the logic for setting up Zebra with the required components.
 */
public class ZebraFactory {

    private final com.artemis.World artemisWorld;
    private final com.dongbat.jbump.World<Object> jbumpWorld;
    private final float zebraOffsetX;
    private final float zebraWidth;
    private final float zebraHeight;

    /**
     * Create a new ZebraFactory.
     *
     * @param artemisWorld The Artemis ECS world
     * @param jbumpWorld The Jbump physics world
     * @param zebraOffsetX The X offset for Zebra's collision box
     * @param zebraWidth The width of Zebra's collision box
     * @param zebraHeight The height of Zebra's collision box
     */
    public ZebraFactory(com.artemis.World artemisWorld,
                       com.dongbat.jbump.World<Object> jbumpWorld,
                       float zebraOffsetX,
                       float zebraWidth,
                       float zebraHeight) {
        this.artemisWorld = artemisWorld;
        this.jbumpWorld = jbumpWorld;
        this.zebraOffsetX = zebraOffsetX;
        this.zebraWidth = zebraWidth;
        this.zebraHeight = zebraHeight;
    }

    /**
     * Create a new Zebra entity at the specified position.
     *
     * @param startingPosX The starting X position
     * @param startingPosY The starting Y position
     * @return The entity ID of the created Zebra
     */
    public int createZebra(float startingPosX, float startingPosY) {
        int entityId = artemisWorld.create();

        // Use the entityId as the object type for dynamic items
        Item<Integer> zebraItem = new Item<>(entityId);

        // 1. TRANSFORM
        TransformComponent t = artemisWorld.edit(entityId).create(TransformComponent.class);
        t.x = startingPosX;
        t.y = startingPosY;

        // 2. PHYSICS
        PhysicsComponent p = artemisWorld.edit(entityId).create(PhysicsComponent.class);
        p.vx = 0;
        p.vy = 0;
        p.onGround = false; // Start false, collision loop will fix

        // 3. JUMP ITEM
        // Add the component and initialize the Jbump Item at the starting position
        JbumpItemComponent j = artemisWorld.edit(entityId).create(JbumpItemComponent.class);
        j.item = zebraItem;

        // Add the item to the Jbump World (x, y, width, height)
        // The type for the Item's user data is Integer, which is compatible with World<Object>
        jbumpWorld.add((Item)zebraItem, startingPosX + zebraOffsetX, startingPosY, zebraWidth, zebraHeight);

        // 4. STATE
        ZebraStateComponent s = artemisWorld.edit(entityId).create(ZebraStateComponent.class);
        s.state = ZebraState.GRAZING;
        s.dir = Direction.LEFT;
        s.stateTime = 0f;

        // 5. ANIMATION
        ZebraAnimationComponent anim = artemisWorld.edit(entityId).create(ZebraAnimationComponent.class);
        ZebraAnimationsFactory.initAnimations(anim);

        // 6. ACTIONS
        ActionComponent actions = artemisWorld.edit(entityId).create(ActionComponent.class);
        // Initialize actor position to match entity position
        actions.actor.setPosition(t.x, t.y);

        return entityId;
    }

    /**
     * Create a new Zebra entity at the specified position with custom direction.
     *
     * @param x The starting X position
     * @param y The starting Y position
     * @param direction The initial direction Zebra should face
     * @return The entity ID of the created Zebra
     */
    public int createZebra(float x, float y, Direction direction) {
        int entityId = createZebra(x, y);
        ZebraStateComponent s = artemisWorld.getMapper(ZebraStateComponent.class).get(entityId);
        s.dir = direction;
        return entityId;
    }

    /**
     * Create a new Zebra entity at the specified position with custom state.
     *
     * @param x The starting X position
     * @param y The starting Y position
     * @param initialState The initial state Zebra should be in
     * @return The entity ID of the created Zebra
     */
    public int createZebra(float x, float y, ZebraState initialState) {
        int entityId = createZebra(x, y);
        ZebraStateComponent s = artemisWorld.getMapper(ZebraStateComponent.class).get(entityId);
        s.state = initialState;
        return entityId;
    }

    /**
     * Create a new Zebra entity at the specified position with custom state and direction.
     *
     * @param x The starting X position
     * @param y The starting Y position
     * @param initialState The initial state Zebra should be in
     * @param direction The initial direction Zebra should face
     * @return The entity ID of the created Zebra
     */
    public int createZebra(float x, float y, ZebraState initialState, Direction direction) {
        int entityId = createZebra(x, y);
        ZebraStateComponent s = artemisWorld.getMapper(ZebraStateComponent.class).get(entityId);
        s.state = initialState;
        s.dir = direction;
        return entityId;
    }
}
