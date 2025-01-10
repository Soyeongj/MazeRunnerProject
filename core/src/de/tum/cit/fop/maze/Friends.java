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

public class Friends {
    private Texture friendTexture;
    private List<Vector2> followingFriendsPositions = new ArrayList<>();
    private Vector2[] mapFriendsPositions;
    private boolean[] isMapFriendSaved;
    private static final float FOLLOWING_DISTANCE = 5f;
    private BitmapFont font;

    private float scale = 0.2f;
    private Vector2 lastPlayerPosition;

    private int followingFriendsCount;

    public Friends(TiledMap map, Player player) {
        friendTexture = new Texture("oldman_right_1.png");

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixel Game.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 10;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.BLACK;

        this.font = generator.generateFont(parameter);
        generator.dispose();

        font.getData().setScale(0.8f);
        lastPlayerPosition = new Vector2(player.getX(), player.getY());

        mapFriendsPositions = loadFriendPositions(map);
        isMapFriendSaved = new boolean[mapFriendsPositions.length];

        int followingFriendsCount = followingFriendsPositions.size();


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

    private void initializeInitialFollowers(Player player) {
        for (int i = 0; i < 3; i++) {
            Vector2 initialPosition = new Vector2(player.getX() - (i + 1) * FOLLOWING_DISTANCE, player.getY());
            followingFriendsPositions.add(initialPosition);
        }
    }

    public void render(SpriteBatch batch, Player player) {
        for (int i = 0; i < mapFriendsPositions.length; i++) {
            if (!isMapFriendSaved[i]) {
                batch.draw(friendTexture, mapFriendsPositions[i].x, mapFriendsPositions[i].y,
                        friendTexture.getWidth() * scale, friendTexture.getHeight() * scale);
                Vector2 playerPosition = new Vector2(player.getX(), player.getY());
                float distance = playerPosition.dst(mapFriendsPositions[i]);
                if (distance <= 50) {
                    font.draw(batch, "help me!", mapFriendsPositions[i].x - 9, mapFriendsPositions[i].y + 10);
                }
            }
        }

        for (Vector2 pos : followingFriendsPositions) {
            batch.draw(friendTexture, pos.x, pos.y,
                    friendTexture.getWidth() * scale, friendTexture.getHeight() * scale);
        }
    }

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
            Vector2 firstFriendTarget = new Vector2(player.getX(), player.getY());
            Vector2 firstFriendCurrent = followingFriendsPositions.get(0);
            followingFriendsPositions.set(0, firstFriendCurrent.lerp(firstFriendTarget, 0.1f));

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
                if (checkCollision(friendPosition.x, friendPosition.y, friendTexture.getWidth() * scale, friendTexture.getHeight() * scale,
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

    public int getFollowingFriendsCount() {
        return followingFriendsPositions.size();
    }

    public List<Vector2> getFollowingFriendsPositions() {
        return followingFriendsPositions;
    }
}

