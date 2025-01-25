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
 * This class handles rendering, collection mechanics, and state persistence for items.
 *
 * Items are loaded from a specific layer in the TiledMap, rendered as textures,
 * and can be collected by the player if they are within a specified proximity.
 *
 * Subclasses must implement the {@link #onCollected()} method to define behavior when an item is collected.
 */
public abstract class CollectibleItem {
  ;
    // Core components
    private final List<Texture> textures;
    private final List<Vector2> positions;
    private final float scale;

    /**
     * Constructs a CollectibleItem with the specified textures, map, and layer name.
     *
     * @param textures  the list of textures representing the collectible items
     * @param map       the TiledMap containing item positions
     * @param layerName the name of the layer in the TiledMap where item positions are defined
     */
    protected CollectibleItem(List<Texture> textures, TiledMap map, String layerName) {
        this.textures = new ArrayList<>(textures);
        this.positions = initializePositions(map, layerName);
        this.scale = 0.2f;
    }

    /**
     * Initializes item positions based on the objects in the specified TiledMap layer.
     *
     * @param map       the TiledMap containing item positions
     * @param layerName the name of the layer to extract item positions from
     * @return a list of positions extracted from the map
     */
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

    /**
     * Extracts the position from a map object, supporting rectangle objects or direct coordinates.
     *
     * @param object the map object to extract the position from
     * @return the extracted position, or null if no valid position is found
     */
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

    /**
     * Renders all collectible items at their current positions.
     *
     * @param batch the SpriteBatch used for rendering
     */
    public void render(SpriteBatch batch) {
        for (int i = 0; i < positions.size(); i++) {
            renderSingleItem(batch, i);
        }
    }

    /**
     * Renders a single collectible item at the specified index.
     *
     * @param batch the SpriteBatch used for rendering
     * @param index the index of the item to render
     */
    private void renderSingleItem(SpriteBatch batch, int index) {
        Texture texture = textures.get(index % textures.size());
        Vector2 position = positions.get(index);

        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;

        batch.draw(texture, position.x, position.y, width, height);
    }


    /**
     * Checks if the player is within range to collect all nearby items.
     *
     * @param playerPosition the player's current position
     * @param proximity      the maximum distance for item collection
     * @return the number of items collected
     */
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

    /**
     * Checks if the player is within the specified proximity to the item.
     *
     * @param playerPos the player's position
     * @param itemPos   the item's position
     * @param proximity the maximum distance for collection
     * @return true if the player is within range, false otherwise
     */
    private boolean isWithinCollectionRange(Vector2 playerPos, Vector2 itemPos, float proximity) {
        return playerPos.dst(itemPos) <= proximity;
    }

    /**
     * Abstract method to define behavior when an item is collected.
     * Must be implemented by subclasses.
     */
    protected abstract void onCollected();

    /**
     * Saves the current state of collectible items to preferences.
     *
     * @param preferences the Preferences instance for saving state
     * @param prefix      a prefix to differentiate this item's data
     */
    public void saveState(Preferences preferences, String prefix) {
        preferences.putInteger(prefix + "itemCount", positions.size());

        for (int i = 0; i < positions.size(); i++) {
            Vector2 position = positions.get(i);
            savePosition(preferences, prefix, i, position);
        }

        preferences.flush();
    }

    /**
     * Saves the position of a specific item to preferences.
     *
     * @param preferences the Preferences instance for saving state
     * @param prefix      a prefix to differentiate this item's data
     * @param index       the index of the item
     * @param position    the position of the item
     */
    private void savePosition(Preferences preferences, String prefix, int index, Vector2 position) {
        preferences.putFloat(prefix + "itemPosX" + index, position.x);
        preferences.putFloat(prefix + "itemPosY" + index, position.y);
    }

    /**
     * Loads the state of collectible items from preferences.
     *
     * @param preferences the Preferences instance containing saved state
     * @param prefix      a prefix to differentiate this item's data
     */
    public void loadState(Preferences preferences, String prefix) {
        positions.clear();
        int itemCount = preferences.getInteger(prefix + "itemCount", 0);

        for (int i = 0; i < itemCount; i++) {
            loadPosition(preferences, prefix, i);
        }
    }

    /**
     * Loads the position of a specific item from preferences.
     *
     * @param preferences the Preferences instance containing saved state
     * @param prefix      a prefix to differentiate this item's data
     * @param index       the index of the item
     */
    private void loadPosition(Preferences preferences, String prefix, int index) {
        float x = preferences.getFloat(prefix + "itemPosX" + index, 0);
        float y = preferences.getFloat(prefix + "itemPosY" + index, 0);
        positions.add(new Vector2(x, y));
    }

    /**
     * Disposes of textures used by this collectible item to free memory.
     */
    public void dispose() {
        textures.forEach(Texture::dispose);
    }
}