package pacman.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle; // Import Rectangle

public class Pacman extends Entity {
    private Texture leftTexture;
    private Texture rightTexture;
    private Texture upTexture;
    private Texture downTexture;
    private float speed;
    private boolean poweredUp;
    private float powerUpTime;
    private Maze maze;
    private Vector2 currentDirection; // Menambahkan currentDirection untuk Pacman

    public Pacman(Vector2 startPosition, Maze maze) {
        super(startPosition, "pacmanRight.png", new Vector2(30, 30)); // Ukuran 30x30
        leftTexture = new Texture("pacmanLeft.png");
        rightTexture = new Texture("pacmanRight.png");
        upTexture = new Texture("pacmanUp.png");
        downTexture = new Texture("pacmanDown.png");
        speed = 150f;
        poweredUp = false;
        powerUpTime = 0;
        this.maze = maze;
        this.currentDirection = new Vector2(1, 0); // Default arah kanan
    }

    @Override
    public void update(float delta) {
        Vector2 desiredMove = new Vector2(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            desiredMove.x = -speed * delta;
            texture = leftTexture;
            currentDirection.set(-1, 0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            desiredMove.x = speed * delta;
            texture = rightTexture;
            currentDirection.set(1, 0);
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            desiredMove.y = speed * delta;
            texture = upTexture;
            currentDirection.set(0, 1);
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            desiredMove.y = -speed * delta;
            texture = downTexture;
            currentDirection.set(0, -1);
        } else {
            // Jika tidak ada input, Pac-Man berhenti
            currentDirection.set(0, 0);
        }

        // Coba bergerak secara horizontal
        Vector2 potentialX = new Vector2(position.x + desiredMove.x, position.y);
        Rectangle boundsX = new Rectangle(potentialX.x, potentialX.y, size.x, size.y);
        if (!maze.collidesWithWall(boundsX)) {
            position.x = potentialX.x;
        }

        // Coba bergerak secara vertikal
        Vector2 potentialY = new Vector2(position.x, position.y + desiredMove.y);
        Rectangle boundsY = new Rectangle(potentialY.x, potentialY.y, size.x, size.y);
        if (!maze.collidesWithWall(boundsY)) {
            position.y = potentialY.y;
        }

        // Menjaga Pacman tetap di dalam batas maze (Tambahan keamanan)
        position.x = Math.max(0, Math.min(maze.getWidth() - size.x, position.x));
        position.y = Math.max(0, Math.min(maze.getHeight() - size.y, position.y));

        if (poweredUp) {
            powerUpTime -= delta;
            if (powerUpTime <= 0) {
                poweredUp = false;
                texture = rightTexture; // Kembali ke tekstur normal
            }
        }
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
        // Atur kembali arah internal ke kanan (atau arah default lainnya)
        this.currentDirection.set(1, 0);
        // Atur kembali tekstur yang digunakan agar cocok dengan arah
        this.texture = this.rightTexture;
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
