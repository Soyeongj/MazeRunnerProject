package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Friends {
    private Texture[] friends = {
            new Texture("oldman_right_1.png"),
            new Texture("oldman_right_1.png"),
            new Texture("oldman_right_1.png")
    };
    private float friend1x = 300, friend1y = 300;
    private float friend2x = 500, friend2y = 400;
    private float friend3x = 600, friend3y = 500;
    private Vector2[] friendsPositions = {
            new Vector2(friend1x, friend1y),
            new Vector2(friend2x, friend2y),
            new Vector2(friend3x, friend3y),
    };
    private boolean[] isFriendSaved = {false, false, false};
    public Friends() {
    }
    public void render(SpriteBatch batch) {
        for (int i = 0; i < friendsPositions.length; i++) {
            if (!isFriendSaved[i]) {
                batch.draw(friends[i], friendsPositions[i].x, friendsPositions[i].y);
            }
        }
    }
    public boolean checkAndSaveFriend(Vector2 playerPosition, float proximity, int index) {
        if (!isFriendSaved[index]) {
            float distance = playerPosition.dst(friendsPositions[index]);
            if (distance <= proximity) {
                isFriendSaved[index] = true;
                friendsPositions[index] = new Vector2(-1000, -1000);
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

    public void dispose() {
        for (Texture friend : friends) {
            friend.dispose();
        }
    }
}




