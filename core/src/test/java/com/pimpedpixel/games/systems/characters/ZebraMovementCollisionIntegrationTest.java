package com.pimpedpixel.games.systems.characters;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.World;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Integration test to verify that zebra movement properly respects jbump collision boundaries.
 */
public class ZebraMovementCollisionIntegrationTest {

    private com.artemis.World artemisWorld;
    private com.dongbat.jbump.World<Object> jbumpWorld;
    private ZebraStateSystem zebraSystem;

    @Before
    public void setUp() {
        // Create Artemis world
        WorldConfiguration config = new WorldConfigurationBuilder()
            .with(
                new ZebraStateSystem(jbumpWorld = new com.dongbat.jbump.World<>())
            )
            .build();
        
        artemisWorld = new com.artemis.World(config);
        zebraSystem = artemisWorld.getSystem(ZebraStateSystem.class);
        
        // Setup collision environment
        setupCollisionEnvironment();
    }

    private void setupCollisionEnvironment() {
        // Add boundary walls
        jbumpWorld.add(new Item<>("BOUNDARY_WALL"), -64f, 0f, 64f, 800f);
        jbumpWorld.add(new Item<>("BOUNDARY_WALL"), 1280f, 0f, 64f, 800f);
        
        // Add collision blocks that create a "corridor" 
        // Left barrier: blocks from y=0 to y=400 at x=200
        for (int y = 0; y < 400; y += 64) {
            jbumpWorld.add(new Item<>("MAP_COLLISION"), 200f, (float) y, 64f, 64f);
        }
        
        // Right barrier: blocks from y=0 to y=400 at x=800
        for (int y = 0; y < 400; y += 64) {
            jbumpWorld.add(new Item<>("MAP_COLLISION"), 800f, (float) y, 64f, 64f);
        }
        
        // Bottom barrier: blocks from x=200 to x=800 at y=0
        for (int x = 264; x < 800; x += 64) {
            jbumpWorld.add(new Item<>("MAP_COLLISION"), (float) x, 0f, 64f, 64f);
        }
    }

    @Test
    public void testZebraMovementRespectsBoundaryWalls() {
        // Test that zebra cannot move beyond boundary walls
        
        // Test movement towards left boundary
        boolean leftCollision = zebraSystem.wouldPathCollide(100f, -100f, 200f, 30f, 40f, 5);
        assertTrue("Zebra should collide with left boundary wall", leftCollision);
        
        // Test movement towards right boundary  
        boolean rightCollision = zebraSystem.wouldPathCollide(1200f, 1300f, 200f, 30f, 40f, 5);
        assertTrue("Zebra should collide with right boundary wall", rightCollision);
        
        // Test movement within safe bounds
        boolean safeMovement = zebraSystem.wouldPathCollide(500f, 600f, 200f, 30f, 40f, 5);
        assertFalse("Zebra should move freely within bounds", safeMovement);
    }

    @Test
    public void testZebraMovementRespectsCollisionBlocks() {
        // Test that zebra cannot walk through collision blocks
        
        // Test movement through left barrier (x=200)
        boolean leftBarrierCollision = zebraSystem.wouldPathCollide(100f, 300f, 200f, 30f, 40f, 5);
        assertTrue("Zebra should collide with left barrier", leftBarrierCollision);
        
        // Test movement through right barrier (x=800)
        boolean rightBarrierCollision = zebraSystem.wouldPathCollide(700f, 900f, 200f, 30f, 40f, 5);
        assertTrue("Zebra should collide with right barrier", rightBarrierCollision);
        
        // Test movement through bottom barrier (y=0) - need to test at lower y position
        boolean bottomBarrierCollision = zebraSystem.wouldPathCollide(500f, 500f, 20f, 30f, 40f, 5);
        assertTrue("Zebra should collide with bottom barrier", bottomBarrierCollision);
        
        // Test movement within the corridor (should be clear)
        boolean corridorMovement = zebraSystem.wouldPathCollide(300f, 700f, 300f, 30f, 40f, 5);
        assertFalse("Zebra should move freely within corridor", corridorMovement);
    }

    @Test
    public void testZebraMovementPathFinding() {
        // Test that zebra can find valid paths around obstacles
        
        // Test movement that would hit a barrier but can go around
        // This is more complex and would require actual pathfinding, 
        // but for now we test that the zebra stops when it would collide
        
        // Test a path that starts clear but ends in collision
        boolean partialPathCollision = zebraSystem.wouldPathCollide(400f, 250f, 200f, 30f, 40f, 5);
        assertTrue("Zebra should detect collision when path ends in barrier", partialPathCollision);
        
        // Test a completely clear path
        boolean clearPath = zebraSystem.wouldPathCollide(400f, 600f, 300f, 30f, 40f, 5);
        assertFalse("Zebra should move on completely clear path", clearPath);
    }

    @Test
    public void testZebraCollisionDetectionEdgeCases() {
        // Test edge cases in collision detection
        
        // Test with very small movement steps
        boolean smallStepCollision = zebraSystem.wouldPathCollide(190f, 210f, 200f, 30f, 40f, 10);
        assertTrue("Zebra should detect collision even with small steps", smallStepCollision);
        
        // Test with very large movement steps - use more steps to ensure collision detection
        boolean largeStepCollision = zebraSystem.wouldPathCollide(100f, 900f, 200f, 30f, 40f, 10);
        assertTrue("Zebra should detect collision with large steps", largeStepCollision);
        
        // Test at exact boundary positions
        boolean exactBoundary = zebraSystem.wouldPathCollide(199f, 201f, 200f, 30f, 40f, 5);
        assertTrue("Zebra should detect collision at exact boundaries", exactBoundary);
    }

    @Test
    public void testZebraMovementPerformanceWithCollisions() {
        // Test performance of collision detection with many checks
        long startTime = System.nanoTime();
        
        // Perform many path collision checks
        int checks = 0;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 10; j++) {
                float startX = 100f + i * 10f;
                float endX = 200f + i * 10f;
                float y = 100f + j * 50f;
                zebraSystem.wouldPathCollide(startX, endX, y, 30f, 40f, 5);
                checks++;
            }
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("Path collision detection performance: " + durationMs + "ms for " + checks + " checks");
        
        // Should complete in reasonable time (less than 500ms for 1000 checks)
        assertTrue("Path collision detection should be performant", durationMs < 500);
    }
}