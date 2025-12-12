package com.pimpedpixel.games.systems.characters;

import com.dongbat.jbump.World;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Debug test to understand collision detection issues.
 */
public class ZebraCollisionDebugTest {

    private World<Object> jbumpWorld;
    private ZebraStateSystem zebraSystem;

    @Before
    public void setUp() {
        jbumpWorld = new World<>();
        zebraSystem = new ZebraStateSystem(jbumpWorld);
        
        // Add a single test collision block
        Item<Object> testBlock = new Item<>("MAP_COLLISION");
        jbumpWorld.add(testBlock, 500f, 150f, 64f, 64f);
        
        System.out.println("Added collision block at: x=" + 500f + ", y=" + 150f + ", w=" + 64f + ", h=" + 64f);
    }

    @Test
    public void debugCollisionDetection() {
        // Test position that should definitely collide (inside the block)
        boolean collisionInside = zebraSystem.wouldCollide(500f, 150f, 32f, 32f);
        System.out.println("Collision at (500, 150) - inside block: " + collisionInside);
        
        // Test position that should definitely NOT collide (far away)
        boolean collisionFar = zebraSystem.wouldCollide(100f, 100f, 32f, 32f);
        System.out.println("Collision at (100, 100) - far away: " + collisionFar);
        
        // Test position at edge of block
        boolean collisionEdge = zebraSystem.wouldCollide(564f, 150f, 32f, 32f); // x = 500 + 64
        System.out.println("Collision at (564, 150) - at edge: " + collisionEdge);
        
        // Test position just outside block
        boolean collisionOutside = zebraSystem.wouldCollide(570f, 150f, 32f, 32f);
        System.out.println("Collision at (570, 150) - just outside: " + collisionOutside);
        
        // Debug: Print the block position
        for (Item<Object> item : jbumpWorld.getItems()) {
            if (item.userData.equals("MAP_COLLISION")) {
                Rect rect = jbumpWorld.getRect(item);
                System.out.println("Block position: x=" + rect.x + ", y=" + rect.y + ", w=" + rect.w + ", h=" + rect.h);
            }
        }
        
        // Basic assertions
        assertTrue("Should collide inside block", collisionInside);
        assertFalse("Should not collide far away", collisionFar);
    }

    @Test
    public void debugBoundaryCollision() {
        // Add boundary walls
        Item<Object> leftWall = new Item<>("BOUNDARY_WALL");
        jbumpWorld.add(leftWall, -64f, 0f, 64f, 800f);
        
        Item<Object> rightWall = new Item<>("BOUNDARY_WALL");
        jbumpWorld.add(rightWall, 1280f, 0f, 64f, 800f);
        
        System.out.println("Added left wall at: x=" + -64f + ", y=" + 0f + ", w=" + 64f + ", h=" + 800f);
        System.out.println("Added right wall at: x=" + 1280f + ", y=" + 0f + ", w=" + 64f + ", h=" + 800f);
        
        // Test collision with left boundary
        boolean leftCollision = zebraSystem.wouldCollide(-32f, 100f, 32f, 32f);
        System.out.println("Collision at (-32, 100) - near left wall: " + leftCollision);
        
        // Test collision with right boundary
        boolean rightCollision = zebraSystem.wouldCollide(1280f, 100f, 32f, 32f);
        System.out.println("Collision at (1280, 100) - at right wall: " + rightCollision);
        
        // Test no collision in middle
        boolean middleCollision = zebraSystem.wouldCollide(600f, 100f, 32f, 32f);
        System.out.println("Collision at (600, 100) - in middle: " + middleCollision);
        
        // Debug: Print boundary positions
        for (Item<Object> item : jbumpWorld.getItems()) {
            if (item.userData.equals("BOUNDARY_WALL")) {
                Rect rect = jbumpWorld.getRect(item);
                System.out.println("Boundary wall: x=" + rect.x + ", y=" + rect.y + ", w=" + rect.w + ", h=" + rect.h);
            }
        }
    }
}