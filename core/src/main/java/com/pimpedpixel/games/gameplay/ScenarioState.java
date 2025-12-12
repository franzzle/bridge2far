package com.pimpedpixel.games.gameplay;

import java.util.*;

/**
 * Singleton class for managing global scenario state across the game.
 * Tracks scenario attempts, death locations, level progression, and statistics.
 */
public class ScenarioState {
    private static ScenarioState instance;
    
    // Current game state
    private int currentLevelIndex = 0;
    private int currentScenarioIndex = 0;
    private boolean treasureFoundThisScenario = false;
    
    // Scenario tracking per level
    private Map<Integer, LevelScenarioData> levelScenarioDataMap = new HashMap<>();
    
    // Global statistics
    private int totalLevelsUnlocked = 0;
    private int totalScenariosCompleted = 0;
    private int totalDeaths = 0;
    
    // Private constructor for singleton
    private ScenarioState() {
        // Initialize with default values
    }
    
    /**
     * Get the singleton instance
     */
    public static synchronized ScenarioState getInstance() {
        if (instance == null) {
            instance = new ScenarioState();
        }
        return instance;
    }
    
    /**
     * Reset the singleton instance (for testing or new game)
     */
    public static synchronized void resetInstance() {
        instance = new ScenarioState();
    }
    
    /**
     * Data class for tracking scenario-specific information per level
     */
    public static class LevelScenarioData {
        private Map<Integer, ScenarioAttemptData> scenarioAttempts = new HashMap<>();
        private int scenarioOccurrences = 0;
        private int currentScenarioRotationIndex = 0;
        
        public ScenarioAttemptData getOrCreateScenarioAttempt(int scenarioIndex) {
            return scenarioAttempts.computeIfAbsent(scenarioIndex, k -> new ScenarioAttemptData());
        }
        
        public void incrementScenarioOccurrences() {
            scenarioOccurrences++;
        }
        
        public int getScenarioOccurrences() {
            return scenarioOccurrences;
        }
        
        public int getCurrentScenarioRotationIndex() {
            return currentScenarioRotationIndex;
        }
        
        public void setCurrentScenarioRotationIndex(int index) {
            this.currentScenarioRotationIndex = index;
        }
        
        public void incrementRotationIndex() {
            this.currentScenarioRotationIndex++;
        }
    }
    
    /**
     * Data class for tracking individual scenario attempts
     */
    public static class ScenarioAttemptData {
        private List<DeathLocation> deathLocations = new ArrayList<>();
        private int attemptCount = 0;
        private boolean treasureFound = false;
        
        public void recordDeath(float x, float y) {
            deathLocations.add(new DeathLocation(x, y));
            attemptCount++;
        }
        
        public List<DeathLocation> getDeathLocations() {
            return Collections.unmodifiableList(deathLocations);
        }
        
        public int getAttemptCount() {
            return attemptCount;
        }
        
        public boolean isTreasureFound() {
            return treasureFound;
        }
        
        public void setTreasureFound(boolean treasureFound) {
            this.treasureFound = treasureFound;
        }
    }
    
    /**
     * Simple data class for storing death locations
     */
    public static class DeathLocation {
        private final float x;
        private final float y;
        private final long timestamp;
        
        public DeathLocation(float x, float y) {
            this.x = x;
            this.y = y;
            this.timestamp = System.currentTimeMillis();
        }
        
        public float getX() { return x; }
        public float getY() { return y; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return "DeathLocation{x=" + x + ", y=" + y + ", timestamp=" + timestamp + "}";
        }
    }
    
    // Public API methods
    
    /**
     * Initialize or reset state for a new level
     */
    public void initializeLevel(int levelIndex) {
        this.currentLevelIndex = levelIndex;
        this.currentScenarioIndex = 0;
        this.treasureFoundThisScenario = false;
        
        // Initialize level data if not exists
        levelScenarioDataMap.computeIfAbsent(levelIndex, k -> new LevelScenarioData());
    }
    
    /**
     * Record a death at the given location
     */
    public void recordDeath(float x, float y) {
        LevelScenarioData levelData = levelScenarioDataMap.get(currentLevelIndex);
        if (levelData != null) {
            ScenarioAttemptData scenarioData = levelData.getOrCreateScenarioAttempt(currentScenarioIndex);
            scenarioData.recordDeath(x, y);
            totalDeaths++;
        }
    }
    
