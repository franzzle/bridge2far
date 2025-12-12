package com.pimpedpixel.games.systems.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.artemis.Component;

public class BloodAnimationComponent extends Component {
    public Animation<TextureRegion> flowing;
    public Animation<TextureRegion> drying;
    public Animation<TextureRegion> dried;
    public BloodState state = BloodState.FLOWING;
    public float stateTime = 0f;
    public float animationSpeed = 1.0f / 12.0f; // 12 fps
    public Direction orientation = Direction.LEFT; // Default orientation
}