package pacman.com;

import com.badlogic.gdx.ApplicationAdapter;
//Base class utama LibGDX. override create(), render(), dispose() untuk bikin game loop-nya.
import com.badlogic.gdx.Gdx;
//Gdx adalah akses hal inti LibGDX (input, audio, file, dll).
import com.badlogic.gdx.Input;
//Input dipakai buat cek tombol keyboard (misalnya: Input.Keys.ENTER).
import com.badlogic.gdx.audio.Music;
//Music = untuk file audio panjang (seperti lagu background).
import com.badlogic.gdx.audio.Sound;
//Sound = untuk efek suara pendek (seperti suara mati atau makan).
import com.badlogic.gdx.graphics.GL20;
//Dipakai untuk Gdx.gl.glClear(). membersihkan layar di setiap frame (pakai OpenGL).
import com.badlogic.gdx.graphics.OrthographicCamera;
// Kamera 2D yang digunakan agar tampilan game bisa digeser/zoom sesuai kebutuhan.
import com.badlogic.gdx.graphics.Texture;
//Texture: untuk gambar (dot, pacman, hantu, background, dll).
import com.badlogic.gdx.graphics.g2d.BitmapFont;
//BitmapFont: untuk menampilkan teks seperti skor dan nyawa.
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//SpriteBatch: menggambar semua objek dalam satu batch (efisien untuk performa).
import com.badlogic.gdx.math.Rectangle;
//Rectangle: dipakai untuk hitbox (tabrakan).
import com.badlogic.gdx.math.Vector2;
//Vector2: representasi posisi 2D (x dan y).
import com.badlogic.gdx.utils.Array;
//Array: array milik LibGDX, mirip ArrayList tapi lebih ringan.
import com.badlogic.gdx.utils.viewport.FitViewport;
//FitViewport: menyesuaikan tampilan game ke ukuran layar tapi tetap menjaga rasio aspek.
import java.util.Random;
//Untuk acak posisi power-up, menentukan jenis power-up, dan variasi lainnya.

enum GameState {
    MENU,
    PLAYING,
    RESPAWNING,
    GAME_OVER,
    GAME_WON
}

public class Main extends ApplicationAdapter {
    private SpriteBatch batch; // untuk menggambar (gambar, font)
    private OrthographicCamera camera; // kamera untuk atur tampilan game
    private FitViewport viewport; // atur ukuran layar
    private BitmapFont font; // teks skor & lives
    private Texture menuBackground, dotTexture; // gambar background dan titik

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
    private float respawnTimer,powerUpRemainingTime; // untuk waktu hidup, untuk waktu power upnya
    private Music music, musicScared; //suara musik dan musik ketika ada powerup
    private Sound soundDie; //suara pas pacman mati



    @Override
    public void create() {
        batch = new SpriteBatch();
        //Membuat SpriteBatch untuk menggambar objek 2D (gambar, tulisan, animasi).
        font = new BitmapFont();
        font.getData().setScale(2);
        //Memperbesar ukuran font jadi 2x lipat dari ukuran standar. (contoh :score dan darahnya)
        menuBackground = new Texture("MainScreenMenu.png");
        //Memuat gambar background untuk menu utama dari file gambar MainScreenMenu.png.
        random = new Random();
        camera = new OrthographicCamera();

        // === PERBAIKAN KUNCI ADA DI SINI ===
        // 1. BUAT MAZE DULUAN agar kita tahu ukurannya
        maze = new Maze();

        // 2. SETELAH MAZE ADA, BARU BUAT VIEWPORT menggunakan ukurannya
        viewport = new FitViewport(maze.getWidth(), maze.getHeight(), camera);
        //FitViewport menjaga rasio aspek saat jendela diresize.
        viewport.apply(); // Terapkan viewport
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        // 3. MULAI GAME setelah semua komponen dasar siap
        startGame();
    }

