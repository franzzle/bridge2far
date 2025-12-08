package com.pimpedpixel.games.systems.characters;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class CharacterMovementSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<PhysicsComponent> mPhysics;
    private ComponentMapper<HarryStateComponent> mState;

    private final float moveSpeed = 120f;
    private final float jumpSpeed = 260f;
    private final float gravity = -600f;
    private final float groundY;

    public CharacterMovementSystem(float groundY) {
        super(Aspect.all(TransformComponent.class, PhysicsComponent.class, HarryStateComponent.class));
        this.groundY = groundY;
    }

    @Override
    protected void process(int entityId) {
        float dt = world.getDelta();

        TransformComponent t = mTransform.get(entityId);
        PhysicsComponent p = mPhysics.get(entityId);
        HarryStateComponent s = mState.get(entityId);

        // --- INPUT ---
        boolean left  = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean jump  = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        // Horizontal movement
        p.vx = 0;
        if (left) {
            p.vx = -moveSpeed;
            s.dir = Direction.LEFT;
        } else if (right) {
            p.vx = moveSpeed;
            s.dir = Direction.RIGHT;
        }

        // Jump
        if (jump && p.onGround) {
            p.vy = jumpSpeed;
            p.onGround = false;
            s.state = HarryState.JUMPING;

            s.stateTime = 0f;
        }

        // Gravity
        p.vy += gravity * dt;

        // Integrate position
        t.x += p.vx * dt;
        t.y += p.vy * dt;

        // Simple ground collision
        if (t.y <= groundY) {
            t.y = groundY;
            if (!p.onGround) {
                p.onGround = true;
                p.vy = 0;
            }
        } else {
            p.onGround = false;
        }

        // --- STATE MACHINE ---
        if (!p.onGround) {
            if (p.vy > 0) {
                s.state = HarryState.JUMPING;
            } else {
                s.state = HarryState.JUMPING;
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
