package com.pimpedpixel.games.systems.playfield;

import com.artemis.Component;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class TiledMapComponent extends Component {
    public TiledMap map;
    public OrthogonalTiledMapRenderer renderer;
}
