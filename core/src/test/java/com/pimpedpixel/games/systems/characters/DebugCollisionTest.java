package com.pimpedpixel.games.systems.characters;

import com.dongbat.jbump.World;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import org.junit.Before;
import org.junit.Test;

/**
 * Debug test to understand collision detection issues.
 */
public class DebugCollisionTest {

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
        
        // Add a specific test collision block
        Item<Object> testBlock = new Item<>("MAP_COLLISION");
        jbumpWorld.add(testBlock, 500f, 150f, 64f, 64f);
    }

    @Test
    public void debugCollisionDetection() {
        // Debug the specific failing cases
        
        System.out.println("=== Debugging collision detection ===");
        
        // Print all collision items
        System.out.println("Collision items in world:");
        for (Item<Object> item : jbumpWorld.getItems()) {
            Rect rect = jbumpWorld.getRect(item);
            System.out.println(item.userData + " at (" + rect.x + "," + rect.y + ") size (" + rect.w + "," + rect.h + ")");
        }
        
        // Test case 1: Position (570, 150) with size (32, 32)
        System.out.println("\n=== Test case 1: (570, 150) size (32, 32) ===");
        float x1 = 570f, y1 = 150f, w1 = 32f, h1 = 32f;
        System.out.println("Testing position: (" + x1 + "," + y1 + ") size (" + w1 + "," + h1 + ")");
        System.out.println("Object bounds: x=" + x1 + " to " + (x1 + w1) + ", y=" + y1 + " to " + (y1 + h1));
        
        // Check against the test block at (500, 150) size (64, 64)
        Item<Object> testBlock = null;
        for (Item<Object> item : jbumpWorld.getItems()) {
            if (item.userData.equals("MAP_COLLISION")) {
                testBlock = item;
                break;
            }
        }
        
        if (testBlock != null) {
            Rect blockRect = jbumpWorld.getRect(testBlock);
            System.out.println("Block bounds: x=" + blockRect.x + " to " + (blockRect.x + blockRect.w) + ", y=" + blockRect.y + " to " + (blockRect.y + blockRect.h));
            
            // Manual collision check
            boolean xOverlap = x1 < blockRect.x + blockRect.w && x1 + w1 > blockRect.x;
            boolean yOverlap = y1 < blockRect.y + blockRect.h && y1 + h1 > blockRect.y;
            boolean manualCollision = xOverlap && yOverlap;
            
            System.out.println("X overlap: " + xOverlap + " (" + x1 + " < " + (blockRect.x + blockRect.w) + " && " + (x1 + w1) + " > " + blockRect.x + ")");
            System.out.println("Y overlap: " + yOverlap + " (" + y1 + " < " + (blockRect.y + blockRect.h) + " && " + (y1 + h1) + " > " + blockRect.y + ")");
            System.out.println("Manual collision result: " + manualCollision);
        }
        
        boolean result1 = zebraSystem.wouldCollide(x1, y1, w1, h1);
        System.out.println("System collision result: " + result1);
        
        // Test case 2: Position (-30, 100) with size (32, 32)
        System.out.println("\n=== Test case 2: (-30, 100) size (32, 32) ===");
        float x2 = -30f, y2 = 100f, w2 = 32f, h2 = 32f;
        System.out.println("Testing position: (" + x2 + "," + y2 + ") size (" + w2 + "," + h2 + ")");
        System.out.println("Object bounds: x=" + x2 + " to " + (x2 + w2) + ", y=" + y2 + " to " + (y2 + h2));
        
        // Check against left boundary wall
        Item<Object> leftWall = null;
        for (Item<Object> item : jbumpWorld.getItems()) {
            if (item.userData.equals("BOUNDARY_WALL")) {
                Rect wallRect = jbumpWorld.getRect(item);
                if (wallRect.x == -64f) {
                    leftWall = item;
                    break;
                }
            }
        }
        
        if (leftWall != null) {
            Rect wallRect = jbumpWorld.getRect(leftWall);
            System.out.println("Left wall bounds: x=" + wallRect.x + " to " + (wallRect.x + wallRect.w) + ", y=" + wallRect.y + " to " + (wallRect.y + wallRect.h));
            
            // Manual collision check
            boolean xOverlap = x2 < wallRect.x + wallRect.w && x2 + w2 > wallRect.x;
            boolean yOverlap = y2 < wallRect.y + wallRect.h && y2 + h2 > wallRect.y;
            boolean manualCollision = xOverlap && yOverlap;
            
            System.out.println("X overlap: " + xOverlap + " (" + x2 + " < " + (wallRect.x + wallRect.w) + " && " + (x2 + w2) + " > " + wallRect.x + ")");
            System.out.println("Y overlap: " + yOverlap + " (" + y2 + " < " + (wallRect.y + wallRect.h) + " && " + (y2 + h2) + " > " + wallRect.y + ")");
            System.out.println("Manual collision result: " + manualCollision);
        }
        
        boolean result2 = zebraSystem.wouldCollide(x2, y2, w2, h2);
        System.out.println("System collision result: " + result2);
    }
}