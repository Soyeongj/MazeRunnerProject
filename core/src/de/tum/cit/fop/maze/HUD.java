package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class HUD {
    private Texture HUDpanel;
    private Texture friendHUD;
    private BitmapFont font;
    private int lives;
    private int score;
    private float scale = 0.2f;
    private float globalTimer;
    private Texture keyIcon;
    private boolean keyCollected;



    public HUD() {
        this.HUDpanel = new Texture("sand.png");
        this.friendHUD = new Texture("oldman_right_1.png");
        this.keyIcon = new Texture("key.png");
        this.font = new BitmapFont();
        this.lives = 3;
        this.score = 0;
        this.font.getData().setScale(0.3f);
        this.globalTimer = 0f;
        this.keyCollected = false;


    }
    public void updateTimer(float delta) {
        globalTimer += delta;
    }

    public float getGlobalTimer() {
        return globalTimer;
    }

    public void render(SpriteBatch batch) {
        // Draw the HUD background
        for (int x = 0; x <= 420; x += HUDpanel.getWidth()) {
            for (int y = 307; y <= 335; y += HUDpanel.getHeight()) {
                batch.draw(HUDpanel, x, y);
            }
        }

        font.draw(batch, "Score: " + score, 100, 318);

        font.draw(batch, "Fri ends: " + lives, 100, 323);

        font.draw(batch, "Key Collected: ", 150, 323);

        // Draw the friend HUD icons
        for (int i = 0; i < lives; i++) {
            batch.draw(friendHUD, 119 + (i * 5), 314,friendHUD.getWidth() * scale, friendHUD.getHeight() * scale);
        }

        if (keyCollected) {
            batch.draw(keyIcon, 187, 319, keyIcon.getWidth() * scale, keyIcon.getHeight() * scale);
        }

    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void incrementLives() {
        this.lives++;
    }

    public void decrementLives() {
        this.lives--;

    }

    public void setScore(int score) {
        this.score = score;
    }

    public void incrementScore(int amount) {
        this.score += amount;
    }

    public void decrementScore(int amount) {
        if (score - amount >= 0) {
            this.score -= amount;
        }
    }

    public int getLives() {
        return lives;
    }


    public void collectKey() {
        // Mark the key as collected
        if (!keyCollected) {
            keyCollected = true;
        }
    }



    public void dispose() {
        HUDpanel.dispose();
        friendHUD.dispose();
        font.dispose();
    }
}