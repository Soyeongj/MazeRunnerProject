package de.tum.cit.fop.maze;

import com.badlogic.gdx.math.Vector2;

interface NearbyPlayer {
    boolean isPlayerNearby(Vector2 playerPosition);
}
