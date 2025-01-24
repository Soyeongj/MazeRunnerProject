package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * The MenuScreen class is responsible for displaying the main menu of the game.
 * It extends the LibGDX Screen class and sets up the UI components for the menu.
 */
public class MenuScreen implements Screen {
    private final MazeRunnerGame game;
    private final Stage stage;

    /**
     * Constructor for MenuScreen. Sets up the camera, viewport, stage, and UI elements.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public MenuScreen(MazeRunnerGame game) {
        this.game = game;
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false); // Reset the camera to default orthographic view
        camera.zoom = 1.0f; // Use default zoom level

        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.add(new Label("Menu Screen", game.getSkin(), "title")).padBottom(80).row();
        TextButton map1Button = new TextButton("Play Map 1", game.getSkin());
        table.add(map1Button).width(300).row();


        map1Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setCurrentMapPath("map1.tmx");

                game.setScreen(new GameScreen(game, "map1.tmx",true));
            }
        });

        TextButton map2Button = new TextButton("Play Map 2", game.getSkin());
        table.add(map2Button).width(300).row();

        map2Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setCurrentMapPath("map2.tmx");
                game.setScreen(new GameScreen(game, "map2.tmx",true));
            }
        });


        TextButton map3Button = new TextButton("Play Map 3", game.getSkin());
        table.add(map3Button).width(300).row();

        map3Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setCurrentMapPath("map3.tmx");
                game.setScreen(new GameScreen(game, "map3.tmx",true));
            }
        });

        TextButton map4Button = new TextButton("Play Map 4", game.getSkin());
        table.add(map4Button).width(300).row();

        map4Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setCurrentMapPath("map4.tmx");
                game.setScreen(new GameScreen(game, "map4.tmx",true));
            }
        });

        TextButton map5Button = new TextButton("Play Map 5", game.getSkin());
        table.add(map5Button).width(300).row();

        map5Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setCurrentMapPath("map5.tmx");
                game.setScreen(new GameScreen(game, "map5.tmx",true));
            }
        });


        TextButton exitButton = new TextButton("Exit", game.getSkin());
        table.add(exitButton).width(300).row();
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        TextButton resumeButton = new TextButton("Continue To Play", game.getSkin());
        table.add(resumeButton).width(300).row();
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String lastMapPath = game.getCurrentMapPath();

                if (lastMapPath == null) {
                    lastMapPath = "map1.tmx";
                    game.setCurrentMapPath(lastMapPath);
                }

                Screen currentScreen = game.getScreen();

                if (currentScreen instanceof GameScreen) {
                    GameScreen gameScreen = (GameScreen) currentScreen;
                    gameScreen.loadState();
                } else {
                    GameScreen gameScreen = new GameScreen(game, lastMapPath,false);
                    gameScreen.loadState();
                    game.setScreen(gameScreen);
                }
            }
        });

    }


    @Override
    /**
     * Renders the menu screen by clearing the screen and updating and drawing the UI stage.
     *
     * @param delta The time in seconds since the last render, used for managing frame rate and smooth animations.
     */
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
        stage.draw(); // Draw the stage
    }

    @Override
    /**
     * Resizes the stage viewport when the screen is resized.
     * This ensures the stage elements are properly scaled when the window size changes.
     *
     * @param width  The new width of the screen.
     * @param height The new height of the screen.
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    @Override
    public void dispose() {
        /**
         * Disposes of the resources used by the menu screen, particularly the stage.
         * This is called when the screen is no longer needed, freeing up memory.
         */
        stage.dispose();
    }

    @Override
    /**
     * Initializes the input processor and handles the background music when the menu screen is displayed.
     * This method is called when the menu screen is shown, allowing for input and music to be managed.
     */
    public void show() {
        Gdx.input.setInputProcessor(stage);
        SoundManager.stopBackgroundMusic();
        SoundManager.playMenuMusic();
    }

    // The following methods are part of the Screen interface but are not used in this screen.
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