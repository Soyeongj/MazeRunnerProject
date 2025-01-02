package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Arrow {
    private Texture arrowTexture;
    private Vector2 position;
    private float rotation;

    public Arrow() {
        arrowTexture = new Texture("arrow.png"); // Create your arrow texture
        position = new Vector2();
    }

    public void update(Vector2 playerPosition, Array<Door> doors,boolean hasKey) {
        if (!hasKey) {
            return;
        }
        Door nearestDoor = findNearestDoor(playerPosition, doors);
        if (nearestDoor != null) {
            Vector2 doorPosition = nearestDoor.getPosition();

            float dx = doorPosition.x - playerPosition.x;
            float dy = doorPosition.y - playerPosition.y;
            rotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

            position.set(playerPosition.x, playerPosition.y + 10);
        }
    }

    private Door findNearestDoor(Vector2 playerPosition, Array<Door> doors) {
        Door nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Door door : doors) {
            float distance = Vector2.dst(playerPosition.x, playerPosition.y,
                    door.getPosition().x, door.getPosition().y);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = door;
            }
        }
        return nearest;
    }

    public void render(SpriteBatch batch) {
        batch.draw(arrowTexture,
                position.x - arrowTexture.getWidth()/2,
                position.y - arrowTexture.getHeight()/2,
                arrowTexture.getWidth()/2,
                arrowTexture.getHeight()/2,
                arrowTexture.getWidth(),
                arrowTexture.getHeight(),
                0.03f, 0.023f, rotation,
                0, 0,
                arrowTexture.getWidth(),
                arrowTexture.getHeight(),
                false, false);
    }

    public void dispose() {
        if (arrowTexture != null) {
            arrowTexture.dispose();
        }
    }
}