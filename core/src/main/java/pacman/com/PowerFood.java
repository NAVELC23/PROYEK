package pacman.com;

import com.badlogic.gdx.math.Vector2;

public class PowerFood extends PowerUp {
    public PowerFood(Vector2 position) {
        super(position, "powerFood.png", 15f); // PowerFood texture, 15-second duration
    }

    @Override
    public int getScoreValue() {
        return 50; // Score awarded
    }
}
