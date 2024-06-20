import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;

public class OnetGame extends JFrame implements ActionListener {
    int gridSize = 4; // Initial grid size (4x4)
    int[][] board = new int[gridSize][gridSize]; // The game board
    JButton[][] buttons = new JButton[gridSize][gridSize]; // Buttons for each cell in the grid
    private ImageIcon[] icons; // Array to hold the icons for the game
    private int score = 0; // Player's score
    private int level = 1; // Current level
    private JLabel scoreLabel; // Label to display the score
    private JLabel levelLabel; // Label to display the level
    private JLabel timerLabel; // Label to display the remaining time
    private Point firstClick = null; // To store the first clicked point
    private Timer timer; // Timer for the game
    private int timeRemaining = 60; // Time remaining for the level (60 seconds)
    private JPanel gamePanel; // Panel for the game grid
    private Point matchStart, matchEnd; // Start and end points for drawing lines

    public OnetGame() {
        // Measure initialization time
        long startTime = System.nanoTime();
        initializeIcons(); // Load icons
        initializeBoard(); // Setup the game board
        initializeUI(); // Initialize user interface
        startTimer(); // Start the timer for the game
        long endTime = System.nanoTime();
        System.out.println("Total initialization time: " + (endTime - startTime) + " ns");
    }

    // Load icons for the game
    private void initializeIcons() {
        int numPairs = 18; // Number of image pairs
        icons = new ImageIcon[numPairs]; // Create an array for icons

        for (int i = 0; i < numPairs; i++) {
            // Load images using getResource from the images folder within src
            String imagePath = "/images/" + (i + 1) + ".png";
            icons[i] = new ImageIcon(Objects.requireNonNull(getClass().getResource(imagePath)));

            // Check if the image was loaded successfully
            if (icons[i].getImageLoadStatus() != MediaTracker.COMPLETE) {
                System.out.println("Failed to load image: " + imagePath);
            }
        }
    }

