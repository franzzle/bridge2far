package com.pimpedpixel.games.systems.hud;

import com.artemis.Component;

/**
 * Component that stores timer information for HUD display.
 */
public class TimerComponent extends Component {
    public float remainingTime;
    public float totalTime;
    public boolean isRunning = false;
    public boolean isExpired = false;
    
    public TimerComponent() {
        // Default constructor
    }
    
    public TimerComponent(float totalTime) {
        this.totalTime = totalTime;
        this.remainingTime = totalTime;
    }
    
    /**
     * Start the timer.
     */
    public void start() {
        this.isRunning = true;
        this.isExpired = false;
    }
    
    /**
     * Stop the timer.
     */
    public void stop() {
        this.isRunning = false;
    }
    
    /**
     * Reset the timer to its initial state.
     */
    public void reset() {
        this.remainingTime = this.totalTime;
        this.isRunning = false;
        this.isExpired = false;
    }
    
    /**
     * Update the timer by delta time.
     * @return true if timer has expired, false otherwise
     */
    public boolean update(float delta) {
        if (!isRunning || isExpired) {
            return isExpired;
        }
        
        remainingTime -= delta;
        
        if (remainingTime <= 0) {
            remainingTime = 0;
            isExpired = true;
            isRunning = false;
            return true;
        }
        
        return false;
    }
    
    /**
     * Get remaining time in seconds (rounded to nearest second).
     */
    public int getRemainingSeconds() {
        return Math.max(0, Math.round(remainingTime));
    }
    
    /**
     * Get remaining time as formatted string (MM:SS).
     */
    public String getFormattedTime() {
        int seconds = getRemainingSeconds();
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}