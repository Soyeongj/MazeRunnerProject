package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

import java.util.*;

public class Griever implements  Renderable {
    // Animation and Texture Related Variables
    private Map<String, Texture[]> grieverTextures;
    private Texture griever;
    private float grieverStateTime;
    private String fixedGrieverDirection;
    private final float grieverAnimationTime = 0.1f;
    private final float scale = 0.4f;

    // Position and Movement Related Variables
    private float monsterX, monsterY;
    private final float monsterSpeed = 15.0f;
    private final float detectionRange = 50.0f;
    private boolean isGrieverFollowingPlayer = false;
    private Vector2 currentTarget;
    private final Random random = new Random();
    private boolean isRandomMovement = false;

    // Stun Related Variables
    private boolean isGrieverStunned = false;
    private float stunTimer = 0.0f;
    private final float stunDuration = 3.0f;

    // Layer Related Variables
    private final TiledMapTileLayer pathLayer;
    private final TiledMapTileLayer path2Layer;

    //Griever-Damage Related Variables
    private float LivesCoolDownTimer = 0;

    //Boundaries
    private static final float MAX_X = 478.86f;
    private static final float MAX_Y = 478f;
    private static final float MIN_X = 0f;
    private static final float MIN_Y = 0f;

    public Griever(float startX, float startY, TiledMapTileLayer pathLayer, TiledMapTileLayer path2Layer) {
        this.monsterX = startX;
        this.monsterY = startY;
        this.pathLayer = pathLayer;
        this.path2Layer = path2Layer;

        initializeTextures();
    }

    private void initializeTextures() {
        grieverTextures = new HashMap<>();
        grieverTextures.put("up", new Texture[]{new Texture("monster_up1.png"), new Texture("monster_up2.png")});
        grieverTextures.put("down", new Texture[]{new Texture("monster_down1.png"), new Texture("monster_down2.png")});
        grieverTextures.put("left", new Texture[]{new Texture("monster_left1.png"), new Texture("monster_left2.png")});
        grieverTextures.put("right", new Texture[]{new Texture("monster_right1.png"), new Texture("monster_right2.png")});

        fixedGrieverDirection = "right";
        griever = grieverTextures.get(fixedGrieverDirection)[0];
        grieverStateTime = 0f;
    }


    public static Array<Griever> loadGrieversFromTiledMap(TiledMap map, TiledMapTileLayer pathLayer, TiledMapTileLayer path2Layer) {
        Array<Griever> grievers = new Array<>();

        MapLayer grieverLayer = map.getLayers().get("griever");  // "griever" 레이어에서 그리버 객체 읽기
        MapObjects objects = grieverLayer.getObjects();

        for (MapObject object : objects) {
            // "griever" 속성값이 1인 타일만 선택
            Object grieverProperty = object.getProperties().get("griever");
            if (grieverProperty != null && "1".equals(grieverProperty.toString())) {
                float x = Float.parseFloat(object.getProperties().get("x").toString());
                float y = Float.parseFloat(object.getProperties().get("y").toString());

                // 그리버 객체 생성 후 배열에 추가
                Griever griever = new Griever(x, y, pathLayer, path2Layer);
                grievers.add(griever);
            }
        }
        return grievers;
    }

    // Main update method orchestrating all update logic
    public void update(float delta, float playerX, float playerY, String playerDirection, HUD hud, Player player, Friends friends, List<Wall> walls) {
        if (handleStunState(delta, hud)) {
            return;
        }

        updateGrieverState(playerX, playerY);
        handleMovement(delta, playerX, playerY, walls);
        updateAnimation(delta);
        checkStunCondition(playerX, playerY, playerDirection);
        checkPlayerCollision(player, hud, friends, delta);
    }

    // Handles stun state and returns true if griever is stunned
    private boolean handleStunState(float delta, HUD hud) {
        if (isGrieverStunned) {
            stunTimer += delta;
            hud.stunMessage();
            if (stunTimer >= stunDuration) {
                isGrieverStunned = false;
                stunTimer = 0;
            }
            return true;
        }
        return false;
    }

