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
                String lastMapPath = game.getCurrentMapPath(); // 마지막으로 플레이한 맵 경로 가져오기

                if (lastMapPath == null) {
                    // 마지막 맵 경로가 없으면 초기 상태로 새 게임 시작
                    lastMapPath = "map1.tmx"; // 기본값 설정
                    game.setCurrentMapPath(lastMapPath); // 기본 맵 경로 저장
                }

                Screen currentScreen = game.getScreen();

                if (currentScreen instanceof GameScreen) {
                    GameScreen gameScreen = (GameScreen) currentScreen;
                    gameScreen.loadState(); // 현재 상태 불러오기
                } else {
                    GameScreen gameScreen = new GameScreen(game, lastMapPath,false); // 마지막 맵으로 GameScreen 생성
                    gameScreen.loadState(); // 상태 불러오기
                    game.setScreen(gameScreen); // 화면 설정
                }
            }
        });

    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Update the stage
        stage.draw(); // Draw the stage
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Update the stage viewport on resize
    }

    @Override
    public void dispose() {
        // Dispose of the stage when screen is disposed
        stage.dispose();
    }

    @Override
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