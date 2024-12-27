package de.tum.cit.fop.maze;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Door {
    private Vector2 position;
    private Rectangle bounds;
    private static final float INTERACTION_DISTANCE = 10f;

    public Door(float x, float y, float width, float height) {
        position = new Vector2(x, y);
        bounds = new Rectangle(x, y, width, height);
    }
    public boolean isPlayerNear(Vector2 point) {
        return position.dst(point) < INTERACTION_DISTANCE;
    }


}
