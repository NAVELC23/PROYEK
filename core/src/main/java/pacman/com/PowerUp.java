package pacman.com;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class PowerUp extends Entity {
    protected float duration; // Total duration of the power-up effect
    protected float remainingTime; // Remaining time for the power-up effect
    protected boolean active; // Whether the power-up is currently active/collectible

    public PowerUp(Vector2 position, String texturePath, float duration) {
        super(position, texturePath, new Vector2(20, 20)); // Power-up size 20x20
        this.duration = duration; //
        this.remainingTime = duration; //
        this.active = true; // Initially active
    }

    @Override
    public void update(float delta) {
        if (active) { //
            remainingTime -= delta; // Decrease remaining time
            if (remainingTime <= 0) { //
                active = false; // Deactivate if time runs out
            }
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, size.x, size.y);
    }

    public abstract int getScoreValue(); // Abstract method for score value

    public boolean isActive() {
        return active; //
    }

    public void collect() {
        active = false; // Deactivate after collection
    }
}
