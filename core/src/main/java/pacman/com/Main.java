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

import java.util.Random; // Import Random

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private BitmapFont font;
    private Texture menuBackground;

    // Game Objects
    private Pacman pacman;
    private Array<Ghost> ghosts;
    private Array<Rectangle> dots;
    private Array<PowerUp> powerUps;
    private Maze maze;
    private Texture dotTexture;

    // Game State
    private int score;
    private int lives;
    private boolean gameOver;
    private boolean gameWon;
    private boolean inMenu;
    private float gameTime;
    private float powerUpSpawnTimer;
    private Random random; // Add Random object

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);
        menuBackground = new Texture("MainScreenMenu.png");
        random = new Random(); // Initialize Random

        camera = new OrthographicCamera();
        // Viewport size should match new maze size (18x19 tiles, @40px/tile)
        // Note: original comment said 19x21, but Maze class defines 18x19. Using 18x19 for consistency.
        viewport = new FitViewport(18 * 40, 19 * 40, camera); // Width 720, Height 760 (based on Maze.java TOTAL_COLS and TOTAL_ROWS)
        viewport.apply();
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();

        startGame(); //
    }

    private void startGame() {
        // Initialize maze (18x19 tiles)
        maze = new Maze(viewport.getWorldWidth(), viewport.getWorldHeight());

        // Initialize Pacman
        // Pac-Man's starting position at tile (9, 14) -> (9 * 40) + 5 = 365, (14 * 40) + 5 = 565.
        // In the image, P is at row 14, column 9 (from bottom).
        pacman = new Pacman(new Vector2(9 * maze.getTileSize() + 5,
            14 * maze.getTileSize() + 5), maze);

        // Initialize Ghosts
        ghosts = new Array<>(); //
        // Ghost initial positions from image: b, p, o, r (columns 7,8,9,10, row 8 or 9)
        // We use tile center
        // Column 7, Row 8 = (7*40 + 5, 8*40 + 5) = (285, 325) -- for 'b' (Blue)
        // Column 8, Row 8 = (8*40 + 5, 8*40 + 5) = (325, 325) -- for 'p' (Pink)
        // Column 9, Row 8 = (9*40 + 5, 8*40 + 5) = (365, 325) -- for 'o' (Orange)
        // Column 10, Row 8 = (10*40 + 5, 8*40 + 5) = (405, 325) -- for 'r' (Red)
        // Note: The position in the image is the tile center, but since entities are 30x30, we need a 5px offset.

        ghosts.add(new Ghost(new Vector2(7 * maze.getTileSize() + 5, 8 * maze.getTileSize() + 5), GhostType.BLUE, pacman, maze)); //
        ghosts.add(new Ghost(new Vector2(8 * maze.getTileSize() + 5, 8 * maze.getTileSize() + 5), GhostType.PINK, pacman, maze)); //
        ghosts.add(new Ghost(new Vector2(9 * maze.getTileSize() + 5, 8 * maze.getTileSize() + 5), GhostType.ORANGE, pacman, maze)); //
        ghosts.add(new Ghost(new Vector2(10 * maze.getTileSize() + 5, 8 * maze.getTileSize() + 5), GhostType.RED, pacman, maze)); //


        // Initialize dots
        dots = new Array<>(); //
        dotTexture = new Texture("dot.png"); //
        initializeDots(); //

        // Initialize power ups
        powerUps = new Array<>(); //
        powerUpSpawnTimer = 10f; // Power up spawns every 10-25 seconds

        // Game State
        score = 0; //
        lives = 3; //
        gameOver = false; //
        gameWon = false; //
        inMenu = true; //
        gameTime = 0; //
    }

    // Modify initializeDots logic for better maze accuracy
    private void initializeDots() {
        float tileSize = maze.getTileSize(); //
        dots.clear(); // Clear existing dots first for restarts

        // Iterate through each possible tile
        for (int y = 0; y < maze.getHeight() / tileSize; y++) {
            for (int x = 0; x < maze.getWidth() / tileSize; x++) {
                // Calculate tile center
                float tileCenterX = x * tileSize + tileSize / 2; //
                float tileCenterY = y * tileSize + tileSize / 2; //

                // Create a small bounding box for the dot at the tile center
                Rectangle dotBounds = new Rectangle(tileCenterX - 5, tileCenterY - 5, 10, 10); // Dot 10x10

                // Only add dot if the tile is not a wall and not too close to Pac-Man or ghost initial positions.
                if (!maze.collidesWithWall(dotBounds) && !isNearInitialEntityPosition(tileCenterX, tileCenterY)) { //
                    dots.add(dotBounds); //
                }
            }
        }
    }

    private boolean isNearInitialEntityPosition(float x, float y) {
        // Distance tolerance for spawn area (approx. 2.5 tiles)
        float spawnTolerance = maze.getTileSize() * 2.5f;

        // Pacman's initial position (based on code above: x=365, y=565)
        if (Vector2.dst(x, y, 9 * maze.getTileSize() + maze.getTileSize()/2, 14 * maze.getTileSize() + maze.getTileSize()/2) < spawnTolerance) return true; //

        // Ghosts' initial positions (based on code above)
        if (Vector2.dst(x, y, 7 * maze.getTileSize() + maze.getTileSize()/2, 8 * maze.getTileSize() + maze.getTileSize()/2) < spawnTolerance) return true; // Blue Ghost
        if (Vector2.dst(x, y, 8 * maze.getTileSize() + maze.getTileSize()/2, 8 * maze.getTileSize() + maze.getTileSize()/2) < spawnTolerance) return true; // Pink Ghost
        if (Vector2.dst(x, y, 9 * maze.getTileSize() + maze.getTileSize()/2, 8 * maze.getTileSize() + maze.getTileSize()/2) < spawnTolerance) return true; // Orange Ghost
        if (Vector2.dst(x, y, 10 * maze.getTileSize() + maze.getTileSize()/2, 8 * maze.getTileSize() + maze.getTileSize()/2) < spawnTolerance) return true; // Red Ghost

        return false;
    }


    @Override
    public void render() {
        //Cek restart di awal (bekerja di semua kondisi)
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartGame();
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (inMenu) { //
            renderMenu(); //
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { //
                inMenu = false; //
            }
            return;
        }

        if (!gameOver && !gameWon) { //
            update(Gdx.graphics.getDeltaTime()); //
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        maze.render(batch); // Render maze

        for (Rectangle dot : dots) { // Render dots
            batch.draw(dotTexture, dot.x, dot.y, dot.width, dot.height);
        }

        for (PowerUp powerUp : powerUps) { // Render power-ups
            if (powerUp.isActive()) { //
                powerUp.render(batch); //
            }
        }

        for (Ghost ghost : ghosts) { // Render ghosts
            ghost.render(batch); //
        }

        pacman.render(batch); // Render Pacman

        font.draw(batch, "Score: " + score, 20, viewport.getWorldHeight() - 20); // UI position
        font.draw(batch, "Lives: " + lives, viewport.getWorldWidth() - 150, viewport.getWorldHeight() - 20); // UI position

        if (gameOver) { //
            font.draw(batch, "GAME OVER", viewport.getWorldWidth() / 2 - 100, viewport.getWorldHeight() / 2 + 50); //
            font.draw(batch, "Press R to restart", viewport.getWorldWidth() / 2 - 120, viewport.getWorldHeight() / 2); //
        }
        if (gameWon) { //
            font.draw(batch, "YOU WIN!", viewport.getWorldWidth() / 2 - 100, viewport.getWorldHeight() / 2 + 50); //
            font.draw(batch, "Press R to restart", viewport.getWorldWidth() / 2 - 120, viewport.getWorldHeight() / 2); //
        }

        batch.end();
    }

    private void renderMenu() {
        batch.begin();
        batch.draw(menuBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight()); // Draw menu background
        font.draw(batch, "Press ENTER to Start", viewport.getWorldWidth() / 2 - 150, viewport.getWorldHeight() / 2 - 100); // Draw menu text
        batch.end();
    }

    private void update(float delta) {
        if (inMenu || gameOver || gameWon) {
            return; // Don't update game logic if in menu/game over/game won
        }

        gameTime += delta; //
        powerUpSpawnTimer -= delta; //

        if (powerUpSpawnTimer <= 0) { //
            spawnRandomPowerUp(); //
            powerUpSpawnTimer = 15f + random.nextFloat() * 10f; // Spawn between 15-25 seconds
        }

        pacman.update(delta); // Update Pacman
        for (Ghost ghost : ghosts) { // Update Ghosts
            ghost.update(delta);
        }
        for (PowerUp powerUp : powerUps) { // Update PowerUps
            powerUp.update(delta);
        }

        checkDotCollisions(); //
        checkPowerUpCollisions(); //
        checkGhostCollisions(); //

        if (dots.size == 0 && !gameOver) { // If all dots collected and game not over, then win
            gameWon = true; //
        }

    }

    // Tambahkan method baru untuk restart game
    private void restartGame() {
        // 1. Hapus resource lama
        dispose();

        // 2. Recreate CORE OBJECTS (wajib!)
        batch = new SpriteBatch(); // <-- SpriteBatch HARUS dibuat ulang
        font = new BitmapFont();   // <-- Font juga
        font.getData().setScale(2);

        // 3. Reload texture
        menuBackground = new Texture("MainScreenMenu.png");
        dotTexture = new Texture("dot.png");

        // 4. Reset game state
        gameOver = false;
        gameWon = false;
        inMenu = false;
        score = 0;
        lives = 3;

        // 5. Inisialisasi ulang game
        startGame(); // Panggil startGame() untuk setup maze, pacman, dll
    }

    private void spawnRandomPowerUp() {
        // Don't spawn too many active power-ups simultaneously
        int activePowerUpsCount = 0;
        for (PowerUp pu : powerUps) {
            if (pu.isActive()) activePowerUpsCount++;
        }
        if (activePowerUpsCount >= 2) return; // Max 2 active power-ups

        float x, y;
        int attempts = 0;
        float tileSize = maze.getTileSize(); //
        do {
            // Random tile coordinates
            int tileX = random.nextInt((int)(maze.getWidth() / tileSize)); //
            int tileY = random.nextInt((int)(maze.getHeight() / tileSize)); //

            // Calculate power-up position at the tile center (power-up size 20x20)
            x = tileX * tileSize + (tileSize / 2) - (20 / 2); // 20 is power-up size
            y = tileY * tileSize + (tileSize / 2) - (20 / 2); //

            attempts++;
            // Check if the tile is a wall, or too close to initial entity positions
        } while ((maze.collidesWithWall(new Rectangle(x, y, 20, 20)) || isNearInitialEntityPosition(x + 10, y + 10)) && attempts < 100); // x+10, y+10 is powerup center

        if (attempts < 100) { // If a valid spot was found within attempts
            PowerUp powerUp;
            float rand = random.nextFloat(); // Use the random object
            if (rand < 0.4f) { // 40% Cherry
                powerUp = new Cherry(new Vector2(x, y)); //
            } else if (rand < 0.8f) { // 40% Cherry2
                powerUp = new Cherry2(new Vector2(x, y)); //
            } else { // 20% PowerFood
                powerUp = new PowerFood(new Vector2(x, y)); //
            }
            powerUps.add(powerUp); //
        }
    }

    private void checkDotCollisions() {
        Rectangle pacmanBounds = new Rectangle(pacman.getPosition().x, pacman.getPosition().y, pacman.getSize().x, pacman.getSize().y); //
        for (int i = dots.size - 1; i >= 0; i--) { // Iterate backwards to safely remove
            Rectangle dot = dots.get(i); //
            if (pacmanBounds.overlaps(dot)) { // Use overlaps for accuracy
                dots.removeIndex(i); //
                score += 10; //
            }
        }
    }

    private void checkPowerUpCollisions() {
        Rectangle pacmanBounds = new Rectangle(pacman.getPosition().x, pacman.getPosition().y, pacman.getSize().x, pacman.getSize().y); //
        for (int i = powerUps.size - 1; i >= 0; i--) { // Iterate backwards to safely remove
            PowerUp powerUp = powerUps.get(i); //
            if (powerUp.isActive() && pacmanBounds.overlaps(new Rectangle(powerUp.getPosition().x, powerUp.getPosition().y, powerUp.getSize().x, powerUp.getSize().y))) { //
                score += powerUp.getScoreValue(); // Add score

                if (powerUp instanceof PowerFood) { // If it's a PowerFood
                    pacman.setPoweredUp(true, 10f); // Power-up Pacman for 10 seconds
                    for (Ghost ghost : ghosts) { // Make all ghosts scared
                        ghost.setScared(true); //
                    }
                }

                powerUp.collect(); // Set power-up as collected (inactive)
            } else if (!powerUp.isActive()) { // If power-up is no longer active (e.g., duration ran out)
                powerUps.removeIndex(i); // Remove inactive power-up
            }
        }
    }

    private void checkGhostCollisions() {
        Rectangle pacmanBounds = new Rectangle(pacman.getPosition().x, pacman.getPosition().y, pacman.getSize().x, pacman.getSize().y); //
        for (Ghost ghost : ghosts) { //
            Rectangle ghostBounds = new Rectangle(ghost.getPosition().x, ghost.getPosition().y, ghost.getSize().x, ghost.getSize().y); //
            if (pacmanBounds.overlaps(ghostBounds)) { //
                if (ghost.isScared()) { // If ghost is scared
                    ghost.respawn(); // Ghost respawns
                    score += 200; // Earn points for eating scared ghost
                } else if (pacman.isPoweredUp()) {
                    // If Pacman is powered up, but ghost is not scared yet, make the ghost scared
                    ghost.setScared(true); //
                } else { // Pacman collides with non-scared ghost
                    lives--; // Lose a life
                    if (lives <= 0) { // Game Over
                        gameOver = true; //
                    }
                    // Reset Pac-Man and all ghosts to initial positions
                    pacman.getPosition().set(9 * maze.getTileSize() + 5,
                        14 * maze.getTileSize() + 5); //
                    for (Ghost g : ghosts) { //
                        g.respawn(); // Reset ghost position
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height); //
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0); //
        camera.update(); //
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (menuBackground != null) menuBackground.dispose();
        if (maze != null) maze.dispose();
        if (pacman != null) pacman.dispose();
        if (dotTexture != null) dotTexture.dispose();
        for (Ghost ghost : ghosts) ghost.dispose();
        for (PowerUp powerUp : powerUps) powerUp.dispose();
    }
}
