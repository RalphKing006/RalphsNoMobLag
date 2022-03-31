package com.zenya.nomoblag.event;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

public class PlayerChunkChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled;
    private Player player;

    public PlayerChunkChangeEvent(Player player) {
        this.isCancelled = false;
        this.player = player;
    }

    public Player player() {
        return player;
    }

    public Chunk chunk() {
        return player.getLocation().getChunk();
    }

    public World world() {
        return player.getWorld();
    }

    public Chunk previousChunk() {
        double relativeX = player.getVelocity().getX();
        double relativeZ = player.getVelocity().getZ();
        if (relativeX > 0) {
            relativeX = 1;
        } else {
            relativeX = -1;
        }
        if (relativeZ > 0) {
            relativeZ = 1;
        } else {
            relativeZ = -1;
        }

        return chunk().getWorld().getChunkAt(chunk().getX() + (int) relativeX, chunk().getZ() + (int) relativeZ);
    }

    public Chunk[] getNearbyChunks(int radius) {
        List<Chunk> nearbyChunks = new ArrayList<>();
        int cX = chunk().getX();
        int cZ = chunk().getZ();

        for (int x = cX - radius; x <= cX + radius; x++) {
            for (int z = cZ - radius; z <= cZ + radius; z++) {
                nearbyChunks.add(chunk().getWorld().getChunkAt(x, z));
            }
        }
        return nearbyChunks.toArray(Chunk[]::new);
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
