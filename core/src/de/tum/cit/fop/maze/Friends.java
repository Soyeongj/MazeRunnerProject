package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;

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


    private boolean[] isFriendSaved = {false, false, false};
    private float scale = 1.0f;
    private BitmapFont font;

    public Friends() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixel Game.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 10;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.BLACK;

        // Generate the font
        this.font = generator.generateFont(parameter);
        generator.dispose();

        font.getData().setScale(0.8f);
    }
    public void render(SpriteBatch batch, Player player) {
        for (int i = 0; i < friendsPositions.length; i++) {
            if (!isFriendSaved[i]) {
                batch.draw(friends[i], friendsPositions[i].x, friendsPositions[i].y,  friends[i].getWidth() * scale, friends[i].getHeight() * scale);
                Vector2 playerPosition = new Vector2(player.getX(), player.getY());
                float distance = playerPosition.dst(friendsPositions[i]);
                if (distance <= 50) {
                    font.draw(batch,"help me!", friendsPositions[i].x-9, friendsPositions[i].y+10);
                }
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

    public void setScale(float scale) {
        this.scale = scale;  // Set new scale
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
}


