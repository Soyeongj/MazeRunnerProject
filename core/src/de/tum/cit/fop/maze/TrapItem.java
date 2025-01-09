package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;


public class TrapItem extends CollectibleItem {
    private long fogEffectStartTime = 0;
    private boolean isFogActive = false;
    private static final long FOG_DURATION = 3000;

    public TrapItem(TiledMap map) {
        // 타일맵의 객체 수에 따라 텍스처를 동적으로 생성
        super(generateTextures(map, "trapitem"), map, "trapitem");
    }

    private static Texture[] generateTextures(TiledMap map, String layerName) {
        MapObjects objects = map.getLayers().get(layerName).getObjects();
        int objectCount = objects.getCount();

        // 객체 수만큼 텍스처 배열 생성
        Texture[] textures = new Texture[objectCount];
        Texture defaultTexture = new Texture("key.png"); // 트랩에 사용할 텍스처
        for (int i = 0; i < objectCount; i++) {
            textures[i] = defaultTexture; // 모든 객체에 동일한 텍스처 사용
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
