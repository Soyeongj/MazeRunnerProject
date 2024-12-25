package de.tum.cit.fop.maze;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;

public class Wall {
    private int x, y; // 현재 위치
    private int originalX, originalY; // 원래 위치
    private int targetX, targetY; // 목표 위치
    private String direction; // 이동 방향
    private float lastMoveTime = 0f; // 마지막 이동 시간
    private static final float MOVE_INTERVAL = 5.0f; // 이동 간격 (초 단위)
    private static final float STAY_DURATION = 0.3f; // 목표 위치에서 대기하는 시간 (0.1초)
    private float stayTimer = 0f; // 목표 위치 대기 시간 측정
    private boolean isAtTarget = false; // 목표 위치에 도달했는지 여부
    private TiledMapTileLayer layer;
    private Griever griever;
    private boolean isGrieverDead = false;
    private Vector2 keySpawnPosition = null;

    public Wall(int x, int y, String direction, TiledMapTileLayer layer, Griever griever) {
        this.x = x;
        this.y = y;
        this.originalX = x;
        this.originalY = y;
        this.direction = direction;
        this.layer = layer;
        this.griever = griever;
    }

    public void update(float delta, float globalTimer) {
        float timeSinceLastMove = globalTimer - lastMoveTime;

        if (isAtTarget) {
            stayTimer += delta;
            if (stayTimer >= STAY_DURATION) {
                moveToOriginal();
                isAtTarget = false;
                stayTimer = 0f;
            }
            return;
        }

        if (timeSinceLastMove >= MOVE_INTERVAL && !isAtTarget) {
            move();
            lastMoveTime = globalTimer;
        }
        checkAndMoveGriever(griever);
    }

    private void move() {
        Cell cell = layer.getCell(x, y);
        if (cell == null) return;

        layer.setCell(x, y, null); // 현재 위치에서 타일 제거

        targetX = x;
        targetY = y;

        switch (direction) {
            case "left": targetX = x - 1; break;
            case "right": targetX = x + 1; break;
            case "up": targetY = y + 1; break;
            case "down": targetY = y - 1; break;
        }

        if (layer.getCell(targetX, targetY) != null) {
            System.err.println("Collision detected at target position: " + targetX + ", " + targetY);
            return; // 충돌 발생 시 이동 취소
        }

        layer.setCell(targetX, targetY, cell); // 목표 위치에 타일 설정
        x = targetX;
        y = targetY;
        isAtTarget = true;
        stayTimer = 0f;
    }

    private void moveToOriginal() {
        Cell cell = layer.getCell(x, y);
        if (cell == null) return;

        layer.setCell(x, y, null); // 현재 위치에서 타일 제거

        if (layer.getCell(originalX, originalY) != null) {
            System.err.println("Collision detected at original position: " + originalX + ", " + originalY);
            return; // 충돌 발생 시 복귀 취소
        }

        layer.setCell(originalX, originalY, cell); // 원래 위치에 타일 설정
        x = originalX;
        y = originalY;
    }

    private void animateMove(float delta) {
        // 애니메이션용 보간 처리 (가상 위치 계산 가능)
        float interpolatedX = x + (targetX - x) * (delta / MOVE_INTERVAL);
        float interpolatedY = y + (targetY - y) * (delta / MOVE_INTERVAL);
        renderWall(interpolatedX, interpolatedY);
    }

    private void checkAndMoveGriever(Griever griever) {

        float grieverX = griever.getMonsterX();
        float grieverY = griever.getMonsterY();


        float wallX = targetX * layer.getTileWidth();
        float wallY = targetY * layer.getTileHeight();
        float wallWidth = layer.getTileWidth();
        float wallHeight = layer.getTileHeight();


        if (x != originalX || y != originalY || isAtTarget) {
            if (checkCollision(grieverX, grieverY, griever.getWidth() * griever.getScale(), griever.getHeight() * griever.getScale(),
                    wallX, wallY, wallWidth, wallHeight)) {

                griever.setPosition(-1000, -1000);
                keySpawnPosition = new Vector2(grieverX, grieverY);
                isGrieverDead = true;

            }
        }
    }
    private boolean checkCollision(float x1, float y1, float width1, float height1,
                                   float x2, float y2, float width2, float height2) {
        return x1 < x2 + width2 && x1 + width1 > x2 &&
                y1 < y2 + height2 && y1 + height1 > y2;
    }







    private void renderWall(float interpolatedX, float interpolatedY) {
        // 애니메이션 렌더링 또는 디버깅 출력
        System.out.println("Animating wall at (" + interpolatedX + ", " + interpolatedY + ")");
    }

    public Vector2 getKeySpawnPosition() {
        return keySpawnPosition;
    }
    public boolean isGrieverDead() {
        return isGrieverDead;
    }


}