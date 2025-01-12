package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
    protected final float textureWidthScale = 4.0f;
    protected final float textureHeightScale = 3.0f;

    // HUD (Heads-up Display)
    private HUD hud; // Add HUD reference here

    // Constructor
    public AbstractGameScreen(MazeRunnerGame game, Texture texture, float finalTime) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.texture = texture;
        this.fadeAlpha = 0f;
        this.finalTime = finalTime;
        this.hud = new HUD();  // Initialize HUD
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
            fadeAlpha = Math.min(fadeAlpha, 1f); // Prevent fadeAlpha from exceeding 1f;
        }

        batch.begin();

        // Calculate screen and texture dimensions
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float textureWidth = texture.getWidth();
        float textureHeight = texture.getHeight();

        float scaledTextureWidth = textureWidth * textureWidthScale;
        float scaledTextureHeight = textureHeight * textureHeightScale;

        // Draw background texture with fade effect
        batch.setColor(1f, 1f, 1f, fadeAlpha); // The fadeAlpha determines the transparency level of the content being drawn.
        batch.draw(texture, (screenWidth - scaledTextureWidth) / 2, (screenHeight - scaledTextureHeight) / 2,
                scaledTextureWidth, scaledTextureHeight);

        // Draw HUD (pass screenWidth and screenHeight)
        hud.setScreenDimensions(screenWidth, screenHeight);  // Update HUD with new screen dimensions
        hud.render(batch, null); // Pass the player or any other necessary object to render, can be null if not needed

        // Draw text
        batch.setColor(1f, 1f, 1f, 1f);
        font.getData().setScale(2.5f);
        drawText("Press ENTER to Go to Menu or ESC to Quit", screenWidth * 0.05f, screenHeight - 120);
        drawText("Your Score: " + (int) finalTime, screenWidth * 0.05f, screenHeight - 200);

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
        hud.dispose(); // Dispose HUD when done
    }

    @Override
    public void resize(int width, int height) {
        // Pass screen size to HUD when resizing
        hud.setScreenDimensions(width, height);
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
