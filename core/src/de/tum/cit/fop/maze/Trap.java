package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Player;

public class Trap {
    private Vector2 position;
    private Rectangle bounds;
    private float LivesCoolDown = 0f;
    private float needleRenderTime = 0f;
    private boolean needlesVisible = false;
    private Texture needleTexture;

    public Trap(float x, float y, float width, float height,String needleTexture) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
        this.needleTexture = new Texture("spike.png");
    }

    // Check if the player is exactly inside the trap bounds
    public boolean isPlayerOnTrap(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }

    public void test(Vector2 playerPosition, HUD hud, Player player, float delta) {
        if (LivesCoolDown <= 0 &&isPlayerOnTrap(playerPosition)) {

            if ( hud.getLives() > 1) {
                hud.decrementLives();
                player.triggerRedEffect();
                LivesCoolDown = 7;
                needlesVisible = true;
                needleRenderTime = 3f;
            } else {
                hud.setLives(0);
                player.setDead();
            }
        }
        if (LivesCoolDown > 0) {
            LivesCoolDown -= delta;
        }
        if (needleRenderTime > 0) {
            needleRenderTime -= delta;
            if (needleRenderTime <= 0) {
                needlesVisible = false;
            }
        }
    }
    public void renderNeedles(SpriteBatch batch) {
        if (needlesVisible) {
            batch.draw(needleTexture, position.x, position.y, bounds.width, bounds.height);
        }
    }

    public void dispose() {
        needleTexture.dispose();
    }


}