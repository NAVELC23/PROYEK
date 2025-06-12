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
    private Vector2 currentDirection; // Current movement direction of the ghost
    private Random random;

    // For grid-based movement
    private Vector2 targetTileCenter; // Current target tile center
    private float tileTolerance; // Tolerance to reach tile center before making new decision

    public Ghost(Vector2 startPosition, GhostType type, Pacman pacman, Maze maze) {
        super(startPosition, GhostType.getTexturePath(type), new Vector2(30, 30)); // Size 30x30
        this.type = type; //
        this.speed = type.getBaseSpeed(); //
        this.startPosition = startPosition; //
        this.pacman = pacman; //
        this.maze = maze; //
        this.isScared = false; //
        this.scaredTime = 0; //
        this.random = new Random(); //

        // Initialize initial direction (e.g., random or downwards), important to avoid being stuck at start
        this.currentDirection = getRandomInitialDirection(); //
        this.targetTileCenter = getCenterOfCurrentTile(); // Initialize target tile center

        // Tolerance to be considered at the "center" of a tile.
        // This should be small enough not to overshoot intersections, but large enough to handle float errors.
        this.tileTolerance = 5.0f; // 5 pixels from tile center
    }

    private Vector2 getRandomInitialDirection() {
        Vector2[] directions = {new Vector2(1, 0), new Vector2(-1, 0), new Vector2(0, 1), new Vector2(0, -1)};
        // Try to find a valid direction from the starting position
        for (Vector2 dir : directions) {
            Rectangle tempBounds = new Rectangle(position.x + dir.x * maze.getTileSize(), position.y + dir.y * maze.getTileSize(), size.x, size.y);
            if (!maze.collidesWithWall(tempBounds)) {
                return dir;
            }
        }
        return new Vector2(0, -1); // Fallback, e.g., downwards
    }

    @Override
    public void update(float delta) {
        if (isScared) { //
            scaredTime -= delta; //
            if (scaredTime <= 0) { //
                setScared(false); //
            }
            moveScared(delta); //
        } else {
            switch (type) { //
                case RED:
                    chasePacman(delta); //
                    break;
                case PINK:
                    // Pinky chases 4 tiles ahead of Pacman
                    chaseTarget(delta, getPinkyTarget()); //
                    break;
                case BLUE:
                    // Inky (Blue) target is complex, usually needs Blinky (Red)
                    // For simplicity, if Pacman is close, chase, if far, randomize
                    if (getCenter().dst(pacman.getCenter()) < maze.getTileSize() * 5) {
                        chasePacman(delta);
                    } else {
                        moveRandomly(delta);
                    }
                    break;
                case ORANGE:
                    // Clyde (Orange) chases Pacman if far, random if close
                    if (getCenter().dst(pacman.getCenter()) > maze.getTileSize() * 8) { // If distance > 8 tiles
                        chasePacman(delta); //
                    } else {
                        moveRandomly(delta); //
                    }
                    break;
            }
        }
    }

    // Helper: get ghost's center position
    private Vector2 getCenter() {
        return new Vector2(position.x + size.x / 2, position.y + size.y / 2);
    }

    // Helper: get the center of the tile the ghost is currently on
    private Vector2 getCenterOfCurrentTile() {
        float tileSize = maze.getTileSize();
        // Ensure floor division to get correct tile index
        float centerX = (float)Math.floor(getCenter().x / tileSize) * tileSize + tileSize / 2;
        float centerY = (float)Math.floor(getCenter().y / tileSize) / tileSize * tileSize + tileSize / 2;
        return new Vector2(centerX, centerY);
    }

    // Helper: check if ghost has reached the center of the target tile (intersection)
    private boolean isAtTargetTileCenter() {
        return getCenter().dst(targetTileCenter) < tileTolerance;
    }

    // Get valid directions (not hitting walls) from current position
    private List<Vector2> getValidDirections(Vector2 currentPos, Vector2 currentDir, float moveAmt) {
        List<Vector2> validDirs = new ArrayList<>();
        // Possible directions (right, left, up, down)
        Vector2[] possibleDirections = {new Vector2(1, 0), new Vector2(-1, 0), new Vector2(0, 1), new Vector2(0, -1)};

        for (Vector2 dir : possibleDirections) {
            // Avoid immediately reversing 180 degrees, unless there's no other choice.
            // Use dot product to check if directions are opposite (dot product = -1)
            if (currentDir.len() > 0 && dir.dot(currentDir) < -0.9f) { // Check if almost opposite
                continue;
            }

            // Calculate the center of the next tile the ghost would occupy
            float nextTileX = (float)Math.floor(currentPos.x / maze.getTileSize()) * maze.getTileSize() + dir.x * maze.getTileSize() + maze.getTileSize() / 2;
            float nextTileY = (float)Math.floor(currentPos.y / maze.getTileSize()) * maze.getTileSize() + dir.y * maze.getTileSize() + maze.getTileSize() / 2;

            // Create a bounding box centered in the target tile
            Rectangle tempBounds = new Rectangle(nextTileX - size.x / 2, nextTileY - size.y / 2, size.x, size.y);

            if (!maze.collidesWithWall(tempBounds)) { //
                validDirs.add(dir); //
            }
        }

        // If no valid directions except reversing 180 degrees, allow reversing
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

    // --- Ghost Movement Strategies ---

    private void moveRandomly(float delta) {
        float moveAmount = speed * delta;
        Vector2 currentCenter = getCenter();

        // Make a new directional decision only if at the tile center or if current direction hits a wall
        if (isAtTargetTileCenter() || maze.collidesWithWall(new Rectangle(position.x + currentDirection.x * moveAmount, position.y + currentDirection.y * moveAmount, size.x, size.y))) {
            targetTileCenter = getCenterOfCurrentTile(); // Update target center

            List<Vector2> validDirs = getValidDirections(targetTileCenter, currentDirection, moveAmount); //
            if (!validDirs.isEmpty()) { //
                currentDirection = validDirs.get(random.nextInt(validDirs.size())); //
            } else {
                currentDirection.set(0, 0); // Stuck, no valid direction
            }
        }

        // Move in the chosen direction
        Vector2 potentialNextPosition = new Vector2(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
        Rectangle nextBounds = new Rectangle(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);

        if (!maze.collidesWithWall(nextBounds)) { //
            position.set(potentialNextPosition); //
        } else {
            // If still colliding (e.g., due to floating point error or high speed),
            // try to find another valid direction.
            List<Vector2> validDirs = getValidDirections(currentCenter, currentDirection, moveAmount); //
            if (!validDirs.isEmpty()) { //
                currentDirection = validDirs.get(random.nextInt(validDirs.size())); // Choose a random valid direction
                // Try moving again with the new direction
                potentialNextPosition.set(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
                nextBounds.set(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);
                if (!maze.collidesWithWall(nextBounds)) { //
                    position.set(potentialNextPosition); //
                } else {
                    position.set(position); // Stay in place if still colliding
                }
            } else {
                position.set(position); // No valid direction, stuck
            }
        }
    }

    private void moveScared(float delta) {
        float moveAmount = speed * 0.5f * delta; // Slower speed when scared
        Vector2 currentCenter = getCenter();

        if (isAtTargetTileCenter() || maze.collidesWithWall(new Rectangle(position.x + currentDirection.x * moveAmount, position.y + currentDirection.y * moveAmount, size.x, size.y))) {
            targetTileCenter = getCenterOfCurrentTile(); //

            List<Vector2> validDirs = getValidDirections(targetTileCenter, currentDirection, moveAmount); //
            Vector2 escapeDirection = new Vector2(currentCenter.x - pacman.getCenter().x, currentCenter.y - pacman.getCenter().y).nor(); // Direction away from Pacman

            if (!validDirs.isEmpty()) { //
                Vector2 bestDir = null;
                float minAngle = Float.MAX_VALUE; // Find the smallest angle towards the escape direction

                for (Vector2 dir : validDirs) {
                    float angle = dir.angleRad(escapeDirection); // Angle between ghost's direction and escape direction
                    if (angle < minAngle) {
                        minAngle = angle;
                        bestDir = dir;
                    }
                }
                currentDirection = bestDir; //
            } else {
                currentDirection.set(0, 0); // Cannot move
            }
        }

        Vector2 potentialNextPosition = new Vector2(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
        Rectangle nextBounds = new Rectangle(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);

        if (!maze.collidesWithWall(nextBounds)) { //
            position.set(potentialNextPosition); //
        } else {
            List<Vector2> validDirs = getValidDirections(currentCenter, currentDirection, moveAmount); //
            if (!validDirs.isEmpty()) { //
                currentDirection = validDirs.get(random.nextInt(validDirs.size())); // Pick randomly from valid
                potentialNextPosition.set(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
                nextBounds.set(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);
                if (!maze.collidesWithWall(nextBounds)) { //
                    position.set(potentialNextPosition); //
                } else {
                    position.set(position); //
                }
            } else {
                position.set(position); //
            }
        }
    }

    private void chasePacman(float delta) {
        chaseTarget(delta, pacman.getCenter()); //
    }

    private void chaseTarget(float delta, Vector2 target) {
        float moveAmount = speed * delta; //
        Vector2 currentCenter = getCenter(); //

        if (isAtTargetTileCenter() || maze.collidesWithWall(new Rectangle(position.x + currentDirection.x * moveAmount, position.y + currentDirection.y * moveAmount, size.x, size.y))) {
            targetTileCenter = getCenterOfCurrentTile(); //

            List<Vector2> validDirs = getValidDirections(targetTileCenter, currentDirection, moveAmount); //
            Vector2 targetDirection = new Vector2(target.x - currentCenter.x, target.y - currentCenter.y).nor(); // Normalize direction to target

            if (!validDirs.isEmpty()) { //
                Vector2 bestDir = null;
                float minAngle = Float.MAX_VALUE; // Find smallest angle towards target

                for (Vector2 dir : validDirs) {
                    float angle = dir.angleRad(targetDirection); // Angle between ghost's direction and target direction
                    if (angle < minAngle) {
                        minAngle = angle;
                        bestDir = dir;
                    }
                }
                currentDirection = bestDir; //
            } else {
                currentDirection.set(0, 0); // Cannot move
            }
        }

        Vector2 potentialNextPosition = new Vector2(position).add(currentDirection.x * moveAmount, currentDirection.y * moveAmount);
        Rectangle nextBounds = new Rectangle(potentialNextPosition.x, potentialNextPosition.y, size.x, size.y);

        if (!maze.collidesWithWall(nextBounds)) { //
            position.set(potentialNextPosition); //
        } else {
            List<Vector2> validDirs = getValidDirections(currentCenter, currentDirection, moveAmount); //
            if (!validDirs.isEmpty()) { //
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
                if (!maze.collidesWithWall(nextBounds)) { //
                    position.set(potentialNextPosition); //
                } else {
                    position.set(position); //
                }
            } else {
                position.set(position); //
            }
        }
    }

    // Target for Pinky (4 tiles in front of Pacman)
    private Vector2 getPinkyTarget() {
        Vector2 pacmanPos = pacman.getPosition(); //
        Vector2 pacmanDir = pacman.getDirection(); // Assuming Pacman has getDirection()

        // If Pacman is not moving, Pinky can target a few tiles in front (e.g., 4 to the right)
        if (pacmanDir.epsilonEquals(0, 0)) {
            return new Vector2(pacmanPos.x + 4 * maze.getTileSize(), pacmanPos.y); //
        }
        return new Vector2(pacmanPos).add(pacmanDir.scl(4 * maze.getTileSize())); //
    }

    public void setScared(boolean scared) {
        this.isScared = scared; //
        if (scared) { //
            this.texture = new Texture("scaredGhost.png"); //
            scaredTime = 8f; // Scared duration time
        } else {
            this.texture = new Texture(GhostType.getTexturePath(type)); // Revert to original texture
        }
    }

    public boolean isScared() {
        return isScared; //
    }

    public void respawn() {
        position.set(startPosition); // Reset position to start
        setScared(false); // Not scared anymore
        currentDirection = getRandomInitialDirection(); // Reset direction on respawn
        targetTileCenter = getCenterOfCurrentTile(); // Reset target tile
    }
}
