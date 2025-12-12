package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.pimpedpixel.games.gameplay.ScenarioState;
import com.pimpedpixel.games.systems.characters.ActionComponent;
import com.pimpedpixel.games.systems.characters.DisabledJbumpColliderComponent;
import com.pimpedpixel.games.systems.characters.Direction;
import com.pimpedpixel.games.systems.characters.HarryState;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;
import com.pimpedpixel.games.systems.characters.JbumpItemComponent;
import com.pimpedpixel.games.systems.characters.TransformComponent;
import com.pimpedpixel.games.systems.characters.ZebraOverrideComponent;
import com.pimpedpixel.games.systems.characters.ZebraState;
import com.pimpedpixel.games.systems.characters.ZebraStateComponent;
import com.dongbat.jbump.Rect;
import com.dongbat.jbump.World;

public class HarryDeathSequenceSystem extends IteratingSystem {
    private static final float ZEBRA_ATTACK_SPEED = 220f;
    private static final float SHRED_DURATION_SECONDS = 0.8f;
    private static final float ZEBRA_TARGET_OFFSET_X = 16f;

    private ComponentMapper<HarryStateComponent> mHarryState;
    private ComponentMapper<TransformComponent> mTransform;
    private ComponentMapper<PlaySoundComponent> mPlaySound;
    private ComponentMapper<HarryDeathSequenceComponent> mDeathSequence;

    private ComponentMapper<ZebraStateComponent> mZebraState;
    private ComponentMapper<ActionComponent> mActions;
    private ComponentMapper<ZebraOverrideComponent> mZebraOverride;
    private ComponentMapper<JbumpItemComponent> mJbumpItem;
    private ComponentMapper<DisabledJbumpColliderComponent> mDisabledCollider;

    private EntitySubscription zebraSubscription;
    private final World<Object> jbumpWorld;

    public HarryDeathSequenceSystem(World<Object> jbumpWorld) {
        super(Aspect.all(HarryStateComponent.class, TransformComponent.class));
        this.jbumpWorld = jbumpWorld;
    }

    @Override
    protected void initialize() {
        zebraSubscription = world.getAspectSubscriptionManager().get(
            Aspect.all(ZebraStateComponent.class, TransformComponent.class, ActionComponent.class)
        );
    }

    @Override
    protected void process(int entityId) {
        HarryStateComponent harryState = mHarryState.get(entityId);
        if (harryState.state != HarryState.DYING) {
            restoreHarryHitbox(entityId);
            if (mDeathSequence.has(entityId)) {
                mDeathSequence.remove(entityId);
            }
            return;
        }

        HarryDeathSequenceComponent seq = mDeathSequence.get(entityId);
        if (seq == null) {
            seq = mDeathSequence.create(entityId);
            seq.harryEntityId = entityId;
            startSequence(entityId, harryState, seq);
        }

        if (seq.gruntIssued) {
            seq.gruntDone = !mPlaySound.has(entityId);
        } else {
            seq.gruntDone = true;
        }

        if (seq.zebraEntityId == -1 || !mZebraOverride.has(seq.zebraEntityId)) {
            seq.zebraDone = true;
        } else {
            ZebraOverrideComponent zebraOverride = mZebraOverride.get(seq.zebraEntityId);
            seq.zebraDone = zebraOverride != null && zebraOverride.deathSequenceDone;
            if (seq.zebraDone) {
                mZebraOverride.remove(seq.zebraEntityId);
            }
        }

        seq.done = seq.gruntDone && seq.zebraDone;
    }

    private void startSequence(int harryEntityId, HarryStateComponent harryState, HarryDeathSequenceComponent seq) {
        TransformComponent harryTransform = mTransform.get(harryEntityId);
        if (harryTransform == null) {
            seq.done = true;
            return;
        }

        disableHarryHitbox(harryEntityId);
        maybePlayGruntOncePerLevel(harryEntityId, seq);
        startZebraApproachAndShred(harryTransform, seq);
    }

    private void maybePlayGruntOncePerLevel(int harryEntityId, HarryDeathSequenceComponent seq) {
        ScenarioState scenarioState = ScenarioState.getInstance();
        if (scenarioState.hasPlayedDeathGruntThisLevel()) {
            return;
        }

        PlaySoundComponent playSound = mPlaySound.create(harryEntityId);
        playSound.soundId = SoundId.GRUNT;
        playSound.blocking = true;

        seq.gruntIssued = true;
        scenarioState.markDeathGruntPlayedThisLevel();
    }

