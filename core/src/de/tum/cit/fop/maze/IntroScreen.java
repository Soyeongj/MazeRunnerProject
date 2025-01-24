package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class IntroScreen implements Screen {

    private final Game game;
    private final Texture introImage;
    private final float introDuration = 3f; // 10 seconds
    private float elapsedTime = 0f;
    private final String mapPath; // Store the map path

    public IntroScreen(Game game, String mapPath) {
        this.game = game;
        this.introImage = new Texture(Gdx.files.internal("intro.png"));
        this.mapPath = mapPath; // Initialize the map path
        SoundManager.playGameStartSound(); // Play intro sound
    }

    @Override
    public void show() {
        SoundManager.stopMenuMusic();
    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;

        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the intro image
        SpriteBatch batch = new SpriteBatch();
        batch.begin();
        batch.draw(introImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        // Check if the intro duration has elapsed
        if (elapsedTime >= introDuration) {
            SoundManager.playBackgroundMusic(); // Play background music
            game.setScreen(new GameScreen((MazeRunnerGame) game, mapPath, true)); // Switch to GameScreen with the selected map
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        introImage.dispose();
    }
}
