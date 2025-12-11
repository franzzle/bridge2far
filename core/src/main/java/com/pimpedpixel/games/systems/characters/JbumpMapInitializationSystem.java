package com.pimpedpixel.games.systems.characters;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.World;
import com.pimpedpixel.games.DesignResolution;

/**
 * Builds all static Jbump collision geometry from the Tiled map.
 * Collision is determined by TILE PROPERTY: collision = true
 * (added inside the .tsx file, not the GUI if disabled).
 */
public class JbumpMapInitializationSystem extends BaseSystem {

    private final TiledMap map;
    private final World<Object> jbumpWorld;
    private final String groundLayerName;

    private static final String STATIC_ITEM_IDENTIFIER = "MAP_COLLISION";
    private static final float SCALE = DesignResolution.ASSET_SCALE;

    public JbumpMapInitializationSystem(
        TiledMap map,
        World<Object> jbumpWorld,
        String groundLayerName
    ) {
        this.map = map;
        this.jbumpWorld = jbumpWorld;
        this.groundLayerName = groundLayerName;
    }

    @Override
    protected void initialize() {

        Gdx.app.log("JbumpMapInitializationSystem",
            "Starting collision map build...");

        MapLayers layers = map.getLayers();

        TiledMapTileLayer ground = (TiledMapTileLayer) layers.get(groundLayerName);

        if (exitIfMissing(ground, groundLayerName)) return;

        addCollisionLayer(ground);

        Gdx.app.log("JbumpMapInitializationSystem",
            "Collision setup complete. Total static items in world: "
                + jbumpWorld.getItems().size());

        // Disable system — initialization only
        setEnabled(false);
    }

    private boolean exitIfMissing(TiledMapTileLayer layer, String name) {
        if (layer == null) {
            Gdx.app.error("JbumpMapInitializationSystem",
                "Collision layer '" + name + "' not found!");
            return true;
        }
        return false;
    }

    /** Build collisions from a layer. Every tile with property collision=true is solid. */
    private void addCollisionLayer(TiledMapTileLayer layer) {

        float rawTileWidth = layer.getTileWidth();
        float rawTileHeight = layer.getTileHeight();

        float scaledTileWidth = rawTileWidth * SCALE;
        float scaledTileHeight = rawTileHeight * SCALE;

        boolean logOnce = true;

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {

                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell == null) continue;

                TiledMapTile tile = cell.getTile();
                if (tile == null) continue;

                // TILE PROPERTY CHECK — safest method
                boolean isCollision =
                    tile.getProperties().get("collision",  true, Boolean.class);

                if (!isCollision) continue;

                // Log once so we know collision is actually detected
                if (logOnce) {
                    Gdx.app.log("JbumpMapInitializationSystem",
                        "Detected collision tiles via property on layer '" + layer.getName() + "'.");
                    logOnce = false;
                }

                // Convert tile coords → world coords (scaled)
                float rawWorldX = x * rawTileWidth;
                float rawWorldY = y * rawTileHeight;

                float worldX = rawWorldX * SCALE;
                float worldY = rawWorldY * SCALE;

                // Create static Jbump item
                Item<Object> item = new Item<>(STATIC_ITEM_IDENTIFIER);
                jbumpWorld.add(item, worldX, worldY, scaledTileWidth, scaledTileHeight);
            }
        }

        // Add boundary walls around the playable area
        addBoundaryWalls(layer.getWidth(), layer.getHeight(), scaledTileWidth, scaledTileHeight);

        for (Item<Object> item : jbumpWorld.getItems()) {
            float x = jbumpWorld.getRect(item).x;
            float y = jbumpWorld.getRect(item).y;
            float w = jbumpWorld.getRect(item).w;
            float h = jbumpWorld.getRect(item).h;

            Gdx.app.log("COLLISION_DEBUG",
                item.userData + " at " + x + "," + y + " size " + w + "," + h);
        }

    }

    /**
     * Adds boundary walls around the entire playable area to prevent the player
     * from leaving the room and falling into the abyss.
     */
    private void addBoundaryWalls(int mapWidth, int mapHeight, float tileWidth, float tileHeight) {
        
        // Calculate the total map dimensions
        float mapWidthPixels = mapWidth * tileWidth;
        float mapHeightPixels = mapHeight * tileHeight;
        
        // Create boundary walls (1 tile thick)
        String boundaryIdentifier = "BOUNDARY_WALL";
        
        // Left wall - placed just left of the map
        Item<Object> leftWall = new Item<>(boundaryIdentifier);
        jbumpWorld.add(leftWall, -tileWidth, 0, tileWidth, mapHeightPixels);
        
        // Right wall - placed just right of the map
        Item<Object> rightWall = new Item<>(boundaryIdentifier);
        jbumpWorld.add(rightWall, mapWidthPixels, 0, tileWidth, mapHeightPixels);
        
        // Bottom wall - placed just below the map
        Item<Object> bottomWall = new Item<>(boundaryIdentifier);
        jbumpWorld.add(bottomWall, 0, -tileHeight, mapWidthPixels, tileHeight);
        
        // Top wall - placed just above the map
        Item<Object> topWall = new Item<>(boundaryIdentifier);
        jbumpWorld.add(topWall, 0, mapHeightPixels, mapWidthPixels, tileHeight);
        
        Gdx.app.log("JbumpMapInitializationSystem", 
            "Added boundary walls: Left at x=" + (-tileWidth) + ", Right at x=" + mapWidthPixels + 
            ", Bottom at y=" + (-tileHeight) + ", Top at y=" + mapHeightPixels);
    }

    @Override
    protected void processSystem() {
        // Not used — initialization only
    }
}
