package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Represents a navigational arrow that points to the nearest door in the maze.
 * The arrow updates its position and rotation based on the player's position
 * and the nearest door, provided the player has collected the key.
 */
public class Arrow {
    // Constants
    private static final float ARROW_SCALE = 0.15f;
    private static final float EXIT_SCALE = 0.25f;
    private static final float VERTICAL_OFFSET = 10f;

    // Textures
    private final Texture arrowTexture;
    private final Texture exitTexture;

    // Position and rotation
    private final Vector2 position;
    private float rotation;

    /**
     * Constructs an Arrow object with default textures and initializes its position.
     */
    public Arrow() {
        this.arrowTexture = new Texture("arrow.png");
        this.exitTexture = new Texture("exit.png");
        this.position = new Vector2();
    }

    /**
     * Updates the arrow's position and rotation based on player position and nearest door.
     *
     * @param playerPosition The current position of the player
     * @param doors Available doors in the maze
     * @param hasKey Whether the player has collected the key
     */
    public void update(Vector2 playerPosition, Array<Door> doors, boolean hasKey) {
        if (!hasKey) {
            return;
        }

        Door nearestDoor = findNearestDoor(playerPosition, doors);
        if (nearestDoor != null) {
            updateArrowTransform(playerPosition, nearestDoor.getPosition());
        }
    }

    /**
     * Updates the arrow's position and rotation to point towards the target position.
     *
     * @param playerPosition the current position of the player
     * @param targetPosition the position of the target (nearest door)
     */
    private void updateArrowTransform(Vector2 playerPosition, Vector2 targetPosition) {
        float dx = targetPosition.x - playerPosition.x;
        float dy = targetPosition.y - playerPosition.y;

        rotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        position.set(playerPosition.x, playerPosition.y + VERTICAL_OFFSET);
    }

    /**
     * Finds the nearest door to the player's position from the given array of doors.
     *
     * @param playerPosition the current position of the player
     * @param doors          the array of doors available in the maze
     * @return the nearest door to the player, or null if no doors are available
     */
    private Door findNearestDoor(Vector2 playerPosition, Array<Door> doors) {
        Door nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Door door : doors) {
            float distance = calculateDistance(playerPosition, door.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = door;
            }
        }

        return nearest;
    }

    /**
     * Calculates the distance between two points in 2D space.
     *
     * @param point1 the first point
     * @param point2 the second point
     * @return the distance between the two points
     */
    private float calculateDistance(Vector2 point1, Vector2 point2) {
        return Vector2.dst(point1.x, point1.y, point2.x, point2.y);
    }

    /**
     * Renders the arrow and the exit indicator on the screen.
     *
     * @param batch the SpriteBatch used for rendering
     */
    public void render(SpriteBatch batch) {
        renderExitIndicator(batch);
        renderDirectionalArrow(batch);
    }

    /**
     * Renders the exit indicator on the screen.
     *
     * @param batch the SpriteBatch used for rendering
     */
    private void renderExitIndicator(SpriteBatch batch) {
        drawTexture(batch, exitTexture, EXIT_SCALE, 0);
    }

    /**
     * Renders the directional arrow on the screen, pointing towards the nearest door.
     *
     * @param batch the SpriteBatch used for rendering
     */
    private void renderDirectionalArrow(SpriteBatch batch) {
        drawTexture(batch, arrowTexture, ARROW_SCALE, rotation);
    }

    /**
     * Draws a texture at the current arrow position with the given scale and rotation.
     *
     * @param batch    the SpriteBatch used for rendering
     * @param texture  the texture to draw
     * @param scale    the scale of the texture
     * @param rotation the rotation of the texture in degrees
     */
    private void drawTexture(SpriteBatch batch, Texture texture, float scale, float rotation) {
        float width = texture.getWidth();
        float height = texture.getHeight();
        float originX = width / 2;
        float originY = height / 2;

        batch.draw(texture,
                position.x - originX,
                position.y - originY,
                originX,
                originY,
                width,
                height,
                scale, scale,
                rotation,
                0, 0,
                (int) width, (int) height,
                false, false);
    }
    /**
     * Disposes of the textures used by the arrow to free up memory.
     */
    public void dispose() {
        if (arrowTexture != null) {
            arrowTexture.dispose();
        }
        if (exitTexture != null) {
            exitTexture.dispose();
        }
    }
}