    private void startZebraApproachAndShred(TransformComponent harryTransform, HarryDeathSequenceComponent seq) {
        int zebraId = findClosestZebra(harryTransform.x);
        seq.zebraEntityId = zebraId;
        if (zebraId == -1) {
            seq.zebraDone = true;
            return;
        }

        TransformComponent zebraTransform = mTransform.get(zebraId);
        ZebraStateComponent zebraState = mZebraState.get(zebraId);
        ActionComponent zebraActions = mActions.get(zebraId);
        if (zebraTransform == null || zebraState == null || zebraActions == null) {
            seq.zebraDone = true;
            return;
        }

        ZebraOverrideComponent zebraOverride = mZebraOverride.create(zebraId);
        zebraOverride.deathSequenceActive = true;
        zebraOverride.deathSequenceDone = false;

        zebraActions.clearActions();

        float targetX = harryTransform.x + (zebraTransform.x < harryTransform.x ? -ZEBRA_TARGET_OFFSET_X : ZEBRA_TARGET_OFFSET_X);
        float moveDistance = Math.abs(targetX - zebraTransform.x);
        float moveDuration = Math.max(0.05f, moveDistance / ZEBRA_ATTACK_SPEED);

        zebraState.dir = (zebraTransform.x <= targetX) ? Direction.RIGHT : Direction.LEFT;
        zebraState.state = ZebraState.WALKING;
        zebraState.stateTime = 0f;

        zebraActions.addAction(
            Actions.sequence(
                Actions.moveTo(targetX, zebraTransform.y, moveDuration),
                Actions.run(() -> {
                    zebraState.state = ZebraState.SHREDDING;
                    zebraState.stateTime = 0f;
                    disableHarryHitbox(seq.harryEntityId);
                }),
                Actions.delay(SHRED_DURATION_SECONDS),
                Actions.run(() -> {
                    zebraState.state = ZebraState.GRAZING;
                    zebraState.stateTime = 0f;
                    restoreHarryHitbox(seq.harryEntityId);
                    zebraOverride.deathSequenceDone = true;
                    zebraOverride.deathSequenceActive = false;
                })
            )
        );
    }

    private void disableHarryHitbox(int harryEntityId) {
        if (harryEntityId < 0) {
            return;
        }

        if (jbumpWorld == null || !mJbumpItem.has(harryEntityId)) {
            return;
        }

        JbumpItemComponent jbumpItem = mJbumpItem.get(harryEntityId);
        if (jbumpItem == null || jbumpItem.item == null) {
            return;
        }

        DisabledJbumpColliderComponent disabled = mDisabledCollider.create(harryEntityId);
        if (disabled.disabled) {
            return;
        }

        Rect rect = jbumpWorld.getRect(jbumpItem.item);
        if (rect == null) {
            return;
        }

        disabled.x = rect.x;
        disabled.y = rect.y;
        disabled.w = rect.w;
        disabled.h = rect.h;
        disabled.disabled = true;
        jbumpWorld.remove(jbumpItem.item);
    }

    private void restoreHarryHitbox(int harryEntityId) {
        if (harryEntityId < 0) {
            return;
        }

        if (jbumpWorld == null || !mJbumpItem.has(harryEntityId) || !mDisabledCollider.has(harryEntityId)) {
            return;
        }

        DisabledJbumpColliderComponent disabled = mDisabledCollider.get(harryEntityId);
        JbumpItemComponent jbumpItem = mJbumpItem.get(harryEntityId);
        if (disabled == null || !disabled.disabled || jbumpItem == null || jbumpItem.item == null) {
            return;
        }

        jbumpWorld.add((com.dongbat.jbump.Item) jbumpItem.item, disabled.x, disabled.y, disabled.w, disabled.h);
        disabled.disabled = false;
    }

    private int findClosestZebra(float harryX) {
        if (zebraSubscription == null || zebraSubscription.getEntities().isEmpty()) {
            return -1;
        }

        int[] ids = zebraSubscription.getEntities().getData();
        int size = zebraSubscription.getEntities().size();

        int bestId = -1;
        float bestDistance = Float.MAX_VALUE;

        for (int i = 0; i < size; i++) {
            int zebraId = ids[i];
            TransformComponent zebraTransform = mTransform.get(zebraId);
            if (zebraTransform == null) {
                continue;
            }
            float distance = Math.abs(zebraTransform.x - harryX);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestId = zebraId;
            }
        }

        return bestId;
    }
}
