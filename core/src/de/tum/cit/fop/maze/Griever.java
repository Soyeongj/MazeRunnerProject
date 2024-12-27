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
    private TiledMapTileLayer pathLayer;

    public Griever(float startX, float startY, TiledMapTileLayer collisionLayer, TiledMapTileLayer pathLayer) {
        this.monsterX = startX;
        this.monsterY = startY;
        this.previousX = startX;
        this.previousY = startY;
        this.collisionLayer = collisionLayer;
        this.pathLayer = pathLayer;

        // Load griever textures
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
    private boolean isPathTile(float x, float y) {
        TiledMapTileLayer.Cell cell = pathLayer.getCell(
                (int) (x / pathLayer.getTileWidth()),
                (int) (y / pathLayer.getTileHeight())
        );
        return cell != null && cell.getTile() != null;
    }

    private Vector2 getRandomDirection() {
        float angle = random.nextFloat() * 360; // Random angle in degrees
        return new Vector2((float) Math.cos(Math.toRadians(angle)), (float) Math.sin(Math.toRadians(angle))).nor();
    }
    public void update(float delta, float playerX, float playerY, String playerDirection) {
        grieverRectangle.setSize(griever.getWidth() * scale, griever.getHeight() * scale);


        if (isGrieverStunned) {
            stunTimer += delta;
            if (stunTimer >= stunDuration) {
                isGrieverStunned = false;
                stunTimer = 0;
            }
            return;
        }

        previousX = monsterX;
        previousY = monsterY;

        float deltaX = 0, deltaY = 0;


        float distance = (float) Math.sqrt(Math.pow(playerX - monsterX, 2) + Math.pow(playerY - monsterY, 2));


        if (distance <= detectionRange) {
            isGrieverFollowingPlayer = true;
        } else {
            isGrieverFollowingPlayer = false;
        }

        if (isGrieverFollowingPlayer) {

            Vector2 direction = new Vector2(playerX - monsterX, playerY - monsterY).nor();
            deltaX = direction.x * monsterSpeed * delta;
            deltaY = direction.y * monsterSpeed * delta;


            if (!isPathTile(monsterX + deltaX, monsterY + deltaY)) {
                return;
            }
        } else {

            randomMovementTimer += delta;
            if (randomMovementTimer >= randomMovementInterval) {
                randomDirection = getRandomDirection();
                randomMovementTimer = 0f;
            }

            deltaX = randomDirection.x * monsterSpeed * delta;
            deltaY = randomDirection.y * monsterSpeed * delta;


            if (!isPathTile(monsterX + deltaX, monsterY + deltaY)) {
                randomDirection = getRandomDirection();
                return;
            }
        }


        monsterX += deltaX;
        monsterY += deltaY;
        grieverRectangle.setPosition(monsterX, monsterY);


        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            fixedGrieverDirection = deltaX > 0 ? "right" : "left";
        } else {
            fixedGrieverDirection = deltaY > 0 ? "up" : "down";
        }


        grieverStateTime += delta;
        if (grieverStateTime >= grieverAnimationTime) {
            griever = getGrieverTextureForDirection(fixedGrieverDirection);
            grieverStateTime = 0;
        }


        checkStunCondition(playerX, playerY, playerDirection);
    }




    private String getNewDirectionOnCollision(String currentDirection) {
        String[] possibleDirections = {"up", "down", "left", "right"};
        String newDirection;

        do {
            newDirection = possibleDirections[random.nextInt(possibleDirections.length)];
        } while (newDirection.equals(currentDirection)); //except for current direction

        return newDirection;
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
        return griever.getWidth();
    }


    public float getHeight() {
        return griever.getHeight();
    }


    public float getScale() {
        return scale;
    }

}