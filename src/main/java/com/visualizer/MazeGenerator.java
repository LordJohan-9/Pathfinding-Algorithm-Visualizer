package com.visualizer;

import com.visualizer.logic.AlgorithmEngine;
import com.visualizer.model.MazeState;
import com.visualizer.model.NodeType;

import java.io.*;

/**
 * Utility class to generate sample maze files for the Pathfinding Visualizer.
 * Run this class to create 5 pre-made maze layouts.
 */
public class MazeGenerator {
    
    private static final int ROWS = 25;
    private static final int COLS = 40;
    
    public static void main(String[] args) {
        File mazesDir = new File("mazes");
        if (!mazesDir.exists()) {
            mazesDir.mkdirs();
        }
        
        generateSimpleMaze();
        generateSpiralMaze();
        generateMazeWithRooms();
        generateDiagonalWallsMaze();
        generateComplexMaze();
        
        System.out.println("All maze files generated in 'mazes' folder!");
    }
    
    /**
     * Maze 1: Simple corridor maze
     */
    private static void generateSimpleMaze() {
        AlgorithmEngine engine = new AlgorithmEngine(ROWS, COLS);
        
        // Create horizontal walls with gaps
        for (int x = 5; x < COLS - 5; x++) {
            engine.setNodeType(x, 6, NodeType.WALL);
            engine.setNodeType(x, 12, NodeType.WALL);
            engine.setNodeType(x, 18, NodeType.WALL);
        }
        
        // Create gaps in walls
        engine.setNodeType(15, 6, NodeType.EMPTY);
        engine.setNodeType(25, 12, NodeType.EMPTY);
        engine.setNodeType(10, 18, NodeType.EMPTY);
        
        // Set start and end
        engine.setNodeType(2, ROWS / 2, NodeType.START);
        engine.setNodeType(COLS - 3, ROWS / 2, NodeType.END);
        
        saveMaze(engine, "mazes/01_simple_corridor.maze");
    }
    
    /**
     * Maze 2: Spiral maze
     */
    private static void generateSpiralMaze() {
        AlgorithmEngine engine = new AlgorithmEngine(ROWS, COLS);
        
        // Outer spiral wall
        for (int x = 3; x < COLS - 3; x++) {
            engine.setNodeType(x, 3, NodeType.WALL);
            engine.setNodeType(x, ROWS - 4, NodeType.WALL);
        }
        for (int y = 3; y < ROWS - 3; y++) {
            engine.setNodeType(3, y, NodeType.WALL);
            engine.setNodeType(COLS - 4, y, NodeType.WALL);
        }
        
        // Inner spiral wall
        for (int x = 6; x < COLS - 6; x++) {
            engine.setNodeType(x, 6, NodeType.WALL);
        }
        for (int y = 6; y < ROWS - 6; y++) {
            engine.setNodeType(COLS - 7, y, NodeType.WALL);
        }
        
        // More inner walls
        for (int y = 6; y < ROWS - 6; y++) {
            engine.setNodeType(9, y, NodeType.WALL);
        }
        for (int x = 9; x < COLS - 9; x++) {
            engine.setNodeType(x, ROWS - 7, NodeType.WALL);
        }
        
        // Create entrance/exit gaps
        engine.setNodeType(COLS - 4, 10, NodeType.EMPTY);
        engine.setNodeType(9, 10, NodeType.EMPTY);
        engine.setNodeType(15, ROWS - 7, NodeType.EMPTY);
        
        // Set start and end
        engine.setNodeType(5, 5, NodeType.START);
        engine.setNodeType(COLS - 5, ROWS - 5, NodeType.END);
        
        saveMaze(engine, "mazes/02_spiral.maze");
    }
    
