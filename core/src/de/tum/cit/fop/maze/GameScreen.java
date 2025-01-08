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
import com.badlogic.gdx.audio.Sound;



import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import static java.lang.Math.exp;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private float currentZoom = 0.05f;
    private final float MIN_ZOOM = 0.05f;
    private final float MAX_ZOOM = 0.50f;
    private final float ZOOM_SPEED = 0.01f;
    private Vector3 lastPosition;
    private Viewport viewport;
    private final TiledMap tiledMap;
    private TiledMapTileLayer movingWallsLayer;
    private List<Wall> walls;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private Player player;
    private Friends friends;
    private HUD hud;
    private SpriteBatch batch;
    private Array<Griever> grievers;
    private Array<Key> keys;
    private Item item;
    private Array<Door> doors;
    private Array<Trap> traps;
    private Arrow arrow;
    private Sound rockSound;

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

        camera.zoom = 0.05f;

        tiledMap = new TmxMapLoader().load("map1.tmx");
        TiledMapTileLayer wallsLayer = (TiledMapTileLayer) tiledMap.getLayers().get("walls");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);


        centerCameraOnMap();
        hud = new HUD();
        this.friends = new Friends();
        this.item = new Item();
        player = new Player(155, 259, (TiledMapTileLayer) tiledMap.getLayers().get(0));
        grievers = new Array<>();
        grievers.add(new Griever(160, 280, (TiledMapTileLayer) tiledMap.getLayers().get("path"), (TiledMapTileLayer) tiledMap.getLayers().get("path2")));
        batch = new SpriteBatch();


        keys = new Array<>();

        movingWallsLayer = tiledMap.getLayers().get("moving walls") instanceof TiledMapTileLayer
                ? (TiledMapTileLayer) tiledMap.getLayers().get("moving walls")
                : null;

        if (movingWallsLayer != null) {
            walls = Wall.createWallsFromLayer(movingWallsLayer, grievers, hud);
        }



        TiledMapTileLayer doorsLayer = (TiledMapTileLayer) tiledMap.getLayers().get("exits");
        doors = createDoorsFromLayer(doorsLayer);
        TiledMapTileLayer trapLayer = (TiledMapTileLayer) tiledMap.getLayers().get("static obstacles");
        String rockTexture = "assets/rock1.png";
        traps = createTrapsFromLayer(trapLayer, rockTexture);
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        lastPosition = new Vector3(camera.position.x, camera.position.y, 0);

        arrow = new Arrow();
        SoundManager.initialize();

    }

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


    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            saveState();
            game.goToMenu();
        }

        hud.updateTimer(delta);

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen

        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            zoomCamera(-ZOOM_SPEED);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            zoomCamera(ZOOM_SPEED);
        }

        float currentGlobalTime = hud.getGlobalTimer();

        walls.forEach(wall -> {
            wall.update(delta, hud.getGlobalTimer());
            wall.checkAndMovePlayer(player, hud.getGlobalTimer(),friends);
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


        player.update(delta, moveUp, moveDown, moveLeft, moveRight, runKeyPressed);
        player.render(batch);

        for (Griever griever : grievers) {
            griever.update(delta, player.getX(), player.getY(), player.getDirection(), hud, player,friends);
            griever.updateMovement(delta);
            griever.render(batch);
        }
        for (Wall wall : walls) {
            if (wall.isGrieverDead() && !wall.hasKeySpawned()) {
                keys.add(new Key(wall.getKeySpawnPosition().x, wall.getKeySpawnPosition().y));
                wall.setKeySpawned(true);
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

        if (hud.getLives() <= 0) {
            hud.stopTimer();
            float finalTime = 0;
            game.setScreen(new GameOverScreen(game,finalTime));
            SoundManager.playGameOverSound();
            return;
        }

        hud.updateScoreTimer(delta);
        friends.render(batch,player,delta);
        item.render(batch);
        Vector2 playerPosition = new Vector2(player.getX(), player.getY());


        for (Door door : doors) {
            door.tryToOpen(playerPosition, hud, game);
        }

        for (Trap trap : traps) {
            trap.test(playerPosition,hud,player,delta,friends);
            trap.render(batch);

        }

        arrow.update(playerPosition,doors,hud.isKeyCollected());
        arrow.render(batch);
        friends.update(player, hud, 3f,delta);
        item.update(player, hud, 3f);


        hud.render(batch, player);

        batch.end();
    }

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



    public void saveState() {
        player.savePlayerState();
        for (Griever griever : grievers) {
            griever.saveGrieverstate();
        }
        friends.saveFriendsStates();
        item.saveItemState();

        for (Trap trap : traps) {
            trap.saveTrapState();
        }
        for (Wall wall: walls) {
            wall.saveWallState();
        }
        for (Key key : keys) {
            key.saveKeyState();
        }
        hud.saveHUDState();


    }

    public void loadState() {
        player.loadPlayerState();
        for (Griever griever: grievers) {
            griever.loadGrieverstate();
        }
        friends.loadFriendsStates();
        item.loadItemState();

        for (Trap trap : traps) {
            trap.loadTrapState();
        }
        for (Wall wall : walls) {
            wall.loadWallState();
        }
        for (Key key : keys) {
            key.loadKeyState();
        }
        hud.loadHUDState();


    }



    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        hud.setScreenDimensions(width, height);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.set(lastPosition);
        camera.update();
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
        for (Griever griever : grievers) {
            griever.dispose();
        }
        arrow.dispose();
    }}