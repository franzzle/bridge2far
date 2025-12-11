package com.pimpedpixel.games.gameplay;

import com.badlogic.gdx.utils.Json;
import org.junit.Test;
import static org.junit.Assert.*;

public class LevelParsingTest {
    
    @Test
    public void testLevelStructureParsing() {
        // Test JSON that matches our levelInfo.json structure
        String testJson = "{\n" +
            "  \"levels\": [\n" +
            "    {\n" +
            "      \"levelNumber\": 1,\n" +
            "      \"scenarios\": [\n" +
            "        {\n" +
            "          \"title\": \"Test Scenario\",\n" +
            "          \"timeLimit\": 30,\n" +
            "          \"startingPositionX\": 100,\n" +
            "          \"startingPositionY\": 500,\n" +
            "          \"groundLayer\": {\n" +
            "            \"matchingRow\": 5,\n" +
            "            \"cellStates\": [1, 0, 1, 1, 0]\n" +
            "          },\n" +
            "          \"baseLayer\": {\n" +
            "            \"matchingRow\": 10,\n" +
            "            \"cellStates\": [0, 1, 0, 1, 1]\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        Json json = new Json();
        LevelLoader.LevelContainer container = json.fromJson(LevelLoader.LevelContainer.class, testJson);
        
        assertNotNull("Container should not be null", container);
        assertNotNull("Levels should not be null", container.getLevels());
        assertEquals("Should have 1 level", 1, container.getLevels().length);
        
        Level level = container.getLevels()[0];
        assertNotNull("Level should not be null", level);
        assertEquals("Level number should be 1", 1, level.getLevelNumber());
        
        assertNotNull("Scenarios should not be null", level.getScenarios());
        assertEquals("Should have 1 scenario", 1, level.getScenarios().size());
        
        Scenario scenario = level.getScenarios().get(0);
        assertNotNull("Scenario should not be null", scenario);
        assertEquals("Title should match", "Test Scenario", scenario.getTitle());
        assertEquals("Time limit should be 30", 30, scenario.getTimeLimit());
        assertEquals("Starting X should be 100", 100f, scenario.getStartingPositionX(), 0.01f);
        assertEquals("Starting Y should be 500", 500f, scenario.getStartingPositionY(), 0.01f);
        
        assertNotNull("Ground layer should not be null", scenario.getGroundLayer());
        assertEquals("Ground layer row should be 5", 5, scenario.getGroundLayer().getMatchingRow());
        assertNotNull("Ground layer cells should not be null", scenario.getGroundLayer().getCellStates());
        assertEquals("Ground layer should have 5 cells", 5, scenario.getGroundLayer().getCellStates().size());
        
        assertNotNull("Base layer should not be null", scenario.getBaseLayer());
        assertEquals("Base layer row should be 10", 10, scenario.getBaseLayer().getMatchingRow());
        assertNotNull("Base layer cells should not be null", scenario.getBaseLayer().getCellStates());
        assertEquals("Base layer should have 5 cells", 5, scenario.getBaseLayer().getCellStates().size());
        
        System.out.println("✅ JSON parsing test passed!");
    }
    
    @Test
    public void testSystemDefaultsParsing() {
        // Test JSON that includes systemdefaults
        String testJson = "{\n" +
            "  \"systemdefaults\" : {\n" +
            "    \"moveSpeed\" : 120,\n" +
            "    \"jumpSpeed\" : 260,\n" +
            "    \"gravity\" : -600,\n" +
            "    \"spawnX\" : 20,\n" +
            "    \"spawnY\" : 700,\n" +
            "    \"timeLimit\" : 30,\n" +
            "    \"dyingDuration\" : 2,\n" +
            "    \"diedDuration\" : 1,\n" +
            "    \"timerDecrementAmount\" : 1\n" +
            "  },\n" +
            "  \"levels\": [\n" +
            "    {\n" +
            "      \"levelNumber\": 1,\n" +
            "      \"scenarios\": [\n" +
            "        {\n" +
            "          \"title\": \"Test Scenario\",\n" +
            "          \"timeLimit\": 30,\n" +
            "          \"startingPositionX\" : 100,\n" +
            "          \"startingPositionY\" : 500,\n" +
            "          \"groundLayer\": {\n" +
            "            \"matchingRow\" : 5,\n" +
            "            \"cellStates\": [1, 0, 1, 1, 0]\n" +
            "          },\n" +
            "          \"baseLayer\": {\n" +
            "            \"matchingRow\" : 10,\n" +
            "            \"cellStates\": [0, 1, 0, 1, 1]\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        Json json = new Json();
        LevelLoader.LevelContainer container = json.fromJson(LevelLoader.LevelContainer.class, testJson);
        
        assertNotNull("Container should not be null", container);
        assertNotNull("System defaults should not be null", container.getSystemdefaults());
        
        LevelLoader.SystemDefaults systemDefaults = container.getSystemdefaults();
        assertEquals("moveSpeed should be 120", 120, systemDefaults.getMoveSpeed());
        assertEquals("jumpSpeed should be 260", 260, systemDefaults.getJumpSpeed());
        assertEquals("gravity should be -600", -600, systemDefaults.getGravity());
        assertEquals("spawnX should be 20", 20, systemDefaults.getSpawnX());
        assertEquals("spawnY should be 700", 700, systemDefaults.getSpawnY());
        assertEquals("timeLimit should be 30", 30, systemDefaults.getTimeLimit());
        assertEquals("dyingDuration should be 2", 2, systemDefaults.getDyingDuration());
        assertEquals("diedDuration should be 1", 1, systemDefaults.getDiedDuration());
        assertEquals("timerDecrementAmount should be 1", 1, systemDefaults.getTimerDecrementAmount());
        
        System.out.println("✅ System defaults parsing test passed!");
    }
}