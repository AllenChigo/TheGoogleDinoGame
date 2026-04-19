import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage; // NEW: For handling images
import java.io.File;                 // NEW: For reading files
import java.io.IOException;          // NEW: For error handling
import javax.imageio.ImageIO;        // NEW: For loading images
import java.util.ArrayList;
import java.util.Random;

public class DinoGame extends JPanel implements ActionListener, KeyListener {
    // Game dimensions
    private final int WIDTH = 800;
    private final int HEIGHT = 300;
    private final int GROUND_Y = 240;
    
    // Images
    private BufferedImage dinoImg;
    private BufferedImage cactusImg;
    
    // Dino properties
    private int dinoY = 200, dinoVY = 0;
    private final int GRAVITY = 1;
    private boolean isJumping = false;

    // Obstacles
    private ArrayList<Rectangle> obstacles = new ArrayList<>();
    private int obstacleTimer = 0;
    private int nextSpawnTime = 0; 
    private int score = 0;
    private boolean gameOver = false;
    private Timer timer;
    private Random random = new Random();
    
    // Difficulty Settings
    private final int BASE_MIN_DELAY = 40;
    private final int BASE_OBSTACLE_SPEED = 8;
    private final int DIFFICULTY_INCREASE_INTERVAL = 15;

    public DinoGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        
        loadSprites(); // Load images before starting
        
        timer = new Timer(20, this); 
        timer.start();
        calculateNextSpawnTime();
    }
    
    /**
     * NEW: Load images from the project folder.
     * If images are missing, it prints an error but the game still runs (invisible sprites).
     */
    private void loadSprites() {
        try {
            // Ensure these files exist in your project root!
            dinoImg = ImageIO.read(new File("dino.png"));
            cactusImg = ImageIO.read(new File("cactus.png"));
        } catch (IOException e) {
            System.out.println("Error loading images: " + e.getMessage());
            System.out.println("Make sure 'dino.png' and 'cactus.png' are in the project folder.");
        }
    }

    private void calculateNextSpawnTime() {
        int difficulty = getDifficultyLevel();
        int minDelay = Math.max(20, BASE_MIN_DELAY - (difficulty * 2));
        int randomVariance = 30 + (difficulty * 5);
        nextSpawnTime = obstacleTimer + minDelay + random.nextInt(randomVariance);
    }
    
    private int getCurrentObstacleSpeed() {
        return Math.min(15, BASE_OBSTACLE_SPEED + (score / DIFFICULTY_INCREASE_INTERVAL));
    }
    
    private int getDifficultyLevel() {
        return (score / DIFFICULTY_INCREASE_INTERVAL) + 1;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw Ground
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(0, GROUND_Y, WIDTH, GROUND_Y);

        // NEW: Draw Dino Image
        if (dinoImg != null) {
            // Draw image at dino coordinates
            g.drawImage(dinoImg, 50, dinoY, 40, 40, null); 
        } else {
            // Fallback if image fails to load
            g.setColor(Color.BLACK);
            g.fillRect(50, dinoY, 40, 40);
        }

        // NEW: Draw Cactus Images
        for (Rectangle rect : obstacles) {
            if (cactusImg != null) {
                // Draw image scaled to the obstacle's random width/height
                g.drawImage(cactusImg, rect.x, rect.y, rect.width, rect.height, null);
            } else {
                // Fallback
                g.setColor(Color.RED);
                g.fillRect(rect.x, rect.y, rect.width, rect.height);
            }
        }

        // UI
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Level: " + getDifficultyLevel(), 10, 40);
        
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("GAME OVER", 300, HEIGHT / 2 - 20);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Press SPACE to Restart", 300, HEIGHT / 2 + 10);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // Dino Physics
        dinoY += dinoVY;
        if (dinoY < (GROUND_Y - 40)) { 
            dinoVY += GRAVITY;
        } else {
            dinoY = (GROUND_Y - 40);
            dinoVY = 0;
            isJumping = false;
        }

        // Spawn Logic
        obstacleTimer++;
        if (obstacleTimer >= nextSpawnTime) {
            spawnRandomObstacle();
            calculateNextSpawnTime();
        }

        // Move Obstacles
        int currentSpeed = getCurrentObstacleSpeed();
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle rect = obstacles.get(i);
            rect.x -= currentSpeed;

            if (rect.intersects(new Rectangle(50, dinoY, 40, 40))) {
                gameOver = true;
                timer.stop();
            }

            if (rect.x + rect.width < 0) {
                obstacles.remove(i);
                score++;
                i--;
            }
        }
        repaint();
    }
    
    private void spawnRandomObstacle() {
        // Random Dimensions create "various sizes" of cactus
        int w = 20 + random.nextInt(30); 
        int h = 30 + random.nextInt(40);
        obstacles.add(new Rectangle(WIDTH, GROUND_Y - h, w, h));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                dinoY = 200;
                obstacles.clear();
                score = 0;
                gameOver = false;
                obstacleTimer = 0;
                calculateNextSpawnTime();
                timer.start();
            } else if (!isJumping) {
                dinoVY = -16; 
                isJumping = true;
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Dino Run: Sprite Edition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new DinoGame());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
