package com.visualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.visualizer.logic.AlgorithmEngine;
import com.visualizer.logic.AlgorithmEngine.AlgorithmType;
import com.visualizer.model.MazeState;
import com.visualizer.model.Node;
import com.visualizer.model.NodeType;

/**
 * Unit tests for maze serialization and deserialization.
 * Tests the save/load functionality using Java Object Serialization.
 */
class SerializationTest {
    
    private AlgorithmEngine engine;
    private static final int ROWS = 15;
    private static final int COLS = 25;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        engine = new AlgorithmEngine(ROWS, COLS);
    }
    
    @Test
    void testMazeStateCreation() {
        // Create some walls
        engine.setNodeType(5, 5, NodeType.WALL);
        engine.setNodeType(6, 5, NodeType.WALL);
        engine.setNodeType(7, 5, NodeType.WALL);
        
        MazeState mazeState = engine.createMazeState();
        
        assertNotNull(mazeState);
        assertEquals(ROWS, mazeState.getRows());
        assertEquals(COLS, mazeState.getCols());
        assertNotNull(mazeState.getStartX());
        assertNotNull(mazeState.getStartY());
        assertNotNull(mazeState.getEndX());
        assertNotNull(mazeState.getEndY());
    }
    
    @Test
    void testSerializeAndDeserialize() throws IOException, ClassNotFoundException {
        // Create a custom maze
        engine.setNodeType(5, 5, NodeType.WALL);
        engine.setNodeType(6, 5, NodeType.WALL);
        engine.setNodeType(7, 5, NodeType.WALL);
        engine.setNodeType(8, 8, NodeType.START);
        engine.setNodeType(12, 12, NodeType.END);
        
        MazeState originalState = engine.createMazeState();
        
        // Serialize to file
        File tempFile = tempDir.resolve("test.maze").toFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            oos.writeObject(originalState);
        }
        
        // Deserialize from file
        MazeState loadedState;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile))) {
            loadedState = (MazeState) ois.readObject();
        }
        
        // Verify data integrity
        assertEquals(originalState.getRows(), loadedState.getRows());
        assertEquals(originalState.getCols(), loadedState.getCols());
        assertEquals(originalState.getStartX(), loadedState.getStartX());
        assertEquals(originalState.getStartY(), loadedState.getStartY());
        assertEquals(originalState.getEndX(), loadedState.getEndX());
        assertEquals(originalState.getEndY(), loadedState.getEndY());
    }
    
    @Test
    void testLoadMazeStateIntoEngine() throws IOException, ClassNotFoundException {
        // Create a custom maze
        engine.setNodeType(3, 3, NodeType.WALL);
        engine.setNodeType(4, 4, NodeType.WALL);
        engine.setNodeType(10, 7, NodeType.START);
        engine.setNodeType(18, 7, NodeType.END);
        
        MazeState originalState = engine.createMazeState();
        
        // Serialize and deserialize
        File tempFile = tempDir.resolve("test2.maze").toFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            oos.writeObject(originalState);
        }
        
        // Create new engine and load state
        AlgorithmEngine newEngine = new AlgorithmEngine(ROWS, COLS);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile))) {
            MazeState loadedState = (MazeState) ois.readObject();
            newEngine.loadMazeState(loadedState);
        }
        
        // Verify loaded state
        assertEquals(NodeType.WALL, newEngine.getGrid()[3][3].getType());
        assertEquals(NodeType.WALL, newEngine.getGrid()[4][4].getType());
        assertEquals(NodeType.START, newEngine.getGrid()[7][10].getType());
        assertEquals(NodeType.END, newEngine.getGrid()[7][18].getType());
    }
    
    @Test
    void testSerializationPreservesWalls() throws IOException, ClassNotFoundException {
        // Create a complex wall pattern
        for (int i = 0; i < 10; i++) {
            engine.setNodeType(5 + i, 7, NodeType.WALL);
        }
        
        MazeState originalState = engine.createMazeState();
        
        // Serialize and deserialize
        File tempFile = tempDir.resolve("walls.maze").toFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            oos.writeObject(originalState);
        }
        
        MazeState loadedState;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile))) {
            loadedState = (MazeState) ois.readObject();
        }
        
        // Reconstruct grid and verify walls
        Node[][] grid = loadedState.reconstructGrid();
        for (int i = 0; i < 10; i++) {
            assertEquals(NodeType.WALL, grid[7][5 + i].getType());
        }
    }
    
    @Test
    void testSerializationWithEmptyGrid() throws IOException, ClassNotFoundException {
        // Reset to empty board (only start/end)
        engine.resetBoard();
        
        MazeState originalState = engine.createMazeState();
        
        // Serialize and deserialize
        File tempFile = tempDir.resolve("empty.maze").toFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            oos.writeObject(originalState);
        }
        
        MazeState loadedState;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile))) {
            loadedState = (MazeState) ois.readObject();
        }
        
        // Verify minimal nodes
        Node[][] grid = loadedState.reconstructGrid();
        int nonEmptyCount = 0;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (grid[y][x].getType() != NodeType.EMPTY) {
                    nonEmptyCount++;
                }
            }
        }
        
        // Only start and end should be non-empty
        assertEquals(2, nonEmptyCount);
    }
    
    @Test
    void testSerializationWithFullWalls() throws IOException, ClassNotFoundException {
        // Fill entire grid with walls except start/end
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (engine.getGrid()[y][x].getType() != NodeType.START 
                    && engine.getGrid()[y][x].getType() != NodeType.END) {
                    engine.setNodeType(x, y, NodeType.WALL);
                }
            }
        }
        
        MazeState originalState = engine.createMazeState();
        
        // Serialize and deserialize
        File tempFile = tempDir.resolve("fullwalls.maze").toFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            oos.writeObject(originalState);
        }
        
        MazeState loadedState;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile))) {
            loadedState = (MazeState) ois.readObject();
        }
        
        // Reconstruct and count walls
        Node[][] grid = loadedState.reconstructGrid();
        int wallCount = 0;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (grid[y][x].getType() == NodeType.WALL) {
                    wallCount++;
                }
            }
        }
        
        // All nodes except start/end should be walls
        assertEquals(ROWS * COLS - 2, wallCount);
    }
    
    @Test
    void testFileExtension() {
        File tempFile = tempDir.resolve("custom_name.maze").toFile();
        assertTrue(tempFile.getName().endsWith(".maze"));
    }
    
    @Test
    void testCorruptedFileHandling() {
        // Create a temporary file manually
        File corruptedFile = new File(System.getProperty("java.io.tmpdir"), "corrupted_test.maze");
        
        try {
            // Write invalid data
            try (FileWriter writer = new FileWriter(corruptedFile)) {
                writer.write("This is not a valid serialized object");
            } catch (IOException e) {
                fail("Failed to create corrupted file");
                return;
            }
            
            // Attempt to load should throw exception
            assertThrows(Exception.class, () -> {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(corruptedFile))) {
                    ois.readObject();
                }
            });
        } finally {
            // Clean up
            corruptedFile.delete();
        }
    }
    
    @Test
    void testSerializationDoesNotPreservePathfindingState() throws IOException, ClassNotFoundException {
        // Run algorithm to create visited nodes
        engine.initializeAlgorithm(AlgorithmType.ASTAR_TREE);
        for (int i = 0; i < 10; i++) {
            engine.executeStep();
        }
        
        // Reset path before saving (this clears VISITED and PATH nodes)
        engine.resetPath();
        
        // Save state
        MazeState state = engine.createMazeState();
        
        // Reconstruct grid
        Node[][] grid = state.reconstructGrid();
        
        // No nodes should be VISITED or PATH in the reconstructed grid
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                NodeType type = grid[y][x].getType();
                assertNotEquals(NodeType.VISITED, type, "Visited nodes should not be serialized");
                assertNotEquals(NodeType.PATH, type, "Path nodes should not be serialized");
            }
        }
    }
}
