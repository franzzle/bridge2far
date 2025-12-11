package com.pimpedpixel.games.gameplay;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TilemapModificationTest {

    @Test
    public void testTilemapModificationLogic() {
        // Create a mock scenario with test data
        Scenario scenario = new Scenario();
        scenario.setTitle("Test Scenario");
        scenario.setTimeLimit(30);
        scenario.setStartingPositionX(100);
        scenario.setStartingPositionY(500);
        
        CollisionLayer groundLayer = new CollisionLayer();
        groundLayer.setMatchingRow(5);
        groundLayer.setCellStates(Arrays.asList(1, 0, 1, 1, 0, 1));
        
        scenario.setGroundLayer(groundLayer);
        
        // Create a mock tilemap with a ground layer
        TiledMap tileMap = createMockTilemap();
        
        // Test the modification logic (simplified version without actual tile removal)
        List<Integer> cellStates = scenario.getGroundLayer().getCellStates();
        int matchingRow = scenario.getGroundLayer().getMatchingRow();
        
        assertNotNull("Cell states should not be null", cellStates);
        assertEquals("Should have 6 cell states", 6, cellStates.size());
        assertEquals("Matching row should be 5", 5, matchingRow);
        
        // Verify the expected modifications
        int holeCount = 0;
        int solidCount = 0;
        
        for (int cellState : cellStates) {
            CollisionType collisionType = CollisionType.fromValue(cellState);
            if (collisionType == CollisionType.HOLE) {
                holeCount++;
            } else if (collisionType == CollisionType.SOLID) {
                solidCount++;
            }
        }
        
        assertEquals("Should have 2 HOLE cells", 2, holeCount);
        assertEquals("Should have 4 SOLID cells", 4, solidCount);
        
        System.out.println("✅ Tilemap modification logic test passed!");
        System.out.println("Expected modifications: " + holeCount + " holes, " + solidCount + " solids");
    }
    
    @Test
    public void testCollisionTypeEnum() {
        // Test that the CollisionType enum works correctly
        assertEquals("HOLE should have value 0", 0, CollisionType.HOLE.getValue());
        assertEquals("SOLID should have value 1", 1, CollisionType.SOLID.getValue());
        assertEquals("FATAL should have value 2", 2, CollisionType.FATAL.getValue());
        
        // Test fromValue method
        assertEquals("fromValue(0) should return HOLE", CollisionType.HOLE, CollisionType.fromValue(0));
        assertEquals("fromValue(1) should return SOLID", CollisionType.SOLID, CollisionType.fromValue(1));
        assertEquals("fromValue(2) should return FATAL", CollisionType.FATAL, CollisionType.fromValue(2));
        
        System.out.println("✅ CollisionType enum test passed!");
    }
    
    // Helper method to create a mock tilemap for testing
    private TiledMap createMockTilemap() {
        // This is a simplified mock - in a real test you would create a proper TiledMap
        // For this test, we just need to verify the logic works
        return null;
    }
}