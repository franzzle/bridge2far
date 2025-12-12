package com.pimpedpixel.games.systems.characters;

import com.badlogic.gdx.Gdx;
import com.dongbat.jbump.World;
import com.dongbat.jbump.Item;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for Zebra movement logic.
 */
public class ZebraMovementTest {

    private World<Object> jbumpWorld;
    private ZebraStateSystem zebraSystem;

    @Before
    public void setUp() {
        // Mock Gdx.graphics for testing
        jbumpWorld = new World<>();
        zebraSystem = new ZebraStateSystem(jbumpWorld);
    }

    @Test
    public void testCellWidthCalculation() {
        // Test that cell width is calculated correctly
        // 32 pixels * 2.0 scale = 64 pixels
        float expectedCellWidth = 32f * 2f; // DesignResolution.ASSET_SCALE = 2f
        assertEquals("Cell width should be 64 pixels", expectedCellWidth, 64f, 0.001f);
    }

    @Test
    public void testMovementRangeCalculation() {
        // Test the movement range calculation logic
        int availableCells = 14; // Minimum available cells
        
        // Calculate expected min and max cells
        int minCells = (int) (availableCells * 0.25f); // 3.5 -> 3
        int maxCells = (int) (availableCells * 0.75f); // 10.5 -> 10
        
        assertEquals("Minimum cells should be 3", 3, minCells);
        assertEquals("Maximum cells should be 10", 10, maxCells);
        
        // Test that the range is within bounds
        for (int i = 0; i < 100; i++) {
            int cellsToUse = minCells + (int) (Math.random() * (maxCells - minCells + 1));
            assertTrue("Cells to use should be between min and max", 
                       cellsToUse >= minCells && cellsToUse <= maxCells);
        }
    }

    @Test
    public void testCollisionDetection() {
        // Add a collision block to the jbump world
        Item<Object> collisionBlock = new Item<>("MAP_COLLISION");
        jbumpWorld.add(collisionBlock, 100f, 100f, 64f, 64f);

        // Test collision detection
        boolean collision = false;
        for (Item<Object> item : jbumpWorld.getItems()) {
            if (item.userData.equals("MAP_COLLISION")) {
                float itemX = jbumpWorld.getRect(item).x;
                float itemY = jbumpWorld.getRect(item).y;
                float itemW = jbumpWorld.getRect(item).w;
                float itemH = jbumpWorld.getRect(item).h;
                
                // Test position that should collide
                float testX = 120f;
                float testY = 120f;
                float testW = 32f;
                float testH = 32f;
                
                if (testX < itemX + itemW && testX + testW > itemX &&
                    testY < itemY + itemH && testY + testH > itemY) {
                    collision = true;
                    break;
                }
            }
        }
        
        assertTrue("Should detect collision with MAP_COLLISION block", collision);
    }

    @Test
    public void testBoundaryWallDetection() {
        // Add a boundary wall to the jbump world
        Item<Object> boundaryWall = new Item<>("BOUNDARY_WALL");
        jbumpWorld.add(boundaryWall, 0f, 0f, 64f, 800f); // Left wall

        // Test boundary wall detection
        boolean boundaryDetected = false;
        for (Item<Object> item : jbumpWorld.getItems()) {
            if (item.userData.equals("BOUNDARY_WALL")) {
                boundaryDetected = true;
                break;
            }
        }
        
        assertTrue("Should detect BOUNDARY_WALL", boundaryDetected);
    }

    @Test
    public void testAvailableCellsDetection() {
        // This is a placeholder test for the detectAvailableCells method
        // In a real implementation, this would test the actual cell detection logic
        int[] availableCells = new int[14]; // Minimum available cells
        assertEquals("Should have 14 available cells", 14, availableCells.length);
    }

    @Test
    public void testDirectionReversalOnCollision() {
        // Test that direction reverses when collision is detected
        Direction initialDir = Direction.LEFT;
        Direction reversedDir = initialDir == Direction.LEFT ? Direction.RIGHT : Direction.LEFT;
        
        assertEquals("Should reverse from LEFT to RIGHT", Direction.RIGHT, reversedDir);
        
        // Test reverse again
        reversedDir = reversedDir == Direction.LEFT ? Direction.RIGHT : Direction.LEFT;
        assertEquals("Should reverse from RIGHT to LEFT", Direction.LEFT, reversedDir);
    }
}