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
 * Abstract base class for game screens implementing common functionality
 * for rendering, screen management, and user input handling.
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

    private BitmapFont initializeFont() {
        BitmapFont font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(FONT_SCALE);
        return font;
    }

    @Override
    public void render(float delta) {
        updateFade(delta);
        renderScreen();
        handleInput();
    }

    private void updateFade(float delta) {
        if (fadeAlpha < 1f) {
            fadeAlpha = Math.min(fadeAlpha + FADE_SPEED * delta, 1f);
        }
    }

    private void renderScreen() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        ScreenUtils.clear(Color.BLACK);
        renderBackground();
        renderUI();

        batch.end();
    }

    private void renderBackground() {
        float scaledWidth = backgroundTexture.getWidth() * textureWidthScale;
        float scaledHeight = backgroundTexture.getHeight() * textureHeightScale;
        float x = (screenWidth - scaledWidth) / 2;
        float y = (screenHeight - scaledHeight) / 2;

        batch.setColor(1f, 1f, 1f, fadeAlpha);
        batch.draw(backgroundTexture, x, y, scaledWidth, scaledHeight);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void renderUI() {
        drawText("Press ENTER to Go to Menu or ESC to Quit", screenWidth * 0.2f, screenHeight - 20);
        drawText("Your Score: " + (int) finalTime, screenWidth * 0.2f, screenHeight - 85);
    }

    private void handleInput() {
        if (fadeAlpha >= 1f) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.goToMenu();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
            }
        }
    }

    protected void drawText(String text, float x, float y) {
        font.draw(batch, text, x, y);
    }

    protected void setScreenDimensions(float width, float height) {
        this.screenWidth = width;
        this.screenHeight = height;
        updateCamera();
    }

    private void updateCamera() {
        camera.setToOrtho(false, screenWidth, screenHeight);
        camera.position.set(screenWidth / 2f, screenHeight / 2f, 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        setScreenDimensions(width, height);
    }

    @Override
    public void show() {
        fadeAlpha = 0f;
    }

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