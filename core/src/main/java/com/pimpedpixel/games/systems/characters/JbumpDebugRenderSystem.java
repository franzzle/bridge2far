package com.pimpedpixel.games.systems.characters;

import com.artemis.BaseSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Camera;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import com.dongbat.jbump.World;

public class JbumpDebugRenderSystem extends BaseSystem {

    private final World<Object> world;
    private final ShapeRenderer renderer;
    private final Camera camera;

    public JbumpDebugRenderSystem(World<Object> world,
                                  ShapeRenderer renderer,
                                  Camera camera) {
        this.world = world;
        this.renderer = renderer;
        this.camera = camera;
    }

    @Override
    protected void processSystem() {
        renderer.setProjectionMatrix(camera.combined);

        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.YELLOW);

        for (Item<Object> item : world.getItems()) {
            final Rect rect = world.getRect(item);

            // rect.x/y is bottom-left — just like LibGDX expects
            renderer.rect(rect.x, rect.y, rect.w, rect.h);
        }

        renderer.end();
    }
}
