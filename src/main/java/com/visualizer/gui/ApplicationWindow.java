package com.visualizer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.visualizer.logic.AlgorithmEngine;
import com.visualizer.logic.AlgorithmEngine.AlgorithmType;
import com.visualizer.model.MazeState;
import com.visualizer.model.Node;

/**
 * Main application window containing the visualizer panel and control toolbar.
 * Handles menu events and coordinates between UI components and the algorithm engine.
 */
public class ApplicationWindow extends JFrame {
    
    private static final String TITLE = "Pathfinding Algorithm Visualizer";
    private static final int DEFAULT_ROWS = 25;
    private static final int DEFAULT_COLS = 40;
    private static final int TIMER_DELAY = 30; // milliseconds between steps
    
    private final AlgorithmEngine engine;
    private final VisualizerPanel visualizerPanel;
    private final javax.swing.Timer animationTimer;
    
    // UI Components
    private JComboBox<String> algorithmComboBox;
    private JSlider speedSlider;
    private JLabel speedLabel;
    private JButton startButton;
    private JButton clearPathButton;
    private JButton clearBoardButton;
    private JButton saveButton;
    private JButton loadButton;
    
    /**
     * Creates and initializes the main application window.
     */
    public ApplicationWindow() {
        super(TITLE);
        
        this.engine = new AlgorithmEngine(DEFAULT_ROWS, DEFAULT_COLS);
        this.visualizerPanel = new VisualizerPanel(engine);
        this.animationTimer = new javax.swing.Timer(TIMER_DELAY, this::timerTick);
        
        initializeUI();
        setupWindow();
    }
    
    /**
     * Initializes all UI components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create toolbar
        JPanel toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);
        
        // Add visualizer panel
        add(visualizerPanel, BorderLayout.CENTER);
        
        // Initialize timer (not started yet)
        animationTimer.setInitialDelay(0);
    }
    
    /**
     * Creates the toolbar with all control buttons.
     * @return the toolbar panel
     */
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(Color.DARK_GRAY);
        
        // Algorithm selector
        JLabel algoLabel = new JLabel("Algorithm:");
        algoLabel.setForeground(Color.WHITE);
        toolbar.add(algoLabel);
        
        algorithmComboBox = new JComboBox<>(new String[]{
            "A* Tree Search",
            "A* Graph Search", 
            "Dijkstra",
            "Breadth First Search",
            "Depth First Search"
        });
        algorithmComboBox.setSelectedIndex(0);
        algorithmComboBox.addActionListener(e -> onAlgorithmChanged());
        toolbar.add(algorithmComboBox);
        
        toolbar.add(createSeparator());
        
        // Start button
        startButton = createButton("Start", e -> onStartClicked());
        toolbar.add(startButton);
        
        // Clear Path button
        clearPathButton = createButton("Clear Path", e -> onClearPathClicked());
        toolbar.add(clearPathButton);
        
        // Clear Board button
        clearBoardButton = createButton("Clear Board", e -> onClearBoardClicked());
        toolbar.add(clearBoardButton);
        
        toolbar.add(createSeparator());
        
        // Save button
        saveButton = createButton("Save Maze", e -> onSaveClicked());
        toolbar.add(saveButton);
        
        // Load button
        loadButton = createButton("Load Maze", e -> onLoadClicked());
        toolbar.add(loadButton);
        
        toolbar.add(createSeparator());
        
        // Speed slider
        JLabel speedTitleLabel = new JLabel("Speed:");
        speedTitleLabel.setForeground(Color.WHITE);
        toolbar.add(speedTitleLabel);
        
        speedSlider = new JSlider(1, 100, 50); // Min: 1ms (fast), Max: 100ms (slow), Default: 50ms
        speedSlider.setPreferredSize(new Dimension(100, 25));
        speedSlider.setBackground(Color.DARK_GRAY);
        speedSlider.addChangeListener(e -> onSpeedChanged());
        toolbar.add(speedSlider);
        
        speedLabel = new JLabel("50ms");
        speedLabel.setForeground(Color.LIGHT_GRAY);
        speedLabel.setPreferredSize(new Dimension(45, 25));
        toolbar.add(speedLabel);
        
