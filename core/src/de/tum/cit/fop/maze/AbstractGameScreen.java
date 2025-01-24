package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Abstract base class for game screens, implementing common functionality
 * for rendering, screen management, and user input handling.
 *
 * This class provides utilities for displaying a background, rendering text,
 * managing fade effects, and handling basic user inputs such as navigation
 * to the menu or exiting the game.
 *
 */
public abstract class AbstractGameScreen implements Screen {
    // Constants
    private static final float FADE_SPEED = 0.5f;
    private static final float DEFAULT_TEXTURE_WIDTH_SCALE = 4.0f;
    private static final float DEFAULT_TEXTURE_HEIGHT_SCALE = 3.0f;
    private static final float FONT_SCALE = 2.5f;

    // Screen Components
    protected final MazeRunnerGame game;
    protected final SpriteBatch batch;
    protected final BitmapFont font;

    // Screen Properties
    protected final OrthographicCamera camera;
    protected float screenWidth;
    protected float screenHeight;

    // Visual Assets
    protected Texture backgroundTexture;
    protected float textureWidthScale;
    protected float textureHeightScale;

    // Game State
    protected float fadeAlpha;
    protected final float finalTime;

    /**
     * Constructs an AbstractGameScreen with the specified game instance,
     * background texture, and final score.
     *
     * @param game             the game instance managing the screen
     * @param backgroundTexture the texture to display as the background
     * @param finalTime        the player's final score or completion time
     */
    public AbstractGameScreen(MazeRunnerGame game, Texture backgroundTexture, float finalTime) {
        this.game = game;
        this.backgroundTexture = backgroundTexture;
        this.finalTime = finalTime;

        // Initialize graphics components
        this.batch = new SpriteBatch();
        this.font = initializeFont();
        this.camera = new OrthographicCamera();

        // Set default scales
        this.textureWidthScale = DEFAULT_TEXTURE_WIDTH_SCALE;
        this.textureHeightScale = DEFAULT_TEXTURE_HEIGHT_SCALE;

        // Initialize screen
        setScreenDimensions(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Initializes and configures the BitmapFont for text rendering.
     *
     * @return the initialized BitmapFont instance
     */
    private BitmapFont initializeFont() {
        BitmapFont font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(FONT_SCALE);
        return font;
    }

    /**
     * Renders the game screen, including the background and UI components.
     * Handles user input during the rendering process.
     *
     * @param delta the time in seconds since the last render
     */
    @Override
    public void render(float delta) {
        updateFade(delta);
        renderScreen();
        handleInput();
    }

    /**
     * Updates the fade effect for screen transitions.
     *
     * @param delta the time in seconds since the last update
     */
    private void updateFade(float delta) {
        if (fadeAlpha < 1f) {
            fadeAlpha = Math.min(fadeAlpha + FADE_SPEED * delta, 1f);
        }
    }

    /**
     * Handles the main rendering logic for the screen, including
     * background and UI elements.
     */
    private void renderScreen() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        ScreenUtils.clear(Color.BLACK);
        renderBackground();
        renderUI();

        batch.end();
    }

    /**
     * Renders the background texture with scaling and fade effects.
     */
    private void renderBackground() {
        float scaledWidth = backgroundTexture.getWidth() * textureWidthScale;
        float scaledHeight = backgroundTexture.getHeight() * textureHeightScale;
        float x = (screenWidth - scaledWidth) / 2;
        float y = (screenHeight - scaledHeight) / 2;

        batch.setColor(1f, 1f, 1f, fadeAlpha);
        batch.draw(backgroundTexture, x, y, scaledWidth, scaledHeight);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Renders UI components such as instructions and the player's score.
     */
    private void renderUI() {
        drawText("Press ENTER to Go to Menu or ESC to Quit", screenWidth * 0.2f, screenHeight - 20);
        drawText("Your Score: " + (int) finalTime, screenWidth * 0.2f, screenHeight - 85);
    }

    /**
     * Handles user input to navigate to the menu or exit the game.
     */
    private void handleInput() {
        if (fadeAlpha >= 1f) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.goToMenu();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
            }
        }
    }

    /**
     * Draws the specified text at the given screen position.
     *
     * @param text the text to draw
     * @param x    the x-coordinate for the text
     * @param y    the y-coordinate for the text
     */
    protected void drawText(String text, float x, float y) {
        font.draw(batch, text, x, y);
    }

    /**
     * Sets the screen dimensions and updates the camera to match.
     *
     * @param width  the width of the screen
     * @param height the height of the screen
     */
    protected void setScreenDimensions(float width, float height) {
        this.screenWidth = width;
        this.screenHeight = height;
        updateCamera();
    }

    /**
     * Updates the camera to match the current screen dimensions.
     */
    private void updateCamera() {
        camera.setToOrtho(false, screenWidth, screenHeight);
        camera.position.set(screenWidth / 2f, screenHeight / 2f, 0);
        camera.update();
    }

    /**
     * Called when the screen is resized.
     *
     * @param width  the new width of the screen
     * @param height the new height of the screen
     */
    @Override
    public void resize(int width, int height) {
        setScreenDimensions(width, height);
    }

    /**
     * Called when the screen is shown. Resets the fade effect.
     */
    @Override
    public void show() {
        fadeAlpha = 0f;
    }

    /**
     * Disposes of resources used by the screen, including the batch, font, and background texture.
     */
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }

    // Unused Screen interface methods
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}