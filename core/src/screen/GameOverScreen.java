package screen;

import com.badlogic.gdx.graphics.Texture;
import game.MazeRunnerGame;

public class GameOverScreen extends AbstractGameScreen {

    public GameOverScreen(MazeRunnerGame game, float finalTime) {
        super(game, new Texture("screen/gameover.jpg"), finalTime);
    }

}
