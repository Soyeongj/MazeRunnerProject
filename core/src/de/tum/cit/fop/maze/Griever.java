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
    private final float monsterSpeed = 10.0f;
    private final float detectionRange = 37.0f;
    private boolean isGrieverFollowingPlayer = false;
    private final float grieverAnimationTime = 0.1f; // Time between animation frames
    private Rectangle grieverRectangle;
    private final float scale = 0.2f;

    private boolean isGrieverStunned = false;
    private float stunTimer = 0.0f;
    private final float stunDuration = 3.0f; // 3 seconds stun duration

    private Vector2 randomDirection;
    private float randomMovementTimer = 0f;
    private final float randomMovementInterval = 8.0f;
    private final Random random = new Random();

    private final TiledMapTileLayer collisionLayer;
    private final TiledMapTileLayer pathLayer;

    public Griever(float startX, float startY, TiledMapTileLayer collisionLayer, TiledMapTileLayer pathLayer) {
        this.monsterX = startX;
        this.monsterY = startY;
        this.collisionLayer = collisionLayer;
        this.pathLayer = pathLayer;

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

    private Vector2 findAlternativeDirection(float delta) {
        Vector2[] possibleDirections = {
                new Vector2(1, 0), // Right
                new Vector2(-1, 0), // Left
                new Vector2(0, 1), // Up
                new Vector2(0, -1) // Down
        };

        for (Vector2 direction : possibleDirections) {
            float tempDeltaX = direction.x * monsterSpeed * delta;
            float tempDeltaY = direction.y * monsterSpeed * delta;
            if (isPathTile(monsterX + tempDeltaX, monsterY + tempDeltaY)) {
                return new Vector2(tempDeltaX, tempDeltaY);
            }
        }
        return null;
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

        float deltaX = 0, deltaY = 0;
        float distance = (float) Math.sqrt(Math.pow(playerX - monsterX, 2) + Math.pow(playerY - monsterY, 2));
        isGrieverFollowingPlayer = distance <= detectionRange;

        if (isGrieverFollowingPlayer) {
            Vector2 directionToPlayer = new Vector2(playerX - monsterX, playerY - monsterY).nor();
            deltaX = directionToPlayer.x * monsterSpeed * delta;
            deltaY = directionToPlayer.y * monsterSpeed * delta;

            if (!isPathTile(monsterX + deltaX, monsterY + deltaY)) {
                Vector2 alternative = findAlternativeDirection(delta);
                if (alternative != null) {
                    deltaX = alternative.x;
                    deltaY = alternative.y;
                } else {
                    return;
                }
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

        updateAnimation(delta);
        checkStunCondition(playerX, playerY, playerDirection);
    }

    private void updateAnimation(float delta) {
        grieverStateTime += delta;
        if (grieverStateTime >= grieverAnimationTime) {
            griever = getGrieverTextureForDirection(fixedGrieverDirection);
            grieverStateTime = 0;
        }
    }

    private void checkStunCondition(float playerX, float playerY, String playerDirection) {
        float distance = (float) Math.sqrt(Math.pow(playerX - monsterX, 2) + Math.pow(playerY - monsterY, 2));
        if (distance <= 5f && !isGrieverStunned) {
            if (isGrieverInOppositeDirection(playerDirection)) {
                isGrieverStunned = true;
                stunTimer = 0;
            }
        }
    }

    private boolean isGrieverInOppositeDirection(String playerDirection) {
        return (fixedGrieverDirection.equals("left") && playerDirection.equals("right")) ||
                (fixedGrieverDirection.equals("right") && playerDirection.equals("left")) ||
                (fixedGrieverDirection.equals("up") && playerDirection.equals("down")) ||
                (fixedGrieverDirection.equals("down") && playerDirection.equals("up"));
    }

    private Texture getGrieverTextureForDirection(String direction) {
        Texture[] textures = grieverTextures.get(direction);
        return (griever == textures[0]) ? textures[1] : textures[0];
    }

    public void render(SpriteBatch batch) {
        batch.draw(griever, monsterX, monsterY, griever.getWidth() * scale, griever.getHeight() * scale);
    }

    public void dispose() {
        for (Texture[] textures : grieverTextures.values()) {
            for (Texture texture : textures) {
                texture.dispose();
            }
        }
    }

    public void setPosition(float x, float y) {
        this.monsterX = x;
        this.monsterY = y;
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
