package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;
import java.util.ArrayList;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private Viewport viewport;
    private final TiledMap tiledMap;
    private TiledMapTileLayer movingWallsLayer;
    private List<Wall> walls;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private Player player;
    private Friends friends;
    private HUD hud;
    private SpriteBatch batch;
    private Griever griever;
    private float LivesCoolDownTimer = 0f;
    private Key key;
    private Item item;



    /**
     * Constructor for GameScreen. Sets up the camera and Tiled map.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;


        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        camera.zoom = 0.2f; // Zoom in to focus on the map's center


        tiledMap = new TmxMapLoader().load("map1.tmx");
        TiledMapTileLayer wallsLayer = (TiledMapTileLayer) tiledMap.getLayers().get("walls");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);


        centerCameraOnMap();

        hud = new HUD();
        this.friends = new Friends();
        this.item = new Item();
        player = new Player(155, 259, (TiledMapTileLayer) tiledMap.getLayers().get(0));
        griever = new Griever(87, 160, (TiledMapTileLayer) tiledMap.getLayers().get(0));
        batch = new SpriteBatch();

        friends.setScale(0.2f);

        this.key = new Key(189,286);


        movingWallsLayer = tiledMap.getLayers().get("moving walls") instanceof TiledMapTileLayer
                ? (TiledMapTileLayer) tiledMap.getLayers().get("moving walls")
                : null;
        if (movingWallsLayer != null) {
            initializeWalls(batch, movingWallsLayer);
        }
    }
    private void initializeWalls(SpriteBatch spriteBatch, TiledMapTileLayer movingWallsLayer) {
        walls = new ArrayList<>();

        for (int x = 0; x < movingWallsLayer.getWidth(); x++) {
            for (int y = 0; y < movingWallsLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = movingWallsLayer.getCell(x, y);
                if (cell != null && cell.getTile().getProperties().containsKey("direction")) {
                    String direction = cell.getTile().getProperties().get("direction", String.class);
                    walls.add(new Wall(x, y, direction, movingWallsLayer, griever, hud));
                }
            }
        }
    }


    /**
     * Centers the camera on the map based on its dimensions and logs debug information.
     */
    private void centerCameraOnMap() {

        int tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class); // Pixel width per tile
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class); // Pixel height per tile
        int mapWidth = tiledMap.getProperties().get("width", Integer.class); // Tile count width
        int mapHeight = tiledMap.getProperties().get("height", Integer.class); // Tile count height


        float centerX = (mapWidth * tileWidth) / 2f;
        float centerY = (mapHeight * tileHeight) / 2f;


        camera.position.set(centerX, centerY, 0);
        camera.update();
    }

    private void updateCameraPosition() {

        float playerX = player.getX();
        float playerY = player.getY();


        float cameraHalfWidth = camera.viewportWidth / 2f;
        float cameraHalfHeight = camera.viewportHeight / 2f;

        int mapWidth = tiledMap.getProperties().get("width", Integer.class) * tiledMap.getProperties().get("tilewidth", Integer.class);
        int mapHeight = tiledMap.getProperties().get("height", Integer.class) * tiledMap.getProperties().get("tileheight", Integer.class);

        float cameraX = Math.max(cameraHalfWidth, Math.min(playerX, mapWidth - cameraHalfWidth));
        float cameraY = Math.max(cameraHalfHeight, Math.min(playerY, mapHeight - cameraHalfHeight));

        camera.position.set(cameraX, cameraY, 0);
    }


    @Override
    public void render(float delta) {
        // Check for escape key press to go back to the menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }

        hud.updateTimer(delta);

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen

        float currentGlobalTime = hud.getGlobalTimer();

        //update moving walls
        for (Wall wall : walls) {
            wall.update(delta,currentGlobalTime);
            wall.checkAndMovePlayer(player, currentGlobalTime);
        }

        camera.position.set(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();


        mapRenderer.setView(camera);
        mapRenderer.render();
        mapRenderer.getBatch().begin();
        mapRenderer.renderTileLayer(movingWallsLayer);
        mapRenderer.getBatch().end();

        boolean moveUp = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean moveDown = Gdx.input.isKeyPressed(Input.Keys.S);
        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean runKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);


        player.update(delta, moveUp, moveDown, moveLeft, moveRight, runKeyPressed);
        player.render(batch);

        griever.update(delta, player.getX(), player.getY(), player.getDirection(),hud);
        griever.render(batch);


        boolean isGrieverDead = false;

        if (key == null) {
            key = new Key(189, 286);
        }

        for (Wall wall : walls) {
            if (wall.isGrieverDead()) {
                isGrieverDead = true;
                key.setPosition(wall.getKeySpawnPosition().x, wall.getKeySpawnPosition().y);
                break;
            }
        }


        if (isGrieverDead && key != null) {
            key.render(batch);
        }


        if (key != null && isGrieverDead) {
            key.checkProximityToPlayer(player);
            if (key.isCollected()) {
                hud.collectKey();
                key.setPosition(-1000, -1000);
            }


        }


        if (hud.getLives() <= 0) {
            game.setScreen(new GameOverScreen(game)); // Transition to Game Over screen
        }

        friends.render(batch);
        item.render(batch);
        hud.render(batch, player);



        Vector2 playerPosition = new Vector2(player.getX(), player.getY());
        int savedFriends = friends.checkAndSaveAllFriends(playerPosition, 3f);
        int count = item.checkAndCollectAllItmes(playerPosition, 3f);
        hud.render(batch, player);
        for (int i = 0; i < savedFriends; i++) {
            hud.incrementLives();
        }
        for (int i = 0; i < count; i++) {
            player.increaseSpeed(3f);
        }

        int diffX = (int) (player.getX() - griever.getMonsterX());
        int diffY = (int) (player.getY() - griever.getMonsterY());
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

        if (LivesCoolDownTimer <= 0 && distance < 5f && griever.isGrieverNotStunned()) {
            if (hud.getLives() > 1) {
                hud.decrementLives();
                player.triggerRedEffect();
                LivesCoolDownTimer = 7;
            } else {
                hud.setLives(0);
                player.revertToPrevious();
                player.setDead();
            }
        }


        if (LivesCoolDownTimer > 0) {
            LivesCoolDownTimer -= delta;
        }







        batch.end();


    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        hud.setScreenDimensions(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        tiledMap.dispose();
        mapRenderer.dispose();
        batch.dispose();
        player.dispose();
        hud.dispose();
        friends.dispose();
        griever.dispose();
        hud.dispose();
    }}