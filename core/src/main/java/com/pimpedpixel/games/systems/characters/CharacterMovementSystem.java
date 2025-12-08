package com.pimpedpixel.games.systems.characters;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.dongbat.jbump.*;

// New Jbump Imports

// NOTE: You must also import your JbumpItemComponent here
// import com.pimpedpixel.games.components.JbumpItemComponent;

public class CharacterMovementSystem extends IteratingSystem {

    // Existing Mappers
    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<PhysicsComponent> mPhysics;
    private ComponentMapper<HarryStateComponent> mState;

    // New Mapper for Jbump Item
    private ComponentMapper<JbumpItemComponent> mJbumpItem;

    // Jbump World
    private final World jbumpWorld;

    private final float moveSpeed = 120f;
    private final float jumpSpeed = 260f;
    private final float gravity = -600f;

    // Custom CollisionFilter for the character (standard platformer behavior)
    private final static CollisionFilter playerFilter = (item, other) -> Response.slide;

    // The groundY field is no longer needed, as collision is handled by the Jbump world.

    public CharacterMovementSystem(World jbumpWorld) {
        // Updated Aspect to include the JbumpItemComponent
        super(Aspect.all(TransformComponent.class, PhysicsComponent.class, HarryStateComponent.class, JbumpItemComponent.class));
        this.jbumpWorld = jbumpWorld;
    }

    @Override
    protected void process(int entityId) {
        float dt = world.getDelta();
        TransformComponent t = mTransform.get(entityId);
        PhysicsComponent p = mPhysics.get(entityId);
        HarryStateComponent s = mState.get(entityId);
        Item<Integer> item = mJbumpItem.get(entityId).item; // Get the Jbump Item

        // --- INPUT ---
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean jump = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        // Horizontal movement (calculating velocity)
        p.vx = 0;
        if (left) {
            p.vx = -moveSpeed;
            s.dir = Direction.LEFT;
        } else if (right) {
            p.vx = moveSpeed;
            s.dir = Direction.RIGHT;
        }

        // Jump (applying impulse)
        if (jump && p.onGround) {
            p.vy = jumpSpeed;
            p.onGround = false;
            s.state = HarryState.JUMPING;
            s.stateTime = 0f;
        }

        // Gravity (applying acceleration)
        p.vy += gravity * dt;

        // Calculate target position based on integrated velocity
        float dx = p.vx * dt;
        float dy = p.vy * dt;
        float newX = t.x + dx;
        float newY = t.y + dy;

        // --- JBUMP COLLISION RESOLUTION ---

        // 1. Ask Jbump to move the item and resolve collisions.
        Response.Result result = jbumpWorld.move(item, newX, newY, playerFilter);

        // 2. Update the character's position with the collision-resolved position.
        t.x = result.goalX;
        t.y = result.goalY;

        // 3. Check collision results for 'onGround' status.
        boolean touchedGround = false;

        // Loop through all generated collisions
        for (int i = 0; i < result.projectedCollisions.size(); i++) {
            Collision collision = result.projectedCollisions.get(i);

            // If the collision normal points upward (meaning we hit a surface below us)
            if (collision.normal.y > 0.001f) {
                touchedGround = true;
                p.vy = 0; // Stop vertical movement
                break;
            }
        }

        // 4. Update PhysicsComponent state
        if (touchedGround) {
            p.onGround = true;
            // Ensure velocity is zeroed out if standing still, especially after gravity.
            if (Math.abs(p.vy) < 1f) {
                p.vy = 0;
            }
        } else {
            p.onGround = false;
        }

        // --- STATE MACHINE ---
        if (!p.onGround) {
            // Note: In platformers, you often use FALLING when p.vy < 0 and JUMPING when p.vy > 0.
            if (p.vy > 0) {
                s.state = HarryState.JUMPING;
            } else {
                s.state = HarryState.FALLING; // Keeping your original logic here.
            }
        } else {
            if (Math.abs(p.vx) > 1f) {
                s.state = HarryState.WALKING;
            } else {
                s.state = HarryState.RESTING;
            }
        }
        s.stateTime += dt;
    }
}
