package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;


/**
 * The Player class represents the player character in the game, including its movement, animation, collision detection,
 * state management (e.g., speed boosts), and rendering. It handles user input for player movement, running,
 * speed boosting, and special effects like red visual effects and death.
 */
public class Player {

    //Textures
    private Texture currentTexture;
    private Texture up1, up2, down1, down2, left1, left2, right1, right2, dead;

    //Positions and Movements
    private float x, y;
    private float previousX, previousY;
    private float startX, startY;

    //Speed and Running
    private float speed, runningSpeed, normalSpeed;
    private float runTimer = 0f, cooldownTimer = 0f;
    private boolean isRunning = false, canRun = true;
    private final float runDuration = 2f;
    private final float cooldownDuration = 4f;

    //Animation
    private float stateTime = 0f;
    private String direction = "right";
    private final float walkAnimationTime = 0.1f;

    //Collision
    private TiledMapTileLayer collisionLayer;
    private String blockedKey = "blocked";

    //Speed Up Effect Controls
    private float speedBoostDuration = 0f;
    private float boostedSpeed = 100.0f;
    private boolean isSpeedBoosted = false;

    //VFX Controls
    private float redEffectTimer = 0f;
    private boolean isInRedEffect = false;

    //Player states
    private float scale = 0.2f;
    private boolean isDead;
    private static final String PREFERENCES_NAME = "PlayerState";

    /**
     * Constructs a new Player with the specified collision layer.
     * Initializes the player's textures and sets the initial state.
     *
     * @param collisionLayer The collision layer used to detect collisions with the environment.
     */
    public Player(TiledMapTileLayer collisionLayer) {
        this.speed = 30.0f;
        this.runningSpeed = 70.0f;
        this.normalSpeed = speed;
        this.collisionLayer = collisionLayer;

        this.up1 = new Texture("boy_up1.png");
        this.up2 = new Texture("boy_up2.png");
        this.down1 = new Texture("boy_down1.png");
        this.down2 = new Texture("boy_down2.png");
        this.left1 = new Texture("boy_left1.png");
        this.left2 = new Texture("boy_left2.png");
        this.right1 = new Texture("boy_right1.png");
        this.right2 = new Texture("boy_right2.png");
        this.dead = new Texture("boydead.png");

        this.currentTexture = down1;
        this.isDead = false;

    }

    /**
     * Loads the player's initial state from the TiledMap by reading its position from the "player" layer.
     *
     * @param map The TiledMap containing the player’s initial position.
     * @param collisionLayer The collision layer for detecting collisions.
     * @return The initialized Player object.
     */
    public static Player loadPlayerFromTiledMap(TiledMap map, TiledMapTileLayer collisionLayer) {
        MapLayer playerLayer = map.getLayers().get("player");
        Player player = new Player(collisionLayer);

        MapObjects objects = playerLayer.getObjects();
        for (MapObject object : objects) {
            Object playerProperty = object.getProperties().get("player");
            if (playerProperty != null && "1".equals(playerProperty.toString())) {
                float startX = Float.parseFloat(object.getProperties().get("x").toString());
                float startY = Float.parseFloat(object.getProperties().get("y").toString());

                player.setX(startX);
                player.setY(startY);
                player.setStartX(startX);
                player.setStartY(startY);

            }
        }
        return player;
    }

    /**
     * Updates the player's state each frame, including movement, running, and effects.
     * Handles player input for movement and manages the player’s speed and animations.
     *
     * @param delta The delta time between frames.
     * @param moveUp Whether the player is moving up.
     * @param moveDown Whether the player is moving down.
     * @param moveLeft Whether the player is moving left.
     * @param moveRight Whether the player is moving right.
     * @param runKeyPressed Whether the player is pressing the run key.
     * @param friends The Friends object to manage following friends.
     */

