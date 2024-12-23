package de.tum.cit.fop.maze;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class Wall {
    private int x, y; // 현재 위치
    private int originalX, originalY; // 원래 위치
    private String direction; // 이동 방향
    private float lastMoveTime = 0f; // 마지막 이동 시간
    private static final float MOVE_INTERVAL = 5.0f; // 이동 간격 (초 단위)

    private boolean isAnimating = false; // 애니메이션 중인지 여부
    private float animationProgress = 0f; // 애니메이션 진행률
    private float animationDuration = 0.5f; // 애니메이션 지속 시간
    private int targetX, targetY; // 이동 목표 위치
    private boolean returningToOriginal = false; // 원래 위치로 돌아가는지 여부

    public Wall(int x, int y, String direction) {
        this.x = x;
        this.y = y;
        this.originalX = x;
        this.originalY = y;
        this.direction = direction;
    }

    public void update(float delta, float globalTimer, TiledMapTileLayer layer) {
        float timeSinceLastMove = globalTimer - lastMoveTime;

        if (isAnimating) {
            animate(delta, layer); // 애니메이션 처리
            return;
        }

        if (timeSinceLastMove >= MOVE_INTERVAL) {
            if (returningToOriginal) {
                moveToOriginal(layer); // 원래 위치로 복귀
            } else {
                move(layer); // 목표 위치로 이동
            }
            lastMoveTime = globalTimer;
        }
    }

    private void move(TiledMapTileLayer layer) {
        TiledMapTileLayer.Cell cell = layer.getCell(x, y);
        if (cell == null) return;

        layer.setCell(x, y, null); // 현재 위치 제거

        targetX = x;
        targetY = y;

        switch (direction) {
            case "left": targetX = x - 1; break;
            case "right": targetX = x + 1; break;
            case "up": targetY = y + 1; break;
            case "down": targetY = y - 1; break;
        }

        // 이동 목표 타일에 설정
        layer.setCell(targetX, targetY, cell);
        isAnimating = true; // 애니메이션 시작
        animationProgress = 0f; // 애니메이션 진행률 초기화
        returningToOriginal = false; // 복귀 상태 초기화
    }

    private void moveToOriginal(TiledMapTileLayer layer) {
        TiledMapTileLayer.Cell cell = layer.getCell(targetX, targetY);
        if (cell == null) return;

        layer.setCell(targetX, targetY, null); // 현재 위치 제거
        layer.setCell(originalX, originalY, cell); // 원래 위치 설정

        isAnimating = true; // 애니메이션 시작
        animationProgress = 0f; // 애니메이션 진행률 초기화
        returningToOriginal = true; // 복귀 중 상태 설정
    }

    private void animate(float delta, TiledMapTileLayer layer) {
        animationProgress += delta / animationDuration;

        if (animationProgress >= 1f) {
            if (returningToOriginal) {
                // 원래 위치로 돌아가는 애니메이션 완료
                x = originalX;
                y = originalY;
                returningToOriginal = false; // 복귀 완료
                isAnimating = false;
            } else {
                // 목표 위치로 이동하는 애니메이션 완료
                x = targetX;
                y = targetY;
                isAnimating = false;
                returningToOriginal = true; // 다음 이동은 복귀
            }
            animationProgress = 0f; // 진행률 초기화
        } else {
            float interpolatedX, interpolatedY;
            if (returningToOriginal) {
                // 원래 위치로 돌아가는 애니메이션 진행
                interpolatedX = targetX + (originalX - targetX) * animationProgress;
                interpolatedY = targetY + (originalY - targetY) * animationProgress;
            } else {
                // 목표 위치로 이동하는 애니메이션 진행
                interpolatedX = x + (targetX - x) * animationProgress;
                interpolatedY = y + (targetY - y) * animationProgress;
            }

            renderWall(interpolatedX, interpolatedY, layer);
        }
    }

    private void renderWall(float interpolatedX, float interpolatedY, TiledMapTileLayer layer) {
        System.out.println("Animating wall at (" + interpolatedX + ", " + interpolatedY + ")");
        // 실제 애니메이션 렌더링 코드 필요
    }
}
