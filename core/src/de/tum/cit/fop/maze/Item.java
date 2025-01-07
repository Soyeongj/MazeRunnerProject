package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Gdx;

public class Item {
    private Texture[] items = {
            new Texture("potion_red.png"),
            new Texture("potion_red.png"),
            new Texture("potion_red.png"),
    };
    private Vector2[] itemPositions = {
            new Vector2(120, 183),
            new Vector2(110, 256),
            new Vector2(220, 283),
    };
    private boolean[] isItemCollected = {false,false,false};
    private float scale = 0.2f;
    private static final String PREFERENCES_NAME = "ItemState";

    public Item() {
    }
    public void render(SpriteBatch batch) {
        for (int i = 0; i < itemPositions.length; i++) {
            if (!isItemCollected[i]) {
                batch.draw(items[i], itemPositions[i].x, itemPositions[i].y,  items[i].getWidth() * scale, items[i].getHeight() * scale);
            }
        }
    }
    public boolean checkAndCollectSpeedItem(Vector2 playerPosition, float proximity, int index) {
        if (!isItemCollected[index]) {
            float distance = playerPosition.dst(itemPositions[index]);
            if (distance <= proximity) {
                isItemCollected[index] = true;
                itemPositions[index] = new Vector2(-1000, -1000);
                return true;
            }
        }
        return false;
    }

    public int checkAndCollectAllItmes(Vector2 playerPosition, float proximity) {
        int count = 0;
        for (int i = 0; i < itemPositions.length; i++) {
            if (checkAndCollectSpeedItem(playerPosition, proximity, i)) {
                count++;
            }
        }
        return count;
    }
    public void update(Player player, HUD hud, float interactionRadius) {
        int collectedItems = checkAndCollectAllItmes(new Vector2(player.getX(), player.getY()), interactionRadius);
        for (int i = 0; i < collectedItems; i++) {
            player.increaseSpeed(3f);
        }
    }


    public void dispose() {
        for (Texture item : items) {
            item.dispose();
        }
    }

    public Vector2[] getItemPositions() {
        return itemPositions;
    }

    public void setItemPositions(Vector2[] itemPositions) {
        this.itemPositions = itemPositions;
    }

    public boolean[] getIsItemCollected() {
        return isItemCollected;
    }

    public void setIsItemCollected(boolean[] isItemCollected) {
        this.isItemCollected = isItemCollected;
    }

    public void saveItemState() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        for (int i = 0; i < isItemCollected.length; i++) {
            preferences.putBoolean("itemCollected" + i, isItemCollected[i]);
            preferences.putFloat("itemPosX" + i, itemPositions[i].x);
            preferences.putFloat("itemPosY" + i, itemPositions[i].y);
        }
        preferences.flush();
    }

    public void loadItemState() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        for (int i = 0; i < isItemCollected.length; i++) {
            isItemCollected[i] = preferences.getBoolean("itemCollected" + i, false);
            float x = preferences.getFloat("itemPosX" + i, itemPositions[i].x);
            float y = preferences.getFloat("itemPosY" + i, itemPositions[i].y);
            itemPositions[i] = new Vector2(x, y);
        }
    }
}