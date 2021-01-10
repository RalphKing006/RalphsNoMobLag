package com.zenya.nomoblag.event;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public class PlayerChunkChangeEvent extends Event implements Cancellable {
    private boolean isCancelled;
    private Player player;
    private Chunk chunk;

    public PlayerChunkChangeEvent(Player player, Chunk chunk) {
        this.isCancelled = false;
        this.player = player;
        this.chunk = chunk;
    }

    public Player getPlayer() {
        return player;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public Chunk getPreviousChunk() {
        double relativeX = player.getVelocity().getX();
        double relativeZ = player.getVelocity().getZ();
        if(relativeX > 0) {
            relativeX = 1;
        } else {
            relativeX = -1;
        }
        if(relativeZ > 0) {
            relativeZ = 1;
        } else {
            relativeZ = -1;
        }

        return chunk.getWorld().getChunkAt(chunk.getX() + (int) relativeX, chunk.getZ() + (int) relativeZ);
    }

    public Chunk[] getNearbyChunks(int radius) {
        ArrayList<Chunk> nearbyChunks = new ArrayList<Chunk>();
        int cX = chunk.getX();
        int cZ = chunk.getZ();

        for(int x=cX-radius; x<=cX+radius; x++) {
            for(int z=cZ-radius; z<=cZ+radius; z++) {
                nearbyChunks.add(chunk.getWorld().getChunkAt(x, z));
            }
        }
        return nearbyChunks.toArray(new Chunk[nearbyChunks.size()]);
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    //Default custom event methods
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}