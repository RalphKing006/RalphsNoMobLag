package com.zenya.nomoblag.scheduler;

import com.zenya.nomoblag.NoMobLag;
import com.zenya.nomoblag.event.PlayerChunkChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class TrackPlayerChunkTask implements NMLTask {
    private static TrackPlayerChunkTask nmlTask;
    private BukkitTask bukkitTask;
    HashMap<Player, int[]> chunkCoords = new HashMap<>();

    public TrackPlayerChunkTask() {
        runTask();
    }

    @Override
    public TaskKey getKey() {
        return TaskKey.TRACK_PLAYER_CHUNK_TASK;
    }

    @Override
    public void runTask() {
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(Bukkit.getOnlinePlayers() != null && Bukkit.getOnlinePlayers().size() != 0) {
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        //Init player
                        if(!chunkCoords.containsKey(player)) {
                            chunkCoords.put(player, new int[]{0, 0});
                        }

                        //Check for chunk change
                        if(player.getLocation().getChunk().getX() != chunkCoords.get(player)[0] || player.getLocation().getChunk().getZ() != chunkCoords.get(player)[1]) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    //Call PlayerChunkChangeEvent
                                    Bukkit.getPluginManager().callEvent(new PlayerChunkChangeEvent(player));
                                }
                            }.runTask(NoMobLag.getInstance());
                        }

                        //Update hashmap
                        chunkCoords.put(player, new int[]{player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ()});
                    }
                }
            }
        }.runTaskTimerAsynchronously(NoMobLag.getInstance(), 0, 20*5);

        //Remove old entries in hashmap
        new BukkitRunnable() {
            @Override
            public void run() {
                if(chunkCoords.keySet() != null && chunkCoords.keySet().size() != 0) {
                    for(Player player : chunkCoords.keySet()) {
                        if(Bukkit.getOnlinePlayers() != null && Bukkit.getOnlinePlayers().size() != 0 && !Bukkit.getOnlinePlayers().contains(player)) {
                            chunkCoords.remove(player);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(NoMobLag.getInstance(), 0, 60*20);
    }

    @Override
    public BukkitTask getTask() {
        return bukkitTask;
    }

    public static TrackPlayerChunkTask getInstance() {
        if(nmlTask == null) {
            nmlTask = new TrackPlayerChunkTask();
        }
        return nmlTask;
    }
}

