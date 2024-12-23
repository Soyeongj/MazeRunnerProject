package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

public class Griever {
    private Texture grieverUp1, grieverUp2, grieverDown1, grieverDown2, grieverLeft1, grieverLeft2, grieverRight1, grieverRight2;
    private Texture griever;
    private float grieverStateTime;
    private String fixedGrieverDirection;
    private float monsterX, monsterY;
    private float monsterSpeed = 10.0f;
    private float detectionRange = 37.0f;
    private boolean isGrieverwaiting = false;
    private boolean isGrieverFollowingPlayer = false;
    private float grieverChaseDelayTimer = 0.0f;
    private final float grieverAnimationTime = 0.1f;  // Time between animation frames
    private float scale = 1.0f;
    private float previousX, previousY;

    private boolean isGrieverStunned = false;
    private float stunTimer = 0.0f;

    private TiledMapTileLayer collisionLayer;
    private String blockedKey = "blocked";

    public Griever(float startX, float startY, TiledMapTileLayer collisionLayer) {
        this.monsterX = startX;
        this.monsterY = startY;
        this.previousX = startX;
        this.previousY = startY;
        this.collisionLayer = collisionLayer;

        // Load griever textures
        grieverRight1 = new Texture("grieverright.png");
        grieverRight2 = new Texture("grieverright2.png");
        grieverLeft1 = new Texture("grieverleft.png");
        grieverLeft2 = new Texture("grieverleft2.png");
        grieverUp1 = new Texture("grieverup.png");
        grieverUp2 = new Texture("grieverup2.png");
        grieverDown1 = new Texture("grieverdown.png");
        grieverDown2 = new Texture("grieverdown2.png");

        griever = grieverRight1;
        fixedGrieverDirection = "right";
        grieverStateTime = 0f;
    }

    public void render(SpriteBatch batch) {
        batch.draw(griever, monsterX, monsterY, griever.getWidth() * scale, griever.getHeight() * scale);
    }

    public void update(float delta, float playerX, float playerY, String playerDirection) {
        int diffX = (int) (playerX - monsterX);
        int diffY = (int) (playerY - monsterY);
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

        if (distance <= detectionRange) {
            isGrieverwaiting = true;
        }

        if (isGrieverwaiting) {
            grieverChaseDelayTimer += delta;
            if (grieverChaseDelayTimer > 1.5f) {
                isGrieverFollowingPlayer = true;
            }
        }

        if (isGrieverStunned) {
            stunTimer += delta;
            if (stunTimer >= 3f) {
                isGrieverStunned = false;
                stunTimer = 0;
            }
        } else if (isGrieverFollowingPlayer) {
            previousX = monsterX;
            previousY = monsterY;

            Vector2 grieverPosition = new Vector2(monsterX, monsterY);
            Vector2 playerPosition = new Vector2(playerX, playerY);
            Vector2 direction = new Vector2(playerPosition).sub(grieverPosition).nor();

            float deltaX = direction.x * monsterSpeed * delta;
            float deltaY = direction.y * monsterSpeed * delta;

            // collision detection
            if (!isCellBlocked(monsterX + deltaX, monsterY)) {
                monsterX += deltaX;
            }
            if (!isCellBlocked(monsterX, monsterY + deltaY)) {
                monsterY += deltaY;
            }

            grieverStateTime += delta;
            if (grieverStateTime >= grieverAnimationTime) {
                griever = getGrieverTextureForDirection(fixedGrieverDirection);
                grieverStateTime = 0;
            }
        }
    }

    private boolean isCellBlocked(float x, float y) {
        TiledMapTileLayer.Cell cell = collisionLayer.getCell(
                (int) (x / collisionLayer.getTileWidth()),
                (int) (y / collisionLayer.getTileHeight())
        );
        return cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey(blockedKey);
    }

    private Texture getGrieverTextureForDirection(String direction) {
        switch (direction) {
            case "right":
                return (griever == grieverRight1) ? grieverRight2 : grieverRight1;
            case "left":
                return (griever == grieverLeft1) ? grieverLeft2 : grieverLeft1;
            case "up":
                return (griever == grieverUp1) ? grieverUp2 : grieverUp1;
            case "down":
                return (griever == grieverDown1) ? grieverDown2 : grieverDown1;
            default:
                return grieverRight1;
        }
    }

    public float getX() {
        return monsterX;
    }


    public float getY() {
        return monsterY;
    }


    public boolean isGrieverStunned() {
        return isGrieverStunned;
    }

    public boolean isGrieverNotStunned() {
        return !isGrieverStunned;
    }

    public void setMonsterX(float monsterX) {
        this.monsterX = monsterX;
    }

    public void setMonsterY(float monsterY) {
        this.monsterY = monsterY;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void revertToPrevious() {
        monsterX = previousX;
        monsterY = previousY;
    }

    public void dispose() {
        grieverRight1.dispose();
        grieverRight2.dispose();
        grieverLeft1.dispose();
        grieverLeft2.dispose();
        grieverUp1.dispose();
        grieverUp2.dispose();
        grieverDown1.dispose();
        grieverDown2.dispose();
    }
}
