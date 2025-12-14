package com.pimpedpixel.games.systems.characters;

import com.artemis.World;
import com.dongbat.jbump.Item;

/**
 * Factory class for creating Harry entities.
 * Encapsulates all the logic for setting up Harry with the required components.
 */
public class HarryFactory {

    private final com.artemis.World artemisWorld;
    private final com.dongbat.jbump.World<Object> jbumpWorld;
    private final float harryOffsetX;
    private final float harryWidth;
    private final float harryHeight;

    /**
     * Create a new HarryFactory.
     *
     * @param artemisWorld The Artemis ECS world
     * @param jbumpWorld The Jbump physics world
     * @param harryOffsetX The X offset for Harry's collision box
     * @param harryWidth The width of Harry's collision box
     * @param harryHeight The height of Harry's collision box
     */
    public HarryFactory(com.artemis.World artemisWorld,
                       com.dongbat.jbump.World<Object> jbumpWorld,
                       float harryOffsetX,
                       float harryWidth,
                       float harryHeight) {
        this.artemisWorld = artemisWorld;
        this.jbumpWorld = jbumpWorld;
        this.harryOffsetX = harryOffsetX;
        this.harryWidth = harryWidth;
        this.harryHeight = harryHeight;
    }

    /**
     * Create a new Harry entity at the specified position.
     *
     * @param x The starting X position
     * @param y The starting Y position
     * @return The entity ID of the created Harry
     */
    public int createHarry(float x, float y) {
        int entityId = artemisWorld.create();

        // Use the entityId as the object type for dynamic items
        Item<Integer> harryItem = new Item<>(entityId);

        // 1. TRANSFORM
        TransformComponent t = artemisWorld.edit(entityId).create(TransformComponent.class);
        t.x = x;
        t.y = y;

        // 2. PHYSICS
        PhysicsComponent p = artemisWorld.edit(entityId).create(PhysicsComponent.class);
        p.vx = 0;
        p.vy = 0;
        p.onGround = false; // Start false, collision loop will fix

        // 3. JUMP ITEM
        // Add the component and initialize the Jbump Item at the starting position
        JbumpItemComponent j = artemisWorld.edit(entityId).create(JbumpItemComponent.class);
        j.item = harryItem;

        // Add the item to the Jbump World (x, y, width, height)
        // The type for the Item's user data is Integer, which is compatible with World<Object>
        jbumpWorld.add((Item)harryItem, x + harryOffsetX, y, harryWidth, harryHeight);

        // 4. STATE
        HarryStateComponent s = artemisWorld.edit(entityId).create(HarryStateComponent.class);
        s.state = HarryState.RESTING;
        s.dir = Direction.RIGHT;
        s.stateTime = 0f;

        // 5. ANIMATION
        HarryAnimationComponent anim = artemisWorld.edit(entityId).create(HarryAnimationComponent.class);
        HarryAnimationsFactory.initAnimations(anim);

        return entityId;
    }

    /**
     * Create a new Harry entity at the specified position with custom direction.
     *
     * @param x The starting X position
     * @param y The starting Y position
     * @param direction The initial direction Harry should face
     * @return The entity ID of the created Harry
     */
    public int createHarry(float x, float y, Direction direction) {
        int entityId = createHarry(x, y);
        HarryStateComponent s = artemisWorld.getMapper(HarryStateComponent.class).get(entityId);
        s.dir = direction;
        return entityId;
    }

    /**
     * Create a new Harry entity at the specified position with custom state.
     *
     * @param x The starting X position
     * @param y The starting Y position
     * @param initialState The initial state Harry should be in
     * @return The entity ID of the created Harry
     */
    public int createHarry(float x, float y, HarryState initialState) {
        int entityId = createHarry(x, y);
        HarryStateComponent s = artemisWorld.getMapper(HarryStateComponent.class).get(entityId);
        s.state = initialState;
        return entityId;
    }

    /**
     * Create a new Harry entity at the specified position with custom state and direction.
     *
     * @param x The starting X position
     * @param y The starting Y position
     * @param initialState The initial state Harry should be in
     * @param direction The initial direction Harry should face
     * @return The entity ID of the created Harry
     */
    public int createHarry(float x, float y, HarryState initialState, Direction direction) {
        int entityId = createHarry(x, y);
        HarryStateComponent s = artemisWorld.getMapper(HarryStateComponent.class).get(entityId);
        s.state = initialState;
        s.dir = direction;
        return entityId;
    }
}
