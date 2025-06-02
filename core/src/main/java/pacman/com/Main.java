package pacman.com;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private BitmapFont font;
    private Texture menuBackground;

    // Game objects
    private Pacman pacman;
    private Array<Ghost> ghosts;
    private Array<Rectangle> dots;
    private Array<PowerUp> powerUps;
    private Maze maze;
    private Texture dotTexture;

    // Game state
    private int score;
    private int lives;
    private boolean gameOver;
    private boolean gameWon;
    private boolean inMenu;
    private float gameTime;
    private float powerUpSpawnTimer;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);
        menuBackground = new Texture("MainScreenMenu.png");

        camera = new OrthographicCamera();
        viewport = new FitViewport(720, 800, camera); // 18x20 tiles (40px each)
        viewport.apply();
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();

        startGame();
    }

    private void startGame() {
        // Initialize maze (18x20 tiles)
        maze = new Maze(720, 800); // 18*40=720, 20*40=800

        // Initialize pacman
        pacman = new Pacman(new Vector2(360, 100)); // Center bottom

        // Initialize ghosts
        ghosts = new Array<>();
        ghosts.add(new Ghost(new Vector2(360, 700), GhostType.RED, pacman, maze)); // Top center
        ghosts.add(new Ghost(new Vector2(120, 700), GhostType.PINK, pacman, maze)); // Top left
        ghosts.add(new Ghost(new Vector2(600, 700), GhostType.BLUE, pacman, maze)); // Top right
        ghosts.add(new Ghost(new Vector2(360, 500), GhostType.ORANGE, pacman, maze)); // Middle

        // Initialize dots
        dots = new Array<>();
        dotTexture = new Texture("dot.png");
        initializeDots();

        // Initialize power ups
        powerUps = new Array<>();
        powerUpSpawnTimer = 10f; // First power up after 10 seconds

        // Game state
        score = 0;
        lives = 3;
        gameOver = false;
        gameWon = false;
        inMenu = true;
        gameTime = 0;
    }

    private void initializeDots() {
        float tileSize = maze.getTileSize();
        for (float x = tileSize; x < maze.getWidth() - tileSize; x += tileSize) {
            for (float y = tileSize; y < maze.getHeight() - tileSize; y += tileSize) {
                if (!maze.isWallAt(x, y) &&
                    !isNearStartPosition(x, y) &&
                    Math.random() > 0.3) { // 70% chance for dot
                    dots.add(new Rectangle(x - 5, y - 5, 10, 10));
                }
            }
        }
    }

    private boolean isNearStartPosition(float x, float y) {
        // Don't place dots near pacman or ghost start positions
        return (x >= 320 && x <= 400 && y >= 80 && y <= 120) || // Pacman area
            (x >= 320 && x <= 400 && y >= 680 && y <= 720) || // Ghost area 1
            (x >= 100 && x <= 140 && y >= 680 && y <= 720) || // Ghost area 2
            (x >= 580 && x <= 620 && y >= 680 && y <= 720) || // Ghost area 3
            (x >= 320 && x <= 400 && y >= 480 && y <= 520);    // Ghost area 4
    }

    @Override
    public void render() {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (inMenu) {
            renderMenu();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                inMenu = false;
            }
            return;
        }

        if (!gameOver && !gameWon) {
            update(Gdx.graphics.getDeltaTime());
        }

        // Draw everything
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw maze
        maze.render(batch);

        // Draw dots
        for (Rectangle dot : dots) {
            batch.draw(dotTexture, dot.x, dot.y, dot.width, dot.height);
        }

        // Draw power ups
        for (PowerUp powerUp : powerUps) {
            if (powerUp.isActive()) {
                powerUp.render(batch);
            }
        }

        // Draw ghosts
        for (Ghost ghost : ghosts) {
            ghost.render(batch);
        }

        // Draw pacman
        pacman.render(batch);

        // Draw UI
        font.draw(batch, "Score: " + score, 20, 780);
        font.draw(batch, "Lives: " + lives, 600, 780);

        if (gameOver) {
            font.draw(batch, "GAME OVER", 300, 400);
            font.draw(batch, "Press R to restart", 280, 350);
        }
        if (gameWon) {
            font.draw(batch, "YOU WIN!", 310, 400);
            font.draw(batch, "Press R to restart", 280, 350);
        }

        batch.end();
    }

    private void renderMenu() {
        batch.begin();
        batch.draw(menuBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        font.draw(batch, "Press ENTER to Start", 250, 200);
        batch.end();
    }

    private void update(float delta) {
        gameTime += delta;
        powerUpSpawnTimer -= delta;

        // Spawn random power ups
        if (powerUpSpawnTimer <= 0) {
            spawnRandomPowerUp();
            powerUpSpawnTimer = 15f + (float)Math.random() * 15f; // 15-30 seconds
        }

        // Update game objects
        pacman.update(delta);
        for (Ghost ghost : ghosts) {
            ghost.update(delta);
        }
        for (PowerUp powerUp : powerUps) {
            powerUp.update(delta);
        }

        // Check collisions
        checkDotCollisions();
        checkPowerUpCollisions();
        checkGhostCollisions();

        // Check win condition
        if (dots.size == 0) {
            gameWon = true;
        }

        // Handle restart
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            dispose();
            startGame();
        }
    }

    private void spawnRandomPowerUp() {
        if (powerUps.size >= 3) return; // Max 3 power ups at once

        float x, y;
        int attempts = 0;
        do {
            x = (float)Math.random() * (maze.getWidth() - 40) + 20;
            y = (float)Math.random() * (maze.getHeight() - 40) + 20;
            attempts++;
        } while ((maze.isWallAt(x, y) || isTooCloseToOtherPowerUps(x, y)) && attempts < 100);

        if (attempts < 100) {
            PowerUp powerUp;
            float rand = (float)Math.random();
            if (rand < 0.4f) {
                powerUp = new Cherry(new Vector2(x, y));
            } else if (rand < 0.8f) {
                powerUp = new Cherry2(new Vector2(x, y));
            } else {
                powerUp = new PowerFood(new Vector2(x, y));
            }
            powerUps.add(powerUp);
        }
    }

    private boolean isTooCloseToOtherPowerUps(float x, float y) {
        for (PowerUp powerUp : powerUps) {
            if (powerUp.isActive() &&
                Vector2.dst(x, y, powerUp.getPosition().x, powerUp.getPosition().y) < 100) {
                return true;
            }
        }
        return false;
    }

    private void checkDotCollisions() {
        for (int i = dots.size - 1; i >= 0; i--) {
            Rectangle dot = dots.get(i);
            if (pacman.getPosition().dst(dot.x + dot.width/2, dot.y + dot.height/2) < 20) {
                dots.removeIndex(i);
                score += 10;
            }
        }
    }

    private void checkPowerUpCollisions() {
        for (int i = powerUps.size - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            if (powerUp.isActive() && pacman.collidesWith(powerUp)) {
                score += powerUp.getScoreValue();

                if (powerUp instanceof PowerFood) {
                    pacman.setPoweredUp(true, 10f);
                    for (Ghost ghost : ghosts) {
                        ghost.setScared(true);
                    }
                }

                powerUp.collect();
                powerUps.removeIndex(i);
            } else if (!powerUp.isActive()) {
                powerUps.removeIndex(i);
            }
        }
    }

    private void checkGhostCollisions() {
        for (Ghost ghost : ghosts) {
            if (pacman.collidesWith(ghost)) {
                if (ghost.isScared()) {
                    // Pacman eats ghost
                    ghost.respawn();
                    score += 200;
                } else if (pacman.isPoweredUp()) {
                    // Ghost is scared but not yet by PowerFood
                    ghost.setScared(true);
                } else {
                    // Pacman loses a life
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                    } else {
                        // Reset positions
                        pacman.getPosition().set(360, 100);
                        for (Ghost g : ghosts) {
                            g.respawn();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        menuBackground.dispose();
        if (maze != null) maze.dispose();
        if (pacman != null) pacman.dispose();
        if (dotTexture != null) dotTexture.dispose();
        for (Ghost ghost : ghosts) {
            ghost.dispose();
        }
        for (PowerUp powerUp : powerUps) {
            powerUp.dispose();
        }
    }
}
