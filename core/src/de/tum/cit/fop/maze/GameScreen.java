package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
    private float currentZoom = 0.1f;
    private final float MIN_ZOOM = 0.08f;
    private final float MAX_ZOOM = 0.4f;
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
    private Griever griever;
    private Key key;
    private Item item;
    private Array<Door> doors;
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

        camera.zoom = 0.1f;

        tiledMap = new TmxMapLoader().load("map1.tmx");
        TiledMapTileLayer wallsLayer = (TiledMapTileLayer) tiledMap.getLayers().get("walls");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);


        centerCameraOnMap();

        hud = new HUD();
        this.friends = new Friends();
        this.item = new Item();
        player = new Player(155, 259, (TiledMapTileLayer) tiledMap.getLayers().get(0));
        griever = new Griever(87, 160, (TiledMapTileLayer) tiledMap.getLayers().get("path"),(TiledMapTileLayer) tiledMap.getLayers().get("path2"));
        batch = new SpriteBatch();

        friends.setScale(0.2f);

        this.key = new Key(189,286);


        movingWallsLayer = tiledMap.getLayers().get("moving walls") instanceof TiledMapTileLayer
                ? (TiledMapTileLayer) tiledMap.getLayers().get("moving walls")
                : null;

        if (movingWallsLayer != null) {
            walls = Wall.createWallsFromLayer(movingWallsLayer, griever, hud);
        }

        TiledMapTileLayer doorsLayer = (TiledMapTileLayer) tiledMap.getLayers().get("exits");
        doors = createDoorsFromLayer(doorsLayer);

        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        lastPosition = new Vector3(camera.position.x, camera.position.y, 0);
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
            savePlayerState();
            game.goToMenu();
        }

        hud.updateTimer(delta);

        ScreenUtils.clear(0, 0, 0, 1); // Clear the screen

        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            // Zoom in
            zoomCamera(-ZOOM_SPEED);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            // Zoom out
            zoomCamera(ZOOM_SPEED);
        }

        float currentGlobalTime = hud.getGlobalTimer();

        walls.forEach(wall -> {
            wall.update(delta, hud.getGlobalTimer());
            wall.checkAndMovePlayer(player, hud.getGlobalTimer());
        });

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

        griever.update(delta, player.getX(), player.getY(), player.getDirection(),hud,player);
        griever.updateMovement(delta);
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

        if (key != null) {
            key.update(player, hud); // Key의 상태와 HUD 동기화
        }


        if (hud.getLives() <= 0) {
            hud.stopTimer();
            float finalTime = 0;
            game.setScreen(new GameOverScreen(game,finalTime));
            return;
        }

        hud.updateScoreTimer(delta);
        friends.render(batch,player);
        item.render(batch);
        hud.render(batch, player);
        Vector2 playerPosition = new Vector2(player.getX(), player.getY());


        for (Door door : doors) {
            door.tryToOpen(playerPosition, hud, game);
        }

        friends.update(player, hud, 3f);
        item.update(player, hud, 3f);



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



    public void savePlayerState() {
        Preferences prefs = Gdx.app.getPreferences("PlayerState");
        prefs.putFloat("playerX", player.getX());
        prefs.putFloat("playerY", player.getY());
        prefs.putInteger("playerLives", hud.getLives());
        prefs.putFloat("grieverX", griever.getMonsterX());
        prefs.putFloat("grieverY", griever.getMonsterY());
        prefs.putFloat("scoreTimer", hud.getScoreTimer()); // Save current timer value

        for (int i = 0; i < friends.getIsFriendSaved().length; i++) {
            prefs.putBoolean("friendSaved" + i, friends.getIsFriendSaved()[i]);
            prefs.putFloat("friend" + i + "X", friends.getFriendsPositions()[i].x);
            prefs.putFloat("friend" + i + "Y", friends.getFriendsPositions()[i].y);
        }
        for (int i = 0; i < item.getIsItemCollected().length; i++) {
            prefs.putBoolean("itemCollected" + i, item.getIsItemCollected()[i]);
            prefs.putFloat("item" + i + "X", item.getItemPositions()[i].x);
            prefs.putFloat("item" + i + "Y", item.getItemPositions()[i].y);
        }
        prefs.putFloat("keyX", key.getX());
        prefs.putFloat("keyY", key.getY());
        prefs.putBoolean("keyCollected", hud.isKeyCollected());

        for (Wall wall : walls) {
            prefs.putBoolean("grieverDead", wall.isGrieverDead());
        }

        prefs.flush();
    }

    public void loadPlayerState() {
        Preferences prefs = Gdx.app.getPreferences("PlayerState");
        if (prefs.contains("playerX") && prefs.contains("playerY")) {
            float x = prefs.getFloat("playerX");
            player.setX(x);
            float y = prefs.getFloat("playerY");
            player.setY(y);
            int lives = prefs.getInteger("playerLives");
            hud.setLives(lives);
            float monsterX = prefs.getFloat("grieverX");
            float monsterY = prefs.getFloat("grieverY");
            griever.setPosition((int) monsterX, (int) monsterY);
            for (int i = 0; i < friends.getIsFriendSaved().length; i++) {
                friends.getIsFriendSaved()[i] = prefs.getBoolean("friendSaved" + i, false); // Default to false if not saved
                float friendX = prefs.getFloat("friend" + i + "X", -1000);  // Default to an invalid position if not saved
                float friendY = prefs.getFloat("friend" + i + "Y", -1000);
                friends.getFriendsPositions()[i] = new Vector2(friendX, friendY);
            }
            for (int i = 0;i <item.getIsItemCollected().length; i++) {
                item.getIsItemCollected()[i] = prefs.getBoolean("itemCollected" + i, false);
                float itemX = prefs.getFloat("item" + i + "X", -1000);
                float itemY = prefs.getFloat("item" + i + "Y", -1000);
                item.getItemPositions()[i] = new Vector2(itemX, itemY);
            }
            boolean isKeyCollected = prefs.getBoolean("keyCollected", false);
            hud.setKeyCollected(isKeyCollected);
            float keyX = prefs.getFloat("keyX", -1000);
            float keyY = prefs.getFloat("keyY", -1000);
            key.setX(keyX);
            key.setY(keyY);
            boolean isGrieverDead = prefs.getBoolean("grieverDead", false);
            for (Wall wall : walls) {
                wall.setGrieverDead(isGrieverDead);
            }
            if (prefs.contains("scoreTimer")) {
                float savedScoreTimer = prefs.getFloat("scoreTimer");
                hud.setScoreTimer(savedScoreTimer); // Restore countdown
                hud.startTimer();
            }

        }
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
        griever.dispose();
        hud.dispose();
    }}