    // Updates griever's state based on player position
    private void updateGrieverState(float playerX, float playerY) {
        float distance = calculateDistance(playerX, playerY);
        boolean wasFollowingPlayer = isGrieverFollowingPlayer;
        isGrieverFollowingPlayer = distance <= detectionRange;

        if (isGrieverFollowingPlayer && !wasFollowingPlayer) {
            resetGrieverState();
        }
    }

    // Handles all movement logic
    private void handleMovement(float delta, float playerX, float playerY, List<Wall> walls) {
        if (isGrieverFollowingPlayer) {
            handlePlayerFollowing(delta, playerX, playerY);
        } else {
            handleRandomMovement(delta,walls);
        }
    }

    // Handles movement when following player
    private void handlePlayerFollowing(float delta, float playerX, float playerY) {
        if (currentTarget == null || reachedTarget()) {
            updateTargetTowardsPlayer(playerX, playerY);
        }

        if (currentTarget != null) {
            moveTowardsTarget(delta, pathLayer);
        }
    }

    private void handleRandomMovement(float delta, List<Wall> walls) {
        isRandomMovement = true;

        // Check proximity to moving walls
        for (Wall wall : walls) {
            float wallX = wall.getX();
            float wallY = wall.getY();

            // Measure the distance between the Griever and the wall
            float distanceToWall = Vector2.dst(monsterX, monsterY, wallX, wallY);


            // If too close to a moving wall, move away
            if (distanceToWall <= 15f) {
                avoidMovingWall(wall);
                return; // Exit early, as the Griever should avoid the wall
            }
        }

        // Proceed with random movement if no walls are too close
        if (currentTarget == null || reachedTarget()) {
            currentTarget = findNextTargetWithMinDistance(10f);
            if (currentTarget == null) {
                return;
            }
        }
        moveTowardsTarget(delta, pathLayer);
    }

    // Helper method to move the Griever away from a moving wall
    private void avoidMovingWall(Wall wall) {
        float wallX = wall.getX();
        float wallY = wall.getY();

        // Determine the opposite direction from the wall
        float dx = monsterX - wallX;
        float dy = monsterY - wallY;

        // Normalize to get a unit vector and scale for a small move
        Vector2 escapeVector = new Vector2(dx, dy).nor().scl(5f);

        // Update Griever's position to move away from the wall
        monsterX += escapeVector.x;
        monsterY += escapeVector.y;

        // Ensure the new position is valid
        if (!isValidPosition(monsterX, monsterY) || !isPathTile(monsterX, monsterY, path2Layer)) {
            monsterX -= escapeVector.x; // Revert if invalid move
            monsterY -= escapeVector.y;
        }
    }

    // Updates target when following player
    private void updateTargetTowardsPlayer(float playerX, float playerY) {
        int attempts = 0;
        Vector2 newTarget = null;

        while (newTarget == null && attempts < 3) {
            newTarget = findNextTargetTowardsPlayer(playerX, playerY);
            attempts++;
        }

        if (newTarget == null) {
            switchToRandomMovement();
            return;
        }

        currentTarget = newTarget;
    }

    private void moveTowardsTarget(float delta, TiledMapTileLayer currentLayer) {
        Vector2 directionToTarget = new Vector2(currentTarget.x - monsterX, currentTarget.y - monsterY);
        float distanceToTarget = directionToTarget.len();

        if (distanceToTarget > 1f) {
            directionToTarget.nor();
            float deltaX = directionToTarget.x * monsterSpeed * delta;
            float deltaY = directionToTarget.y * monsterSpeed * delta;

            updateGrieverDirection(deltaX, deltaY);

            // Calculate new position
            float newX = monsterX + deltaX;
            float newY = monsterY + deltaY;

            // Check boundaries and path validity
            if (isValidPosition(newX, newY) && isPathTile(newX, newY, currentLayer)) {
                monsterX = newX;
                monsterY = newY;
            } else {
                handleCollision();
            }
        }
    }

    // Add method to check if position is within boundaries
    private boolean isValidPosition(float x, float y) {
        return x >= MIN_X && x <= MAX_X && y >= MIN_Y && y <= MAX_Y;
    }


