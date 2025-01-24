package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;

/**
 * IntroScreen class renders the background texture and manages viewport and camera
 * adjustments to align the texture with the resized screen.
 */
public class IntroScreen implements Screen {

    // Constants for default scaling
    private static final float DEFAULT_TEXTURE_WIDTH_SCALE = 4.0f;
    private static final float DEFAULT_TEXTURE_HEIGHT_SCALE = 3.0f;

    // Screen Components
    private final MazeRunnerGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;

    // Visual Assets
    private Texture backgroundTexture;
    private float textureWidthScale;
    private float textureHeightScale;

    // Screen Properties
    private float screenWidth;
    private float screenHeight;

    // Intro Screen State
    private boolean isShowingIntro;
    private float introDuration = 3f; // Duration of the intro screen in seconds

    public IntroScreen(MazeRunnerGame game, Texture backgroundTexture) {
        this.game = game;
        this.backgroundTexture = backgroundTexture;

        // Initialize graphics components
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();

        // Set default scales
        this.textureWidthScale = DEFAULT_TEXTURE_WIDTH_SCALE;
        this.textureHeightScale = DEFAULT_TEXTURE_HEIGHT_SCALE;

        // Initialize screen dimensions
        setScreenDimensions(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        SoundManager.stopMenuMusic();
        isShowingIntro = true;

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                isShowingIntro = false;
                SoundManager.playBackgroundMusic();
                game.setScreen(game); // Return to the main game screen
            }
        }, introDuration);
    }

    @Override
    public void render(float delta) {
        if (isShowingIntro) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            renderBackground();
            batch.end();
        }
    }

    private void renderBackground() {
        float scaledWidth = backgroundTexture.getWidth() * textureWidthScale;
        float scaledHeight = backgroundTexture.getHeight() * textureHeightScale;
        float x = (screenWidth - scaledWidth) / 2;
        float y = (screenHeight - scaledHeight) / 2;

        ScreenUtils.clear(0, 0, 0, 1);
        batch.draw(backgroundTexture, x, y, scaledWidth, scaledHeight);
    }

    private void setScreenDimensions(float width, float height) {
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
    public void hide() {
        isShowingIntro = false;
    }

    @Override
    public void pause() {
        // Placeholder for future functionality if required
    }

    @Override
    public void resume() {
        // Placeholder for future functionality if required
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}