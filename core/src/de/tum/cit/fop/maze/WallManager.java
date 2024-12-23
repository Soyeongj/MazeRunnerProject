package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class WallManager {
    private List<Wall> walls;

    public WallManager() {
        this.walls = new ArrayList<>();
    }

    public void addYWalls(float x, float yStart, float yEnd, float step, float width, float height,String texturePath) {
        for (float y = yStart; y <= yEnd; y += step) {
            walls.add(new Wall(texturePath, x, y, width, height, texturePath != null)); // No texture
        }
    }


    public void addXWalls(float y, float xStart, float xEnd, float step, float width, float height, String texturePath) {
        for (float x = xStart; x <= xEnd; x += step) {
            walls.add(new Wall(texturePath, x, y, width, height, texturePath != null));
        }
    }

    // Render all walls
    public void render(SpriteBatch batch) {
        for (Wall wall : walls) {
            if (wall.isRenderable()) {
                batch.draw(wall.getTexture(), wall.getBrickRect().x, wall.getBrickRect().y,
                        wall.getBrickRect().width, wall.getBrickRect().height);
            }
        }
    }

    public List<Wall> getWalls() {
        return walls;
    }

    // Dispose all wall textures
    public void dispose() {
        for (Wall wall : walls) {
            wall.dispose();
        }
    }
}
