package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;

public class GameClearScreen extends AbstractGameScreen {

    public GameClearScreen(MazeRunnerGame game, float finalTime) {
        super(game, new Texture("desert.png"), finalTime);

        this.textureWidthScale = 6.15f;
        this.textureHeightScale = 4.0f;
    }
}
