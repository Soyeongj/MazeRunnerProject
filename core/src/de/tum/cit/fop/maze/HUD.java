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
import de.tum.cit.fop.maze.Player;


public class HUD {

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
    private float scoreTimer;
    private boolean isGameRunning = true;
    private float finalTime;


    public HUD() {

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

    public float getGlobalTimer() {
        return globalTimer;
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


    public void startTimer() {
        isGameRunning = true;
    }

    public void render(SpriteBatch batch, Player player) {
        batch.setProjectionMatrix(hudCamera.combined);

        font.draw(batch, "Remaining Friends: " + lives, screenWidth * 0.07f, screenHeight - 30);
        font.draw(batch, "Key Collected: " + (keyCollected ? "Yes" : "No"), screenWidth * 0.6f, screenHeight - 39);
        font.draw(batch, "Time: " +  (int) scoreTimer, screenWidth * 0.6f, screenHeight - 105); // Display timer


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

    public void needKey() {
        message = "You need a key!";
        messageTimer = 1f;
    }

    public void pressE() {
        message = "Press E to open!";
        messageTimer = 1f;
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

    public void setScoreTimer(float scoreTimer) {
        this.scoreTimer = scoreTimer;
    }

    public float getScoreTimer() {
        return scoreTimer;
    }

    public void dispose() {

        font.dispose();
    }
}