package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class AbstractGameScreen implements Screen {

    // Core Game Components
    protected final MazeRunnerGame game;
    protected final SpriteBatch batch;
    protected final BitmapFont font;
    protected final Texture texture;

    // Screen Effects and Animation Variables
    protected float fadeAlpha; // controls fade effect (current opacity of the screen content)
    private static final float FADE_SPEED = 0.5f; // speed of fade animation
    protected float finalTime; // player's final time(score)

    // Screen Layout Constants
    protected float textureWidthScale = 4.0f;
    protected float textureHeightScale = 3.0f;

    // Camera for screen layout adjustment
    protected OrthographicCamera screenCamera;

    // Variables for screen size
    protected float screenWidth;
    protected float screenHeight;

    // Constructor
    public AbstractGameScreen(MazeRunnerGame game, Texture texture, float finalTime) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.texture = texture;
        this.fadeAlpha = 0f;
        this.finalTime = finalTime;

        // Initialize camera for dynamic screen resizing
        screenCamera = new OrthographicCamera();
        setScreenDimensions(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        fadeAlpha = 0f; // Reset fade effect when screen becomes active
    }

    @Override
    public void render(float delta) {
        // Update fade effect
        if (fadeAlpha < 1f) {
            fadeAlpha += FADE_SPEED * delta;
            fadeAlpha = Math.min(fadeAlpha, 1f); // Prevent fadeAlpha from exceeding 1f
        }

        batch.setProjectionMatrix(screenCamera.combined);
        batch.begin();

        // Calculate screen and texture dimensions
        float textureWidth = texture.getWidth();
        float textureHeight = texture.getHeight();
        float scaledTextureWidth = textureWidth * textureWidthScale;
        float scaledTextureHeight = textureHeight * textureHeightScale;

        // Draw background texture with fade effect
        batch.setColor(1f, 1f, 1f, fadeAlpha); // The fadeAlpha determines the transparency level of the content being drawn.
        batch.draw(texture, (screenWidth - scaledTextureWidth) / 2, (screenHeight - scaledTextureHeight) / 2,
                scaledTextureWidth, scaledTextureHeight);

        // Draw text
        batch.setColor(1f, 1f, 1f, 1f);
        font.getData().setScale(2.5f);
        drawText("Press ENTER to Go to Menu or ESC to Quit", screenWidth * 0.2f, screenHeight - 30);
        drawText("Your Score: " + (int) finalTime, screenWidth * 0.2f, screenHeight - 105);

        batch.end();

        // Handle Input after fade completes
        if (fadeAlpha >= 1f) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.goToMenu();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
            }
        }
    }

    protected void drawText(String text, float x, float y) {
        font.draw(batch, text, x, y);
    }

    // Resource Clean Up
    @Override
    public void dispose() {
        batch.dispose();
        texture.dispose();
        font.dispose();
    }

    @Override
    public void resize(int width, int height) {
        setScreenDimensions(width, height);
    }

    protected void setScreenDimensions(float width, float height) {
        this.screenWidth = width;
        this.screenHeight = height;
        screenCamera.setToOrtho(false, screenWidth, screenHeight);
        screenCamera.position.set(screenWidth / 2f, screenHeight / 2f, 0);
        screenCamera.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
