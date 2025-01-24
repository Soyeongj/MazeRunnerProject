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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract base class for collectible items in the maze game.
 * Handles rendering, collection mechanics, and state persistence.
 */
public abstract class CollectibleItem {
    // Constants
    private static final float DEFAULT_SCALE = 0.2f;

    // Core components
    private final List<Texture> textures;
    private final List<Vector2> positions;
    private final float scale;

    protected CollectibleItem(List<Texture> textures, TiledMap map, String layerName) {
        this.textures = new ArrayList<>(textures);
        this.positions = initializePositions(map, layerName);
        this.scale = DEFAULT_SCALE;
    }

    private List<Vector2> initializePositions(TiledMap map, String layerName) {
        List<Vector2> itemPositions = new ArrayList<>();
        MapObjects objects = map.getLayers().get(layerName).getObjects();

        for (MapObject object : objects) {
            Vector2 position = extractPosition(object);
            if (position != null) {
                itemPositions.add(position);
            }
        }

        return itemPositions;
    }

    private Vector2 extractPosition(MapObject object) {
        if (object instanceof RectangleMapObject) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            return new Vector2(rect.x, rect.y);
        } else if (object.getProperties().containsKey("x") &&
                object.getProperties().containsKey("y")) {
            float x = object.getProperties().get("x", Float.class);
            float y = object.getProperties().get("y", Float.class);
            return new Vector2(x, y);
        }
        return null;
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < positions.size(); i++) {
            renderSingleItem(batch, i);
        }
    }

    private void renderSingleItem(SpriteBatch batch, int index) {
        Texture texture = textures.get(index % textures.size());
        Vector2 position = positions.get(index);

        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;

        batch.draw(texture, position.x, position.y, width, height);
    }


    public int checkAndCollectAll(Vector2 playerPosition, float proximity) {
        int collectedCount = 0;
        Iterator<Vector2> iterator = positions.iterator();

        while (iterator.hasNext()) {
            Vector2 position = iterator.next();
            if (isWithinCollectionRange(playerPosition, position, proximity)) {
                iterator.remove();
                onCollected();
                collectedCount++;
            }
        }

        return collectedCount;
    }


    private boolean isWithinCollectionRange(Vector2 playerPos, Vector2 itemPos, float proximity) {
        return playerPos.dst(itemPos) <= proximity;
    }


    protected abstract void onCollected();


    public void saveState(Preferences preferences, String prefix) {
        preferences.putInteger(prefix + "itemCount", positions.size());

        for (int i = 0; i < positions.size(); i++) {
            Vector2 position = positions.get(i);
            savePosition(preferences, prefix, i, position);
        }

        preferences.flush();
    }


    private void savePosition(Preferences preferences, String prefix, int index, Vector2 position) {
        preferences.putFloat(prefix + "itemPosX" + index, position.x);
        preferences.putFloat(prefix + "itemPosY" + index, position.y);
    }


    public void loadState(Preferences preferences, String prefix) {
        positions.clear();
        int itemCount = preferences.getInteger(prefix + "itemCount", 0);

        for (int i = 0; i < itemCount; i++) {
            loadPosition(preferences, prefix, i);
        }
    }

    private void loadPosition(Preferences preferences, String prefix, int index) {
        float x = preferences.getFloat(prefix + "itemPosX" + index, 0);
        float y = preferences.getFloat(prefix + "itemPosY" + index, 0);
        positions.add(new Vector2(x, y));
    }

    public void dispose() {
        textures.forEach(Texture::dispose);
    }
}