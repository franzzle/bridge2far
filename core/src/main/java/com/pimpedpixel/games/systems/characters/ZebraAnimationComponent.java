package com.pimpedpixel.games.systems.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.artemis.Component;

public class ZebraAnimationComponent extends Component {
    public Animation<TextureRegion> grazingLeft;
    public Animation<TextureRegion> grazingRight;
    public Animation<TextureRegion> walkingLeft;
    public Animation<TextureRegion> walkingRight;
    public Animation<TextureRegion> shreddingLeft;
    public Animation<TextureRegion> shreddingRight;
}