    /**
     * Maze 3: Rooms with doorways
     */
    private static void generateMazeWithRooms() {
        AlgorithmEngine engine = new AlgorithmEngine(ROWS, COLS);
        
        // Create 4 rooms
        int[][] roomWalls = {
            // Room 1 (top-left)
            {5, 5, 15, 5}, {5, 5, 5, 11}, {15, 5, 15, 11}, {5, 11, 15, 11},
            // Room 2 (top-right)
            {25, 5, 35, 5}, {25, 5, 25, 11}, {35, 5, 35, 11}, {25, 11, 35, 11},
            // Room 3 (bottom-left)
            {5, 14, 15, 14}, {5, 14, 5, 20}, {15, 14, 15, 20}, {5, 20, 15, 20},
            // Room 4 (bottom-right)
            {25, 14, 35, 14}, {25, 14, 25, 20}, {35, 14, 35, 20}, {25, 20, 35, 20}
        };
        
        // Draw room walls
        for (int[] wall : roomWalls) {
            if (wall[0] == wall[2]) { // Vertical wall
                for (int y = Math.min(wall[1], wall[3]); y <= Math.max(wall[1], wall[3]); y++) {
                    engine.setNodeType(wall[0], y, NodeType.WALL);
                }
            } else { // Horizontal wall
                for (int x = Math.min(wall[0], wall[2]); x <= Math.max(wall[0], wall[2]); x++) {
                    engine.setNodeType(x, wall[1], NodeType.WALL);
                }
            }
        }
        
        // Create doorways
        engine.setNodeType(10, 5, NodeType.EMPTY);
        engine.setNodeType(30, 5, NodeType.EMPTY);
        engine.setNodeType(10, 20, NodeType.EMPTY);
        engine.setNodeType(30, 20, NodeType.EMPTY);
        engine.setNodeType(15, 8, NodeType.EMPTY);
        engine.setNodeType(25, 17, NodeType.EMPTY);
        
        // Corridor connecting rooms
        for (int x = 16; x < 25; x++) {
            engine.setNodeType(x, 12, NodeType.EMPTY);
        }
        
        // Set start and end
        engine.setNodeType(10, 8, NodeType.START);
        engine.setNodeType(30, 17, NodeType.END);
        
        saveMaze(engine, "mazes/03_rooms.maze");
    }
    
    /**
     * Maze 4: Diagonal walls maze
     */
    private static void generateDiagonalWallsMaze() {
        AlgorithmEngine engine = new AlgorithmEngine(ROWS, COLS);
        
        // Create diagonal walls
        for (int i = 0; i < 12; i++) {
            engine.setNodeType(5 + i, 3 + i, NodeType.WALL);
            engine.setNodeType(20 + i, 3 + i, NodeType.WALL);
            engine.setNodeType(5 + i, 10 + i, NodeType.WALL);
            engine.setNodeType(20 + i, 10 + i, NodeType.WALL);
        }
        
        // Add some horizontal barriers
        for (int x = 8; x < 20; x++) {
            engine.setNodeType(x, 8, NodeType.WALL);
            engine.setNodeType(x + 12, 16, NodeType.WALL);
        }
        
        // Create gaps
        engine.setNodeType(14, 8, NodeType.EMPTY);
        engine.setNodeType(26, 16, NodeType.EMPTY);
        
        // Set start and end
        engine.setNodeType(3, 3, NodeType.START);
        engine.setNodeType(COLS - 5, ROWS - 5, NodeType.END);
        
        saveMaze(engine, "mazes/04_diagonal.maze");
    }
    
    /**
     * Maze 5: Complex maze with multiple paths
     */
    private static void generateComplexMaze() {
        AlgorithmEngine engine = new AlgorithmEngine(ROWS, COLS);
        
        // Create a maze pattern
        // Vertical walls
        for (int y = 2; y < ROWS - 2; y++) {
            if (y != 6 && y != 12 && y != 18) {
                engine.setNodeType(8, y, NodeType.WALL);
            }
            if (y != 4 && y != 10 && y != 16) {
                engine.setNodeType(16, y, NodeType.WALL);
            }
            if (y != 8 && y != 14 && y != 20) {
                engine.setNodeType(24, y, NodeType.WALL);
            }
            if (y != 5 && y != 11 && y != 17) {
                engine.setNodeType(32, y, NodeType.WALL);
            }
        }
        
        // Horizontal walls
        for (int x = 2; x < COLS - 2; x++) {
            if (x != 12 && x != 20 && x != 28) {
                engine.setNodeType(x, 5, NodeType.WALL);
            }
            if (x != 10 && x != 22 && x != 30) {
                engine.setNodeType(x, 10, NodeType.WALL);
            }
            if (x != 14 && x != 26 && x != 34) {
                engine.setNodeType(x, 15, NodeType.WALL);
            }
            if (x != 8 && x != 18 && x != 28) {
                engine.setNodeType(x, 20, NodeType.WALL);
            }
        }
        
        // Add some extra obstacles
        engine.setNodeType(20, 12, NodeType.WALL);
        engine.setNodeType(20, 13, NodeType.WALL);
        engine.setNodeType(21, 12, NodeType.WALL);
        engine.setNodeType(21, 13, NodeType.WALL);
        
        // Set start and end
        engine.setNodeType(3, ROWS / 2, NodeType.START);
        engine.setNodeType(COLS - 4, ROWS / 2, NodeType.END);
        
        saveMaze(engine, "mazes/05_complex.maze");
    }
    
    /**
     * Saves a maze to a file.
     */
    private static void saveMaze(AlgorithmEngine engine, String filename) {
        try {
            MazeState state = engine.createMazeState();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(state);
            }
            System.out.println("Created: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to create " + filename + ": " + e.getMessage());
        }
    }
}
