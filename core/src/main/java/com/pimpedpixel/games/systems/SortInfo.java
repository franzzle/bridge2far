package com.pimpedpixel.games.systems;


public class SortInfo {
    public enum Type {
        LAYER,
        CHARACTER
    }

    public Type type;
    public String layerName; // only for layers
    public float sortY;

    public SortInfo(Type type, String layerName, float sortY) {
        this.type = type;
        this.layerName = layerName;
        this.sortY = sortY;
    }

    @Override
    public String toString() {
        return layerName;
    }
}

