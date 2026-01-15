<div align="center">

# 🕹️ Ex3: Pac-Man & Graph Algorithms
**Ariel University | Introduction to Computer Science**

<br>

**Student:** Bnaya Amitay  
**ID:** 213741051

<br>

---

### 📺 Project Video Demo
**Watch the video to see the algorithm explanation and gameplay:**

[**Watch Video on YouTube/Drive**](INSERT_YOUR_LINK_HERE)

---

<br>

## 🧠 My Smart Algorithm Logic
The goal of this algorithm is to clear the map efficiently while staying safe. <br>
It uses **BFS** to calculate distances and makes decisions based on this logic:

<br>

### 🛠️ How it Works 
1. **Virtual Walls (Ghost House)**: The algorithm marks the ghost respawn area as "walls" <br>
   to prevent Pac-Man from getting trapped inside.
2. **Ghost Safety Zones**: It creates "virtual walls" around dangerous ghosts. <br>
   This forces the BFS to find paths that stay away from danger.
3. **Dynamic Safety**: The safety radius is 8 steps, but reduces to 3 steps <br>
   when only 5 food pellets remain to finish the game faster.
4. **Manual Override**: You can switch to **Manual Mode** at any time <br>
   by pressing **ENTER** and controlling Pac-Man with your keyboard.

<br>

### 🚀 Priority List
* **Trap Detection**: If ghosts are near, it takes the only safe exit.
* **Emergency Escape**: If a ghost is **less than 3 steps away**, it runs away.
* **Power Pill Strategy**: If a ghost is close but a **Green Pill** is nearby, <br>
  Pac-Man prioritizes eating the pill to attack.
* **Finding Food**: If it's safe, Pac-Man seeks the nearest pink dot <br>
  with the most "food neighbors".

<br>

---

## 💻 My Custom Game Server 
Beyond the algorithm, I implemented a full **Pac-Man Game Server** from scratch. <br>
The server manages the entire game logic independently:

* **Entity Management**: Handles the positions and states of Pac-Man and Ghosts.
* **Collision Engine**: Detects when Pac-Man eats food, pellets, or hits a ghost.
* **Game Rules**: Manages the scoring system, lives, and "Win/Loss" states.
* **Custom GUI**: A visual interface built with `StdDraw` that renders the <br>
  game state in real-time with custom images and animations.
* **Logic Validation**: Includes **JUnit tests** to verify the integrity of the <br>
  server's mechanics, such as movement constraints and score updates.

<br>

<p align="center">
  <img src="pacman_demo1.png" width="350" alt="Algorithm in action">
  <br>
  <i>Figure 1: My algorithm navigating the board (Professor's Game)</i>
</p>

<br>

<p align="center">
  <img src="pacman_demo2.png" width="350" alt="Server Gameplay">
  <br>
  <i>Figure 2: My custom game server running with full GUI and logic</i>
</p>

<br>

---

## 📁 Project Structure
* **`src/`**: The root folder for all Java source code, divided into:
    * **`assignments/`**: Core logic for the assignment, including **`Ex3Algo`** and **`Ex3Main`**.
    * **`Server/`**: Full implementation of the custom game server, including entities, GUI, and server logic.
* **`libs/`**: External libraries required for the project (e.g., `StdDraw`).
* **`Images/`**: Graphical assets (icons, sprites) used by the GUI to render the characters and map.
* **`Ex3_docs/`**: Detailed documentation of the Pac-Man smart algorithm and logic <br> implemented in Ex3Algo.java, including movement strategies and decision priorities.

<br>

---

## 🏃 How to Run

### 🎮 1. The Game (Algorithm Demo)
To run the original game (Level 4, DT 200) using my smart algorithm:
1. Open the folder **`Ex3_2`** from the submitted ZIP.
2. Double-click the **`Ex3_2.jar`** file.
3. **Tip:** Press **ENTER** during the game to switch between Auto and Manual mode.

### 🕹️ 2. My Custom Pac-Man Game (Full Server)
To run the full game server that I built:
1. Open the folder **`Ex3_3`** from the submitted ZIP.
2. Double-click the **`Ex3_3.jar`** file.
3. Use your keyboard to play and test the server logic.

</div>
