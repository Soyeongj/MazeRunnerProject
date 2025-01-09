package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class TrapItem implements Renderable {

    private Texture[] trapItems = {
            new Texture("potion_red.png"),
            new Texture("potion_red.png"),
            new Texture("potion_red.png"),
    };

    private Vector2[] trapItemPositions = {
            new Vector2(150, 200),
            new Vector2(300, 350),
            new Vector2(450, 250),
    };

    private boolean[] isTrapItemCollected = {false, false, false};
    private float scale = 0.2f;
    private long fogEffectStartTime = 0;
    private boolean isFogActive = false;
    private static final long FOG_DURATION = 3000;

    @Override
    public void render(SpriteBatch batch) {
        for (int i = 0; i < trapItemPositions.length; i++) {
            if (!isTrapItemCollected[i]) {
                batch.draw(trapItems[i], trapItemPositions[i].x, trapItemPositions[i].y,
                        trapItems[i].getWidth() * scale, trapItems[i].getHeight() * scale);
            }
        }
    }

    public boolean checkAndCollectTrapItem(Vector2 playerPosition, float proximity, int index) {
        if (!isTrapItemCollected[index]) {
            float distance = playerPosition.dst(trapItemPositions[index]);
            if (distance <= proximity) {
                isTrapItemCollected[index] = true;
                trapItemPositions[index] = new Vector2(-1000, -1000); // Move offscreen
                activateFogEffect();
                return true;
            }
        }
        return false;
    }

    public int checkAndCollectAllTrapItems(Vector2 playerPosition, float proximity) {
        int count = 0;
        for (int i = 0; i < trapItemPositions.length; i++) {
            if (checkAndCollectTrapItem(playerPosition, proximity, i)) {
                count++;
            }
        }
        return count;
    }


    public void activateFogEffect() {
        isFogActive = true;
        fogEffectStartTime = TimeUtils.millis();
    }

    public void updateFogEffect() {
        if (isFogActive) {
            long currentTime = TimeUtils.millis();
            if (currentTime - fogEffectStartTime >= FOG_DURATION) {
                isFogActive = false;
            }
        }
    }

    public void update(Player player, float interactionRadius) {
        checkAndCollectAllTrapItems(new Vector2(player.getX(), player.getY()), interactionRadius);
        updateFogEffect();
    }

    public boolean isFogActive() {
        return isFogActive;
    }

    @Override
    public void dispose() {
        for (Texture trapItem : trapItems) {
            trapItem.dispose();
        }
    }

    public void saveTrapItemState() {
        Preferences preferences = Gdx.app.getPreferences("TrapItem");
        for (int i = 0; i < isTrapItemCollected.length; i++) {
            preferences.putBoolean("itemCollected" + i, isTrapItemCollected[i]);
            preferences.putFloat("itemPosX" + i, trapItemPositions[i].x);
            preferences.putFloat("itemPosY" + i, trapItemPositions[i].y);
        }
        preferences.flush();
    }

    public void loadTrapItemState() {
        Preferences preferences = Gdx.app.getPreferences("TrapItem");
        for (int i = 0; i < isTrapItemCollected.length; i++) {
            isTrapItemCollected[i] = preferences.getBoolean("itemCollected" + i, false);
            float x = preferences.getFloat("itemPosX" + i, trapItemPositions[i].x);
            float y = preferences.getFloat("itemPosY" + i, trapItemPositions[i].y);
            trapItemPositions[i] = new Vector2(x, y);
        }
    }
}