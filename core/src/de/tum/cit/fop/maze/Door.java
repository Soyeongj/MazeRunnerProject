package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Door implements NearbyPlayer {
    private Vector2 position;
    private Rectangle bounds;

    public Door(float x, float y, float width, float height) {
        position = new Vector2(x, y);
        bounds = new Rectangle(x, y, width, height);
    }

    @Override
    public boolean isPlayerNearby(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }


    public void tryToOpen(Vector2 playerPosition, HUD hud, MazeRunnerGame game) {
        if (isPlayerNearby(playerPosition)) {
            if (hud.isKeyCollected()) {
                hud.stopTimer();
                float finalTime = 1000 + (hud.getFinalTime() * 5) + hud.getLives()* 5 ;
                game.setScreen(new GameClearScreen(game, finalTime));
                SoundManager.playVictorySound();
            }
        }


    }

    public Vector2 getPosition() {
        return position;
    }


}