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

    // Objek Game
    private Pacman pacman;
    private Array<Ghost> ghosts;
    private Array<Rectangle> dots;
    private Array<PowerUp> powerUps;
    private Maze maze;
    private Texture dotTexture;

    // Status Game
    private int score;
    private int lives;
    private boolean gameOver;
    private boolean gameWon;
    private boolean inMenu;
    private float gameTime;
    private float powerUpSpawnTimer;
    private Random random; // Tambahkan objek Random

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);
        menuBackground = new Texture("MainScreenMenu.png");
        random = new Random(); // Inisialisasi Random

        camera = new OrthographicCamera();
        // Ukuran viewport harus sesuai dengan ukuran maze baru (19x21 tiles, @40px/tile)
        viewport = new FitViewport(19 * 40, 21 * 40, camera); // Lebar 760, Tinggi 840
        viewport.apply();
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();

        startGame();
    }

    private void startGame() {
        // Inisialisasi maze (19x21 tiles)
        maze = new Maze(viewport.getWorldWidth(), viewport.getWorldHeight());

        // Inisialisasi pacman
        // Posisi awal Pac-Man di tile (9, 14) -> (9 * 40) + 5 = 365, (14 * 40) + 5 = 565.
        // Di gambar, P ada di row 14, column 9 (dari bawah).
        pacman = new Pacman(new Vector2(9 * maze.getTileSize() + 5,
            14 * maze.getTileSize() + 5), maze);

        // Inisialisasi hantu
        ghosts = new Array<>();
        // Posisi awal hantu di gambar: b, p, o, r (sesuai kolom 7,8,9,10, baris 8 atau 9)
        // Kita gunakan pusat tile
        // Column 7, Row 8 = (7*40 + 5, 8*40 + 5) = (285, 325) -- untuk 'b' (Blue)
        // Column 8, Row 8 = (8*40 + 5, 8*40 + 5) = (325, 325) -- untuk 'p' (Pink)
        // Column 9, Row 8 = (9*40 + 5, 8*40 + 5) = (365, 325) -- untuk 'o' (Orange)
        // Column 10, Row 8 = (10*40 + 5, 8*40 + 5) = (405, 325) -- untuk 'r' (Red)
        // Note: Posisi di gambar adalah pusat tile, tapi karena entitas 30x30, kita harus offset 5px

        ghosts.add(new Ghost(new Vector2(7 * maze.getTileSize() + 5, 8 * maze.getTileSize() + 5), GhostType.BLUE, pacman, maze));
        ghosts.add(new Ghost(new Vector2(8 * maze.getTileSize() + 5, 8 * maze.getTileSize() + 5), GhostType.PINK, pacman, maze));
        ghosts.add(new Ghost(new Vector2(9 * maze.getTileSize() + 5, 8 * maze.getTileSize() + 5), GhostType.ORANGE, pacman, maze));
        ghosts.add(new Ghost(new Vector2(10 * maze.getTileSize() + 5, 8 * maze.getTileSize() + 5), GhostType.RED, pacman, maze));


        // Inisialisasi dots
        dots = new Array<>();
        dotTexture = new Texture("dot.png");
        initializeDots();

        // Inisialisasi power up
        powerUps = new Array<>();
        powerUpSpawnTimer = 10f; // Power up spawn setiap 10-25 detik

        // Status Game
        score = 0;
        lives = 3;
        gameOver = false;
        gameWon = false;
        inMenu = true;
        gameTime = 0;
    }

    // Ubah logika initializeDots agar lebih akurat dengan maze baru
    private void initializeDots() {
        float tileSize = maze.getTileSize();
        dots.clear(); // Clear existing dots first for restarts

        // Iterasi melalui setiap tile yang mungkin
        for (int y = 0; y < maze.getHeight() / tileSize; y++) {
            for (int x = 0; x < maze.getWidth() / tileSize; x++) {
                // Hitung pusat tile
                float tileCenterX = x * tileSize + tileSize / 2;
                float tileCenterY = y * tileSize + tileSize / 2;

                // Buat bounding box kecil untuk dot di tengah tile
                Rectangle dotBounds = new Rectangle(tileCenterX - 5, tileCenterY - 5, 10, 10); // Dot 10x10

                // Hanya tambahkan dot jika tile tersebut bukan dinding
                // dan tidak terlalu dekat dengan posisi awal Pac-Man atau hantu.
                // Fungsi isWallAt di Maze.java harus akurat.
                // isNearInitialEntityPosition akan memerlukan sedikit penyesuaian untuk koordinat baru
                if (!maze.collidesWithWall(dotBounds) && !isNearInitialEntityPosition(tileCenterX, tileCenterY)) {
                    dots.add(dotBounds);
                }
            }
        }
    }

    private boolean isNearInitialEntityPosition(float x, float y) {
        // Toleransi jarak untuk area spawn (sekitar 2.5 tiles)
        float spawnTolerance = maze.getTileSize() * 2.5f;

        // Posisi awal Pacman (berdasarkan kode di atas: x=365, y=565)
        if (Vector2.dst(x, y, 9 * maze.getTileSize() + maze.getTileSize()/2, 14 * maze.getTileSize() + maze.getTileSize()/2) < spawnTolerance) return true;

        // Posisi awal Ghosts (berdasarkan kode di atas)
        if (Vector2.dst(x, y, 7 * maze.getTileSize() + maze.getTileSize()/2, 8 * maze.getTileSize() + maze.getTileSize()/2) < spawnTolerance) return true; // Blue Ghost
        if (Vector2.dst(x, y, 8 * maze.getTileSize() + maze.getTileSize()/2, 8 * maze.getTileSize() + maze.getTileSize()/2) < spawnTolerance) return true; // Pink Ghost
        if (Vector2.dst(x, y, 9 * maze.getTileSize() + maze.getTileSize()/2, 8 * maze.getTileSize() + maze.getTileSize()/2) < spawnTolerance) return true; // Orange Ghost
        if (Vector2.dst(x, y, 10 * maze.getTileSize() + maze.getTileSize()/2, 8 * maze.getTileSize() + maze.getTileSize()/2) < spawnTolerance) return true; // Red Ghost

        return false;
    }


    @Override
    public void render() {
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

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        maze.render(batch);

        for (Rectangle dot : dots) {
            batch.draw(dotTexture, dot.x, dot.y, dot.width, dot.height);
        }

        for (PowerUp powerUp : powerUps) {
            if (powerUp.isActive()) {
                powerUp.render(batch);
            }
        }

        for (Ghost ghost : ghosts) {
            ghost.render(batch);
        }

        pacman.render(batch);

        font.draw(batch, "Score: " + score, 20, viewport.getWorldHeight() - 20); // Posisi UI
        font.draw(batch, "Lives: " + lives, viewport.getWorldWidth() - 150, viewport.getWorldHeight() - 20); // Posisi UI

        if (gameOver) {
            font.draw(batch, "GAME OVER", viewport.getWorldWidth() / 2 - 100, viewport.getWorldHeight() / 2 + 50);
            font.draw(batch, "Press R to restart", viewport.getWorldWidth() / 2 - 120, viewport.getWorldHeight() / 2);
        }
        if (gameWon) {
            font.draw(batch, "YOU WIN!", viewport.getWorldWidth() / 2 - 100, viewport.getWorldHeight() / 2 + 50);
            font.draw(batch, "Press R to restart", viewport.getWorldWidth() / 2 - 120, viewport.getWorldHeight() / 2);
        }

        batch.end();
    }

    private void renderMenu() {
        batch.begin();
        batch.draw(menuBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        font.draw(batch, "Press ENTER to Start", viewport.getWorldWidth() / 2 - 150, viewport.getWorldHeight() / 2 - 100);
        batch.end();
    }

    private void update(float delta) {
        gameTime += delta;
        powerUpSpawnTimer -= delta;

        if (powerUpSpawnTimer <= 0) {
            spawnRandomPowerUp();
            powerUpSpawnTimer = 15f + random.nextFloat() * 10f; // Spawn antara 15-25 detik
        }

        pacman.update(delta);
        for (Ghost ghost : ghosts) {
            ghost.update(delta);
        }
        for (PowerUp powerUp : powerUps) {
            powerUp.update(delta);
        }

        checkDotCollisions();
        checkPowerUpCollisions();
        checkGhostCollisions();

        if (dots.size == 0 && !gameOver) { // Pastikan game belum over saat menang
            gameWon = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            dispose(); // Dispose resources sebelum restart
            create();  // Panggil create() untuk memulai game baru
        }
    }

    private void spawnRandomPowerUp() {
        // Jangan terlalu banyak power-up aktif sekaligus
        int activePowerUpsCount = 0;
        for (PowerUp pu : powerUps) {
            if (pu.isActive()) activePowerUpsCount++;
        }
        if (activePowerUpsCount >= 2) return; // Maksimal 2 power-up aktif

        float x, y;
        int attempts = 0;
        float tileSize = maze.getTileSize();
        do {
            // Random tile coordinates
            int tileX = random.nextInt((int)(maze.getWidth() / tileSize));
            int tileY = random.nextInt((int)(maze.getHeight() / tileSize));

            // Hitung posisi power-up di tengah tile (ukuran power-up 20x20)
            x = tileX * tileSize + (tileSize / 2) - (20 / 2); // 20 adalah ukuran power-up
            y = tileY * tileSize + (tileSize / 2) - (20 / 2);

            attempts++;
            // Check if the tile is a wall, or too close to initial entity positions
        } while ((maze.collidesWithWall(new Rectangle(x, y, 20, 20)) || isNearInitialEntityPosition(x + 10, y + 10)) && attempts < 100); // x+10, y+10 adalah center powerup

        if (attempts < 100) {
            PowerUp powerUp;
            float rand = random.nextFloat(); // Gunakan objek random
            if (rand < 0.4f) { // 40% Cherry
                powerUp = new Cherry(new Vector2(x, y));
            } else if (rand < 0.8f) { // 40% Cherry2
                powerUp = new Cherry2(new Vector2(x, y));
            } else { // 20% PowerFood
                powerUp = new PowerFood(new Vector2(x, y));
            }
            powerUps.add(powerUp);
        }
    }

    private void checkDotCollisions() {
        Rectangle pacmanBounds = new Rectangle(pacman.getPosition().x, pacman.getPosition().y, pacman.getSize().x, pacman.getSize().y);
        for (int i = dots.size - 1; i >= 0; i--) {
            Rectangle dot = dots.get(i);
            if (pacmanBounds.overlaps(dot)) { // Gunakan overlaps untuk akurasi
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
                    pacman.setPoweredUp(true, 10f); // Power-up Pacman selama 10 detik
                    for (Ghost ghost : ghosts) {
                        ghost.setScared(true); // Buat semua hantu ketakutan
                    }
                }

                powerUp.collect(); // Set power-up as collected (inactive)
            } else if (!powerUp.isActive()) {
                powerUps.removeIndex(i); // Hapus power-up yang sudah tidak aktif
            }
        }
    }

    private void checkGhostCollisions() {
        Rectangle pacmanBounds = new Rectangle(pacman.getPosition().x, pacman.getPosition().y, pacman.getSize().x, pacman.getSize().y);
        for (Ghost ghost : ghosts) {
            Rectangle ghostBounds = new Rectangle(ghost.getPosition().x, ghost.getPosition().y, ghost.getSize().x, ghost.getSize().y);
            if (pacmanBounds.overlaps(ghostBounds)) {
                if (ghost.isScared()) {
                    ghost.respawn();
                    score += 200;
                } else if (pacman.isPoweredUp()) {
                    // Jika Pacman powered up, tetapi hantu belum scared, buat hantu scared
                    ghost.setScared(true);
                } else {
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                    } else {
                        // Reset posisi Pac-Man dan semua hantu ke posisi awal
                        pacman.getPosition().set(9 * maze.getTileSize() + 5,
                            14 * maze.getTileSize() + 5);
                        for (Ghost g : ghosts) {
                            g.respawn(); // Reset posisi hantu
                        }
                    }
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
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
