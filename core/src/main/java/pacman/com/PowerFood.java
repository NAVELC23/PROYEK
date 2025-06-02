package pacman.com;

import com.badlogic.gdx.math.Vector2;

public class PowerFood extends PowerUp {
    public PowerFood(Vector2 position) {
        super(position, "powerFood.png", 10f);
    }

    @Override
    public int getScoreValue() {
        return 50;
    }
}
