package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
/**
 * Represents the screen displayed when the player successfully completes the game.
 * This screen shows a background image and the player's final score.
 *
 * Extends the {@link AbstractGameScreen} to inherit common screen functionalities such as rendering and input handling.
 *
 */
public class GameClearScreen extends AbstractGameScreen {

    /**
     * Constructs a GameClearScreen instance with the specified game instance and final score.
     *
     * @param game      the instance of {@link MazeRunnerGame} managing the game
     * @param finalTime the player's final score or completion time
     */
    public GameClearScreen(MazeRunnerGame game, float finalTime) {
        super(game, new Texture("desert.png"), finalTime);

    }
}