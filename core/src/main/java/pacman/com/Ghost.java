package pacman.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ghost extends Entity {
    private final GhostType type;
    private final float speed;
    private final Vector2 startPosition;
    private boolean isScared;
    private float scaredTime;
    private final Pacman pacman;
    private final Maze maze;
    private Vector2 currentDirection;
    private final Random random;

    private final Texture normalTexture;
    private final Texture scaredTexture;

    public Ghost(Vector2 startPosition, GhostType type, Pacman pacman, Maze maze) {
        super(startPosition, GhostType.getTexturePath(type), new Vector2(30, 30));

        this.normalTexture = new Texture(GhostType.getTexturePath(type));
        this.scaredTexture = new Texture("scaredGhost.png");
        this.texture = normalTexture;

        this.startPosition = new Vector2(startPosition);
        this.type = type;
        this.pacman = pacman;
        this.maze = maze;
        this.speed = type.getBaseSpeed();
        this.random = new Random();
        this.currentDirection = new Vector2(0, -1);
    }

    @Override
    public void update(float delta) {
        if (isScared) {
            // Jika takut, lari kembali ke markas
            scaredTime -= delta;
            if (scaredTime <= 0) {
                setScared(false);
            }
            move(delta, startPosition, true); // true = lari (fleeing)
        } else {
            // --- INI LOGIKA UTAMA YANG ANDA INGINKAN ---
            float detectionRange = getDetectionRange();
            float distanceToPacman = getCenter().dst(pacman.getCenter());

            if (distanceToPacman < detectionRange) {
                // Jika Pacman di dalam jangkauan, KEJAR!
                System.out.println(type + " MENGEJAR PACMAN"); // Debugging
                move(delta, pacman.getCenter(), false); // false = kejar (not fleeing)
            } else {
                // Jika Pacman jauh, PATROLI/BERKELIARAN.
                System.out.println(type + " BERPATROLI"); // Debugging
                patrol(delta);
            }
        }
    }

    private void move(float delta, Vector2 target, boolean isFleeing) {
        if (isAtTileCenter() || isAboutToHitWall()) {
            List<Vector2> validDirections = getValidDirections();
            if (!validDirections.isEmpty()) {
                currentDirection.set(chooseBestDirection(validDirections, target, isFleeing));
            }
        }
        position.mulAdd(currentDirection, speed * delta);
    }

    // Metode BARU untuk patroli/berkeliaran
    private void patrol(float delta) {
        // Logikanya adalah memilih arah acak di setiap persimpangan
        if (isAtTileCenter() || isAboutToHitWall()) {
            List<Vector2> validDirections = getValidDirections();
            if (!validDirections.isEmpty()) {
                // Pilih salah satu arah secara acak dari yang tersedia
                currentDirection.set(validDirections.get(random.nextInt(validDirections.size())));
            }
        }
        position.mulAdd(currentDirection, speed * delta);
    }

    private float getDetectionRange() {
        // Beri setiap hantu jangkauan deteksi yang berbeda untuk kepribadian unik
        float tileSize = maze.getTileSize();
        switch (type) {
            case RED: return tileSize * 8;    // Merah paling peka
            case PINK: return tileSize * 6;   // Pink cukup peka
            case BLUE: return tileSize * 7;   // Biru normal
            case ORANGE: return tileSize * 5; // Oranye paling tidak peka
            default: return tileSize * 6;
        }
    }

    private Vector2 chooseBestDirection(List<Vector2> directions, Vector2 target, boolean isFleeing) {
        Vector2 bestDirection = directions.get(0);
        float bestMetric = -1;

        for (Vector2 dir : directions) {
            Vector2 nextTilePos = new Vector2(getCenter()).mulAdd(dir, maze.getTileSize());
            float distanceToTarget = nextTilePos.dst2(target);

            if (isFleeing) { // Cari jarak terjauh
                if (distanceToTarget > bestMetric) {
                    bestMetric = distanceToTarget;
                    bestDirection = dir;
                }
            } else { // Cari jarak terdekat
                if (bestMetric == -1 || distanceToTarget < bestMetric) {
                    bestMetric = distanceToTarget;
                    bestDirection = dir;
                }
            }
        }
        return bestDirection;
    }

    // Semua metode bantuan di bawah ini biarkan seperti sebelumnya karena sudah bagus
    private List<Vector2> getValidDirections() {
        List<Vector2> validDirs = new ArrayList<>();
        Vector2[] possibleDirections = {new Vector2(1, 0), new Vector2(-1, 0), new Vector2(0, 1), new Vector2(0, -1)};
        Vector2 oppositeDirection = new Vector2(-currentDirection.x, -currentDirection.y);

        for (Vector2 dir : possibleDirections) {
            if (dir.equals(oppositeDirection) && countValidExits() > 1) {
                continue;
            }
            if (isPathClear(dir)) {
                validDirs.add(dir);
            }
        }
        if (validDirs.isEmpty() && isPathClear(oppositeDirection)) {
            validDirs.add(oppositeDirection);
        }
        return validDirs;
    }

    private int countValidExits() {
        int count = 0;
        Vector2[] possibleDirections = {new Vector2(1, 0), new Vector2(-1, 0), new Vector2(0, 1), new Vector2(0, -1)};
        for (Vector2 dir : possibleDirections) {
            if (isPathClear(dir)) {
                count++;
            }
        }
        return count;
    }

    private boolean isPathClear(Vector2 direction) {
        if(direction.isZero()) return false;
        Vector2 checkPos = new Vector2(position).mulAdd(direction, 2.0f);
        Rectangle checkBounds = new Rectangle(checkPos.x, checkPos.y, size.x, size.y);
        return !maze.collidesWithWall(checkBounds);
    }

    private boolean isAtTileCenter() {
        float tolerance = speed * Gdx.graphics.getDeltaTime() / 2f;
        float tileSize = maze.getTileSize();
        Vector2 center = getCenter();
        float tileCenterX = (float) (Math.floor(center.x / tileSize) * tileSize + tileSize / 2);
        float tileCenterY = (float) (Math.floor(center.y / tileSize) * tileSize + tileSize / 2);
        return center.dst(tileCenterX, tileCenterY) < tolerance;
    }

    private boolean isAboutToHitWall() {
        if(currentDirection.isZero()) return true;
        return !isPathClear(currentDirection);
    }

    public Vector2 getCenter() {
        return new Vector2(position.x + size.x / 2, position.y + size.y / 2);
    }

    // ... Metode setScared, isScared, respawn, dispose ...
    public void setScared(boolean scared) {
        this.isScared = scared;
        this.texture = scared ? scaredTexture : normalTexture;
        if (scared) {
            this.scaredTime = 10f;
        }
    }

    public boolean isScared() {
        return isScared;
    }

    public void respawn() {
        position.set(startPosition);
        setScared(false);
        currentDirection.set(0, -1);
    }

    @Override
    public void dispose() {
        normalTexture.dispose();
        scaredTexture.dispose();
    }

}
