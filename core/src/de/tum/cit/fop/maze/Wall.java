package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;


public class Wall {
    private int x, y; // Current position
    private int originalX, originalY; // Original position
    private String direction; // Movement direction
    private float lastMoveTime = 0f; // Last movement time
    private static final float MOVE_INTERVAL = 3f; // Movement interval in seconds

    private boolean isAnimating = false; // Animation status
    private float animationProgress = 0f; // Animation progress
    private float animationDuration = 0.5f; // Animation duration
    private int targetX, targetY; // Target position
    private boolean returningToOriginal = false; // Is moving back to original position

    private Griever griever; // Reference to the Griever
    private boolean isGrieverDead = false;
    private Vector2 keySpawnPosition = null;
    private Player player;
    private HUD hud;
    private float lastLifeResetTime = 0f; // Timer for last life reset
    private static final float LIFE_RESET_DELAY = 5f; // 5 seconds delay between life resets
    private boolean isLifeResetting = false; // Flag to check if life is being reset


    public Wall(int x, int y, String direction, Griever griever, Player player, HUD hud) {
        this.x = x;
        this.y = y;
        this.originalX = x;
        this.originalY = y;
        this.direction = direction;
        this.griever = griever;
        this.player = player;
        this.hud = hud;
    }

    public void update(float delta, float globalTimer, TiledMapTileLayer layer) {
        float timeSinceLastMove = globalTimer - lastMoveTime;

        if (isAnimating) {
            animate(delta, layer); // Handle animation
            return;
        }

        if (timeSinceLastMove >= MOVE_INTERVAL) {
            if (returningToOriginal) {
                moveToOriginal(layer); // Move back to original position
            } else {
                move(layer); // Move to target position
            }
            lastMoveTime = globalTimer;
        }

        // Check if the player is trapped by the wall. This should occur whether or not animation is still happening.
        checkAndMoveGriever();
        reducePlayerLife(delta);
    }



    private void move(TiledMapTileLayer layer) {
        TiledMapTileLayer.Cell cell = layer.getCell(x, y);
        if (cell == null) return;

        layer.setCell(x, y, null); // Remove from current position

        targetX = x;
        targetY = y;

        switch (direction) {
            case "left": targetX = x - 1; break;
            case "right": targetX = x + 1; break;
            case "up": targetY = y + 1; break;
            case "down": targetY = y - 1; break;
        }



        // Set wall at the target position
        layer.setCell(targetX, targetY, cell);
        isAnimating = true; // Start animation
        animationProgress = 0f; // Reset animation progress
        returningToOriginal = false; // Reset return status
    }

    private void moveToOriginal(TiledMapTileLayer layer) {
        TiledMapTileLayer.Cell cell = layer.getCell(targetX, targetY);
        if (cell == null) return;

        layer.setCell(targetX, targetY, null); // Remove from target position
        layer.setCell(originalX, originalY, cell); // Move back to original position

        isAnimating = true; // Start animation
        animationProgress = 0f; // Reset animation progress
        returningToOriginal = true; // Mark as returning
    }

    private void checkAndMoveGriever() {
        boolean isWallMoved = (x != originalX || y != originalY || (isAnimating && (targetX != originalX || targetY != originalY)));

        float grieverX = griever.getMonsterX();
        float grieverY = griever.getMonsterY();

        boolean isGrieverInBounds1 = (grieverX >= 174 && grieverX <= 206 && grieverY >= 272 && grieverY <= 300);
        boolean isGrieverInBounds2 = (grieverY >= 271 && grieverY <= 302 && grieverX >= 96 && grieverX <= 124);

        if (isWallMoved && (isGrieverInBounds1 || isGrieverInBounds2)) {
            if (isGrieverInBounds1 && !isGrieverDead) {
                keySpawnPosition = new Vector2(189, 286);
            } else if (isGrieverInBounds2 && !isGrieverDead) {
                keySpawnPosition = new Vector2(110, 286);
            }

            if (!isGrieverDead) {
                griever.setPosition(-1000, -1000);
                isGrieverDead = true;
            }

        }
    }

    public void reducePlayerLife(float delta) {
        // Check if the player is in bounds and the wall has moved
        boolean isWallMoved = (x != originalX || y != originalY || (isAnimating && (targetX != originalX || targetY != originalY)));
        float playerX = player.getX();
        float playerY = player.getY();

        boolean isPlayerInBounds1 = (playerX >= 174 && playerX <= 206 && playerY >= 272 && playerY <= 300);
        boolean isPlayerInBounds2 = (playerY >= 271 && playerY <= 302 && playerX >= 96 && playerX <= 124);
        boolean isPlayerInBounds = isPlayerInBounds1 || isPlayerInBounds2;

        // Only proceed if the player is in bounds, the wall has moved, and a reset is not already in progress
        if (isPlayerInBounds && isWallMoved && !isLifeResetting) {
            int currentLives = hud.getLives();

            // Decrement the life after 5 seconds and prevent further resets during this period
            if (lastLifeResetTime >= LIFE_RESET_DELAY) {
                if (currentLives == 3) {
                    hud.setLives(2); // Decrement life from 3 to 2
                    isLifeResetting = true; // Block further resets during this period
                } else if (currentLives == 2) {
                    hud.setLives(1); // Decrement life from 2 to 1
                    isLifeResetting = true; // Block further resets during this period
                } else if (currentLives == 1) {
                    hud.setLives(0); // Decrement life from 1 to 0
                    isLifeResetting = true; // Block further resets during this period
                }

                // Reset the timer after decrementing life
                lastLifeResetTime = 0f;
            }
        }

        // Allow resets again after the 5-second delay
        if (isLifeResetting && lastLifeResetTime >= LIFE_RESET_DELAY) {
            isLifeResetting = false; // Allow life decrement again
        }

        // Update the timer with delta time
        lastLifeResetTime += delta;
    }







    private void animate(float delta, TiledMapTileLayer layer) {
        animationProgress += delta / animationDuration;

        if (animationProgress >= 1f) {
            if (returningToOriginal) {
                // Finish returning to original position animation
                x = originalX;
                y = originalY;
                returningToOriginal = false; // Mark return complete
                isAnimating = false;

                // After returning, immediately update the state

            } else {
                // Finish moving to target position animation
                x = targetX;
                y = targetY;
                isAnimating = false;
                returningToOriginal = true; // Next move will be a return


            }
            animationProgress = 0f; // Reset progress
        }
    }


    public int getX() {
        return x;
    }

    public boolean isGrieverDead() {
        return isGrieverDead;
    }

    public boolean isWallMoved() {
        return (x != originalX || y != originalY || (isAnimating && (targetX != originalX || targetY != originalY)));
    }

    public Vector2 getKeySpawnPosition() {
        return keySpawnPosition;
    }




}