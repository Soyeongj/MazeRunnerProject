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
 * Manages the "friends" in the maze game. Friends are either on the map waiting
 * to be saved or following the player after being saved. This class handles
 * their rendering, movement, saving, and interaction with the player and other entities.
 *
 * Friends can collide with walls or be caught by a Griever, affecting the player's lives.
 * It also manages saving and loading friend states.
 *
 */
public class Friends {

    private List<Vector2> followingFriendsPositions = new ArrayList<>();
    private Vector2[] mapFriendsPositions;
    private boolean[] isMapFriendSaved;
    private static float FOLLOWING_DISTANCE;
    private BitmapFont font;
    private float scale;
    private Vector2 lastPlayerPosition;

    // Variables for animation state
    private float stateTime;
    private float walkAnimationTime;
    private Texture currentTexture;
    private Texture up1, up2, down1, down2, left1, left2, right1, right2;

    //Friends States
    private static String PREFERENCES_NAME;


    /**
     * Constructs a Friends instance, initializing friends' textures, positions, and states.
     *
     * @param map    the TiledMap containing the friends' positions
     * @param player the player object for initial friend placement
     */
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

        // Initialize font for rendering "help me!" messages
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixel Game.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 10;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.BLACK;

        this.font = generator.generateFont(parameter);
        generator.dispose();
        font.getData().setScale(0.8f);

        // Initialize player-related positions and map friends
        lastPlayerPosition = new Vector2(player.getX(), player.getY());
        mapFriendsPositions = loadFriendPositions(map);
        isMapFriendSaved = new boolean[mapFriendsPositions.length];
        initializeInitialFollowers(player);

