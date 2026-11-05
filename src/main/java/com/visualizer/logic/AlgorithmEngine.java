package com.visualizer.logic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import com.visualizer.model.Node;
import com.visualizer.model.NodeType;

/**
 * Core pathfinding engine that executes multiple pathfinding algorithms.
 * Supports: A* Tree Search, A* Graph Search, Dijkstra's, BFS, and DFS.
 * Decoupled from GUI - returns step-by-step results for animation.
 */
public class AlgorithmEngine {
    
    public enum AlgorithmType {
        ASTAR_TREE("A* Tree Search"),
        ASTAR_GRAPH("A* Graph Search"),
        DIJKSTRA("Dijkstra"),
        BFS("Breadth First Search"),
        DFS("Depth First Search");
        
        private final String displayName;
        
        AlgorithmType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum AlgorithmState {
        IDLE,
        RUNNING,
        PATH_FOUND,
        NO_PATH_FOUND
    }
    
    private final int rows;
    private final int cols;
    private Node[][] grid;
    private Node startNode;
    private Node endNode;
    
    // Algorithm state
    private AlgorithmType currentAlgorithm;
    private AlgorithmState algorithmState;
    
    // Collections for pathfinding (used differently per algorithm)
    private PriorityQueue<Node> openSetPriority;  // For A* and Dijkstra
    private Deque<Node> openSetDeque;             // For BFS (Queue) and DFS (Stack)
    private HashSet<Node> closedSet;
    private HashMap<Node, Node> cameFrom;
    
    // Current step tracking
    private Node currentVisitingNode;
    
    /**
     * Creates a new AlgorithmEngine with the specified grid dimensions.
     * @param rows number of rows in the grid
     * @param cols number of columns in the grid
     */
    public AlgorithmEngine(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Node[rows][cols];
        this.algorithmState = AlgorithmState.IDLE;
        this.currentAlgorithm = AlgorithmType.ASTAR_TREE;
        
        initializeGrid();
    }
    
