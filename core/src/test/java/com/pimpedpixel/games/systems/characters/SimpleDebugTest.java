package com.pimpedpixel.games.systems.characters;

import com.dongbat.jbump.World;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import org.junit.Test;

/**
 * Simple debug test to understand collision detection.
 */
public class SimpleDebugTest {

    @Test
    public void testSimpleCollision() {
        World<Object> jbumpWorld = new World<>();
        ZebraStateSystem zebraSystem = new ZebraStateSystem(jbumpWorld);
        
        // Add a simple collision block
        Item<Object> block = new Item<>("MAP_COLLISION");
        jbumpWorld.add(block, 500f, 150f, 64f, 64f);
        
        // Test position that should NOT collide
        float x = 570f, y = 150f, width = 32f, height = 32f;
        
        System.out.println("Testing collision at: " + x + "," + y + " size: " + width + "," + height);
        System.out.println("Object bounds: " + x + "->" + (x + width) + ", " + y + "->" + (y + height));
        
        // Get the block bounds
        Rect blockRect = jbumpWorld.getRect(block);
        System.out.println("Block bounds: " + blockRect.x + "->" + (blockRect.x + blockRect.w) + ", " + blockRect.y + "->" + (blockRect.y + blockRect.h));
        
        // Manual calculation
        boolean xOverlap = x < blockRect.x + blockRect.w && x + width > blockRect.x;
        boolean yOverlap = y < blockRect.y + blockRect.h && y + height > blockRect.y;
        
        System.out.println("X overlap: " + xOverlap);
        System.out.println("  " + x + " < " + (blockRect.x + blockRect.w) + " = " + (x < blockRect.x + blockRect.w));
        System.out.println("  " + (x + width) + " > " + blockRect.x + " = " + ((x + width) > blockRect.x));
        
        System.out.println("Y overlap: " + yOverlap);
        System.out.println("  " + y + " < " + (blockRect.y + blockRect.h) + " = " + (y < blockRect.y + blockRect.h));
        System.out.println("  " + (y + height) + " > " + blockRect.y + " = " + ((y + height) > blockRect.y));
        
        System.out.println("Manual result: " + (xOverlap && yOverlap));
        
        // System result
        boolean systemResult = zebraSystem.wouldCollide(x, y, width, height);
        System.out.println("System result: " + systemResult);
        
        // Test with boundary wall
        System.out.println("\n=== Testing boundary wall ===");
        Item<Object> wall = new Item<>("BOUNDARY_WALL");
        jbumpWorld.add(wall, -64f, 0f, 64f, 800f);
        
        float x2 = -30f, y2 = 100f, width2 = 32f, height2 = 32f;
        
        System.out.println("Testing collision at: " + x2 + "," + y2 + " size: " + width2 + "," + height2);
        System.out.println("Object bounds: " + x2 + "->" + (x2 + width2) + ", " + y2 + "->" + (y2 + height2));
        
        // Get the wall bounds
        Rect wallRect = jbumpWorld.getRect(wall);
        System.out.println("Wall bounds: " + wallRect.x + "->" + (wallRect.x + wallRect.w) + ", " + wallRect.y + "->" + (wallRect.y + wallRect.h));
        
        // Manual calculation
        boolean xOverlap2 = x2 < wallRect.x + wallRect.w && x2 + width2 > wallRect.x;
        boolean yOverlap2 = y2 < wallRect.y + wallRect.h && y2 + height2 > wallRect.y;
        
        System.out.println("X overlap: " + xOverlap2);
        System.out.println("  " + x2 + " < " + (wallRect.x + wallRect.w) + " = " + (x2 < wallRect.x + wallRect.w));
        System.out.println("  " + (x2 + width2) + " > " + wallRect.x + " = " + ((x2 + width2) > wallRect.x));
        
        System.out.println("Y overlap: " + yOverlap2);
        System.out.println("  " + y2 + " < " + (wallRect.y + wallRect.h) + " = " + (y2 < wallRect.y + wallRect.h));
        System.out.println("  " + (y2 + height2) + " > " + wallRect.y + " = " + ((y2 + height2) > wallRect.y));
        
        System.out.println("Manual result: " + (xOverlap2 && yOverlap2));
        
        // System result
        boolean systemResult2 = zebraSystem.wouldCollide(x2, y2, width2, height2);
        System.out.println("System result: " + systemResult2);
    }
}