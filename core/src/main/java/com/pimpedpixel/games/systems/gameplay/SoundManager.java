package com.pimpedpixel.games.systems.gameplay;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    final Sound jumpingSound;

    public SoundManager(AssetManager assetManager) {
        jumpingSound = assetManager.get( "soundfx/jumping.ogg", Sound.class);
    }

    void play(long soundId){
        jumpingSound.play();
    }
}
