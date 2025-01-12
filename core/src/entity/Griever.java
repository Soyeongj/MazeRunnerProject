package entity;

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
import game.HUD;
import render.Renderable;

import java.util.*;

public class Griever implements Renderable {
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

    public Griever(float startX, float startY, TiledMapTileLayer pathLayer, TiledMapTileLayer path2Layer) {
        this.monsterX = startX;
        this.monsterY = startY;
        this.pathLayer = pathLayer;
        this.path2Layer = path2Layer;

        initializeTextures();
    }

    private void initializeTextures() {
        grieverTextures = new HashMap<>();
        grieverTextures.put("up", new Texture[]{new Texture("character/monster_up1.png"), new Texture("character/monster_up2.png")});
        grieverTextures.put("down", new Texture[]{new Texture("character/monster_down1.png"), new Texture("character/monster_down2.png")});
        grieverTextures.put("left", new Texture[]{new Texture("character/monster_left1.png"), new Texture("character/monster_left2.png")});
        grieverTextures.put("right", new Texture[]{new Texture("character/monster_right1.png"), new Texture("character/monster_right2.png")});

        fixedGrieverDirection = "right";
        griever = grieverTextures.get(fixedGrieverDirection)[0];
        grieverStateTime = 0f;
    }

    // Updates griever's direction based on movement for animation
    private void updateGrieverDirection(float deltaX, float deltaY) {
        if (Math.abs(deltaX) > 0.01f || Math.abs(deltaY) > 0.01f) {
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                fixedGrieverDirection = deltaX > 0 ? "right" : "left";
            } else {
                fixedGrieverDirection = deltaY > 0 ? "up" : "down";
            }
        }
    }

    private void updateAnimation(float delta) {
        grieverStateTime += delta;
        if (grieverStateTime >= grieverAnimationTime) {
            griever = getGrieverTextureForDirection(fixedGrieverDirection);
            grieverStateTime = 0;
        }
    }


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

    // Main update method orchestrating all update logic
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

    // Handles stun state and returns true if griever is stunned
    private boolean handleStunState(float delta, HUD hud) {
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

    private boolean isGrieverInOppositeDirection(String playerDirection) {
        boolean result = (fixedGrieverDirection.equals("left") && playerDirection.equals("right")) ||
                (fixedGrieverDirection.equals("right") && playerDirection.equals("left")) ||
                (fixedGrieverDirection.equals("up") && playerDirection.equals("down")) ||
                (fixedGrieverDirection.equals("down") && playerDirection.equals("up"));
        return result;
    }




    // Handles all movement logic
    private void handleMovement(float delta, float playerX, float playerY) {
        if (isGrieverFollowingPlayer) {
            handlePlayerFollowing(delta, playerX, playerY);
        } else {
            handleRandomMovement(delta);
        }
    }

    // Handles movement when following player
    private void handlePlayerFollowing(float delta, float playerX, float playerY) {
        if (currentTarget == null || reachedTarget()) {
            updateTargetTowardsPlayer(playerX, playerY);
        }

        if (currentTarget != null) {
            moveTowardsTarget(delta, pathLayer);
        }
    }

    // Updates target tile when following player
    private void updateTargetTowardsPlayer(float playerX, float playerY) {
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

    // Updates griever's state based on player position
    private void updateGrieverState(float playerX, float playerY) {
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



    // Handles random movement when not following player
    private void handleRandomMovement(float delta) {
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


    //finds target tile for random movement
    private Vector2 findNextTargetWithMinDistance(float minDistance) {
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
    // Add method to check if position is within boundaries
    private boolean isValidPosition(float x, float y) {
        return x >= MIN_X && x <= MAX_X && y >= MIN_Y && y <= MAX_Y;
    }


    // Handles collision with obstacles
    private void handleCollision() {
        if (isGrieverFollowingPlayer) {
            currentTarget = null;  // Force recalculation of path
        } else {
            currentTarget = findNextTargetWithMinDistance(15f);
        }
    }


    // Helper method to calculate distance to player
    private float calculateDistance(float playerX, float playerY) {
        return (float) Math.sqrt(Math.pow(playerX - monsterX, 2) + Math.pow(playerY - monsterY, 2));
    }


    private boolean reachedTarget() {
        float tolerance = 2f;
        return Math.abs(monsterX - currentTarget.x) < tolerance &&
                Math.abs(monsterY - currentTarget.y) < tolerance;
    }


    private boolean isPathTile(float x, float y, TiledMapTileLayer layer) {
        int tileX = (int) (x / layer.getTileWidth());
        int tileY = (int) (y / layer.getTileHeight());
        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null;
    }


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


    //Render Method
    public void render(SpriteBatch batch) {
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



    public void dispose() {
        for (Texture[] textures : grieverTextures.values()) {
            for (Texture texture : textures) {
                texture.dispose();
            }
        }
    }


    public void saveGrieverstate() {
        Preferences pref = Gdx.app.getPreferences("grieverstate");
        pref.putFloat("x", monsterX);
        pref.putFloat("y", monsterY);
        pref.putBoolean("isGrieverStunned", isGrieverStunned);
        pref.putBoolean("isGrieverFollowing",isGrieverFollowingPlayer);
        pref.putBoolean("isGrieverRandom",isRandomMovement);
        pref.putFloat("livescooldown",LivesCoolDownTimer);
        pref.flush();
    }
    public void loadGrieverstate() {
        Preferences pref = Gdx.app.getPreferences("grieverstate");
        monsterX = pref.getFloat("x", monsterX);
        monsterY = pref.getFloat("y", monsterY);
        isGrieverStunned = pref.getBoolean("isGrieverStunned", isGrieverStunned);
        isGrieverFollowingPlayer = pref.getBoolean("isGrieverFollowing", isGrieverFollowingPlayer);
        isRandomMovement = pref.getBoolean("isRandomMovement", isRandomMovement);
        LivesCoolDownTimer = pref.getFloat("livescooldown", LivesCoolDownTimer);
    }
}