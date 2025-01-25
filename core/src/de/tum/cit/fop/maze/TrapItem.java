package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The TrapItem class represents a collectible item in the game that triggers a special trap effect,
 * such as a fog effect, when collected by the player.
 */
public class TrapItem extends CollectibleItem {
    private long fogEffectStartTime;
    private boolean isFogActive;
    private static long FOG_DURATION;// Duration for how long the fog effect lasts
    private static String PREFERENCES_NAME;



    /**
     * Constructs a TrapItem object using the provided map.
     * Initializes the textures and sets up the item.
     *
     * @param map The TiledMap used to generate textures and set up the item
     */
    public TrapItem(TiledMap map) {
        super(generateTextures(map, "trapitem"), map, "trapitem");
        this.fogEffectStartTime = 0;
        this.isFogActive = false;
        this.FOG_DURATION = 3000;
        this.PREFERENCES_NAME = "trapitemStates";
    }

    /**
     * Generates a list of textures for the TrapItem based on the map layer.
     *
     * @param map       The TiledMap containing the trap item objects
     * @param layerName The name of the layer in the map
     * @return A list of textures representing the trap item
     */
    private static List<Texture> generateTextures(TiledMap map, String layerName) {
        MapObjects objects = map.getLayers().get(layerName).getObjects();
        int objectCount = objects.getCount();

        Texture defaultTexture = new Texture("bomb.png");
        List<Texture> textures = new ArrayList<>();

        for (int i = 0; i < objectCount; i++) {
            textures.add(defaultTexture);
        }
        return textures;
    }

    /**
     * Called when the TrapItem is collected. Plays the sound and activates the fog effect.
     */
    @Override
    protected void onCollected() {
        SoundManager.playEvilLaughSound();
        activateFogEffect();
    }

    /**
     * Activates the fog effect, marking it as active and setting the start time.
     */
    public void activateFogEffect() {
        isFogActive = true;
        fogEffectStartTime = TimeUtils.millis();
    }

    /**
     * Updates the fog effect, deactivating it once the duration has passed.
     */
    public void updateFogEffect() {
        if (isFogActive) {
            long currentTime = TimeUtils.millis();
            if (currentTime - fogEffectStartTime >= FOG_DURATION) {
                isFogActive = false;
            }
        }
    }

    /**
     * Updates the TrapItem by checking for player collection and updating the fog effect.
     *
     * @param player            The player interacting with the TrapItem
     * @param interactionRadius The radius within which the player can collect the item
     */
    public void update(Player player, float interactionRadius) {
        checkAndCollectAll(new Vector2(player.getX(), player.getY()), interactionRadius);
        updateFogEffect();
    }

    /**
     * Checks if the fog effect is currently active.
     *
     * @return True if the fog effect is active, false otherwise
     */
    public boolean isFogActive() {
        return isFogActive;
    }

    /**
     * Saves the current state of the TrapItem, including the fog effect status.
     */
    public void saveTrapItemState() {
        saveState(Gdx.app.getPreferences(PREFERENCES_NAME), "TrapItem");
    }

    /**
     * Loads the saved state of the TrapItem, restoring the fog effect status.
     */
    public void loadTrapItemState() {
        loadState(Gdx.app.getPreferences(PREFERENCES_NAME), "TrapItem");
    }
}