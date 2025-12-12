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
        
        // Create flowing animation (frames 1-8)
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

        // Create drying animation (frames 9-12) 
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

        // Create dried animation (frame 13 - static)
        TextureRegion driedFrame = bloodAtlas.findRegion("blood-flowing-13");
        if (driedFrame == null) {
            System.err.println("Missing blood dried frame: blood-flowing-13");
            return;
        }
        anim.dried = new Animation<>(DRIED_FRAME_DURATION, driedFrame);
        anim.dried.setPlayMode(Animation.PlayMode.NORMAL);
    }
}