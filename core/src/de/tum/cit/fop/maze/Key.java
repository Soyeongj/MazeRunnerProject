package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Key {
    private Texture keyTexture;
    private float x, y;
    private boolean isCollected;
    private float scale = 0.2f;
    private float proximityRange = 5f;
    private static final String PREFERENCES_NAME = "KeyState";



    public Key(float x, float y) {
        this.x = x;
        this.y = y;
        this.isCollected = false;
        keyTexture = new Texture("key.png");

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

    public boolean checkProximityToPlayer(Player player) {
        float distance = (float) Math.sqrt(Math.pow(player.getX() - x, 2) + Math.pow(player.getY() - y, 2));
        return distance < proximityRange;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(Player player, HUD hud) {
        if (checkProximityToPlayer(player)&& !isCollected) {
            isCollected = true;
            hud.collectKey();
            setPosition(-1000, -1000);
            SoundManager.playKeyCollectedSound();
        }
    }


    public void setCollected(boolean collected) {
        isCollected = collected;
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


}