    public void update(float delta, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean runKeyPressed, Friends friends) {
        previousX = x;
        previousY = y;

        float slowdownFactor = 1 - 0.05f * friends.getFollowingFriendsPositions().size();

        if (isSpeedBoosted) {
            speedBoostDuration -= delta;
            speed = boostedSpeed;
            if (speedBoostDuration <= 0) {
                resetSpeedBoost();
            }
        }

        if (isInRedEffect) {
            redEffectTimer += delta;
            if (redEffectTimer >= 3f) {
                isInRedEffect = false;
                redEffectTimer = 0f;
            }
        }

        if (canRun && runKeyPressed) {
            isRunning = true;
        } else {
            isRunning = false;
        }

        if (isRunning) {
            runTimer += delta;
            if (runTimer >= runDuration) {
                canRun = false;
                runTimer = runDuration;
            }
        } else if (!canRun) {
            cooldownTimer += delta;
            if (cooldownTimer >= cooldownDuration) {
                canRun = true;
                cooldownTimer = 0;
                runTimer = 0;
            }
        }

        float currentSpeed = speed * slowdownFactor;

        if (isRunning) {
            currentSpeed = runningSpeed * slowdownFactor;
        }


        if (moveUp && y < 478) {
            y += currentSpeed * delta;
            if (collidesTop()) {
                revertToPrevious();
            } else {
                direction = "up";
                animate(delta, up1, up2);
            }
        } else if (moveDown && y > 0) {
            y -= currentSpeed * delta;
            if (collidesBottom()) {
                revertToPrevious();
            } else {
                direction = "down";
                animate(delta, down1, down2);
            }
        }
        if (moveLeft && x > 0) {
            x -= currentSpeed * delta;
            if (collidesLeft()) {
                revertToPrevious();
            } else {
                direction = "left";
                animate(delta, left1, left2);
            }
        } else if (moveRight && x < 478.6) {
            x += currentSpeed * delta;
            if (collidesRight()) {
                revertToPrevious();
            } else {
                direction = "right";
                animate(delta, right1, right2);
            }
        }
    }

    /**
     * Reverts the player's position to the previous valid position after a collision.
     */
    public void revertToPrevious() {
        x = previousX;
        y = previousY;
    }

    /**
     * Checks if the player is colliding with a blocked cell at the specified coordinates.
     *
     * @param x The x-coordinate to check.
     * @param y The y-coordinate to check.
     * @return True if the cell is blocked, false otherwise.
     */
    private boolean isCellBlocked(float x, float y) {
        TiledMapTileLayer.Cell cell = collisionLayer.getCell(
                (int) (x / collisionLayer.getTileWidth()),
                (int) (y / collisionLayer.getTileHeight())
        );
        return cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey(blockedKey);
    }