    private void startGame() {
        maze = new Maze();
        music = Gdx.audio.newMusic(Gdx.files.internal("Pac-man theme remix - By Arsenic1987.mp3"));
        music.setLooping(true);
        music.setVolume(.3f);

        // --- KOORDINAT SPAWN SUDAH DIPASTIKAN AMAN UNTUK LABIRIN BARU ---
        Vector2 pacmanStartPos = new Vector2(
            9 * maze.getTileSize() + 5,
            5 * maze.getTileSize() + 5); //lokasi pacman
        pacman = new Pacman(pacmanStartPos, maze);

        ghosts = new Array<>();
        ghosts.add(new Ghost(new Vector2(9 * maze.getTileSize() + 5, 11 * maze.getTileSize() + 5), GhostType.RED, pacman, maze));
        ghosts.add(new Ghost(new Vector2(8 * maze.getTileSize() + 5, 10 * maze.getTileSize() + 5), GhostType.PINK, pacman, maze));
        ghosts.add(new Ghost(new Vector2(10 * maze.getTileSize() + 5, 10 * maze.getTileSize() + 5), GhostType.BLUE, pacman, maze));
        ghosts.add(new Ghost(new Vector2(9 * maze.getTileSize() + 5, 9 * maze.getTileSize() + 5), GhostType.ORANGE, pacman, maze));
        //lokasi ghost
        dots = new Array<>();
        if (dotTexture == null) {
            dotTexture = new Texture("dot.png");
        } // pointnya

        initializeDots(); // dimunculkan dotnya

        powerUps = new Array<>(); // skillnya dibuat
        powerUpSpawnTimer = 5f; //waktu awal (timer) selama 5 detik sebelum power-up pertama muncul di dalam game.

        score = 0; // score awal
        lives = 3; // darah awal
        currentState = GameState.MENU; // Mulai dari menu
        gameTime = 0; //waktu awal
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);// atur warna latar (hitam)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); //// bersihkan/tampilkan layar dengan warna itu

        camera.update(); //Memperbarui posisi dan proyeksi kamera
        batch.setProjectionMatrix(camera.combined); //Menyinkronkan SpriteBatch dengan kamera.
        //Supaya objek yang digambar mengikuti pandangan kamera (zoom, posisi, dst).

