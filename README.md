<H1> Data Structure Final Project - Matching Game (Onet) </H1></br>

<H1>Overview</h1>
This project implements the Onet game, a tile-matching puzzle game where players connect pairs of identical tiles on a board. The implementation compares two different data structures—2D array and HashMap—to manage the game board and perform various operations efficiently.

<H1>Features</h1></br>

2D Array Implementation:

Represents the game board using a fixed-size 2D array.
Supports operations such as initialization, shuffling, match checking, pathfinding, connectivity checking, removing images, and checking if the board is empty.
Suitable for games with a static grid size and where direct index-based access is beneficial.
HashMap Implementation:

Uses a HashMap to dynamically manage the game board.
Enables efficient insertion, deletion, and lookup operations based on key-value pairs.
Particularly useful for games where the grid size may change dynamically or where frequent updates are required.
Performance Evaluation
The project evaluates and compares the performance of the 2D array and HashMap implementations for various game operations:

Initialization: Setting up the game board.
Shuffling: Randomizing the positions of tiles on the board.
Match Checking: Identifying pairs of adjacent or connectable tiles.
Pathfinding: Finding valid paths between tiles on the board.
Connectivity Checking: Determining if there is a path between two given tiles.
Removing Images: Handling the removal of matched tiles and updating the board.
Empty Board Check: Verifying if the game board is empty.

 <H1>Methodology</h1>
Implementation: The game logic and data structures (2D array and HashMap) are implemented in Java.
Measurement: Execution times (in nanoseconds) for each operation are measured using performance profiling techniques.
Analysis: The performance metrics are analyzed to determine the suitability of each data structure for the Onet game.

<H1>Conclusion </h1>
The project provides insights into the practical application of data structures in game development. Developers can use the findings to choose the most suitable data structure based on performance requirements and game dynamics. The comparison between the 2D array and HashMap implementations highlights their respective strengths and limitations in managing board games like Onet.
