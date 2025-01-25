# Maze Game Project

## Overview
This project is a maze game developed using Java and the libGDX framework. The game challenges players to navigate through a dynamic maze with moving walls and other mechanics.

---

## Project Structure
```
foph2425projectfop-kleague/
├── de.tum.cit.fop.maze/    # All game-related classes are stored here
│   ├── AbstractGameScreen.java # Base class for all screens
│   ├── Arrow.java          # Arrows for player interaction
│   ├── CollectibleItem.java  # Items that can be collected 
│   ├── Door.java           # Door elements in the maze
│   ├── Friends.java        # Ally characters in the game  
│   ├── GameClearScreen.java  # Screen shown on game completion
│   ├── GameOverScreen.java   # Screen shown when the game is over  
│   ├── GameScreen.java      # General screen logic      
│   ├── Griever.java        # Enemy characters in the game
│   ├── HUD.java            # Heads-up display for the game
│   ├── Item.java           # Items that update player's state
│   ├── Key.java            # Key items to unlock doors
│   ├── MazeRunnerGame.java # Main game class handling game flow
│   ├── MenuScreen.java     # Main menu of the game 
│   ├── Player.java    # Player-related logic (movement, interactions)
│   ├── SoundManager.java   # Manages game sounds
│   ├── Trap.java           # Traps in the environment
│   ├── TrapItem.java       # Items that trigger traps
│   └── Wall.java           # Static and dynamic walls
├── desktop/                # Desktop launcher for the game
│   └── DesktopLauncher.java
├── assets/                 # Game assets (images, sounds, and maps)
├── docs/                   # Documentation files
│   ├── README.md           # Documentation overview
│   ├── javadoc/            # JavaDoc HTML files
│   └── uml-diagram.png     # UML class diagram
├── gradle/                 # Gradle configuration files
├── .gradle/                # Gradle build system files
└── .idea/                  # IntelliJ IDEA project configuration files
```

### Class Hierarchy
Below is the organization of classes in this project:

#### 1. Game Core
- **MazeRunnerGame**
- **GameScreen**
- **AbstractGameScreen** (Base class for screens)
    - **GameClearScreen** (inherits from `AbstractGameScreen`)
    - **GameOverScreen** (inherits from `AbstractGameScreen`)
- **MenuScreen**

#### 2. Game Entities
- **Player**
- **Friends**
- **Griever**

#### 3. Items and Objects
- **Item** (Base class for all items)
    - **CollectibleItem** (inherits from `Item`)
        - **TrapItem** (inherits from `CollectibleItem`)
        - **Key** (inherits from `CollectibleItem`)

#### 4. Environment
- **Wall**
- **Door**
- **Trap**

#### 5. Game Utilities
- **HUD**
- **SoundManager**
- **Arrow**

---

## UML Class Diagram
Below is a visual representation of the class hierarchy. (Refer to `docs/uml-diagram.png` for the full diagram.)

![UML Diagram](./docs/uml-diagram.png)

---

## How to Run and Use the Game

### Prerequisites
- Java Development Kit (JDK) 8 or later
- Gradle installed (if not bundled with the project)
- IntelliJ IDEA or any other preferred IDE

### Steps to Run the Game
1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/maze-game.git
   ```
2. Open the project in IntelliJ IDEA.
3. Ensure all dependencies are resolved (Gradle sync may be required).
4. Navigate to the `de.tum.cit.fop.maze` package and run the `MazeRunnerGame` class.
5. The game will launch in a new window.

---

## Rules and Game Mechanics

### Basic Rules
1. The player starts at the entrance of the maze and must reach the exit.
2. Walls move every 10 seconds based on their predefined direction (left, right, up, or down).
3. If the player collides with a wall, the game is over.
4. Completing the maze within a time limit awards bonus points.

### Additional Features (Beyond Minimum Requirements)
- **Dynamic Walls**: Walls return to their original positions after each move.
- **Power-Ups**: Players can collect items to temporarily stop wall movement.
- **Timer**: Players must complete the maze within a certain time limit.
- **Score System**: Tracks the player's performance based on time and power-up collection.

---

## License
This project is licensed under the MIT License. See `LICENSE` for details.

