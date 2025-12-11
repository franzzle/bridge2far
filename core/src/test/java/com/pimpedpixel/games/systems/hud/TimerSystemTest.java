package com.pimpedpixel.games.systems.hud;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TimerSystemTest {
    
    private TimerComponent timer;
    
    @Before
    public void setUp() {
        // Create a timer with 60 seconds (1 minute)
        timer = new TimerComponent(60f);
    }
    
    @Test
    public void testTimerInitialization() {
        assertEquals(60f, timer.totalTime, 0.001f);
        assertEquals(60f, timer.remainingTime, 0.001f);
        assertFalse(timer.isRunning);
        assertFalse(timer.isExpired);
    }
    
    @Test
    public void testTimerStart() {
        timer.start();
        assertTrue(timer.isRunning);
        assertFalse(timer.isExpired);
    }
    
    @Test
    public void testTimerUpdate() {
        timer.start();
        
        // Update with 1 second delta
        boolean expired = timer.update(1.0f);
        assertFalse(expired);
        assertEquals(59f, timer.remainingTime, 0.001f);
        assertTrue(timer.isRunning);
        assertFalse(timer.isExpired);
    }
    
    @Test
    public void testTimerExpiration() {
        timer.start();
        
        // Update with 60 seconds (should expire)
        boolean expired = timer.update(60.0f);
        assertTrue(expired);
        assertEquals(0f, timer.remainingTime, 0.001f);
        assertFalse(timer.isRunning);
        assertTrue(timer.isExpired);
    }
    
    @Test
    public void testTimerOverUpdate() {
        timer.start();
        
        // Update with more time than remaining (should clamp to 0)
        boolean expired = timer.update(120.0f);
        assertTrue(expired);
        assertEquals(0f, timer.remainingTime, 0.001f);
        assertFalse(timer.isRunning);
        assertTrue(timer.isExpired);
    }
    
    @Test
    public void testTimerStop() {
        timer.start();
        timer.update(10.0f); // Should have 50 seconds left
        
        timer.stop();
        boolean expired = timer.update(10.0f); // Should not update when stopped
        
        assertFalse(expired);
        assertEquals(50f, timer.remainingTime, 0.001f);
        assertFalse(timer.isRunning);
        assertFalse(timer.isExpired);
    }
    
    @Test
    public void testTimerReset() {
        timer.start();
        timer.update(20.0f); // Should have 40 seconds left
        
        timer.reset();
        
        assertEquals(60f, timer.remainingTime, 0.001f);
        assertFalse(timer.isRunning);
        assertFalse(timer.isExpired);
    }
    
    @Test
    public void testGetRemainingSeconds() {
        timer.start();
        timer.update(30.5f); // Should have ~29.5 seconds left
        
        int remainingSeconds = timer.getRemainingSeconds();
        assertEquals(30, remainingSeconds); // Should round to nearest second
    }
    
    @Test
    public void testGetFormattedTime() {
        // Test various time formatting
        TimerComponent timer1 = new TimerComponent(60f); // 1:00
        timer1.start();
        assertEquals("01:00", timer1.getFormattedTime());
        
        timer1.update(30f); // 0:30
        assertEquals("00:30", timer1.getFormattedTime());
        
        timer1.update(15f); // 0:15
        assertEquals("00:15", timer1.getFormattedTime());
        
        timer1.update(14f); // 0:01
        assertEquals("00:01", timer1.getFormattedTime());
        
        timer1.update(1f); // 0:00
        assertEquals("00:00", timer1.getFormattedTime());
    }
    
    @Test
    public void testTimerWithDifferentTimeLimits() {
        // Test with 30 seconds
        TimerComponent shortTimer = new TimerComponent(30f);
        shortTimer.start();
        assertEquals("00:30", shortTimer.getFormattedTime());
        
        // Test with 120 seconds (2 minutes)
        TimerComponent longTimer = new TimerComponent(120f);
        longTimer.start();
        assertEquals("02:00", longTimer.getFormattedTime());
        
        longTimer.update(60f); // 1 minute passed
        assertEquals("01:00", longTimer.getFormattedTime());
    }
}