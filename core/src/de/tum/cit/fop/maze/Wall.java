package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Wall {
    private Texture texture;
    private Rectangle brickRect;

    public Wall(String texturePath, float x, float y) {
        this.texture = new Texture(texturePath);
        this.brickRect = new Rectangle(x,y,texture.getWidth(),texture.getHeight());
    }
    public void render(SpriteBatch batch) {
        batch.draw(texture, brickRect.x, brickRect.y);
    }
    public Rectangle getBrickRect() {
        return brickRect;
    }
    public void dispose() {
        texture.dispose();
    }
}
