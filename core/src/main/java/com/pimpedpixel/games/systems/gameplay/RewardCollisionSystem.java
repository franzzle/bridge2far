package com.pimpedpixel.games.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import com.dongbat.jbump.World;
import com.pimpedpixel.games.systems.characters.DisabledJbumpColliderComponent;
import com.pimpedpixel.games.systems.characters.HarryStateComponent;
import com.pimpedpixel.games.systems.characters.JbumpItemComponent;
import com.pimpedpixel.games.systems.characters.TransformComponent;
import com.pimpedpixel.games.gameplay.ScenarioState;

/**
 * System for detecting collisions between Harry and reward objects.
 * When Harry's bounding box overlaps with a reward object, plays the unlock sound.
 */
public class RewardCollisionSystem extends IteratingSystem {

    ComponentMapper<HarryStateComponent> mHarryState;
    ComponentMapper<TransformComponent> mTransform;
    ComponentMapper<JbumpItemComponent> mJbumpItem;
    ComponentMapper<PlaySoundComponent> mPlaySound;
    ComponentMapper<DisabledJbumpColliderComponent> mDisabledCollider;

    private final World<Object> jbumpWorld;
    private final TiledMap tileMap;
    private final String rewardLayerName;
    private final float harryOffsetX;
    private final float harryWidth;
    private final float harryHeight;
    private final float assetScale;

    // Track which rewards have been collected to avoid duplicate sounds
    private boolean rewardCollected = false;

    public RewardCollisionSystem(World<Object> jbumpWorld, TiledMap tileMap, String rewardLayerName,
                               float harryOffsetX, float harryWidth, float harryHeight, float assetScale) {
        super(Aspect.all(HarryStateComponent.class, TransformComponent.class, JbumpItemComponent.class));
        this.jbumpWorld = jbumpWorld;
        this.tileMap = tileMap;
        this.rewardLayerName = rewardLayerName;
        this.harryOffsetX = harryOffsetX;
        this.harryWidth = harryWidth;
        this.harryHeight = harryHeight;
        this.assetScale = assetScale;
    }

    @Override
    protected void process(int entityId) {
        HarryStateComponent stateComp = mHarryState.get(entityId);
        TransformComponent transformComp = mTransform.get(entityId);
        JbumpItemComponent jbumpItemComp = mJbumpItem.get(entityId);

        if (mDisabledCollider != null && mDisabledCollider.has(entityId)) {
            DisabledJbumpColliderComponent disabled = mDisabledCollider.get(entityId);
            if (disabled != null && disabled.disabled) {
                return;
            }
        }

        // Check if we should reset the reward collected flag
        // Reset when Harry is in RESTING state (back at start position)
        if (stateComp.state == com.pimpedpixel.games.systems.characters.HarryState.RESTING) {
            rewardCollected = false;
        }

        // Only check for reward collisions if we haven't collected the reward yet
        if (!rewardCollected) {
            checkRewardCollisions(entityId, transformComp, jbumpItemComp);
        }
    }

    private void checkRewardCollisions(int entityId, TransformComponent transformComp, JbumpItemComponent jbumpItemComp) {
        // Check if treasure was already found in this scenario (play sound only once)
        ScenarioState scenarioState = ScenarioState.getInstance();
        if (scenarioState.isTreasureFoundThisScenario()) {
            System.out.println("Treasure already found in this scenario, skipping reward check.");
            return;
        }

        // Get the reward layer from the tile map
        MapLayer rewardLayer = tileMap.getLayers().get(rewardLayerName);

        if (rewardLayer != null) {
            // Get Harry's position from Jbump world
            @SuppressWarnings("unchecked")
            Item<Integer> harryItem = (Item<Integer>) jbumpItemComp.item;

            // Get the position from the Jbump world and use our stored dimensions
            Rect rect = jbumpWorld.getRect(harryItem);
            if (rect == null) {
                return;
            }
            float x = rect.x;
            float y = rect.y;

            // Create Harry's bounding box using the position from Jbump and our stored dimensions
            Rectangle harryBounds = new Rectangle(x, y, harryWidth, harryHeight);

            // Check each reward object for collision
            for (MapObject mapObject : rewardLayer.getObjects()) {
                // Only process objects named "reward"
                if ("reward".equals(mapObject.getName())) {
                    // Get the reward position and scale it to match game coordinates
                    float rewardX = mapObject.getProperties().get("x", Float.class) * assetScale;
                    float rewardY = mapObject.getProperties().get("y", Float.class) * assetScale;

                    // Create a reasonable bounding box for the reward (point objects need dimensions)
                    // Use a 32x32 pixel area around the reward point
                    Rectangle rewardBounds = new Rectangle(rewardX, rewardY, 32f * assetScale, 32f * assetScale);

                    // Check if Harry's bounding box overlaps with the reward object
                    if (harryBounds.overlaps(rewardBounds)) {
                        playUnlockSound(entityId);

                        // Record treasure found in scenario state
                        scenarioState.recordTreasureFound();

                        rewardCollected = true;
                        System.out.println("Harry collected reward! Playing unlock sound.");
                        break; // Only collect one reward at a time
                    }
                }
            }
        }
    }

    private void playUnlockSound(int entityId) {
        // Create a PlaySoundComponent to trigger the unlock sound
        PlaySoundComponent playSound = mPlaySound.create(entityId);
        playSound.soundId = SoundId.UNLOCK; // Use enum for type safety
    }
}