    // Initialize the game board with icons
    private void initializeBoard() {
        // Measure initialization time
        long startTime = System.nanoTime();

        board = new int[gridSize][gridSize]; // Initialize the board array
        buttons = new JButton[gridSize][gridSize]; // Initialize the buttons array
        ArrayList<Integer> positions = new ArrayList<>(); // List to hold icon positions

        int numPairs = (gridSize * gridSize) / 2; // Number of pairs in the grid

        for (int i = 0; i < numPairs; i++) {
            positions.add(i % icons.length); // Add the same icon twice to make pairs
            positions.add(i % icons.length); // Add twice for pairs
        }

        // Measure sorting time (shuffling positions)
        long sortingStartTime = System.nanoTime();
        Collections.shuffle(positions); // Shuffle the positions to randomize
        long sortingEndTime = System.nanoTime();
        System.out.println("Sorting (shuffling) time: " + (sortingEndTime - sortingStartTime) + " ns");

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (!positions.isEmpty()) {
                    board[i][j] = positions.remove(0); // Assign a random icon to each cell
                }
            }
        }
        long endTime = System.nanoTime();
        System.out.println("Board initialization time: " + (endTime - startTime) + " ns");
    }

    // Initialize the user interface
    private void initializeUI() {
        setTitle("Onet Game"); // Set the title of the window
        setSize(800, 600); // Set the size of the window
        setLayout(new BorderLayout()); // Set the layout to BorderLayout

        gamePanel = new JPanel(new GridLayout(gridSize, gridSize)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw a connection line if there is a match
                if (matchStart != null && matchEnd != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(2));
                    drawConnectionLine(g2d, matchStart, matchEnd);
                }
            }
        };

        int buttonSize = 100; // Size of each button
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(buttonSize, buttonSize)); // Set button size
                buttons[i][j].setMargin(new Insets(0, 0, 0, 0)); // Remove margin to fit the image inside the box
                buttons[i][j].setContentAreaFilled(false); // Remove content area
                buttons[i][j].setBorderPainted(true);

                // Get icon and adjust its size before setting to the button
                ImageIcon icon = icons[board[i][j]];
                buttons[i][j].setIcon(scaleImageIcon(icon, buttonSize, buttonSize)); // Set image to JButton with adjusted size
                buttons[i][j].addActionListener(this); // Add action listener to button
                gamePanel.add(buttons[i][j]); // Add button to the game panel
            }
        }

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS)); // Set layout to BoxLayout
        infoPanel.setPreferredSize(new Dimension(200, getHeight())); // Set preferred size

        scoreLabel = new JLabel("Score: " + score); // Initialize score label
        scoreLabel.setFont(new Font("Serif", Font.BOLD, 24)); // Set font
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align
        levelLabel = new JLabel("Level: " + level); // Initialize level label
        levelLabel.setFont(new Font("Serif", Font.BOLD, 24)); // Set font
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align
        timerLabel = new JLabel("Time: " + timeRemaining); // Initialize timer label
        timerLabel.setFont(new Font("Serif", Font.BOLD, 24)); // Set font
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align

        // Button for shuffling the board
        JButton shuffleButton = new JButton("Shuffle");
        shuffleButton.setFont(new Font("Serif", Font.BOLD, 24)); // Set font
        shuffleButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align
        shuffleButton.addActionListener(e -> shuffleBoard()); // Add action listener to shuffle button

        // Add components to the info panel
        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(scoreLabel);
        infoPanel.add(levelLabel);
        infoPanel.add(timerLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(shuffleButton);
        infoPanel.add(Box.createVerticalGlue());

        add(infoPanel, BorderLayout.EAST); // Add info panel to the right
        add(gamePanel, BorderLayout.CENTER); // Add game panel to the center

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set default close operation
        setVisible(true); // Make the window visible
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle button clicks
        JButton clickedButton = (JButton) e.getSource();
        Point clickedPoint = getButtonPosition(clickedButton);

        if (clickedPoint == null) {
            return; // Return if clicked point is null
        }

        if (firstClick == null) {
            firstClick = clickedPoint; // Set first click
        } else {
            // Check if the two clicks form a matching pair and are connectable
            if (isMatch(firstClick, clickedPoint) && isConnectable(firstClick, clickedPoint)) {
                matchStart = firstClick;
                matchEnd = clickedPoint;
                gamePanel.repaint(); // Redraw to display the line
                removeImages(firstClick, clickedPoint); // Remove matched images
                score += 10; // Increment score
                scoreLabel.setText("Score: " + score); // Update score label
                if (isBoardEmpty()) {
                    nextLevel(); // Proceed to the next level if the board is empty
                }
                // Task to clear the line after 0.5 seconds
                TimerTask clearLineTask = new TimerTask() {
                    public void run() {
                        matchStart = null;
                        matchEnd = null;
                        gamePanel.repaint(); // Redraw to remove the line
                    }
                };
                new Timer().schedule(clearLineTask, 500); // Remove line after 0.5 seconds
            }
            firstClick = null; // Reset first click
        }
    }


    private Point getButtonPosition(JButton button) {
        // Iterate through each row of the grid
        for (int i = 0; i < gridSize; i++) {
            // Iterate through each column of the grid
            for (int j = 0; j < gridSize; j++) {
                // Check if the current button in the grid matches the specified button
                if (buttons[i][j] == button) {
                    // Return the position as a Point object
                    return new Point(i, j);
                }
            }
        }
        // Return null if the button is not found in the grid
        return null;
    }

    private boolean isMatch(Point p1, Point p2) {
        long startTime = System.nanoTime(); // Start timing the match check

        // Check if the points are the same; if so, they can't be a match
        if (p1.equals(p2)) {
            return false;
        }

        // Check if the images at the two points match
        boolean result = board[p1.x][p1.y] == board[p2.x][p2.y];

        long endTime = System.nanoTime(); // End timing the match check
        System.out.println("Match checking (searching) time: " + (endTime - startTime) + " ns");

        return result;
    }

    private void removeImages(Point p1, Point p2) {
        if (p1 != null && p2 != null) {
            long startTime = System.nanoTime(); // Start timing the removal

            // Make the buttons at the specified points invisible
            buttons[p1.x][p1.y].setVisible(false);
            buttons[p2.x][p2.y].setVisible(false);

            // Mark the board positions as empty
            board[p1.x][p1.y] = -1;
            board[p2.x][p2.y] = -1;

            long endTime = System.nanoTime(); // End timing the removal
            System.out.println("Time to remove images: " + (endTime - startTime) + " ns");
        }
    }

    private boolean isBoardEmpty() {
        long startTime = System.nanoTime(); // Start timing the empty board check

        // Iterate through all buttons to check their visibility
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (buttons[i][j].isVisible()) {
                    return false; // If any button is visible, the board is not empty
                }
            }
        }

        long endTime = System.nanoTime(); // End timing the empty board check
        System.out.println("Empty board check time: " + (endTime - startTime) + " ns");

        return true; // All buttons are invisible, the board is empty
    }


    private void nextLevel() {
        gridSize += 2; // Increase the grid size by 2 for the next level
        if (gridSize % 2 != 0) {
            gridSize++; // Ensure the grid size is even
        }
        level++; // Increment the level
        levelLabel.setText("Level: " + level); // Update the level label

        // Update the time remaining for the next level
        int baseTime = 60; // Base time for the first level
        int additionalTime = 30; // Additional time for each subsequent level
        timeRemaining = baseTime + (level - 1) * additionalTime;

        // Show a congratulatory dialog
        timer.cancel(); // Stop the current timer
        int result = JOptionPane.showOptionDialog(this,
                "Congratulations, you have completed this level!",
                "Level Completed",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Proceed to the Next Level"},
                "Proceed to the Next Level");

        // Exit the game if the dialog is closed
        if (result == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }

        // Reinitialize the board for the next level
        initializeBoard();
        gamePanel.removeAll();
        gamePanel.setLayout(new GridLayout(gridSize, gridSize)); // Set layout again

        // Reinitialize buttons array with new grid size
        buttons = new JButton[gridSize][gridSize];
        int buttonSize = 100; // JButton size
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(buttonSize, buttonSize)); // Set JButton size
                buttons[i][j].setMargin(new Insets(0, 0, 0, 0)); // Remove margin to fit the image inside the box
                buttons[i][j].setContentAreaFilled(false); // Remove content area
                buttons[i][j].setBorderPainted(true);

                // Get icon and adjust its size before setting to the button
                ImageIcon icon = icons[board[i][j]];
                buttons[i][j].setIcon(scaleImageIcon(icon, buttonSize, buttonSize)); // Set image to JButton with adjusted size
                buttons[i][j].addActionListener(this);
                gamePanel.add(buttons[i][j]);
            }
        }

        gamePanel.revalidate();
        gamePanel.repaint();
        startTimer(); // Start the timer for the new level
    }


    private void startTimer() {
        timerLabel.setText("Time: " + timeRemaining); // Update the timer label with the remaining time
        timer = new Timer(); // Initialize a new timer
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeRemaining--; // Decrease the remaining time by 1 second
                timerLabel.setText("Time: " + timeRemaining); // Update the timer label

                // Check if the time has run out
                if (timeRemaining <= 0) {
                    timer.cancel(); // Stop the timer
                    // Show a game over message and exit the game
                    JOptionPane.showMessageDialog(OnetGame.this, "Time's up! Game over.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            }
        }, 1000, 1000); // Schedule the task to run every second
    }


    // Logic for shuffling the game board
    private void shuffleBoard() {
        long startTime = System.nanoTime(); // Start timing the board shuffling

        ArrayList<Integer> visibleIcons = new ArrayList<>();

        // Collect all visible icons
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (board[i][j] != -1) {
                    visibleIcons.add(board[i][j]);
                }
            }
        }

        // Shuffle positions of visible icons
        Collections.shuffle(visibleIcons);

        // Assign shuffled icons back to the game board
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (board[i][j] != -1) {
                    board[i][j] = visibleIcons.remove(0);
                }
            }
        }

        // Update button view with the shuffled icons
        updateButtonIcons();

        long endTime = System.nanoTime(); // End timing the board shuffling
        System.out.println("Board shuffling time: " + (endTime - startTime) + " ns");
    }

    // Update button icons with correct size
    private void updateButtonIcons() {
        int buttonSize = 100; // JButton size
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (board[i][j] != -1) {
                    ImageIcon icon = icons[board[i][j]];
                    buttons[i][j].setIcon(scaleImageIcon(icon, buttonSize, buttonSize)); // Set image to JButton with adjusted size
                    buttons[i][j].setVisible(true); // Make button visible
                    buttons[i][j].setBorderPainted(true); // Enable border painting
                } else {
                    buttons[i][j].setVisible(false); // Make button invisible if no icon is assigned
                }
            }
        }
    }

    // Additional function to adjust ImageIcon size with the button
    private ImageIcon scaleImageIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage(); // Get the original image
        Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); // Scale the image
        return new ImageIcon(scaledImage); // Return the scaled image as an ImageIcon
    }



    private ArrayList<Point> findPath(Point start, Point end) {
        long startTime = System.nanoTime(); // Start timing the path finding

        int[] dx = {1, -1, 0, 0}; // Possible x-direction movements
        int[] dy = {0, 0, 1, -1}; // Possible y-direction movements
        int gridSize = board.length; // Get the size of the grid
        boolean[][] visited = new boolean[gridSize][gridSize]; // Visited array to keep track of visited points
        Point[][] prev = new Point[gridSize][gridSize]; // Array to store previous points for path reconstruction

        Queue<Point> queue = new LinkedList<>(); // Queue for BFS
        queue.add(start); // Add starting point to the queue
        visited[start.x][start.y] = true; // Mark starting point as visited

        // BFS loop
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(end)) { // If the end point is reached
                long endTime = System.nanoTime(); // End timing the path finding
                System.out.println("Path finding time: " + (endTime - startTime) + " ns");
                return reconstructPath(prev, end); // Reconstruct and return the path
            }

            // Explore all possible movements
            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];

                // Ensure the new position is within the grid and not visited
                if (nx >= 0 && nx < gridSize && ny >= 0 && ny < gridSize) {
                    if (!visited[nx][ny] && (board[nx][ny] == -1 || (nx == end.x && ny == end.y))) {
                        visited[nx][ny] = true; // Mark the new position as visited
                        prev[nx][ny] = current; // Set the current point as the previous point for the new position
                        queue.add(new Point(nx, ny)); // Add the new position to the queue
                    }
                }
            }
        }

        long endTime = System.nanoTime(); // End timing the path finding
        System.out.println("Path finding time: " + (endTime - startTime) + " ns");
        return new ArrayList<>(); // Return an empty list if no path is found
    }


    private void drawConnectionLine(Graphics2D g2d, Point p1, Point p2) {
        // Find the path between the two points
        ArrayList<Point> path = findPath(p1, p2);

        // Iterate through each segment of the path
        for (int i = 0; i < path.size() - 1; i++) {
            // Ensure points are within the grid bounds
            if (path.get(i).x >= 0 && path.get(i).x < gridSize &&
                    path.get(i).y >= 0 && path.get(i).y < gridSize &&
                    path.get(i + 1).x >= 0 && path.get(i + 1).x < gridSize &&
                    path.get(i + 1).y >= 0 && path.get(i + 1).y < gridSize) {

                // Get buttons at the current and next points in the path
                JButton b1 = buttons[path.get(i).x][path.get(i).y];
                JButton b2 = buttons[path.get(i + 1).x][path.get(i + 1).y];

                // Get the bounds of the buttons
                Rectangle r1 = b1.getBounds();
                Rectangle r2 = b2.getBounds();

                // Calculate the center points of the buttons
                Point start = new Point(r1.x + r1.width / 2, r1.y + r1.height / 2);
                Point end = new Point(r2.x + r2.width / 2, r2.y + r2.height / 2);

                // Draw a line between the center points
                g2d.drawLine(start.x, start.y, end.x, end.y);
            }
        }
    }

    // Logic to check if two points can be connected
    private boolean isConnectable(Point p1, Point p2) {
        long startTime = System.nanoTime();

        // Check if there's a path between the points
        boolean result = !findPath(p1, p2).isEmpty();

        long endTime = System.nanoTime();

        // Output the time taken to check connectivity
        System.out.println("Connectivity checking time: " + (endTime - startTime) + " ns");

        return result;
    }


    // Rebuild path from end point to start point
    private ArrayList<Point> reconstructPath(Point[][] prev, Point end) {
        ArrayList<Point> path = new ArrayList<>();

        // Traverse from the end point back to the start point
        for (Point at = end; at != null; at = prev[at.x][at.y]) {
            path.add(at);
        }

        // Reverse the path to get it from start to end
        Collections.reverse(path);

        return path;
    }

    public static void main(String[] args) {
        // Launch the game in the Event Dispatch Thread
        SwingUtilities.invokeLater(OnetGame::new);
    }
}