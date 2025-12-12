package com.pimpedpixel.games.systems.characters;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.pimpedpixel.games.DesignResolution;

/**
 * Renders blood animations with crisp pixel-art scaling.
 */
public class BloodRenderSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<BloodAnimationComponent> mAnim;

    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    
    // Blood state durations
    private static final float FLOWING_DURATION = 1.0f;    // 1 second flowing
    private static final float DRYING_DURATION = 1.5f;    // 1.5 seconds drying
    private static final float DRIED_DURATION = 5.0f;     // 5 seconds dried before removal

    public BloodRenderSystem(SpriteBatch batch, OrthographicCamera camera) {
        super(Aspect.all(TransformComponent.class, BloodAnimationComponent.class));
        this.batch = batch;
        this.camera = camera;
    }

    @Override
    protected void begin() {
        // Optional but very helpful for pixel art: snap camera to whole pixels
        camera.position.x = Math.round(camera.position.x);
        camera.position.y = Math.round(camera.position.y);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
    }

    @Override
    protected void end() {
        batch.end();
    }

    @Override
    protected void process(int entityId) {
        TransformComponent t = mTransform.get(entityId);
        BloodAnimationComponent anim = mAnim.get(entityId);

        // Update blood state based on time
        anim.stateTime += world.getDelta();
        
        // State transitions
        if (anim.state == BloodState.FLOWING && anim.stateTime >= FLOWING_DURATION) {
            anim.state = BloodState.DRYING;
            anim.stateTime = 0f; // Reset timer for drying state
        } else if (anim.state == BloodState.DRYING && anim.stateTime >= DRYING_DURATION) {
            anim.state = BloodState.DRIED;
            anim.stateTime = 0f; // Reset timer for dried state
        } else if (anim.state == BloodState.DRIED && anim.stateTime >= DRIED_DURATION) {
            // Remove blood entity after it has been dried for the duration
            world.delete(entityId);
            return;
        }

        // Select the appropriate animation based on state
        Animation<TextureRegion> animation = selectAnimation(anim);
        TextureRegion frame = animation.getKeyFrame(anim.stateTime, false); // No looping

        float scale = DesignResolution.CHARACTER_SCALE; // Use same scale as characters

        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;

        // Snap to whole pixels to avoid blur
        float drawX = Math.round(t.x);
        float drawY = Math.round(t.y);

        // Bottom-left anchor (blood should appear on the ground)
        batch.draw(frame, drawX, drawY, width, height);
    }

    private Animation<TextureRegion> selectAnimation(BloodAnimationComponent anim) {
        switch (anim.state) {
            case DRYING:
                return anim.drying;
            case DRIED:
                return anim.dried;
            case FLOWING:
            default:
                return anim.flowing;
        }
    }
}