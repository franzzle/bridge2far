package com.pimpedpixel.games.systems.characters;

import com.artemis.Component;

public class ZebraStateComponent extends Component {
    public ZebraState state;
    public ZebraState previousState = ZebraState.GRAZING;
    public Direction dir;
    public float stateTime;
}
