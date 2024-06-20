import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;

public class OnetGameWithHashMap extends JFrame implements ActionListener {
    int gridSize = 4; // Grid size of the game board
    HashMap<Point, Integer> board = new HashMap<>(); // HashMap to store the board state
    HashMap<Point, JButton> buttons = new HashMap<>(); // HashMap to store buttons on the board

    private ImageIcon[] icons; // Array to hold icons for the game
    private int score = 0; // Player's score
    private int level = 1; // Current game level
    private JLabel scoreLabel; // Label to display score
    private JLabel levelLabel; // Label to display level
    private JLabel timerLabel; // Label to display remaining time
    private Point firstClick = null; // Store the first clicked button
    private Timer timer; // Timer for the game
    private int timeRemaining = 60; // Remaining time for the current level
    private int buttonSize = 100; // Size of each button
    private JPanel gamePanel; // Panel to hold the game grid
    private Point matchStart, matchEnd; // Points to store matched buttons for drawing lines

    public OnetGameWithHashMap() {
        long startTime = System.nanoTime(); // Start measuring initialization time

        buttons = new HashMap<>();
        initializeIcons(); // Initialize icons for the game
        initializeBoard(); // Set up the board
        initializeUI(); // Set up the UI
        startTimer(); // Start the game timer
        long endTime = System.nanoTime(); // End measuring initialization time
        System.out.println("Total initialization time: " + (endTime - startTime) + " nanoseconds");
    }

    private void initializeIcons() {
        int numPairs = 18; // Number of pairs of icons
        icons = new ImageIcon[numPairs]; // Initialize the icons array

        for (int i = 0; i < numPairs; i++) {
            String imagePath = "/images/" + (i + 1) + ".png"; // Path to the icon image
            icons[i] = new ImageIcon(Objects.requireNonNull(getClass().getResource(imagePath))); // Load the icon

            if (icons[i].getImageLoadStatus() != MediaTracker.COMPLETE) {
                System.out.println("Failed to load image: " + imagePath); // Print error if image fails to load
            }
        }
    }

