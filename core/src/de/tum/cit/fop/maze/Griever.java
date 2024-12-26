package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Griever {
    private Map<String, Texture[]> grieverTextures;
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

    private Vector2 randomDirection; // To store the current random direction
    private float randomMovementTimer = 0f; // Timer for random movement
    private final float randomMovementInterval = 8.0f; // Change direction every 8 seconds
    private Random random = new Random(); // Random object for generating directions

    private TiledMapTileLayer collisionLayer;
    private String blockedKey = "blocked";
    public Griever(float startX, float startY, TiledMapTileLayer collisionLayer) {
        this.monsterX = startX;
        this.monsterY = startY;
        this.previousX = startX;
        this.previousY = startY;
        this.collisionLayer = collisionLayer;

        // Load griever textures
        // 초기화 시 맵에 텍스처 배열 저장
        grieverTextures = new HashMap<>();
        grieverTextures.put("up", new Texture[]{new Texture("grieverup.png"), new Texture("grieverup2.png")});
        grieverTextures.put("down", new Texture[]{new Texture("grieverdown.png"), new Texture("grieverdown2.png")});
        grieverTextures.put("left", new Texture[]{new Texture("grieverleft.png"), new Texture("grieverleft2.png")});
        grieverTextures.put("right", new Texture[]{new Texture("grieverright.png"), new Texture("grieverright2.png")});

        fixedGrieverDirection = "right";
        griever = grieverTextures.get(fixedGrieverDirection)[0];
        grieverStateTime = 0f;
        grieverRectangle = new Rectangle(monsterX, monsterY, griever.getWidth(), griever.getHeight());
        randomDirection = getRandomDirection();
    }
    private Vector2 getRandomDirection() {
        float angle = random.nextFloat() * 360; // Random angle in degrees
        return new Vector2((float) Math.cos(Math.toRadians(angle)), (float) Math.sin(Math.toRadians(angle))).nor();
    }

    public void update(float delta, float playerX, float playerY, String playerDirection,HUD hud) {
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
            if (collidesHorizontal()) revertToPrevious(delta);

            monsterY += deltaY;
            if (collidesVertical()) revertToPrevious(delta);

            grieverRectangle.setPosition(monsterX, monsterY);

            grieverStateTime += delta;
            if (grieverStateTime >= grieverAnimationTime) {
                griever = getGrieverTextureForDirection(fixedGrieverDirection);
                grieverStateTime = 0;
            }
        }

        checkStunCondition(playerX, playerY, playerDirection,hud);
    }
    private String getNewDirectionOnCollision(String currentDirection) {
        String[] possibleDirections = {"up", "down", "left", "right"};
        String newDirection;

        do {
            newDirection = possibleDirections[random.nextInt(possibleDirections.length)];
        } while (newDirection.equals(currentDirection)); //except for current direction

        return newDirection;
    }


    private void checkStunCondition(float playerX, float playerY, String playerDirection, HUD hud) {
        float distance = (float) Math.sqrt(Math.pow(playerX - monsterX, 2) + Math.pow(playerY - monsterY, 2));
        if (distance <= 6.5f && !isGrieverStunned) {
            boolean isOppositeDirection = isGrieverInOppositeDirection(playerDirection);
            if (isOppositeDirection) {
                isGrieverStunned = true;
                stunTimer = 0;
                hud.stunMessage();
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
        Texture[] textures = grieverTextures.get(direction);
        return (griever == textures[0]) ? textures[1] : textures[0];
    }

    public void render(SpriteBatch batch) {
        batch.draw(griever, monsterX, monsterY, griever.getWidth() * scale, griever.getHeight() * scale);
    }

    public void revertToPrevious(float delta) {
        monsterX = previousX;
        monsterY = previousY;
        grieverRectangle.setPosition(monsterX, monsterY);

        fixedGrieverDirection = getNewDirectionOnCollision(fixedGrieverDirection);
        moveInDirection(fixedGrieverDirection, delta);
    }

    private void moveInDirection(String direction, float delta) {
        float adjustedSpeed = (monsterSpeed * delta);

        switch (direction) {
            case "up":
                monsterY += adjustedSpeed;
                break;
            case "down":
                monsterY -= adjustedSpeed;
                break;
            case "left":
                monsterX -= adjustedSpeed;
                break;
            case "right":
                monsterX += adjustedSpeed;
                break;
        }

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
        setPosition(-1000, -1000);
    }

    public void dispose() {
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