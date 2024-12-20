package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
    private Player playerObject;
    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private SpriteBatch batch;
    private Wall wall;
    private Friends friends;
    private HUD hud;
    private Griever griever;
    private float livesCoolDownTimer;
    float deathTransitionTimer = 0;


    public GameScreen(MazeRunnerGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false);
        this.camera.zoom = 0.75f;

        this.batch = game.getSpriteBatch();

        this.playerObject = new Player(320, 120);
        this.wall = new Wall("wall.png", 200, 200);
        this.friends = new Friends();
        this.hud = new HUD();
        this.griever = new Griever(400,400);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(1, 1, 1, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }

        batch.begin();

        float previousX = playerObject.getX();
        float previousY = playerObject.getY();

        boolean moveUp = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean moveDown = Gdx.input.isKeyPressed(Input.Keys.S);
        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean runKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

        playerObject.update(delta, moveUp, moveDown, moveLeft, moveRight, runKeyPressed);
        playerObject.render(batch);

        griever.render(batch);
        float playerX = playerObject.getX();
        float playerY = playerObject.getY();
        String playerDirection = playerObject.getDirection();
        griever.update(delta,playerX,playerY,playerDirection);

        wall.render(batch);
        if (wall.getBrickRect().overlaps(playerObject.getBoundingBox())) {
            playerObject.setX(previousX);
            playerObject.setY(previousY);
        }

        friends.render(batch);
        Vector2 playerPosition = new Vector2(playerObject.getX(), playerObject.getY());
        int savedFriends = friends.checkAndSaveAllFriends(playerPosition, 10f);
        if (savedFriends > 0) {
            for (int i = 0; i < savedFriends; i++) {
                hud.incrementLives();
            }
        }

        hud.render(batch);


        float monsterX = griever.getX();
        float monsterY = griever.getY();
        int diffX = (int) (playerX - monsterX);
        int diffY = (int) (playerY - monsterY);
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);
        boolean isGrieverStunned = false;

        if (livesCoolDownTimer <= 0 && distance < 30 && !isGrieverStunned) {
            if (hud.getLives() > 1) {
                hud.decrementLives();
                livesCoolDownTimer = 7;
            } else {
                hud.setLives(0);
                playerObject.setDead();
                playerObject.setX(previousX);
                playerObject.setY(previousY);
                playerObject.setDead();
            }
        }

        if (playerObject.isDead()) {
            deathTransitionTimer += delta;
            if (deathTransitionTimer >= 1f) {
                game.goToMenu();
            }

        }


        if (livesCoolDownTimer > 0) {
            livesCoolDownTimer -= delta;
        }




        batch.end();
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

    @Override
    public void dispose() {
        playerObject.dispose();
        wall.dispose();
        friends.dispose();
        hud.dispose();
        griever.dispose();
    }
}
