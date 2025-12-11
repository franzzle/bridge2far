package com.pimpedpixel.games.systems.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class ZebraAnimationsFactory {

    private static final float WALK_FRAME_DURATION = 0.12f;
    private static final float GRAZE_FRAME_DURATION = 0.2f;
    private static final float ONE_FRAME_DURATION = 0.2f;

    public static void initAnimations(ZebraAnimationComponent anim) {
        // GRAZING (4 frames each)
        anim.grazingLeft = new Animation<>(
            GRAZE_FRAME_DURATION,
            new Array<>(new TextureRegion[]{
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-grazing-left-1.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-grazing-left-2.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-grazing-left-3.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-grazing-left-4.png")
            }),
            Animation.PlayMode.LOOP
        );

        anim.grazingRight = new Animation<>(
            GRAZE_FRAME_DURATION,
            new Array<>(new TextureRegion[]{
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-grazing-right-1.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-grazing-right-2.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-grazing-right-3.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-grazing-right-4.png")
            }),
            Animation.PlayMode.LOOP
        );

        // WALKING (2 frames each)
        anim.walkingLeft = new Animation<>(
            WALK_FRAME_DURATION,
            new Array<>(new TextureRegion[]{
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-walking-left-1.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-walking-left-2.png")
            }),
            Animation.PlayMode.LOOP
        );

        anim.walkingRight = new Animation<>(
            WALK_FRAME_DURATION,
            new Array<>(new TextureRegion[]{
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-walking-right-1.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-walking-right-2.png")
            }),
            Animation.PlayMode.LOOP
        );

        // SHREDDING (4 frames each) - NEW ANIMATION
        anim.shreddingLeft = new Animation<>(
            GRAZE_FRAME_DURATION,
            new Array<>(new TextureRegion[]{
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-shredding-left-1.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-shredding-left-2.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-shredding-left-3.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-shredding-left-4.png")
            }),
            Animation.PlayMode.LOOP
        );

        anim.shreddingRight = new Animation<>(
            GRAZE_FRAME_DURATION,
            new Array<>(new TextureRegion[]{
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-shredding-right-1.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-shredding-right-2.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-shredding-right-3.png"),
                ZebraTextureLoader.loadColorKeyedRegion("characters/raelus/zebra-shredding-right-4.png")
            }),
            Animation.PlayMode.LOOP
        );
    }

    private static Animation<TextureRegion> singleFrame(String path) {
        TextureRegion region = ZebraTextureLoader.loadColorKeyedRegion(path);
        Animation<TextureRegion> anim = new Animation<>(ONE_FRAME_DURATION, region);
        anim.setPlayMode(Animation.PlayMode.NORMAL);
        return anim;
    }
}