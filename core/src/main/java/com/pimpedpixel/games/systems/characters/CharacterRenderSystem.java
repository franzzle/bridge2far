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
 * Renders Harry with crisp pixel-art scaling.
 */
public class CharacterRenderSystem extends IteratingSystem {

    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<HarryStateComponent> mState;
    private ComponentMapper<HarryAnimationComponent> mAnim;

    private final SpriteBatch batch;
    private final OrthographicCamera camera;

    public CharacterRenderSystem(SpriteBatch batch, OrthographicCamera camera) {
        super(Aspect.all(
            TransformComponent.class,
            HarryStateComponent.class,
            HarryAnimationComponent.class
        ));
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
        // blending is enabled by default, but if you ever disabled it:
        // batch.enableBlending();
        batch.begin();
    }

    @Override
    protected void end() {
        batch.end();
    }

    @Override
    protected void process(int entityId) {
        TransformComponent t = mTransform.get(entityId);
        HarryStateComponent s = mState.get(entityId);
        HarryAnimationComponent a = mAnim.get(entityId);

        Animation<TextureRegion> animation = selectAnimation(s, a);
        boolean looping = (s.state == HarryState.WALKING);
        TextureRegion frame = animation.getKeyFrame(s.stateTime, looping);

        float scale = DesignResolution.CHARACTER_SCALE; // 2f * ASSET_SCALE

        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;

        // Snap to whole pixels to avoid blur
        float drawX = Math.round(t.x);
        float drawY = Math.round(t.y);

        // Bottom-left anchor
        batch.draw(frame, drawX, drawY, width, height);
    }

    private Animation<TextureRegion> selectAnimation(HarryStateComponent s, HarryAnimationComponent a) {
        switch (s.state) {
            case WALKING:
                return (s.dir == Direction.LEFT) ? a.walkingLeft : a.walkingRight;
            case JUMPING:
                return (s.dir == Direction.LEFT) ? a.jumpingLeft : a.jumpingRight;
            case FALLING:
                return (s.dir == Direction.LEFT) ? a.fallingLeft : a.fallingRight;
            case DYING:
                return (s.dir == Direction.LEFT) ? a.dyingLeft : a.dyingRight;
            case RESTING:
            default:
                return (s.dir == Direction.LEFT) ? a.restingLeft : a.restingRight;
        }
    }
}
