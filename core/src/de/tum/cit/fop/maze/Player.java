package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Player {
    private Texture currentTexture;
    private Texture up1, up2, down1, down2, left1, left2, right1, right2, dead;
    private float x, y;
    private float speed, runningSpeed;
    private float runTimer = 0f, cooldownTimer = 0f;
    private boolean isRunning = false, canRun = true;
    private float stateTime = 0f;
    private String direction = "right";
    private float scale = 0.2f;
    private float previousX, previousY;

    private final float runDuration = 2f;
    private final float cooldownDuration = 4f;
    private final float walkAnimationTime = 0.1f;

    private boolean isDead;

    private TiledMapTileLayer collisionLayer;
    private String blockedKey = "blocked";

    private float redEffectTimer = 0f;
    private boolean isInRedEffect = false;

    private float speedBoostDuration = 0f;
    private float boostedSpeed = 100.0f;
    private boolean isSpeedBoosted = false;

    private Rectangle bound;

    private static final String PREFERENCES_NAME = "PlayerState";


    public Player(float startX, float startY, TiledMapTileLayer collisionLayer) {
        this.x = startX;
        this.y = startY;
        this.speed = 30.0f;
        this.runningSpeed = 70.0f;
        this.previousX = startX;
        this.previousY = startY;
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

        this.currentTexture = right1;
        this.isDead = false;

        this.bound = new Rectangle();
    }

    public void update(float delta, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean runKeyPressed) {
        previousX = x;
        previousY = y;

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

        float currentSpeed = isRunning ? runningSpeed : speed;

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

    public void revertToPrevious() {
        x = previousX;
        y = previousY;
    }

    private boolean isCellBlocked(float x, float y) {
        TiledMapTileLayer.Cell cell = collisionLayer.getCell(
                (int) (x / collisionLayer.getTileWidth()),
                (int) (y / collisionLayer.getTileHeight())
        );
        return cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey(blockedKey);
    }

    private boolean collidesRight() {
        for (float step = 0; step < 1; step += collisionLayer.getTileHeight() / 2) {
            if (isCellBlocked(x + currentTexture.getWidth() * scale, y + step)) {
                return true;
            }
        }
        return false;
    }

    private boolean collidesLeft() {
        for (float step = 0; step < 1; step += collisionLayer.getTileHeight() / 2) {
            if (isCellBlocked(x, y + step)) {
                return true;
            }
        }
        return false;
    }

    private boolean collidesTop() {
        for (float step = 0; step < 1; step += collisionLayer.getTileWidth() / 2) {
            if (isCellBlocked(x + step, y + currentTexture.getHeight() * scale)) {
                return true;
            }
        }
        return false;
    }

    private boolean collidesBottom() {
        for (float step = 0; step < 1; step += collisionLayer.getTileWidth() / 2) {
            if (isCellBlocked(x + step, y)) {
                return true;
            }
        }
        return false;
    }

    private void animate(float delta, Texture texture1, Texture texture2) {
        stateTime += delta;
        if (stateTime >= walkAnimationTime) {
            currentTexture = (currentTexture == texture1) ? texture2 : texture1;
            stateTime = 0f;
        }
    }

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

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isDead() {
        return isDead;
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

    public void increaseSpeed(float duration) {
        isSpeedBoosted = true;
        speedBoostDuration = duration;
    }

    public void resetSpeedBoost() {
        isSpeedBoosted = false;
        speed = 10f;
    }

    public Rectangle getBound() {
        return bound;
    }

    public void setBound(Rectangle bound) {
        this.bound = bound;
    }

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
}