    /**
     * Checks if the player is colliding with the right side.
     *
     * @return True if there is a collision on the right side, false otherwise.
     */
    private boolean collidesRight() {
        for (float step = 0; step < 1; step += collisionLayer.getTileHeight() / 2) {
            if (isCellBlocked(x + currentTexture.getWidth() * scale, y + step)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the player is colliding with the left side.
     *
     * @return True if there is a collision on the left side, false otherwise.
     */
    private boolean collidesLeft() {
        for (float step = 0; step < 1; step += collisionLayer.getTileHeight() / 2) {
            if (isCellBlocked(x, y + step)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the player is colliding with the top side.
     *
     * @return True if there is a collision on the top side, false otherwise.
     */
    private boolean collidesTop() {
        for (float step = 0; step < 1; step += collisionLayer.getTileWidth() / 2) {
            if (isCellBlocked(x + step, y + currentTexture.getHeight() * scale)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the player is colliding with the bottom side.
     *
     * @return True if there is a collision on the bottom side, false otherwise.
     */
    private boolean collidesBottom() {
        for (float step = 0; step < 1; step += collisionLayer.getTileWidth() / 2) {
            if (isCellBlocked(x + step, y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Animates the player's movement by alternating between two textures.
     *
     * @param delta The delta time between frames.
     * @param texture1 The first texture to alternate to.
     * @param texture2 The second texture to alternate to.
     */
    private void animate(float delta, Texture texture1, Texture texture2) {
        stateTime += delta;
        if (stateTime >= walkAnimationTime) {
            currentTexture = (currentTexture == texture1) ? texture2 : texture1;
            stateTime = 0f;
        }
    }

    /**
     * Renders the player on the screen.
     * If the player is in a red effect, applies a visual pulse effect.
     *
     * @param batch The SpriteBatch used to render the player.
     */
    public void render(SpriteBatch batch) {
        if (isInRedEffect) {
            float pulse = 0.5f + 0.5f * MathUtils.sin(redEffectTimer * 5);
            batch.setColor(1, 1 - pulse, 1 - pulse, 1);
            batch.draw(currentTexture, x, y, currentTexture.getWidth() * scale, currentTexture.getHeight() * scale);
            batch.setColor(1, 1, 1, 1);
        } else {
            batch.draw(currentTexture, x, y, currentTexture.getWidth() * scale, currentTexture.getHeight() * scale);
        }
    }

    /**
     * Increases the player's speed for a given duration.
     *
     * @param duration The duration for the speed boost in seconds.
     */
    public void increaseSpeed(float duration) {
        isSpeedBoosted = true;
        speedBoostDuration = duration;
    }

    /**
     * Resets the player's speed to normal after the speed boost duration expires.
     */
    public void resetSpeedBoost() {
        isSpeedBoosted = false;
        speed = normalSpeed;
    }

    /**
     * Saves the player's current state (position, speed, effect timers, etc.) to persistent storage.
     */
    public void savePlayerState() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        preferences.putFloat("x", x);
        preferences.putFloat("y", y);
        preferences.putFloat("speed", speed);
        preferences.putBoolean("isDead", isDead);
        preferences.putString("direction", direction);
        preferences.putBoolean("isSpeedBoosted", isSpeedBoosted);
        preferences.putFloat("speedBoostDuration", speedBoostDuration);
        preferences.putBoolean("isInRedEffect", isInRedEffect);
        preferences.putFloat("redEffectTimer", redEffectTimer);
        preferences.putBoolean("isRunning", isRunning);
        preferences.putBoolean("canRun", canRun);
        preferences.putFloat("runTimer", runTimer);
        preferences.putFloat("cooldownTimer", cooldownTimer);
        preferences.flush();
    }


    /**
     * Loads the player's state from persistent storage and restores it.
     */
    public void loadPlayerState() {
        Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        x = preferences.getFloat("x", x);
        y = preferences.getFloat("y", y);
        speed = preferences.getFloat("speed", speed);
        isDead = preferences.getBoolean("isDead", isDead);
        direction = preferences.getString("direction", direction);
        isSpeedBoosted = preferences.getBoolean("isSpeedBoosted", isSpeedBoosted);
        speedBoostDuration = preferences.getFloat("speedBoostDuration", speedBoostDuration);
        isInRedEffect = preferences.getBoolean("isInRedEffect", isInRedEffect);
        redEffectTimer = preferences.getFloat("redEffectTimer", redEffectTimer);
        isRunning = preferences.getBoolean("isRunning", isRunning);
        canRun = preferences.getBoolean("canRun", canRun);
        runTimer = preferences.getFloat("runTimer", runTimer);
        cooldownTimer = preferences.getFloat("cooldownTimer", cooldownTimer);
    }

    // Getter and Setter methods for player position and state
    public void setStartX(float startX) {
        this.startX = startX;
    }
    public void setStartY(float startY) {
        this.startY = startY;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
    public float getWidth() {
        return currentTexture.getWidth();
    }
    public float getHeight() {
        return currentTexture.getHeight();
    }
    public float getScale() {
        return scale;
    }
    public String getDirection() {
        return direction;
    }
    public void setTexture(Texture texture) {
        this.currentTexture = texture;
    }
    public void setDead() {
        this.isDead = true;
        setTexture(dead);
    }
    public void triggerRedEffect() {
        isInRedEffect = true;
        redEffectTimer = 0f;
    }

    /**
     * Disposes of the player’s resources (textures) to free up memory when the player is no longer needed.
     */
    public void dispose() {
        up1.dispose();
        up2.dispose();
        down1.dispose();
        down2.dispose();
        left1.dispose();
        left2.dispose();
        right1.dispose();
        right2.dispose();
        dead.dispose();
    }
}