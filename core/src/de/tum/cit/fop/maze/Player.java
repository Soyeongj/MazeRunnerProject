package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

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
    private boolean moved = false;
    private float previousX, previousY;

    private final float runDuration = 2f;
    private final float cooldownDuration = 4f;
    private final float walkAnimationTime = 0.1f;

    private boolean isDead;

    private TiledMapTileLayer collisionLayer;
    private String blockedKey = "blocked";

    private Texture redup1,redup2,reddown1,reddown2,redleft1,redleft2,redright1,redright2;
    private float redEffectTimer = 0f;
    private boolean isInRedEffect = false;

    private float speedBoostDuration = 0f;
    private float boostedSpeed = 100.0f;
    private boolean isSpeedBoosted = false;

    private Rectangle bound;

    private boolean isStunned = false;
    private float stunTimer = 0f;


    public Player(float startX, float startY, TiledMapTileLayer collisionLayer) {
        this.x = startX;
        this.y = startY;
        this.speed = 30.0f;
        this.runningSpeed = 70.0f;
        this.previousX = startX;
        this.previousY = startY;
        this.collisionLayer = collisionLayer;

        // Load textures
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

        this.redup2 = new Texture("orc_up_2.png");
        this.redup1 = new Texture("orc_up_1.png");
        this.reddown2 = new Texture("orc_down_2.png");
        this.redleft1 = new Texture("orc_left_1.png");
        this.redleft2 = new Texture("orc_left_2.png");
        this.redright1 = new Texture("orc_right_1.png");
        this.redright2 = new Texture("orc_right_2.png");
        this.reddown1 = new Texture("orc_down_1.png");

        this.bound = new Rectangle();

    }

    public void update(float delta, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean runKeyPressed) {


        previousX = x;
        previousY = y;
        moved = false;

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

        // Movement logic
        if (moveUp && y < 478) {
            y += currentSpeed * delta;
            if (collidesTop()) {
                revertToPrevious();
            } else {
                direction = "up";
                animate(delta, isInRedEffect ? redup1 : up1, isInRedEffect ? redup2 : up2);  // Use red textures if in red effect
                moved = true;
            }
        } else if (moveDown && y > 0) {
            y -= currentSpeed * delta;
            if (collidesBottom()) {
                revertToPrevious();
            } else {
                direction = "down";
                animate(delta, isInRedEffect ? reddown1 : down1, isInRedEffect ? reddown2 : down2);
                moved = true;
            }
        }
        if (moveLeft && x > 0) {
            x -= currentSpeed * delta;
            if (collidesLeft()) {
                revertToPrevious();
            } else {
                direction = "left";
                animate(delta, isInRedEffect ? redleft1 : left1, isInRedEffect ? redleft2 : left2);
                moved = true;
            }
        } else if (moveRight && x < 478.6) {
            x += currentSpeed * delta;
            if (collidesRight()) {
                revertToPrevious();
            } else {
                direction = "right";
                animate(delta, isInRedEffect ? redright1 : right1, isInRedEffect ? redright2 : right2);
                moved = true;
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

        batch.draw(currentTexture, x, y, currentTexture.getWidth() * scale, currentTexture.getHeight() * scale);


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
        redup1.dispose();
        redup2.dispose();
        reddown1.dispose();
        reddown2.dispose();
        redleft1.dispose();
        redleft2.dispose();
        redright1.dispose();
        redright2.dispose();
    }


}