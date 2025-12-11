package com.pimpedpixel.games.gameplay;

public class Scenario {
    private String title;
    private int timeLimit;
    private float startingPositionX;
    private float startingPositionY;
    private CollisionLayer groundLayer;
    private CollisionLayer baseLayer;

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

    public float getStartingPositionX() {
        return startingPositionX;
    }

    public void setStartingPositionX(float startingPositionX) {
        this.startingPositionX = startingPositionX;
    }

    public float getStartingPositionY() {
        return startingPositionY;
    }

    public void setStartingPositionY(float startingPositionY) {
        this.startingPositionY = startingPositionY;
    }

    public CollisionLayer getGroundLayer() {
        return groundLayer;
    }

    public void setGroundLayer(CollisionLayer groundLayer) {
        this.groundLayer = groundLayer;
    }

    public CollisionLayer getBaseLayer() {
        return baseLayer;
    }

    public void setBaseLayer(CollisionLayer baseLayer) {
        this.baseLayer = baseLayer;
    }
}
