package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.scenes.scene2d.ui.Table.Debug.cell;

public class Wall {
    private int x, y;
    private int originalX, originalY;
    private int targetX, targetY;
    private String direction;
    private float lastMoveTime = 0f;
    private static final float MOVE_INTERVAL = 5.0f;
    private static final float STAY_DURATION = 0.3f;
    private float stayTimer = 0f;
    private boolean isAtTarget = false;
    private TiledMapTileLayer layer;
    private Griever griever;
    private boolean isGrieverDead = false;
    private Vector2 keySpawnPosition = null;
    private HUD hud;
    private boolean isPlayerRemoved = false;
    private float removalCooldown = 0f;

    private TiledMapTileLayer.Cell cell; // 타일 정보를 저장
    private TextureRegion texture; // 타일 텍스처


    public Wall(int x, int y, String direction, TiledMapTileLayer layer, Griever griever, HUD hud) {
        this.x = x;
        this.y = y;
        this.originalX = x;
        this.originalY = y;
        this.direction = direction;
        this.layer = layer;
        this.griever = griever;
        this.hud = hud;

        this.cell = layer.getCell(x, y);
        if (cell != null && cell.getTile() != null) {
            this.texture = cell.getTile().getTextureRegion();
        }
    }

    public static List<Wall> createWallsFromLayer(TiledMapTileLayer movingWallsLayer, Griever griever, HUD hud) {
        List<Wall> walls = new ArrayList<>();
        for (int x = 0; x < movingWallsLayer.getWidth(); x++) {
            for (int y = 0; y < movingWallsLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = movingWallsLayer.getCell(x, y);
                if (cell != null && cell.getTile().getProperties().containsKey("direction")) {
                    String direction = cell.getTile().getProperties().get("direction", String.class);
                    walls.add(new Wall(x, y, direction, movingWallsLayer, griever, hud));
                }
            }
        }
        return walls;
    }

    public void render(SpriteBatch batch) {
        if (texture != null) {
            float worldX = x * layer.getTileWidth();
            float worldY = y * layer.getTileHeight();
            batch.draw(texture, worldX, worldY, layer.getTileWidth(), layer.getTileHeight());
        }
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
            return;
        }

        layer.setCell(targetX, targetY, cell);
        x = targetX;
        y = targetY;
        isAtTarget = true;
        stayTimer = 0f;
    }

    private void moveToOriginal() {
        Cell cell = layer.getCell(x, y);
        if (cell == null) return;

        layer.setCell(x, y, null);

        if (layer.getCell(originalX, originalY) != null) {
            return;
        }

        layer.setCell(originalX, originalY, cell);
        x = originalX;
        y = originalY;
    }

    private void animateMove(float delta) {
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
            if (griever.isRandomMovement()) {
                // Griever가 랜덤 움직임 중일 경우 죽음을 방지
                System.out.println("Griever death prevented during random movement by Wall.");
                return;
            }

            if (checkCollision(grieverX, grieverY, griever.getWidth() * griever.getScale(), griever.getHeight() * griever.getScale(),
                    wallX, wallY, wallWidth, wallHeight)) {
                griever.setPosition(-1000, -1000);
                keySpawnPosition = new Vector2(grieverX, grieverY);
                isGrieverDead = true;
            }
        }
    }

    public void checkAndMovePlayer(Player player, float globalTimer) {

        float playerX = player.getX();
        float playerY = player.getY();


        float wallX = targetX * layer.getTileWidth();
        float wallY = targetY * layer.getTileHeight();
        float wallWidth = layer.getTileWidth();
        float wallHeight = layer.getTileHeight();


        if ((x != originalX || y != originalY || isAtTarget) && !isPlayerRemoved) {
            if (checkCollision(playerX, playerY, player.getWidth() * player.getScale(), player.getHeight() * player.getScale(),
                    wallX, wallY, wallWidth, wallHeight)) {


                if (hud != null) {
                    if (hud.getLives() > 1) {
                        hud.decrementLives();
                        player.triggerRedEffect();
                    } else {
                        hud.setLives(0);
                        player.setDead();
                    }
                }


                player.setX(-1000);
                player.setY(-1000);
                isPlayerRemoved = true;
            }
        }


        if (isPlayerRemoved && !isAtTarget && x == originalX && y == originalY) {
            float safeX = 230;
            float safeY = 250;

            player.setX(safeX);
            player.setY(safeY);

            isPlayerRemoved = false;
        }

    }

    private boolean checkCollision(float x1, float y1, float width1, float height1,
                                   float x2, float y2, float width2, float height2) {
        return x1 < x2 + width2 && x1 + width1 > x2 &&
                y1 < y2 + height2 && y1 + height1 > y2;
    }


    private void renderWall(float interpolatedX, float interpolatedY) {
    }

    public Vector2 getKeySpawnPosition() {
        return keySpawnPosition;
    }
    public boolean isGrieverDead() {
        return isGrieverDead;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setGrieverDead(boolean grieverDead) {
        isGrieverDead = grieverDead;
    }

}