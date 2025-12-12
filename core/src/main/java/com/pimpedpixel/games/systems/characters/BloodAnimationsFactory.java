package com.pimpedpixel.games.systems.characters;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BloodAnimationsFactory {
    private static final float FLOWING_FRAME_DURATION = 1.0f / 12.0f; // 12 fps
    private static final float DRYING_FRAME_DURATION = 1.0f / 8.0f;  // 8 fps for drying
    private static final float DRIED_FRAME_DURATION = 1.0f;           // 1 fps for dried (static)

    public static void initAnimations(BloodAnimationComponent anim, AssetManager assetManager) {
        // Load the blood animation atlas
        TextureAtlas bloodAtlas = assetManager.get("animations/blood-flowing.txt", TextureAtlas.class);
        
        if (bloodAtlas == null) {
            System.err.println("Blood animation atlas not loaded!");
            return;
        }
        
        // Create flowing animation (frames 1-8) - dynamic blood spreading
        TextureRegion[] flowingFrames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            TextureRegion frame = bloodAtlas.findRegion("blood-flowing-" + (i + 1));
            if (frame == null) {
                System.err.println("Missing blood flowing frame: blood-flowing-" + (i + 1));
                return;
            }
            flowingFrames[i] = frame;
        }
        anim.flowing = new Animation<>(FLOWING_FRAME_DURATION, flowingFrames);
        anim.flowing.setPlayMode(Animation.PlayMode.NORMAL);

        // Create drying animation (frames 9-12) - blood starting to dry
        TextureRegion[] dryingFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            TextureRegion frame = bloodAtlas.findRegion("blood-flowing-" + (i + 9));
            if (frame == null) {
                System.err.println("Missing blood drying frame: blood-flowing-" + (i + 9));
                return;
            }
            dryingFrames[i] = frame;
        }
        anim.drying = new Animation<>(DRYING_FRAME_DURATION, dryingFrames);
        anim.drying.setPlayMode(Animation.PlayMode.NORMAL);

        // Create dried animation (frames 13-17) - fully dried blood pool
        // Use frames 13-17 for a more varied dried appearance
        TextureRegion[] driedFrames = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            TextureRegion frame = bloodAtlas.findRegion("blood-flowing-" + (i + 13));
            if (frame == null) {
                System.err.println("Missing blood dried frame: blood-flowing-" + (i + 13));
                return;
            }
            driedFrames[i] = frame;
        }
        // Use a very slow frame rate for dried state to create subtle variation
        anim.dried = new Animation<>(DRIED_FRAME_DURATION * 2, driedFrames);
        anim.dried.setPlayMode(Animation.PlayMode.LOOP);
    }
}