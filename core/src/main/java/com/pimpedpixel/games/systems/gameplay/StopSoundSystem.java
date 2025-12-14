package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

public class StopSoundSystem extends IteratingSystem {
    private ComponentMapper<StopSoundComponent> mStopSound;
    private final SoundManager soundManager;

    public StopSoundSystem(SoundManager soundManager) {
        super(Aspect.all(StopSoundComponent.class));
        this.soundManager = soundManager;
    }

    @Override
    protected void process(int entityId) {
        StopSoundComponent comp = mStopSound.get(entityId);
        soundManager.stop(comp.soundId);
        mStopSound.remove(entityId);
    }
}

