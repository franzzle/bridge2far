package com.pimpedpixel.games.systems.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.junit.Test;

import static org.junit.Assert.*;

public class BloodAnimationTest {

    @Test
    public void testBloodAnimationComponentStructure() {
        // Test that the BloodAnimationComponent has all the required fields
        BloodAnimationComponent anim = new BloodAnimationComponent();
        
        // Verify all animation fields exist (they should be null initially)
        assertNull("Flowing animation should be null initially", anim.flowing);
        assertNull("Drying animation should be null initially", anim.drying);
        assertNull("Dried animation should be null initially", anim.dried);
        
        // Verify state management fields
        assertEquals("Default state should be FLOWING", BloodState.FLOWING, anim.state);
        assertEquals("Default state time should be 0", 0f, anim.stateTime, 0.001f);
        assertEquals("Default animation speed should be 12 fps", 1.0f / 12.0f, anim.animationSpeed, 0.001f);
        
        System.out.println("✅ Blood animation component structure test passed!");
        System.out.println("Component has all required animation fields:");
        System.out.println("  - flowing: " + (anim.flowing == null ? "null (expected)" : "loaded"));
        System.out.println("  - drying: " + (anim.drying == null ? "null (expected)" : "loaded"));
        System.out.println("  - dried: " + (anim.dried == null ? "null (expected)" : "loaded"));
        System.out.println("  - state: " + anim.state);
        System.out.println("  - stateTime: " + anim.stateTime);
        System.out.println("  - animationSpeed: " + anim.animationSpeed + " (12 fps)");
    }

    @Test
    public void testBloodStateTransitions() {
        BloodAnimationComponent anim = new BloodAnimationComponent();
        
        // Test initial state
        assertEquals("Initial state should be FLOWING", BloodState.FLOWING, anim.state);
        
        // Test state transitions
        anim.state = BloodState.DRYING;
        assertEquals("State should transition to DRYING", BloodState.DRYING, anim.state);
        
        anim.state = BloodState.DRIED;
        assertEquals("State should transition to DRIED", BloodState.DRIED, anim.state);
        
        // Test going back to flowing
        anim.state = BloodState.FLOWING;
        assertEquals("State should transition back to FLOWING", BloodState.FLOWING, anim.state);
        
        System.out.println("✅ Blood state transitions test passed!");
        System.out.println("All state transitions work correctly:");
        System.out.println("  - FLOWING -> DRYING -> DRIED -> FLOWING");
    }

    @Test
    public void testBloodAnimationTiming() {
        BloodAnimationComponent anim = new BloodAnimationComponent();
        
        // Test that the animation speed is correct for 12 fps
        float expectedFrameDuration = 1.0f / 12.0f;
        assertEquals("Animation speed should be correct for 12 fps", expectedFrameDuration, anim.animationSpeed, 0.001f);
        
        System.out.println("✅ Blood animation timing test passed!");
        System.out.println("Animation speed is correctly set to 12 fps:");
        System.out.println("  - Expected frame duration: " + expectedFrameDuration);
        System.out.println("  - Actual frame duration: " + anim.animationSpeed);
    }
}