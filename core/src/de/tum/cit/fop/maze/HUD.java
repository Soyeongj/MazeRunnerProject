package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.Preferences;

/**
 * The HUD (Heads-Up Display) class manages and renders the game's user interface elements.
 * It handles information such as the player's lives, score, collected keys, and timers,
 * as well as displaying messages to the player.
 */
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

    /**
     * Constructor for the HUD class. Initializes the font, camera, and game state variables.
     */
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

    /**
     * Sets the dimensions of the screen and updates the HUD camera accordingly.
     *
     * @param width  The width of the screen.
     * @param height The height of the screen.
     */
    public void setScreenDimensions(float width, float height) {
        this.screenWidth = width;
        this.screenHeight = height;
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.position.set(screenWidth / 2f, screenHeight / 2f, 0);
        hudCamera.update();
    }

    /**
     * Updates the global timer, which tracks the time elapsed in the game.
     *
     * @param delta The time in seconds since the last frame.
     */
    public void updateTimer(float delta) {
        globalTimer += delta;
    }

    /**
     * Updates the score timer, which tracks the remaining time before the game ends.
     *
     * @param delta The time in seconds since the last frame.
     */
    public void updateScoreTimer(float delta) {
        if (isGameRunning) {
            scoreTimer -= delta;
        }
    }

    /**
     * Stops the timer and records the final time when the game ends.
     */
    public void stopTimer() {
        isGameRunning = false;
        finalTime = scoreTimer;

    }
    /**
     * Gets the final time when the game ends.
     *
     * @return The final time as a float.
     */
    public float getFinalTime() {
        return finalTime;
    }

    /**
     * Renders the HUD elements such as lives, score, key collected status, and timer.
     * It also displays messages if applicable.
     *
     * @param batch  The SpriteBatch used for rendering the HUD.
     * @param player The player object, used to display messages near the player.
     */
    public void render(SpriteBatch batch, Player player) {
        batch.setProjectionMatrix(hudCamera.combined);

        font.draw(batch, "Saved Friends: " + lives, screenWidth * 0.07f, screenHeight - 30);
        font.draw(batch, "Score: " + (1000 + (int)scoreTimer*5  + (getLives()*150)) , screenWidth * 0.07f, screenHeight - 105);
        font.draw(batch, "Key Collected: " + (keyCollected ? "Yes" : "No"), screenWidth * 0.6f, screenHeight - 30);
        font.draw(batch, "Time: " +  (int) scoreTimer, screenWidth * 0.6f, screenHeight - 105);

        if (messageTimer > 0) {
            font.draw(batch, message, player.getX(), player.getY() + player.getHeight() + 10);
            messageTimer -= Gdx.graphics.getDeltaTime();
        }
    }

    /**
     * Increments the number of lives by 1.
     */
    public void incrementLives() {
        this.lives++;
    }

    /**
     * Decrements the number of lives by 1 and displays a message.
     */
    public void decrementLives() {
        this.lives--;
        message = "You lost a friend.";
        messageTimer = MESSAGE_DISPLAY_DURATION;

        SoundManager.playLostFriendSound();
    }

    /**
     * Displays a message indicating that the player stunned a griever.
     */
    public void stunMessage() {
        message = "You stunned griever!";
        messageTimer = 2f;
    }

    /**
     * Displays a message indicating that the player cannot leave without friends.
     */
    public void needFriend() {
        message = "You cannot leave without friends!";
        messageTimer = 1f;
    }

    /**
     * Saves the current state of the HUD (lives, score timer, key collected, etc.)
     * to the preferences for persistence between sessions.
     */
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

    /**
     * Loads the saved state of the HUD from preferences.
     */
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

    /**
     * Disposes of the resources used by the HUD, such as the font.
     */
    public void dispose() {
        font.dispose();
    }
}