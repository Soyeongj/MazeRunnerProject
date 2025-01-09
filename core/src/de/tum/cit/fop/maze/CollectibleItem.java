package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class CollectibleItem implements Renderable {

    protected Texture[] items;
    protected Vector2[] itemPositions;
    protected boolean[] isItemCollected;
    protected float scale = 0.2f;

    public CollectibleItem(Texture[] items, Vector2[] positions) {
        this.items = items;
        this.itemPositions = positions;
        this.isItemCollected = new boolean[items.length];
    }

    @Override
    public void render(SpriteBatch batch) {
        for (int i = 0; i < itemPositions.length; i++) {
            if (!isItemCollected[i]) {
                batch.draw(items[i], itemPositions[i].x, itemPositions[i].y,
                        items[i].getWidth() * scale, items[i].getHeight() * scale);
            }
        }
    }

    public boolean checkAndCollectItem(Vector2 playerPosition, float proximity, int index) {
        if (!isItemCollected[index]) {
            float distance = playerPosition.dst(itemPositions[index]);
            if (distance <= proximity) {
                isItemCollected[index] = true;
                itemPositions[index] = new Vector2(-1000, -1000); // Move offscreen
                applyEffect();
                return true;
            }
        }
        return false;
    }

    public void checkAndCollectAllItems(Vector2 playerPosition, float proximity) {
        for (int i = 0; i < itemPositions.length; i++) {
            checkAndCollectItem(playerPosition, proximity, i);
        }
    }

    public abstract void applyEffect();

    @Override
    public void dispose() {
        for (Texture item : items) {
            item.dispose();
        }
    }
}
