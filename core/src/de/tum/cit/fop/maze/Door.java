package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a door in the maze that can be opened when specific conditions are met.
 * The door serves as an exit point and requires both a key and rescued friends to open.
 */
public class Door {
    // Constants
    private static final float SCORE_TIME_MULTIPLIER = 5f;
    private static final float BASE_SCORE = 1000f;
    private static final float LIFE_BONUS = 150f;

    // Core components
    private final Vector2 position;
    private final Rectangle bounds;


    public Door(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
    }


    public boolean isPlayerNearby(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }

    public void tryToOpen(Vector2 playerPosition, HUD hud, MazeRunnerGame game, Friends friends) {
        if (!isPlayerNearby(playerPosition) || !hud.isKeyCollected()) {
            return;
        }

        if (hasSavedFriends(friends)) {
            completeLevel(game, hud);
        } else {
            hud.needFriend();
        }
    }

    private boolean hasSavedFriends(Friends friends) {
        return !friends.getFollowingFriendsPositions().isEmpty();
    }


    private void completeLevel(MazeRunnerGame game, HUD hud) {
        hud.stopTimer();
        float finalScore = calculateFinalScore(hud);

        game.setScreen(new GameClearScreen(game, finalScore));
        SoundManager.playVictorySound();
    }


    private float calculateFinalScore(HUD hud) {
        return BASE_SCORE +
                (hud.getFinalTime() * SCORE_TIME_MULTIPLIER) +
                (hud.getLives() * LIFE_BONUS);
    }


    public Vector2 getPosition() {
        return position.cpy(); // Return a copy to prevent external modification
    }
}