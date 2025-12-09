package com.pimpedpixel.games.gameplay;

public class Scenario {
    private String title;
    private int timeLimit;
    private CollisionLayer groundLayer;
    private CollisionLayer topCollisionLayer;

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public CollisionLayer getGroundLayer() {
        return groundLayer;
    }

    public void setGroundLayer(CollisionLayer groundLayer) {
        this.groundLayer = groundLayer;
    }

    public CollisionLayer getTopCollisionLayer() {
        return topCollisionLayer;
    }

    public void setTopCollisionLayer(CollisionLayer topCollisionLayer) {
        this.topCollisionLayer = topCollisionLayer;
    }
}
