package pacman.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;

public class Pacman extends Entity {
    private Texture leftTexture, rightTexture, upTexture, downTexture;
    private boolean poweredUp;
    private float powerUpTime;
    private Maze maze;
    private Vector2 currentDirection;
    private Vector2 nextDirection;
    private Vector2 targetPosition;
    private boolean isMoving;
    private float moveSpeed = 100f;

    // Tambahan
    private boolean isDead = false;
    private Vector2 spawnPosition;

    public Pacman(Vector2 startPosition, Maze maze) {
        super(startPosition, "pacmanRight.png", new Vector2(maze.getTileSize() * 0.85f, maze.getTileSize() * 0.85f));
        this.maze = maze;
        this.spawnPosition = new Vector2(startPosition);

        leftTexture = new Texture("pacmanLeft.png");
        rightTexture = new Texture("pacmanRight.png");
        upTexture = new Texture("pacmanUp.png");
        downTexture = new Texture("pacmanDown.png");
        poweredUp = false;
        powerUpTime = 0;

        currentDirection = new Vector2(1, 0);
        nextDirection = new Vector2(1, 0);
        targetPosition = new Vector2(position).add(currentDirection.cpy().scl(maze.getTileSize()));
        isMoving = true;
    }

    @Override
    public void update(float delta) {
        if (isDead) return;

        float stepSize = maze.getTileSize();

        // Input arah baru (hanya disimpan dulu)
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            nextDirection.set(-1, 0);
            texture = leftTexture;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            nextDirection.set(1, 0);
            texture = rightTexture;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            nextDirection.set(0, 1);
            texture = upTexture;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            nextDirection.set(0, -1);
            texture = downTexture;
        }

        if (isMoving) {
            Vector2 directionToTarget = new Vector2(targetPosition).sub(position).nor();
            float distanceToMove = moveSpeed * delta;
            float distanceRemaining = targetPosition.dst(position);

            if (distanceToMove >= distanceRemaining) {
                position.set(targetPosition);
                isMoving = false;
            } else {
                position.add(directionToTarget.scl(distanceToMove));
            }
        }

        if (!isMoving) {
            if (canMove(nextDirection)) {
                currentDirection.set(nextDirection);
            }

            if (canMove(currentDirection)) {
                targetPosition.set(position).add(currentDirection.cpy().scl(stepSize));
                isMoving = true;
            }
        }

        if (poweredUp) {
            powerUpTime -= delta;
            if (powerUpTime <= 0) {
                poweredUp = false;
                texture = rightTexture;
            }
        }
    }

    private boolean canMove(Vector2 direction) {
        Vector2 target = new Vector2(position).add(direction.cpy().scl(maze.getTileSize()));
        Rectangle bounds = new Rectangle(target.x, target.y, size.x, size.y);
        return !maze.collidesWithWall(bounds);
    }

    public Vector2 getCenter() {
        return new Vector2(position.x + size.x / 2, position.y + size.y / 2);
    }

    public Vector2 getDirection() {
        return currentDirection;
    }

    public void setPoweredUp(boolean poweredUp, float duration) {
        this.poweredUp = poweredUp;
        this.powerUpTime = duration;
        if (poweredUp) {
            texture = new Texture("PACMAN GELAP.png");
        } else {
            texture = rightTexture;
        }
    }

    public boolean isPoweredUp() {
        return poweredUp;
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, size.x, size.y);
    }

    public void resetDirection() {
        this.currentDirection.set(1, 0);
        this.texture = this.rightTexture;
    }

    // Tambahan
    public void die() {
        isDead = true;
        isMoving = false;
    }

    public void respawn() {
        isDead = false;
        position.set(spawnPosition);
        currentDirection.set(1, 0);
        nextDirection.set(1, 0);
        targetPosition.set(position).add(currentDirection.cpy().scl(maze.getTileSize()));
        texture = rightTexture;
        isMoving = true;
    }

    @Override
    public void dispose() {
        super.dispose();
        leftTexture.dispose();
        rightTexture.dispose();
        upTexture.dispose();
        downTexture.dispose();
    }
}
