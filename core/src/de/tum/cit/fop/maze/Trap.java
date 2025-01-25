package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

/**
 * Represents a trap in the game, which includes a rock that falls when a player is nearby.
 * The trap also interacts with the player's lives and HUD when activated.
 */
public class Trap {
    // Positions
    private Vector2 position;
    private Rectangle bounds;

    // Rock Falling Controls
    private Texture rockTexture;
    private Vector2 rockPosition;
    private float rockFallDuration;
    public boolean isRockFalling;
    private float rockStartY;
    private static float fallSpeed;
    private static float fallHeight;

    // Damage Controls
    private float livesCoolDown;

    //Trap States
    private static String PREFERENCES_NAME;

    /**
     * Constructs a Trap object with the specified position, size, and rock texture path.
     *
     * @param x The x-coordinate of the trap's position
     * @param y The y-coordinate of the trap's position
     * @param width The width of the trap
     * @param height The height of the trap
     * @param rockTexturePath The file path to the texture for the rock
     */
    public Trap(float x, float y, float width, float height, String rockTexturePath) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
        this.rockTexture = new Texture(rockTexturePath);
        this.rockPosition = new Vector2(x, y + fallHeight); // Set initial position of the rock
        this.rockStartY = y + fallHeight; // The rock's starting Y position before falling
        this.rockFallDuration = 0f;
        this.isRockFalling = false;
        this.fallSpeed = 300f;
        this.fallHeight = 70f;
        this.livesCoolDown = 0f;
        this.PREFERENCES_NAME = "Trap States";

    }

    /**
     * Checks if the player is within the bounds of the trap.
     *
     * @param playerPosition The position of the player
     * @return True if the player is within the bounds of the trap, false otherwise
     */
    public boolean isPlayerNearby(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }

    /**
     * Causes the rock to fall when the player is nearby, updating the rock's position and applying damage.
     * The trap also checks cooldowns to prevent multiple activations in quick succession.
     *
     * @param playerPosition The position of the player
     * @param hud The HUD object to update the player's lives
     * @param player The player object to trigger effects (e.g., red effect)
     * @param delta The time elapsed since the last frame
     * @param friends The Friends object to manage the player's friends (e.g., removing a friend if necessary)
     */
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
            rockPosition.y = rockStartY - (fallSpeed * rockFallDuration);

            // If the rock reaches the ground (position of the trap)
            if (rockPosition.y <= position.y) {
                rockPosition.y = position.y;
                isRockFalling = false;
                if (hud.getLives() >= 0) {
                    friends.removeFriendAt(friends.getFollowingFriendsPositions().size() - 1);
                    hud.decrementLives();
                    player.triggerRedEffect();
                    livesCoolDown = 1;
                } else {
                    hud.setLives(0);
                    player.setDead();
                    player.revertToPrevious();
                }

                // Reset the rock position after a short delay
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        rockPosition.y = rockStartY;
                    }
                }, 0.5f);
            }
        }

        // Decrease the cooldown over time
        if (livesCoolDown > 0) {
            livesCoolDown -= delta;
        }
    }

    /**
     * Renders the rock if it is falling or has been moved from its initial position.
     *
     * @param batch The SpriteBatch used to render the rock texture
     */
    public void render(SpriteBatch batch) {
        if (isRockFalling || rockPosition.y != rockStartY) {
            batch.draw(rockTexture, rockPosition.x, rockPosition.y, bounds.width, bounds.height); // Draw the rock
        }
    }

    /**
     * Saves the current state of the trap, including its position and rock falling status.
     */
    public void saveTrapState() {
        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
        prefs.putFloat("positionX", position.x);
        prefs.putFloat("positionY", position.y);
        prefs.putFloat("lcd", livesCoolDown);
        prefs.putBoolean("isRockFalling", isRockFalling);
    }

    /**
     * Loads the saved state of the trap, including its position and rock falling status.
     */
    public void loadTrapState() {
        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
        position.x = prefs.getFloat("positionX", position.x);
        position.y = prefs.getFloat("positionY", position.y);
        livesCoolDown = prefs.getFloat("lcd", livesCoolDown);
        isRockFalling = prefs.getBoolean("isRockFalling", isRockFalling);
    }

    /**
     * Disposes of the resources used by the trap, including the rock texture.
     */
    public void dispose() {
        rockTexture.dispose();
    }
}
