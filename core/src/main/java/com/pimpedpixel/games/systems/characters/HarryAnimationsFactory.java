package com.pimpedpixel.games.systems.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class HarryAnimationsFactory {

    private static final float WALK_FRAME_DURATION = 0.12f;
    private static final float ONE_FRAME_DURATION = 0.2f;

    public static void initAnimations(HarryAnimationComponent anim) {
        // RESTING
        anim.restingLeft = singleFrame("characters/harry/harry-resting-left-1.png");
        anim.restingRight = singleFrame("characters/harry/harry-resting-right-1.png");

        // WALKING (3 frames each)
        anim.walkingLeft = new Animation<>(
                WALK_FRAME_DURATION,
                new Array<>(new TextureRegion[]{
                        HarryTextureLoader.loadColorKeyedRegion("characters/harry/harry-walking-left-1.png"),
                        HarryTextureLoader.loadColorKeyedRegion("characters/harry/harry-walking-left-2.png"),
                        HarryTextureLoader.loadColorKeyedRegion("characters/harry/harry-walking-left-3.png")
                }),
                Animation.PlayMode.LOOP
        );

        anim.walkingRight = new Animation<>(
                WALK_FRAME_DURATION,
                new Array<>(new TextureRegion[]{
                        HarryTextureLoader.loadColorKeyedRegion("characters/harry/harry-walking-right-1.png"),
                        HarryTextureLoader.loadColorKeyedRegion("characters/harry/harry-walking-right-2.png"),
                        HarryTextureLoader.loadColorKeyedRegion("characters/harry/harry-walking-right-3.png")
                }),
                Animation.PlayMode.LOOP
        );

        // JUMPING
        anim.jumpingLeft = singleFrame("characters/harry/harry-jumping-left-1.png");
        anim.jumpingRight = singleFrame("characters/harry/harry-jumping-right-1.png");

        // FALLING
        anim.fallingLeft = singleFrame("characters/harry/harry-falling-left-1.png");
        anim.fallingRight = singleFrame("characters/harry/harry-falling-right-1.png");

        // DYING (one frame each, you can later extend to multi-frame)
        anim.dyingLeft = singleFrame("characters/harry/harry-dying-left-1.png");
        anim.dyingRight = singleFrame("characters/harry/harry-dying-right-1.png");
    }

    private static Animation<TextureRegion> singleFrame(String path) {
        TextureRegion region = HarryTextureLoader.loadColorKeyedRegion(path);
        Animation<TextureRegion> anim = new Animation<>(ONE_FRAME_DURATION, region);
        anim.setPlayMode(Animation.PlayMode.NORMAL);
        return anim;
    }
}
