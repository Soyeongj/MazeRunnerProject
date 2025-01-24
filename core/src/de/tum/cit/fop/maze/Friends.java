package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages friends in the maze game, including their positions, animations,
 * following behavior, and interactions with other game elements.
 */

public class Friends {

    private List<Vector2> followingFriendsPositions = new ArrayList<>(); //A list that tracks the positions of friends who follow the player
    private Vector2[] mapFriendsPositions; //Positions of friends located on the map, defined in the TiledMap.
    private boolean[] isMapFriendSaved; // An array that keeps track of whether each friend has been saved by the player
    private static final float FOLLOWING_DISTANCE = 5f;
    private BitmapFont font;

    // Scaling factor for friend textures
    private float scale = 0.2f;

    // Store last position of the player for movement calculations
    private Vector2 lastPlayerPosition;

    // Variables for animation state
    private float stateTime = 0f;
    private float walkAnimationTime = 0.1f;
    private Texture currentTexture;
    private Texture up1, up2, down1, down2, left1, left2, right1, right2;


    public Friends(TiledMap map, Player player) {
        right1 = new Texture("oldman_right_1.png");
        right2 = new Texture("oldman_right_2.png");
        left1 = new Texture("oldman_left_1.png");
        left2 = new Texture("oldman_left_2.png");
        up1= new Texture("oldman_up_1.png");
        up2 = new Texture("oldman_up_2.png");
        down1 = new Texture("oldman_down_1.png");
        down2 = new Texture("oldman_down_2.png");

        currentTexture = down1;

        // Initialize font for displaying "help me!"
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixel Game.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 10;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.BLACK;

        this.font = generator.generateFont(parameter);
        generator.dispose();
        font.getData().setScale(0.8f);

        // Set the initial last player position
        lastPlayerPosition = new Vector2(player.getX(), player.getY());

        mapFriendsPositions = loadFriendPositions(map);
        isMapFriendSaved = new boolean[mapFriendsPositions.length];

        initializeInitialFollowers(player);
    }

    private Vector2[] loadFriendPositions(TiledMap map) {
        Array<Vector2> positions = new Array<>();
        MapLayer friendsLayer = map.getLayers().get("friend");

        MapObjects objects = friendsLayer.getObjects();
        for (MapObject object : objects) {
            float x = Float.parseFloat(object.getProperties().get("x").toString());
            float y = Float.parseFloat(object.getProperties().get("y").toString());
            Object friendProperty = object.getProperties().get("friend");
            if (friendProperty != null && "1".equals(friendProperty.toString())) {
                positions.add(new Vector2(x, y));
            }
        }

        return positions.toArray(Vector2.class);
    }

    // Initialize 3 followers behind the player at a distance of FOLLOWING_DISTANCE
    private void initializeInitialFollowers(Player player) {
        for (int i = 0; i < 3; i++) {
            Vector2 initialPosition = new Vector2(player.getX() - (i + 1) * FOLLOWING_DISTANCE, player.getY());
            followingFriendsPositions.add(initialPosition);
        }
    }

    private void animate(float delta, Texture texture1, Texture texture2) {
        stateTime += delta;
        if (stateTime >= walkAnimationTime) {
            currentTexture = (currentTexture == texture1) ? texture2 : texture1;
            stateTime = 0f;
        }
    }

    private void updateAnimationDirection(Vector2 direction, float delta) {
        if (Math.abs(direction.x) > Math.abs(direction.y)) {
            if (direction.x > 0) {
                animate(delta, right1, right2);
            } else {
                animate(delta, left1, left2);
            }
        } else {
            if (direction.y > 0) {
                animate(delta, up1, up2);
            } else {
                animate(delta, down1, down2);
            }
        }
    }

    public void render(SpriteBatch batch, Player player) {
        // Render map friends with "help me!" message if player is nearby
        for (int i = 0; i < mapFriendsPositions.length; i++) {
            if (!isMapFriendSaved[i]) {
                batch.draw(currentTexture, mapFriendsPositions[i].x, mapFriendsPositions[i].y,
                        currentTexture.getWidth() * scale, currentTexture.getHeight() * scale);
                Vector2 playerPosition = new Vector2(player.getX(), player.getY());
                float distance = playerPosition.dst(mapFriendsPositions[i]);
                if (distance <= 50) {
                    font.draw(batch, "help me!", mapFriendsPositions[i].x - 9, mapFriendsPositions[i].y + 10);
                }
            }
        }

        // Render the friends following the player
        for (Vector2 pos : followingFriendsPositions) {
            batch.draw(currentTexture, pos.x, pos.y,
                    currentTexture.getWidth() * scale, currentTexture.getHeight() * scale);
        }
    }
    // Check if the player is close enough to a map friend to save them.

    public boolean checkAndSaveMapFriend(Vector2 playerPosition, float proximity, int index) {
        if (!isMapFriendSaved[index]) {
            float distance = playerPosition.dst(mapFriendsPositions[index]);
            if (distance <= proximity) {
                isMapFriendSaved[index] = true;
                followingFriendsPositions.add(new Vector2(playerPosition));
                SoundManager.playSaveFriendSound();
                return true;
            }
        }
        return false;
    }

    public int checkAndSaveAllMapFriends(Vector2 playerPosition, float proximity) {
        int savedFriends = 0;
        for (int i = 0; i < mapFriendsPositions.length; i++) {
            if (checkAndSaveMapFriend(playerPosition, proximity, i)) {
                savedFriends++;
            }
        }
        return savedFriends;
    }

