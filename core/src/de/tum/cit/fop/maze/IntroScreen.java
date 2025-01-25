package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * IntroScreen class handles the transition between map selection and game start.
 * It displays an intro image for a specified duration before transitioning to the game.
 */
public class IntroScreen implements Screen {
    private final MazeRunnerGame game;
    private final String mapPath;
    private final SpriteBatch batch;
    private final Texture introImage;
    private final float INTRO_DURATION = 7.0f; // Duration of intro screen in seconds

    // Viewport and camera for proper resizing
    private OrthographicCamera camera;
    private Viewport viewport;

    /**
     * Constructor for IntroScreen
     *
     * @param game The main game class
     * @param mapPath The path to the selected map
     */
    public IntroScreen(MazeRunnerGame game, String mapPath) {
        this.game = game;
        this.mapPath = mapPath;

        // Setup camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        camera.setToOrtho(false);

        this.batch = new SpriteBatch();
        this.introImage = new Texture(Gdx.files.internal("intro.png"));

        // Play game start sound
        SoundManager.playGameStartSound();
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        ScreenUtils.clear(0, 0, 0, 1);

        // Update camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Draw the intro image
        batch.begin();
        batch.draw(introImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.end();
    }

    @Override
    public void show() {
        // Stop menu music
        SoundManager.stopMenuMusic();

        // Schedule transition to game screen
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Transition to game screen with the selected map
                game.setScreen(new GameScreen(game, mapPath));

                // Start background game music
                SoundManager.playBackgroundMusic();

                // Dispose of this screen
                dispose();
            }
        }, INTRO_DURATION);
    }

    @Override
    public void resize(int width, int height) {
        // Update the viewport with new screen dimensions
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        introImage.dispose();
    }
}