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


public abstract class CollectibleItem implements Renderable {
    protected Texture[] textures;
    protected Vector2[] positions;
    protected boolean[] isCollected;
    protected float scale = 0.2f;

    protected CollectibleItem(Texture[] textures, TiledMap map, String layerName) {
        this.textures = textures;
        this.positions = loadItemPositions(map, layerName);

        // 아이템 수집 상태 초기화
        this.isCollected = new boolean[positions.length];
    }

    private static Vector2[] loadItemPositions(TiledMap map, String layerName) {
        Array<Vector2> positions = new Array<>();
        MapObjects objects = map.getLayers().get(layerName).getObjects();

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                positions.add(new Vector2(rect.x, rect.y)); // 아이템 좌표 추가
            } else if (object.getProperties().containsKey("x") && object.getProperties().containsKey("y")) {
                float x = object.getProperties().get("x", Float.class);
                float y = object.getProperties().get("y", Float.class);
                positions.add(new Vector2(x, y));
            }
        }
        return positions.toArray(Vector2.class);
    }

    @Override
    public void render(SpriteBatch batch) {
        for (int i = 0; i < Math.min(textures.length, positions.length); i++) {
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
