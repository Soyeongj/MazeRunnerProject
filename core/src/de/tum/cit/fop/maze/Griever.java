package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

import java.util.*;

/**
 * The Griever class represents an enemy character (Griever) in the maze.
 * It is responsible for the Griever's movement, animation, detection of the player,
 * and interactions such as being stunned.
 */
public class Griever  {
    // Animation and Texture Related Variables
    private Map<String, Texture[]> grieverTextures;
    private Texture griever;
    private float grieverStateTime;
    private String fixedGrieverDirection;
    private final float grieverAnimationTime = 0.1f;
    private final float scale = 0.4f;

    // Position and Movement Related Variables
    private float monsterX, monsterY;
    private final float monsterSpeed = 15.0f;
    private final float detectionRange = 100.0f;
    private boolean isGrieverFollowingPlayer = false;
    private Vector2 currentTarget;
    private final Random random = new Random();
    private boolean isRandomMovement = false;

    // Stun Related Variables
    private boolean isGrieverStunned = false;
    private float stunTimer = 0.0f;
    private final float stunDuration = 3.0f;

    // Layer Related Variables
    private final TiledMapTileLayer pathLayer;
    private final TiledMapTileLayer path2Layer;

    //Griever-Damage Related Variables
    private float LivesCoolDownTimer = 0;

    private static final float MAX_X = 478.86f;
    private static final float MAX_Y = 478f;
    private static final float MIN_X = 0f;
    private static final float MIN_Y = 0f;

    /**
     * Constructs a Griever at the given starting position.
     *
     * @param startX      The starting X-coordinate of the Griever.
     * @param startY      The starting Y-coordinate of the Griever.
     * @param pathLayer   The path layer used for collision detection.
     * @param path2Layer  The second path layer for movement calculations.
     */
    public Griever(float startX, float startY, TiledMapTileLayer pathLayer, TiledMapTileLayer path2Layer) {
        this.monsterX = startX;
        this.monsterY = startY;
        this.pathLayer = pathLayer;
        this.path2Layer = path2Layer;

        initializeTextures();
    }

    /**
     * Initializes the textures for the Griever's animations for different directions.
     */
    private void initializeTextures() {
        grieverTextures = new HashMap<>();
        grieverTextures.put("up", new Texture[]{new Texture("monster_up1.png"), new Texture("monster_up2.png")});
        grieverTextures.put("down", new Texture[]{new Texture("monster_down1.png"), new Texture("monster_down2.png")});
        grieverTextures.put("left", new Texture[]{new Texture("monster_left1.png"), new Texture("monster_left2.png")});
        grieverTextures.put("right", new Texture[]{new Texture("monster_right1.png"), new Texture("monster_right2.png")});

        fixedGrieverDirection = "right";
        griever = grieverTextures.get(fixedGrieverDirection)[0];
        grieverStateTime = 0f;
    }

