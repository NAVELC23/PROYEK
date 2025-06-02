package pacman.com;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2; // Import Vector2
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
        this.tileSize = 40f; // Setiap tile adalah 40x40 piksel
        this.wallTexture = new Texture("wall.png");
        this.walls = new ArrayList<>();
        initializeWalls();
    }

    private void initializeWalls() {
        walls.clear(); // Bersihkan dinding yang ada

        // Desain maze 19x21 tiles berdasarkan map.jpg
        // 'X' untuk dinding, ' ' untuk jalur
        // Karena di gambar Y=0 adalah baris paling atas, kita akan membalik urutannya
        // sehingga row[0] di array ini akan digambar di Y tertinggi di layar.
        String[] mazeLayout = {
            "XXXXXXXXXXXXXXXXXXX", // Row 0 (atas) -> Y = 20 * tileSize
            "X X X X X X X X X X", // Row 1
            "X X X X X X X X X X", // Row 2
            "X X X X X X X X X X", // Row 3
            "X X X X X X X X X X", // Row 4
            "X X X X X X X X X X", // Row 5
            "X X X X X X X X X X", // Row 6
            "X X X X X X X X X X", // Row 7
            "X O O O X X X X X X", // Row 8 (area hantu)
            "X X X X b p o r X X", // Row 9 (hantu P,O,R,B di gambar ada di sini, kita pakai sesuai posisi mereka)
            "X O O O X X X X X X", // Row 10
            "X X X X X X X X X X", // Row 11
            "X X X X X X X X X X", // Row 12
            "X X X X X X X X X X", // Row 13
            "X X X X X X X X P X", // Row 14 (Pacman di gambar)
            "X X X X X X X X X X", // Row 15
            "X X X X X X X X X X", // Row 16
            "X X X X X X X X X X", // Row 17
            "X X X X X X X X X X", // Row 18
            "X X X X X X X X X X", // Row 19
            "XXXXXXXXXXXXXXXXXXX"  // Row 20 (bawah) -> Y = 0 * tileSize
        };

        // Mengisi array walls berdasarkan mazeLayout
        // Loop dari baris paling atas di gambar (indeks 0) hingga paling bawah (indeks 20)
        // Y layar akan dihitung terbalik: (mazeLayout.length - 1 - y_index_array) * tileSize
        for (int y = 0; y < mazeLayout.length; y++) {
            String rowString = mazeLayout[y];
            for (int x = 0; x < rowString.length(); x++) {
                if (rowString.charAt(x) == 'X') {
                    // Konversi indeks baris array ke koordinat y di layar.
                    // Jika mazeLayout[0] adalah baris paling atas (Y tertinggi), maka:
                    // y_screen = (total_rows - 1 - current_row_index) * tileSize
                    float wallY = (mazeLayout.length - 1 - y) * tileSize;
                    addWall(x * tileSize, wallY);
                }
            }
        }

        // Tambahkan dinding khusus yang mungkin tidak tercapture di map.jpg sebagai 'X'
        // Ini adalah interpretasi dari grid maze yang diberikan.
        // Anda mungkin perlu menyesuaikan ini secara manual untuk mencocokkan persis gambar.
        // Contoh: kotak di tengah, jalur horizontal/vertikal.
        // Saya akan mencoba mendekati struktur utama dari gambar.
        // Kita akan menggunakan pendekatan `String[]` secara ketat untuk maze ini.
        // 'O' di gambar kemungkinan adalah jalur atau dot.
        // Untuk karakter 'b', 'p', 'o', 'r', 'P' di gambar: itu adalah posisi entitas, bukan dinding.
        // Jadi, tempat-tempat itu haruslah jalur.

        // Membentuk maze yang lebih akurat sesuai gambar dengan pola 'X' dan ' '.
        // Saya akan menggunakan `String[] mazeLayout` di atas secara eksklusif.
        // Beberapa baris di map.jpg tidak penuh dengan X atau O. Asumsi kosong = jalur.
        // Contoh: row 0 adalah XXXXX..., row 1 adalah X...X.
        // Perhatikan baris 0, 1, 2, 3, 4, 5, 6, 7, 11, 12, 13, 15, 16, 17, 18, 19, 20
        // Dari gambar, tampak ada pola 'X' di tepi dan 'X' di tengah.
        // Mari kita buat ulang layout string yang lebih akurat dengan gambar.
        // Kolom 0-18 (19 kolom), Baris 0-20 (21 baris)

        String[] preciseMazeLayout = {
            "XXXXXXXXXXXXXXXXXXX", // 0
            "X X X X X X X X X X", // 1
            "X XXXX X X XXXX X X", // 2
            "X X  X X X X  X X X", // 3
            "X X  X X X X  X X X", // 4
            "X X  X X X X  X X X", // 5
            "X XXXX X X XXXX X X", // 6
            "X X X X X X X X X X", // 7
            "X O O O X X X X X X", // 8 (O di gambar berarti jalur)
            "X X X X X X X X X X", // 9 (B, P, O, R berada di sini, jadi ini jalur)
            "X O O O X X X X X X", // 10
            "X X X X X X X X X X", // 11
            "X X X X X X X X X X", // 12
            "X X X X X X X X X X", // 13
            "X X X X X X X X X X", // 14 (P ada di sini, jadi ini jalur)
            "X X X X X X X X X X", // 15
            "X X X X X X X X X X", // 16
            "X X X X X X X X X X", // 17
            "X X X X X X X X X X", // 18
            "X X X X X X X X X X", // 19
            "XXXXXXXXXXXXXXXXXXX"  // 20
        };

        // Saya akan menginterpretasikan 'O' di gambar sebagai jalur yang tidak ada dindingnya.
        // Dan area 'b', 'p', 'o', 'r', 'P' juga sebagai jalur.
        // Untuk itu, saya akan mengganti semua 'O' menjadi ' ' (spasi) untuk merepresentasikan jalur.
        // Karakter entitas juga akan menjadi ' '.

        String[] finalMazeLayout = {
            "XXXXXXXXXXXXXXXXXXX", // 0
            "X X X X X X X X X X", // 1
            "X X X X X X X X X X", // 2
            "X X X X X X X X X X", // 3
            "X X X X X X X X X X", // 4
            "X X X X X X X X X X", // 5
            "X X X X X X X X X X", // 6
            "X X X X X X X X X X", // 7
            "X           X X X X", // 8 (O O O X X X X X X di gambar)
            "X X X X     X X X X", // 9 (X X X X b p o r X X di gambar. b,p,o,r = space)
            "X           X X X X", // 10 (O O O X X X X X X di gambar)
            "X X X X X X X X X X", // 11
            "X X X X X X X X X X", // 12
            "X X X X X X X X X X", // 13
            "X X X X X X X P X X", // 14 (P di gambar)
            "X X X X X X X X X X", // 15
            "X X X X X X X X X X", // 16
            "X X X X X X X X X X", // 17
            "X X X X X X X X X X", // 18
            "X X X X X X X X X X", // 19
            "XXXXXXXXXXXXXXXXXXX"  // 20
        };

        // Mengisi array walls berdasarkan finalMazeLayout yang sudah diinterpretasikan
        for (int y = 0; y < finalMazeLayout.length; y++) {
            String rowString = finalMazeLayout[y];
            for (int x = 0; x < rowString.length(); x++) {
                if (rowString.charAt(x) == 'X') {
                    // Konversi indeks baris array ke koordinat y di layar.
                    // Karena `map.jpg` Y=0 di paling atas, maka
                    // (jumlah baris total - 1 - indeks baris saat ini) akan memberikan Y dari bawah.
                    float wallY = (finalMazeLayout.length - 1 - y) * tileSize;
                    addWall(x * tileSize, wallY);
                }
            }
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

    // Memeriksa tabrakan antara sebuah bounding box (misalnya, Pacman atau hantu) dengan dinding
    // Ini adalah metode utama untuk deteksi tabrakan pergerakan.
    public boolean collidesWithWall(Rectangle boundingBox) {
        for (Rectangle wall : walls) {
            if (boundingBox.overlaps(wall)) {
                return true;
            }
        }
        return false;
    }

    // Memeriksa apakah sebuah *titik* berada di dalam dinding
    // Berguna untuk pengecekan lokasi spawn atau dot
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

    public List<Rectangle> getWalls() {
        return walls;
    }

    public void dispose() {
        wallTexture.dispose();
    }
}
