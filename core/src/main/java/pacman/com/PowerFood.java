package pacman.com;

import com.badlogic.gdx.math.Vector2;

public class PowerFood extends PowerUp {
    public PowerFood(Vector2 position) {
        super(position, "powerFood.png", 10f); // Durasi PowerFood 10 detik
    }

    @Override
    public int getScoreValue() {
        return 50; // Skor 50
    }
}
