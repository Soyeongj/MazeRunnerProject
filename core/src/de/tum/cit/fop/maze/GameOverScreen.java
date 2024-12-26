package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;

public class GameOverScreen implements Screen {

    private final MazeRunnerGame game;
    private final SpriteBatch batch;
    private final Texture gameOverTexture;
    private final BitmapFont font;

    private final float textureWidthScale = 4.0f;
    private final float textureHeightScale = 3.0f;

    private final float textOffsetX = 370f;
    private final float textOffsetY = 150f;

    public GameOverScreen(MazeRunnerGame game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.gameOverTexture = new Texture("gameover.jpg");
        font.setColor(Color.WHITE);
    }

    @Override
    public void show() {
        // Perform any setup needed
    }

    @Override
    public void render(float delta) {
        batch.begin();

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float textureWidth = gameOverTexture.getWidth();
        float textureHeight = gameOverTexture.getHeight();

        float scaledTextureWidth = textureWidth * textureWidthScale;
        float scaledTextureHeight = textureHeight * textureHeightScale;

        batch.draw(gameOverTexture, (screenWidth - scaledTextureWidth) / 2, (screenHeight - scaledTextureHeight) / 2,
                scaledTextureWidth, scaledTextureHeight);

        font.getData().setScale(2.5f);  // Further increase text size for the options
        font.draw(batch, "Press ENTER to Restart or ESC to Quit", screenWidth / 2 - textOffsetX, screenHeight / 2 - textOffsetY);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game)); // Restart the game
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
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
        gameOverTexture.dispose();
        font.dispose();
    }
}
