package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameOverScreen implements Screen {

    private final MazeRunnerGame game;
    private final SpriteBatch batch;
    private final Texture gameOverTexture;
    private final BitmapFont font;

    private final float textureWidthScale = 4.0f;
    private final float textureHeightScale = 3.0f;

    private final float textOffsetX = 370f;
    private final float textOffsetY = 150f;

    private float fadeAlpha;
    private static final float FADE_SPEED = 0.5f;
    private float finalTime;


    public GameOverScreen(MazeRunnerGame game, float finalTime) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.gameOverTexture = new Texture("gameover.jpg");
        this.fadeAlpha = 0f;
        font.setColor(Color.WHITE);
        this.finalTime = finalTime;
    }

    @Override
    public void show() {
        // Reset alpha when the screen is shown
        fadeAlpha = 0f;
    }

    @Override
    public void render(float delta) {
        if (fadeAlpha < 1f) {
            fadeAlpha += FADE_SPEED * delta;
            fadeAlpha = Math.min(fadeAlpha, 1f);
        }

        batch.begin();

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float textureWidth = gameOverTexture.getWidth();
        float textureHeight = gameOverTexture.getHeight();

        float scaledTextureWidth = textureWidth * textureWidthScale;
        float scaledTextureHeight = textureHeight * textureHeightScale;

        batch.setColor(1f, 1f, 1f, fadeAlpha);

        batch.draw(gameOverTexture, (screenWidth - scaledTextureWidth) / 2, (screenHeight - scaledTextureHeight) / 2,
                scaledTextureWidth, scaledTextureHeight);

        batch.setColor(1f, 1f, 1f, 1f);
        font.getData().setScale(2.5f);
        font.draw(batch, "Press ENTER to Go to Menu or ESC to Quit", screenWidth / 2 - textOffsetX, screenHeight / 2 - textOffsetY);
        font.draw(batch, "Your Score: " + (int) finalTime, screenWidth / 2 - 190, screenHeight / 2 - 230);
        batch.end();

        if (fadeAlpha >= 1f) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.goToMenu();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
            }
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