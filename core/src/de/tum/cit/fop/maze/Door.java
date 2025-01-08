package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Vector2;

public class Door extends AbstractGameObject {

    public Door(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    @Override
    public boolean isPlayerNear(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }

    @Override
    public void interact(Vector2 playerPosition, HUD hud, MazeRunnerGame game, Player player, Friends friends, float delta) {
        if (isPlayerNear(playerPosition)) {
            if (hud.isKeyCollected()) {
                hud.stopTimer();
                float finalTime = 1000 + (hud.getFinalTime() * 5) + hud.getLives() * 5;
                game.setScreen(new GameClearScreen(game, finalTime));
                SoundManager.playVictorySound();
            }
        }
    }
}