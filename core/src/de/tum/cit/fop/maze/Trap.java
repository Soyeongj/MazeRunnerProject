package de.tum.cit.fop.maze;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.audio.Sound;

import java.util.TimerTask;


public class Trap {
    private Vector2 position;
    private Rectangle bounds;
    private float livesCoolDown = 0f;
    private float rockFallDuration = 0f;
    private boolean isRockFalling = false;
    private Texture rockTexture;
    private Vector2 rockPosition;
    private float rockStartY;
    private static final float fallSpeed = 300f;
    private static final float fallHeight = 70f;

    public Trap(float x, float y, float width, float height, String rockTexturePath) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
        this.rockTexture = new Texture(rockTexturePath);
        this.rockPosition = new Vector2(x, y + fallHeight);
        this.rockStartY = y + fallHeight;
    }

    public boolean isPlayerOnTrap(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }

    public void test(Vector2 playerPosition, HUD hud, Player player, float delta, Friends friends) {  // Add Friends parameter
        if (livesCoolDown <= 0 && isPlayerOnTrap(playerPosition) && !isRockFalling) {
            isRockFalling = true;
            rockFallDuration = 0f;
            rockPosition.y = rockStartY;
        }

        if (isRockFalling) {
            rockFallDuration += delta;
            rockPosition.y = rockStartY - (fallSpeed * rockFallDuration);

            if (rockPosition.y <= position.y) {
                rockPosition.y = position.y;
                isRockFalling = false;

                if (isPlayerOnTrap(playerPosition)) {
                    if (hud.getLives() > 1) {
                        // Try to remove a friend first
                        if (friends.removeLastSavedFriend()) {
                            // Only decrement lives if no friend was available to remove
                            player.triggerRedEffect();
                            hud.decrementLives();
                            player.triggerRedEffect();
                        }


                        livesCoolDown = 3;
                    } else {
                        hud.setLives(0);
                        player.setDead();
                    }
                }

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        rockPosition.y = rockStartY;
                    }
                }, 0.5f);
            }
        }

        if (livesCoolDown > 0) {
            livesCoolDown -= delta;
        }
    }

    public void render(SpriteBatch batch) {
        if (isRockFalling || rockPosition.y != rockStartY) {
            batch.draw(rockTexture, rockPosition.x, rockPosition.y, bounds.width, bounds.height);
        }
    }

    public void dispose() {
        rockTexture.dispose();
    }
}