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



    public HUD() {
        this.HUDpanel = new Texture("sand.png");
        this.friendHUD = new Texture("oldman_right_1.png");
        this.keyIcon = new Texture("key.png");

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixel Game.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        OrthographicCamera hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        parameter.size = 15;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.BLACK;
        this.font = generator.generateFont(parameter);
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        generator.dispose();

        font.getData().setScale(0.5f);

        this.lives = 3;
        this.score = 0;
        this.globalTimer = 0f;
        this.keyCollected = false;


    }
    public void updateTimer(float delta) {
        globalTimer += delta;
    }

    public float getGlobalTimer() {
        return globalTimer;
    }

    public void render(SpriteBatch batch, Player player)  {
        // Draw the HUD background
        for (int x = 0; x <= 420; x += HUDpanel.getWidth()) {
            for (int y = 307; y <= 335; y += HUDpanel.getHeight()) {
                batch.draw(HUDpanel, x, y);
            }
        }

        font.draw(batch, "Score: " + score, 120, 313);

        font.draw(batch, "Remaining Friends: " + lives, 120, 323);

        font.draw(batch, "Key Collected: ", 250, 323);

        if (messageTimer > 0) {
            font.draw(batch, message, player.getX(), player.getY() + player.getHeight() + 10);
            messageTimer -= Gdx.graphics.getDeltaTime();
        }

        // Draw the friend HUD icons
        for (int i = 0; i < lives; i++) {
            batch.draw(friendHUD, 190 + (i * 5), 320,friendHUD.getWidth() * scale, friendHUD.getHeight() * scale);
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

        message = "You lost a friend.";
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