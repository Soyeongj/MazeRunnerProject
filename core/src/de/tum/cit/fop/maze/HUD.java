package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.Color;



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

    private float screenWidth;
    private float screenHeight;

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
        setScreenDimensions(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void setScreenDimensions(float width, float height) {
        this.screenWidth = width;
        this.screenHeight = height;
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.position.set(screenWidth / 2f, screenHeight / 2f, 0);
        hudCamera.update();
    }

    public void updateTimer(float delta) {
        globalTimer += delta;
    }

    public float getGlobalTimer() {
        return globalTimer;
    }

    public void render(SpriteBatch batch, Player player) {
        batch.setProjectionMatrix(hudCamera.combined);

        font.draw(batch, "Score: " + score, screenWidth * 0.07f, screenHeight - 100);
        font.draw(batch, "Remaining Friends: " + lives, screenWidth * 0.07f, screenHeight - 30);
        font.draw(batch, "Key Collected: " + (keyCollected ? "Yes" : "No"), screenWidth * 0.6f, screenHeight - 39);

        if (messageTimer > 0) {
            font.draw(batch, message, player.getX(), player.getY() + player.getHeight() + 10);
            messageTimer -= Gdx.graphics.getDeltaTime();
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
        if (!keyCollected) {
            keyCollected = true;
        }
    }

    public boolean isKeyCollected() {
        return keyCollected;
    }

    public void setKeyCollected(boolean keyCollected) {
        this.keyCollected = keyCollected;
    }

    public void dispose() {
        HUDpanel.dispose();
        friendHUD.dispose();
        font.dispose();
    }
}