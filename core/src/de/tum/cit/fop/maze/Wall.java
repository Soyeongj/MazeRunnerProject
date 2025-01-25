package de.tum.cit.fop.maze;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a moving wall in the game maze that can interact with player, friends and grievers.
 * Handles wall movement, collision detection, and state management.
 */

public class Wall {

    //Positions
    private int x, y;
    private int originalX, originalY;
    private int targetX, targetY;

    //Moving Walls and Player Collision Controls
    private String direction;
    private float lastMoveTime;
    private static float MOVE_INTERVAL;
    private static float STAY_DURATION;
    private float stayTimer;
    private boolean isAtTarget;
    private boolean isPlayerRemoved;

    //Game Entities
    private Array<Griever> grievers;
    private HUD hud;

    //Griever and Key State Controls
    private Map<Griever, Boolean> grieverDeadStates = new HashMap<>();
    private Map<Griever, Vector2> grieverKeySpawnPositions = new HashMap<>();
    private Map<Griever, Boolean> grieverKeySpawned = new HashMap<>();

    //Textures
    private TiledMapTileLayer layer;
    private TiledMapTileLayer.Cell cell;
    private TextureRegion texture;

    //Walls states
    private static String PREFERENCES_NAME;



    /**
     * Constructs a Wall object with initial position and game properties.
     *
     * @param x Initial x-coordinate of the wall
     * @param y Initial y-coordinate of the wall
     * @param direction Movement direction of the wall
     * @param layer Tiled map layer containing the wall
     * @param grievers List of grievers in the game
     * @param hud Heads-up display for game state tracking
     */
    public Wall(int x, int y, String direction, TiledMapTileLayer layer, Array<Griever> grievers, HUD hud) {
        this.x = x;
        this.y = y;
        this.originalX = x;
        this.originalY = y;
        this.direction = direction;
        this.layer = layer;
        this.grievers = grievers;
        this.hud = hud;

        this.cell = layer.getCell(x, y);
        if (cell != null && cell.getTile() != null) {
            this.texture = cell.getTile().getTextureRegion();
        }

        for (Griever griever : grievers) {
            grieverDeadStates.put(griever, false);
            grieverKeySpawnPositions.put(griever, null);
            grieverKeySpawned.put(griever, false);
        }

        this.lastMoveTime = 0f;
        this.MOVE_INTERVAL = 5.0f;
        this.STAY_DURATION = 0.3f;
        this.stayTimer = 0f;
        this.isAtTarget = false;
        this.isPlayerRemoved = false;

        this.PREFERENCES_NAME = "WallStates";


    }

    /**
     * Creates wall objects from a tiled map layer with moving wall properties.
     *
     * @param movingWallsLayer The tiled map layer containing wall tiles
     * @param grievers List of grievers in the game
     * @param hud Heads-up display for game state
     * @return List of Wall objects created from the layer
     */
    public static List<Wall> createWallsFromLayer(TiledMapTileLayer movingWallsLayer, Array<Griever> grievers, HUD hud) {
        List<Wall> walls = new ArrayList<>();
        for (int x = 0; x < movingWallsLayer.getWidth(); x++) {
            for (int y = 0; y < movingWallsLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = movingWallsLayer.getCell(x, y);
                if (cell != null && cell.getTile().getProperties().containsKey("direction")) {
                    String direction = cell.getTile().getProperties().get("direction", String.class);
                    walls.add(new Wall(x, y, direction, movingWallsLayer, grievers, hud));
                }
            }
        }
        return walls;
    }

    /**
     *  Renders the wall's texture at its current position.
     *
     * @param batch SpriteBatch used for drawing the wall's texture
     */
    public void render(SpriteBatch batch) {
        if (texture != null) {
            float worldX = x * layer.getTileWidth();
            float worldY = y * layer.getTileHeight();
            batch.draw(texture, worldX, worldY, layer.getTileWidth(), layer.getTileHeight());
        }
    }

