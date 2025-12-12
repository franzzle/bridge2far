package com.pimpedpixel.games.config;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

/**
 * Singleton class for managing debug configuration loaded from debugconfig.json.
 * Provides easy access to debug settings throughout the application.
 */
public class DebugConfig {
    
    // Singleton instance
    private static DebugConfig instance;
    
    // Debug configuration properties (matching debugconfig.json)
    private boolean boundingboxes;
    private boolean hidegroundlayer;
    
    // Private constructor to prevent instantiation
    private DebugConfig() {
        // Initialize with default values
        this.boundingboxes = false;
        this.hidegroundlayer = false;
    }
    
    /**
     * Get the singleton instance of DebugConfig.
     * If not initialized, creates and loads the configuration.
     *
     * @return The singleton DebugConfig instance
     */
    public static synchronized DebugConfig getInstance() {
        if (instance == null) {
            instance = new DebugConfig();
            instance.loadConfig("config/debugconfig.json");
        }
        return instance;
    }
    
    /**
     * Load debug configuration from the specified JSON file.
     *
     * @param filePath Path to the debugconfig.json file
     */
    public void loadConfig(String filePath) {
        try {
            FileHandle file = Gdx.files.internal(filePath);
            if (file.exists()) {
                Json json = new Json();
                DebugConfigData configData = json.fromJson(DebugConfigData.class, file);
                
                // Copy values from the loaded data
                this.boundingboxes = configData.isBoundingboxes();
                this.hidegroundlayer = configData.isHidegroundlayer();
                
                System.out.println("DebugConfig loaded successfully:");
                System.out.println("  boundingboxes: " + this.boundingboxes);
                System.out.println("  hidegroundlayer: " + this.hidegroundlayer);
            } else {
                System.err.println("Debug config file not found: " + filePath + ", using default values");
            }
        } catch (Exception e) {
            System.err.println("Failed to load debug config: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Reload the debug configuration from file.
     * Useful for applying changes without restarting the application.
     */
    public void reload() {
        loadConfig("config/debugconfig.json");
    }
    
    // Getters matching the property names in debugconfig.json
    
    /**
     * Get whether bounding boxes should be displayed.
     *
     * @return true if bounding boxes should be shown, false otherwise
     */
    public boolean isBoundingboxes() {
        return boundingboxes;
    }
    
    /**
     * Set whether bounding boxes should be displayed.
     *
     * @param boundingboxes true to show bounding boxes, false to hide them
     */
    public void setBoundingboxes(boolean boundingboxes) {
        this.boundingboxes = boundingboxes;
    }
    
    /**
     * Get whether the ground layer should be hidden.
     *
     * @return true if ground layer should be hidden, false otherwise
     */
    public boolean isHidegroundlayer() {
        return hidegroundlayer;
    }
    
    /**
     * Set whether the ground layer should be hidden.
     *
     * @param hidegroundlayer true to hide ground layer, false to show it
     */
    public void setHidegroundlayer(boolean hidegroundlayer) {
        this.hidegroundlayer = hidegroundlayer;
    }
    
    /**
     * Helper class to match the JSON structure in debugconfig.json.
     */
    private static class DebugConfigData {
        private boolean boundingboxes;
        private boolean hidegroundlayer;
        
        // Getters and setters for JSON parsing
        public boolean isBoundingboxes() {
            return boundingboxes;
        }
        
        public void setBoundingboxes(boolean boundingboxes) {
            this.boundingboxes = boundingboxes;
        }
        
        public boolean isHidegroundlayer() {
            return hidegroundlayer;
        }
        
        public void setHidegroundlayer(boolean hidegroundlayer) {
            this.hidegroundlayer = hidegroundlayer;
        }
    }
}
