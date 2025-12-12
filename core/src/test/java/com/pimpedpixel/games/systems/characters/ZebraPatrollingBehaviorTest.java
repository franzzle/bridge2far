package com.pimpedpixel.games.systems.characters;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.World;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for zebra deterministic patrolling behavior.
 * Tests that zebras walk as far left as possible, then reverse to walk as far right as possible.
 */
public class ZebraPatrollingBehaviorTest {

    private World<Object> jbumpWorld;
    private ZebraStateSystem zebraSystem;

    @Before
    public void setUp() {
        jbumpWorld = new World<>();
        zebraSystem = new ZebraStateSystem(jbumpWorld);
        
        // Setup collision environment for patrolling test
        setupPatrollingEnvironment();
    }

    private void setupPatrollingEnvironment() {
        // Create a corridor for patrolling
        // Left boundary at x=100
        jbumpWorld.add(new Item<>("MAP_COLLISION"), 100f, 0f, 64f, 800f);
        
        // Right boundary at x=900
        jbumpWorld.add(new Item<>("MAP_COLLISION"), 900f, 0f, 64f, 800f);
        
        // Add some obstacles in the middle
        jbumpWorld.add(new Item<>("MAP_COLLISION"), 400f, 100f, 64f, 64f);
        jbumpWorld.add(new Item<>("MAP_COLLISION"), 600f, 200f, 64f, 64f);
    }

    @Test
    public void testDeterministicLeftMovement() {
        // Test that zebra tries to move as far left as possible
        
        // Start position in the middle
        float startX = 500f;
        float y = 150f;
        float width = 30f;
        float height = 40f;
        float maxRange = 400f; // Would go to x=100, but collision at x=100
        
        // Find maximum left movement
        float maxLeft = zebraSystem.findMaximumLeftMovement(startX, y, width, height, maxRange);
        
        System.out.println("Starting from " + startX + ", max left movement: " + maxLeft);
        
        // Should move left but stop before hitting the left boundary at x=100
        assertTrue("Should move left from starting position", maxLeft < startX);
        assertTrue("Should not go beyond left boundary", maxLeft > 100f);
        
        // Verify no collision at the found position
        boolean collision = zebraSystem.wouldPathCollide(startX, maxLeft, y, width, height, 5);
        assertFalse("Found position should not collide", collision);
    }

    @Test
    public void testDeterministicRightMovement() {
        // Test that zebra tries to move as far right as possible
        
        // Start position in the middle
        float startX = 500f;
        float y = 150f;
        float width = 30f;
        float height = 40f;
        float maxRange = 400f; // Would go to x=900, but collision at x=900
        
        // Find maximum right movement
        float maxRight = zebraSystem.findMaximumRightMovement(startX, y, width, height, maxRange);
        
        System.out.println("Starting from " + startX + ", max right movement: " + maxRight);
        
        // Should move right but stop before hitting the right boundary at x=900
        assertTrue("Should move right from starting position", maxRight > startX);
        assertTrue("Should not go beyond right boundary", maxRight < 900f);
        
        // Verify no collision at the found position
        boolean collision = zebraSystem.wouldPathCollide(startX, maxRight, y, width, height, 5);
        assertFalse("Found position should not collide", collision);
    }