        this.FOLLOWING_DISTANCE = 5f;
        this.PREFERENCES_NAME = "friendsState";
        this.scale = 0.2f;
        this.stateTime = 0f;
        this.walkAnimationTime = 0.1f;
    }

    /**
     * Loads the positions of friends from the TiledMap layer.
     *
     * @param map the TiledMap containing the friend layer
     * @return an array of Vector2 representing friend positions
     */
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

    /**
     * Initializes a set number of friends to follow the player at the start of the game.
     *
     * @param player the player object for initial positioning
     */
    private void initializeInitialFollowers(Player player) {
        for (int i = 0; i < 3; i++) {
            Vector2 initialPosition = new Vector2(player.getX() - (i + 1) * FOLLOWING_DISTANCE, player.getY());
            followingFriendsPositions.add(initialPosition);
        }
    }

    /**
     * Updates the animation state by alternating between two textures based on a time threshold.
     * This method is used to create a walking animation effect for the character.
     *
     * @param delta     the time in seconds since the last frame
     * @param texture1  the first texture for the animation frame
     * @param texture2  the second texture for the animation frame
     */
    private void animate(float delta, Texture texture1, Texture texture2) {
        stateTime += delta;
        if (stateTime >= walkAnimationTime) {
            currentTexture = (currentTexture == texture1) ? texture2 : texture1;
            stateTime = 0f;
        }
    }

    /**
     * Determines the direction of movement based on the given direction vector
     * and updates the animation accordingly. It selects the appropriate textures
     * for the animation based on whether the movement is horizontal or vertical.
     *
     * @param direction the direction vector of the movement
     * @param delta     the time in seconds since the last frame
     */
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

    /**
     * Renders all friends, including map friends and followers.
     * Displays "help me!" above unsaved map friends when the player is nearby.
     *
     * @param batch  the SpriteBatch used for rendering
     * @param player the player object for proximity calculations
     */
    public void render(SpriteBatch batch, Player player) {
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

    /**
     * Checks if the player is within the specified proximity to a map friend at the given index.
     * If the friend is within range and has not been saved, they are marked as saved,
     * added to the list of following friends, and a sound effect is played.
     *
     * @param playerPosition the current position of the player
     * @param proximity      the maximum distance within which a friend can be saved
     * @param index          the index of the map friend to check
     * @return true if the friend was saved, false otherwise
     */
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

    /**
     * Iterates through all map friends to check if they are within the specified proximity to the player.
     * Marks any nearby unsaved friends as saved, adds them to the list of following friends,
     * and increments the count of saved friends.
     *
     * @param playerPosition the current position of the player
     * @param proximity      the maximum distance within which friends can be saved
     * @return the number of friends saved during this check
     */
    public int checkAndSaveAllMapFriends(Vector2 playerPosition, float proximity) {
        int savedFriends = 0;
        for (int i = 0; i < mapFriendsPositions.length; i++) {
            if (checkAndSaveMapFriend(playerPosition, proximity, i)) {
                savedFriends++;
            }
        }
        return savedFriends;
    }

    /**
     * Updates the positions of friends following the player based on the player's movement.
     * The first friend moves towards the player, while subsequent friends follow the friend
     * directly in front of them. If no friends are following, the last player position is updated.
     *
     * @param player the player whose movement the friends follow
     * @param delta  the time elapsed since the last frame
     */
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

    /**
     * Updates the positions of friends following the player, checking for collisions
     * with other game entities such as Grievers or walls.
     *
     * @param player            the player object for movement calculations
     * @param hud               the HUD for managing game statistics
     * @param interactionRadius the radius within which friends can be saved
     * @param delta             the time elapsed since the last frame
     * @param griever           the Griever object for collision detection
     * @param wall              the Wall object for collision detection
     */
    public void update(Player player, HUD hud, float interactionRadius, float delta, Griever griever,Wall wall) {
        int savedFriends = checkAndSaveAllMapFriends(new Vector2(player.getX(), player.getY()), interactionRadius);
        for (int i = 0; i < savedFriends; i++) {
            hud.incrementLives();
        }

        updateFollowingPositions(player, delta);
        checkFriendCollisionWithGriever(griever,hud);
        checkFriendsCollisionWithWall(wall,hud);
    }

    /**
     * Removes a friend at the specified index from the list of friends following the player.
     *
     * @param index the index of the friend to remove
     */
    public void removeFriendAt(int index) {
        if (index >= 0 && index < followingFriendsPositions.size()) {
            followingFriendsPositions.remove(index);
        }
    }

    /**
     * Checks for collisions between friends following the player and the Griever.
     * If a collision is detected, the friend is removed, and the player's lives are decremented.
     *
     * @param griever the Griever object representing the monster
     * @param hud     the HUD object for managing game statistics, such as player lives
     */
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

    /**
     * Checks for collisions between friends following the player and moving walls.
     * If a collision is detected, the friend is removed, and the player's lives are decremented.
     *
     * @param wall the Wall object representing the moving wall
     * @param hud  the HUD object for managing game statistics, such as player lives
     */
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

    /**
     * Checks for a collision between two rectangular objects.
     *
     * @param x1      the x-coordinate of the first rectangle
     * @param y1      the y-coordinate of the first rectangle
     * @param width1  the width of the first rectangle
     * @param height1 the height of the first rectangle
     * @param x2      the x-coordinate of the second rectangle
     * @param y2      the y-coordinate of the second rectangle
     * @param width2  the width of the second rectangle
     * @param height2 the height of the second rectangle
     * @return true if the rectangles overlap, false otherwise
     */
    private boolean checkCollision(float x1, float y1, float width1, float height1,
                                   float x2, float y2, float width2, float height2) {
        return x1 < x2 + width2 && x1 + width1 > x2 &&
                y1 < y2 + height2 && y1 + height1 > y2;
    }

    /**
     * Saves the state of all friends (both map friends and followers) to preferences.
     */
    public void saveFriendState() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
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

    /**
     * Loads the state of all friends (both map friends and followers) from preferences.
     */
    public void loadFriendState() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
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

    public List<Vector2> getFollowingFriendsPositions() {
        return followingFriendsPositions;
    }

    /**
     * Releases resources used by the Friends class, including textures and fonts.
     */
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