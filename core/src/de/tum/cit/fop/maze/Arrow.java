package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Preferences;

public class Arrow {
    private Texture arrowTexture;
    private Texture exitTexture;
    private Vector2 position;
    private float rotation;

    public Arrow() {
        arrowTexture = new Texture("arrow.png");
        exitTexture = new Texture("exit.png");
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
        batch.draw(exitTexture,
                position.x - exitTexture.getWidth() / 2,
                position.y - exitTexture.getHeight() / 2,
                exitTexture.getWidth() / 2,
                exitTexture.getHeight() / 2,
                exitTexture.getWidth(),
                exitTexture.getHeight(),
                0.25f, 0.25f, 0, // exit는 회전하지 않음
                0, 0,
                exitTexture.getWidth(),
                exitTexture.getHeight(),
                false, false);

        batch.draw(arrowTexture,
                position.x - arrowTexture.getWidth()/2,
                position.y - arrowTexture.getHeight()/2,
                arrowTexture.getWidth()/2,
                arrowTexture.getHeight()/2,
                arrowTexture.getWidth(),
                arrowTexture.getHeight(),
                0.15f, 0.15f, rotation,
                0, 0,
                arrowTexture.getWidth(),
                arrowTexture.getHeight(),
                false, false);

    }

    public void dispose() {
        if (arrowTexture != null) {
            arrowTexture.dispose();
        }
        if (exitTexture != null) {
            exitTexture.dispose();
        }
    }

    public void saveArrowState() {
        Preferences prefs = Gdx.app.getPreferences("arrow");
        prefs.putFloat("rotation", rotation);
        prefs.flush();
    }

    public void loadArrowState() {
        Preferences prefs = Gdx.app.getPreferences("arrow");
        rotation = prefs.getFloat("rotation", rotation);
    }
}