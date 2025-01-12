package item;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import map.Door;
import render.Renderable;

public class Arrow implements Renderable {
    //Textures for Arrow and Exit
    private Texture arrowTexture;
    private Texture exitTexture;

    //Arrow position and rotation
    private Vector2 position;
    private float rotation;


    //Constructor
    public Arrow() {
        arrowTexture = new Texture("item/arrow.png");
        exitTexture = new Texture("exit.png");
        position = new Vector2();
    }

    //Arrow Rotation and Position Calculation
    public void update(Vector2 playerPosition, Array<Door> doors, boolean hasKey) {
        if (!hasKey) {
            return; // Exit early if the player does not have the key
        }
        // Find the nearest door
        Door nearestDoor = findNearestDoor(playerPosition, doors);
        if (nearestDoor != null) {
            Vector2 doorPosition = nearestDoor.getPosition();

            // Calculate rotation angle towards the nearest door
            float dx = doorPosition.x - playerPosition.x;
            float dy = doorPosition.y - playerPosition.y;
            rotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

            // Set arrow's position slightly above the player
            position.set(playerPosition.x, playerPosition.y + 10);
        }
    }

    // Helper Method: Find the Nearest Door to the Player
    private Door findNearestDoor(Vector2 playerPosition, Array<Door> doors) {
        Door nearest = null;
        float minDistance = Float.MAX_VALUE;

        // Iterate through all doors to find the closest one
        for (Door door : doors) {
            float distance = Vector2.dst(playerPosition.x, playerPosition.y,
                    door.getPosition().x, door.getPosition().y);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = door;
            }
        }
        return nearest; // Return the closest door found
    }


    @Override
    public void render(SpriteBatch batch) {
        batch.draw(exitTexture,
                position.x - exitTexture.getWidth() / 2,
                position.y - exitTexture.getHeight() / 2,
                exitTexture.getWidth() / 2,
                exitTexture.getHeight() / 2,
                exitTexture.getWidth(),
                exitTexture.getHeight(),
                0.25f, 0.25f, 0, //no rotation
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
                0.15f, 0.15f, rotation, //rotation applied
                0, 0,
                arrowTexture.getWidth(),
                arrowTexture.getHeight(),
                false, false);

    }

    //Resource Clean Up
    @Override
    public void dispose() {
        if (arrowTexture != null) {
            arrowTexture.dispose();
        }
        if (exitTexture != null) {
            exitTexture.dispose();
        }
    }

}