        return toolbar;
    }
    
    /**
     * Creates a styled button.
     * @param text the button text
     * @param listener the action listener
     * @return the created button
     */
    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        button.setFocusable(false);
        return button;
    }
    
    /**
     * Creates a vertical separator for the toolbar.
     * @return the separator component
     */
    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 25));
        return separator;
    }
    
    /**
     * Configures the window properties.
     */
    private void setupWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null); // Center on screen
    }
    
    // Event Handlers
    
    /**
     * Handles algorithm selection changes.
     */
    private void onAlgorithmChanged() {
        // Just update the selection - actual algorithm is chosen when Start is clicked
    }
    
    /**
     * Handles speed slider changes.
     */
    private void onSpeedChanged() {
        int delay = speedSlider.getValue();
        speedLabel.setText(delay + "ms");
        animationTimer.setDelay(delay);
    }
    
    /**
     * Handles Start button click.
     */
    private void onStartClicked() {
        if (animationTimer.isRunning()) {
            // Stop the animation
            animationTimer.stop();
            startButton.setText("Start");
            return;
        }
        
        // Reset and initialize algorithm
        engine.resetPath();
        
        // Map dropdown selection to algorithm type
        AlgorithmType type = switch (algorithmComboBox.getSelectedIndex()) {
            case 0 -> AlgorithmType.ASTAR_TREE;
            case 1 -> AlgorithmType.ASTAR_GRAPH;
            case 2 -> AlgorithmType.DIJKSTRA;
            case 3 -> AlgorithmType.BFS;
            case 4 -> AlgorithmType.DFS;
            default -> AlgorithmType.ASTAR_TREE;
        };
        
        engine.initializeAlgorithm(type);
        
        // Start animation
        startButton.setText("Stop");
        animationTimer.start();
    }
    
    /**
     * Handles Clear Path button click.
     */
    private void onClearPathClicked() {
        animationTimer.stop();
        startButton.setText("Start");
        engine.resetPath();
        visualizerPanel.repaint();
    }
    
    /**
     * Handles Clear Board button click.
     */
    private void onClearBoardClicked() {
        animationTimer.stop();
        startButton.setText("Start");
        engine.resetBoard();
        visualizerPanel.repaint();
    }
    
    /**
     * Handles Save Maze button click.
     */
    private void onSaveClicked() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Maze Layout");
        fileChooser.setSelectedFile(new File("custom_layout.maze"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Maze Files (*.maze)", "maze"));
        
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Ensure .maze extension
            if (!file.getName().endsWith(".maze")) {
                file = new File(file.getAbsolutePath() + ".maze");
            }
            
            saveMazeToFile(file);
        }
    }
    
    /**
     * Saves the current maze state to a file.
     * @param file the file to save to
     */
    private void saveMazeToFile(File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            MazeState mazeState = engine.createMazeState();
            oos.writeObject(mazeState);
            JOptionPane.showMessageDialog(this, 
                "Maze saved successfully!", 
                "Save Complete", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving maze: " + e.getMessage(), 
                "Save Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handles Load Maze button click.
     */
    private void onLoadClicked() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Maze Layout");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Maze Files (*.maze)", "maze"));
        
        // Set default directory to mazes folder
        File mazesDir = new File("mazes");
        if (mazesDir.exists() && mazesDir.isDirectory()) {
            fileChooser.setCurrentDirectory(mazesDir);
        }
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            loadMazeFromFile(file);
        }
    }
    
    /**
     * Loads a maze state from a file.
     * @param file the file to load from
     */
    private void loadMazeFromFile(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            MazeState mazeState = (MazeState) ois.readObject();
            
            // Validate dimensions
            if (mazeState.getRows() != engine.getRows() || mazeState.getCols() != engine.getCols()) {
                JOptionPane.showMessageDialog(this, 
                    "Maze dimensions do not match. Expected " + engine.getRows() + "x" + engine.getCols() + 
                    ", got " + mazeState.getRows() + "x" + mazeState.getCols(), 
                    "Load Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            engine.loadMazeState(mazeState);
            animationTimer.stop();
            startButton.setText("Start");
            visualizerPanel.repaint();
            
            JOptionPane.showMessageDialog(this, 
                "Maze loaded successfully!", 
                "Load Complete", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading maze: " + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Timer tick handler - executes one step of the algorithm.
     * @param e the action event
     */
    private void timerTick(ActionEvent e) {
        Node visitedNode = engine.executeStep();
        
        if (visitedNode != null) {
            visualizerPanel.repaint();
        }
        
        // Check if algorithm finished
        if (engine.getAlgorithmState() != AlgorithmEngine.AlgorithmState.RUNNING) {
            animationTimer.stop();
            startButton.setText("Start");
            visualizerPanel.repaint();
        }
    }
    
    /**
     * Entry point for the application.
     * Ensures UI is created on the Event Dispatch Thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ApplicationWindow window = new ApplicationWindow();
            window.setVisible(true);
        });
    }
}
