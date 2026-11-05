# Pathfinding Algorithm Visualizer

A Java Swing application that allows users to interactively draw mazes and watch multiple pathfinding algorithms (A*, Dijkstra, BFS, DFS) find the shortest path between a start and end point. The application visualizes the algorithm's exploration process in real-time with configurable speed.

![Pathfinding Visualizer](https://img.shields.io/badge/Java-21+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

## Table of Contents

- [Features](#features)
- [Algorithms](#algorithms)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Technical Details](#technical-details)
- [Testing](#testing)
- [Maze Files](#maze-files)
- [License](#license)

## Features

- **Interactive Grid**: Draw walls and place start/end nodes using mouse
- **Real-time Animation**: Watch algorithms explore the grid step-by-step
- **Configurable Speed**: Adjust animation speed from 1ms to 100ms per step
- **Save/Load**: Persist maze layouts using Java Serialization
- **Multiple Algorithms**: A* Tree Search, A* Graph Search, Dijkstra, BFS, and DFS
- **Unit Tests**: Comprehensive JUnit 5 test suite
- **Clean Code**: Well-organized, documented codebase under 1,500 lines

## Algorithms

| Algorithm | Description | Guarantees |
|-----------|-------------|------------|
| **A* Tree Search** | A* that can re-expand nodes for better paths | Shortest path |
| **A* Graph Search** | A* with closed set (no re-expansion) | Shortest path, more efficient |
| **Dijkstra** | Uniform cost search without heuristic | Shortest path |
| **BFS** | Queue-based level-by-level exploration | Shortest path (unweighted) |
| **DFS** | Stack-based deep exploration | Finds path (not necessarily shortest) |

## Prerequisites

- **Java 21+**
- **Gradle 8.5+** (included via wrapper)

## Installation

```bash
# Clone or navigate to the project directory
cd pathfinding-visualizer

# Build the project
./gradlew build

# Or on Windows:
.\gradlew build
```

## Usage

### Running the Application

```bash
# Run the application
./gradlew run

# Or on Windows:
.\gradlew run
```

### Building the JAR

```bash
# Build JAR file
./gradlew jar

# Run the JAR
java -jar build/libs/pathfinding-visualizer-1.0.0.jar
```

### Mouse Controls

| Action | Key Combination | Result |
|--------|-----------------|--------|
| Draw Wall | Left-click & drag | Create impassable walls |
| Erase Wall | Right-click & drag | Remove walls |
| Move Start | Shift + click | Place start node (green) |
| Move End | Ctrl + click | Place end node (red) |

### Toolbar Controls

- **Algorithm Selector**: Choose from 5 pathfinding algorithms
- **Start/Stop**: Begin or pause the animation
- **Clear Path**: Reset visited nodes (keeps walls)
- **Clear Board**: Reset entire grid
- **Save Maze**: Save current layout to `.maze` file
- **Load Maze**: Load saved maze from `.maze` file
- **Speed Slider**: Adjust animation speed (1-100ms)

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/visualizer/
│   │       ├── Main.java               # Entry point
│   │       ├── MazeGenerator.java      # Maze file generator utility
│   │       ├── gui/
│   │       │   ├── ApplicationWindow.java
│   │       │   └── VisualizerPanel.java
│   │       ├── model/
│   │       │   ├── Node.java
│   │       │   ├── NodeType.java
│   │       │   └── MazeState.java
│   │       └── logic/
│   │           └── AlgorithmEngine.java
│   └── resources/
└── test/
    └── java/
        └── com/visualizer/
            ├── AlgorithmTest.java
            └── SerializationTest.java
```

## Technical Details

### Collections Framework

- **PriorityQueue<Node>**: Open set for A* and Dijkstra (sorted by fCost)
- **HashSet<Node>**: Closed set for visited nodes (O(1) lookup)
- **HashMap<Node, Node>**: Parent tracking for path reconstruction
- **Node[][]**: 2D grid matrix storing node states

### Swing GUI

- **JFrame**: Main application window
- **JPanel**: Custom `VisualizerPanel` with `paintComponent()` rendering
- **JComboBox**: Algorithm selector
- **JSlider**: Speed control
- **JButton**: Control buttons (Start, Clear, Save, Load)
- **JFileChooser**: File save/load dialog

### Serialization

- **MazeState**: Data transfer object for maze layouts
- **ObjectOutputStream/ObjectInputStream**: Binary file I/O
- **transient fields**: Runtime-only data excluded from save

### Threading

- All GUI operations on Event Dispatch Thread (EDT)
- Timer-based animation (60 FPS max)
- No blocking while loops in EDT

## Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew check

# Build with tests
./gradlew build
```

### Test Coverage

- **AlgorithmTest.java**: 21 tests for pathfinding algorithms
- **SerializationTest.java**: 9 tests for save/load functionality

## Maze Files

The project includes 5 pre-made maze files in the `mazes/` folder:

1. **01_simple_corridor.maze** - Simple horizontal barriers
2. **02_spiral.maze** - Spiral pattern
3. **03_rooms.maze** - Four connected rooms
4. **04_diagonal.maze** - Diagonal obstacles
5. **05_complex.maze** - Multiple paths and obstacles

To generate more mazes:

```bash
java -cp build/classes/java/main com.visualizer.MazeGenerator
```

## License

MIT License - See LICENSE file for details.
