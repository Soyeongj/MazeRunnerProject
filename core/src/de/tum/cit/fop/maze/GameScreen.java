package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private Player player;
    private Friends friends;
    private HUD hud;
    private SpriteBatch batch;
    private Griever griever;
    private WallManager wallManager;
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
        this.friends = new Friends();

        // Load Tiled map
        tiledMap = new TmxMapLoader().load("map1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        // Center the camera on the map
        centerCameraOnMap();
        hud = new HUD();
        player = new Player(155,259);
        griever = new Griever(118,283);
        griever.setScale(0.2f);
        batch = new SpriteBatch();
        friends.setScale(0.2f);
        wallManager = new WallManager();
        int tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class); // Pixel width per tile
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class); // Pixel height per tile
        wallManager.addYWalls(177f,176,265,tileHeight,tileWidth,tileHeight, null);
        wallManager.addXWalls(160,144,174,tileWidth,tileHeight,tileWidth,null);



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

        // Debugging outputs
        System.out.println("Map Properties:");
        System.out.println("  Map Width (tiles): " + mapWidth);
        System.out.println("  Map Height (tiles): " + mapHeight);
        System.out.println("  Tile Width (pixels): " + tileWidth);
        System.out.println("  Tile Height (pixels): " + tileHeight);
        System.out.println("Calculated Center:");
        System.out.println("  Center X: " + centerX);
        System.out.println("  Center Y: " + centerY);

        // Set camera position to the center of the map
        camera.position.set(centerX, centerY, 0);
        camera.update(); // Apply the updated position
    }

    @Override
    public void render(float delta) {
        // Check for escape key press to go back to the menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen

        camera.update(); // Update the camera

        batch.begin();



        // Render the Tiled map
        mapRenderer.setView(camera);
        mapRenderer.render();
        boolean moveUp = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean moveDown = Gdx.input.isKeyPressed(Input.Keys.S);
        boolean moveLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean moveRight = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean runKeyPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);




        // Update and render the player
        player.update(delta, moveUp, moveDown, moveLeft, moveRight, runKeyPressed);
        player.render(batch);
        friends.render(batch);
        griever.render(batch);
        griever.update(delta, player.getX(), player.getY(), player.getDirection());

        Vector2 playerPosition = new Vector2(player.getX(), player.getY());
        int savedFriends = friends.checkAndSaveAllFriends(playerPosition, 3f);
        hud.render(batch);
        for (int i = 0; i < savedFriends; i++) {
            hud.incrementLives();
        }

        wallManager.render(batch);

        for (Wall wall : wallManager.getWalls()) {
            if (player.getBoundingBox().overlaps(wall.getBrickRect())) {
                player.revertToPrevious();
                break;
            }
        }
        for (Wall wall : wallManager.getWalls()) {
            if (griever.getGrieverRectangle().overlaps(wall.getBrickRect())) {
                griever.revertToPrevious();
                break;
            }
        }


        int diffX = (int) (player.getX() - griever.getX());
        int diffY = (int) (player.getY() - griever.getY());
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);
        if (LivesCoolDownTimer <= 0 && distance < 10f && griever.isGrieverNotStunned() ) {
            if (hud.getLives() > 1) {
                hud.decrementLives();
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
        wallManager.dispose();
    }
}