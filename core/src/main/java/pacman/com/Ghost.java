package pacman.com;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle; // Import Rectangle

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ghost extends Entity {
    private GhostType type;
    private float speed;
    private Vector2 startPosition;
    private boolean isScared;
    private float scaredTime;
    private Pacman pacman;
    private Maze maze;
    private Vector2 currentDirection; // Arah pergerakan hantu saat ini
    private Random random;

    // Untuk pergerakan berbasis grid
    private Vector2 targetTileCenter; // Pusat tile tujuan saat ini
    private float tileTolerance; // Toleransi untuk mencapai pusat tile sebelum membuat keputusan baru

    public Ghost(Vector2 startPosition, GhostType type, Pacman pacman, Maze maze) {
        super(startPosition, GhostType.getTexturePath(type), new Vector2(30, 30)); // Ukuran 30x30
        this.type = type;
        this.speed = type.getBaseSpeed();
        this.startPosition = startPosition;
        this.pacman = pacman;
        this.maze = maze;
        this.isScared = false;
        this.scaredTime = 0;
        this.random = new Random();

        // Inisialisasi arah awal (misal: acak atau ke bawah), penting untuk menghindari stuck di awal
        this.currentDirection = getRandomInitialDirection();
        this.targetTileCenter = getCenterOfCurrentTile(); // Inisialisasi target tile

        // Toleransi untuk dianggap berada di "tengah" tile.
        // Ini harus cukup kecil agar tidak melewati persimpangan, tapi cukup besar untuk mengatasi float error.
        this.tileTolerance = 5.0f; // 5 piksel dari pusat tile
    }

    private Vector2 getRandomInitialDirection() {
        Vector2[] directions = {new Vector2(1, 0), new Vector2(-1, 0), new Vector2(0, 1), new Vector2(0, -1)};
        // Coba cari arah yang valid dari posisi awal
        for (Vector2 dir : directions) {
            Rectangle tempBounds = new Rectangle(position.x + dir.x * maze.getTileSize(), position.y + dir.y * maze.getTileSize(), size.x, size.y);
            if (!maze.collidesWithWall(tempBounds)) {
                return dir;
            }
        }
        return new Vector2(0, -1); // Fallback, misalnya ke bawah
    }

    @Override
    public void update(float delta) {
        if (isScared) {
            scaredTime -= delta;
            if (scaredTime <= 0) {
                setScared(false);
            }
            moveScared(delta);
        } else {
            switch (type) {
                case RED:
                    chasePacman(delta);
                    break;
                case PINK:
                    // Pinky mengejar 4 tile di depan Pacman
                    chaseTarget(delta, getPinkyTarget());
                    break;
                case BLUE:
                    // Inky (Blue) target kompleks, biasanya butuh Blinky (Red)
                    // Untuk kesederhanaan, jika Pacman dekat, kejar, jika jauh, acak
                    if (getCenter().dst(pacman.getCenter()) < maze.getTileSize() * 5) {
                        chasePacman(delta);
                    } else {
                        moveRandomly(delta);
                    }
                    break;
                case ORANGE:
                    // Clyde (Orange) mengejar Pacman jika jauh, acak jika dekat
                    if (getCenter().dst(pacman.getCenter()) > maze.getTileSize() * 8) { // Jika jarak > 8 tile
                        chasePacman(delta);
                    } else {
                        moveRandomly(delta);
                    }
                    break;
            }
        }
    }

    // Helper: mendapatkan posisi tengah hantu
    private Vector2 getCenter() {
        return new Vector2(position.x + size.x / 2, position.y + size.y / 2);
    }

    // Helper: mendapatkan pusat tile tempat hantu berada
    private Vector2 getCenterOfCurrentTile() {
        float tileSize = maze.getTileSize();
        // Pastikan pembulatan ke bawah untuk mendapatkan indeks tile yang benar
        float centerX = (float)Math.floor(getCenter().x / tileSize) * tileSize + tileSize / 2;
        float centerY = (float)Math.floor(getCenter().y / tileSize) * tileSize + tileSize / 2;
        return new Vector2(centerX, centerY);
    }

    // Helper: memeriksa apakah hantu sudah di tengah tile tujuan (persimpangan)
    private boolean isAtTargetTileCenter() {
        return getCenter().dst(targetTileCenter) < tileTolerance;
    }

    // Mendapatkan arah-arah valid (tidak menabrak dinding) dari posisi saat ini
    private List<Vector2> getValidDirections(Vector2 currentPos, Vector2 currentDir, float moveAmt) {
        List<Vector2> validDirs = new ArrayList<>();
        // Arah yang mungkin (kanan, kiri, atas, bawah)
        Vector2[] possibleDirections = {new Vector2(1, 0), new Vector2(-1, 0), new Vector2(0, 1), new Vector2(0, -1)};

        for (Vector2 dir : possibleDirections) {
            // Hindari berbalik arah 180 derajat secara instan, kecuali jika tidak ada pilihan lain.
            // Gunakan dot product untuk memeriksa jika arahnya berlawanan (dot product = -1)
            if (currentDir.len() > 0 && dir.dot(currentDir) < -0.9f) { // Cek jika hampir berlawanan
                continue;
            }

            // Hitung posisi tengah tile berikutnya yang akan diisi oleh hantu
            // Ini untuk memeriksa apakah tile *itu sendiri* adalah dinding
            float nextTileX = (float)Math.floor(currentPos.x / maze.getTileSize()) * maze.getTileSize() + dir.x * maze.getTileSize() + maze.getTileSize() / 2;
            float nextTileY = (float)Math.floor(currentPos.y / maze.getTileSize()) * maze.getTileSize() + dir.y * maze.getTileSize() + maze.getTileSize() / 2;

            // Buat bounding box di tengah tile yang akan dituju
            // Ukuran bounding box hantu (30x30), jadi offset 15 dari tengah tile
            Rectangle tempBounds = new Rectangle(nextTileX - size.x / 2, nextTileY - size.y / 2, size.x, size.y);

            if (!maze.collidesWithWall(tempBounds)) {
                validDirs.add(dir);
            }
        }

        // Jika tidak ada arah valid kecuali berbalik 180 derajat, izinkan berbalik arah
        if (validDirs.isEmpty() && currentDir.len() > 0) {
            Vector2 reverseDir = new Vector2(-currentDir.x, -currentDir.y);
            float nextTileX = (float)Math.floor(currentPos.x / maze.getTileSize()) * maze.getTileSize() + reverseDir.x * maze.getTileSize() + maze.getTileSize() / 2;
            float nextTileY = (float)Math.floor(currentPos.y / maze.getTileSize()) * maze.getTileSize() + reverseDir.y * maze.getTileSize() + maze.getTileSize() / 2;
            Rectangle tempBounds = new Rectangle(nextTileX - size.x / 2, nextTileY - size.y / 2, size.x, size.y);
            if (!maze.collidesWithWall(tempBounds)) {
                validDirs.add(reverseDir);
            }
        }
        return validDirs;
    }

    // --- Strategi Pergerakan Hantu ---

    private void moveRandomly(float delta) {
        float moveAmount = speed * delta;
        Vector2 currentCenter = getCenter();

        // Ambil keputusan arah baru hanya jika sudah di tengah tile
        // atau jika arah saat ini menabrak dinding
        if (isAtTargetTileCenter() || maze.collidesWithWall(new Rectangle(position.x + currentDirection.x * moveAmount, position.y + currentDirection.y * moveAmount, size.x, size.y))) {
            targetTileCenter = getCenterOfCurrentTile(); // Perbarui target center

            List<Vector2> validDirs = getValidDirections(targetTileCenter, currentDirection, moveAmount);
            if (!validDirs.isEmpty()) {
                currentDirection = validDirs.get(random.nextInt(validDirs.size()));
            } else {
                currentDirection.set(0, 0); // Stuck, tidak ada arah valid
            }
        }

        // Bergerak ke arah yang dipilih
        Vector2 potentialNextPosition = new Vector2(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
        Rectangle nextBounds = new Rectangle(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);

        if (!maze.collidesWithWall(nextBounds)) {
            position.set(potentialNextPosition);
        } else {
            // Jika masih menabrak (misal karena floating point error atau kecepatan terlalu tinggi),
            // coba cari arah yang masih valid.
            List<Vector2> validDirs = getValidDirections(currentCenter, currentDirection, moveAmount);
            if (!validDirs.isEmpty()) {
                currentDirection = validDirs.get(random.nextInt(validDirs.size()));
                // Coba bergerak lagi dengan arah baru
                potentialNextPosition.set(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
                nextBounds.set(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);
                if (!maze.collidesWithWall(nextBounds)) {
                    position.set(potentialNextPosition);
                } else {
                    position.set(position); // Tetap di tempat jika masih menabrak
                }
            } else {
                position.set(position); // Tidak ada arah valid, stuck
            }
        }
    }

    private void moveScared(float delta) {
        float moveAmount = speed * 0.5f * delta; // Kecepatan lambat saat scared
        Vector2 currentCenter = getCenter();

        if (isAtTargetTileCenter() || maze.collidesWithWall(new Rectangle(position.x + currentDirection.x * moveAmount, position.y + currentDirection.y * moveAmount, size.x, size.y))) {
            targetTileCenter = getCenterOfCurrentTile();

            List<Vector2> validDirs = getValidDirections(targetTileCenter, currentDirection, moveAmount);
            Vector2 escapeDirection = new Vector2(currentCenter.x - pacman.getCenter().x, currentCenter.y - pacman.getCenter().y).nor();

            if (!validDirs.isEmpty()) {
                Vector2 bestDir = null;
                float minAngle = Float.MAX_VALUE; // Cari sudut terkecil ke arah escape

                for (Vector2 dir : validDirs) {
                    float angle = dir.angleRad(escapeDirection); // Sudut antara arah hantu dan arah menjauhi Pacman
                    if (angle < minAngle) {
                        minAngle = angle;
                        bestDir = dir;
                    }
                }
                currentDirection = bestDir;
            } else {
                currentDirection.set(0, 0); // Tidak bisa bergerak
            }
        }

        Vector2 potentialNextPosition = new Vector2(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
        Rectangle nextBounds = new Rectangle(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);

        if (!maze.collidesWithWall(nextBounds)) {
            position.set(potentialNextPosition);
        } else {
            List<Vector2> validDirs = getValidDirections(currentCenter, currentDirection, moveAmount);
            if (!validDirs.isEmpty()) {
                currentDirection = validDirs.get(random.nextInt(validDirs.size())); // Pilih acak dari valid
                potentialNextPosition.set(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
                nextBounds.set(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);
                if (!maze.collidesWithWall(nextBounds)) {
                    position.set(potentialNextPosition);
                } else {
                    position.set(position);
                }
            } else {
                position.set(position);
            }
        }
    }

    private void chasePacman(float delta) {
        chaseTarget(delta, pacman.getCenter());
    }

    private void chaseTarget(float delta, Vector2 target) {
        float moveAmount = speed * delta;
        Vector2 currentCenter = getCenter();

        if (isAtTargetTileCenter() || maze.collidesWithWall(new Rectangle(position.x + currentDirection.x * moveAmount, position.y + currentDirection.y * moveAmount, size.x, size.y))) {
            targetTileCenter = getCenterOfCurrentTile();

            List<Vector2> validDirs = getValidDirections(targetTileCenter, currentDirection, moveAmount);
            Vector2 targetDirection = new Vector2(target.x - currentCenter.x, target.y - currentCenter.y).nor();

            if (!validDirs.isEmpty()) {
                Vector2 bestDir = null;
                float minAngle = Float.MAX_VALUE; // Cari sudut terkecil ke arah target

                for (Vector2 dir : validDirs) {
                    float angle = dir.angleRad(targetDirection); // Sudut antara arah hantu dan arah target
                    if (angle < minAngle) {
                        minAngle = angle;
                        bestDir = dir;
                    }
                }
                currentDirection = bestDir;
            } else {
                currentDirection.set(0, 0); // Tidak bisa bergerak
            }
        }

        Vector2 potentialNextPosition = new Vector2(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
        Rectangle nextBounds = new Rectangle(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);

        if (!maze.collidesWithWall(nextBounds)) {
            position.set(potentialNextPosition);
        } else {
            List<Vector2> validDirs = getValidDirections(currentCenter, currentDirection, moveAmount);
            if (!validDirs.isEmpty()) {
                Vector2 bestDir = null;
                float minAngle = Float.MAX_VALUE;

                for (Vector2 dir : validDirs) {
                    float angle = dir.angleRad(new Vector2(target).sub(currentCenter));
                    if (angle < minAngle) {
                        minAngle = angle;
                        bestDir = dir;
                    }
                }
                currentDirection = bestDir;
                potentialNextPosition.set(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
                nextBounds.set(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);
                if (!maze.collidesWithWall(nextBounds)) {
                    position.set(potentialNextPosition);
                } else {
                    position.set(position);
                }
            } else {
                position.set(position);
            }
        }
    }

    // Target untuk Pinky (4 tile di depan Pacman)
    private Vector2 getPinkyTarget() {
        Vector2 pacmanPos = pacman.getPosition();
        Vector2 pacmanDir = pacman.getDirection(); // Asumsi Pacman punya getDirection()

        // Jika Pacman tidak bergerak, Pinky bisa target beberapa tile di depannya (misal: 4 ke kanan)
        if (pacmanDir.epsilonEquals(0, 0)) {
            return new Vector2(pacmanPos.x + 4 * maze.getTileSize(), pacmanPos.y);
        }
        return new Vector2(pacmanPos).add(pacmanDir.scl(4 * maze.getTileSize()));
    }


    public void setScared(boolean scared) {
        this.isScared = scared;
        if (scared) {
            this.texture = new Texture("scaredGhost.png");
            scaredTime = 8f; // Durasi scared time
        } else {
            this.texture = new Texture(GhostType.getTexturePath(type));
        }
    }

    public boolean isScared() {
        return isScared;
    }

    public void respawn() {
        position.set(startPosition);
        setScared(false);
        currentDirection = getRandomInitialDirection(); // Reset arah saat respawn
        targetTileCenter = getCenterOfCurrentTile(); // Reset target tile
    }
}
