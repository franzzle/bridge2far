package com.pimpedpixel.games.systems.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.artemis.Component;

public class HarryAnimationComponent extends Component {
    public Animation<TextureRegion> restingLeft;
    public Animation<TextureRegion> restingRight;
    public Animation<TextureRegion> walkingLeft;
    public Animation<TextureRegion> walkingRight;
    public Animation<TextureRegion> jumpingLeft;
    public Animation<TextureRegion> jumpingRight;
    public Animation<TextureRegion> fallingLeft;
    public Animation<TextureRegion> fallingRight;
    public Animation<TextureRegion> dyingLeft;
    public Animation<TextureRegion> dyingRight;
}
