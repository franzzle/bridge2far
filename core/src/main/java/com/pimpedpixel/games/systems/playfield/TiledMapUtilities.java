package com.pimpedpixel.games.systems.playfield;

import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class TiledMapUtilities {
    public static int findLowestTileY(TiledMapTileLayer layer) {
        int lowestY = Integer.MAX_VALUE;

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    if (y < lowestY) {
                        lowestY = y;
                    }
                }
            }
        }

        // If no tile found, return -1
        return lowestY == Integer.MAX_VALUE ? -1 : lowestY;
    }

    public static String getDescription(RectangleMapObject mapObject){
        if(mapObject.getProperties() != null && mapObject.getProperties().containsKey("description")){
            return mapObject.getProperties().get("description", String.class);
        }else{
            if(mapObject.getName().startsWith("object")){
                return mapObject.getName().replace("object", "");
            }else{
                return mapObject.getName();
            }
        }
    }
}
