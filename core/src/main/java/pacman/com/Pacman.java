package pacman.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Pacman extends Entity {
    private Texture leftTexture;
    private Texture rightTexture;
    private Texture upTexture;
    private Texture downTexture;
    private float speed;
    private boolean poweredUp;
    private float powerUpTime;

    public Pacman(Vector2 startPosition) {
        super(startPosition, "pacmanRight.png", new Vector2(30, 30));
        leftTexture = new Texture("pacmanLeft.png");
        rightTexture = new Texture("pacmanRight.png");
        upTexture = new Texture("pacmanUp.png");
        downTexture = new Texture("pacmanDown.png");
        speed = 150f;
        poweredUp = false;
        powerUpTime = 0;
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            position.x -= speed * delta;
            texture = leftTexture;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            position.x += speed * delta;
            texture = rightTexture;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            position.y += speed * delta;
            texture = upTexture;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            position.y -= speed * delta;
            texture = downTexture;
        }

        if (poweredUp) {
            powerUpTime -= delta;
            if (powerUpTime <= 0) {
                poweredUp = false;
            }
        }
    }

    public void setPoweredUp(boolean poweredUp, float duration) {
        this.poweredUp = poweredUp;
        this.powerUpTime = duration;
        if (poweredUp) {
            texture = new Texture("PACMAN GELAP.png");
        } else {
            texture = rightTexture;
        }
    }

    public boolean isPoweredUp() {
        return poweredUp;
    }

    @Override
    public void dispose() {
        super.dispose();
        leftTexture.dispose();
        rightTexture.dispose();
        upTexture.dispose();
        downTexture.dispose();
    }
}
