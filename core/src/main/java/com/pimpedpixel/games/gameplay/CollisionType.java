package com.pimpedpixel.games.gameplay;

public enum CollisionType {
    HOLE(0),
    SOLID(1),
    FATAL(2);

    private final int value;

    CollisionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CollisionType fromValue(int value) {
        for (CollisionType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid CollisionType value: " + value);
    }
}
