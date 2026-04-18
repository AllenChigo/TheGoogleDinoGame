import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator; // Required for safe removal during iteration
import java.util.Random;

public class DinoGame extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 800;
    private final int HEIGHT = 300;
    private final int GROUND_Y = 240;
    
    // Images
    private BufferedImage dinoImg;
    private BufferedImage cactusImg;
    private BufferedImage cloudImg;
    private BufferedImage ufoImg; // Represents planes/ufos/stars
    
    // Game State
    private int dinoY = 200, dinoVY = 0;
    private final int GRAVITY = 1;
    private boolean isJumping = false;
    private boolean gameOver = false;
    private int score = 0;
    
    // Entity Lists
    private ArrayList<Rectangle> obstacles = new ArrayList<>();
    private ArrayList<BackgroundEntity> bgItems = new ArrayList<>(); // NEW: Background objects
    
    // Timers & Logic
    private Timer timer;
    private Random random = new Random();
    private int obstacleTimer = 0;
    private int nextSpawnTime = 0;
    
    // Difficulty
    private final int BASE_MIN_DELAY = 40;
    private final int BASE_OBSTACLE_SPEED = 8;
    private final int DIFFICULTY_INCREASE_INTERVAL = 15;

    // NEW: Inner class to manage background objects
    class BackgroundEntity {
        int x, y, speed, width, height;
        BufferedImage img;

        public BackgroundEntity(int x, int y, int speed, BufferedImage img, int w, int h) {
            this.x = x;
            this.y = y;
            this.speed = speed; // Independent speed
            this.img = img;
            this.width = w;
            this.height = h;
        }
    }

    public DinoGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(240, 248, 255)); // AliceBlue (Sky color)
        setFocusable(true);
        addKeyListener(this);
        
        loadSprites();
        
        timer = new Timer(20, this); 
        timer.start();
        calculateNextSpawnTime();
    }
    
    private void loadSprites() {
        try {
            dinoImg = ImageIO.read(new File("dino.png"));
            cactusImg = ImageIO.read(new File("cactus.png"));
            
            // NEW: Load background assets
            // If you only have one, just copy/paste the file and rename it, 
            // or use the same image for both variables.
            cloudImg = ImageIO.read(new File("cloud.png"));
            ufoImg = ImageIO.read(new File("ufo.png")); 
            
        } catch (IOException e) {
            System.out.println("Error loading images. Ensure dino.png, cactus.png, cloud.png, and ufo.png exist.");
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
        
        // 1. Draw Background Entities FIRST (So they are behind everything)
        for (BackgroundEntity bg : bgItems) {
            if (bg.img != null) {
                g.drawImage(bg.img, bg.x, bg.y, bg.width, bg.height, null);
            } else {
                g.setColor(Color.LIGHT_GRAY); // Fallback placeholder
                g.fillOval(bg.x, bg.y, bg.width, bg.height / 2);
            }
        }

        // 2. Draw Ground
        g.setColor(Color.GRAY);
        g.drawLine(0, GROUND_Y, WIDTH, GROUND_Y);

        // 3. Draw Dino
        if (dinoImg != null) g.drawImage(dinoImg, 50, dinoY, 40, 40, null);
        else { g.setColor(Color.BLACK); g.fillRect(50, dinoY, 40, 40); }

        // 4. Draw Obstacles
        for (Rectangle rect : obstacles) {
            if (cactusImg != null) g.drawImage(cactusImg, rect.x, rect.y, rect.width, rect.height, null);
            else { g.setColor(Color.RED); g.fillRect(rect.x, rect.y, rect.width, rect.height); }
        }

        // UI
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Level: " + getDifficultyLevel(), 10, 40);
        
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("GAME OVER", 300, HEIGHT / 2 - 20);
            g.drawString("Press SPACE", 320, HEIGHT / 2 + 20);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // --- GAMEPLAY LOGIC ---
        dinoY += dinoVY;
        if (dinoY < (GROUND_Y - 40)) { dinoVY += GRAVITY; } 
        else { dinoY = (GROUND_Y - 40); dinoVY = 0; isJumping = false; }

        obstacleTimer++;
        if (obstacleTimer >= nextSpawnTime) {
            spawnRandomObstacle();
            calculateNextSpawnTime();
        }
        
        // --- NEW: BACKGROUND SPAWNING LOGIC ---
        // 2% chance per frame to spawn a cloud, 0.5% chance for a UFO
        if (random.nextInt(100) < 2) spawnBackgroundItem("cloud");
        if (random.nextInt(200) < 1) spawnBackgroundItem("ufo");

        // Move Obstacles
        int currentSpeed = getCurrentObstacleSpeed();
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle rect = obstacles.get(i);
            rect.x -= currentSpeed;
            
            if (rect.intersects(new Rectangle(50, dinoY, 30, 30))) { // Hitbox slightly smaller than sprite
                gameOver = true;
                timer.stop();
            }
            if (rect.x + rect.width < 0) {
                obstacles.remove(i);
                score++;
                i--;
            }
        }
        
        // --- NEW: MOVE BACKGROUND ITEMS ---
        // Using Iterator to safely remove items while looping
        Iterator<BackgroundEntity> bgIter = bgItems.iterator();
        while (bgIter.hasNext()) {
            BackgroundEntity bg = bgIter.next();
            bg.x -= bg.speed; // Move at their own speed
            if (bg.x + bg.width < -100) {
                bgIter.remove(); // Despawn when off-screen
            }
        }

        repaint();
    }
    
    private void spawnRandomObstacle() {
        int w = 20 + random.nextInt(30); 
        int h = 30 + random.nextInt(40);
        obstacles.add(new Rectangle(WIDTH, GROUND_Y - h, w, h));
    }
    
    /**
     * NEW: Spawns a background entity with random properties
     */
    private void spawnBackgroundItem(String type) {
        int y = random.nextInt(150); // Random height in the sky (0-150)
        int speed; 
        BufferedImage img;
        int width, height;

        if (type.equals("cloud")) {
            speed = 1 + random.nextInt(3); // Slow (1-3)
            img = cloudImg;
            width = 60 + random.nextInt(40); // Random size
            height = 30 + random.nextInt(20);
        } else { // UFO or Plane
            speed = 4 + random.nextInt(5); // Fast (4-8)
            img = ufoImg;
            width = 40;
            height = 30;
        }
        
        bgItems.add(new BackgroundEntity(WIDTH, y, speed, img, width, height));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                dinoY = 200;
                obstacles.clear();
                bgItems.clear(); // Clear background on restart
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
        JFrame frame = new JFrame("Dino Run: Parallax");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new DinoGame());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
