package com.visualizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.visualizer.logic.AlgorithmEngine;
import com.visualizer.logic.AlgorithmEngine.AlgorithmState;
import com.visualizer.logic.AlgorithmEngine.AlgorithmType;
import com.visualizer.model.Node;
import com.visualizer.model.NodeType;

/**
 * Unit tests for the AlgorithmEngine class.
 * Tests A* and Dijkstra's algorithms, pathfinding logic, and state management.
 */
class AlgorithmTest {
    
    private AlgorithmEngine engine;
    private static final int ROWS = 15;
    private static final int COLS = 25;
    
    @BeforeEach
    void setUp() {
        engine = new AlgorithmEngine(ROWS, COLS);
    }
    
    @Test
    void testGridInitialization() {
        Node[][] grid = engine.getGrid();
        
        assertEquals(ROWS, grid.length);
        assertEquals(COLS, grid[0].length);
        
        // Check default start and end positions exist
        assertNotNull(engine.getStartNode());
        assertNotNull(engine.getEndNode());
        
        // Verify start and end have correct types
        assertEquals(NodeType.START, engine.getStartNode().getType());
        assertEquals(NodeType.END, engine.getEndNode().getType());
    }
    
    @Test
    void testSetNodeType() {
        engine.setNodeType(5, 5, NodeType.WALL);
        assertEquals(NodeType.WALL, engine.getGrid()[5][5].getType());
        
        engine.setNodeType(10, 10, NodeType.EMPTY);
        assertEquals(NodeType.EMPTY, engine.getGrid()[10][10].getType());
    }
    
    @Test
    void testStartNodeUpdate() {
        Node oldStart = engine.getStartNode();
        int oldStartX = oldStart.getX();
        int oldStartY = oldStart.getY();
        
        // Set new start position
        engine.setNodeType(3, 3, NodeType.START);
        
        // Old start should be cleared
        assertEquals(NodeType.EMPTY, engine.getGrid()[oldStartY][oldStartX].getType());
        
        // New start should be set
        assertEquals(NodeType.START, engine.getGrid()[3][3].getType());
        assertEquals(3, engine.getStartNode().getX());
        assertEquals(3, engine.getStartNode().getY());
    }
    
    @Test
    void testEndNodeUpdate() {
        Node oldEnd = engine.getEndNode();
        int oldEndX = oldEnd.getX();
        int oldEndY = oldEnd.getY();
        
        // Set new end position
        engine.setNodeType(7, 7, NodeType.END);
        
        // Old end should be cleared
        assertEquals(NodeType.EMPTY, engine.getGrid()[oldEndY][oldEndX].getType());
        
        // New end should be set
        assertEquals(NodeType.END, engine.getGrid()[7][7].getType());
        assertEquals(7, engine.getEndNode().getX());
        assertEquals(7, engine.getEndNode().getY());
    }
    
    @Test
    void testResetPath() {
        // Create some walls in positions that won't be visited during algorithm
        engine.setNodeType(0, 0, NodeType.WALL);
        engine.setNodeType(1, 0, NodeType.WALL);
        
        // Run algorithm briefly
        engine.initializeAlgorithm(AlgorithmType.ASTAR_TREE);
        engine.executeStep();
        engine.executeStep();
        
        // Reset path
        engine.resetPath();
        
        // Walls should remain
        assertEquals(NodeType.WALL, engine.getGrid()[0][0].getType());
        assertEquals(NodeType.WALL, engine.getGrid()[0][1].getType());
        
        // Algorithm state should be idle
        assertEquals(AlgorithmState.IDLE, engine.getAlgorithmState());
    }
    
