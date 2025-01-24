package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import static com.badlogic.gdx.Gdx.audio;
/**
 * Manages all sound and music assets for the game, including background music, sound effects, and their playback.
 * This class handles initialization, playback, and disposal of sounds and music files.
 */
public class SoundManager {
    private static Sound rockSound;
    private static Sound lostFreindSound;
    private static Sound saveFriendSound;
    private static Sound monsterDiedSound;
    private static Sound keyCollectedSound;
    private static Sound itemCollectedSound;
    private static Sound gameOverSound;
    private static Sound victorySound;
    private static Sound evilLaughSound;
    private static Music backgroundMusic;
    private static Music menuMusic;
    private static Sound gameStartSound;

    /**
     * Initializes the sound and music assets by loading them from the resources folder.
     * This method sets up the sounds for various events and the background music.
     */
    public static void initialize() {
        rockSound = audio.newSound(Gdx.files.internal("assets/rockSound.mp3"));
        lostFreindSound = audio.newSound(Gdx.files.internal("assets/lostFriendSound.mp3"));
        saveFriendSound = audio.newSound(Gdx.files.internal("assets/saveFriendSound.mp3"));
        monsterDiedSound = audio.newSound(Gdx.files.internal("assets/monsterDiedSound.mp3"));
        keyCollectedSound = audio.newSound(Gdx.files.internal("assets/keyCollectedSound.mp3"));
        itemCollectedSound = audio.newSound(Gdx.files.internal("assets/itemCollectedSound.mp3"));
        gameOverSound = audio.newSound(Gdx.files.internal("assets/gameOverSound.mp3"));
        victorySound = audio.newSound(Gdx.files.internal("assets/victorySound.mp3"));
        evilLaughSound = audio.newSound(Gdx.files.internal("assets/evillaughSound.wav"));
        backgroundMusic = audio.newMusic(Gdx.files.internal("assets/backgroundMusic.wav"));
        menuMusic = audio.newMusic(Gdx.files.internal("assets/menuMusic.mp3"));
        gameStartSound = audio.newSound(Gdx.files.internal("assets/gameStartSound.mp3"));
        // Set looping for background and menu music
        backgroundMusic.setLooping(true);
        menuMusic.setLooping(true);
    }

    /**
     * Plays the sound associated with a falling rock.
     */
    public static void playRockSound() {
        if (rockSound != null) {
            rockSound.play();
        }
    }

    /**
     * Plays the sound when a friend is lost.
     */
    public static void playLostFriendSound() {
        if (lostFreindSound != null) {
            lostFreindSound.play();
        }
    }

    /**
     * Plays the sound when a monster dies.
     */
    public static void playMonsterDiedSound() {
        if (monsterDiedSound != null) {
            monsterDiedSound.play();
        }
    }

    /**
     * Plays the sound when a key is collected.
     */
    public static void playKeyCollectedSound() {
        if (keyCollectedSound != null) {
            keyCollectedSound.play();
        }
    }

    /**
     * Plays the sound when the game is over.
     */
    public static void playGameOverSound() {
        if (gameOverSound != null) {
            gameOverSound.play();
        }
    }
    /**
     * Plays the sound when a friend is saved.
     */
    public static void playSaveFriendSound() {
        if (saveFriendSound != null) {
            saveFriendSound.play();
        }
    }

    /**
     * Plays the sound when an item is collected.
     */
    public static void playItemCollectedSound() {
        if (itemCollectedSound != null) {
            itemCollectedSound.play();
        }
    }

    /**
     * Plays the sound when the player wins the game.
     */
    public static void playVictorySound() {
        if (victorySound != null) {
            victorySound.play();
        }
    }

    /**
     * Plays the evil laugh sound, typically triggered by an enemy or an event.
     */
    public static void playEvilLaughSound() {
        if (evilLaughSound != null) {
            evilLaughSound.play();
        }
    }

    /**
     * Plays the background music, if it is not already playing.
     */
    public static void playBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    /**
     * Stops the background music if it is currently playing.
     */
    public static void stopBackgroundMusic() {
        if(backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    /**
     * Plays the menu music, if it is not already playing.
     */
    public static void playMenuMusic() {
        if (menuMusic != null && !menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    /**
     * Stops the menu music if it is currently playing.
     */
    public static void stopMenuMusic() {
        if(menuMusic != null) {
            menuMusic.stop();
        }
    }

    /**
     * Plays the sound at the start of the game.
     */
    public static void playGameStartSound() {
        if (gameStartSound != null) {
            gameStartSound.play();
        }
    }

    /**
     * Disposes of all sound and music resources to free up memory.
     * This method should be called when the sound manager is no longer needed.
     */
    public static void dispose() {
        if (rockSound != null) {
            rockSound.dispose();
        }
        if (lostFreindSound != null) {
            lostFreindSound.dispose();
        }
        if (saveFriendSound != null) {
            saveFriendSound.dispose();
        }
        if (monsterDiedSound != null) {
            monsterDiedSound.dispose();
        }
        if (keyCollectedSound != null) {
            keyCollectedSound.dispose();
        }
        if (itemCollectedSound != null) {
            itemCollectedSound.dispose();
        }
        if (gameOverSound != null) {
            gameOverSound.dispose();
        }
        if (victorySound != null) {
            victorySound.dispose();
        }
        if (evilLaughSound != null) {
            evilLaughSound.dispose();
        }
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
        if (menuMusic != null) {
            menuMusic.dispose();
        }
        if (gameStartSound != null) {
            gameStartSound.dispose();
        }
    }
}