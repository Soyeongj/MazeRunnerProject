package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.Preferences;


public class HUD  {
    //Constants
    private static final float MESSAGE_DISPLAY_DURATION = 4f;
    private static final String PREFERENCES_NAME = "HUDState";

    //Font and Display
    private BitmapFont font;
    private OrthographicCamera hudCamera;

    //Game State Variables
    private int lives;
    private float globalTimer;
    private boolean isGameRunning = true;
    private float finalTime;
    private float scoreTimer;
    private boolean keyCollected;


    //Screen Dimensions
    private float screenWidth;
    private float screenHeight;

    //Messages
    private String message = "";
    private float messageTimer = 0f;



    public HUD() {
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
        this.globalTimer = 0f;
        this.keyCollected = false;
        scoreTimer = 300;

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

    public void updateScoreTimer(float delta) {
        if (isGameRunning) {
            scoreTimer -= delta;
        }
    }
    public void stopTimer() {
        isGameRunning = false;
        finalTime = scoreTimer;

    }

    public float getFinalTime() {
        return finalTime;
    }
    public void render(SpriteBatch batch, Player player) {
        batch.setProjectionMatrix(hudCamera.combined);

        font.draw(batch, "Saved Friends: " + lives, screenWidth * 0.07f, screenHeight - 30);
        font.draw(batch, "Score: " + (1000 + (int)scoreTimer*5  + (getLives()*150)) , screenWidth * 0.07f, screenHeight - 105);
        font.draw(batch, "Key Collected: " + (keyCollected ? "Yes" : "No"), screenWidth * 0.5f, screenHeight - 30);
        font.draw(batch, "Time: " +  (int) scoreTimer, screenWidth * 0.5f, screenHeight - 105);

        if (messageTimer > 0) {
            font.draw(batch, message, player.getX(), player.getY() + player.getHeight() + 10);
            messageTimer -= Gdx.graphics.getDeltaTime();
        }
    }
    public void incrementLives() {
        this.lives++;
    }

    public void decrementLives() {
        this.lives--;
        message = "You lost a friend.";
        messageTimer = MESSAGE_DISPLAY_DURATION;

        SoundManager.playLostFriendSound();
    }

    public void stunMessage() {
        message = "You stunned griever!";
        messageTimer = 2f;
    }

    public void needFriend() {
        message = "You cannot leave without friends!";
        messageTimer = 1f;
    }

    public void saveHUDState() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        preferences.putInteger("lives", lives);
        preferences.putFloat("scoreTimer", scoreTimer);
        preferences.putBoolean("keyCollected", keyCollected);
        preferences.putBoolean("isGameRunning", isGameRunning);
        preferences.putFloat("finalTime", finalTime);
        preferences.putFloat("globalTimer", globalTimer);
        preferences.flush();
    }

    public void loadHUDState() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        lives = preferences.getInteger("lives", lives);
        scoreTimer = preferences.getFloat("scoreTimer", scoreTimer);
        keyCollected = preferences.getBoolean("keyCollected", keyCollected);
        isGameRunning = preferences.getBoolean("isGameRunning", isGameRunning);
        finalTime = preferences.getFloat("finalTime", finalTime);
        globalTimer = preferences.getFloat("globalTimer", globalTimer);
    }

    public float getGlobalTimer() {
        return globalTimer;
    }
    public float getScoreTimer() {
        return scoreTimer;
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
    public void setLives(int lives) {
        this.lives = lives;
    }

    public void dispose() {
        font.dispose();
    }
}