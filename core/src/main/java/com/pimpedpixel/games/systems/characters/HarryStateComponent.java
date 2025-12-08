package com.pimpedpixel.games.systems.characters;

import com.artemis.Component;

public class HarryStateComponent extends Component {
    public HarryState state = HarryState.RESTING;
    public HarryState previousState = HarryState.RESTING; // <— add this
    public Direction dir = Direction.RIGHT;
    public float stateTime = 0f;
}