    /**
     * Updates wall movement and checks for collisions with game entities.
     *
     * @param delta Time since last frame
     * @param globalTimer Overall game timer
     */
    public void update(float delta, float globalTimer) {
        float timeSinceLastMove = globalTimer - lastMoveTime;

        if (isAtTarget) {
            stayTimer += delta;
            if (stayTimer >= STAY_DURATION) {
                moveToOriginal();
                isAtTarget = false;
                stayTimer = 0f;
            }
            return;
        }

        if (timeSinceLastMove >= MOVE_INTERVAL && !isAtTarget) {
            move();
            lastMoveTime = globalTimer;
        }

        for (Griever griever : grievers) {
            checkAndMoveGriever(griever);
        }
    }

    /**
     * Moves the wall in its predefined direction,
     * updating its position on the map layer.
     */
    private void move() {
        Cell cell = layer.getCell(x, y);
        if (cell == null) return;

        layer.setCell(x, y, null);

        targetX = x;
        targetY = y;

        switch (direction) {
            case "left": targetX = x - 1; break;
            case "right": targetX = x + 1; break;
            case "up": targetY = y + 1; break;
            case "down": targetY = y - 1; break;
        }

        if (layer.getCell(targetX, targetY) != null) {
            return;
        }

        layer.setCell(targetX, targetY, cell);
        x = targetX;
        y = targetY;
        isAtTarget = true;
        stayTimer = 0f;
    }

    /**
     * Moves the wall back to its original position (originalX, originalY).
     * Updates the map layer to reflect the move, provided the original position
     * is not occupied.
     */
    private void moveToOriginal() {
        Cell cell = layer.getCell(x, y);
        if (cell == null) return;

        layer.setCell(x, y, null);

        if (layer.getCell(originalX, originalY) != null) {
            return;
        }

        layer.setCell(originalX, originalY, cell);
        x = originalX;
        y = originalY;
    }

    /**
     * Checks collision between a wall and a specific griever,
     * removing the griever if collision occurs.
     *
     * @param griever The griever to check for collision
     */
    private void checkAndMoveGriever(Griever griever) {
        float grieverX = griever.getMonsterX();
        float grieverY = griever.getMonsterY();

        float wallX = targetX * layer.getTileWidth();
        float wallY = targetY * layer.getTileHeight();
        float wallWidth = layer.getTileWidth();
        float wallHeight = layer.getTileHeight();

        if (x != originalX || y != originalY || isAtTarget) {
            if (checkCollision(grieverX, grieverY, griever.getWidth() * griever.getScale(), griever.getHeight() * griever.getScale(),
                    wallX, wallY, wallWidth, wallHeight)) {
                griever.setPosition(-10000, -10000);
                grieverKeySpawnPositions.put(griever, new Vector2(grieverX, grieverY));
                grieverDeadStates.put(griever, true);
                grieverKeySpawned.put(griever, false);
                SoundManager.playMonsterDiedSound();
            }
        }
    }

    /**
     * Checks and handles collision between the wall and a player.
     *
     * @param player The game player
     * @param friends Player's friends(lives) in the game
     */
    public void checkAndMovePlayer(Player player, Friends friends) {
        float playerX = player.getX();
        float playerY = player.getY();

        float wallX = targetX * layer.getTileWidth();
        float wallY = targetY * layer.getTileHeight();
        float wallWidth = layer.getTileWidth();
        float wallHeight = layer.getTileHeight();

        if ((x != originalX || y != originalY || isAtTarget) && !isPlayerRemoved) {
            if (checkCollision(playerX, playerY, player.getWidth() * player.getScale(), player.getHeight() * player.getScale(),
                    wallX, wallY, wallWidth, wallHeight)) {

                if (hud != null) {
                    if (hud.getLives() >= 0) {
                        friends.removeFriendAt(friends.getFollowingFriendsPositions().size()-1);
                        hud.decrementLives();
                        player.triggerRedEffect();
                    } else {
                        hud.setLives(0);
                        player.setDead();
                    }
                }

                player.setX(-1000);
                player.setY(-1000);
                isPlayerRemoved = true;
            }
        }

        if (isPlayerRemoved && !isAtTarget && x == originalX && y == originalY) {
            float safeX = 230;
            float safeY = 250;

            player.setX(safeX);
            player.setY(safeY);

            isPlayerRemoved = false;
        }

    }

