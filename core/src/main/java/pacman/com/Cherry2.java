package pacman.com;

import com.badlogic.gdx.math.Vector2;

public class Cherry2 extends PowerUp {
    public Cherry2(Vector2 position) {
        super(position, "cherry2.png", 8f);
    }

    @Override
    public int getScoreValue() {
        return 300;
    }
}
