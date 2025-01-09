package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;

public class GameOverScreen extends AbstractGameScreen {

    public GameOverScreen(MazeRunnerGame game, float finalTime) {
        super(game, new Texture("gameover.jpg"), finalTime);
    }

}