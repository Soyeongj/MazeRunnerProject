package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a door in the maze that can be opened when specific conditions are met.
 * The door serves as an exit point and requires both a key and at least one rescued friend to open.
 */
public class Door {
    // Constants
    private static final float SCORE_TIME_MULTIPLIER = 5f;
    private static final float BASE_SCORE = 1000f;
    private static final float LIFE_BONUS = 150f;

    // Core components
    private final Vector2 position;
    private final Rectangle bounds;

    /**
     * Constructs a Door object with the specified position and size.
     *
     * @param x      the x-coordinate of the door
     * @param y      the y-coordinate of the door
     * @param width  the width of the door
     * @param height the height of the door
     */
    public Door(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
    }

    /**
     * Checks if the player is near the door based on their position.
     *
     * @param playerPosition the position of the player
     * @return true if the player is near the door, false otherwise
     */
    public boolean isPlayerNearby(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }

    /**
     * Attempts to open the door if the player meets the necessary conditions.
     *
     * The player must be near the door, have collected the key, and have at least one saved friend.
     * If the conditions are met, the level is completed. Otherwise, a HUD message is displayed.
     *
     * @param playerPosition the position of the player
     * @param hud            the HUD instance managing game information
     * @param game           the current instance of the MazeRunnerGame
     * @param friends        the Friends instance containing information about saved friends
     */
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

    /**
     * Checks if the player has saved any friends.
     *
     * @param friends the Friends instance containing information about saved friends
     * @return true if there are saved friends, false otherwise
     */
    private boolean hasSavedFriends(Friends friends) {
        return !friends.getFollowingFriendsPositions().isEmpty();
    }

    /**
     * Completes the level by stopping the timer, calculating the final score, and transitioning
     * to the GameClearScreen. A victory sound is also played.
     *
     * @param game the current instance of the MazeRunnerGame
     * @param hud  the HUD instance managing game information
     */
    private void completeLevel(MazeRunnerGame game, HUD hud) {
        hud.stopTimer();
        float finalScore = calculateFinalScore(hud);

        game.setScreen(new GameClearScreen(game, finalScore));
        SoundManager.playVictorySound();
    }

    /**
     * Calculates the final score based on base score, time, and remaining lives.
     *
     * @param hud the HUD instance containing game statistics
     * @return the calculated final score
     */
    private float calculateFinalScore(HUD hud) {
        return BASE_SCORE +
                (hud.getFinalTime() * SCORE_TIME_MULTIPLIER) +
                (hud.getLives() * LIFE_BONUS);
    }

    /**
     * Gets the position of the door.
     * A copy of the position is returned to prevent external modification.
     *
     * @return a copy of the door's position as a Vector2
     */
    public Vector2 getPosition() {
        return position.cpy();
    }
}