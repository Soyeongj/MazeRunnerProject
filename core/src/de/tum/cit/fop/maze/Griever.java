package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class Griever {
    private Texture grieverUp1, grieverUp2, grieverDown1, grieverDown2, grieverLeft1, grieverLeft2, grieverRight1, grieverRight2;
    private Texture griever;
    private float grieverStateTime;
    private String fixedGrieverDirection;
    private float monsterX, monsterY;
    private float monsterSpeed = 10.0f;
    private float detectionRange = 37.0f;
    private boolean isGrieverFollowingPlayer = false;
    private final float grieverAnimationTime = 0.1f; // Time between animation frames
    private Rectangle grieverRectangle;
    private float scale = 0.2f;
    private float previousX, previousY;

    private boolean isGrieverStunned = false;
    private float stunTimer = 0.0f;
    private final float stunDuration = 3.0f; // 3 seconds stun duration

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
        grieverRectangle = new Rectangle(monsterX, monsterY, griever.getWidth(), griever.getHeight());
    }

    public void update(float delta, float playerX, float playerY, String playerDirection) {
        grieverRectangle.setSize(griever.getWidth() * scale, griever.getHeight() * scale);

        int diffX = (int) (playerX - monsterX);
        int diffY = (int) (playerY - monsterY);
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

        if (isGrieverStunned) {
            stunTimer += delta;
            if (stunTimer >= stunDuration) {
                isGrieverStunned = false;
                stunTimer = 0;
            }
            return; // Skip updates while stunned
        }

        if (distance <= detectionRange) {
            isGrieverFollowingPlayer = true;
        }

        if (isGrieverFollowingPlayer) {
            previousX = monsterX;
            previousY = monsterY;

            Vector2 grieverPosition = new Vector2(monsterX, monsterY);
            Vector2 playerPosition = new Vector2(playerX, playerY);
            Vector2 direction = playerPosition.sub(grieverPosition).nor();
            float deltaX = direction.x * monsterSpeed * delta;
            float deltaY = direction.y * monsterSpeed * delta;

            if (Math.abs(direction.x) > Math.abs(direction.y)) {
                fixedGrieverDirection = direction.x > 0 ? "right" : "left";
            } else {
                fixedGrieverDirection = direction.y > 0 ? "up" : "down";
            }

            monsterX += deltaX;
            if (collidesHorizontal()) revertToPrevious();

            monsterY += deltaY;
            if (collidesVertical()) revertToPrevious();

            grieverRectangle.setPosition(monsterX, monsterY);

            grieverStateTime += delta;
            if (grieverStateTime >= grieverAnimationTime) {
                griever = getGrieverTextureForDirection(fixedGrieverDirection);
                grieverStateTime = 0;
            }
        }

        checkStunCondition(playerX, playerY, playerDirection);
    }

    private void checkStunCondition(float playerX, float playerY, String playerDirection) {
        float distance = (float) Math.sqrt(Math.pow(playerX - monsterX, 2) + Math.pow(playerY - monsterY, 2));
        if (distance <= 5f && !isGrieverStunned) {
            boolean isOppositeDirection = isGrieverInOppositeDirection(playerDirection);
            if (isOppositeDirection) {
                isGrieverStunned = true;
                stunTimer = 0;
            }
        }
    }

    public boolean isGrieverInOppositeDirection(String playerDirection) {
        return (fixedGrieverDirection.equals("left") && playerDirection.equals("right")) ||
                (fixedGrieverDirection.equals("right") && playerDirection.equals("left")) ||
                (fixedGrieverDirection.equals("up") && playerDirection.equals("down")) ||
                (fixedGrieverDirection.equals("down") && playerDirection.equals("up"));
    }

    private boolean collidesHorizontal() {
        float step = collisionLayer.getTileHeight() / 2;
        for (float offset = 0; offset < grieverRectangle.height; offset += step) {
            if (isCellBlocked(monsterX + grieverRectangle.width, monsterY + offset) || // Right edge
                    isCellBlocked(monsterX, monsterY + offset)) { // Left edge
                return true;
            }
        }
        return false;
    }

    private boolean collidesVertical() {
        float step = collisionLayer.getTileWidth() / 2;
        for (float offset = 0; offset < grieverRectangle.width; offset += step) {
            if (isCellBlocked(monsterX + offset, monsterY + grieverRectangle.height) || // Top edge
                    isCellBlocked(monsterX + offset, monsterY)) { // Bottom edge
                return true;
            }
        }
        return false;
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

    public void render(SpriteBatch batch) {
        batch.draw(griever, monsterX, monsterY, griever.getWidth() * scale, griever.getHeight() * scale);
    }

    public void revertToPrevious() {
        monsterX = previousX;
        monsterY = previousY;
        grieverRectangle.setPosition(monsterX, monsterY);
    }

    public float getMonsterX() {
        return monsterX;
    }
    public float getMonsterY() {
        return monsterY;
    }
    public boolean isGrieverNotStunned() {
        return !isGrieverStunned;
    }

    public void killGriever() {
        monsterX = -1000;
        monsterY = -1000;
        grieverRectangle.setPosition(monsterX, monsterY);
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

    public void setPosition(int i, int i1) {
        monsterX = i;
        monsterY = i1;
    }

    public float getWidth() {
        return griever.getWidth(); // 텍스처의 실제 너비 반환
    }

    // Griever 텍스처의 높이 반환
    public float getHeight() {
        return griever.getHeight(); // 텍스처의 실제 높이 반환
    }

    // Griever의 스케일 반환
    public float getScale() {
        return scale;
    }
}
