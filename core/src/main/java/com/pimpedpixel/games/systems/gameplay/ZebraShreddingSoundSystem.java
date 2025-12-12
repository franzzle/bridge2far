package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.pimpedpixel.games.systems.characters.ZebraState;
import com.pimpedpixel.games.systems.characters.ZebraStateComponent;

public class ZebraShreddingSoundSystem extends IteratingSystem {
    private ComponentMapper<ZebraStateComponent> mZebraState;
    private ComponentMapper<PlaySoundComponent> mPlaySound;
    private ComponentMapper<StopSoundComponent> mStopSound;

    public ZebraShreddingSoundSystem() {
        super(Aspect.all(ZebraStateComponent.class));
    }

    @Override
    protected void process(int entityId) {
        ZebraStateComponent zebraState = mZebraState.get(entityId);

        boolean enteredShredding =
            zebraState.previousState != ZebraState.SHREDDING &&
                zebraState.state == ZebraState.SHREDDING;
        boolean exitedShredding =
            zebraState.previousState == ZebraState.SHREDDING &&
                zebraState.state != ZebraState.SHREDDING;

        if (enteredShredding) {
            PlaySoundComponent playSound = mPlaySound.create(entityId);
            playSound.soundId = SoundId.SHRED;
            playSound.looping = true;
            playSound.blocking = false;
            playSound.started = false;
        } else if (exitedShredding) {
            if (mPlaySound.has(entityId)) {
                PlaySoundComponent playSound = mPlaySound.get(entityId);
                if (playSound != null && playSound.soundId == SoundId.SHRED && playSound.looping) {
                    mPlaySound.remove(entityId);
                }
            }
            StopSoundComponent stopSound = mStopSound.create(entityId);
            stopSound.soundId = SoundId.SHRED;
        }

        zebraState.previousState = zebraState.state;
    }
}

