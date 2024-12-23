package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Wall {
    private Texture texture;
    private Rectangle brickRect;
    private boolean renderable;

    public Wall(String texturePath, float x, float y, float width, float height, boolean renderable) {
        this.renderable = renderable;
        if (renderable && texturePath != null) {
            this.texture = new Texture(texturePath);
        }
        this.brickRect = new Rectangle(x, y, width, height);
    }

    public Rectangle getBrickRect() {
        return brickRect;
    }

    public Texture getTexture() {
        return texture;
    }

    public boolean isRenderable() {
        return renderable;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
