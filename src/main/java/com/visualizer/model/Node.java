package com.visualizer.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a single node in the pathfinding grid.
 * Contains coordinates, type, and pathfinding cost metrics.
 * Implements Serializable for file persistence and Comparable for PriorityQueue ordering.
 */
public class Node implements Serializable, Comparable<Node> {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private final int x;
    private final int y;
    private NodeType type;
    
    // Pathfinding costs - transient to avoid serialization
    private transient double gCost; // Distance from start
    private transient double hCost; // Heuristic distance to end
    private transient double fCost; // gCost + hCost
    private transient Node parent;  // For path reconstruction
    
    /**
     * Creates a new node at the specified grid position.
     * @param x the x-coordinate (column)
     * @param y the y-coordinate (row)
     */
    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.type = NodeType.EMPTY;
        this.gCost = Double.MAX_VALUE;
        this.hCost = 0;
        this.fCost = Double.MAX_VALUE;
        this.parent = null;
    }
    
    /**
     * Resets the pathfinding costs to initial state.
     * Called when starting a new algorithm run.
     */
    public void resetPathfindingState() {
        this.gCost = Double.MAX_VALUE;
        this.hCost = 0;
        this.fCost = Double.MAX_VALUE;
        this.parent = null;
    }
    
    /**
     * Calculates and updates the fCost based on gCost and hCost.
     */
    public void calculateFCost() {
        this.fCost = this.gCost + this.hCost;
    }
    
    /**
     * Calculates the Manhattan distance to another node.
     * @param other the target node
     * @return the Manhattan distance (|x1-x2| + |y1-y2|)
     */
    public int manhattanDistance(Node other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
    
    /**
     * Compares nodes by fCost for PriorityQueue ordering.
     * Lower fCost = higher priority.
     */
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.fCost, other.fCost);
    }
    
    /**
     * Two nodes are equal if they have the same coordinates.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return x == node.x && y == node.y;
    }
    
    @Override
    public int hashCode() {
        return 31 * x + y;
    }
    
    // Getters and Setters
    
    public int getX() { return x; }
    public int getY() { return y; }
    public NodeType getType() { return type; }
    public void setType(NodeType type) { this.type = type; }
    public double getGCost() { return gCost; }
    public void setGCost(double gCost) { this.gCost = gCost; }
    public void setHCost(double hCost) { this.hCost = hCost; }
    public void setParent(Node parent) { this.parent = parent; }

    public Node getParent() {
        return parent;
    }
}
