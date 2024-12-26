package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
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

    public void dispose() {
        for (Texture item : items) {
            item.dispose();
        }
    }
}
