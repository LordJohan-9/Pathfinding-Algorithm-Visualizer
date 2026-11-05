package com.visualizer.gui;

import com.visualizer.logic.AlgorithmEngine;
import com.visualizer.logic.AlgorithmEngine.AlgorithmType;
import com.visualizer.model.Node;
import com.visualizer.model.NodeType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Custom JPanel that renders the pathfinding grid and handles mouse input.
 * Uses low-level Graphics rendering for the grid visualization.
 */
public class VisualizerPanel extends JPanel {
    
    private static final int CELL_SIZE = 20;
    private static final int GRID_LINE_WIDTH = 1;
    
    private final AlgorithmEngine engine;
    private final int rows;
    private final int cols;
    
    // Interaction state
    private boolean isDrawingWall = false;
    private boolean isErasingWall = false;
    private boolean isSettingStart = false;
    private boolean isSettingEnd = false;
    
    /**
     * Creates a new VisualizerPanel with the specified engine.
     * @param engine the algorithm engine providing the grid data
     */
    public VisualizerPanel(AlgorithmEngine engine) {
        this.engine = engine;
        this.rows = engine.getRows();
        this.cols = engine.getCols();
        
        setPreferredSize(new Dimension(
            cols * CELL_SIZE + GRID_LINE_WIDTH,
            rows * CELL_SIZE + GRID_LINE_WIDTH
        ));
        
        setBackground(Color.GRAY);
        setupMouseListeners();
    }
    
    /**
     * Sets up mouse listeners for drawing walls and placing nodes.
     */
    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseRelease(e);
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
        };
        
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        
        // Keyboard listener for modifiers
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    isSettingStart = true;
                } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isSettingEnd = true;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    isSettingStart = false;
                } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isSettingEnd = false;
                }
            }
        });
    }
    
    /**
     * Handles mouse press events for starting wall drawing or node placement.
     */
    private void handleMousePress(MouseEvent e) {
        Point gridPos = screenToGrid(e.getPoint());
        if (!isValidGridPosition(gridPos)) return;
        
        Node clickedNode = engine.getGrid()[gridPos.y][gridPos.x];
        
        // Check for start/end placement with modifiers
        if (isSettingStart || (e.isShiftDown() && !SwingUtilities.isRightMouseButton(e))) {
            engine.setNodeType(gridPos.x, gridPos.y, NodeType.START);
            repaint();
            return;
        }
        
        if (isSettingEnd || (e.isControlDown() && !SwingUtilities.isRightMouseButton(e))) {
            engine.setNodeType(gridPos.x, gridPos.y, NodeType.END);
            repaint();
            return;
        }
        
        // Wall drawing/erasing
        if (SwingUtilities.isLeftMouseButton(e)) {
            isDrawingWall = true;
            // Don't overwrite start/end nodes
            if (clickedNode.getType() != NodeType.START && clickedNode.getType() != NodeType.END) {
                engine.setNodeType(gridPos.x, gridPos.y, NodeType.WALL);
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            isErasingWall = true;
            if (clickedNode.getType() != NodeType.START && clickedNode.getType() != NodeType.END) {
                engine.setNodeType(gridPos.x, gridPos.y, NodeType.EMPTY);
            }
        }
        
        repaint();
    }
    
    /**
     * Handles mouse release events to stop drawing.
     */
    private void handleMouseRelease(MouseEvent e) {
        isDrawingWall = false;
        isErasingWall = false;
    }
    
    /**
     * Handles mouse drag events for continuous drawing.
     */
    private void handleMouseDrag(MouseEvent e) {
        Point gridPos = screenToGrid(e.getPoint());
        if (!isValidGridPosition(gridPos)) return;
        
        Node node = engine.getGrid()[gridPos.y][gridPos.x];
        
        // Don't overwrite start/end nodes
        if (node.getType() == NodeType.START || node.getType() == NodeType.END) {
            return;
        }
        
        if (isDrawingWall) {
            engine.setNodeType(gridPos.x, gridPos.y, NodeType.WALL);
            repaint();
        } else if (isErasingWall) {
            engine.setNodeType(gridPos.x, gridPos.y, NodeType.EMPTY);
            repaint();
        }
    }
    
    /**
     * Converts screen coordinates to grid coordinates.
     * @param screenPoint the screen position
     * @return the grid position
     */
    private Point screenToGrid(Point screenPoint) {
        int gridX = screenPoint.x / CELL_SIZE;
        int gridY = screenPoint.y / CELL_SIZE;
        return new Point(gridX, gridY);
    }
    
    /**
     * Checks if a grid position is valid.
     * @param gridPos the grid position to check
     * @return true if within bounds
     */
    private boolean isValidGridPosition(Point gridPos) {
        return gridPos.x >= 0 && gridPos.x < cols && gridPos.y >= 0 && gridPos.y < rows;
    }
    
    /**
     * Custom paint method that renders the grid.
     * @param g the Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        Node[][] grid = engine.getGrid();
        
        // Draw each cell
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Node node = grid[y][x];
                drawCell(g2d, x, y, node.getType().getColor());
            }
        }
        
        // Draw grid lines
        g2d.setColor(Color.LIGHT_GRAY);
        for (int y = 0; y <= rows; y++) {
            g2d.drawLine(0, y * CELL_SIZE, cols * CELL_SIZE, y * CELL_SIZE);
        }
        for (int x = 0; x <= cols; x++) {
            g2d.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, rows * CELL_SIZE);
        }
        
        // Draw "No Path Found" message if applicable
        if (engine.getAlgorithmState() == AlgorithmEngine.AlgorithmState.NO_PATH_FOUND) {
            drawNoPathMessage(g2d);
        }
    }
    
    /**
     * Draws a single cell at the specified grid position.
     * @param g2d the Graphics2D context
     * @param x the grid x-coordinate
     * @param y the grid y-coordinate
     * @param color the fill color
     */
    private void drawCell(Graphics2D g2d, int x, int y, Color color) {
        g2d.setColor(color);
        g2d.fillRect(
            x * CELL_SIZE + GRID_LINE_WIDTH,
            y * CELL_SIZE + GRID_LINE_WIDTH,
            CELL_SIZE - GRID_LINE_WIDTH,
            CELL_SIZE - GRID_LINE_WIDTH
        );
    }
    
    /**
     * Draws the "No Path Found" message centered on the grid.
     * @param g2d the Graphics2D context
     */
    private void drawNoPathMessage(Graphics2D g2d) {
        String message = "No Path Found";
        
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        
        int textWidth = fm.stringWidth(message);
        int textHeight = fm.getHeight();
        
        int centerX = (cols * CELL_SIZE) / 2;
        int centerY = (rows * CELL_SIZE) / 2;
        
        // Draw background box
        int boxPadding = 20;
        int boxWidth = textWidth + boxPadding * 2;
        int boxHeight = textHeight + boxPadding;
        
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(
            centerX - boxWidth / 2,
            centerY - boxHeight / 2,
            boxWidth,
            boxHeight
        );
        
        // Draw text
        g2d.setColor(Color.WHITE);
        g2d.drawString(
            message,
            centerX - textWidth / 2,
            centerY + fm.getAscent() / 2 - fm.getDescent() / 2
        );
    }
}
