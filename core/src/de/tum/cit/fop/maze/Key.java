package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * The Key class represents a collectible key in the game. The key has a position, a texture,
 * and can be collected by the player if they are close enough.
 * It also handles rendering the key and checking proximity to the player.
 */
public class Key  {

    //Texture
    private Texture keyTexture;

    //Key Components
    private float x, y;
    private boolean isCollected;

    //Constants
    private float scale = 0.2f;
    private float proximityRange = 5f;

    /**
     * Constructor to initialize the key at a specific position.
     *
     * @param x The x-coordinate of the key's position.
     * @param y The y-coordinate of the key's position.
     */
    public Key(float x, float y) {
        this.x = x;
        this.y = y;
        this.isCollected = false;
        keyTexture = new Texture("key.png");

    }

    /**
     * Renders the key on the screen if it has not been collected.
     *
     * @param batch The sprite batch used to draw the key texture.
     */
    public void render(SpriteBatch batch) {
        if (!isCollected) {
            batch.draw(keyTexture, x, y, keyTexture.getWidth() * scale, keyTexture.getHeight() * scale);
        }
    }

    /**
     * Checks if the key has been collected.
     *
     * @return true if the key has been collected, false otherwise.
     */
    public boolean isCollected() {
        return isCollected;
    }

    /**
     * Checks the proximity of the player to the key. If the player is within the proximity range,
     * the key is considered collectible.
     *
     * @param player The player object to check proximity against.
     * @return true if the player is close enough to the key, false otherwise.
     */
    public boolean checkProximityToPlayer(Player player) {
        float distance = (float) Math.sqrt(Math.pow(player.getX() - x, 2) + Math.pow(player.getY() - y, 2));
        return distance < proximityRange;
    }

    /**
     * Updates the key's state by checking if the player is close enough to collect it.
     * If collected, the key is marked as collected and the HUD is updated.
     *
     * @param player The player object to check for proximity.
     * @param hud The HUD object to update when the key is collected.
     */
    public void update(Player player, HUD hud) {
        if (checkProximityToPlayer(player)&& !isCollected) {
            isCollected = true;
            hud.collectKey();
            SoundManager.playKeyCollectedSound();
        }
    }

    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }
    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Disposes of the resources used by the key, including its texture.
     * This is called to free memory when the key is no longer needed.
     */
    public void dispose() {
        keyTexture.dispose();
    }
}