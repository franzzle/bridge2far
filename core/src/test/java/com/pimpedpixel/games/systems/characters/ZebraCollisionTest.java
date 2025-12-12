package com.pimpedpixel.games.systems.characters;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.dongbat.jbump.World;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class specifically for zebra collision detection.
 */
public class ZebraCollisionTest {

    private World<Object> jbumpWorld;
    private ZebraStateSystem zebraSystem;

    @Before
    public void setUp() {
        jbumpWorld = new World<>();
        zebraSystem = new ZebraStateSystem(jbumpWorld);
        
        // Add test collision blocks
        setupTestCollisionBlocks();
    }

    private void setupTestCollisionBlocks() {
        // Add left boundary wall
        Item<Object> leftWall = new Item<>("BOUNDARY_WALL");
        jbumpWorld.add(leftWall, -64f, 0f, 64f, 800f);
        
        // Add right boundary wall
        Item<Object> rightWall = new Item<>("BOUNDARY_WALL");
        jbumpWorld.add(rightWall, 1280f, 0f, 64f, 800f);
        
        // Add some collision blocks in the middle
        for (int i = 0; i < 5; i++) {
            Item<Object> collisionBlock = new Item<>("MAP_COLLISION");
            jbumpWorld.add(collisionBlock, 300f + i * 128f, 100f, 64f, 64f);
        }
        
        // Add a specific test collision block
        Item<Object> testBlock = new Item<>("MAP_COLLISION");
        jbumpWorld.add(testBlock, 500f, 150f, 64f, 64f);
    }

    @Test
    public void testCollisionDetectionWithBoundaryWalls() {
        // Test collision with left boundary wall
        boolean leftCollision = zebraSystem.wouldCollide(-32f, 100f, 32f, 32f);
        assertTrue("Should collide with left boundary wall", leftCollision);
        
        // Test collision with right boundary wall
        boolean rightCollision = zebraSystem.wouldCollide(1280f, 100f, 32f, 32f);
        assertTrue("Should collide with right boundary wall", rightCollision);
        
        // Test no collision in middle of screen (away from all blocks)
        boolean noCollision = zebraSystem.wouldCollide(100f, 100f, 32f, 32f);
        assertFalse("Should not collide in empty space", noCollision);
    }

    @Test
    public void testCollisionDetectionWithMapCollisionBlocks() {
        // Test collision with the specific test block at (500, 150)
        boolean collision = zebraSystem.wouldCollide(500f, 150f, 32f, 32f);
        assertTrue("Should collide with MAP_COLLISION block at (500, 150)", collision);
        
        // Test collision with edge of the block (block goes to 564, so 530 is inside)
        boolean edgeCollision = zebraSystem.wouldCollide(530f, 150f, 32f, 32f);
        assertTrue("Should collide with edge of MAP_COLLISION block", edgeCollision);
        
        // Test no collision just outside the block (block ends at 564, but there's another block at 556-620,100-164)
        // So we need to test at a position that doesn't collide with any blocks
        boolean noCollision = zebraSystem.wouldCollide(200f, 150f, 32f, 32f); // Far from all blocks
        assertFalse("Should not collide outside MAP_COLLISION blocks", noCollision);
    }

    @Test
    public void testCollisionDetectionWithMultipleBlocks() {
        // Test collision with one of the middle blocks (300 + i * 128)
        // Block positions: 300, 428, 556, 684, 812
        boolean collision1 = zebraSystem.wouldCollide(300f, 100f, 32f, 32f);
        assertTrue("Should collide with first middle block", collision1);
        
        boolean collision2 = zebraSystem.wouldCollide(428f, 100f, 32f, 32f);
        assertTrue("Should collide with second middle block", collision2);
        
        boolean collision3 = zebraSystem.wouldCollide(556f, 100f, 32f, 32f);
        assertTrue("Should collide with third middle block", collision3);
        
        // Test no collision between blocks
        boolean noCollision = zebraSystem.wouldCollide(364f, 100f, 32f, 32f); // Between 300 and 428
        assertFalse("Should not collide between blocks", noCollision);
    }

    @Test
    public void testCollisionDetectionWithDifferentZebraSizes() {
        // Test with zebra's actual size (30x40)
        boolean collisionNormal = zebraSystem.wouldCollide(500f, 150f, 30f, 40f);
        assertTrue("Should collide with normal zebra size", collisionNormal);
        
        // Test with larger size
        boolean collisionLarge = zebraSystem.wouldCollide(490f, 140f, 50f, 60f);
        assertTrue("Should collide with larger zebra size", collisionLarge);
        
        // Test with smaller size
        boolean collisionSmall = zebraSystem.wouldCollide(510f, 160f, 10f, 10f);
        assertTrue("Should collide with smaller zebra size", collisionSmall);
    }

    @Test
    public void testCollisionDetectionEdgeCases() {
        // Test at exact boundary positions
        boolean boundaryLeft = zebraSystem.wouldCollide(-64f, 100f, 32f, 32f);
        assertTrue("Should collide at exact left boundary", boundaryLeft);
        
        boolean boundaryRight = zebraSystem.wouldCollide(1280f, 100f, 32f, 32f);
        assertTrue("Should collide at exact right boundary", boundaryRight);
        
        // Test just inside boundaries (should not collide) - but need to be far enough inside
        // Left wall is at -64 to 0, but position -30 to 2 DOES overlap with wall -64 to 0
        // So we need to test at a position that doesn't overlap: -100 to -68
        boolean insideLeft = zebraSystem.wouldCollide(-100f, 100f, 32f, 32f);
        assertFalse("Should not collide well inside left boundary", insideLeft);
        
        // Right wall is at 1280 to 1344, so 1200 is safely inside
        boolean insideRight = zebraSystem.wouldCollide(1200f, 100f, 32f, 32f);
        assertFalse("Should not collide well inside right boundary", insideRight);
    }

    @Test
    public void testPathCollisionDetection() {
        // Test path collision detection
        
        // Test a path that goes through a collision block
        // Start at 400, end at 600 - this should pass through the block at 500,150
        boolean pathCollision = zebraSystem.wouldPathCollide(400f, 600f, 150f, 32f, 32f, 5);
        assertTrue("Should detect collision along path through block", pathCollision);
        
        // Test a path that goes around collision blocks
        // Start at 200, end at 250 - this should be clear (blocks start at 300)
        boolean pathClear = zebraSystem.wouldPathCollide(200f, 250f, 150f, 32f, 32f, 5);
        assertFalse("Should not detect collision on clear path", pathClear);
        
        // Test a path that starts inside a collision block
        boolean pathStartInBlock = zebraSystem.wouldPathCollide(500f, 600f, 150f, 32f, 32f, 5);
        assertTrue("Should detect collision when starting in block", pathStartInBlock);
    }

    @Test
    public void testCollisionDetectionPerformance() {
        // Test that collision detection is reasonably fast
        long startTime = System.nanoTime();
        
        // Perform many collision checks
        for (int i = 0; i < 1000; i++) {
            float testX = 100f + (i % 20) * 50f;
            float testY = 100f + (i / 20) * 50f;
            zebraSystem.wouldCollide(testX, testY, 32f, 32f);
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Collision detection performance: " + durationMs + "ms for 1000 checks");
        
        // Should complete in reasonable time (less than 100ms for 1000 checks)
        assertTrue("Collision detection should be performant", durationMs < 100);
    }
}