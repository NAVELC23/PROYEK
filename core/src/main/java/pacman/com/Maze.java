package pacman.com;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Maze {
    private Texture wallTexture;
    private List<Rectangle> walls;
    private float width;
    private float height;
    private float tileSize;

    public Maze(float width, float height) {
        this.width = width;
        this.height = height;
        this.tileSize = 40f;
        this.wallTexture = new Texture("wall.png");
        this.walls = new ArrayList<>();
        initializeWalls();
    }

    private void initializeWalls() {
        // Create a simple 18x20 maze (adjust as needed)
        // Outer walls
        for (int x = 0; x < width; x += tileSize) {
            addWall(x, 0);
            addWall(x, height - tileSize);
        }
        for (int y = Math.round(tileSize); y < height - Math.round(tileSize); y += Math.round(tileSize)) {
            addWall(0, y);
            addWall(width - tileSize, y);
        }

        // Inner walls - create a simple pacman maze pattern
        // Add more walls as needed for your maze design
        for (int x = 3; x < 15; x++) {
            if (x != 9) addWall(x * tileSize, 10 * tileSize);
        }
        // Add more maze elements here...
    }

    private void addWall(float x, float y) {
        walls.add(new Rectangle(x, y, tileSize, tileSize));
    }

    public void render(SpriteBatch batch) {
        for (Rectangle wall : walls) {
            batch.draw(wallTexture, wall.x, wall.y, wall.width, wall.height);
        }
    }

    public boolean isWallAt(float x, float y) {
        for (Rectangle wall : walls) {
            if (wall.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getTileSize() {
        return tileSize;
    }

    public void dispose() {
        wallTexture.dispose();
    }
}