    /**
     * Updates the Griever's direction based on its movement.
     * This method determines whether the Griever should be moving up, down, left, or right
     * depending on the changes in the X and Y coordinates.
     *
     * @param deltaX The change in the X-coordinate (horizontal movement).
     * @param deltaY The change in the Y-coordinate (vertical movement).
     */
     private void updateGrieverDirection(float deltaX, float deltaY) {
        if (Math.abs(deltaX) > 0.01f || Math.abs(deltaY) > 0.01f) {
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                fixedGrieverDirection = deltaX > 0 ? "right" : "left";
            } else {
                fixedGrieverDirection = deltaY > 0 ? "up" : "down";
            }
        }
    }

    /**
     * Updates the Griever's animation by switching the texture based on the current direction.
     * This method ensures that the Griever's texture changes at a set animation rate.
     *
     * @param delta The time in seconds since the last update, used to control the animation timing.
     */
    private void updateAnimation(float delta) {
        grieverStateTime += delta;
        if (grieverStateTime >= grieverAnimationTime) {
            griever = getGrieverTextureForDirection(fixedGrieverDirection);
            grieverStateTime = 0;
        }
    }

    /**
     * Loads all Griever objects from the provided TiledMap, creating instances of Griever
     * based on the map's properties and adding them to an array.
     *
     * @param map The TiledMap containing the game world and Griever data.
     * @param pathLayer The first path layer used for Griever's movement logic.
     * @param path2Layer The second path layer used for Griever's movement logic.
     * @return An array of Griever instances loaded from the map.
     */
    public static Array<Griever> loadGrieversFromTiledMap(TiledMap map, TiledMapTileLayer pathLayer, TiledMapTileLayer path2Layer) {
        Array<Griever> grievers = new Array<>();

        MapLayer grieverLayer = map.getLayers().get("griever");
        MapObjects objects = grieverLayer.getObjects();

        for (MapObject object : objects) {
            Object grieverProperty = object.getProperties().get("griever");
            if (grieverProperty != null && "1".equals(grieverProperty.toString())) {
                float x = Float.parseFloat(object.getProperties().get("x").toString());
                float y = Float.parseFloat(object.getProperties().get("y").toString());


                Griever griever = new Griever(x, y, pathLayer, path2Layer);
                grievers.add(griever);
            }
        }
        return grievers;
    }

    /**
     * The main update method orchestrates all the update logic for the Griever.
     * It handles stun state, movement, animation, and checks for collisions with the player.
     *
     * @param delta The time in seconds since the last update.
     * @param playerX The X-coordinate of the player.
     * @param playerY The Y-coordinate of the player.
     * @param playerDirection The direction the player is currently facing.
     * @param hud The HUD (Heads Up Display) that tracks game progress.
     * @param player The Player instance, for collision detection and interactions.
     * @param friends The Friends instance, to manage the state of the player's friends.
     */
    public void update(float delta, float playerX, float playerY, String playerDirection, HUD hud, Player player, Friends friends) {
        if (handleStunState(delta, hud)) {
            return;
        }

        updateGrieverState(playerX, playerY);
        handleMovement(delta, playerX, playerY);
        updateAnimation(delta);
        checkStunCondition(playerX, playerY, playerDirection);
        checkPlayerCollision(player, hud, friends, delta);
    }

    /**
     * Handles the Griever's stun state. If the Griever is stunned, it updates the stun timer
     * and displays a message. Returns true if the Griever is currently stunned.
     *
     * @param delta The time in seconds since the last update.
     * @param hud The HUD to display the stun message.
     * @return true if the Griever is stunned, false otherwise.
     */    private boolean handleStunState(float delta, HUD hud) {
        if (isGrieverStunned) {
            stunTimer += delta;
            hud.stunMessage();
            if (stunTimer >= stunDuration) {
                isGrieverStunned = false;
                stunTimer = 0;
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if the Griever is within stun range of the player and if the player is facing
     * the Griever in the opposite direction. If so, the Griever becomes stunned.
     *
     * @param playerX The X-coordinate of the player.
     * @param playerY The Y-coordinate of the player.
     * @param playerDirection The direction the player is currently facing.
     */
    private void checkStunCondition(float playerX, float playerY, String playerDirection) {
        float distance = (float) Math.sqrt(Math.pow(playerX - monsterX, 2) + Math.pow(playerY - monsterY, 2));
        if (distance <= 10f) {
            if (isGrieverInOppositeDirection(playerDirection) && !isGrieverStunned) {
                isGrieverStunned = true;
                stunTimer = 0;
            } else {
            }
        }
    }

    /**
     * Checks if the Griever is facing the player in the opposite direction.
     *
     * @param playerDirection The direction the player is facing.
     * @return true if the Griever is facing the opposite direction of the player, false otherwise.
     */
    private boolean isGrieverInOppositeDirection(String playerDirection) {
        boolean result = (fixedGrieverDirection.equals("left") && playerDirection.equals("right")) ||
                (fixedGrieverDirection.equals("right") && playerDirection.equals("left")) ||
                (fixedGrieverDirection.equals("up") && playerDirection.equals("down")) ||
                (fixedGrieverDirection.equals("down") && playerDirection.equals("up"));
        return result;
    }

    /**
     * Handles all movement logic of the Griever, including whether it is following the player or moving randomly.
     *
     * @param delta The time in seconds since the last update.
     * @param playerX The X-coordinate of the player.
     * @param playerY The Y-coordinate of the player.
     */
    private void handleMovement(float delta, float playerX, float playerY) {
        if (isGrieverFollowingPlayer) {
            handlePlayerFollowing(delta, playerX, playerY);
        } else {
            handleRandomMovement(delta);
        }
    }

    /**
     * Handles movement when the Griever is following the player.
     * Updates the Griever's target and moves it towards the player.
     *
     * @param delta The time in seconds since the last update.
     * @param playerX The X-coordinate of the player.
     * @param playerY The Y-coordinate of the player.
     */    private void handlePlayerFollowing(float delta, float playerX, float playerY) {
        if (currentTarget == null || reachedTarget()) {
            updateTargetTowardsPlayer(playerX, playerY);
        }

        if (currentTarget != null) {
            moveTowardsTarget(delta, pathLayer);
        }
    }

    /**
     * Updates the Griever's target to move towards the player's position.
     * The method tries to find the closest valid path tile to follow the player.
     *
     * @param playerX The X-coordinate of the player.
     * @param playerY The Y-coordinate of the player.
     */    private void updateTargetTowardsPlayer(float playerX, float playerY) {
        Vector2[] directions = {
                new Vector2(1, 0), new Vector2(-1, 0),
                new Vector2(0, 1), new Vector2(0, -1)
        };

        Vector2 closestTarget = null;
        float closestDistance = Float.MAX_VALUE;
        int attempts = 0;

        while (closestTarget == null && attempts < 3) {
            for (Vector2 direction : directions) {
                float nextX = monsterX + direction.x * pathLayer.getTileWidth();
                float nextY = monsterY + direction.y * pathLayer.getTileHeight();


                if (isPathTile(nextX, nextY, pathLayer) &&
                        isPathClear(monsterX, monsterY, nextX, nextY)) {
                    Vector2 potentialTarget = new Vector2(nextX, nextY);
                    float distanceToPlayer = Vector2.dst(nextX, nextY, playerX, playerY);


                    if (distanceToPlayer < closestDistance) {
                        closestTarget = potentialTarget;
                        closestDistance = distanceToPlayer;
                    }
                }
            }
            attempts++;
        }

        if (closestTarget == null) {
            isGrieverFollowingPlayer = false;
            closestTarget = findNextTargetWithMinDistance(15f);
        }

        currentTarget = closestTarget;
    }

    /**
     * Updates the Griever's state based on the player's position. Determines whether the Griever
     * should be following the player or moving randomly based on the distance between the Griever
     * and the player.
     *
     * @param playerX The X-coordinate of the player.
     * @param playerY The Y-coordinate of the player.
     */    private void updateGrieverState(float playerX, float playerY) {
        float distance = calculateDistance(playerX, playerY);
        boolean wasFollowingPlayer = isGrieverFollowingPlayer;
        isGrieverFollowingPlayer = distance <= detectionRange;

        if (isGrieverFollowingPlayer && !wasFollowingPlayer) {
            isRandomMovement = false;
            currentTarget = null;
        } else if (!isGrieverFollowingPlayer && wasFollowingPlayer) {
            isRandomMovement = true;
            if (!isPathTile(monsterX, monsterY, path2Layer)) {
                Vector2 nearestPath2Tile = findNearestPath2Tile();
                if (nearestPath2Tile != null) {
                    currentTarget = nearestPath2Tile;
                }
            } else {
                currentTarget = findNextTargetWithMinDistance(10f);
            }
        }
    }

    /**
     * Finds the nearest valid tile in the path2Layer within a maximum search radius.
     * The search is done in 45-degree increments around the Griever's current position.
     *
     * @return A Vector2 representing the coordinates of the nearest valid tile, or null if no valid tile is found.
     */
    private Vector2 findNearestPath2Tile() {
        float searchRadius = path2Layer.getTileWidth();
        final float MAX_SEARCH_RADIUS = path2Layer.getTileWidth() * 10;

        while (searchRadius <= MAX_SEARCH_RADIUS) {
            for (int angleStep = 0; angleStep < 360; angleStep += 45) {
                float angle = (float) Math.toRadians(angleStep);
                float searchX = monsterX + (float) (Math.cos(angle) * searchRadius);
                float searchY = monsterY + (float) (Math.sin(angle) * searchRadius);

                if (isPathTile(searchX, searchY, path2Layer) &&
                        isPathClear(monsterX, monsterY, searchX, searchY)) {
                    return new Vector2(searchX, searchY);
                }
            }
            searchRadius += path2Layer.getTileWidth();
        }
        return null;
    }



    /**
     * Handles the Griever's random movement when it is not following the player.
     * It calculates a random target tile to move towards and checks if the Griever is in a valid path tile.
     *
     * @param delta The time in seconds since the last update.
     */    private void handleRandomMovement(float delta) {
        isRandomMovement = true;

        // if it's not in path2 layer
        if (!isPathTile(monsterX, monsterY, path2Layer)) {
            if (currentTarget == null) {
                Vector2 nearestPath2Tile = findNearestPath2Tile();
                if (nearestPath2Tile != null) {
                    currentTarget = nearestPath2Tile;
                } else {
                    return;
                }
            }
            // when moving to path2Layer, griever can use path layer
            moveTowardsTarget(delta, pathLayer);
        } else {
            if (currentTarget == null || reachedTarget()) {
                currentTarget = findNextTargetWithMinDistance(10f);
                if (currentTarget == null) {
                    return;
                }
            }
            moveTowardsTarget(delta, path2Layer);
        }
    }


    /**
     * Finds the next target tile for random movement, ensuring the target is at least a minimum distance
     * away from the Griever's current position.
     *
     * @param minDistance The minimum distance the target tile should be away from the Griever's current position.
     * @return A Vector2 representing the coordinates of the target tile, or null if no valid target is found.
     */    private Vector2 findNextTargetWithMinDistance(float minDistance) {
        List<Vector2> directions = Arrays.asList(
                new Vector2(1, 0),
                new Vector2(-1, 0),
                new Vector2(0, 1),
                new Vector2(0, -1)
        );
        Collections.shuffle(directions);


        for (Vector2 direction : directions) {

            float nextX = monsterX + direction.x * path2Layer.getTileWidth();
            float nextY = monsterY + direction.y * path2Layer.getTileHeight();


            if (isPathTile(nextX, nextY, path2Layer)) {

                float distance = Vector2.dst(monsterX, monsterY, nextX, nextY);

                if (distance >= minDistance) {
                    return new Vector2(nextX, nextY);
                }
            }
        }
        return null;
    }

    /**
     * Moves the Griever towards a specified target tile on the given layer.
     *
     * @param delta The time in seconds since the last update.
     * @param currentLayer The layer on which the Griever should move.
     */
    private void moveTowardsTarget(float delta, TiledMapTileLayer currentLayer) {
        Vector2 directionToTarget = new Vector2(currentTarget.x - monsterX, currentTarget.y - monsterY);
        float distanceToTarget = directionToTarget.len();

        if (distanceToTarget > 1f) {
            directionToTarget.nor();
            float deltaX = directionToTarget.x * monsterSpeed * delta;
            float deltaY = directionToTarget.y * monsterSpeed * delta;

            updateGrieverDirection(deltaX, deltaY);

            float newX = monsterX + deltaX;
            float newY = monsterY + deltaY;

            if (isValidPosition(newX, newY) && isPathTile(newX, newY, currentLayer)) {
                monsterX = newX;
                monsterY = newY;
            } else {
                handleCollision();
            }
        }
    }

    /**
     * Checks if the specified position (x, y) is within the valid boundaries defined by the game.
     *
     * @param x The X-coordinate to check.
     * @param y The Y-coordinate to check.
     * @return true if the position is within the valid boundaries, false otherwise.
     */    private boolean isValidPosition(float x, float y) {
        return x >= MIN_X && x <= MAX_X && y >= MIN_Y && y <= MAX_Y;
    }


    /**
     * Handles collision with obstacles. The Griever will either recalculate its path or move to a random target.
     */    private void handleCollision() {
        if (isGrieverFollowingPlayer) {
            currentTarget = null;  // Force recalculation of path
        } else {
            currentTarget = findNextTargetWithMinDistance(15f);
        }
    }


    /**
     * Helper method to calculate the distance between the Griever and the player.
     *
     * @param playerX The X-coordinate of the player.
     * @param playerY The Y-coordinate of the player.
     * @return The calculated distance.
     */    private float calculateDistance(float playerX, float playerY) {
        return (float) Math.sqrt(Math.pow(playerX - monsterX, 2) + Math.pow(playerY - monsterY, 2));
    }

    /**
     * Checks if the Griever has reached its target tile.
     *
     * @return true if the Griever has reached the target, false otherwise.
     */
    private boolean reachedTarget() {
        float tolerance = 2f;
        return Math.abs(monsterX - currentTarget.x) < tolerance &&
                Math.abs(monsterY - currentTarget.y) < tolerance;
    }

    /**
     * Checks if a given position (x, y) is a valid path tile on the specified layer.
     *
     * @param x The X-coordinate to check.
     * @param y The Y-coordinate to check.
     * @param layer The TiledMapTileLayer to check for path tiles.
     * @return true if the position is a valid path tile, false otherwise.
     */
    private boolean isPathTile(float x, float y, TiledMapTileLayer layer) {
        int tileX = (int) (x / layer.getTileWidth());
        int tileY = (int) (y / layer.getTileHeight());
        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null;
    }

    /**
     * Checks if the path between the current position and the target position is clear, meaning there are no obstacles.
     *
     * @param startX The starting X-coordinate.
     * @param startY The starting Y-coordinate.
     * @param endX The target X-coordinate.
     * @param endY The target Y-coordinate.
     * @return true if the path is clear, false otherwise.
     */
    private boolean isPathClear(float startX, float startY, float endX, float endY) {
        float steps = 30;
        for (int i = 0; i <= steps; i++) {
            float t = i / steps;
            float checkX = startX + (endX - startX) * t;
            float checkY = startY + (endY - startY) * t;

            if (!isPathTile(checkX, checkY, pathLayer)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the Griever collides with the player. If a collision occurs, the player loses a life,
     * and the Griever performs the appropriate actions.
     *
     * @param player The player instance.
     * @param hud The HUD (Heads Up Display) to manage player lives.
     * @param friends The Friends instance to manage the state of the player's friends.
     * @param delta The time in seconds since the last update.
     */
    public void checkPlayerCollision(Player player, HUD hud, Friends friends, float delta) {
        int diffX = (int) (player.getX() - this.getMonsterX());
        int diffY = (int) (player.getY() - this.getMonsterY());
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

        if (LivesCoolDownTimer <= 0 && distance < 5f && this.isGrieverNotStunned()) {
            if (hud.getLives() >= 0) {
                friends.removeFriendAt(friends.getFollowingFriendsPositions().size()-1);
                hud.decrementLives();
                player.triggerRedEffect();
                LivesCoolDownTimer = 2;
            } else {
                hud.setLives(0);
                player.revertToPrevious();
                player.setDead();
            }
        }

        if (LivesCoolDownTimer > 0) {
            LivesCoolDownTimer -= delta;
        }
    }


    /**
     * Renders the Griever at its current position using the specified SpriteBatch.
     * The Griever's texture is drawn at the current monsterX and monsterY coordinates,
     * with scaling applied to the texture's width and height.
     *
     * @param batch The SpriteBatch used for rendering the Griever's texture.
     */    public void render(SpriteBatch batch) {
        batch.draw(griever, monsterX, monsterY, griever.getWidth() * scale, griever.getHeight() * scale);
    }

    //Setters and Getters
    public void setPosition(float x, float y) {
        this.monsterX = x;
        this.monsterY = y;
    }
    private Texture getGrieverTextureForDirection(String direction) {
        Texture[] textures = grieverTextures.get(direction);
        return (griever == textures[0]) ? textures[1] : textures[0];
    }
    public float getMonsterX() {
        return monsterX;
    }
    public float getMonsterY() {
        return monsterY;
    }
    public boolean isGrieverNotStunned() {
        return !isGrieverStunned;
    }
    public float getWidth() {
        return griever.getWidth();
    }
    public float getHeight() {
        return griever.getHeight();
    }
    public float getScale() {
        return scale;
    }

    /**
     * Saves the current state of the Griever (position, stunned state, following state, random movement state,
     * and lives cooldown) to the preferences file.
     *
     * @param index The index used to differentiate between different Griever states in the preferences file.
     */
    public void saveGrieverstate(int index) {
        Preferences pref = Gdx.app.getPreferences("grieverstate");
        pref.putFloat("x_" + index, monsterX);
        pref.putFloat("y_" + index, monsterY);
        pref.putBoolean("isGrieverStunned_" + index, isGrieverStunned);
        pref.putBoolean("isGrieverFollowing_" + index, isGrieverFollowingPlayer);
        pref.putBoolean("isGrieverRandom_" + index, isRandomMovement);
        pref.putFloat("livescooldown_" + index, LivesCoolDownTimer);
        pref.flush();
    }

    /**
     * Loads the Griever's state (position, stunned state, following state, random movement state,
     * and lives cooldown) from the preferences file based on the given index.
     *
     * @param index The index used to load the specific Griever state from the preferences file.
     */
    public void loadGrieverstate(int index) {
        Preferences pref = Gdx.app.getPreferences("grieverstate");
        monsterX = pref.getFloat("x_" + index, monsterX);
        monsterY = pref.getFloat("y_" + index, monsterY);
        isGrieverStunned = pref.getBoolean("isGrieverStunned_" + index, isGrieverStunned);
        isGrieverFollowingPlayer = pref.getBoolean("isGrieverFollowing_" + index, isGrieverFollowingPlayer);
        isRandomMovement = pref.getBoolean("isGrieverRandom_" + index, isRandomMovement);
        LivesCoolDownTimer = pref.getFloat("livescooldown_" + index, LivesCoolDownTimer);
    }


    /**
     * Disposes of all textures related to the Griever to free up memory when no longer needed.
     * This method iterates over all Griever textures and disposes of them.
     */
    public void dispose() {
        for (Texture[] textures : grieverTextures.values()) {
            for (Texture texture : textures) {
                texture.dispose();
            }
        }
    }
}