    // Updates griever's direction based on movement
    private void updateGrieverDirection(float deltaX, float deltaY) {
        if (Math.abs(deltaX) > 0.01f || Math.abs(deltaY) > 0.01f) {
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                fixedGrieverDirection = deltaX > 0 ? "right" : "left";
            } else {
                fixedGrieverDirection = deltaY > 0 ? "up" : "down";
            }
        }
    }

    // Handles collision with obstacles
    private void handleCollision() {
        if (isGrieverFollowingPlayer) {
            currentTarget = null;  // Force recalculation of path
        } else {
            currentTarget = findNextTargetWithMinDistance(10f);
        }
    }

    // Helper method to reset griever state
    private void resetGrieverState() {
        isRandomMovement = false;
        currentTarget = null;
    }

    // Helper method to calculate distance to player
    private float calculateDistance(float playerX, float playerY) {
        return (float) Math.sqrt(Math.pow(playerX - monsterX, 2) + Math.pow(playerY - monsterY, 2));
    }

    private Vector2 findNextTargetWithMinDistance(float minDistance) {
        // 4개의 기본 방향을 정의 (오른쪽, 왼쪽, 위, 아래)
        List<Vector2> directions = Arrays.asList(
                new Vector2(1, 0),  // Right
                new Vector2(-1, 0), // Left
                new Vector2(0, 1),  // Up
                new Vector2(0, -1)  // Down
        );
        Collections.shuffle(directions); // 방향을 랜덤하게 섞음

        // 각 방향에 대해 검사
        for (Vector2 direction : directions) {
            // 다음 타일의 위치 계산
            float nextX = monsterX + direction.x * path2Layer.getTileWidth();
            float nextY = monsterY + direction.y * path2Layer.getTileHeight();

            // 해당 위치가 이동 가능한 타일인지 확인
            if (isPathTile(nextX, nextY, path2Layer)) {
                // 현재 위치와 다음 위치 사이의 거리 계산
                float distance = Vector2.dst(monsterX, monsterY, nextX, nextY);
                // 최소 거리 조건을 만족하면 해당 위치를 반환
                if (distance >= minDistance) {
                    return new Vector2(nextX, nextY);
                }
            }
        }
        return null; // 적절한 목표점을 찾지 못한 경우
    }

    private void switchToRandomMovement() {
        isGrieverFollowingPlayer = false;
        currentTarget = findNextTargetWithMinDistance(10f); // 랜덤 목표 즉시 설정
        if (currentTarget != null) {
        } else {
        }
    }

    // 목표에 도달했는지 확인
    private boolean reachedTarget() {
        float tolerance = 2f; // 도달 판정 거리를 약간 늘림
        return Math.abs(monsterX - currentTarget.x) < tolerance &&
                Math.abs(monsterY - currentTarget.y) < tolerance;
    }

    // 다음 목표를 설정
    private Vector2 findNextTargetTowardsPlayer(float playerX, float playerY) {
        Vector2[] directions = {
                new Vector2(1, 0), new Vector2(-1, 0),
                new Vector2(0, 1), new Vector2(0, -1)
        };

        Vector2 closestTarget = null;
        float closestDistance = Float.MAX_VALUE;

        // 현재 위치에서 실제로 이동 가능한 방향만 고려
        for (Vector2 direction : directions) {
            float nextX = monsterX + direction.x * pathLayer.getTileWidth();
            float nextY = monsterY + direction.y * pathLayer.getTileHeight();

            // 이동 가능성 체크를 더 엄격하게
            if (isPathTile(nextX, nextY, pathLayer) &&
                    isPathClear(monsterX, monsterY, nextX, nextY)) {
                Vector2 potentialTarget = new Vector2(nextX, nextY);
                float distanceToPlayer = Vector2.dst(nextX, nextY, playerX, playerY);

                if (distanceToPlayer < closestDistance) {
                    closestTarget = potentialTarget;
                    closestDistance = distanceToPlayer;
                }
            }
        }

        // 유효한 타겟을 찾지 못했다면 랜덤 이동으로 전환
        if (closestTarget == null) {
            switchToRandomMovement();
            return findNextTargetWithMinDistance(10f);
        }

        return closestTarget;
    }

    private boolean isPathClear(float startX, float startY, float endX, float endY) {
        // 시작점과 끝점 사이의 여러 지점을 체크
        float steps = 5; // 체크할 중간 지점의 수
        for (int i = 0; i <= steps; i++) {
            float t = i / steps;
            float checkX = startX + (endX - startX) * t;
            float checkY = startY + (endY - startY) * t;

            if (!isPathTile(checkX, checkY, pathLayer)) {
                return false;
            }
        }
        return true;
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
        if (distance <= 10f) {
            // 방향 조건 확인
            if (isGrieverInOppositeDirection(playerDirection) && !isGrieverStunned) {
                isGrieverStunned = true;
                stunTimer = 0;
            } else {
            }
        }
    }

    private boolean isGrieverInOppositeDirection(String playerDirection) {
        boolean result = (fixedGrieverDirection.equals("left") && playerDirection.equals("right")) ||
                (fixedGrieverDirection.equals("right") && playerDirection.equals("left")) ||
                (fixedGrieverDirection.equals("up") && playerDirection.equals("down")) ||
                (fixedGrieverDirection.equals("down") && playerDirection.equals("up"));
        return result;
    }


    public void checkPlayerCollision(Player player, HUD hud, Friends friends, float delta) {
        int diffX = (int) (player.getX() - this.getMonsterX());
        int diffY = (int) (player.getY() - this.getMonsterY());
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

        if (LivesCoolDownTimer <= 0 && distance < 5f && this.isGrieverNotStunned()) {
            if (hud.getLives() >= 0) {
                friends.removeFriendAt(friends.getFollowingFriendsPositions().size()-1);
                hud.decrementLives();
                player.triggerRedEffect();
                LivesCoolDownTimer = 2;
            } else {
                hud.setLives(0);
                player.revertToPrevious();
                player.setDead();
            }
        }

        if (LivesCoolDownTimer > 0) {
            LivesCoolDownTimer -= delta;
        }
    }


    //Render Method
    public void render(SpriteBatch batch) {
        batch.draw(griever, monsterX, monsterY, griever.getWidth() * scale, griever.getHeight() * scale);
    }

    //Setters and Getters
    public void setPosition(float x, float y) {
        this.monsterX = x;
        this.monsterY = y;
    }

    private Texture getGrieverTextureForDirection(String direction) {
        Texture[] textures = grieverTextures.get(direction);
        return (griever == textures[0]) ? textures[1] : textures[0];
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

    public boolean isRandomMovement() {
        return isRandomMovement;
    }

    private boolean isPathTile(float x, float y, TiledMapTileLayer layer) {
        int tileX = (int) (x / layer.getTileWidth());
        int tileY = (int) (y / layer.getTileHeight());
        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null; // 해당 타일이 유효한지 확인
    }


    public void dispose() {
        for (Texture[] textures : grieverTextures.values()) {
            for (Texture texture : textures) {
                texture.dispose();
            }
        }
    }


    public void saveGrieverstate() {
        Preferences pref = Gdx.app.getPreferences("grieverstate");
        pref.putFloat("x", monsterX);
        pref.putFloat("y", monsterY);
        pref.putBoolean("isGrieverStunned", isGrieverStunned);
        pref.putBoolean("isGrieverFollowing",isGrieverFollowingPlayer);
        pref.putBoolean("isGrieverRandom",isRandomMovement);
        pref.putFloat("livescooldown",LivesCoolDownTimer);
        pref.flush();
    }
    public void loadGrieverstate() {
        Preferences pref = Gdx.app.getPreferences("grieverstate");
        monsterX = pref.getFloat("x", monsterX);
        monsterY = pref.getFloat("y", monsterY);
        isGrieverStunned = pref.getBoolean("isGrieverStunned", isGrieverStunned);
        isGrieverFollowingPlayer = pref.getBoolean("isGrieverFollowing", isGrieverFollowingPlayer);
        isRandomMovement = pref.getBoolean("isRandomMovement", isRandomMovement);
        LivesCoolDownTimer = pref.getFloat("livescooldown", LivesCoolDownTimer);
    }
}