    /**
     * Checks for rectangular collision between two game objects.
     *
     * @param x1 X-coordinate of first object
     * @param y1 Y-coordinate of first object
     * @param width1 Width of first object
     * @param height1 Height of first object
     * @param x2 X-coordinate of second object
     * @param y2 Y-coordinate of second object
     * @param width2 Width of second object
     * @param height2 Height of second object
     * @return True if objects overlap, false otherwise
     */
    private boolean checkCollision(float x1, float y1, float width1, float height1,
                                   float x2, float y2, float width2, float height2) {
        return x1 < x2 + width2 && x1 + width1 > x2 &&
                y1 < y2 + height2 && y1 + height1 > y2;
    }


    public TiledMapTileLayer getLayer() {
        return layer;
    }


    public int getTargetY() {
        return targetY;
    }


    public int getTargetX() {
        return targetX;
    }


    public int getX() {
        return x;
    }


    public int getY() {
        return y;
    }


    public int getOriginalX() {
        return originalX;
    }


    public int getOriginalY() {
        return originalY;
    }


    public boolean isAtTarget() {
        return isAtTarget;
    }


    public Vector2 getKeySpawnPosition(Griever griever) {
        return grieverKeySpawnPositions.get(griever);
    }


    public boolean isGrieverDead(Griever griever) {
        return grieverDeadStates.getOrDefault(griever, false);
    }


    public boolean hasKeySpawned(Griever griever) {
        return grieverKeySpawned.getOrDefault(griever, false);
    }

    public void setKeySpawned(Griever griever, boolean spawned) {
        grieverKeySpawned.put(griever, spawned);
    }

    /**
     * Saves the current state of walls, including griever and key states.
     * Stores information in game preferences for potential game restoration.
     */
    public void saveWallState() {
        Preferences pref = Gdx.app.getPreferences(PREFERENCES_NAME);
        pref.putInteger("numGrievers", grievers.size);

        for (int i = 0; i < grievers.size; i++) {
            Griever griever = grievers.get(i);
            pref.putBoolean("grieverDead_" + i, grieverDeadStates.get(griever));
            pref.putBoolean("keySpawned_" + i, grieverKeySpawned.get(griever));

            Vector2 spawnPos = grieverKeySpawnPositions.get(griever);
            if (spawnPos != null) {
                pref.putFloat("keySpawnX_" + i, spawnPos.x);
                pref.putFloat("keySpawnY_" + i, spawnPos.y);
            }
        }
        pref.flush();
    }

    /**
     * Loads previously saved wall state from game preferences.
     * Restores griever and key states to their previous configuration.
     */
    public void loadWallState() {
        Preferences pref = Gdx.app.getPreferences(PREFERENCES_NAME);
        int numGrievers = pref.getInteger("numGrievers", 0);

        for (int i = 0; i < numGrievers && i < grievers.size; i++) {
            Griever griever = grievers.get(i);
            grieverDeadStates.put(griever, pref.getBoolean("grieverDead_" + i, false));
            grieverKeySpawned.put(griever, pref.getBoolean("keySpawned_" + i, false));

            if (pref.contains("keySpawnX_" + i) && pref.contains("keySpawnY_" + i)) {
                float x = pref.getFloat("keySpawnX_" + i);
                float y = pref.getFloat("keySpawnY_" + i);
                grieverKeySpawnPositions.put(griever, new Vector2(x, y));
            }
        }
    }
}