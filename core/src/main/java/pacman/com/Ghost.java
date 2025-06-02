package pacman.com;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;


public class Ghost extends Entity {
    private GhostType type;
    private float speed;
    private Vector2 startPosition;
    private boolean isScared;
    private float scaredTime;
    private Pacman pacman;
    private Maze maze;

    public Ghost(Vector2 startPosition, GhostType type, Pacman pacman, Maze maze) {
        super(startPosition, GhostType.getTexturePath(type), new Vector2(30, 30));
        this.type = type;
        this.speed = type.getBaseSpeed();
        this.startPosition = startPosition;
        this.pacman = pacman;
        this.maze = maze;
        this.isScared = false;
        this.scaredTime = 0;
    }

    @Override
    public void update(float delta) {
        if (isScared) {
            scaredTime -= delta;
            if (scaredTime <= 0) {
                setScared(false);
            }
            // Move randomly when scared
            position.x += (Math.random() * 2 - 1) * speed * 0.5f * delta;
            position.y += (Math.random() * 2 - 1) * speed * 0.5f * delta;
        } else {
            // Behavior based on ghost type
            switch (type) {
                case RED:
                    chasePacman(delta);
                    break;
                case PINK:
                    followPacman(delta);
                    break;
                case BLUE:
                    if (isPacmanInRange(4)) {
                        chasePacman(delta);
                    } else {
                        moveRandomly(delta);
                    }
                    break;
                case ORANGE:
                    moveRandomly(delta);
                    break;
            }
        }

        // Keep ghost within bounds
        position.x = Math.max(0, Math.min(maze.getWidth() - size.x, position.x));
        position.y = Math.max(0, Math.min(maze.getHeight() - size.y, position.y));
    }

    private void chasePacman(float delta) {
        Vector2 direction = new Vector2(pacman.getPosition().x - position.x,
            pacman.getPosition().y - position.y).nor();
        position.x += direction.x * speed * delta;
        position.y += direction.y * speed * delta;
    }

    private void followPacman(float delta) {
        Vector2 pacmanDirection = new Vector2();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) pacmanDirection.x = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) pacmanDirection.x = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) pacmanDirection.y = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) pacmanDirection.y = -1;

        Vector2 target = new Vector2(pacman.getPosition()).add(pacmanDirection.scl(60));
        Vector2 direction = new Vector2(target.x - position.x, target.y - position.y).nor();
        position.x += direction.x * speed * delta;
        position.y += direction.y * speed * delta;
    }

    private void moveRandomly(float delta) {
        position.x += (Math.random() * 2 - 1) * speed * delta;
        position.y += (Math.random() * 2 - 1) * speed * delta;
    }

    private boolean isPacmanInRange(int tiles) {
        float tileSize = maze.getTileSize();
        float dx = Math.abs(pacman.getPosition().x - position.x);
        float dy = Math.abs(pacman.getPosition().y - position.y);
        return dx <= tiles * tileSize && dy <= tiles * tileSize;
    }

    public void setScared(boolean scared) {
        this.isScared = scared;
        if (scared) {
            this.texture = new Texture("scaredGhost.png");
            scaredTime = 4f; // 4 seconds of scared time
        } else {
            this.texture = new Texture(GhostType.getTexturePath(type));
        }
    }

    public boolean isScared() {
        return isScared;
    }

    public void respawn() {
        position.set(startPosition);
        setScared(false);
    }
}
