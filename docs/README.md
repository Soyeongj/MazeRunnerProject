# FoP Project - Maze Runner, Team K-league

## 🔗 Overview
This project is a maze game developed using Java and the libGDX framework. The game challenges players to navigate through a dynamic maze with moving walls and other mechanics.

---

## 📌 Project Structure
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
│   ├── README.md           # Documentation and UMLs overview
│   ├── LICENSE.            # Details about License
│   ├── UML Diagram.png     # UML Diagram
│   └── javadoc/            # JavaDoc HTML files
├── gradle/                 # Gradle configuration files
├── .gradle/                # Gradle build system files
└── .idea/                  # IntelliJ IDEA project configuration files
```

### Class Hierarchy
Below is the organization of classes in this project:

### 1️⃣  Game Core
- **MazeRunnerGame**
- **GameScreen**
- **AbstractGameScreen** (Base class for screens)
    - **GameClearScreen** (inherits from `AbstractGameScreen`)
    - **GameOverScreen** (inherits from `AbstractGameScreen`)
- **MenuScreen**

### 2️⃣  Game Entities
- **Player**
- **Friends**
- **Griever**

### 3️⃣   Items and Objects
- **CollectibleItem** (Base class for all items)
    - **TrapItem** (inherits from `CollectibleItem`)
    - **Item** (inherits from `CollectibleItem`)
- **Key**

### 4️⃣  Environment
- **Wall**
- **Door**
- **Trap**

### 5️⃣  Game Utilities
- **HUD**
- **SoundManager**
- **Arrow**

---

## 🎯 UML Class Diagram
Below is a visual representation of the class hierarchy.

![UML Diagram](./UML%20Diagram.png)

---

## 🔍 How to Run and Use the Game

### Prerequisites
- Java Development Kit (JDK) 17 (Amazon Corretto 17 recommended)
- Gradle installed (if not bundled with the project)
- IntelliJ IDEA or any other preferred IDE

### Steps to Run the Game
1. Clone this repository:
   ```bash
   git clone https://github.com/Soyeongj/MazeRunnerProject.git
   ```
2. Open the project in IntelliJ IDEA.
3. Ensure all dependencies are resolved (Gradle sync may be required).
4. Navigate to the `de.tum.cit.fop.maze` package and run the `DesktopLauncher` class.
5. The game will launch in a new window.


### 🛠 Troubleshooting

### ✔️ Fix the Classpath
When running this project, make sure to use the provided **Run Configuration** and set the classpath correctly.  
Follow these steps:
1. Open **Run Configuration**
2. Set the **Classpath** to: `MazeRunnerProject.desktop.main`


### ✔️ Set the Main Class
In some cases, the **Main Class** may not be automatically detected.  
If this happens, set it manually to: `de.tum.cit.fop.maze.DesktopLauncher`

**Steps to set the Main Class:**
1. Open **Run Configuration**
2. Locate the **Main Class** field
3. Enter: `de.tum.cit.fop.maze.DesktopLauncher`


### ✔️ Fix Gradle JVM
Some systems may encounter issues with Gradle JVM settings.  
To fix this:
1. Click **"Open Gradle Settings"**
2. Set **Gradle JVM** to **"Project SDK"**


### ✔️ Fix VM Options for Windows, Linux, and Mac
Depending on your operating system, you may need to adjust the VM options.

#### ✅ **Windows & Linux**
If you are running the project on **Windows or Linux**, you must **remove** the `-XstartOnFirstThread` VM option.  
**How to remove it:**
1. Open **Run Configuration**
2. Remove the `-XstartOnFirstThread` option if present

#### ✅ **MacOS**
If you are running the project on **macOS**, you **must add** the `-XstartOnFirstThread` VM option **if it is not set automatically**.  
**How to add it:**
1. Open **Run Configuration**
2. Add the following VM option: `-XstartOnFirstThread`




---

## 🚀 Rules and Game Mechanics

### Basic Rules
1. The player starts in the safe zone of the maze with three friends and must reach the exit  within a time limit, carrying a key and at least one friend.
2. Walls move every 5 seconds based on their predefined direction (left, right, up, or down).
3. If the player or friends collide with obstacles(moving walls, traps) or a griever, the player loses a life. Each life is represented as a saved friend.
4. If the player lose all friends and the player lose one more life, the game is over.
5. Saving more friends awards bonus points; however, having more saved friends slows down the player's movement speed.

### Basic Controls
1. Use W, A, S, D keys to move the player up, left, down, and right, respectively.
2. Hold Shift to temporarily increase the player's speed for 2 seconds. This ability has a cooldown of 4 seconds.
3. Press 1 to zoom in and 2 to zoom out.
4. Press Esc during gameplay to pause the game. From the pause menu, you can select a different map to start a new game or continue playing.


##  Additional Features (Beyond Minimum Requirements)

### 🔸 Moving Walls
Certain walls, distinguished by their **unique colors**, move every **5 seconds**.  
While they pose **a threat** to the player's life, they are also **the only way to kill Grievers and obtain the key**.

### 🔸 Grievers & Key Mechanics
Grievers **chase the player within a certain range**.  
The only way to **kill a Griever** is by using **moving walls**.  
When a Griever is killed, it **drops a key**, which the player must collect to escape the maze.

### 🔸 Stun Ability
The player possesses the ability to **stun a Griever for 3 seconds** if they **face it within a certain range**, providing a brief tactical advantage.

### 🔸 Limited Vision
When the player collects a **trap item**, **the screen darkens** for 3 seconds, restricting the player's vision and making navigation more challenging.

### 🔸 Multiple Conditions for Exit
To escape through the exit, the player must **rescue and bring at least one friend** along, in addition to **finding the key**.

### 🔸 Friendship Dynamics
Friends, represented as **lives**, follow the player closely.
- Having more friends **grants bonus points**, but at the same time,
- Having more friends **reduces the player's speed**, requiring strategic planning to balance risk and reward.

---

## License
This project is licensed under the MIT License. See `LICENSE` for details.