        //Jika game dalam mode menu, tampilkan layar menu (renderMenu()).
        if (currentState == GameState.MENU) {
            renderMenu();
            //Jika user menekan ENTER, ubah state ke PLAYING dan mulai musik.
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                currentState = GameState.PLAYING;
                music.play();
            }
            return;
        }
        //Menjalankan logika game (gerak pacman, collision, timer, dll).
        update(Gdx.graphics.getDeltaTime());

        batch.begin();
        //Semua objek 2D harus digambar di antara batch.begin() dan batch.end().

        maze.render(batch);//menggambar labirin.
        for (Rectangle dot : dots) { batch.draw(dotTexture, dot.x, dot.y, dot.width, dot.height); }
        //menggambar semua titik makanan (dot).
        for (PowerUp powerUp : powerUps) { if (powerUp.isActive()) powerUp.render(batch); }
        //menggambar power-up aktif.
        pacman.render(batch);
        //menggambar pacman.
        for (Ghost ghost : ghosts) { ghost.render(batch); }
        //menggambar ghost

        font.draw(batch, "Score: " + score, 20, viewport.getWorldHeight() - 20);
        //mengambar score dan lokasinya
        font.draw(batch, "Lives: " + lives, viewport.getWorldWidth() - 150, viewport.getWorldHeight() - 20);
        //mengambar darah dan lokasinya


        //Menampilkan teks "GAME OVER" dan Menunggu tombol R ditekan untuk restart game
        if (currentState == GameState.GAME_OVER) {
            font.draw(batch, "GAME OVER", viewport.getWorldWidth() / 2 - 100, viewport.getWorldHeight() / 2 + 50);
            font.draw(batch, "Press R to restart", viewport.getWorldWidth() / 2 - 120, viewport.getWorldHeight() / 2);
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) restartGame();
            //Sama seperti GAME_OVER, tapi untuk kasus menang.
        } else if (currentState == GameState.GAME_WON) {
            font.draw(batch, "YOU WIN!", viewport.getWorldWidth() / 2 - 100, viewport.getWorldHeight() / 2 + 50);
            font.draw(batch, "Press R to restart", viewport.getWorldWidth() / 2 - 120, viewport.getWorldHeight() / 2);
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) restartGame();
        }
        //Selesai menggambar semua objek dalam frame ini.
        //Frame lalu ditampilkan ke layar.
        batch.end();
    }

    private void update(float delta) {
        //Kalau pemain menekan tombol R, game di-reset lewat restartGame()
        // dan fungsi update() langsung berhenti (return).
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartGame();
            return;
        }
        //Kalau game dalam status PLAYING, maka:
        if (currentState == GameState.PLAYING) {
            //Total waktu game ditambah.
            gameTime += delta;
            //Hitung mundur timer Power-Up
            powerUpSpawnTimer -= delta;
            //Setiap detik, timer dikurangi (delta).
            if (powerUpSpawnTimer <= 0) {
                //Kalau habis, spawn PowerUp baru.
                spawnRandomPowerUp();
                //Timer di-reset ke angka acak antara 8–15 detik.
                powerUpSpawnTimer = 8f + random.nextFloat() * 7f;
            }
            //Perbarui posisi dan logika dari Pacman, Ghost, dan PowerUp.
            pacman.update(delta);
            for (Ghost ghost : ghosts) { ghost.update(delta); }
            for (PowerUp powerUp : powerUps) { powerUp.update(delta); }
            //Cek apakah Pacman menyentuh dot, PowerUp, atau Ghost.
            checkDotCollisions();
            checkPowerUpCollisions();
            checkGhostCollisions();
            //Kalau semua titik (dot) habis, berarti menang.
            if (dots.size == 0) {
                currentState = GameState.GAME_WON;
            }
            //Jika dalam mode RESPWANING/hidup ulang
        } else if (currentState == GameState.RESPAWNING) {
            //Kalau Pacman mati dan sedang nunggu respawn:
            respawnTimer -= delta;
            //Tunggu beberapa detik (respawnTimer) → setelah itu reset posisi dan lanjut main lagi.
            if (respawnTimer <= 0) {
                resetPositionsAfterDeath();
                currentState = GameState.PLAYING;
            }
        }

        if (powerUpRemainingTime > 0) {
            //Hitung mundur durasi PowerUp (misalnya efek "makan Ghost").
            powerUpRemainingTime -= delta;
            if (powerUpRemainingTime <= 0 && musicScared != null) {
                musicScared.stop();
                musicScared.dispose();
                musicScared = null;

                // Reset ghost state jika perlu
                for (Ghost ghost : ghosts) {
                    ghost.setScared(false);
                }
            }
        }

    }

    private void checkGhostCollisions() {
        //Mengecek apakah permainan sedang berjalan
        if (currentState != GameState.PLAYING) return;
        //ni adalah bounding box (kotak pembatas) untuk Pacman.
        //Digunakan untuk mendeteksi tabrakan dengan Ghost.
        Rectangle pacmanBounds = new Rectangle(pacman.getPosition().x, pacman.getPosition().y, pacman.getSize().x, pacman.getSize().y);
        for (Ghost ghost : ghosts) {
            //Membuat bounding box untuk Ghost.
            Rectangle ghostBounds = new Rectangle(ghost.getPosition().x, ghost.getPosition().y, ghost.getSize().x, ghost.getSize().y);
            if (pacmanBounds.overlaps(ghostBounds)) {
               // Mengecek apakah kotak Ghost dan Pacman saling tumpang tindih (tabrakan).
                if (ghost.isScared()) {
                    ghost.respawn();
                    //Ghost akan dihapus dari map dan dikembalikan ke kandang.
                    score += 200;//tambah score 200
                } else if (!pacman.isPoweredUp()) {
                    //Pacman mati (animasi, suara).
                    pacman.die();
                    soundDie = Gdx.audio.newSound(Gdx.files.internal("Pac-Man Death - Sound Effect (HD).mp3"));
                    soundDie.play();
                    lives--;//darah berkurang
                    if (lives <= 0) {
                        currentState = GameState.GAME_OVER;// game kalah
                    } else {
                        currentState = GameState.RESPAWNING; //hidup kembali
                        respawnTimer = 1.5f; //1.5 detik untuk respawn
                    }
                }
            }
        }
    }

    private void resetPositionsAfterDeath() {
        pacman.respawn(); // Ganti ini dari sekedar set position
        for(Ghost ghost : ghosts) {
            ghost.respawn();
        }
        pacman.setPoweredUp(false, 0);
    }

    private void restartGame() {
        disposeCurrentGameAssets(); //// Bersihkan semua objek/aset yang sedang dipakai
        startGame(); //Mulai game dari awal
    }

    private void disposeCurrentGameAssets() {
        if (pacman != null) pacman.dispose(); //hapus objek Pacman dari memori (jika ada).
        if(ghosts != null) { for(Ghost g : ghosts) g.dispose(); } //semua Ghost dihapus satu per satu.
        if(powerUps != null) { for(PowerUp p : powerUps) p.dispose(); } //semua item power-up (Cherry, PowerFood, dll) dibuang.
        if(music != null) music.stop();
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
        if (music != null) music.dispose();
        if (musicScared != null) {musicScared.stop();musicScared.dispose();}
    }

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
        if (activePowerUpsCount >= 5) return;

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
            if (rand < 0.35f) powerUps.add(new Cherry(new Vector2(x, y)));  //35%
            else if (rand < 0.60f) powerUps.add(new Cherry2(new Vector2(x, y))); //25%
            else powerUps.add(new PowerFood(new Vector2(x, y))); //45%
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
                    pacman.setPoweredUp(true, 5f);
                    for (Ghost ghost : ghosts) ghost.setScared(true);
                    musicScared = Gdx.audio.newMusic(Gdx.files.internal("Pac man scared ghost sound.mp3"));
                    musicScared.setLooping(true);
                    musicScared.play();
                    powerUpRemainingTime = 5f;
                }
                powerUp.collect();
            }
        }
    }
}
