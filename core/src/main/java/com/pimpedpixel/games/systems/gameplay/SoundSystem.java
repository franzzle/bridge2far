package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

public class SoundSystem extends IteratingSystem {

    ComponentMapper<PlaySoundComponent> mPlaySound;
    private final SoundManager soundManager;

    public SoundSystem(SoundManager soundManager) {
        super(Aspect.all(PlaySoundComponent.class));  // ✅ this is what was missing
        this.soundManager = soundManager;
    }

    @Override
    protected void process(int entityId) {
        PlaySoundComponent comp = mPlaySound.get(entityId);

        soundManager.play(comp.soundId);

        // one-shot: remove after playing
        mPlaySound.remove(entityId);
    }
}
