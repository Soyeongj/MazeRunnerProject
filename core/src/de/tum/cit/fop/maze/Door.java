package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Door {
    private Vector2 position;
    private Rectangle bounds;
    private static final float INTERACTION_DISTANCE = 10f;

    public Door(float x, float y, float width, float height) {
        position = new Vector2(x, y);
        bounds = new Rectangle(x, y, width, height);
    }
    public boolean isPlayerNear(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }

    public void tryToOpen(Vector2 playerPosition, HUD hud, MazeRunnerGame game) {
        if (isPlayerNear(playerPosition)) {
            if (hud.isKeyCollected()) {
                hud.pressE();
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    hud.stopTimer();
                    float finalTime = 1000 + (hud.getFinalTime() * 5);
                    game.setScreen(new GameClearScreen(game, finalTime));
                }
            } else {
                hud.needKey();
            }
        }
    }
}