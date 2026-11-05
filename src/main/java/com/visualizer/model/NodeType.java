package com.visualizer.model;

import java.awt.Color;

/**
 * Enum representing the different states a node can have in the pathfinding grid.
 * Each state has an associated color for visual rendering.
 */
public enum NodeType {
    EMPTY(Color.WHITE),
    WALL(Color.BLACK),
    START(new Color(0, 200, 0)),      // Green
    END(new Color(220, 50, 50)),       // Red
    VISITED(new Color(135, 206, 250)), // Light Blue
    PATH(new Color(255, 215, 0));      // Yellow

    private final Color color;

    NodeType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
