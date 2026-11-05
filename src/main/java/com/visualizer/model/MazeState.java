package com.visualizer.model;

import java.io.Serializable;

/**
 * Data transfer object containing the complete state of a maze.
 * Used for serialization to save and load maze layouts.
 * Only stores essential data: grid coordinates and node types.
 */
public class MazeState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final int rows;
    private final int cols;
    private final NodeType[][] nodeTypes;
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;
    
    /**
     * Creates a new MazeState from the current grid.
     * @param grid the 2D array of nodes to capture
     * @param startX the x-coordinate of the start node
     * @param startY the y-coordinate of the start node
     * @param endX the x-coordinate of the end node
     * @param endY the y-coordinate of the end node
     */
    public MazeState(Node[][] grid, int startX, int startY, int endX, int endY) {
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.nodeTypes = new NodeType[rows][cols];
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        
        // Extract only the NodeType from each node
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                nodeTypes[y][x] = grid[y][x].getType();
            }
        }
    }
    
    /**
     * Reconstructs a Node grid from this MazeState.
     * @return a new 2D array of nodes with the saved types
     */
    public Node[][] reconstructGrid() {
        Node[][] grid = new Node[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x] = new Node(x, y);
                grid[y][x].setType(nodeTypes[y][x]);
            }
        }
        return grid;
    }
    
    // Getters
    
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public int getEndX() { return endX; }
    public int getEndY() { return endY; }
}
