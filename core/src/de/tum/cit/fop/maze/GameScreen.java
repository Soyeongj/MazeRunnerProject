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

import java.util.List;
import java.util.ArrayList;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
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



    /**
     * Constructor for GameScreen. Sets up the camera and Tiled map.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game) {
        this.game = game;

        // Create and configure the camera for the game view
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.zoom = 0.2f; // Zoom in to focus on the map's center

        // Load Tiled map
        tiledMap = new TmxMapLoader().load("map1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);


        centerCameraOnMap();

        hud = new HUD();
        this.friends = new Friends();
        player = new Player(155, 259, (TiledMapTileLayer) tiledMap.getLayers().get(0));
        griever = new Griever(118, 283, (TiledMapTileLayer) tiledMap.getLayers().get(0));
        batch = new SpriteBatch();

        griever.setScale(0.2f);
        friends.setScale(0.2f);

        //load moving wall layer
        movingWallsLayer = (TiledMapTileLayer) tiledMap.getLayers().get("moving walls");
        initializeWalls();
    }
    private void initializeWalls() {
        walls = new ArrayList<>();

        for (int x = 0; x < movingWallsLayer.getWidth(); x++) {
            for (int y = 0; y < movingWallsLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = movingWallsLayer.getCell(x, y);
                if (cell != null && cell.getTile().getProperties().containsKey("direction")) {
                    String direction = cell.getTile().getProperties().get("direction", String.class);
                    walls.add(new Wall(x, y, direction));
                    System.out.println("Wall initialized at x=" + x + ", y=" + y + ", direction=" + direction);
                }
            }
        }

        System.out.println("Total walls initialized: " + walls.size());
    }



    /**
     * Centers the camera on the map based on its dimensions and logs debug information.
     */
    private void centerCameraOnMap() {

        int tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class); // Pixel width per tile
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class); // Pixel height per tile
        int mapWidth = tiledMap.getProperties().get("width", Integer.class); // Tile count width
        int mapHeight = tiledMap.getProperties().get("height", Integer.class); // Tile count height

        // Calculate map center in pixels
        float centerX = (mapWidth * tileWidth) / 2f;
        float centerY = (mapHeight * tileHeight) / 2f;

        // Set camera position to the center of the map
        camera.position.set(centerX, centerY, 0);
        camera.update(); // Apply the updated position
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
            wall.update(currentGlobalTime, movingWallsLayer);
        }


        camera.update(); // Update the camera

        batch.begin();

        // Render the Tiled map
        mapRenderer.setView(camera);
        mapRenderer.render();

        //이동벽만 별도로 렌더링
        mapRenderer.getBatch().begin();

// 디버깅 코드 추가: movingWallsLayer 렌더링 전
        System.out.println("Rendering movingWallsLayer...");
        mapRenderer.renderTileLayer(movingWallsLayer); // "moving walls" 레이어만 렌더링
        System.out.println("Finished rendering movingWallsLayer.");

        mapRenderer.getBatch().end();

        boolean moveUp = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean moveDown = Gdx.input.isKeyPressed(Input.Keys.S);
        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean runKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);



        // Update and render the player
        player.update(delta, moveUp, moveDown, moveLeft, moveRight, runKeyPressed);
        player.render(batch);

        griever.update(delta, player.getX(), player.getY(), player.getDirection());
        griever.render(batch);

        friends.render(batch);
        hud.render(batch);


        Vector2 playerPosition = new Vector2(player.getX(), player.getY());
        int savedFriends = friends.checkAndSaveAllFriends(playerPosition, 3f);
        hud.render(batch);
        for (int i = 0; i < savedFriends; i++) {
            hud.incrementLives();
        }

        // Add collision detection(player-griever) and lives management logic
        int diffX = (int) (player.getX() - griever.getX());
        int diffY = (int) (player.getY() - griever.getY());
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

        if (LivesCoolDownTimer <= 0 && distance < 10f && griever.isGrieverNotStunned()) {
            if (hud.getLives() > 1) {
                hud.decrementLives();
                LivesCoolDownTimer = 7;
            } else {
                hud.setLives(0);
                player.revertToPrevious();
                player.setDead();
            }
        }

        // Decrease cooldown timer
        if (LivesCoolDownTimer > 0) {
            LivesCoolDownTimer -= delta;
        }

        batch.end();




    }


    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.setToOrtho(false);
        centerCameraOnMap();
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
    }
}