    /**
     * Initializes the grid with empty nodes and default start/end positions.
     */
    private void initializeGrid() {
        // Create empty grid
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x] = new Node(x, y);
            }
        }
        
        // Set default start and end positions
        int startY = rows / 2;
        int startX = cols / 5;
        int endX = cols - cols / 5;
        
        grid[startY][startX].setType(NodeType.START);
        grid[startY][endX].setType(NodeType.END);
        
        startNode = grid[startY][startX];
        endNode = grid[startY][endX];
    }
    
    /**
     * Resets only the pathfinding state (visited/path nodes back to empty).
     * Keeps walls, start, and end intact.
     */
    public void resetPath() {
        algorithmState = AlgorithmState.IDLE;
        currentVisitingNode = null;
        
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Node node = grid[y][x];
                node.resetPathfindingState();
                
                if (node.getType() == NodeType.VISITED || node.getType() == NodeType.PATH) {
                    node.setType(NodeType.EMPTY);
                }
            }
        }
    }
    
    /**
     * Resets the entire board to initial state.
     */
    public void resetBoard() {
        algorithmState = AlgorithmState.IDLE;
        currentVisitingNode = null;
        initializeGrid();
    }
    
    /**
     * Initializes the algorithm data structures based on algorithm type.
     * @param algorithmType the algorithm to use
     * @return the starting node
     */
    public Node initializeAlgorithm(AlgorithmType algorithmType) {
        this.currentAlgorithm = algorithmType;
        this.algorithmState = AlgorithmState.RUNNING;
        
        // Reset pathfinding state on all nodes
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x].resetPathfindingState();
                if (grid[y][x].getType() == NodeType.VISITED || grid[y][x].getType() == NodeType.PATH) {
                    grid[y][x].setType(NodeType.EMPTY);
                }
            }
        }
        
        // Initialize collections
        closedSet = new HashSet<>();
        cameFrom = new HashMap<>();
        
        // Set start node costs
        startNode.setGCost(0);
        
        switch (algorithmType) {
            case ASTAR_TREE:
            case ASTAR_GRAPH:
            case DIJKSTRA:
                openSetPriority = new PriorityQueue<>();
                openSetDeque = null;
                
                if (algorithmType == AlgorithmType.ASTAR_TREE || algorithmType == AlgorithmType.ASTAR_GRAPH) {
                    startNode.setHCost(startNode.manhattanDistance(endNode));
                } else {
                    startNode.setHCost(0); // Dijkstra doesn't use heuristic
                }
                startNode.calculateFCost();
                openSetPriority.add(startNode);
                break;
                
            case BFS:
                openSetDeque = new ArrayDeque<>(); // Used as Queue (FIFO)
                openSetPriority = null;
                startNode.setHCost(0);
                openSetDeque.addLast(startNode); // Enqueue
                break;
                
            case DFS:
                openSetDeque = new ArrayDeque<>(); // Used as Stack (LIFO)
                openSetPriority = null;
                startNode.setHCost(0);
                openSetDeque.addFirst(startNode); // Push onto stack
                break;
        }
        
        return startNode;
    }
    
    /**
     * Executes one step of the pathfinding algorithm.
     * @return the node that was visited this step, or null if algorithm finished
     */
    public Node executeStep() {
        if (algorithmState != AlgorithmState.RUNNING) {
            return null;
        }
        
        // Choose the appropriate step method based on algorithm type
        switch (currentAlgorithm) {
            case ASTAR_TREE:
                return executeAStarTreeStep();
            case ASTAR_GRAPH:
                return executeAStarGraphStep();
            case DIJKSTRA:
                return executeDijkstraStep();
            case BFS:
                return executeBFSStep();
            case DFS:
                return executeDFSStep();
            default:
                return null;
        }
    }
    
    /**
     * A* Tree Search: Can revisit nodes if a better path is found (allows re-expansion).
     */
    private Node executeAStarTreeStep() {
        if (openSetPriority.isEmpty()) {
            algorithmState = AlgorithmState.NO_PATH_FOUND;
            return null;
        }
        
        Node current = openSetPriority.poll();
        
        // Check if we reached the end
        if (current.equals(endNode)) {
            algorithmState = AlgorithmState.PATH_FOUND;
            reconstructPath();
            return current;
        }
        
        // Mark as visited
        if (current.getType() != NodeType.START && current.getType() != NodeType.END) {
            current.setType(NodeType.VISITED);
        }
        
        currentVisitingNode = current;
        
        // Process neighbors (Tree Search: can revisit if better path found)
        processNeighborsAStarTree(current);
        
        return current;
    }
    
    /**
     * A* Graph Search: Never revisits nodes (more efficient, uses closed set).
     */
    private Node executeAStarGraphStep() {
        if (openSetPriority.isEmpty()) {
            algorithmState = AlgorithmState.NO_PATH_FOUND;
            return null;
        }
        
        Node current = openSetPriority.poll();
        
        // Skip if already processed (Graph Search prevents re-expansion)
        if (closedSet.contains(current)) {
            return executeAStarGraphStep();
        }
        
        // Check if we reached the end
        if (current.equals(endNode)) {
            algorithmState = AlgorithmState.PATH_FOUND;
            reconstructPath();
            return current;
        }
        
        // Add to closed set
        closedSet.add(current);
        
        // Mark as visited
        if (current.getType() != NodeType.START && current.getType() != NodeType.END) {
            current.setType(NodeType.VISITED);
        }
        
        currentVisitingNode = current;
        
        // Process neighbors
        processNeighborsAStarGraph(current);
        
        return current;
    }
    
    /**
     * Dijkstra's Algorithm: Similar to A* Graph Search but without heuristic.
     */
    private Node executeDijkstraStep() {
        if (openSetPriority.isEmpty()) {
            algorithmState = AlgorithmState.NO_PATH_FOUND;
            return null;
        }
        
        Node current = openSetPriority.poll();
        
        // Skip if already processed
        if (closedSet.contains(current)) {
            return executeDijkstraStep();
        }
        
        // Check if we reached the end
        if (current.equals(endNode)) {
            algorithmState = AlgorithmState.PATH_FOUND;
            reconstructPath();
            return current;
        }
        
        // Add to closed set
        closedSet.add(current);
        
        // Mark as visited
        if (current.getType() != NodeType.START && current.getType() != NodeType.END) {
            current.setType(NodeType.VISITED);
        }
        
        currentVisitingNode = current;
        
        // Process neighbors
        processNeighborsDijkstra(current);
        
        return current;
    }
    
    /**
     * Breadth First Search: Explores level by level (guarantees shortest path).
     */
    private Node executeBFSStep() {
        if (openSetDeque.isEmpty()) {
            algorithmState = AlgorithmState.NO_PATH_FOUND;
            return null;
        }
        
        Node current = openSetDeque.pollFirst(); // Dequeue from front (FIFO)
        
        // Skip if already visited
        if (closedSet.contains(current)) {
            return executeBFSStep();
        }
        
        // Add to closed set
        closedSet.add(current);
        
        // Check if we reached the end
        if (current.equals(endNode)) {
            algorithmState = AlgorithmState.PATH_FOUND;
            reconstructPath();
            return current;
        }
        
        // Mark as visited
        if (current.getType() != NodeType.START && current.getType() != NodeType.END) {
            current.setType(NodeType.VISITED);
        }
        
        currentVisitingNode = current;
        
        // Process neighbors
        processNeighborsBFS(current);
        
        return current;
    }
    
    /**
     * Depth First Search: Explores as deep as possible before backtracking.
     * Does NOT guarantee shortest path.
     */
    private Node executeDFSStep() {
        if (openSetDeque.isEmpty()) {
            algorithmState = AlgorithmState.NO_PATH_FOUND;
            return null;
        }
        
        Node current = openSetDeque.pollFirst(); // Pop from front (LIFO behavior)
        
        // Skip if already visited
        if (closedSet.contains(current)) {
            return executeDFSStep();
        }
        
        // Add to closed set
        closedSet.add(current);
        
        // Check if we reached the end
        if (current.equals(endNode)) {
            algorithmState = AlgorithmState.PATH_FOUND;
            reconstructPath();
            return current;
        }
        
        // Mark as visited
        if (current.getType() != NodeType.START && current.getType() != NodeType.END) {
            current.setType(NodeType.VISITED);
        }
        
        currentVisitingNode = current;
        
        // Process neighbors
        processNeighborsDFS(current);
        
        return current;
    }
    
    // Neighbor Processing Methods
    
    /**
     * A* Tree Search: Allows re-expansion if better path found.
     */
    private void processNeighborsAStarTree(Node current) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int newX = current.getX() + dir[0];
            int newY = current.getY() + dir[1];
            
            if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) continue;
            
            Node neighbor = grid[newY][newX];
            
            if (neighbor.getType() == NodeType.WALL) continue;
            
            double tentativeGCost = current.getGCost() + 1;
            
            // Tree Search: Always update if better path found (no closed set check)
            if (tentativeGCost < neighbor.getGCost()) {
                cameFrom.put(neighbor, current);
                neighbor.setGCost(tentativeGCost);
                neighbor.setHCost(neighbor.manhattanDistance(endNode));
                neighbor.calculateFCost();
                neighbor.setParent(current);
                openSetPriority.add(neighbor);
            }
        }
    }
    
    /**
     * A* Graph Search: Only expands unvisited nodes.
     */
    private void processNeighborsAStarGraph(Node current) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int newX = current.getX() + dir[0];
            int newY = current.getY() + dir[1];
            
            if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) continue;
            
            Node neighbor = grid[newY][newX];
            
            if (neighbor.getType() == NodeType.WALL || closedSet.contains(neighbor)) continue;
            
            double tentativeGCost = current.getGCost() + 1;
            
            if (tentativeGCost < neighbor.getGCost()) {
                cameFrom.put(neighbor, current);
                neighbor.setGCost(tentativeGCost);
                neighbor.setHCost(neighbor.manhattanDistance(endNode));
                neighbor.calculateFCost();
                neighbor.setParent(current);
                openSetPriority.add(neighbor);
            }
        }
    }
    
    /**
     * Dijkstra: No heuristic used.
     */
    private void processNeighborsDijkstra(Node current) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int newX = current.getX() + dir[0];
            int newY = current.getY() + dir[1];
            
            if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) continue;
            
            Node neighbor = grid[newY][newX];
            
            if (neighbor.getType() == NodeType.WALL || closedSet.contains(neighbor)) continue;
            
            double tentativeGCost = current.getGCost() + 1;
            
            if (tentativeGCost < neighbor.getGCost()) {
                cameFrom.put(neighbor, current);
                neighbor.setGCost(tentativeGCost);
                neighbor.setHCost(0); // No heuristic
                neighbor.calculateFCost();
                neighbor.setParent(current);
                openSetPriority.add(neighbor);
            }
        }
    }
    
    /**
     * BFS: Add to queue (FIFO order).
     */
    private void processNeighborsBFS(Node current) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int newX = current.getX() + dir[0];
            int newY = current.getY() + dir[1];
            
            if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) continue;
            
            Node neighbor = grid[newY][newX];
            
            if (neighbor.getType() == NodeType.WALL || closedSet.contains(neighbor)) continue;
            
            // BFS discovers nodes at their first encounter
            if (neighbor.getGCost() == Double.MAX_VALUE) {
                cameFrom.put(neighbor, current);
                neighbor.setGCost(current.getGCost() + 1);
                neighbor.setParent(current);
                openSetDeque.addLast(neighbor); // Enqueue at back
            }
        }
    }
    
    /**
     * DFS: Add to stack (LIFO order).
     */
    private void processNeighborsDFS(Node current) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        // Process in reverse order so that "up" is explored first (stack behavior)
        for (int i = directions.length - 1; i >= 0; i--) {
            int newX = current.getX() + directions[i][0];
            int newY = current.getY() + directions[i][1];
            
            if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) continue;
            
            Node neighbor = grid[newY][newX];
            
            if (neighbor.getType() == NodeType.WALL || closedSet.contains(neighbor)) continue;
            
            if (neighbor.getGCost() == Double.MAX_VALUE) {
                cameFrom.put(neighbor, current);
                neighbor.setGCost(current.getGCost() + 1);
                neighbor.setParent(current);
                openSetDeque.addFirst(neighbor); // Push to front (stack)
            }
        }
    }
    
    /**
     * Reconstructs the path from end to start using the cameFrom map.
     * Marks path nodes with PATH type.
     */
    private void reconstructPath() {
        Node current = endNode;
        
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            
            if (current.getType() != NodeType.START) {
                current.setType(NodeType.PATH);
            }
        }
    }
    
    // Getters and Setters
    
    public Node[][] getGrid() { return grid; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public AlgorithmState getAlgorithmState() { return algorithmState; }
    public Node getStartNode() { return startNode; }
    public Node getEndNode() { return endNode; }
    
    /**
     * Sets the node type at the specified position.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param type the new node type
     */
    public void setNodeType(int x, int y, NodeType type) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) {
            return;
        }
        
        Node node = grid[y][x];
        
        // Update start/end references
        if (type == NodeType.START) {
            if (startNode != null && startNode != node) {
                startNode.setType(NodeType.EMPTY);
            }
            startNode = node;
        } else if (type == NodeType.END) {
            if (endNode != null && endNode != node) {
                endNode.setType(NodeType.EMPTY);
            }
            endNode = node;
        }
        
        // Clear start/end reference if overwriting
        if (node == startNode && type != NodeType.START) {
            startNode = null;
        }
        if (node == endNode && type != NodeType.END) {
            endNode = null;
        }
        
        node.setType(type);
    }
    
    /**
     * Loads a maze state from a MazeState object.
     * @param mazeState the maze state to load
     */
    public void loadMazeState(com.visualizer.model.MazeState mazeState) {
        this.grid = mazeState.reconstructGrid();
        
        // Find start and end nodes
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Node node = grid[y][x];
                if (node.getType() == NodeType.START) {
                    startNode = node;
                } else if (node.getType() == NodeType.END) {
                    endNode = node;
                }
            }
        }
        
        algorithmState = AlgorithmState.IDLE;
    }
    
    /**
     * Creates a MazeState for saving.
     * @return the current maze state
     */
    public com.visualizer.model.MazeState createMazeState() {
        int startX = startNode != null ? startNode.getX() : -1;
        int startY = startNode != null ? startNode.getY() : -1;
        int endX = endNode != null ? endNode.getX() : -1;
        int endY = endNode != null ? endNode.getY() : -1;
        
        return new com.visualizer.model.MazeState(grid, startX, startY, endX, endY);
    }
}
