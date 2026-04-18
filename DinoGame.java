import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class DinoGame extends JPanel implements ActionListener, KeyListener {
    // Game dimensions
    private final int WIDTH = 800;
    private final int HEIGHT = 300;
    
    // Dino properties
    private int dinoY = 200, dinoVY = 0;
    private final int GRAVITY = 1;
    private boolean isJumping = false;

    // Obstacles
    private ArrayList<Rectangle> obstacles = new ArrayList<>();
    private int obstacleTimer = 0;
    private int score = 0;
    private boolean gameOver = false;
    private Timer timer;
    
    // Difficulty Settings
    private final int BASE_SPAWN_DELAY = 50;
    private final int BASE_SPAWN_VARIATION = 50;
    private final int BASE_OBSTACLE_SPEED = 8;
    private final int DIFFICULTY_INCREASE_INTERVAL = 10; // Every 10 points

    public DinoGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(20, this); // ~50 FPS
        timer.start();
    }
    
    /**
     * Calculate current obstacle speed based on score
     * Speed increases by 1 every DIFFICULTY_INCREASE_INTERVAL points
     */
    private int getCurrentObstacleSpeed() {
        return BASE_OBSTACLE_SPEED + (score / DIFFICULTY_INCREASE_INTERVAL);
    }
    
    /**
     * Calculate current spawn delay based on score
     * Spawn rate increases (delay decreases) every DIFFICULTY_INCREASE_INTERVAL points
     */
    private int getCurrentSpawnDelay() {
        int delay = BASE_SPAWN_DELAY - (score / DIFFICULTY_INCREASE_INTERVAL) * 3;
        return Math.max(delay, 20); // Minimum delay to prevent overwhelming
    }
    
    /**
     * Get current difficulty level (for display)
     */
    private int getDifficultyLevel() {
        return (score / DIFFICULTY_INCREASE_INTERVAL) + 1;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw Ground
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(0, 240, WIDTH, 240);

        // Draw Dino (Simple Rect)
        g.setColor(Color.DARK_GRAY);
        g.fillRect(50, dinoY, 40, 40);

        // Draw Obstacles
        g.setColor(Color.RED);
        for (Rectangle rect : obstacles) {
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }

        // UI
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Difficulty: " + getDifficultyLevel(), 10, 40);
        g.drawString("Speed: " + getCurrentObstacleSpeed(), 10, 60);
        
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("GAME OVER! Press Space to Restart", 150, HEIGHT / 2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // Apply Gravity
        dinoY += dinoVY;
        if (dinoY < 200) {
            dinoVY += GRAVITY;
        } else {
            dinoY = 200;
            dinoVY = 0;
            isJumping = false;
        }

        // Spawn Obstacles with difficulty scaling
        obstacleTimer++;
        int spawnDelay = getCurrentSpawnDelay();
        int spawnVariation = Math.max(BASE_SPAWN_VARIATION - (score / DIFFICULTY_INCREASE_INTERVAL) * 5, 10);
        
        if (obstacleTimer > spawnDelay + new Random().nextInt(spawnVariation)) {
            obstacles.add(new Rectangle(WIDTH, 200, 20, 40));
            obstacleTimer = 0;
        }

        // Move Obstacles & Check Collision
        int currentSpeed = getCurrentObstacleSpeed();
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle rect = obstacles.get(i);
            rect.x -= currentSpeed; // Dynamic speed based on difficulty

            if (rect.intersects(new Rectangle(50, dinoY, 40, 40))) {
                gameOver = true;
                timer.stop();
            }

            if (rect.x + rect.width < 0) {
                obstacles.remove(i);
                score++;
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                // Restart logic
                dinoY = 200;
                obstacles.clear();
                score = 0;
                gameOver = false;
                obstacleTimer = 0;
                timer.start();
            } else if (!isJumping) {
                dinoVY = -15; // Jump Force
                isJumping = true;
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Dino Run Java");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new DinoGame());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}