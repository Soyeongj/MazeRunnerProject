package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Door implements NearbyPlayer {

    // Variables for Door Position and Bounds
    private Vector2 position;
    private Rectangle bounds;

    public Door(float x, float y, float width, float height) {
        position = new Vector2(x, y);
        bounds = new Rectangle(x, y, width, height);
    }

    // Check if the player is nearby the door
    @Override
    public boolean isPlayerNearby(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y); // Return true if player is within the door's bounds
    }

    // Try to open the door, check if player has the key and if friends are nearby
    public void tryToOpen(Vector2 playerPosition, HUD hud, MazeRunnerGame game, Friends friends) {
        if (isPlayerNearby(playerPosition) && hud.isKeyCollected()) {
            if (friends.getFollowingFriendsPositions().size() > 0) {
                hud.stopTimer();
                //Formula for final score
                float finalTime = 1000 + (hud.getFinalTime() * 5) + hud.getLives() * 150;

                // Transition to the Game Clear screen with the final time
                game.setScreen(new GameClearScreen(game, finalTime));
                SoundManager.playVictorySound();
            } else {
                hud.needFriend();
            }
        }
    }

    public Vector2 getPosition() {
        return position;
    }
}
