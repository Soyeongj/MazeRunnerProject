package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

public class Item extends CollectibleItem {
    private static final String PREFERENCES_NAME = "ItemState";

    public Item(TiledMap map) {
        super(generateTextures(map,"item"),map,"item");
    }

    private static Texture[] generateTextures(TiledMap map, String layerName) {
        MapObjects objects = map.getLayers().get(layerName).getObjects();
        int objectCount = objects.getCount();

        Texture[] textures = new Texture[objectCount];
        Texture defaultTexture = new Texture("potion_red.png");
        for (int i = 0; i < objectCount; i++) {
            textures[i] = defaultTexture;
        }
        return textures;
    }

    @Override
    protected void onCollected() {
        SoundManager.playItemCollectedSound();
    }

    public void update(Player player, float interactionRadius) {
        int collectedItems = checkAndCollectAll(new Vector2(player.getX(), player.getY()), interactionRadius);
        for (int i = 0; i < collectedItems; i++) {
            player.increaseSpeed(3f); // Increase the player's speed upon collecting each item
        }
    }

    public void saveItemState() {
        saveState(Gdx.app.getPreferences(PREFERENCES_NAME), "Item");
    }

    public void loadItemState() {
        loadState(Gdx.app.getPreferences(PREFERENCES_NAME), "Item");
    }
}