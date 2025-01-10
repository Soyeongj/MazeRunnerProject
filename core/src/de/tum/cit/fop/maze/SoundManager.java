package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import static com.badlogic.gdx.Gdx.audio;

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

        backgroundMusic.setLooping(true);
        menuMusic.setLooping(true);
    }

    public static void playRockSound() {
        if (rockSound != null) {
            rockSound.play();
        }
    }

    public static void playLostFriendSound() {
        if (lostFreindSound != null) {
            lostFreindSound.play();
        }
    }

    public static void playMonsterDiedSound() {
        if (monsterDiedSound != null) {
            monsterDiedSound.play();
        }
    }

    public static void playKeyCollectedSound() {
        if (keyCollectedSound != null) {
            keyCollectedSound.play();
        }
    }

    public static void playGameOverSound() {
        if (gameOverSound != null) {
            gameOverSound.play();
        }
    }

    public static void playSaveFriendSound() {
        if (saveFriendSound != null) {
            saveFriendSound.play();
        }
    }
    public static void playItemCollectedSound() {
        if (itemCollectedSound != null) {
            itemCollectedSound.play();
        }
    }
    public static void playVictorySound() {
        if (victorySound != null) {
            victorySound.play();
        }
    }
    public static void playEvilLaughSound() {
        if (evilLaughSound != null) {
            evilLaughSound.play();
        }
    }
    public static void playBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }
    public static void stopBackgroundMusic() {
        if(backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public static void playMenuMusic() {
        if (menuMusic != null && !menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }
    public static void stopMenuMusic() {
        if(menuMusic != null) {
            menuMusic.stop();
        }
    }
    public static void playGameStartSound() {
        if (gameStartSound != null) {
            gameStartSound.play();
        }
    }


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