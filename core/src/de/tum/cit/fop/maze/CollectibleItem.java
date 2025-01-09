package de.tum.cit.fop.maze;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class CollectibleItem implements Renderable {
    protected Texture[] textures;
    protected Vector2[] positions;
    protected boolean[] isCollected;
    protected float scale = 0.4f;

    protected CollectibleItem(Texture[] textures, Vector2[] positions) {
        this.textures = textures;
        this.positions = positions;
        this.isCollected = new boolean[textures.length];
    }

    @Override
    public void render(SpriteBatch batch) {
        for (int i = 0; i < positions.length; i++) {
            if (!isCollected[i]) {
                batch.draw(textures[i], positions[i].x, positions[i].y,
                        textures[i].getWidth() * scale, textures[i].getHeight() * scale);
            }
        }
    }

    public boolean checkAndCollect(Vector2 playerPosition, float proximity, int index) {
        if (!isCollected[index]) {
            float distance = playerPosition.dst(positions[index]);
            if (distance <= proximity) {
                isCollected[index] = true;
                positions[index] = new Vector2(-1000, -1000); // Move offscreen
                onCollected();
                return true;
            }
        }
        return false;
    }

    public int checkAndCollectAll(Vector2 playerPosition, float proximity) {
        int count = 0;
        for (int i = 0; i < positions.length; i++) {
            if (checkAndCollect(playerPosition, proximity, i)) {
                count++;
            }
        }
        return count;
    }

    protected abstract void onCollected();

    public void saveState(Preferences preferences, String prefix) {
        for (int i = 0; i < isCollected.length; i++) {
            preferences.putBoolean(prefix + "itemCollected" + i, isCollected[i]);
            preferences.putFloat(prefix + "itemPosX" + i, positions[i].x);
            preferences.putFloat(prefix + "itemPosY" + i, positions[i].y);
        }
        preferences.flush();
    }

    public void loadState(Preferences preferences, String prefix) {
        for (int i = 0; i < isCollected.length; i++) {
            isCollected[i] = preferences.getBoolean(prefix + "itemCollected" + i, false);
            float x = preferences.getFloat(prefix + "itemPosX" + i, positions[i].x);
            float y = preferences.getFloat(prefix + "itemPosY" + i, positions[i].y);
            positions[i] = new Vector2(x, y);
        }
    }



    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
    }
}
