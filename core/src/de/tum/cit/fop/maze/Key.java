package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Key {
    private Texture keyTexture;
    private float x, y;
    private boolean isCollected;
    private float scale = 0.2f;
    private float proximityRange = 1f;  // Range within which the player can collect the key

    public Key(float x, float y) {
        this.x = x;
        this.y = y;
        this.isCollected = false;
        keyTexture = new Texture("key.png"); // Assuming "key.png" is the texture for the key
    }

    public void render(SpriteBatch batch) {
        if (!isCollected) {
            batch.draw(keyTexture, x, y, keyTexture.getWidth() * scale, keyTexture.getHeight() * scale);
        }
    }

    public void dispose() {
        keyTexture.dispose();
    }

    public boolean isCollected() {
        return isCollected;
    }

    // Check if the player is close enough to the key to collect it
    public void checkProximityToPlayer(Player player) {
        // Calculate distance between the key and the player
        float distance = (float) Math.sqrt(Math.pow(player.getX() - x, 2) + Math.pow(player.getY() - y, 2));

        if (distance < proximityRange && !isCollected) {
            isCollected = true;  // Collect the key
        }
    }

    // Set the key position
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }


}
