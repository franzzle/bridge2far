package com.pimpedpixel.games.systems.playfield;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.IntArray;

/**
 * Renders one or more background layers of a Tiled map, behind characters.
 * Layers are specified by name instead of indices.
 */
public class MapBackgroundRenderSystem extends BaseSystem {

    private final OrthogonalTiledMapRenderer renderer;
    private final OrthographicCamera camera;
    private final String[] layerNames;

    private int[] layerIndices; // resolved once from names

    public MapBackgroundRenderSystem(OrthogonalTiledMapRenderer renderer,
                                     OrthographicCamera camera,
                                     String... layerNames) {
        this.renderer = renderer;
        this.camera = camera;
        this.layerNames = layerNames;
    }

    @Override
    protected void initialize() {
        resolveLayerIndices();
    }

    private void resolveLayerIndices() {
        TiledMap map = renderer.getMap();
        MapLayers layers = map.getLayers();

        IntArray indices = new IntArray();

        for (String name : layerNames) {
            int idx = layers.getIndex(name);
            if (idx == -1) {
                Gdx.app.log("MapBackgroundRenderSystem",
                    "Layer not found: \"" + name + "\"");
            } else {
                indices.add(idx);
            }
        }

        if (indices.size == 0) {
            Gdx.app.log("MapBackgroundRenderSystem",
                "No valid layers resolved for background rendering.");
            layerIndices = new int[0];
        } else {
            layerIndices = indices.toArray();
        }
    }

    @Override
    protected void processSystem() {
        if (layerIndices == null || layerIndices.length == 0) {
            return;
        }

        camera.update();
        renderer.setView(camera);
        renderer.render(layerIndices);
    }
}
