package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Item extends CollectibleItem {
    private static final String PREFERENCES_NAME = "ItemState";

    public Item() {
        super(new Texture[] {
                new Texture("potion_red.png"),
                new Texture("potion_red.png"),
                new Texture("potion_red.png")
        }, new Vector2[] {
                new Vector2(120, 183),
                new Vector2(110, 256),
                new Vector2(220, 283)
        });
    }
    @Override
    protected void onCollected() {
        SoundManager.playItemCollectedSound();
    }
    public void update(Player player, float interactionRadius) {
        int collectedItems = checkAndCollectAll(new Vector2(player.getX(), player.getY()), interactionRadius);
        for (int i = 0; i < collectedItems; i++) {
            player.increaseSpeed(3f);
        }
    }

    public void saveItemState() {
        saveState(Gdx.app.getPreferences(PREFERENCES_NAME), "Item");
    }

    public void loadItemState() {
        loadState(Gdx.app.getPreferences(PREFERENCES_NAME), "Item");
    }
}
