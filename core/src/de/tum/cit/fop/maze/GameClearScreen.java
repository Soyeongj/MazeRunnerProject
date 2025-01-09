package de.tum.cit.fop.maze;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;




public class GameClearScreen implements Screen {
    private final MazeRunnerGame game;
    private final SpriteBatch batch;
    private final Texture gameClearTexture;
    private final BitmapFont font;


    private final float textureWidthScale = 4.0f;
    private final float textureHeightScale = 3.0f;


    private final float textOffsetX = 370f;
    private final float textOffsetY = 150f;


    private float fadeAlpha; // Alpha value for fade effect
    private static final float FADE_SPEED = 0.5f; // Speed of the fade effect
    private float finalTime;


    public GameClearScreen(MazeRunnerGame game, float finalTime) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.gameClearTexture = new Texture("desert.png");
        this.fadeAlpha = 0f;
        font.setColor(Color.WHITE);
        this.finalTime = finalTime;
    }




    @Override
    public void show() {
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
        float textureWidth = gameClearTexture.getWidth();
        float textureHeight = gameClearTexture.getHeight();


        float scaledTextureWidth = textureWidth * textureWidthScale;
        float scaledTextureHeight = textureHeight * textureHeightScale;


        // Set batch color with alpha for fade effect
        batch.setColor(1f, 1f, 1f, fadeAlpha);


        // Draw the game over texture
        batch.draw(gameClearTexture, (screenWidth - scaledTextureWidth) / 2, (screenHeight - scaledTextureHeight) / 2,
                scaledTextureWidth, scaledTextureHeight);


        // Reset the batch color to draw text without transparency
        batch.setColor(1f, 1f, 1f, 1f);
        font.getData().setScale(2.5f); // Further increase text size for the options
        font.draw(batch, "Press ENTER to Go to Menu or ESC to Quit", screenWidth * 0.2f, screenHeight - 30);
        font.draw(batch, "Your Score: " + (int) finalTime, screenWidth * 0.2f, screenHeight - 105);
        batch.end();



        // Handle input
        if (fadeAlpha >= 1f) { // Allow input only after fade-in is complete
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.goToMenu();
            }


            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
            }
        }
    }


    @Override
    public void resize(int i, int i1) {


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


    @Override
    public void dispose() {
        batch.dispose();
        gameClearTexture.dispose();
        font.dispose();
    }
}



