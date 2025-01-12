package de.tum.cit.fop.maze;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class CollectibleItem implements Renderable {

    // Core Variables
    protected List<Texture> textures; // Array of textures
    protected List<Vector2> positions; // Array of item positions
    protected float scale = 0.2f; // Scale(size) for rendering items

    protected CollectibleItem(List<Texture> textures, TiledMap map, String layerName) {
        this.textures = new ArrayList<>(textures);
        this.positions = loadItemPositions(map, layerName);
    }

    // Helper Method: Load item positions from TiledMap
    private List<Vector2> loadItemPositions(TiledMap map, String layerName) {
        List<Vector2> positions = new ArrayList<>();

        MapObjects objects = map.getLayers().get(layerName).getObjects();

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                positions.add(new Vector2(rect.x, rect.y));
            } else if (object.getProperties().containsKey("x") && object.getProperties().containsKey("y")) {
                float x = object.getProperties().get("x", Float.class);
                float y = object.getProperties().get("y", Float.class);
                positions.add(new Vector2(x, y));
            }
        }
        return positions;
    }

    // Render Method: Draw uncollected items
    @Override
    public void render(SpriteBatch batch) {
        for (int i = 0; i < positions.size(); i++) {
            Texture texture = textures.get(i % textures.size()); // Use texture index cyclically if needed
            Vector2 position = positions.get(i);

            batch.draw(texture,
                    position.x,
                    position.y,
                    texture.getWidth() * scale,
                    texture.getHeight() * scale);
        }
    }


    // Collect a Single Item
    public boolean checkAndCollect(Vector2 playerPosition, float proximity, int index) {
        Iterator<Vector2> iterator = positions.iterator();
        while (iterator.hasNext()) {
            Vector2 position = iterator.next();
            float distance = playerPosition.dst(position);

            if (distance <= proximity) {
                iterator.remove(); // ArrayList에서 해당 요소 삭제
                onCollected();     // 수집 시 수행할 동작
                return true;
            }
        }
        return false;
    }

    // Check all items and return count of collected items
    public int checkAndCollectAll(Vector2 playerPosition, float proximity) {
        int count = 0;
        Iterator<Vector2> iterator = positions.iterator();

        while (iterator.hasNext()) {
            Vector2 position = iterator.next();
            if (playerPosition.dst(position) <= proximity) {
                iterator.remove(); // Remove collected item
                onCollected(); // Trigger custom behavior
                count++;
            }
        }

        return count;
    }

    // Abstract Method
    protected abstract void onCollected();

    // Store collection status and positions in preferences
    public void saveState(Preferences preferences, String prefix) {
        preferences.putInteger(prefix + "itemCount", positions.size());

        for (int i = 0; i < positions.size(); i++) {
            preferences.putFloat(prefix + "itemPosX" + i, positions.get(i).x);
            preferences.putFloat(prefix + "itemPosY" + i, positions.get(i).y);
        }

        preferences.flush();
    }

    // Restore collection status and positions from preferences
    public void loadState(Preferences preferences, String prefix) {
        int itemCount = preferences.getInteger(prefix + "itemCount", 0);
        positions.clear();

        for (int i = 0; i < itemCount; i++) {
            float x = preferences.getFloat(prefix + "itemPosX" + i, 0);
            float y = preferences.getFloat(prefix + "itemPosY" + i, 0);
            positions.add(new Vector2(x, y));
        }
    }

    // Dispose Resources: Free textures to avoid memory leaks
    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
    }
}
