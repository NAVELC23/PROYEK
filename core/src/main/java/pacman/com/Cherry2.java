package pacman.com;

import com.badlogic.gdx.math.Vector2;

public class Cherry2 extends PowerUp {
    public Cherry2(Vector2 position) {
        super(position, "cherry2.png", 8f); // Cherry2 texture and a duration of 8 seconds
    }

    @Override
    public int getScoreValue() {
        return 300; // Score awarded when collected
    }
}
