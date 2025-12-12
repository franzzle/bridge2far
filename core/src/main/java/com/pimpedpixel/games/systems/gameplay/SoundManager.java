package com.pimpedpixel.games.systems.gameplay;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    final Sound jumpingSound;
    final Sound unlockSound;

    public SoundManager(AssetManager assetManager) {
        jumpingSound = assetManager.get( "soundfx/jumping.ogg", Sound.class);
        unlockSound = assetManager.get( "soundfx/unlock.ogg", Sound.class);
    }

    void play(SoundId soundId){
        switch (soundId) {
            case JUMPING:
                jumpingSound.play();
                break;
            case UNLOCK:
                unlockSound.play();
                break;
            default:
                // Unknown sound ID, could play a default sound or log an error
                System.err.println("Unknown sound ID: " + soundId);
                break;
        }
    }
}
