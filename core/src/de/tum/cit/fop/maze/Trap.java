package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

public class Trap  {
    //Positions
    private Vector2 position;
    private Rectangle bounds;

    //Rock Falling Controls
    private Texture rockTexture;
    private Vector2 rockPosition;
    private float rockFallDuration = 0f;
    public boolean isRockFalling = false;
    private float rockStartY;
    private static final float fallSpeed = 300f; // Constant speed for the rock's fall
    private static final float fallHeight = 70f; // The height from which the rock will fall

    //Damage Controls
    private float livesCoolDown = 0f; // Cooldown timer for the trap's effect (lives decrement) to prevent rapid reactivation

    public Trap(float x, float y, float width, float height, String rockTexturePath) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
        this.rockTexture = new Texture(rockTexturePath);
        this.rockPosition = new Vector2(x, y + fallHeight); // Set initial position of the rock
        this.rockStartY = y + fallHeight; // The rock's starting Y position before falling
    }

    public boolean isPlayerNearby(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }

    public void fallRock(Vector2 playerPosition, HUD hud, Player player, float delta, Friends friends) {
        // Check if the player is nearby and the rock is not already falling
        if (livesCoolDown <= 0 && isPlayerNearby(playerPosition) && !isRockFalling) {
            isRockFalling = true; // Start the falling rock
            rockFallDuration = 0f; // Reset the falling duration
            rockPosition.y = rockStartY; // Reset the rock to its starting Y position

            SoundManager.playRockSound();
        }

        // If the rock is falling, update its Y position based on fall speed and time elapsed
        if (isRockFalling) {
            rockFallDuration += delta;
            rockPosition.y = rockStartY - (fallSpeed * rockFallDuration); // Update the rock's Y position

            // If the rock reaches the ground (position of the trap)
            if (rockPosition.y <= position.y) {
                rockPosition.y = position.y; // Set the rock position to the ground level
                isRockFalling = false; // Stop the rock from falling
                    if (hud.getLives() >= 0) {
                        friends.removeFriendAt(friends.getFollowingFriendsPositions().size() - 1); // Remove last friend
                        hud.decrementLives(); // Decrement the player's lives
                        player.triggerRedEffect(); // Trigger the red effect on the player
                        livesCoolDown = 1; // Set cooldown to prevent rapid re-triggering
                    } else {
                        hud.setLives(0);
                        player.setDead();
                        player.revertToPrevious();
                    }

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        rockPosition.y = rockStartY; // Reset the rock's Y position to its starting height
                    }
                }, 0.5f); // Wait for 0.5 seconds before resetting the rock's position
            }
        }

        if (livesCoolDown > 0) {
            livesCoolDown -= delta; // Decrease the cooldown over time
        }
    }

    public void render(SpriteBatch batch) {
        if (isRockFalling || rockPosition.y != rockStartY) {
            batch.draw(rockTexture, rockPosition.x, rockPosition.y, bounds.width, bounds.height); // Draw the rock
        }
    }

    public void saveTrapState() {
        Preferences prefs = Gdx.app.getPreferences("trap");
        prefs.putFloat("positionX", position.x);
        prefs.putFloat("positionY", position.y);
        prefs.putFloat("lcd", livesCoolDown);
        prefs.putBoolean("isRockFalling", isRockFalling);
    }

    public void loadTrapState() {
        Preferences prefs = Gdx.app.getPreferences("trap");
        position.x = prefs.getFloat("positionX", position.x);
        position.y = prefs.getFloat("positionY", position.y);
        livesCoolDown = prefs.getFloat("lcd", livesCoolDown);
        isRockFalling = prefs.getBoolean("isRockFalling", isRockFalling);
    }

    public void dispose() {
        rockTexture.dispose();
    }

}
