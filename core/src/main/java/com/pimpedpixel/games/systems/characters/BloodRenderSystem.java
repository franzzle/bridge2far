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
 * Blood is rendered behind characters and can be flipped based on orientation.
 */
public class BloodRenderSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<BloodAnimationComponent> mAnim;
    private ComponentMapper<HarryStateComponent> mHarryState;

    private final SpriteBatch batch;
    private final OrthographicCamera camera;

    // Blood state durations
    // Let blood stick around longer to feel more visceral.
    private static final float FLOWING_DURATION = 2.5f;    // used to be 1s
    private static final float DRYING_DURATION = 16.0f;     // used to be 1.5s
    private static final float DRIED_DURATION = 50.0f;     // used to be 5s

    // Blood scaling constants
    private static final float MAX_BLOOD_WIDTH = 128f;    // 2 cells × 64 pixels per cell
    private static final float BLOOD_SCALE_FACTOR = 0.5f; // Scale factor relative to character scale

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
        boolean looping = (anim.state == BloodState.DRIED); // Only loop for dried state
        TextureRegion frame = animation.getKeyFrame(anim.stateTime, looping);

        // Use a reduced scale for blood to ensure it covers max 2 cells (2x64 pixels)
        // The largest blood frame is 64px wide, so we scale it down appropriately
        float bloodScale = DesignResolution.CHARACTER_SCALE * BLOOD_SCALE_FACTOR;

        float width = frame.getRegionWidth() * bloodScale;
        float height = frame.getRegionHeight() * bloodScale;

        // Snap to whole pixels to avoid blur
        float drawX = Math.round(t.x);
        float drawY = Math.round(t.y);

        // Handle orientation flipping
        boolean flipX = (anim.orientation == Direction.RIGHT);

        // Bottom-left anchor (blood should appear on the ground)
        if (flipX) {
            // Flip the blood horizontally for RIGHT orientation
            batch.draw(frame, drawX + width, drawY, -width, height);
        } else {
            // Normal drawing for LEFT orientation
            batch.draw(frame, drawX, drawY, width, height);
        }
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
