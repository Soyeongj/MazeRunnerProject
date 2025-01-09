package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class TrapItem extends CollectibleItem {
    private long fogEffectStartTime = 0;
    private boolean isFogActive = false;
    private static final long FOG_DURATION = 3000;

    public TrapItem() {
        super(new Texture[] {
                new Texture("potion_red.png"),
                new Texture("potion_red.png"),
                new Texture("potion_red.png")
        }, new Vector2[] {
                new Vector2(150, 200),
                new Vector2(300, 350),
                new Vector2(450, 250)
        });
    }
    @Override
    protected void onCollected() {
        activateFogEffect();
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
        checkAndCollectAll(new Vector2(player.getX(), player.getY()), interactionRadius);
        updateFogEffect();
    }

    public boolean isFogActive() {
        return isFogActive;
    }

    public void saveTrapItemState() {
        saveState(Gdx.app.getPreferences("TrapItem"), "TrapItem");
    }

    public void loadTrapItemState() {
        loadState(Gdx.app.getPreferences("TrapItem"), "TrapItem");
    }



}
