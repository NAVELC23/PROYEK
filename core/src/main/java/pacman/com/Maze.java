package pacman.com;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Maze {
    private final Texture wallTexture;
    private final List<Rectangle> walls;
    private final float width;
    private final float height;
    private final float tileSize;
    private String[] currentLayout;

    private final List<String[]> layouts;
    private int currentLayoutIndex;
    // --- DESAIN LABIRIN FINAL (19x22) ---
    private final String[] originalLayout = {
        "WWWWWWWWWWWWWWWWWWW",
        "W........W........W",
        "W.WW.WWW.W.WWW.WW.W",
        "W*WW.WWW.W.WWW.WW*W",
        "W.................W",
        "W.WW.W.WWWWW.W.WW.W",
        "W....W...W...W....W",
        "WWWW.WWW W WWW.WWWW",
        "WWWW.W       W.WWWW",
        "WWWW.W WGGGW W.WWWW", // G = Ghost house
        "WWWW.W W...W W.WWWW",
        "WWWW.W WWWWW W.WWWW",
        "WWWW.W       W.WWWW",
        "WWWW.WWW W WWW.WWWW",
        "W........W........W",
        "W.WW.WWW.W.WWW.WW.W",
        "W*...............*W",
        "WW.W.W.WWWWW.W.W.WW",
        "W..W.W...W...W.W..W",
        "W.WW...WWWWW...WW.W",
        "W.................W",
        "WWWWWWWWWWWWWWWWWWW"
    };

    private final String[] layoutA = {
        "WWWWWWWWWWWWWWWWWWW",
        "W.................W",
        "W.WW.W.WWWWW.W.WW.W",
        "W*...W...W...W...*W",
        "W.WW.W.W W W.W.WW.W",
        "W....... . .......W",
        "WWWW.WWWW WWWW.WWWW",
        "W....W       W....W",
        "W.WW.W WW.WW W.WW.W",
        "W.WW.W WGGGW W.WW.W",
        "W.WW.W W...W W.WW.W",
        "W.WW.W WWWWW W.WW.W",
        "W....W       W....W",
        "WWWW.WWW   WWW.WWWW",
        "W......W W W......W",
        "W.WW.W.W W W.W.WW.W",
        "W*...W...W...W...*W",
        "W.WW.W.WWWWW.W.WW.W",
        "W.................W",
        "W.WWWWWW.W.WWWWWW.W",
        "W........W........W",
        "WWWWWWWWWWWWWWWWWWW"
    };

    private final String[] layoutB = {
        "WWWWWWWWWWWWWWWWWWW",
        "W.WWWWWW.W.WWWWWW.W",
        "W.W*...W.W.W...*W.W",
        "W.W.W..W.W.W..W.W.W",
        "W...W..W...W..W...W",
        "W.WWWW.WWWWW.WWWW.W",
        "W.................W",
        "WWWW.W.WWWWW.W.WWWW",
        "WWWW.W.......W.WWWW",
        "WWWW.W WGGGW W.WWWW",
        "WWWW.W W...W W.WWWW",
        "WWWW.W WWWWW W.WWWW",
        "WWWW.W.......W.WWWW",
        "WWWW.W.WWWWW.W.WWWW",
        "W.................W",
        "W.WWWW.WWWWW.WWWW.W",
        "W...W..W...W..W...W",
        "W.W.W..W.W.W..W.W.W",
        "W.W*...W.W.W...*W.W",
        "W.WWWWWW.W.WWWWWW.W",
        "W.................W",
        "WWWWWWWWWWWWWWWWWWW"
    };

    public Maze() {
        this.tileSize = 40f;
        this.wallTexture = new Texture("wall.png");
        this.walls = new ArrayList<>();

        this.layouts = new ArrayList<>();
        this.layouts.add(originalLayout);
        this.layouts.add(layoutA);
        this.layouts.add(layoutB);

        this.currentLayout = originalLayout;
        this.width = currentLayout[0].length() * tileSize;
        this.height = currentLayout.length * tileSize;
        initializeWalls();
    }

    private void initializeWalls() {
        walls.clear();
        int numRows = currentLayout.length;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < currentLayout[row].length(); col++) {
                char symbol = currentLayout[row].charAt(col);
                if (symbol == 'W') { // Hanya 'W' yang dianggap dinding
                    float x = col * tileSize;
                    float y = (numRows - 1 - row) * tileSize;
                    walls.add(new Rectangle(x, y, tileSize, tileSize));
                }
            }
        }
    }

    public void shiftLayout(int index) {
        if (index < 0 || index >= layouts.size()) {
            // Kembali ke layout original jika index tidak valid
            this.currentLayout = this.originalLayout;
        } else {
            this.currentLayout = this.layouts.get(index);
        }
        initializeWalls(); // Bangun ulang dinding sesuai layout baru
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

    public boolean isWallAt(float x, float y) {
        int tileX = (int) (x / tileSize);
        int tileY = (int) (y / tileSize);
        return isWallAtTile(tileX, tileY);
    }

    public boolean isWallAtTile(int tileX, int tileY) {
        int numRows = currentLayout.length;
        int row = numRows - 1 - tileY;

        if (row < 0 || row >= numRows || tileX < 0 || tileX >= currentLayout[0].length()) {
            return true;
        }
        return currentLayout[row].charAt(tileX) == 'W';
    }

    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getTileSize() { return tileSize; }

    public void dispose() {
        if (wallTexture != null) {
            wallTexture.dispose();
        }
    }

    public String[] getLayout() {
        return currentLayout;
    }

    public boolean isTileSafeInLayout(int layoutIndex, int tileX, int tileY) {
        if (layoutIndex < 0 || layoutIndex >= layouts.size()) {
            return false;
        }
        String[] futureLayout = layouts.get(layoutIndex);
        int numRows = futureLayout.length;
        int row = numRows - 1 - tileY;

        if (row < 0 || row >= numRows || tileX < 0 || tileX >= futureLayout[0].length()) {
            return false;
        }
        return futureLayout[row].charAt(tileX) != 'W';
    }

    public int getLayoutCount() {
        return layouts.size();
    }

    public int getCurrentLayoutIndex() {
        return currentLayoutIndex;
    }


}
