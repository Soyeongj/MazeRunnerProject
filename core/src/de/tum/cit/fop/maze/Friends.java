package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.Preferences;


public class Friends {
    private Texture[] friends = new Texture[6];

    private float friend1x = 132, friend1y = 183;
    private float friend2x = 105, friend2y = 256;
    private float friend3x = 210, friend3y = 283;

    private Vector2[] friendsPositions = {
            new Vector2(friend1x, friend1y),
            new Vector2(friend2x, friend2y),
            new Vector2(friend3x, friend3y)
    };

    private List<Vector2> followingFriendsPositions = new ArrayList<>();
    private static final float FOLLOWING_DISTANCE = 8f;
    private boolean[] isFriendSaved = new boolean[3];
    private float scale = 0.2f;
    private BitmapFont font;
    private Vector2 lastPlayerPosition;

    private float stateTime = 0f;
    private float walkAnimationTime = 0.1f;
    private Texture currentTexture;
    private Texture up1, up2, down1, down2, left1, left2, right1, right2;

    public Friends() {
        for (int i = 0; i < 6; i++) {
            friends[i] = new Texture("oldman_right_1.png");
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixel Game.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 10;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.BLACK;

        this.font = generator.generateFont(parameter);
        generator.dispose();

        font.getData().setScale(0.8f);
        lastPlayerPosition = new Vector2(0, 0);

        up1 = new Texture("oldman_up_1.png");
        up2 = new Texture("oldman_up_2.png");
        down1 = new Texture("oldman_down_1.png");
        down2 = new Texture("oldman_down_2.png");
        left1 = new Texture("oldman_left_1.png");
        left2 = new Texture("oldman_left_2.png");
        right1 = new Texture("oldman_right_1.png");
        right2 = new Texture("oldman_right_2.png");

        currentTexture = right1;

        initializeInitialFollowers();
    }

    private void initializeInitialFollowers() {
        for (int i = 0; i < 3; i++) {
            followingFriendsPositions.add(new Vector2(0, 0));
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
        for (int i = 0; i < friendsPositions.length; i++) {
            if (!isFriendSaved[i]) {
                batch.draw(currentTexture, friendsPositions[i].x, friendsPositions[i].y,
                        friends[i].getWidth() * scale, friends[i].getHeight() * scale);
                Vector2 playerPosition = new Vector2(player.getX(), player.getY());
                float distance = playerPosition.dst(friendsPositions[i]);
                if (distance <= 50) {
                    font.draw(batch, "help me!", friendsPositions[i].x - 9, friendsPositions[i].y + 10);
                }
            }
        }

        for (int i = 0; i < followingFriendsPositions.size(); i++) {
            Vector2 pos = followingFriendsPositions.get(i);
            batch.draw(currentTexture, pos.x, pos.y,
                    friends[i].getWidth() * scale, friends[i].getHeight() * scale);
        }
    }

    public boolean checkAndSaveFriend(Vector2 playerPosition, float proximity, int index) {
        if (!isFriendSaved[index]) {
            float distance = playerPosition.dst(friendsPositions[index]);
            if (distance <= proximity) {
                isFriendSaved[index] = true;
                followingFriendsPositions.add(new Vector2(playerPosition));
                SoundManager.playSaveFriendSound();
                return true;
            }
        }
        return false;
    }

    public int checkAndSaveAllFriends(Vector2 playerPosition, float proximity) {
        int savedFriends = 0;
        for (int i = 0; i < friendsPositions.length; i++) {
            if (checkAndSaveFriend(playerPosition, proximity, i)) {
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
            updateAnimationDirection(movementDirection, delta);

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

    public void update(Player player, HUD hud, float interactionRadius, float delta) {
        int savedFriends = checkAndSaveAllFriends(new Vector2(player.getX(), player.getY()), interactionRadius);
        for (int i = 0; i < savedFriends; i++) {
            hud.incrementLives();
        }
        updateFollowingPositions(player, delta);
    }


    public void dispose() {
        for (Texture friend : friends) {
            friend.dispose();
        }
        up1.dispose();
        up2.dispose();
        down1.dispose();
        down2.dispose();
        left1.dispose();
        left2.dispose();
        right1.dispose();
        right2.dispose();
    }


    public boolean removeLastSavedFriend() {
        followingFriendsPositions.remove(followingFriendsPositions.size() - 1);

        for (int i = isFriendSaved.length - 1; i >= 0; i--) {
            if (isFriendSaved[i]) {
                isFriendSaved[i] = false;
                friendsPositions[i] = new Vector2(-100, -100);
                return true;
            }
        }

        return false;
    }

    public void saveFriendsStates() {
        Preferences preferences = Gdx.app.getPreferences("Friends");

        for (int i = 0; i < friendsPositions.length; i++) {
            preferences.putFloat("friendsPositionX" + i, friendsPositions[i].x);
            preferences.putFloat("friendsPositionY" + i, friendsPositions[i].y);
        }

        for (int i = 0; i < isFriendSaved.length; i++) {
            preferences.putBoolean("isFriendSaved" + i, isFriendSaved[i]);
        }

        preferences.putInteger("followingFriendsCount", followingFriendsPositions.size());
        for (int i = 0; i < followingFriendsPositions.size(); i++) {
            preferences.putFloat("followingFriendPosX_" + i, followingFriendsPositions.get(i).x);
            preferences.putFloat("followingFriendPosY_" + i, followingFriendsPositions.get(i).y);
        }

        preferences.flush();
    }

    public void loadFriendsStates() {
        Preferences preferences = Gdx.app.getPreferences("Friends");

        for (int i = 0; i < friendsPositions.length; i++) {
            float x = preferences.getFloat("friendsPositionX" + i, friendsPositions[i].x);
            float y = preferences.getFloat("friendsPositionY" + i, friendsPositions[i].y);
            friendsPositions[i] = new Vector2(x, y);
        }

        for (int i = 0; i < isFriendSaved.length; i++) {
            isFriendSaved[i] = preferences.getBoolean("isFriendSaved" + i, false);
        }

        followingFriendsPositions.clear();
        int followingFriendsCount = preferences.getInteger("followingFriendsCount", 3);
        for (int i = 0; i < followingFriendsCount; i++) {
            float x = preferences.getFloat("followingFriendPosX_" + i, 0);
            float y = preferences.getFloat("followingFriendPosY_" + i, 0);
            followingFriendsPositions.add(new Vector2(x, y));
        }
    }
}