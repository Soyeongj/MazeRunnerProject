package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Item extends CollectibleItem {
    private static final String PREFERENCES_NAME = "ItemState";

    public Item(TiledMap map) {
        super(generateTextures(map,"item"),map,"item"); // 타일맵에서 좌표 읽기
    }

    private static Texture[] generateTextures(TiledMap map, String layerName) {
        // 타일맵에서 "item" 레이어의 객체 수만큼 텍스처를 생성
        MapObjects objects = map.getLayers().get(layerName).getObjects();
        int itemCount = objects.getCount(); // 아이템 좌표 수

        Texture[] textures = new Texture[itemCount];
        for (int i = 0; i < itemCount; i++) {
            textures[i] = new Texture("potion_red.png"); // 동일한 텍스처를 사용
        }

        return textures;
    }

    private static Vector2[] loadItemPositions(TiledMap map) {
        // 'item' 속성을 가진 객체들의 위치를 읽어와 반환
        Array<Vector2> positions = new Array<>();

        MapLayer itemsLayer = map.getLayers().get("item");

        MapObjects objects = itemsLayer.getObjects();
        for (MapObject object : objects) {
            float x = Float.parseFloat(object.getProperties().get("x").toString());
            float y = Float.parseFloat(object.getProperties().get("y").toString());

            // item 속성 확인 및 추가
            Object itemProperty = object.getProperties().get("item");
            if (itemProperty != null && "1".equals(itemProperty.toString())) {
                positions.add(new Vector2(x, y));
            }
        }

        return positions.toArray(Vector2.class);
    }

    @Override
    protected void onCollected() {
        SoundManager.playItemCollectedSound();
    }
    public void update(Player player, float interactionRadius) {
        int collectedItems = checkAndCollectAll(new Vector2(player.getX(), player.getY()), interactionRadius);
        for (int i = 0; i < collectedItems; i++) {
            player.increaseSpeed(3f);
        }
    }

    public void saveItemState() {
        saveState(Gdx.app.getPreferences(PREFERENCES_NAME), "Item");
    }

    public void loadItemState() {
        loadState(Gdx.app.getPreferences(PREFERENCES_NAME), "Item");
    }
}
