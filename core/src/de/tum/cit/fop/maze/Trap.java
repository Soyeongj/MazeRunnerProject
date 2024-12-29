package de.tum.cit.fop.maze;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Player;

public class Trap {
    private Vector2 position;
    private Rectangle bounds;
    private float LivesCoolDown = 0f;

    public Trap(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
    }

    // Check if the player is exactly inside the trap bounds
    public boolean isPlayerOnTrap(Vector2 playerPosition) {
        return bounds.contains(playerPosition.x, playerPosition.y);
    }

    // Trigger the trap and make the tile blockable
    public void test(Vector2 playerPosition, HUD hud, Player player, float delta) {
        if (LivesCoolDown <= 0 &&isPlayerOnTrap(playerPosition)) {
            if ( hud.getLives() > 1) {
                hud.decrementLives();
                player.triggerRedEffect();
                LivesCoolDown = 7;
            } else {
                hud.setLives(0);
                player.setDead();
            }
        }
        if (LivesCoolDown > 0) {
            LivesCoolDown -= delta;
        }
    }
}
