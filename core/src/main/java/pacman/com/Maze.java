package pacman.com;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Maze {
    private final Texture wallTexture;
    private final List<Rectangle> walls;
    private final float width;
    private final float height;
    private final float tileSize;

    // --- DESAIN LABIRIN FINAL (19x22) ---
    private final String[] layout = {
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

    public Maze() {
        this.tileSize = 40f;
        this.wallTexture = new Texture("wall.png");
        this.walls = new ArrayList<>();
        this.width = layout[0].length() * tileSize;
        this.height = layout.length * tileSize;
        initializeWalls();
    }

    private void initializeWalls() {
        walls.clear();
        int numRows = layout.length;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < layout[row].length(); col++) {
                char symbol = layout[row].charAt(col);
                if (symbol == 'W') { // Hanya 'W' yang dianggap dinding
                    float x = col * tileSize;
                    float y = (numRows - 1 - row) * tileSize;
                    walls.add(new Rectangle(x, y, tileSize, tileSize));
                }
            }
        }
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
        // Cek berdasarkan tile, bukan piksel, agar lebih akurat
        int tileX = (int) (x / tileSize);
        int tileY = (int) (y / tileSize);
        int numRows = layout.length;

        // Konversi koordinat Y game ke koordinat array
        int row = numRows - 1 - tileY;

        if (row < 0 || row >= numRows || tileX < 0 || tileX >= layout[0].length()) {
            return true; // Anggap di luar peta sebagai dinding
        }
        return layout[row].charAt(tileX) == 'W';
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
        return layout;
    }
}
