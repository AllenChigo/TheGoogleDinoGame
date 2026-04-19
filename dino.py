import pygame
import random

# Initialize Pygame
pygame.init()

# Game Dimensions
WIDTH, HEIGHT = 800, 300
GROUND_Y = 240
FPS = 50

# Colors
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)
RED = (255, 0, 0)
LIGHT_GRAY = (200, 200, 200)

# Difficulty Settings
BASE_MIN_DELAY = 40
BASE_OBSTACLE_SPEED = 8
DIFFICULTY_INTERVAL = 15

screen = pygame.display.set_mode((WIDTH, HEIGHT))
pygame.display.set_caption("Dino Run: Python Edition")
clock = pygame.time.Clock()
font_small = pygame.font.SysFont("Arial", 14, bold=True)
font_large = pygame.font.SysFont("Arial", 30, bold=True)

# Load Sprites
try:
    dino_img = pygame.image.load("dino.png")
    dino_img = pygame.transform.scale(dino_img, (40, 40))
    cactus_img = pygame.image.load("cactus.png")
except:
    print("Images not found. Using colored rectangles as fallback.")
    dino_img = None
    cactus_img = None

class Game:
    def __init__(self):
        self.reset()

    def reset(self):
        self.dino_y = GROUND_Y - 40
        self.dino_vy = 0
        self.is_jumping = False
        self.obstacles = []
        self.obstacle_timer = 0
        self.score = 0
        self.game_over = False
        self.calculate_next_spawn()

    def calculate_next_spawn(self):
        difficulty = (self.score // DIFFICULTY_INTERVAL) + 1
        min_delay = max(20, BASE_MIN_DELAY - (difficulty * 2))
        variance = 30 + (difficulty * 5)
        self.next_spawn_time = self.obstacle_timer + min_delay + random.randint(0, variance)

    def spawn_obstacle(self):
        w = random.randint(20, 50)
        h = random.randint(30, 70)
        rect = pygame.Rect(WIDTH, GROUND_Y - h, w, h)
        self.obstacles.append(rect)

    def update(self):
        if self.game_over:
            return

        # Dino Physics
        self.dino_y += self.dino_vy
        if self.dino_y < GROUND_Y - 40:
            self.dino_vy += 1  # Gravity
        else:
            self.dino_y = GROUND_Y - 40
            self.dino_vy = 0
            self.is_jumping = False

        # Spawn Logic
        self.obstacle_timer += 1
        if self.obstacle_timer >= self.next_spawn_time:
            self.spawn_obstacle()
            self.calculate_next_spawn()

        # Move Obstacles
        speed = min(15, BASE_OBSTACLE_SPEED + (self.score // DIFFICULTY_INTERVAL))
        dino_rect = pygame.Rect(50, self.dino_y, 40, 40)

        for rect in self.obstacles[:]:
            rect.x -= speed
            if dino_rect.colliderect(rect):
                self.game_over = True
            
            if rect.right < 0:
                self.obstacles.remove(rect)
                self.score += 1

    def draw(self):
        screen.fill(WHITE)
        pygame.draw.line(screen, LIGHT_GRAY, (0, GROUND_Y), (WIDTH, GROUND_Y))

        # Draw Dino
        if dino_img:
            screen.blit(dino_img, (50, self.dino_y))
        else:
            pygame.draw.rect(screen, BLACK, (50, self.dino_y, 40, 40))

        # Draw Obstacles
        for rect in self.obstacles:
            if cactus_img:
                scaled_cactus = pygame.transform.scale(cactus_img, (rect.width, rect.height))
                screen.blit(scaled_cactus, rect)
            else:
                pygame.draw.rect(screen, RED, rect)

        # UI
        level = (self.score // DIFFICULTY_INTERVAL) + 1
        score_txt = font_small.render(f"Score: {self.score}", True, BLACK)
        level_txt = font_small.render(f"Level: {level}", True, BLACK)
        screen.blit(score_txt, (10, 10))
        screen.blit(level_txt, (10, 30))

        if self.game_over:
            over_txt = font_large.render("GAME OVER", True, BLACK)
            restart_txt = font_small.render("Press SPACE to Restart", True, BLACK)
            screen.blit(over_txt, (WIDTH//2 - 80, HEIGHT//2 - 20))
            screen.blit(restart_txt, (WIDTH//2 - 70, HEIGHT//2 + 20))

        pygame.display.flip()

# Main Loop
game = Game()
running = True
while running:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False
        
        if event.type == pygame.KEYDOWN:
            if event.key == pygame.K_SPACE:
                if game.game_over:
                    game.reset()
                elif not game.is_jumping:
                    game.dino_vy = -16
                    game.is_jumping = True

    game.update()
    game.draw()
    clock.tick(FPS)

pygame.quit()
