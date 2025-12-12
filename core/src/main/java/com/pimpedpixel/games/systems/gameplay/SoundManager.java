package com.pimpedpixel.games.systems.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    final Sound jumpingSound;
    final Sound unlockSound;
    final Sound bonebreakSound;
    final Music gruntMusic;
    final Music shredMusic;

    public SoundManager(AssetManager assetManager) {
        jumpingSound = assetManager.get( "soundfx/jumping.ogg", Sound.class);
        unlockSound = assetManager.get( "soundfx/unlock.ogg", Sound.class);
        bonebreakSound = assetManager.get("soundfx/bonebreak.ogg", Sound.class);
        gruntMusic = Gdx.audio.newMusic(Gdx.files.internal("soundfx/grunt.ogg"));
        shredMusic = Gdx.audio.newMusic(Gdx.files.internal("soundfx/shred.ogg"));
        shredMusic.setLooping(true);
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
            case SHRED:
                shredMusic.stop();
                shredMusic.play();
                return;
            case BONEBREAK:
                bonebreakSound.play();
                return;
            default:
                // Unknown sound ID, could play a default sound or log an error
                System.err.println("Unknown sound ID: " + soundId);
                return;
        }
    }

    boolean isPlaying(SoundId soundId) {
        switch (soundId) {
            case GRUNT:
                return gruntMusic.isPlaying();
            case SHRED:
                return shredMusic.isPlaying();
            default:
                return false;
        }
    }

    void startLoop(SoundId soundId) {
        if (soundId == SoundId.SHRED) {
            shredMusic.play();
        } else {
            play(soundId);
        }
    }

    void stop(SoundId soundId) {
        switch (soundId) {
            case GRUNT:
                gruntMusic.stop();
                return;
            case SHRED:
                shredMusic.stop();
                return;
            default:
                return;
        }
    }
}
