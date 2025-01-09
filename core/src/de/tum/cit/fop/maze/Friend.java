package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Friend {
    private static final String PREFERENCES_NAME = "FriendState";
    private TiledMap map;
    private Texture[] textures;
    private Vector2[] positions;

    public Friend(TiledMap map) {
        this.map = map;
        this.textures = generateTextures(map, "friend");
        this.positions = loadFriendPositions(map);
    }

    private static Texture[] generateTextures(TiledMap map, String layerName) {
        MapObjects objects = map.getLayers().get(layerName).getObjects();
        int friendCount = objects.getCount();

        Texture[] textures = new Texture[friendCount];
        for (int i = 0; i < friendCount; i++) {
            textures[i] = new Texture("oldman_right_1.png");
        }

        return textures;
    }

    private static Vector2[] loadFriendPositions(TiledMap map) {
        Array<Vector2> positions = new Array<>();
        MapLayer friendsLayer = map.getLayers().get("friend");

        MapObjects objects = friendsLayer.getObjects();
        for (MapObject object : objects) {
            float x = Float.parseFloat(object.getProperties().get("x").toString());
            float y = Float.parseFloat(object.getProperties().get("y").toString());

            Object friendProperty = object.getProperties().get("friend");
            if (friendProperty != null && "1".equals(friendProperty.toString())) {
                positions.add(new Vector2(x, y));
            }
        }

        return positions.toArray(Vector2.class);
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < positions.length; i++) {
            Vector2 position = positions[i];
            batch.draw(textures[i], position.x, position.y);
        }
    }

    public void update(Player player, float interactionRadius, HUD hud) {
        for (Vector2 position : positions) {
            if (position.dst(player.getX(), player.getY()) <= interactionRadius) {
                hud.incrementLives();
            }
        }
    }





}
