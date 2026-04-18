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

    public DinoGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(20, this); // ~50 FPS
        timer.start();
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
        g.drawString("Score: " + score, 10, 20);
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

        // Spawn Obstacles
        obstacleTimer++;
        if (obstacleTimer > 50 + new Random().nextInt(50)) {
            obstacles.add(new Rectangle(WIDTH, 200, 20, 40));
            obstacleTimer = 0;
        }

        // Move Obstacles & Check Collision
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle rect = obstacles.get(i);
            rect.x -= 8; // Speed

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
