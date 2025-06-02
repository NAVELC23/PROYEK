package pacman.com;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity {
    protected Vector2 position;
    protected Texture texture;
    protected Vector2 size;

    public Entity(Vector2 startPosition, String texturePath, Vector2 size) {
        this.position = startPosition;
        this.texture = new Texture(texturePath);
        this.size = size;
    }

    public abstract void update(float delta); // Metode abstrak untuk pembaruan

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size.x, size.y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getSize() {
        return size;
    }

    public void dispose() {
        texture.dispose();
    }

    public boolean collidesWith(Entity other) {
        return position.x < other.position.x + other.size.x &&
            position.x + size.x > other.position.x &&
            position.y < other.position.y + other.size.y &&
            position.y + size.y > other.position.y;
    }
}
