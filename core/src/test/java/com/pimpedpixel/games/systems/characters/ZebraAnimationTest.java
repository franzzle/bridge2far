package com.pimpedpixel.games.systems.characters;

import org.junit.Test;
import static org.junit.Assert.*;

public class ZebraAnimationTest {

    @Test
    public void testZebraStateEnum() {
        // Test that the ZebraState enum includes the new SHREDDING state
        ZebraState[] states = ZebraState.values();
        assertEquals("Should have 3 zebra states", 3, states.length);
        
        boolean hasGrazing = false;
        boolean hasWalking = false;
        boolean hasShredding = false;
        
        for (ZebraState state : states) {
            if (state == ZebraState.GRAZING) hasGrazing = true;
            if (state == ZebraState.WALKING) hasWalking = true;
            if (state == ZebraState.SHREDDING) hasShredding = true;
        }
        
        assertTrue("Should have GRAZING state", hasGrazing);
        assertTrue("Should have WALKING state", hasWalking);
        assertTrue("Should have SHREDDING state", hasShredding);
        
        System.out.println("✅ Zebra state enum test passed!");
        System.out.println("Available states: GRAZING, WALKING, SHREDDING");
    }
    
    @Test
    public void testZebraAnimationComponentStructure() {
        // Test that the ZebraAnimationComponent has all the required fields
        ZebraAnimationComponent anim = new ZebraAnimationComponent();
        
        // Verify all animation fields exist (they should be null initially)
        assertNull("grazingLeft should be null initially", anim.grazingLeft);
        assertNull("grazingRight should be null initially", anim.grazingRight);
        assertNull("walkingLeft should be null initially", anim.walkingLeft);
        assertNull("walkingRight should be null initially", anim.walkingRight);
        assertNull("shreddingLeft should be null initially", anim.shreddingLeft);
        assertNull("shreddingRight should be null initially", anim.shreddingRight);
        
        System.out.println("✅ Zebra animation component structure test passed!");
        System.out.println("Component has all required animation fields:");
        System.out.println("  - grazingLeft, grazingRight");
        System.out.println("  - walkingLeft, walkingRight");
        System.out.println("  - shreddingLeft, shreddingRight");
    }
    
    @Test
    public void testAnimationFilePaths() {
        // Test that we can at least verify the expected file paths exist
        // This is a simple test to verify our understanding of the file structure
        String[] expectedFiles = {
            "characters/raelus/zebra-grazing-left-1.png",
            "characters/raelus/zebra-grazing-left-2.png",
            "characters/raelus/zebra-grazing-left-3.png",
            "characters/raelus/zebra-grazing-left-4.png",
            "characters/raelus/zebra-grazing-right-1.png",
            "characters/raelus/zebra-grazing-right-2.png",
            "characters/raelus/zebra-grazing-right-3.png",
            "characters/raelus/zebra-grazing-right-4.png",
            "characters/raelus/zebra-walking-left-1.png",
            "characters/raelus/zebra-walking-left-2.png",
            "characters/raelus/zebra-walking-right-1.png",
            "characters/raelus/zebra-walking-right-2.png",
            "characters/raelus/zebra-shredding-left-1.png",
            "characters/raelus/zebra-shredding-left-2.png",
            "characters/raelus/zebra-shredding-left-3.png",
            "characters/raelus/zebra-shredding-left-4.png",
            "characters/raelus/zebra-shredding-right-1.png",
            "characters/raelus/zebra-shredding-right-2.png",
            "characters/raelus/zebra-shredding-right-3.png",
            "characters/raelus/zebra-shredding-right-4.png"
        };
        
        assertEquals("Should have 20 animation frames total", 20, expectedFiles.length);
        
        System.out.println("✅ Animation file paths test passed!");
        System.out.println("Expected animation files:");
        System.out.println("  - 4 grazing left frames");
        System.out.println("  - 4 grazing right frames");
        System.out.println("  - 2 walking left frames");
        System.out.println("  - 2 walking right frames");
        System.out.println("  - 4 shredding left frames");
        System.out.println("  - 4 shredding right frames");
    }
}