    /**
     * Record that treasure was found in current scenario
     */
    public void recordTreasureFound() {
        LevelScenarioData levelData = levelScenarioDataMap.get(currentLevelIndex);
        if (levelData != null) {
            ScenarioAttemptData scenarioData = levelData.getOrCreateScenarioAttempt(currentScenarioIndex);
            scenarioData.setTreasureFound(true);
            this.treasureFoundThisScenario = true;
            totalScenariosCompleted++;
        }
    }
    
    /**
     * Get current level index
     */
    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }
    
    /**
     * Get current scenario index
     */
    public int getCurrentScenarioIndex() {
        return currentScenarioIndex;
    }
    
    /**
     * Set current scenario index
     */
    public void setCurrentScenarioIndex(int scenarioIndex) {
        this.currentScenarioIndex = scenarioIndex;
    }
    
    /**
     * Check if treasure was found in current scenario
     */
    public boolean isTreasureFoundThisScenario() {
        return treasureFoundThisScenario;
    }
    
    /**
     * Reset treasure found flag for new scenario
     */
    public void resetTreasureFoundFlag() {
        this.treasureFoundThisScenario = false;
    }
    
    /**
     * Get scenario attempt data for current level and scenario
     */
    public ScenarioAttemptData getCurrentScenarioAttemptData() {
        LevelScenarioData levelData = levelScenarioDataMap.get(currentLevelIndex);
        if (levelData != null) {
            return levelData.getOrCreateScenarioAttempt(currentScenarioIndex);
        }
        return null;
    }
    
    /**
     * Get level scenario data for current level
     */
    public LevelScenarioData getCurrentLevelScenarioData() {
        return levelScenarioDataMap.get(currentLevelIndex);
    }
    
    /**
     * Increment scenario occurrences for current level
     */
    public void incrementScenarioOccurrences() {
        LevelScenarioData levelData = levelScenarioDataMap.get(currentLevelIndex);
        if (levelData != null) {
            levelData.incrementScenarioOccurrences();
        }
    }
    
    /**
     * Advance to next level
     */
    public void advanceToNextLevel() {
        currentLevelIndex++;
        currentScenarioIndex = 0;
        treasureFoundThisScenario = false;
        totalLevelsUnlocked++;
        
        // Initialize new level data
        levelScenarioDataMap.computeIfAbsent(currentLevelIndex, k -> new LevelScenarioData());
    }
    
    /**
     * Get total levels unlocked
     */
    public int getTotalLevelsUnlocked() {
        return totalLevelsUnlocked;
    }
    
    /**
     * Get total scenarios completed
     */
    public int getTotalScenariosCompleted() {
        return totalScenariosCompleted;
    }
    
    /**
     * Get total deaths
     */
    public int getTotalDeaths() {
        return totalDeaths;
    }
    
    /**
     * Get all level scenario data
     */
    public Map<Integer, LevelScenarioData> getAllLevelScenarioData() {
        return Collections.unmodifiableMap(levelScenarioDataMap);
    }
    
    /**
     * Debug method to print current state
     */
    public void printDebugState() {
        System.out.println("=== ScenarioState Debug ===");
        System.out.println("Current Level: " + currentLevelIndex);
        System.out.println("Current Scenario: " + currentScenarioIndex);
        System.out.println("Treasure Found This Scenario: " + treasureFoundThisScenario);
        System.out.println("Total Levels Unlocked: " + totalLevelsUnlocked);
        System.out.println("Total Scenarios Completed: " + totalScenariosCompleted);
        System.out.println("Total Deaths: " + totalDeaths);
        
        for (Map.Entry<Integer, LevelScenarioData> entry : levelScenarioDataMap.entrySet()) {
            System.out.println("\nLevel " + entry.getKey() + ":");
            LevelScenarioData levelData = entry.getValue();
            System.out.println("  Scenario Occurrences: " + levelData.getScenarioOccurrences());
            System.out.println("  Current Rotation Index: " + levelData.getCurrentScenarioRotationIndex());
            
            for (Map.Entry<Integer, ScenarioAttemptData> scenarioEntry : levelData.scenarioAttempts.entrySet()) {
                System.out.println("  Scenario " + scenarioEntry.getKey() + ":");
                ScenarioAttemptData scenarioData = scenarioEntry.getValue();
                System.out.println("    Attempts: " + scenarioData.getAttemptCount());
                System.out.println("    Treasure Found: " + scenarioData.isTreasureFound());
                System.out.println("    Death Locations: " + scenarioData.getDeathLocations().size());
            }
        }
    }
}