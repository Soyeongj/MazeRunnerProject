package de.tum.cit.fop.maze;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

public class Trap implements NearbyPlayer, Renderable {
    private Vector2 position;
    private Rectangle bounds;
    private float livesCoolDown = 0f;
    private float rockFallDuration = 0f;
    public boolean isRockFalling = false;
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

    @Override
    public boolean isPlayerNearby(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }

    public void fallRock(Vector2 playerPosition, HUD hud, Player player, float delta,Friends friends) {
        if (livesCoolDown <= 0 && isPlayerNearby(playerPosition) && !isRockFalling) {
            isRockFalling = true;
            rockFallDuration = 0f;
            rockPosition.y = rockStartY;

            SoundManager.playRockSound();
        }

        if (isRockFalling) {
            rockFallDuration += delta;
            rockPosition.y = rockStartY - (fallSpeed * rockFallDuration);

            if (rockPosition.y <= position.y) {
                rockPosition.y = position.y;
                isRockFalling = false;


                if (isPlayerNearby(playerPosition)) {
                    if (hud.getLives() > 1) {
                        friends.removeFriendAt(friends.getFollowingFriendsPositions().size()-1);
                        hud.decrementLives();
                        player.triggerRedEffect();
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

    public void saveTrapState() {
        Preferences prefs = Gdx.app.getPreferences("trap");
        prefs.putFloat("positionX",position.x);
        prefs.putFloat("positionY",position.y);
        prefs.putFloat("lcd",livesCoolDown);
        prefs.putBoolean("isRockFalling",isRockFalling);
    }
    public void loadTrapState() {
        Preferences prefs = Gdx.app.getPreferences("trap");
        position.x = prefs.getFloat("positionX",position.x);
        position.y = prefs.getFloat("positionY",position.y);
        livesCoolDown = prefs.getFloat("lcd",livesCoolDown);
        isRockFalling = prefs.getBoolean("isRockFalling",isRockFalling);
    }


}