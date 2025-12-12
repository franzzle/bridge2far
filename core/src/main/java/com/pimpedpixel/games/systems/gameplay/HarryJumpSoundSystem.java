package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.pimpedpixel.games.systems.characters.HarryState;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;

public class HarryJumpSoundSystem extends IteratingSystem {
    ComponentMapper<HarryStateComponent> mHarryState;
    ComponentMapper<PlaySoundComponent> mPlaySound;
    public HarryJumpSoundSystem() {
        super(Aspect.all(HarryStateComponent.class));
    }
    @Override
    protected void process(int entityId) {
        HarryStateComponent stateComp = mHarryState.get(entityId);

        boolean justStartedJumping =
                stateComp.previousState != HarryState.JUMPING &&
                stateComp.state          == HarryState.JUMPING;

        if (justStartedJumping) {
            // Attach PlaySoundComponent ONCE to this entity
            PlaySoundComponent playSound = mPlaySound.create(entityId);
            playSound.soundId = SoundId.JUMPING;   // Use enum for type safety
            // you can add extra fields like volume, pitch, etc.
        }

        // IMPORTANT: update previousState *after* you’ve checked the transition
        stateComp.previousState = stateComp.state;
    }
}
