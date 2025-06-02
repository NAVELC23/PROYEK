package pacman.com;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public abstract class PowerUp extends Entity {
    protected float duration;
    protected float remainingTime;
    protected boolean active;

    public PowerUp(Vector2 position, String texturePath, float duration) {
        super(position, texturePath, new Vector2(20, 20));
        this.duration = duration;
        this.remainingTime = duration;
        this.active = true;
    }

    @Override
    public void update(float delta) {
        if (active) {
            remainingTime -= delta;
            if (remainingTime <= 0) {
                active = false;
            }
        }
    }

    public abstract int getScoreValue();

    public boolean isActive() {
        return active;
    }

    public void collect() {
        active = false;
    }
}
