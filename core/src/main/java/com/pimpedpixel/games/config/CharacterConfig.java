package com.pimpedpixel.games.config;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.files.FileHandle;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class for managing character configuration loaded from characters.json.
 * Provides lookup by character name and returns CharacterData instances with properties.
 */
public class CharacterConfig {
    
    // Singleton instance
    private static CharacterConfig instance;
    
    // Map to store character data by name
    private final Map<String, CharacterData> characterMap;
    
    // Private constructor to prevent instantiation
    private CharacterConfig() {
        this.characterMap = new HashMap<>();
    }
    
    /**
     * Get the singleton instance of CharacterConfig.
     * If not initialized, creates and loads the configuration.
     *
     * @return The singleton CharacterConfig instance
     */
    public static synchronized CharacterConfig getInstance() {
        if (instance == null) {
            instance = new CharacterConfig();
            instance.loadCharacters("assets/characters/characters.json");
        }
        return instance;
    }
    
    /**
     * Load character configuration from the specified JSON file.
     *
     * @param filePath Path to the characters.json file
     */
    public void loadCharacters(String filePath) {
        try {
            FileHandle file = new FileHandle(filePath);
            if (file.exists()) {
                Json json = new Json();
                CharacterData[] characters = json.fromJson(CharacterData[].class, file);
                
                // Clear existing data and populate map
                characterMap.clear();
                for (CharacterData character : characters) {
                    characterMap.put(character.getName().toLowerCase(), character);
                }
                
                System.out.println("CharacterConfig loaded successfully with " + characterMap.size() + " characters:");
                for (String name : characterMap.keySet()) {
                    CharacterData data = characterMap.get(name);
                    System.out.println("  " + name + ": width=" + data.getWidth() + 
                                     ", height=" + data.getHeight() + 
                                     ", offset=" + data.getHorizontalOffset());
                }
            } else {
                System.err.println("Character config file not found: " + filePath + ", using empty configuration");
            }
        } catch (Exception e) {
            System.err.println("Failed to load character config: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Reload the character configuration from file.
     * Useful for applying changes without restarting the application.
     */
    public void reload() {
        loadCharacters("assets/characters/characters.json");
    }
    
    /**
     * Get character data by name (case insensitive).
     *
     * @param characterName Name of the character to look up
     * @return CharacterData instance containing the character's properties, or null if not found
     */
    public CharacterData getCharacterByName(String characterName) {
        if (characterName == null) {
            return null;
        }
        return characterMap.get(characterName.toLowerCase());
    }
    
    /**
     * Check if a character with the given name exists.
     *
     * @param characterName Name of the character to check
     * @return true if the character exists, false otherwise
     */
    public boolean hasCharacter(String characterName) {
        return characterName != null && characterMap.containsKey(characterName.toLowerCase());
    }
    
    /**
     * Get all character names in the configuration.
     *
     * @return Set of all character names
     */
    public java.util.Set<String> getAllCharacterNames() {
        return characterMap.keySet();
    }
    
    /**
     * Get all character data in the configuration.
     *
     * @return Collection of all CharacterData instances
     */
    public java.util.Collection<CharacterData> getAllCharacters() {
        return characterMap.values();
    }
    
    /**
     * Class representing character data with properties from characters.json.
     * This is the instance class that gets returned by lookup methods.
     */
    public static class CharacterData {
        private String name;
        private float width;
        private float height;
        private float horizontalOffset;
        
        // Private constructor for internal use
        private CharacterData() {
            // Default constructor for JSON parsing
        }
        
        /**
         * Get the character name.
         *
         * @return The character name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Get the character width.
         *
         * @return The character width
         */
        public float getWidth() {
            return width;
        }
        
        /**
         * Set the character width.
         *
         * @param width The character width to set
         */
        public void setWidth(float width) {
            this.width = width;
        }
        
        /**
         * Get the character height.
         *
         * @return The character height
         */
        public float getHeight() {
            return height;
        }
        
        /**
         * Set the character height.
         *
         * @param height The character height to set
         */
        public void setHeight(float height) {
            this.height = height;
        }
        
        /**
         * Get the horizontal offset for collision detection.
         *
         * @return The horizontal offset
         */
        public float getHorizontalOffset() {
            return horizontalOffset;
        }
        
        /**
         * Set the horizontal offset for collision detection.
         *
         * @param horizontalOffset The horizontal offset to set
         */
        public void setHorizontalOffset(float horizontalOffset) {
            this.horizontalOffset = horizontalOffset;
        }
        
        /**
         * Get the horizontal offset scaled by the asset scale factor.
         *
         * @param assetScale The asset scale factor
         * @return Scaled horizontal offset
         */
        public float getScaledHorizontalOffset(float assetScale) {
            return horizontalOffset * assetScale;
        }
        
        /**
         * Create a copy of this CharacterData instance.
         *
         * @return A new CharacterData instance with the same values
         */
        public CharacterData copy() {
            CharacterData copy = new CharacterData();
            copy.name = this.name;
            copy.width = this.width;
            copy.height = this.height;
            copy.horizontalOffset = this.horizontalOffset;
            return copy;
        }
        
        @Override
        public String toString() {
            return "CharacterData{" +
                "name='" + name + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", horizontalOffset=" + horizontalOffset +
                '}';
        }
    }
}