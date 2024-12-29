package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.*;

public class Griever {
    private Map<String, Texture[]> grieverTextures;
    private Texture griever;
    private float grieverStateTime;
    private String fixedGrieverDirection;
    private float monsterX, monsterY;
    private final float monsterSpeed = 10.0f;
    private final float detectionRange = 100.0f;
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
    private boolean isRandomMovement = false;

    private final TiledMapTileLayer pathLayer;
    private final TiledMapTileLayer path2Layer;
    private Vector2 currentTarget;

    public Griever(float startX, float startY, TiledMapTileLayer pathLayer, TiledMapTileLayer path2Layer) {
        this.monsterX = startX;
        this.monsterY = startY;
        this.pathLayer = pathLayer;
        this.path2Layer = path2Layer;

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

    private boolean isPathTile(float x, float y, TiledMapTileLayer layer) {
        int tileX = (int) (x / layer.getTileWidth());
        int tileY = (int) (y / layer.getTileHeight());
        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null; // 해당 타일이 유효한지 확인
    }

    private boolean isPathTile(float x, float y) {
        return isPathTile(x, y, pathLayer);
    }


    private Vector2 getRandomDirection() {
        float angle = random.nextFloat() * 360; // Random angle in degrees
        return new Vector2((float) Math.cos(Math.toRadians(angle)), (float) Math.sin(Math.toRadians(angle))).nor();
    }

    public boolean isRandomMovement() {
        return isRandomMovement;
    }


    public void update(float delta, float playerX, float playerY, String playerDirection, HUD hud) {
        grieverRectangle.setSize(griever.getWidth() * scale, griever.getHeight() * scale);

        if (isGrieverStunned) {
            stunTimer += delta;
            hud.stunMessage();
            if (stunTimer >= stunDuration) {
                isGrieverStunned = false;
                stunTimer = 0;
            }
            return;
        }

        float distance = (float) Math.sqrt(Math.pow(playerX - monsterX, 2) + Math.pow(playerY - monsterY, 2));
        boolean wasFollowingPlayer = isGrieverFollowingPlayer;
        isGrieverFollowingPlayer = distance <= detectionRange;

        System.out.println("Griever position: (" + monsterX + ", " + monsterY + ")");
        System.out.println("Player position: (" + playerX + ", " + playerY + ")");
        System.out.println("Distance to player: " + distance);
        System.out.println("Following player: " + isGrieverFollowingPlayer);

        // 추적 모드 전환 시 랜덤 상태 초기화
        if (isGrieverFollowingPlayer && !wasFollowingPlayer) {
            System.out.println("Player entered detection range. Switching to follow mode.");
            isRandomMovement = false;
            currentTarget = null; // 추적을 위한 목표 초기화
        }

        float deltaX = 0, deltaY = 0;

        if (isGrieverFollowingPlayer) {
            if (currentTarget == null || reachedTarget()) {
                currentTarget = findNextTargetTowardsPlayer(playerX, playerY);
                if (currentTarget == null) {
                    System.out.println("No valid target found while following the player. Switching to random movement temporarily.");
                    switchToTemporaryRandomMovement(delta); // 임시 랜덤 이동으로 전환
                    return;
                }
                System.out.println("New target set for following player: " + currentTarget);
            }

            // 목표 방향으로 이동
            Vector2 directionToTarget = new Vector2(currentTarget.x - monsterX, currentTarget.y - monsterY).nor();
            deltaX = directionToTarget.x * monsterSpeed * delta;
            deltaY = directionToTarget.y * monsterSpeed * delta;

            if (isPathTile(monsterX + deltaX, monsterY + deltaY, pathLayer)) {
                monsterX += deltaX;
                monsterY += deltaY;
                grieverRectangle.setPosition(monsterX, monsterY);
            } else {
                System.out.println("Collision detected while following the player. Recalculating target.");
                currentTarget = findNextTargetTowardsPlayer(playerX, playerY);
            }
        } else {
            // 랜덤 움직임
            isRandomMovement = true;

            if (currentTarget == null || reachedTarget()) {
                currentTarget = findNextTargetWithMinDistance(10f);
                if (currentTarget == null) {
                    System.out.println("No valid target found. Griever is stuck.");
                    return;
                }
                System.out.println("New target set: " + currentTarget);
            }

            Vector2 directionToTarget = new Vector2(currentTarget.x - monsterX, currentTarget.y - monsterY).nor();
            deltaX = directionToTarget.x * monsterSpeed * delta;
            deltaY = directionToTarget.y * monsterSpeed * delta;

            if (isPathTile(monsterX + deltaX, monsterY + deltaY, path2Layer)) {
                monsterX += deltaX;
                monsterY += deltaY;
                grieverRectangle.setPosition(monsterX, monsterY);
            } else {
                System.out.println("Collision detected! Recalculating target.");
                currentTarget = findNextTargetWithMinDistance(10f);
            }
        }

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            fixedGrieverDirection = deltaX > 0 ? "right" : "left";
        } else if (deltaY != 0) {
            fixedGrieverDirection = deltaY > 0 ? "up" : "down";
        }

        updateAnimation(delta);
        checkStunCondition(playerX, playerY, playerDirection);
    }

