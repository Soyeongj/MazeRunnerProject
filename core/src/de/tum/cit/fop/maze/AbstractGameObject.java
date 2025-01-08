package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class AbstractGameObject {
    protected Vector2 position;
    protected Rectangle bounds;

    public AbstractGameObject(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
    }

    public abstract boolean isPlayerNear(Vector2 playerPosition);

    public abstract void interact(Vector2 playerPosition, HUD hud, MazeRunnerGame game, Player player, Friends friends, float delta);

}
