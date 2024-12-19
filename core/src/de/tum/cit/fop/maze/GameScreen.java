package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
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
        tiledMap = new TmxMapLoader().load("maps/map1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        // Center the camera on the map
        centerCameraOnMap();
    }

    /**
     * Centers the camera on the map based on its dimensions and logs debug information.
     */
    private void centerCameraOnMap() {
        // Retrieve map properties
        int mapWidth = tiledMap.getProperties().get("width", Integer.class); // Tile count width
        int mapHeight = tiledMap.getProperties().get("height", Integer.class); // Tile count height
        int tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class); // Pixel width per tile
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class); // Pixel height per tile

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

        // Render the Tiled map
        mapRenderer.setView(camera);
        mapRenderer.render();
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
    }
}

