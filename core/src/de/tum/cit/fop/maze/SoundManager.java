package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    private static Sound rockSound;
    private static Sound lostFreindSound;

    // 초기화: 필요한 모든 사운드 로드
    public static void initialize() {
        rockSound = Gdx.audio.newSound(Gdx.files.internal("assets/rockSound.mp3"));
        lostFreindSound = Gdx.audio.newSound(Gdx.files.internal("assets/lostFriendSound.mp3"));
    }

    // 돌 소리 재생
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

    // 리소스 해제
    public static void dispose() {
        if (rockSound != null) {
            rockSound.dispose();
        }
        if (lostFreindSound != null) {
            lostFreindSound.dispose();
        }
    }
}
