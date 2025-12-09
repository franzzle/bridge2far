package com.pimpedpixel.games.systems.characters;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.pimpedpixel.games.DesignResolution;

public class CollisionDebugRenderSystem extends BaseSystem {
    private final TiledMap map;
    private final String collisionLayerName;
    private final ShapeRenderer shapeRenderer;
    private static final int COLLISION_GID = 600; // Match this with your collision tile GID
    private static final float SCALE = DesignResolution.ASSET_SCALE;

    public CollisionDebugRenderSystem(TiledMap map, String collisionLayerName) {
        this.map = map;
        this.collisionLayerName = collisionLayerName;
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    protected void dispose() {
        shapeRenderer.dispose();
    }

    @Override
    protected void processSystem() {
        // Enable transparency blending
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0f, 1f, 0.5f); // Magenta with 50% transparency

        MapLayers layers = map.getLayers();
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) layers.get(collisionLayerName);

        if (collisionLayer == null) {
            Gdx.app.error("CollisionDebugRenderSystem", "Collision layer '" + collisionLayerName + "' not found!");
            return;
        }

        // Iterate over the collision layer
        for (int x = 0; x < collisionLayer.getWidth(); x++) {
            for (int y = 0; y < collisionLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    TiledMapTile tile = cell.getTile();
                    if (tile.getId() == COLLISION_GID) {
                        // Calculate world position and dimensions
                        float worldX = x * collisionLayer.getTileWidth() * SCALE;
                        float worldY = y * collisionLayer.getTileHeight() * SCALE;
                        float width = collisionLayer.getTileWidth() * SCALE;
                        float height = collisionLayer.getTileHeight() * SCALE;

                        // Draw a semi-transparent magenta rectangle
                        shapeRenderer.rect(worldX, worldY, width, height);
                    }
                }
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
}
