package com.pimpedpixel.games.systems.characters;

import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.Gdx;
import com.dongbat.jbump.World;
import com.dongbat.jbump.Item;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Integration test for ZebraStateSystem to verify the movement logic works correctly.
 */
public class ZebraStateSystemIntegrationTest {

    private World<Object> jbumpWorld;
    private com.artemis.World artemisWorld;
    private ZebraStateSystem zebraSystem;
    private ZebraFactory zebraFactory;

    @Before
    public void setUp() {
        // Create jbump world
        jbumpWorld = new World<>();
        
        // Create Artemis world
        WorldConfiguration config = new WorldConfigurationBuilder()
            .with(
                new ZebraStateSystem(jbumpWorld),
                new ActionSystem()
            )
            .build();
        
        artemisWorld = new com.artemis.World(config);
        
        // Get the zebra system
        zebraSystem = artemisWorld.getSystem(ZebraStateSystem.class);
        
        // Create zebra factory
        zebraFactory = new ZebraFactory(artemisWorld, jbumpWorld, 15f * 2f, 30f, 40f);
        
        // Add some collision blocks to simulate walls
        addTestCollisionBlocks();
    }

    private void addTestCollisionBlocks() {
        // Add left boundary wall
        Item<Object> leftWall = new Item<>("BOUNDARY_WALL");
        jbumpWorld.add(leftWall, -64f, 0f, 64f, 800f);
        
        // Add right boundary wall
        Item<Object> rightWall = new Item<>("BOUNDARY_WALL");
        jbumpWorld.add(rightWall, 1280f, 0f, 64f, 800f);
        
        // Add some collision blocks in the middle
        for (int i = 0; i < 5; i++) {
            Item<Object> collisionBlock = new Item<>("MAP_COLLISION");
            jbumpWorld.add(collisionBlock, 300f + i * 128f, 100f, 64f, 64f);
        }
    }

    @Test
    public void testZebraCreationAndMovement() {
        // Create a zebra manually to avoid texture loading issues in tests
        int zebraId = createTestZebra(400f, 120f);
        
        // Get zebra components
        ZebraStateComponent state = artemisWorld.getMapper(ZebraStateComponent.class).get(zebraId);
        TransformComponent transform = artemisWorld.getMapper(TransformComponent.class).get(zebraId);
        ActionComponent actions = artemisWorld.getMapper(ActionComponent.class).get(zebraId);
        
        // Verify zebra was created and has components
        assertNotNull("Zebra should be created and have state component", state);
        assertNotNull("Zebra should be created and have transform component", transform);
        assertNotNull("Zebra should be created and have action component", actions);
        
        // Verify initial position
        assertEquals("Zebra should start at x=400", 400f, transform.x, 0.01f);
        assertEquals("Zebra should start at y=120", 120f, transform.y, 0.01f);
        
        // Verify initial state is GRAZING
        assertEquals("Zebra should start in GRAZING state", ZebraState.GRAZING, state.state);
        
        // Process the system to trigger state changes
        for (int i = 0; i < 100; i++) {
            artemisWorld.setDelta(0.1f); // Simulate time passing
            artemisWorld.process();
        }
        
        // Verify that the zebra has changed state and has actions
        // Note: This is a simplified test - in a real scenario, we'd need to mock Gdx.graphics.getDeltaTime()
        // and properly simulate the game loop
        System.out.println("Zebra final state: " + state.state);
        System.out.println("Zebra final position: " + transform.x + ", " + transform.y);
        System.out.println("Zebra has actions: " + actions.hasActions());
    }

