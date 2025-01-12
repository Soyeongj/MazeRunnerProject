package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;


public class TrapItem extends CollectibleItem {
    private long fogEffectStartTime = 0;
    private boolean isFogActive = false;
    private static final long FOG_DURATION = 3000; // Duration for how long the fog effect lasts

    public TrapItem(TiledMap map) {
        super(generateTextures(map, "trapitem"), map, "trapitem");
    }

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


    @Override
    protected void onCollected() {
        SoundManager.playEvilLaughSound();
        activateFogEffect();
    }
    public void activateFogEffect() {
        isFogActive = true;
        fogEffectStartTime = TimeUtils.millis();
    }
    public void updateFogEffect() {
        if (isFogActive) {
            long currentTime = TimeUtils.millis();
            if (currentTime - fogEffectStartTime >= FOG_DURATION) {
                isFogActive = false;
            }
        }
    }
    public void update(Player player, float interactionRadius) {
        checkAndCollectAll(new Vector2(player.getX(), player.getY()), interactionRadius);
        updateFogEffect();
    }

    public boolean isFogActive() {
        return isFogActive;
    }

    public void saveTrapItemState() {
        saveState(Gdx.app.getPreferences("TrapItem"), "TrapItem");
    }

    public void loadTrapItemState() {
        loadState(Gdx.app.getPreferences("TrapItem"), "TrapItem");
    }



}