    private void initializeBoard() {
        board.clear();
        buttons.clear();
        ArrayList<Integer> positions = new ArrayList<>();
        int numPairs = (gridSize * gridSize) / 2; // Calculate the number of pairs needed

        // Add pairs of icons to the positions list
        for (int i = 0; i < numPairs; i++) {
            positions.add(i % icons.length);
            positions.add(i % icons.length); // Add each icon twice to create pairs
        }

        long sortingStartTime = System.nanoTime();
        Collections.shuffle(positions); // Shuffle the positions to randomize
        long sortingEndTime = System.nanoTime();
        System.out.println("Sorting (shuffling) time: " + (sortingEndTime - sortingStartTime) + " ns");

        long startTime = System.nanoTime(); // Start measuring board initialization time
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (!positions.isEmpty()) {
                    board.put(new Point(i, j), positions.remove(0)); // Fill the board with icon pairs
                } else {
                    board.put(new Point(i, j), -1); // Fill remaining cells with -1 if any
                }
            }
        }
        long endTime = System.nanoTime(); // End measuring board initialization time
        System.out.println("Time taken to initialize board: " + (endTime - startTime) + " ns");
    }

    private void initializeUI() {
        setTitle("Onet Game With Hashmap");
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Panel to hold the game grid
        gamePanel = new JPanel(new GridLayout(gridSize, gridSize)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (matchStart != null && matchEnd != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(2));
                    drawConnectionLine(g2d, matchStart, matchEnd); // Draw line between matched icons
                }
            }
        };

        // Initialize buttons for the game grid
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(buttonSize, buttonSize)); // Set button size
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setContentAreaFilled(false);
                button.setBorderPainted(true);

                ImageIcon icon = icons[board.get(new Point(i, j))]; // Get icon for the button
                button.setIcon(scaleImageIcon(icon, buttonSize, buttonSize)); // Scale the icon to fit button
                button.addActionListener(this);
                buttons.put(new Point(i, j), button); // Store button in buttons map
                gamePanel.add(button); // Add button to game panel
            }
        }

        // Panel to hold score, level, and timer information
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(200, getHeight()));

        scoreLabel = new JLabel("Score: " + score); // Initialize score label
        scoreLabel.setFont(new Font("Serif", Font.BOLD, 24));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        levelLabel = new JLabel("Level: " + level); // Initialize level label
        levelLabel.setFont(new Font("Serif", Font.BOLD, 24));
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerLabel = new JLabel("Time: " + timeRemaining); // Initialize timer label
        timerLabel.setFont(new Font("Serif", Font.BOLD, 24));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Shuffle button to reshuffle the board
        JButton shuffleButton = new JButton("Shuffle");
        shuffleButton.setFont(new Font("Serif", Font.BOLD, 24));
        shuffleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        shuffleButton.addActionListener(e -> shuffleBoard());

        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(scoreLabel);
        infoPanel.add(levelLabel);
        infoPanel.add(timerLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(shuffleButton);
        infoPanel.add(Box.createVerticalGlue());

        add(infoPanel, BorderLayout.EAST); // Add info panel to the right
        add(gamePanel, BorderLayout.CENTER); // Add game panel to the center

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true); // Make the frame visible
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton clickedButton = (JButton) e.getSource(); // Get the clicked button
        Point clickedPoint = getButtonPosition(clickedButton); // Get position of the clicked button

        if (clickedPoint == null) {
            return; // If clicked point is null, exit the method
        }

        if (firstClick == null) {
            firstClick = clickedPoint; // Store the first clicked point
        } else {
            // If two buttons are clicked, check if they form a match
            if (isMatch(firstClick, clickedPoint) && isConnectable(firstClick, clickedPoint)) {
                matchStart = firstClick;
                matchEnd = clickedPoint;
                gamePanel.repaint(); // Repaint the game panel to draw the connection line
                removeImages(firstClick, clickedPoint); // Remove matched icons from the board
                score += 10; // Update score
                scoreLabel.setText("Score: " + score);
                if (isBoardEmpty()) {
                    nextLevel(); // Move to the next level if the board is empty
                }
                // Timer task to clear the connection line after a short delay
                TimerTask clearLineTask = new TimerTask() {
                    public void run() {
                        matchStart = null;
                        matchEnd = null;
                        gamePanel.repaint();
                    }
                };
                new Timer().schedule(clearLineTask, 500); // Schedule the timer task
            }
            firstClick = null; // Reset first click
        }
    }

    // Method to get the position of a button in the grid
    private Point getButtonPosition(JButton button) {
        for (Point point : buttons.keySet()) {
            if (buttons.get(point) == button) {
                return point; // Return the point if the button matches
            }
        }
        return null; // Return null if the button is not found
    }

    // Method to check if two points on the board are a match
    private boolean isMatch(Point p1, Point p2) {
        long startTime = System.nanoTime(); // Start time for performance measurement

        boolean result = !p1.equals(p2) && board.get(p1).equals(board.get(p2)); // Check if points are different and have the same icon

        long endTime = System.nanoTime(); // End time for performance measurement
        System.out.println("Time taken to check match: " + (endTime - startTime) + " ns"); // Log the time taken
        return result; // Return the result
    }

    // Method to remove images from the board
    private void removeImages(Point p1, Point p2) {
        long startTime = System.nanoTime(); // Start time for performance measurement

        if (p1 != null && p2 != null) {
            buttons.get(p1).setVisible(false); // Hide the first button
            buttons.get(p2).setVisible(false); // Hide the second button
            board.put(p1, -1); // Mark the first point as empty
            board.put(p2, -1); // Mark the second point as empty
        }

        long endTime = System.nanoTime(); // End time for performance measurement
        System.out.println("Time taken to remove images: " + (endTime - startTime) + " ns"); // Log the time taken
    }

    // Method to check if the board is empty
    private boolean isBoardEmpty() {
        long startTime = System.nanoTime(); // Start time for performance measurement

        boolean result = true; // Assume the board is empty
        for (Point point : buttons.keySet()) {
            if (buttons.get(point).isVisible()) { // If any button is still visible
                result = false; // The board is not empty
                break; // Exit the loop early
            }
        }

        long endTime = System.nanoTime(); // End time for performance measurement
        System.out.println("Time taken to check if board is empty: " + (endTime - startTime) + " ns"); // Log the time taken
        return result; // Return the result
    }


    private void nextLevel() {
        gridSize += 2; // Increase the grid size by 2 for the next level
        if (gridSize % 2 != 0) {
            gridSize++; // Ensure the grid size is even
        }
        level++;
        levelLabel.setText("Level: " + level); // Update the level label

        // Adjust time based on level
        int baseTime = 60;
        int additionalTime = 30;
        timeRemaining = baseTime + (level - 1) * additionalTime; // Calculate new time remaining

        // Stop the previous timer
        if (timer != null) {
            timer.cancel(); // Cancel the existing timer
        }

        // Show dialog to notify the player of level completion
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

        long startTime = System.nanoTime(); // Start time for performance measurement

        // Initialize new board
        initializeBoard();
        gamePanel.removeAll(); // Remove all components from the game panel
        gamePanel.setLayout(new GridLayout(gridSize, gridSize)); // Set new grid layout

        // Update button size based on new grid size
        buttons = new HashMap<>();
        buttonSize = gamePanel.getWidth() / gridSize; // Calculate new button size
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(buttonSize, buttonSize)); // Set new button size
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setContentAreaFilled(false);
                button.setBorderPainted(true);

                Integer iconIndex = board.get(new Point(i, j)); // Get icon index for the button
                if (iconIndex != null && iconIndex != -1) {
                    ImageIcon icon = icons[iconIndex];
                    button.setIcon(scaleImageIcon(icon, buttonSize, buttonSize)); // Set icon for the button
                } else {
                    button.setVisible(false); // Hide the button if no icon
                }

                button.addActionListener(this); // Add action listener to the button
                buttons.put(new Point(i, j), button); // Add button to buttons map
                gamePanel.add(button); // Add button to game panel
            }
        }

        gamePanel.revalidate(); // Revalidate the game panel to reflect changes
        gamePanel.repaint(); // Repaint the game panel
        startTimer(); // Start the timer for the new level

        long endTime = System.nanoTime(); // End time for performance measurement
        System.out.println("Time taken to initialize board for new level: " + (endTime - startTime) + " ns"); // Log the time taken
    }

    // Method to start or restart the timer
    private void startTimer() {
        if (timer != null) {
            timer.cancel(); // Cancel existing timer if any
        }

        timer = new Timer(); // Create a new timer
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                timeRemaining--; // Decrement the remaining time
                timerLabel.setText("Time: " + timeRemaining); // Update the timer label
                if (timeRemaining <= 0) { // Check if time is up
                    timer.cancel(); // Cancel the timer
                    JOptionPane.showMessageDialog(OnetGameWithHashMap.this, "Time's up! Game Over."); // Show game over message
                    System.exit(0); // Exit the game
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1000, 1000); // Schedule the timer task to run every second
    }

    // Method to scale an image icon to the specified width and height
    private ImageIcon scaleImageIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage(); // Get the image from the icon
        Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); // Scale the image
        return new ImageIcon(scaledImage); // Return the scaled icon
    }

    // Method to find a path between two points using BFS (Breadth-First Search)
    private ArrayList<Point> findPath(Point start, Point end) {
        long startTime = System.nanoTime(); // Start time for performance measurement

        int[] dx = {1, -1, 0, 0}; // Directions for x-axis movement (right, left, down, up)
        int[] dy = {0, 0, 1, -1}; // Directions for y-axis movement (right, left, down, up)
        boolean[][] visited = new boolean[gridSize][gridSize]; // Array to keep track of visited points
        Point[][] prev = new Point[gridSize][gridSize]; // Array to store previous points in the path

        Queue<Point> queue = new LinkedList<>(); // Queue for BFS
        queue.add(start); // Add the start point to the queue
        visited[start.x][start.y] = true; // Mark the start point as visited

        while (!queue.isEmpty()) {
            Point current = queue.poll(); // Get the current point from the queue
            if (current.equals(end)) { // If we reached the end point
                long endTime = System.nanoTime(); // End time for performance measurement
                System.out.println("Time taken to find path(searching alogrithm): " + (endTime - startTime) + " ns"); // Log the time taken
                return reconstructPath(prev, end); // Reconstruct and return the path
            }
            for (int i = 0; i < 4; i++) { // Explore all four directions
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];
                if (nx >= 0 && nx < gridSize && ny >= 0 && ny < gridSize) { // Check if the next point is within the grid
                    Integer nextValue = board.get(new Point(nx, ny)); // Get the value at the next point
                    if (!visited[nx][ny] && (nextValue == null || nextValue == -1 || (nx == end.x && ny == end.y))) {
                        visited[nx][ny] = true; // Mark the next point as visited
                        prev[nx][ny] = current; // Set the previous point for the next point
                        queue.add(new Point(nx, ny)); // Add the next point to the queue
                    }
                }
            }
        }

        long endTime = System.nanoTime(); // End time for performance measurement
        System.out.println("Time taken to find path(searching algorithm): " + (endTime - startTime) + " ns"); // Log the time taken
        return new ArrayList<>(); // Return an empty path if no path is found
    }

    // Method to draw a connection line between two points
    private void drawConnectionLine(Graphics2D g2d, Point p1, Point p2) {
        ArrayList<Point> path = findPath(p1, p2); // Find the path between the two points
        for (int i = 0; i < path.size() - 1; i++) { // Iterate through the points in the path
            if (path.get(i).x >= 0 && path.get(i).x < gridSize &&
                    path.get(i).y >= 0 && path.get(i).y < gridSize &&
                    path.get(i + 1).x >= 0 && path.get(i + 1).x < gridSize &&
                    path.get(i + 1).y >= 0 && path.get(i + 1).y < gridSize) {
                JButton b1 = buttons.get(path.get(i)); // Get the first button
                JButton b2 = buttons.get(path.get(i + 1)); // Get the second button
                Rectangle r1 = b1.getBounds(); // Get the bounds of the first button
                Rectangle r2 = b2.getBounds(); // Get the bounds of the second button
                Point start = new Point(r1.x + r1.width / 2, r1.y + r1.height / 2); // Calculate the center of the first button
                Point end = new Point(r2.x + r2.width / 2, r2.y + r2.height / 2); // Calculate the center of the second button
                g2d.drawLine(start.x, start.y, end.x, end.y); // Draw a line between the centers of the two buttons
            }
        }
    }

    // Method to check if two points are connectable
    private boolean isConnectable(Point p1, Point p2) {
        long startTime = System.nanoTime(); // Start time for performance measurement

        boolean result = !findPath(p1, p2).isEmpty(); // Check if a path exists between the points

        long endTime = System.nanoTime(); // End time for performance measurement
        System.out.println("Time taken to check connectability (searching algorithm): " + (endTime - startTime) + " ns"); // Log the time taken

        return result; // Return the result
    }


    // Method to reconstruct the path from the previous points array
    private ArrayList<Point> reconstructPath(Point[][] prev, Point end) {
        ArrayList<Point> path = new ArrayList<>(); // List to store the path
        for (Point at = end; at != null; at = prev[at.x][at.y]) { // Trace back from the end point to the start point
            path.add(at); // Add each point to the path
        }
        Collections.reverse(path); // Reverse the path to get the correct order
        return path; // Return the reconstructed path
    }

    // Method to shuffle the board
    private void shuffleBoard() {
        long startTime = System.nanoTime(); // Start time for performance measurement

        // Collect visible icons
        long collectIconsStart = System.nanoTime();
        ArrayList<Integer> visibleIcons = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Point point = new Point(i, j);
                Integer value = board.get(point);
                if (value != null && value != -1) {
                    visibleIcons.add(value); // Add visible icon to the list
                }
            }
        }
        long collectIconsEnd = System.nanoTime();
        System.out.println("Time taken to collect visible icons: " + (collectIconsEnd - collectIconsStart) + " ns");

        // Shuffle icons
        long shuffleStart = System.nanoTime();
        Collections.shuffle(visibleIcons); // Shuffle the list of visible icons
        long shuffleEnd = System.nanoTime();
        System.out.println("Time taken to shuffle icons: " + (shuffleEnd - shuffleStart) + " ns");


        // Place icons back on the board
        long placeIconsStart = System.nanoTime();
        Iterator<Integer> iterator = visibleIcons.iterator();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Point point = new Point(i, j);
                if (board.get(point) != null && board.get(point) != -1 && iterator.hasNext()) {
                    board.put(point, iterator.next()); // Place shuffled icon back on the board
                }
            }
        }
        long placeIconsEnd = System.nanoTime();
        System.out.println("Time taken to place icons back on board: " + (placeIconsEnd - placeIconsStart) + " ns");

        // Update buttons with new icons
        long updateButtonsStart = System.nanoTime();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Point point = new Point(i, j);
                JButton button = buttons.get(point);
                if (button != null) {
                    Integer value = board.get(point);
                    if (value != null && value != -1) {
                        ImageIcon icon = icons[value];
                        button.setIcon(scaleImageIcon(icon, buttonSize, buttonSize)); // Ensure image scaling
                        button.setVisible(true); // Make button visible
                        button.setBorderPainted(true); // Ensure button border is painted
                    } else {
                        button.setVisible(false); // Hide button if no icon
                    }
                }
            }
        }
        gamePanel.revalidate(); // Revalidate the game panel to apply changes
        gamePanel.repaint(); // Repaint the game panel to reflect changes
        long updateButtonsEnd = System.nanoTime();
        System.out.println("Time taken to update buttons: " + (updateButtonsEnd - updateButtonsStart) + " ns");

        long endTime = System.nanoTime(); // End time for performance measurement
        System.out.println("Total time taken to shuffle board: " + (endTime - startTime) + " ns"); // Log total time taken
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OnetGameWithHashMap::new); // Run the game on the Event Dispatch Thread
    }
}