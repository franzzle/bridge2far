package com.pimpedpixel.games.systems.characters;

import com.artemis.Component;
import com.dongbat.jbump.World;

public class PhysicsWorldComponent extends Component {
    public World jbumpWorld;

    public PhysicsWorldComponent(World world) {
        this.jbumpWorld = world;
    }
}