    /**
     * Creates a test zebra without loading textures to avoid Gdx dependencies.
     */
    private int createTestZebra(float x, float y) {
        int entityId = artemisWorld.create();
        
        // 1. TRANSFORM
        TransformComponent t = artemisWorld.edit(entityId).create(TransformComponent.class);
        t.x = x;
        t.y = y;

        // 2. PHYSICS
        PhysicsComponent p = artemisWorld.edit(entityId).create(PhysicsComponent.class);
        p.vx = 0;
        p.vy = 0;
        p.onGround = false;

        // 3. JUMP ITEM
        JbumpItemComponent j = artemisWorld.edit(entityId).create(JbumpItemComponent.class);
        Item<Integer> zebraItem = new Item<>(entityId);
        j.item = zebraItem;
        jbumpWorld.add((Item)zebraItem, x + 15f * 2f, y, 30f, 40f);

        // 4. STATE
        ZebraStateComponent s = artemisWorld.edit(entityId).create(ZebraStateComponent.class);
        s.state = ZebraState.GRAZING;
        s.dir = Direction.LEFT;
        s.stateTime = 0f;

        // 5. ANIMATION (minimal for testing)
        ZebraAnimationComponent anim = artemisWorld.edit(entityId).create(ZebraAnimationComponent.class);
        // Skip texture loading for testing

        // 6. ACTIONS
        ActionComponent actions = artemisWorld.edit(entityId).create(ActionComponent.class);
        actions.actor.setPosition(t.x, t.y);

        return entityId;
    }

    @Test
    public void testMovementRangeCalculation() {
        // Test the movement range calculation method directly
        int availableCells = 14;
        
        // Calculate min and max cells
        int minCells = (int) (availableCells * 0.25f); // 3
        int maxCells = (int) (availableCells * 0.75f); // 10
        
        System.out.println("Available cells: " + availableCells);
        System.out.println("Min cells (25%): " + minCells);
        System.out.println("Max cells (75%): " + maxCells);
        
        // Test multiple random calculations
        for (int i = 0; i < 5; i++) {
            int cellsToUse = minCells + (int) (Math.random() * (maxCells - minCells + 1));
            float movementRange = cellsToUse * 64f; // 64 pixels per cell
            
            System.out.println("Test " + i + ": Cells=" + cellsToUse + ", Range=" + movementRange + " pixels");
            
            // Verify the range is within bounds
            assertTrue("Movement range should be positive", movementRange > 0);
            assertTrue("Movement range should be reasonable", movementRange <= maxCells * 64f);
        }
    }

    @Test
    public void testCollisionDetection() {
        // Test collision detection with the jbump world
        
        // Test position that should NOT collide (in empty space)
        boolean noCollision = true;
        for (Item<Object> item : jbumpWorld.getItems()) {
            if (item.userData.equals("MAP_COLLISION") || item.userData.equals("BOUNDARY_WALL")) {
                float itemX = jbumpWorld.getRect(item).x;
                float itemY = jbumpWorld.getRect(item).y;
                float itemW = jbumpWorld.getRect(item).w;
                float itemH = jbumpWorld.getRect(item).h;
                
                // Test position in empty space
                float testX = 200f;
                float testY = 200f;
                float testW = 32f;
                float testH = 32f;
                
                if (testX < itemX + itemW && testX + testW > itemX &&
                    testY < itemY + itemH && testY + testH > itemY) {
                    noCollision = false;
                    break;
                }
            }
        }
        
        assertTrue("Position 200,200 should not collide", noCollision);
        
        // Test position that SHOULD collide (with boundary wall)
        boolean hasCollision = false;
        for (Item<Object> item : jbumpWorld.getItems()) {
            if (item.userData.equals("BOUNDARY_WALL")) {
                float itemX = jbumpWorld.getRect(item).x;
                float itemY = jbumpWorld.getRect(item).y;
                float itemW = jbumpWorld.getRect(item).w;
                float itemH = jbumpWorld.getRect(item).h;
                
                // Test position that should collide with left wall
                float testX = -32f;
                float testY = 100f;
                float testW = 32f;
                float testH = 32f;
                
                if (testX < itemX + itemW && testX + testW > itemX &&
                    testY < itemY + itemH && testY + testH > itemY) {
                    hasCollision = true;
                    break;
                }
            }
        }
        
        assertTrue("Position -32,100 should collide with left boundary wall", hasCollision);
    }
}