    @Test
    public void testPatrollingDirectionReversal() {
        // Test the complete patrolling behavior with direction reversal
        
        // Simulate zebra starting position
        float startX = 500f;
        float y = 150f;
        float width = 30f;
        float height = 40f;
        float maxRange = 400f;
        
        // First movement: try to go left as far as possible
        float leftTarget = startX - maxRange;
        boolean leftCollision = zebraSystem.wouldPathCollide(startX, leftTarget, y, width, height, 5);
        
        System.out.println("Left movement from " + startX + " to " + leftTarget + " collides: " + leftCollision);
        
        // Should detect collision when trying to go all the way left
        assertTrue("Should detect collision when moving full range left", leftCollision);
        
        // Find maximum safe left movement
        float maxLeft = zebraSystem.findMaximumLeftMovement(startX, y, width, height, maxRange);
        
        // If we hit left boundary, reverse to right
        if (maxLeft == startX) {
            // Try right movement
            float rightTarget = startX + maxRange;
            boolean rightCollision = zebraSystem.wouldPathCollide(startX, rightTarget, y, width, height, 5);
            
            System.out.println("Right movement from " + startX + " to " + rightTarget + " collides: " + rightCollision);
            
            // Should also detect collision when trying to go all the way right
            assertTrue("Should detect collision when moving full range right", rightCollision);
            
            // Find maximum safe right movement
            float maxRight = zebraSystem.findMaximumRightMovement(startX, y, width, height, maxRange);
            
            System.out.println("Max left: " + maxLeft + ", Max right: " + maxRight);
            
            // Should find some movement in at least one direction
            assertTrue("Should be able to move in at least one direction", maxLeft < startX || maxRight > startX);
        }
    }

    @Test
    public void testBinarySearchPrecision() {
        // Test that binary search provides good precision
        
        float startX = 500f;
        float y = 150f;
        float width = 30f;
        float height = 40f;
        float maxRange = 400f;
        
        // Find maximum movements
        float maxLeft = zebraSystem.findMaximumLeftMovement(startX, y, width, height, maxRange);
        float maxRight = zebraSystem.findMaximumRightMovement(startX, y, width, height, maxRange);
        
        // Test that we can move reasonably far in both directions
        float leftDistance = startX - maxLeft;
        float rightDistance = maxRight - startX;
        
        System.out.println("Left distance: " + leftDistance + ", Right distance: " + rightDistance);
        
        // Should be able to move significant distance in at least one direction
        assertTrue("Should move significant distance left or right", 
                   leftDistance > 100f || rightDistance > 100f);
        
        // Test precision: the found positions should be very close to collision boundaries
        // Move a little further and should hit collision
        float leftTest = maxLeft - 10f;
        float rightTest = maxRight + 10f;
        
        boolean leftNearCollision = zebraSystem.wouldPathCollide(startX, leftTest, y, width, height, 5);
        boolean rightNearCollision = zebraSystem.wouldPathCollide(startX, rightTest, y, width, height, 5);
        
        System.out.println("Left near collision: " + leftNearCollision + ", Right near collision: " + rightNearCollision);
        
        // At least one direction should be near collision boundary
        assertTrue("Should be near collision boundary in at least one direction", 
                   leftNearCollision || rightNearCollision);
    }

    @Test
    public void testPatrollingInCorridor() {
        // Test patrolling behavior in a corridor environment
        
        // Clear a path in the middle
        float corridorY = 300f; // Above the obstacles
        float startX = 500f;
        float width = 30f;
        float height = 40f;
        float maxRange = 400f;
        
        // Should be able to move freely in the corridor
        float leftTarget = startX - maxRange;
        float rightTarget = startX + maxRange;
        
        boolean leftCorridorCollision = zebraSystem.wouldPathCollide(startX, leftTarget, corridorY, width, height, 5);
        boolean rightCorridorCollision = zebraSystem.wouldPathCollide(startX, rightTarget, corridorY, width, height, 5);
        
        System.out.println("Corridor left collision: " + leftCorridorCollision);
        System.out.println("Corridor right collision: " + rightCorridorCollision);
        
        // Should hit boundaries in both directions
        assertTrue("Should hit left boundary in corridor", leftCorridorCollision);
        assertTrue("Should hit right boundary in corridor", rightCorridorCollision);
        
        // Find maximum movements in corridor
        float maxLeft = zebraSystem.findMaximumLeftMovement(startX, corridorY, width, height, maxRange);
        float maxRight = zebraSystem.findMaximumRightMovement(startX, corridorY, width, height, maxRange);
        
        System.out.println("Corridor max left: " + maxLeft + ", max right: " + maxRight);
        
        // Should be able to move significant distance in both directions
        assertTrue("Should move left in corridor", maxLeft < startX);
        assertTrue("Should move right in corridor", maxRight > startX);
    }
}