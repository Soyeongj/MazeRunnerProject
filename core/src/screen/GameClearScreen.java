package screen;

import com.badlogic.gdx.graphics.Texture;
import game.MazeRunnerGame;

public class GameClearScreen extends AbstractGameScreen {

    public GameClearScreen(MazeRunnerGame game, float finalTime) {
        super(game, new Texture("screen/desert.png"), finalTime);

        this.textureWidthScale = 6.15f;
        this.textureHeightScale = 4.0f;
    }
}
