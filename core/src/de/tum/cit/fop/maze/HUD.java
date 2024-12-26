package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

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
    private String message = "";
    private float messageTimer = 0f;
    private static final float MESSAGE_DISPLAY_DURATION = 4f;
    private OrthographicCamera hudCamera;



    public HUD() {
        this.HUDpanel = new Texture("sand.png");
        this.friendHUD = new Texture("oldman_right_1.png");
        this.keyIcon = new Texture("key.png");

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixel Game.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 40;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.BLACK;
        this.font = generator.generateFont(parameter);
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        generator.dispose();

        font.getData().setScale(2f);

        this.lives = 3;
        this.score = 0;
        this.globalTimer = 0f;
        this.keyCollected = false;

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        hudCamera.update();

    }
    public void updateTimer(float delta) {
        globalTimer += delta;
    }

    public float getGlobalTimer() {
        return globalTimer;
    }

    public void render(SpriteBatch batch, Player player) {
        // Set HUD camera
        batch.setProjectionMatrix(hudCamera.combined);

        // Render the HUD text
        font.draw(batch, "Score: " + score, 80, Gdx.graphics.getHeight() - 100);
        font.draw(batch, "Remaining Friends: " + lives, 80, Gdx.graphics.getHeight() - 30);
        font.draw(batch, "Key Collected: " + (keyCollected ? "Yes" : "No"), 800, Gdx.graphics.getHeight() - 40);

        if (messageTimer > 0) {
            font.draw(batch, message, player.getX(), player.getY() + player.getHeight() + 10);
            messageTimer -= Gdx.graphics.getDeltaTime();
        }

        // Draw the friend HUD icons
        for (int i = 0; i < lives; i++) {
            batch.draw(friendHUD, 120 + (i * 30), Gdx.graphics.getHeight() - 60, friendHUD.getWidth() * scale, friendHUD.getHeight() * scale);
        }

        if (keyCollected) {
            batch.draw(keyIcon, 20, Gdx.graphics.getHeight() - 90, keyIcon.getWidth() * scale, keyIcon.getHeight() * scale);
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

        message = "You lost a friend.";
        messageTimer = MESSAGE_DISPLAY_DURATION;

    }

    public void stunMessage() {
        message = "You stunned griever!";
        messageTimer = MESSAGE_DISPLAY_DURATION;
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