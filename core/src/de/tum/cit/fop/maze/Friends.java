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

public class Friends {
    private Texture[] friends = {
            new Texture("oldman_right_1.png"),
            new Texture("oldman_right_1.png"),
            new Texture("oldman_right_1.png")
    };
    private float friend1x = 132, friend1y = 183;
    private float friend2x = 105, friend2y = 256;
    private float friend3x = 210, friend3y = 283;
    private Vector2[] friendsPositions = {
            new Vector2(friend1x, friend1y),
            new Vector2(friend2x, friend2y),
            new Vector2(friend3x, friend3y),
    };

    private List<Vector2> savedFriendsPositions = new ArrayList<>();
    private static final float FOLLOWING_DISTANCE = 5f;
    private boolean[] isFriendSaved = {false, false, false};
    private float scale = 1.0f;
    private BitmapFont font;
    private Vector2 lastPlayerPosition; // To track player movement direction

    public Friends() {
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
    }

    public void render(SpriteBatch batch, Player player) {
        // Render unsaved friends
        for (int i = 0; i < friendsPositions.length; i++) {
            if (!isFriendSaved[i]) {
                batch.draw(friends[i], friendsPositions[i].x, friendsPositions[i].y,
                        friends[i].getWidth() * scale, friends[i].getHeight() * scale);
                Vector2 playerPosition = new Vector2(player.getX(), player.getY());
                float distance = playerPosition.dst(friendsPositions[i]);
                if (distance <= 50) {
                    font.draw(batch, "help me!", friendsPositions[i].x-9, friendsPositions[i].y+10);
                }
            }
        }

        // Render saved friends
        for (int i = 0; i < savedFriendsPositions.size(); i++) {
            Vector2 pos = savedFriendsPositions.get(i);
            batch.draw(friends[i], pos.x, pos.y,
                    friends[i].getWidth() * scale, friends[i].getHeight() * scale);
        }
    }

    private Vector2 getFollowPosition(Vector2 targetPos, Vector2 movementDirection) {
        Vector2 followPos = new Vector2();

        // If moving horizontally
        if (Math.abs(movementDirection.x) > Math.abs(movementDirection.y)) {
            // Moving right
            if (movementDirection.x > 0) {
                followPos.set(targetPos.x - FOLLOWING_DISTANCE, targetPos.y);
            }
            // Moving left
            else {
                followPos.set(targetPos.x + FOLLOWING_DISTANCE, targetPos.y);
            }
        }
        // If moving vertically
        else {
            // Moving up
            if (movementDirection.y > 0) {
                followPos.set(targetPos.x, targetPos.y - FOLLOWING_DISTANCE);
            }
            // Moving down
            else {
                followPos.set(targetPos.x, targetPos.y + FOLLOWING_DISTANCE);
            }
        }

        return followPos;
    }

    public boolean checkAndSaveFriend(Vector2 playerPosition, float proximity, int index) {
        if (!isFriendSaved[index]) {
            float distance = playerPosition.dst(friendsPositions[index]);
            if (distance <= proximity) {
                isFriendSaved[index] = true;
                // Position will be set in updateFollowingPositions
                savedFriendsPositions.add(new Vector2(playerPosition));
                return true;
            }
        }
        return false;
    }

    public void updateFollowingPositions(Player player) {
        if (savedFriendsPositions.isEmpty()) {
            lastPlayerPosition = new Vector2(player.getX(), player.getY());
            return;
        }

        Vector2 currentPlayerPos = new Vector2(player.getX(), player.getY());
        Vector2 movementDirection = new Vector2(
                currentPlayerPos.x - lastPlayerPosition.x,
                currentPlayerPos.y - lastPlayerPosition.y
        );

        // Only update positions if the player has moved
        if (movementDirection.len2() > 0) {
            // Update each friend's position
            for (int i = 0; i < savedFriendsPositions.size(); i++) {
                Vector2 targetPos;
                if (i == 0) {
                    // First friend follows player
                    targetPos = currentPlayerPos;
                } else {
                    // Other friends follow the friend in front of them
                    targetPos = savedFriendsPositions.get(i - 1);
                }

                Vector2 newPos = getFollowPosition(targetPos, movementDirection);
                savedFriendsPositions.set(i, newPos);
            }
        }

        // Update last player position for next frame
        lastPlayerPosition.set(currentPlayerPos);
    }

    public void update(Player player, HUD hud, float interactionRadius) {
        int savedFriends = checkAndSaveAllFriends(new Vector2(player.getX(), player.getY()), interactionRadius);
        for (int i = 0; i < savedFriends; i++) {
            hud.incrementLives();
        }
        updateFollowingPositions(player);
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

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void dispose() {
        for (Texture friend : friends) {
            friend.dispose();
        }
    }

    public Vector2[] getFriendsPositions() {
        return friendsPositions;
    }

    public void setFriendsPositions(Vector2[] friendsPositions) {
        this.friendsPositions = friendsPositions;
    }

    public boolean[] getIsFriendSaved() {
        return isFriendSaved;
    }

    public void setIsFriendSaved(boolean[] isFriendSaved) {
        this.isFriendSaved = isFriendSaved;
    }

    public boolean removeLastSavedFriend() {
        if (!savedFriendsPositions.isEmpty()) {
            // Remove the last friend from the following list
            savedFriendsPositions.remove(savedFriendsPositions.size() - 1);

            // Find the last saved friend and mark it as unsaved
            for (int i = isFriendSaved.length - 1; i >= 0; i--) {
                if (isFriendSaved[i]) {
                    isFriendSaved[i] = false;
                    // Reset the friend's position to be off-screen or to a designated "lost" position
                    friendsPositions[i] = new Vector2(-100, -100);
                    return true;
                }
            }
        }
        return false;
    }
}