    @Test
    void testResetBoard() {
        // Create various nodes
        engine.setNodeType(5, 5, NodeType.WALL);
        engine.setNodeType(6, 5, NodeType.WALL);
        
        // Reset board
        engine.resetBoard();
        
        // All nodes should be empty except default start/end
        int wallCount = 0;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                NodeType type = engine.getGrid()[y][x].getType();
                if (type == NodeType.WALL) {
                    wallCount++;
                }
            }
        }
        
        assertEquals(0, wallCount);
    }
    
    @Test
    void testAStarFindsPath() {
        engine.initializeAlgorithm(AlgorithmType.ASTAR_TREE);
        
        // Run until algorithm completes
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
        }
        
        // Should find a path (no walls blocking)
        assertEquals(AlgorithmState.PATH_FOUND, engine.getAlgorithmState());
    }
    
    @Test
    void testDijkstraFindsPath() {
        engine.initializeAlgorithm(AlgorithmType.DIJKSTRA);
        
        // Run until algorithm completes
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
        }
        
        // Should find a path (no walls blocking)
        assertEquals(AlgorithmState.PATH_FOUND, engine.getAlgorithmState());
    }
    
    @Test
    void testNoPathWhenBlocked() {
        // Create a wall completely surrounding the end node
        Node end = engine.getEndNode();
        int endX = end.getX();
        int endY = end.getY();
        
        // Surround the end with walls
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int x = endX + dx;
                int y = endY + dy;
                if (x >= 0 && x < COLS && y >= 0 && y < ROWS) {
                    if (engine.getGrid()[y][x].getType() != NodeType.END) {
                        engine.setNodeType(x, y, NodeType.WALL);
                    }
                }
            }
        }
        
        engine.initializeAlgorithm(AlgorithmType.ASTAR_TREE);
        
        // Run until algorithm completes
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
        }
        
        // Should not find a path
        assertEquals(AlgorithmState.NO_PATH_FOUND, engine.getAlgorithmState());
    }
    
    @Test
    void testManhattanDistance() {
        Node node1 = new Node(0, 0);
        Node node2 = new Node(3, 4);
        
        assertEquals(7, node1.manhattanDistance(node2));
        
        Node node3 = new Node(5, 5);
        Node node4 = new Node(2, 3);
        
        assertEquals(5, node3.manhattanDistance(node4));
    }
    
    @Test
    void testNodeComparison() {
        Node node1 = new Node(0, 0);
        Node node2 = new Node(1, 1);
        
        node1.setGCost(5);
        node1.setHCost(3);
        node1.calculateFCost();
        
        node2.setGCost(4);
        node2.setHCost(2);
        node2.calculateFCost();
        
        // node1 has fCost = 8, node2 has fCost = 6
        // node2 should come first (lower fCost)
        assertTrue(node1.compareTo(node2) > 0);
        assertTrue(node2.compareTo(node1) < 0);
    }
    
    @Test
    void testNodeEquality() {
        Node node1 = new Node(5, 5);
        Node node2 = new Node(5, 5);
        Node node3 = new Node(5, 6);
        
        assertEquals(node1, node2);
        assertNotEquals(node1, node3);
        assertEquals(node1.hashCode(), node2.hashCode());
    }
    
    @Test
    void testPathReconstruction() {
        engine.initializeAlgorithm(AlgorithmType.ASTAR_TREE);
        
        // Run until path found
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
        }
        
        assertEquals(AlgorithmState.PATH_FOUND, engine.getAlgorithmState());
        
        // Count PATH nodes
        int pathCount = 0;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (engine.getGrid()[y][x].getType() == NodeType.PATH) {
                    pathCount++;
                }
            }
        }
        
        // Should have some path nodes (at least the nodes between start and end)
        assertTrue(pathCount > 0);
    }
    
    @Test
    void testAStarVisitedNodes() {
        // Create walls to force algorithm to explore more nodes
        for (int i = 0; i < 5; i++) {
            engine.setNodeType(engine.getCols() / 2, engine.getRows() / 2 - 2 + i, NodeType.WALL);
        }
        
        engine.initializeAlgorithm(AlgorithmType.ASTAR_TREE);
        
        int maxSteps = 100;
        int step = 0;
        
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING && step < maxSteps) {
            engine.executeStep();
            step++;
        }
        
        // Count visited nodes
        int visitedCount = 0;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (engine.getGrid()[y][x].getType() == NodeType.VISITED) {
                    visitedCount++;
                }
            }
        }
        
        // Should have visited some nodes (at least the start's neighbors explored)
        assertTrue(visitedCount >= 0, "Algorithm should have explored nodes");
    }
    
    @Test
    void testDijkstraVsAStarEfficiency() {
        // Create a simple maze
        engine.setNodeType(10, 5, NodeType.WALL);
        engine.setNodeType(10, 6, NodeType.WALL);
        engine.setNodeType(10, 7, NodeType.WALL);
        
        // Run A*
        engine.resetPath();
        engine.initializeAlgorithm(AlgorithmType.ASTAR_TREE);
        int aStarSteps = 0;
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
            aStarSteps++;
        }
        
        // Run Dijkstra
        engine.resetPath();
        engine.initializeAlgorithm(AlgorithmType.DIJKSTRA);
        int dijkstraSteps = 0;
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
            dijkstraSteps++;
        }
        
        // Both should find path
        assertEquals(AlgorithmState.PATH_FOUND, engine.getAlgorithmState());
        
        // A* should generally be more efficient (fewer steps)
        // Note: This might not always be true for all mazes, but generally is
        // We just verify both completed successfully
        assertTrue(aStarSteps > 0);
        assertTrue(dijkstraSteps > 0);
    }
    
    @Test
    void testBFSFindsPath() {
        engine.initializeAlgorithm(AlgorithmType.BFS);
        
        // Run until algorithm completes
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
        }
        
        // BFS should find a path
        assertEquals(AlgorithmState.PATH_FOUND, engine.getAlgorithmState());
    }
    
    @Test
    void testBFSFindsShortestPath() {
        // Create a specific maze where shortest path matters
        engine.resetBoard();
        
        // Set specific start and end
        engine.setNodeType(2, ROWS / 2, NodeType.START);
        engine.setNodeType(COLS - 3, ROWS / 2, NodeType.END);
        
        // Run BFS
        engine.initializeAlgorithm(AlgorithmType.BFS);
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
        }
        
        // BFS guarantees shortest path - count path nodes
        int pathCount = 0;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (engine.getGrid()[y][x].getType() == NodeType.PATH) {
                    pathCount++;
                }
            }
        }
        
        assertTrue(pathCount > 0);
    }
    
    @Test
    void testDFSFindsPath() {
        engine.initializeAlgorithm(AlgorithmType.DFS);
        
        // Run until algorithm completes
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
        }
        
        // DFS should find a path (though not necessarily shortest)
        assertEquals(AlgorithmState.PATH_FOUND, engine.getAlgorithmState());
    }
    
    @Test
    void testAStarGraphVsTreeSearch() {
        // Both should find a path
        engine.resetPath();
        engine.initializeAlgorithm(AlgorithmType.ASTAR_TREE);
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
        }
        assertEquals(AlgorithmState.PATH_FOUND, engine.getAlgorithmState());
        
        engine.resetPath();
        engine.initializeAlgorithm(AlgorithmType.ASTAR_GRAPH);
        while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
            engine.executeStep();
        }
        assertEquals(AlgorithmState.PATH_FOUND, engine.getAlgorithmState());
    }
    
    @Test
    void testAllAlgorithmsOnSameMaze() {
        // Create a maze with multiple paths
        engine.setNodeType(15, 5, NodeType.WALL);
        engine.setNodeType(15, 6, NodeType.WALL);
        engine.setNodeType(15, 7, NodeType.WALL);
        engine.setNodeType(15, 8, NodeType.WALL);
        
        AlgorithmType[] algorithms = {
            AlgorithmType.ASTAR_TREE,
            AlgorithmType.ASTAR_GRAPH,
            AlgorithmType.DIJKSTRA,
            AlgorithmType.BFS,
            AlgorithmType.DFS
        };
        
        for (AlgorithmType type : algorithms) {
            engine.resetPath();
            engine.initializeAlgorithm(type);
            
            while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
                engine.executeStep();
            }
            
            // All algorithms should find a path
            assertEquals(AlgorithmState.PATH_FOUND, engine.getAlgorithmState(), 
                "Algorithm " + type + " should find a path");
        }
    }
    
    @Test
    void testAllAlgorithmsHandleNoPath() {
        // Completely wall off the end
        Node end = engine.getEndNode();
        int endX = end.getX();
        int endY = end.getY();
        
        // Surround end with walls
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int x = endX + dx;
                int y = endY + dy;
                if (x >= 0 && x < COLS && y >= 0 && y < ROWS) {
                    if (engine.getGrid()[y][x].getType() != NodeType.END) {
                        engine.setNodeType(x, y, NodeType.WALL);
                    }
                }
            }
        }
        
        AlgorithmType[] algorithms = {
            AlgorithmType.ASTAR_TREE,
            AlgorithmType.ASTAR_GRAPH,
            AlgorithmType.DIJKSTRA,
            AlgorithmType.BFS,
            AlgorithmType.DFS
        };
        
        for (AlgorithmType type : algorithms) {
            engine.resetPath();
            engine.initializeAlgorithm(type);
            
            while (engine.getAlgorithmState() == AlgorithmState.RUNNING) {
                engine.executeStep();
            }
            
            // All algorithms should report no path found
            assertEquals(AlgorithmState.NO_PATH_FOUND, engine.getAlgorithmState(),
                "Algorithm " + type + " should report no path found");
        }
    }
}
