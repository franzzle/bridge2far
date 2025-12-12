package com.pimpedpixel.games.systems.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    final Sound jumpingSound;
    final Sound unlockSound;
    final Music gruntMusic;

    public SoundManager(AssetManager assetManager) {
        jumpingSound = assetManager.get( "soundfx/jumping.ogg", Sound.class);
        unlockSound = assetManager.get( "soundfx/unlock.ogg", Sound.class);
        gruntMusic = Gdx.audio.newMusic(Gdx.files.internal("soundfx/grunt.ogg"));
    }

    void play(SoundId soundId){
        switch (soundId) {
            case JUMPING:
                jumpingSound.play();
                return;
            case UNLOCK:
                unlockSound.play();
                return;
            case GRUNT:
                gruntMusic.stop();
                gruntMusic.play();
                return;
            default:
                // Unknown sound ID, could play a default sound or log an error
                System.err.println("Unknown sound ID: " + soundId);
                return;
        }
    }

    boolean isPlaying(SoundId soundId) {
        if (soundId == SoundId.GRUNT) {
            return gruntMusic.isPlaying();
        }
        return false;
    }
}
