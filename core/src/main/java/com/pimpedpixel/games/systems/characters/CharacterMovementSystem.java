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
        Item<Integer> item = mJbumpItem.get(entityId).item;

        // Check if movement should be blocked for certain states
        boolean movementBlocked = s.state == HarryState.DIED || 
                                 s.state == HarryState.DYING || 
                                 s.state == HarryState.DIMINISHING || 
                                 s.state == HarryState.DIMINISHED;

        // --- INPUT ---
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean jump = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        // Horizontal movement - only allow if not in blocked states
        p.vx = 0;
        if (!movementBlocked) {
            if (left) {
                p.vx = -moveSpeed;
                s.dir = Direction.LEFT;
            } else if (right) {
                p.vx = moveSpeed;
                s.dir = Direction.RIGHT;
            }
        }

        // Jump logic - only allow if not in blocked states
        if (jump && p.onGround && !movementBlocked) {
            p.vy = jumpSpeed;
            p.onGround = false;
            s.state = HarryState.JUMPING;
            s.stateTime = 0f;
            s.justJumped = true; // Flag to indicate a jump has just occurred
        }

        // Gravity - apply gravity unless in DIMINISHING or DIMINISHED states
        if (s.state != HarryState.DIMINISHING && s.state != HarryState.DIMINISHED) {
            p.vy += gravity * dt;
        }

        // Calculate target position
        float dx = p.vx * dt;
        float dy = p.vy * dt;
        float newX = t.x + dx;
        float newY = t.y + dy;

        // JBUMP COLLISION RESOLUTION
        Response.Result result = jbumpWorld.move(item, newX, newY, playerFilter);
        t.x = result.goalX;
        t.y = result.goalY;

        // Check for ground collision
        boolean touchedGround = false;
        for (int i = 0; i < result.projectedCollisions.size(); i++) {
            Collision collision = result.projectedCollisions.get(i);
            if (collision.normal.y > 0.001f) {
                touchedGround = true;
                p.vy = 0;
                break;
            }
        }

        // Update ground status
        if (touchedGround) {
            p.onGround = true;
            s.justJumped = false; // Reset the flag when landing
            if (Math.abs(p.vy) < 1f) {
                p.vy = 0;
            }
        } else {
            p.onGround = false;
        }

        // STATE MACHINE - only update states if not in blocked states
        if (!movementBlocked) {
            if (!p.onGround) {
                // If the character just jumped, keep the JUMPING state for a short grace period
                if (s.justJumped) {
                    s.state = HarryState.JUMPING;
                    // Reset the flag after a short grace period (e.g., 0.1 seconds)
                    if (s.stateTime > 0.1f) {
                        s.justJumped = false;
                    }
                }
                // Transition to FALLING if the character is moving downward and the grace period is over
                else if (p.vy < 0) {
                    s.state = HarryState.FALLING;
                }
            } else {
                s.justJumped = false; // Reset the flag when landing
                if (Math.abs(p.vx) > 1f) {
                    s.state = HarryState.WALKING;
                } else {
                    s.state = HarryState.RESTING;
                }
                if(t.y < 130){
                    s.state = HarryState.DYING;
                }
            }
        }

        s.stateTime += dt;
    }
}
