package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Iterator;
import java.util.List;


/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {
    private final MazeRunnerGame game;

    //Display and Rendering Components
    private final Texture backgroundTexture;
    private final OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private Texture introImage;

    //Camera Controls
    private float currentZoom = 0.10f;
    private final float MIN_ZOOM = 0.10f;
    private final float MAX_ZOOM = 0.3f;
    private final float ZOOM_SPEED = 0.01f;
    private Vector3 lastPosition;

    //Map and Level Elements
    private final TiledMap tiledMap;
    private TiledMapTileLayer movingWallsLayer;
    private TiledMapTileLayer pathLayer, path2Layer;

    //Game Entities
    private List<Wall> walls;
    private Player player;
    private Friends friends;
    private HUD hud;
    private Array<Griever> grievers;
    private Array<Key> keys;
    private Item item;
    private Array<Door> doors;
    private Array<Trap> traps;
    private Arrow arrow;
    private TrapItem trapItem;



    /**
     * Constructor for GameScreen. Sets up the camera and Tiled map.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(MazeRunnerGame game, String mapPath) {
        // Core game initialization
        this.game = game;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.backgroundTexture = new Texture(Gdx.files.internal("background.png"));

        // Camera and viewport setup
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(800, 480, camera);
        this.camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        this.camera.zoom = 0.10f;
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.lastPosition = new Vector3(camera.position.x, camera.position.y, 0);

        // Map initialization
        this.tiledMap = new TmxMapLoader().load(mapPath);
        this.mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        // Layer initialization
        TiledMapTileLayer wallsLayer = (TiledMapTileLayer) tiledMap.getLayers().get("walls");
        this.pathLayer = (TiledMapTileLayer) tiledMap.getLayers().get("path");
        this.path2Layer = (TiledMapTileLayer) tiledMap.getLayers().get("path2");
        this.movingWallsLayer = tiledMap.getLayers().get("moving walls") instanceof TiledMapTileLayer
                ? (TiledMapTileLayer) tiledMap.getLayers().get("moving walls")
                : null;
        TiledMapTileLayer doorsLayer = (TiledMapTileLayer) tiledMap.getLayers().get("exits");
        TiledMapTileLayer trapLayer = (TiledMapTileLayer) tiledMap.getLayers().get("static obstacles");

        // UI elements
        this.hud = new HUD();
        this.arrow = new Arrow();

        // Player and character initialization
        this.player = Player.loadPlayerFromTiledMap(tiledMap, wallsLayer);
        this.friends = new Friends(tiledMap, player);
        this.grievers = Griever.loadGrieversFromTiledMap(tiledMap, pathLayer, path2Layer);

        // Game objects initialization
        this.item = new Item(tiledMap);
        this.trapItem = new TrapItem(tiledMap);
        this.keys = new Array<>();
        this.doors = createDoorsFromLayer(doorsLayer);
        this.traps = createTrapsFromLayer(trapLayer, "assets/rock1.png");

        // Moving walls setup (depends on grievers and HUD)
        if (movingWallsLayer != null) {
            this.walls = Wall.createWallsFromLayer(movingWallsLayer, grievers, hud);
        }

        // Final camera setup
        centerCameraOnMap();
    }

    /**
     * Creates an array of Door objects from the specified TiledMapTileLayer.
     *
     * @param layer the TiledMapTileLayer containing the door tiles
     * @return an Array of Door objects created from the layer
     */
    private Array<Door> createDoorsFromLayer(TiledMapTileLayer layer) {
        Array<Door> doors = new Array<>();

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    float worldX = x * layer.getTileWidth();
                    float worldY = y * layer.getTileHeight();

                    doors.add(new Door(worldX, worldY,
                            layer.getTileWidth(),
                            layer.getTileHeight()));
                }
            }
        }

        return doors;
    }

    /**
     * Creates an array of Trap objects from the specified TiledMapTileLayer.
     *
     * @param layer the TiledMapTileLayer containing the trap tiles
     * @param rockTexture the texture to be applied to the traps
     * @return an Array of Trap objects created from the layer
     */
    private Array<Trap> createTrapsFromLayer(TiledMapTileLayer layer, String rockTexture) {
        Array<Trap> traps = new Array<>();
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    float worldX = x * layer.getTileWidth();
                    float worldY = y * layer.getTileHeight();
                    traps.add(new Trap(worldX, worldY, layer.getTileWidth(), layer.getTileHeight(), rockTexture));
                }
            }
        }
        return traps;
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

    /**
     * Renders the game screen. Handles rendering of all game objects, updating the game state,
     * and managing user inputs.
     *
     * @param delta the time in seconds since the last render
     */
    @Override
    public void render(float delta) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            saveState();
            game.goToMenu();
            SoundManager.stopBackgroundMusic();
            SoundManager.playMenuMusic();
        }

        hud.updateTimer(delta);

        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            zoomCamera(-ZOOM_SPEED);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            zoomCamera(ZOOM_SPEED);
        }

        walls.forEach(wall -> {
            wall.update(delta, hud.getGlobalTimer());
            wall.checkAndMovePlayer(player,friends);
        });

        camera.position.set(player.getX() +( player.getWidth() / 2 ) -10, player.getY() + (player.getHeight() / 2) -10, 0);
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

        player.update(delta, moveUp, moveDown, moveLeft, moveRight, runKeyPressed,friends);
        player.render(batch);

        Iterator<Griever> iterator = grievers.iterator();
        while (iterator.hasNext()) {
            Griever griever = iterator.next();
            griever.update(delta, player.getX(), player.getY(), player.getDirection(), hud, player, friends);
            griever.render(batch);

            for (Wall wall : walls) {
                if (wall.isGrieverDead(griever) && !wall.hasKeySpawned(griever)) {
                    Vector2 keyPosition = wall.getKeySpawnPosition(griever);
                    if (keyPosition != null) {
                        iterator.remove();
                        keys.add(new Key(keyPosition.x, keyPosition.y));
                        wall.setKeySpawned(griever, true);
                    }
                }
                friends.update(player, hud, 3f, delta, griever, wall);
            }
        }

        Iterator<Key> keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            Key key = keyIterator.next();
            key.render(batch);
            key.update(player, hud);

            if (key.isCollected()) {
                keyIterator.remove();
            }
        }

        if (hud.getLives() < 0 || hud.getScoreTimer() <= 0) {
            hud.stopTimer();
            float finalTime = 0;
            game.setScreen(new GameOverScreen(game,finalTime));
            SoundManager.playGameOverSound();
            return;
        }

        hud.updateScoreTimer(delta);

        friends.render(batch,player);
        item.render(batch);
        trapItem.render(batch);

        Vector2 playerPosition = new Vector2(player.getX(), player.getY());
        for (Door door : doors) {
            door.tryToOpen(playerPosition, hud, game, friends);
        }
        for (Trap trap : traps) {
            trap.fallRock(playerPosition,hud,player,delta,friends);
            trap.render(batch);
        }
        arrow.update(playerPosition,doors,hud.isKeyCollected());
        arrow.render(batch);

        item.update(player,  7f);
        trapItem.update(player, 7f);
        if (trapItem.isFogActive()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.85f);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();
        }

        hud.render(batch, player);

        batch.end();
    }

    /**
     * Adjusts the camera zoom level.
     *
     * @param amount the amount by which to zoom the camera
     */
    private void zoomCamera(float amount) {
        Vector3 beforeZoom = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(beforeZoom);

        currentZoom = MathUtils.clamp(currentZoom + amount, MIN_ZOOM, MAX_ZOOM);
        camera.zoom = currentZoom;

        Vector3 afterZoom = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(afterZoom);

        camera.position.add(
                beforeZoom.x - afterZoom.x,
                beforeZoom.y - afterZoom.y,
                0
        );
        lastPosition.set(camera.position);
    }

    /**
     * Saves the current state of the game, including player, items, traps, walls, and keys.
     */
    public void saveState() {
        player.savePlayerState();

        item.saveItemState();
        trapItem.saveTrapItemState();

        for (Trap trap : traps) {
            trap.saveTrapState();
        }
        for (Wall wall: walls) {
            wall.saveWallState();
        }

        hud.saveHUDState();
        friends.saveFriendState();
        for (int i = 0; i < grievers.size; i++) {
            grievers.get(i).saveGrieverstate(i);
        }
        Preferences preferences = Gdx.app.getPreferences("Keys");
        preferences.putInteger("numberOfKeys", keys.size);
        for (int i = 0; i < keys.size; i++) {
            Key key = keys.get(i);
            preferences.putFloat("key_" + i + "_x", key.getX());
            preferences.putFloat("key_" + i + "_y", key.getY());
            preferences.putBoolean("key_" + i + "_collected", key.isCollected());
        }

        preferences.flush();
    }

    /**
     * Loads the saved state of the game, restoring player, items, traps, walls, and keys.
     */
    public void loadState() {
        player.loadPlayerState();

        item.loadItemState();
        trapItem.loadTrapItemState();

        for (Trap trap : traps) {
            trap.loadTrapState();
        }
        for (Wall wall : walls) {
            wall.loadWallState();
        }

        hud.loadHUDState();


        for (int i = 0; i < grievers.size; i++) {
            grievers.get(i).loadGrieverstate(i);
        }
        keys.clear();
        Preferences preferences = Gdx.app.getPreferences("Keys");
        int numberOfKeys = preferences.getInteger("numberOfKeys", 0);
        for (int i = 0; i < numberOfKeys; i++) {
            float x = preferences.getFloat("key_" + i + "_x", 0);
            float y = preferences.getFloat("key_" + i + "_y", 0);
            boolean collected = preferences.getBoolean("key_" + i + "_collected", false);

            if (!collected) {
                Key key = new Key(x, y);
                keys.add(key);
            }
        }

        friends.loadFriendState();
    }

    /**
     * Resizes the game viewport and updates relevant components.
     *
     * @param width  the new width of the viewport
     * @param height the new height of the viewport
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        hud.setScreenDimensions(width, height);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.set(lastPosition);
        camera.update();
    }

    /**
     * Called when the game is paused. Override if additional functionality is required.
     */
    @Override
    public void pause() {
    }

    /**
     * Called when the game is resumed. Override if additional functionality is required.
     */
    @Override
    public void resume() {
    }
    /**
     * Sets up the game screen when it is shown. Handles intro screen logic and background music.
     */
    @Override
    public void show() {
        SoundManager.stopMenuMusic();
        SoundManager.playBackgroundMusic();
    }
    /**
     * Called when the game screen is hidden. Override if additional functionality is required.
     */
    @Override
    public void hide() {
    }

    /**
     * Releases all resources used by the game screen to prevent memory leaks.
     */
    @Override
    public void dispose() {
        tiledMap.dispose();
        mapRenderer.dispose();
        batch.dispose();
        player.dispose();
        hud.dispose();
        for (Griever griever : grievers) {
            griever.dispose();
        }
        arrow.dispose();
        backgroundTexture.dispose();
        friends.dispose();
    }
}