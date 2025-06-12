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
        this.texture = new Texture(texturePath); // Load texture from the given path
        this.size = size; // Set entity size
    }

    public abstract void update(float delta); // Abstract method for updating entity state

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size.x, size.y); // Draw the entity
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getSize() {
        return size;
    }

    public void dispose() {
        if (texture != null) { // Ensure texture exists before disposing
            texture.dispose();
        }
    }

    public boolean collidesWith(Entity other) {
        // AABB collision detection
        return position.x < other.position.x + other.size.x &&
            position.x + size.x > other.position.x &&
            position.y < other.position.y + other.size.y &&
            position.y + size.y > other.position.y;
    }
}
