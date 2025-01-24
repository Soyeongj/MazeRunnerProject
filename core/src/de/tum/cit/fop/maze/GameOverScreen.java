package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;

/**
 * Represents the screen displayed when the player loses the game.
 * This screen shows a "Game Over" background image and the player's final score.
 *
 * Extends the {@link AbstractGameScreen} to inherit common screen functionalities such as rendering and input handling.
 *
 */
public class GameOverScreen extends AbstractGameScreen {

    /**
     * Constructs a GameOverScreen instance with the specified game instance and final score.
     *
     * @param game      the instance of {@link MazeRunnerGame} managing the game
     * @param finalTime the player's final score or completion time
     */
    public GameOverScreen(MazeRunnerGame game, float finalTime) {
        super(game, new Texture("game-over.jpg"), finalTime);
    }
}
