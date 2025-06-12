package pacman.com;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class Maze {
    private Texture wallTexture;
    private List<Rectangle> walls;
    private float width;
    private float height;
    private float tileSize;

    // Define maze dimensions: 18 columns x 19 rows
    private final int TOTAL_COLS = 18; // Total columns (including walls)
    private final int TOTAL_ROWS = 19; // Total rows (including walls)

    public Maze(float initialWidth, float initialHeight) { // These parameters are not used for maze size
        this.tileSize = 40f; // Tile size, 40x40 pixels
        this.wallTexture = new Texture("wall.png"); //
        this.walls = new ArrayList<>(); //

        // Set maze width and height based on TOTAL_COLS and TOTAL_ROWS
        this.width = TOTAL_COLS * tileSize; //
        this.height = TOTAL_ROWS * tileSize; //

        initializeWalls(); //
    }

    private void initializeWalls() {
        walls.clear();

        // Build the outer wall frame
        // Bottom row (row 0)
        for (int x = 0; x < TOTAL_COLS; x++) {
            addWall(x * tileSize, 0 * tileSize);
        }

        // Top row (row TOTAL_ROWS - 1)
        for (int x = 0; x < TOTAL_COLS; x++) {
            addWall(x * tileSize, (TOTAL_ROWS - 1) * tileSize);
        }

        // Leftmost column (col 0), from row 1 to (TOTAL_ROWS - 2)
        for (int y = 1; y < TOTAL_ROWS - 1; y++) {
            addWall(0 * tileSize, y * tileSize);
        }

        // Rightmost column (col TOTAL_COLS - 1), from row 1 to (TOTAL_ROWS - 2)
        for (int y = 1; y < TOTAL_ROWS - 1; y++) {
            addWall((TOTAL_COLS - 1) * tileSize, y * tileSize);
        }

        // === Add small 'U' shaped obstacles ===
        // Coordinates are tile positions (col, row). Remember Y=0 is bottom.
        // Ensure they don't overlap with outer walls or each other.

        // U facing up (around bottom-center)
        addUObstacle(5, 3, "up"); // U at col 5, row 3, open upwards

        // U facing down (around top-center)
        addUObstacle(TOTAL_COLS - 8, TOTAL_ROWS - 5, "down"); // U at col 10, row 14, open downwards

        // U facing left (on the right side)
        addUObstacle(TOTAL_COLS - 4, 7, "left"); // U at col 14, row 7, open to the left

        // U facing right (on the left side)
        addUObstacle(3, TOTAL_ROWS - 10, "right"); // U at col 3, row 9, open to the right
    }

    // Helper method to add a 'U' obstacle
    private void addUObstacle(int startCol, int startRow, String orientation) {
        // These coordinates are based on tile index (col, row)
        // Remember screen Y is inverted compared to typical array row indexing

        // Convert to pixel coordinates
        float pixelX = startCol * tileSize;
        float pixelY = startRow * tileSize;

        switch (orientation) {
            case "up": // Open upwards (bottom part is wall)
                addWall(pixelX, pixelY);
                addWall(pixelX + tileSize, pixelY);
                addWall(pixelX + 2 * tileSize, pixelY);
                addWall(pixelX, pixelY + tileSize);
                addWall(pixelX + 2 * tileSize, pixelY + tileSize);
                break;
            case "down": // Open downwards (top part is wall)
                addWall(pixelX, pixelY + tileSize);
                addWall(pixelX + tileSize, pixelY + tileSize);
                addWall(pixelX + 2 * tileSize, pixelY + tileSize);
                addWall(pixelX, pixelY);
                addWall(pixelX + 2 * tileSize, pixelY);
                break;
            case "left": // Open to the left (right part is wall)
                addWall(pixelX + tileSize, pixelY);
                addWall(pixelX + tileSize, pixelY + tileSize);
                addWall(pixelX + tileSize, pixelY + 2 * tileSize);
                addWall(pixelX, pixelY);
                addWall(pixelX, pixelY + 2 * tileSize);
                break;
            case "right": // Open to the right (left part is wall)
                addWall(pixelX, pixelY);
                addWall(pixelX, pixelY + tileSize);
                addWall(pixelX, pixelY + 2 * tileSize);
                addWall(pixelX + tileSize, pixelY);
                addWall(pixelX + tileSize, pixelY + 2 * tileSize);
                break;
            default:
                // Handle error or default behavior
                break;
        }
    }

    private void addWall(float x, float y) {
        walls.add(new Rectangle(x, y, tileSize, tileSize));
    }

    public void render(SpriteBatch batch) {
        for (Rectangle wall : walls) {
            batch.draw(wallTexture, wall.x, wall.y, wall.width, wall.height);
        }
    }

    public boolean collidesWithWall(Rectangle boundingBox) {
        for (Rectangle wall : walls) {
            if (boundingBox.overlaps(wall)) {
                return true;
            }
        }
        return false;
    }

    // Method to check if a point is within a wall
    public boolean isWallAt(float x, float y) {
        // Create a small rectangle at the checked point
        Rectangle checkRect = new Rectangle(x, y, 1, 1);
        for (Rectangle wall : walls) {
            if (wall.overlaps(checkRect)) {
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

    public List<Rectangle> getWalls() {
        return walls;
    }

    public void dispose() {
        if (wallTexture != null) { // Ensure texture exists before disposing
            wallTexture.dispose();
        }
    }
}
