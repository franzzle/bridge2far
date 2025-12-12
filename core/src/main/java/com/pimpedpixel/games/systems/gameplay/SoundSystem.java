package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

public class SoundSystem extends IteratingSystem {

    ComponentMapper<PlaySoundComponent> mPlaySound;
    private final SoundManager soundManager;

    public SoundSystem(SoundManager soundManager) {
        super(Aspect.all(PlaySoundComponent.class));
        this.soundManager = soundManager;
    }

    @Override
    protected void process(int entityId) {
        PlaySoundComponent comp = mPlaySound.get(entityId);

        if (!comp.started) {
            soundManager.play(comp.soundId);
            comp.started = true;
        }

        if (!comp.blocking || !soundManager.isPlaying(comp.soundId)) {
            // one-shot (or finished blocking sound): remove after playing
            mPlaySound.remove(entityId);
        }
    }
}
