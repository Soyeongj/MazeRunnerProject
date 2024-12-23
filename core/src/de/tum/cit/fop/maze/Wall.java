package de.tum.cit.fop.maze;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

public class Wall {
    private int x, y; // 현재 타일의 위치
    private int originalX, originalY; // 원래 위치
    private String direction; // 이동 방향

    private float lastMoveTime = 0f; // 마지막 이동 시간
    private static final float MOVE_INTERVAL = 0.5f; // 10초 간격

    public Wall(int x, int y, String direction) {
        this.x = x;
        this.y = y;
        this.originalX = x;
        this.originalY = y;
        this.direction = direction;
        this.lastMoveTime = 0f;
    }

    public void update(float globalTimer, TiledMapTileLayer layer) {
        float timeSinceLastMove = globalTimer - lastMoveTime;
        System.out.println("Wall at (" + x + "," + y + ") - Time since last move: " + timeSinceLastMove + ", MOVE_INTERVAL: " + MOVE_INTERVAL);

        if (timeSinceLastMove >= MOVE_INTERVAL) {
            move(layer);
            resetPosition(layer);
            lastMoveTime = globalTimer;
            System.out.println("Wall moved. New lastMoveTime: " + lastMoveTime);
        }
    }


    private void move(TiledMapTileLayer layer) {
        TiledMapTileLayer.Cell cell = layer.getCell(x, y);
        if (cell == null) {
            System.out.println("Wall at (" + x + ", " + y + ") has no cell and cannot move.");
            return;
        }

        // 현재 위치 출력
        System.out.println("Wall at (" + x + ", " + y + ") is moving " + direction);


        layer.setCell(x, y, null); // 현재 위치의 타일 제거

        switch (direction) {
            case "left": x -= 1; break;
            case "right": x += 1; break;
            case "up": y += 1; break;
            case "down": y -= 1; break;
        }

        layer.setCell(x, y, cell); // 새로운 위치에 타일 추가
        // 이동 후 위치 출력
        System.out.println("Wall moved to new position: (" + x + ", " + y + ")");
    }

    private void resetPosition(TiledMapTileLayer layer) {
        TiledMapTileLayer.Cell cell = layer.getCell(x, y);
        if (cell == null) return;

        layer.setCell(x, y, null); // 현재 위치의 타일 제거
        x = originalX;
        y = originalY;
        layer.setCell(x, y, cell); // 원래 위치에 타일 복원
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
