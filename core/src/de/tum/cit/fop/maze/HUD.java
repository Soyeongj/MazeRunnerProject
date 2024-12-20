package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HUD {
    private Texture HUDpanel;
    private Texture friendHUD;
    private BitmapFont font;
    private int lives;
    private int score;

    public HUD() {
        this.HUDpanel = new Texture("sand.png");
        this.friendHUD = new Texture("oldman_right_1.png");
        this.font = new BitmapFont();
        this.lives = 3;
        this.score = 0;

    }

    public void render(SpriteBatch batch) {
        // Draw the HUD background
        for (int x = 0; x <= 1560; x += HUDpanel.getWidth()) {
            for (int y = 650; y <= 900; y += HUDpanel.getHeight()) {
                batch.draw(HUDpanel, x, y);
            }
        }

        font.draw(batch, "Score: " + score, 260, 730);

        font.draw(batch, "Friends: " + lives, 260, 690);

        // Draw the friend HUD icons
        for (int i = 0; i < lives; i++) {
            batch.draw(friendHUD, 340 + (i * 30), 677);
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

    public void dispose() {
        HUDpanel.dispose();
        friendHUD.dispose();
        font.dispose();
    }
}
