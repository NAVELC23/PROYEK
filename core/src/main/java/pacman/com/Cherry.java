package pacman.com;

import com.badlogic.gdx.math.Vector2;

public class Cherry extends PowerUp {
    public Cherry(Vector2 position) {
        super(position, "cherry.png", 20f);
    }

    @Override
    public int getScoreValue() {
        return 100;
    }
}
