package de.tum.cit.fop.maze;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

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
    private Array<Griever> grievers;
    public boolean isGrieverDead = false;
    private Vector2 keySpawnPosition = null;
    private HUD hud;


    private TiledMapTileLayer.Cell cell;
    private TextureRegion texture;

    private boolean keySpawned = false;


    public Wall(int x, int y, String direction, TiledMapTileLayer layer, Array<Griever> grievers, HUD hud) {
        this.x = x;
        this.y = y;
        this.originalX = x;
        this.originalY = y;
        this.direction = direction;
        this.layer = layer;
        this.grievers = grievers;
        this.hud = hud;

        this.cell = layer.getCell(x, y);
        if (cell != null && cell.getTile() != null) {
            this.texture = cell.getTile().getTextureRegion();
        }
    }

    public static List<Wall> createWallsFromLayer(TiledMapTileLayer movingWallsLayer, Array<Griever> grievers, HUD hud) {
        List<Wall> walls = new ArrayList<>();
        for (int x = 0; x < movingWallsLayer.getWidth(); x++) {
            for (int y = 0; y < movingWallsLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = movingWallsLayer.getCell(x, y);
                if (cell != null && cell.getTile().getProperties().containsKey("direction")) {
                    String direction = cell.getTile().getProperties().get("direction", String.class);
                    walls.add(new Wall(x, y, direction, movingWallsLayer, grievers, hud));
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

        for (Griever griever : grievers) {
            checkAndMoveGriever(griever);
        }
    }

    private void move() {
        Cell cell = layer.getCell(x, y);
        if (cell == null) return;

        layer.setCell(x, y, null);

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

    private void checkAndMoveGriever(Griever griever) {
        float grieverX = griever.getMonsterX();
        float grieverY = griever.getMonsterY();

        float wallX = targetX * layer.getTileWidth();
        float wallY = targetY * layer.getTileHeight();
        float wallWidth = layer.getTileWidth();
        float wallHeight = layer.getTileHeight();

        if (x != originalX || y != originalY || isAtTarget) {
            if (griever.isRandomMovement()) {
                return;
            }

            if (checkCollision(grieverX, grieverY, griever.getWidth() * griever.getScale(), griever.getHeight() * griever.getScale(),
                    wallX, wallY, wallWidth, wallHeight)) {
                keySpawnPosition = new Vector2(grieverX, grieverY);
                isGrieverDead = true;

                SoundManager.playMonsterDiedSound();

            }
        }
    }

    public void checkAndMovePlayer(Player player, float globalTimer, Friends friends) {
        float playerX = player.getX();
        float playerY = player.getY();

        float wallX = targetX * layer.getTileWidth();
        float wallY = targetY * layer.getTileHeight();
        float wallWidth = layer.getTileWidth();
        float wallHeight = layer.getTileHeight();

        if ((x != originalX || y != originalY || isAtTarget)) {
            if (checkCollision(playerX, playerY, player.getWidth() * player.getScale(), player.getHeight() * player.getScale(),
                    wallX, wallY, wallWidth, wallHeight)) {

                if (hud != null) {
                    if (hud.getLives() >= 0) {
                        friends.removeFriendAt(friends.getFollowingFriendsPositions().size() - 1);
                        hud.decrementLives();
                        player.setX(player.getStartX());
                        player.setY(player.getStartY());
                        player.triggerRedEffect();
                    } else {
                        hud.setLives(0);
                        player.setDead();
                    }
                }
            }
        }
    }

    private boolean checkCollision(float x1, float y1, float width1, float height1,
                                   float x2, float y2, float width2, float height2) {
        return x1 < x2 + width2 && x1 + width1 > x2 &&
                y1 < y2 + height2 && y1 + height1 > y2;
    }

    public TiledMapTileLayer getLayer() {
        return layer;
    }

    public int getTargetY() {
        return targetY;
    }

    public int getTargetX() {
        return targetX;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getOriginalX() {
        return originalX;
    }

    public int getOriginalY() {
        return originalY;
    }

    public boolean isAtTarget() {
        return isAtTarget;
    }

    public Vector2 getKeySpawnPosition() {
        return keySpawnPosition;
    }

    public boolean isGrieverDead() {
        return isGrieverDead;
    }

    public void setGrieverDead(boolean grieverDead) {
        isGrieverDead = grieverDead;
    }

    public boolean hasKeySpawned() {
        return keySpawned;
    }

    public void setKeySpawned(boolean keySpawned) {
        this.keySpawned = keySpawned;
    }



    public void saveWallState() {
        Preferences pref = Gdx.app.getPreferences("wallState");
        pref.putBoolean("grieverDead", isGrieverDead);
        pref.putBoolean("isKeySpawned",keySpawned);
        pref.flush();
    }
    public void loadWallState() {
        Preferences pref = Gdx.app.getPreferences("wallState");
        isGrieverDead = pref.getBoolean("grieverDead", isGrieverDead);
        keySpawned = pref.getBoolean("isKeySpawned", keySpawned);
    }
}