    // 플레이어 방향으로 목표 찾기 실패 시 임시 랜덤 이동으로 전환
    private void switchToTemporaryRandomMovement(float delta) {
        currentTarget = findNextTargetWithMinDistance(10f); // 임시 랜덤 목표 설정
        if (currentTarget != null) {
            System.out.println("Temporarily switched to random movement. New target: " + currentTarget);
        } else {
            System.out.println("Failed to switch to temporary random movement. Griever is stuck.");
        }
    }

    private void switchToRandomMovement() {
        isGrieverFollowingPlayer = false;
        currentTarget = findNextTargetWithMinDistance(10f); // 랜덤 목표 즉시 설정
        if (currentTarget != null) {
            System.out.println("Switched to random movement. New target: " + currentTarget);
        } else {
            System.out.println("Failed to switch to random movement. Griever is stuck.");
        }
    }


    private Vector2 findNextTargetTowardsPlayer(float playerX, float playerY) {
        Vector2[] directions = {
                new Vector2(1, 0),  // Right
                new Vector2(-1, 0), // Left
                new Vector2(0, 1),  // Up
                new Vector2(0, -1)  // Down
        };

        Vector2 closestTarget = null;
        float closestDistance = Float.MAX_VALUE;

        for (Vector2 direction : directions) {
            float nextX = monsterX + direction.x * pathLayer.getTileWidth();
            float nextY = monsterY + direction.y * pathLayer.getTileHeight();

            if (isPathTile(nextX, nextY, pathLayer)) {
                Vector2 directionToPlayer = new Vector2(playerX - nextX, playerY - nextY).nor();
                if (direction.dot(directionToPlayer) > 0) { // 플레이어 방향으로 타일 확인
                    float distanceToPlayer = Vector2.dst(nextX, nextY, playerX, playerY);
                    if (distanceToPlayer < closestDistance) {
                        closestTarget = new Vector2(nextX, nextY);
                        closestDistance = distanceToPlayer;
                    }
                }
            }
        }

        // 가장 가까운 타겟 반환 (없으면 null)
        return closestTarget != null ? closestTarget : new Vector2(playerX, playerY);
    }

    // 목표에 도달했는지 확인
    private boolean reachedTarget() {
        return Math.abs(monsterX - currentTarget.x) < 1f && Math.abs(monsterY - currentTarget.y) < 1f;
    }

    // 다음 목표를 설정
    private Vector2 findNextTargetWithMinDistance(float minDistance) {
        List<Vector2> directions = Arrays.asList(
                new Vector2(1, 0),  // Right
                new Vector2(-1, 0), // Left
                new Vector2(0, 1),  // Up
                new Vector2(0, -1)  // Down
        );
        Collections.shuffle(directions); // 방향 랜덤화

        for (Vector2 direction : directions) {
            float nextX = monsterX + direction.x * path2Layer.getTileWidth();
            float nextY = monsterY + direction.y * path2Layer.getTileHeight();

            if (isPathTile(nextX, nextY, path2Layer)) {
                float distance = Vector2.dst(monsterX, monsterY, nextX, nextY); // 목표와의 거리 계산
                if (distance >= minDistance) { // 최소 거리 조건
                    return new Vector2(nextX, nextY);
                }
            }
        }
        return null;
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
        if (distance <= 5f) {
            // 방향 조건 확인
            if (isGrieverInOppositeDirection(playerDirection) && !isGrieverStunned) {
                isGrieverStunned = true;
                stunTimer = 0;
                System.out.println("Griever is stunned!");
            } else {
                System.out.println("Griever is not stunned: Directions are not opposite.");
            }
        }
    }

    private boolean isGrieverInOppositeDirection(String playerDirection) {
        boolean result = (fixedGrieverDirection.equals("left") && playerDirection.equals("right")) ||
                (fixedGrieverDirection.equals("right") && playerDirection.equals("left")) ||
                (fixedGrieverDirection.equals("up") && playerDirection.equals("down")) ||
                (fixedGrieverDirection.equals("down") && playerDirection.equals("up"));
        System.out.println("Player Direction: " + playerDirection + ", Griever Direction: " + fixedGrieverDirection + ", Opposite: " + result);
        return result;
    }

    private Texture getGrieverTextureForDirection(String direction) {
        Texture[] textures = grieverTextures.get(direction);
        return (griever == textures[0]) ? textures[1] : textures[0];
    }
    public void handleCollisionWithPlayer(Player player, HUD hud, float delta) {
        int diffX = (int) (player.getX() - getMonsterX());
        int diffY = (int) (player.getY() - getMonsterY());
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

        if (distance < 5f && isGrieverNotStunned()) {
            if (hud.getLives() > 1) {
                hud.decrementLives();
                player.triggerRedEffect();
            } else {
                hud.setLives(0);
                player.setDead();
            }
        }
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