    public void updateFollowingPositions(Player player, float delta) {
        // If there are no friends following, just update the last position
        if (followingFriendsPositions.isEmpty()) {
            lastPlayerPosition = new Vector2(player.getX(), player.getY());
            return;
        }

        Vector2 currentPlayerPos = new Vector2(player.getX(), player.getY());
        Vector2 movementDirection = new Vector2(
                currentPlayerPos.x - lastPlayerPosition.x,
                currentPlayerPos.y - lastPlayerPosition.y
        );

        if (movementDirection.len2() > 0) {
            updateAnimationDirection(movementDirection, delta);

            // Update the first friend to move towards the player
            Vector2 firstFriendTarget = new Vector2(player.getX(), player.getY());
            Vector2 firstFriendCurrent = followingFriendsPositions.get(0);
            followingFriendsPositions.set(0, firstFriendCurrent.lerp(firstFriendTarget, 0.1f));

            // Update remaining friends to follow the friend in front of them
            for (int i = 1; i < followingFriendsPositions.size(); i++) {
                Vector2 currentFriendPos = followingFriendsPositions.get(i);
                Vector2 targetFriendPos = followingFriendsPositions.get(i - 1);
                Vector2 direction = new Vector2(
                        targetFriendPos.x - currentFriendPos.x,
                        targetFriendPos.y - currentFriendPos.y
                );

                if (direction.len() > FOLLOWING_DISTANCE) {
                    followingFriendsPositions.set(i, currentFriendPos.lerp(targetFriendPos, 0.1f));
                }
            }
        }

        lastPlayerPosition.set(currentPlayerPos);
    }

    public void update(Player player, HUD hud, float interactionRadius, float delta, Griever griever,Wall wall) {
        int savedFriends = checkAndSaveAllMapFriends(new Vector2(player.getX(), player.getY()), interactionRadius);
        for (int i = 0; i < savedFriends; i++) {
            hud.incrementLives();
        }

        updateFollowingPositions(player, delta);
        checkFriendCollisionWithGriever(griever,hud);
        checkFriendsCollisionWithWall(wall,hud);
    }


    public void removeFriendAt(int index) {
        if (index >= 0 && index < followingFriendsPositions.size()) {
            followingFriendsPositions.remove(index);
        }
    }

    public void checkFriendCollisionWithGriever(Griever griever, HUD hud) {
        for (int i = 0; i < followingFriendsPositions.size(); i++) {
            Vector2 friendPosition = followingFriendsPositions.get(i);

            float diffX = griever.getMonsterX() - friendPosition.x;
            float diffY = griever.getMonsterY() - friendPosition.y;
            float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

            if (distance < 5f) {
                followingFriendsPositions.remove(i);
                hud.decrementLives();
                i--;
            }
        }
    }

    public void checkFriendsCollisionWithWall(Wall wall, HUD hud) {
        for (int i = 0; i < followingFriendsPositions.size(); i++) {
            Vector2 friendPosition = followingFriendsPositions.get(i);

            float wallX = wall.getTargetX() * wall.getLayer().getTileWidth();
            float wallY = wall.getTargetY() * wall.getLayer().getTileHeight();
            float wallWidth = wall.getLayer().getTileWidth();
            float wallHeight = wall.getLayer().getTileHeight();

            if (wall.getX() != wall.getOriginalX() || wall.getY() != wall.getOriginalY() || wall.isAtTarget()) {
                if (checkCollision(friendPosition.x, friendPosition.y, currentTexture.getWidth() * scale, currentTexture.getHeight() * scale,
                        wallX, wallY, wallWidth, wallHeight)) {
                    removeFriendAt(i);
                    hud.decrementLives();
                    break;
                }
            }
        }
    }

    private boolean checkCollision(float x1, float y1, float width1, float height1,
                                   float x2, float y2, float width2, float height2) {
        return x1 < x2 + width2 && x1 + width1 > x2 &&
                y1 < y2 + height2 && y1 + height1 > y2;
    }

    public void saveFriendState() {
        Preferences preferences = Gdx.app.getPreferences("Friends");
        for (int i = 0; i < isMapFriendSaved.length; i++) {
            preferences.putBoolean("mapFriendSaved_" + i, isMapFriendSaved[i]);
        }

        preferences.putInteger("followingFriendsCount", followingFriendsPositions.size());
        for (int i = 0; i < followingFriendsPositions.size(); i++) {
            preferences.putFloat("friendPosX_" + i, followingFriendsPositions.get(i).x);
            preferences.putFloat("friendPosY_" + i, followingFriendsPositions.get(i).y);
        }

        preferences.flush();
    }

    public List<Vector2> getFollowingFriendsPositions() {
        return followingFriendsPositions;
    }

    public void loadFriendState() {
        Preferences preferences = Gdx.app.getPreferences("Friends");
        for (int i = 0; i < isMapFriendSaved.length; i++) {
            isMapFriendSaved[i] = preferences.getBoolean("mapFriendSaved_" + i, false);
        }

        int followingFriendsCount = preferences.getInteger("followingFriendsCount", 0);
        followingFriendsPositions.clear();
        for (int i = 0; i < followingFriendsCount; i++) {
            float x = preferences.getFloat("friendPosX_" + i, 0f);
            float y = preferences.getFloat("friendPosY_" + i, 0f);
            followingFriendsPositions.add(new Vector2(x, y));
        }

    }

    public void dispose() {
        currentTexture.dispose();
        up1.dispose();
        up2.dispose();
        down1.dispose();
        down2.dispose();
        left1.dispose();
        left2.dispose();
        right1.dispose();
        right2.dispose();
    }
}