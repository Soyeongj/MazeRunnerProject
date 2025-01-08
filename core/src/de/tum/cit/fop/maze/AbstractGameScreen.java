package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class AbstractGameScreen implements Screen {

    //Instance Variables
    protected final MazeRunnerGame game;
    protected final SpriteBatch batch;
    protected final BitmapFont font;
    protected final Texture texture;
    protected float fadeAlpha;
    private static final float FADE_SPEED = 0.5f;
    protected float finalTime;

    //Screen Layout Constants
    protected final float textureWidthScale = 4.0f;
    protected final float textureHeightScale = 3.0f;
    protected final float textOffsetX = 370f;
    protected final float textOffsetY = 150f;

    public AbstractGameScreen(MazeRunnerGame game, Texture texture, float finalTime) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.texture = texture;
        this.fadeAlpha = 0f;
        this.finalTime = finalTime;
    }

    @Override
    public void show() {
        fadeAlpha = 0f;
    }

    @Override
    public void render(float delta) {
        //fade effect
        if (fadeAlpha < 1f) {
            fadeAlpha += FADE_SPEED * delta;
            fadeAlpha = Math.min(fadeAlpha, 1f);
        }

        batch.begin();

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float textureWidth = texture.getWidth();
        float textureHeight = texture.getHeight();

        float scaledTextureWidth = textureWidth * textureWidthScale;
        float scaledTextureHeight = textureHeight * textureHeightScale;

        batch.setColor(1f, 1f, 1f, fadeAlpha);
        batch.draw(texture, (screenWidth - scaledTextureWidth) / 2, (screenHeight - scaledTextureHeight) / 2,
                scaledTextureWidth, scaledTextureHeight);

        batch.setColor(1f, 1f, 1f, 1f);
        font.getData().setScale(2.5f);
        drawText("Press ENTER to Go to Menu or ESC to Quit", screenWidth / 2 - textOffsetX, screenHeight / 2 - textOffsetY);
        drawText("Your Score: " + (int) finalTime, screenWidth / 2 - 190, screenHeight / 2 - 230);

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

    protected void drawText(String text, float x, float y) {
        font.draw(batch, text, x, y);
    }

    @Override
    public void dispose() {
        batch.dispose();
        texture.dispose();
        font.dispose();
    }

    @Override
    public void resize(int width, int height) {
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
