package com.pimpedpixel.games.systems.characters;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.World;
import com.pimpedpixel.games.DesignResolution; // Assuming ASSET_SCALE is here

/**
 * Initializes the Jbump World by mapping cells from the TiledMap
 * collision layer into static Jbump Items, applying the ASSET_SCALE.
 */
public class JbumpMapInitializationSystem extends BaseSystem {

    private final TiledMap map;
    private final World<Object> jbumpWorld; // Using World<Object> for compatibility
    private final String collisionLayerName;

    // The GID (Global Tile ID) that represents your collision tile (600 in your CSV data)
    private static final int COLLISION_GID = 600;

    // Identifier for static geometry items
    private static final String STATIC_ITEM_IDENTIFIER = "MAP_COLLISION";

    // Retrieve the scale factor used in rendering
    private static final float SCALE = DesignResolution.ASSET_SCALE;

    public JbumpMapInitializationSystem(TiledMap map,
                                        World<Object> jbumpWorld, // Changed to Object
                                        String collisionLayerName) {
        this.map = map;
        this.jbumpWorld = jbumpWorld;
        this.collisionLayerName = collisionLayerName;
    }

    @Override
    protected void initialize() {
        Gdx.app.log("JbumpMapInitializationSystem", "Starting map collision setup...");

        MapLayers layers = map.getLayers();
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) layers.get(collisionLayerName);

        if (collisionLayer == null) {
            Gdx.app.error("JbumpMapInitializationSystem",
                "Collision layer '" + collisionLayerName + "' not found!");
            return;
        }

        // Get raw tile dimensions
        float rawTileWidth = collisionLayer.getTileWidth();
        float rawTileHeight = collisionLayer.getTileHeight();

        // Calculate scaled tile dimensions for the Jbump Rect
        float scaledTileWidth = rawTileWidth * SCALE;
        float scaledTileHeight = rawTileHeight * SCALE;

        // Iterate over the entire layer
        for (int x = 0; x < collisionLayer.getWidth(); x++) {
            for (int y = 0; y < collisionLayer.getHeight(); y++) {

                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);

                if (cell != null && cell.getTile() != null) {
                    TiledMapTile tile = cell.getTile();

                    if (tile.getId() == COLLISION_GID) {

                        // Calculate raw world position
                        float rawWorldX = x * rawTileWidth;
                        float rawWorldY = y * rawTileHeight;

                        // --- APPLY SCALE TO WORLD COORDINATES ---
                        float worldX = rawWorldX * SCALE;
                        float worldY = rawWorldY * SCALE;

                        // Create the Jbump Item
                        // Since jbumpWorld is World<Object>, we cast the user data
                        Item<Object> item = new Item<>(STATIC_ITEM_IDENTIFIER);

                        // Add the item to the world using the SCALED dimensions
                        jbumpWorld.add(item, worldX, worldY, scaledTileWidth, scaledTileHeight);
                    }
                }
            }
        }

        Gdx.app.log("JbumpMapInitializationSystem",
            "Completed map collision setup. Jbump World contains " + jbumpWorld.getItems().size() + " static items.");

        // Disable the system as its job is done
        this.setEnabled(false);
    }

    @Override
    protected void processSystem() {
        // Initialization system
    }
}
