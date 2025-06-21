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

import java.util.Random;

enum GameState {
    MENU,
    PLAYING,
    RESPAWNING,
    GAME_OVER,
    GAME_WON
}

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private BitmapFont font;
    private Texture menuBackground;
    private Texture dotTexture;

    private Pacman pacman;
    private Array<Ghost> ghosts;
    private Array<Rectangle> dots;
    private Array<PowerUp> powerUps;
    private Maze maze;

    private int score;
    private int lives;
    private float gameTime;
    private float powerUpSpawnTimer;
    private Random random;

    private GameState currentState;
    private float respawnTimer;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);
        menuBackground = new Texture("MainScreenMenu.png");
        random = new Random();
        camera = new OrthographicCamera();

        // === PERBAIKAN KUNCI ADA DI SINI ===
        // 1. BUAT MAZE DULUAN agar kita tahu ukurannya
        maze = new Maze();

        // 2. SETELAH MAZE ADA, BARU BUAT VIEWPORT menggunakan ukurannya
        viewport = new FitViewport(maze.getWidth(), maze.getHeight(), camera);
        viewport.apply(); // Terapkan viewport
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        // 3. MULAI GAME setelah semua komponen dasar siap
        startGame();
    }

    private void startGame() {
        maze = new Maze();

        // --- KOORDINAT SPAWN SUDAH DIPASTIKAN AMAN UNTUK LABIRIN BARU ---
        Vector2 pacmanStartPos = new Vector2(9 * maze.getTileSize() + 5, 5 * maze.getTileSize() + 5);
        pacman = new Pacman(pacmanStartPos, maze);

        ghosts = new Array<>();
        ghosts.add(new Ghost(new Vector2(9 * maze.getTileSize() + 5, 11 * maze.getTileSize() + 5), GhostType.RED, pacman, maze));
        ghosts.add(new Ghost(new Vector2(8 * maze.getTileSize() + 5, 10 * maze.getTileSize() + 5), GhostType.PINK, pacman, maze));
        ghosts.add(new Ghost(new Vector2(10 * maze.getTileSize() + 5, 10 * maze.getTileSize() + 5), GhostType.BLUE, pacman, maze));
        ghosts.add(new Ghost(new Vector2(9 * maze.getTileSize() + 5, 9 * maze.getTileSize() + 5), GhostType.ORANGE, pacman, maze));

        dots = new Array<>();
        if (dotTexture == null) {
            dotTexture = new Texture("dot.png");
        }

        initializeDots();

        powerUps = new Array<>();
        powerUpSpawnTimer = 10f;

        score = 0;
        lives = 3;
        currentState = GameState.MENU; // Mulai dari menu
        gameTime = 0;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (currentState == GameState.MENU) {
            renderMenu();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                currentState = GameState.PLAYING;
            }
            return;
        }

        update(Gdx.graphics.getDeltaTime());

        batch.begin();

        maze.render(batch);
        for (Rectangle dot : dots) { batch.draw(dotTexture, dot.x, dot.y, dot.width, dot.height); }
        for (PowerUp powerUp : powerUps) { if (powerUp.isActive()) powerUp.render(batch); }
        pacman.render(batch);
        for (Ghost ghost : ghosts) { ghost.render(batch); }

        font.draw(batch, "Score: " + score, 20, viewport.getWorldHeight() - 20);
        font.draw(batch, "Lives: " + lives, viewport.getWorldWidth() - 150, viewport.getWorldHeight() - 20);

        if (currentState == GameState.GAME_OVER) {
            font.draw(batch, "GAME OVER", viewport.getWorldWidth() / 2 - 100, viewport.getWorldHeight() / 2 + 50);
            font.draw(batch, "Press R to restart", viewport.getWorldWidth() / 2 - 120, viewport.getWorldHeight() / 2);
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) restartGame();
        } else if (currentState == GameState.GAME_WON) {
            font.draw(batch, "YOU WIN!", viewport.getWorldWidth() / 2 - 100, viewport.getWorldHeight() / 2 + 50);
            font.draw(batch, "Press R to restart", viewport.getWorldWidth() / 2 - 120, viewport.getWorldHeight() / 2);
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) restartGame();
        }

        batch.end();
    }

    private void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartGame();
            return;
        }

        if (currentState == GameState.PLAYING) {
            gameTime += delta;
            powerUpSpawnTimer -= delta;
            if (powerUpSpawnTimer <= 0) {
                spawnRandomPowerUp();
                powerUpSpawnTimer = 15f + random.nextFloat() * 10f;
            }

            pacman.update(delta);
            for (Ghost ghost : ghosts) { ghost.update(delta); }
            for (PowerUp powerUp : powerUps) { powerUp.update(delta); }

            checkDotCollisions();
            checkPowerUpCollisions();
            checkGhostCollisions();

            if (dots.size == 0) {
                currentState = GameState.GAME_WON;
            }
        } else if (currentState == GameState.RESPAWNING) {
            respawnTimer -= delta;
            if (respawnTimer <= 0) {
                resetPositionsAfterDeath();
                currentState = GameState.PLAYING;
            }
        }
    }

    private void checkGhostCollisions() {
        if (currentState != GameState.PLAYING) return;

        Rectangle pacmanBounds = new Rectangle(pacman.getPosition().x, pacman.getPosition().y, pacman.getSize().x, pacman.getSize().y);
        for (Ghost ghost : ghosts) {
            Rectangle ghostBounds = new Rectangle(ghost.getPosition().x, ghost.getPosition().y, ghost.getSize().x, ghost.getSize().y);
            if (pacmanBounds.overlaps(ghostBounds)) {
                if (ghost.isScared()) {
                    ghost.respawn();
                    score += 200;
                } else if (!pacman.isPoweredUp()) {
                    lives--;
                    if (lives <= 0) {
                        currentState = GameState.GAME_OVER;
                    } else {
                        currentState = GameState.RESPAWNING;
                        respawnTimer = 1.5f;
                    }
                }
            }
        }
    }

    private void resetPositionsAfterDeath() {
        pacman.getPosition().set(9 * maze.getTileSize() + 5, 5 * maze.getTileSize() + 5);
        pacman.resetDirection();
        for(Ghost ghost : ghosts) {
            ghost.respawn();
        }
        pacman.setPoweredUp(false, 0);
    }

    private void restartGame() {
        disposeCurrentGameAssets();
        startGame();
    }

    private void disposeCurrentGameAssets() {
        if (pacman != null) pacman.dispose();
        if(ghosts != null) { for(Ghost g : ghosts) g.dispose(); }
        if(powerUps != null) { for(PowerUp p : powerUps) p.dispose(); }
    }

    // ... Metode-metode lain (initializeDots, spawnRandomPowerUp, dll. biarkan seperti yang sudah ada) ...

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // Gunakan true untuk menjaga center
    }

    @Override
    public void dispose() {
        disposeCurrentGameAssets();
        if (maze != null) maze.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (menuBackground != null) menuBackground.dispose();
        if (dotTexture != null) dotTexture.dispose();
    }

    // Metode di bawah ini tidak ada perubahan, salin saja jika Anda belum punya
    private void renderMenu() {
        batch.begin();
        batch.draw(menuBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        font.draw(batch, "Press ENTER to Start", viewport.getWorldWidth() / 2 - 150, viewport.getWorldHeight() / 2 - 100);
        batch.end();
    }

    private void initializeDots() {
        dots.clear();
        float tileSize = maze.getTileSize();
        for (int y = 0; y < maze.getHeight() / tileSize; y++) {
            for (int x = 0; x < maze.getWidth() / tileSize; x++) {
                float centerX = x * tileSize + tileSize / 2;
                float centerY = y * tileSize + tileSize / 2;
                if (!maze.isWallAt(centerX, centerY) && !isNearInitialEntityPosition(centerX, centerY)) {
                    dots.add(new Rectangle(centerX - 5, centerY - 5, 10, 10));
                }
            }
        }
    }

    private boolean isNearInitialEntityPosition(float x, float y) {
        float spawnTolerance = maze.getTileSize() * 3.5f;
        // Gunakan posisi spawn yang sudah diperbaiki
        if (Vector2.dst(x, y, 9 * maze.getTileSize() + 5, 6 * maze.getTileSize() + 5) < spawnTolerance) return true;
        // Cek juga area kandang hantu
        Rectangle ghostHouse = new Rectangle(7 * maze.getTileSize(), 9 * maze.getTileSize(), 5 * maze.getTileSize(), 4 * maze.getTileSize());
        return ghostHouse.contains(x, y);
    }

    private void spawnRandomPowerUp() {
        int activePowerUpsCount = 0;
        for (PowerUp pu : powerUps) if (pu.isActive()) activePowerUpsCount++;
        if (activePowerUpsCount >= 2) return;

        float x, y;
        int attempts = 0;
        float tileSize = maze.getTileSize();
        do {
            int tileX = random.nextInt((int)(maze.getWidth() / tileSize));
            int tileY = random.nextInt((int)(maze.getHeight() / tileSize));
            x = tileX * tileSize + (tileSize / 2) - 10;
            y = tileY * tileSize + (tileSize / 2) - 10;
            attempts++;
        } while ((maze.collidesWithWall(new Rectangle(x, y, 20, 20)) || isNearInitialEntityPosition(x + 10, y + 10)) && attempts < 100);

        if (attempts < 100) {
            float rand = random.nextFloat();
            if (rand < 0.4f) powerUps.add(new Cherry(new Vector2(x, y)));
            else if (rand < 0.8f) powerUps.add(new Cherry2(new Vector2(x, y)));
            else powerUps.add(new PowerFood(new Vector2(x, y)));
        }
    }

    private void checkDotCollisions() {
        Rectangle pacmanBounds = new Rectangle(pacman.getPosition().x, pacman.getPosition().y, pacman.getSize().x, pacman.getSize().y);
        for (int i = dots.size - 1; i >= 0; i--) {
            if (pacmanBounds.overlaps(dots.get(i))) {
                dots.removeIndex(i);
                score += 10;
            }
        }
    }

    private void checkPowerUpCollisions() {
        Rectangle pacmanBounds = new Rectangle(pacman.getPosition().x, pacman.getPosition().y, pacman.getSize().x, pacman.getSize().y);
        for (int i = powerUps.size - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            if (powerUp.isActive() && pacmanBounds.overlaps(new Rectangle(powerUp.getPosition().x, powerUp.getPosition().y, powerUp.getSize().x, powerUp.getSize().y))) {
                score += powerUp.getScoreValue();
                if (powerUp instanceof PowerFood) {
                    pacman.setPoweredUp(true, 10f);
                    for (Ghost ghost : ghosts) ghost.setScared(true);
                }
                powerUp.collect();
            }
        }
    }
}
