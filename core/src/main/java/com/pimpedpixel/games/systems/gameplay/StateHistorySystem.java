package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;
import com.pimpedpixel.games.systems.characters.ZebraStateComponent;

/**
 * Centralized state-history updater so other systems can reliably detect transitions
 * using previousState without racing to update it themselves.
 *
 * Add this system late in the pipeline so it runs after all state changes.
 */
public class StateHistorySystem extends IteratingSystem {
    private ComponentMapper<HarryStateComponent> mHarryState;
    private ComponentMapper<ZebraStateComponent> mZebraState;

    public StateHistorySystem() {
        super(Aspect.one(HarryStateComponent.class, ZebraStateComponent.class));
    }

    @Override
    protected void process(int entityId) {
        if (mHarryState.has(entityId)) {
            HarryStateComponent harryState = mHarryState.get(entityId);
            harryState.previousState = harryState.state;
        }
        if (mZebraState.has(entityId)) {
            ZebraStateComponent zebraState = mZebraState.get(entityId);
            zebraState.previousState = zebraState.state;
        }
    }
}

