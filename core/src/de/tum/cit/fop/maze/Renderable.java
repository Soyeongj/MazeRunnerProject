package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

interface Renderable {
     void render(SpriteBatch batch);

     void dispose();

}