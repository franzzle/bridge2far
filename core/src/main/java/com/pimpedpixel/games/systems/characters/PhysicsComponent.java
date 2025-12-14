package com.pimpedpixel.games.systems.characters;

import com.artemis.Component;

public class PhysicsComponent extends Component {
    public float vx;
    public float vy;
    public boolean onGround = false;
    public boolean onZebraSupport = false;
    public boolean lethalJump = false;
}
