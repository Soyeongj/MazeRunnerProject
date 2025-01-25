package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * The Item class represents a collectible item in the game. This class extends the
 * CollectibleItem class and handles the functionality of collecting items, updating the
 * player's state, and saving or loading the item state.
 */
public class Item extends CollectibleItem {
    private static String PREFERENCES_NAME;

    /**
     * Constructor for creating an Item based on a TiledMap.
     * This constructor initializes the textures for the item using the provided map and layer name.
     *
     * @param map The TiledMap to extract item textures from.
     */
    public Item(TiledMap map) {
        super(generateTextures(map,"item"),map,"item");
        this.PREFERENCES_NAME = "ItemState";
    }

    /**
     * Generates a list of textures for the item from the specified TiledMap and layer name.
     * If there are no specific textures for the item in the map, a default texture is used.
     *
     * @param map The TiledMap containing the item layer.
     * @param layerName The layer name to look for items in the map.
     * @return A list of textures for the item.
     */
    private static List<Texture> generateTextures(TiledMap map, String layerName) {
        MapObjects objects = map.getLayers().get(layerName).getObjects();
        int objectCount = objects.getCount();
        Texture defaultTexture = new Texture("potion_red.png");
        List<Texture> textures = new ArrayList<>();
        for (int i = 0; i < objectCount; i++) {
            textures.add(defaultTexture);
        }
        return textures;
    }

    @Override
    /**
     * This method is called when the item is collected. It plays the item collected sound.
     */
    protected void onCollected() {
        SoundManager.playItemCollectedSound();
    }

    /**
     * Updates the state of the item by checking if the player is within the interaction radius
     * and collects the item if possible. The player's speed is increased for each collected item.
     *
     * @param player The player object that is attempting to collect the item.
     * @param interactionRadius The radius within which the player can collect the item.
     */
    public void update(Player player, float interactionRadius) {
        int collectedItems = checkAndCollectAll(new Vector2(player.getX(), player.getY()), interactionRadius);
        for (int i = 0; i < collectedItems; i++) {
            player.increaseSpeed(3f); // Increase the player's speed upon collecting each item
        }
    }

    /**
     * Saves the current state of the item to preferences. This allows the game to remember
     * the item state between sessions.
     */
    public void saveItemState() {
        saveState(Gdx.app.getPreferences(PREFERENCES_NAME), "Item");
    }

    /**
     * Loads the saved state of the item from preferences. This allows the game to restore
     * the item state from a previous session.
     */
    public void loadItemState() {
        loadState(Gdx.app.getPreferences(PREFERENCES_NAME), "Item");
    }
}