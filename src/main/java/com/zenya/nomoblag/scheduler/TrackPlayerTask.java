package com.zenya.nomoblag.scheduler;

import com.zenya.nomoblag.NoMobLag;
import com.zenya.nomoblag.event.PlayerChunkChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class TrackPlayerTask implements NMLTask {

  private static TrackPlayerTask nmlTask;
  private BukkitTask task;
  private CompletableFuture<HashMap<Player, Location>> playerCoords;

  public TrackPlayerTask() {
    playerCoords = CompletableFuture.supplyAsync(() -> {
      return new HashMap<>();
    });
    runTask();
  }

  @Override
  public TaskKey getKey() {
    return TaskKey.TRACK_PLAYER_TASK;
  }

  @Override
  public void runTask() {
    //Task to fire events
    task = new BukkitRunnable() {
      @Override
      public void run() {
        playerCoords.thenAcceptAsync(coordMap -> {
          if (Bukkit.getOnlinePlayers() != null && Bukkit.getOnlinePlayers().size() != 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
              //Chunk change event
              if (player.getLocation().getChunk().getX() != coordMap.getOrDefault(player, new Location(player.getWorld(), 0, 0, 0)).getChunk().getX() || player.getLocation().getChunk().getZ() != coordMap.getOrDefault(player, new Location(player.getWorld(), 0, 0, 0)).getChunk().getZ()) {
                new BukkitRunnable() {
                  @Override
                  public void run() {
                    Bukkit.getPluginManager().callEvent(new PlayerChunkChangeEvent(player));
                  }
                }.runTask(NoMobLag.getInstance());
                //Force update
                coordMap.put(player, player.getLocation());
              }
            }
          }
        });
      }
    }.runTaskTimerAsynchronously(NoMobLag.getInstance(), 0, 20 / 2);

    //Task to initialise player
    BukkitTask task2 = new BukkitRunnable() {
      public void run() {
        playerCoords.thenAcceptAsync(coordMap -> {
          if (Bukkit.getOnlinePlayers() != null && Bukkit.getOnlinePlayers().size() != 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
              if (!coordMap.containsKey(player)) {
                coordMap.put(player, new Location(player.getWorld(), 0, 0, 0));
              }
            }
          }
        });
      }
    }.runTaskTimerAsynchronously(NoMobLag.getInstance(), 0, 20 * 1);

    //Task to update player location
    BukkitTask task3 = new BukkitRunnable() {
      @Override
      public void run() {
        playerCoords.thenAcceptAsync(coordMap -> {
          if (Bukkit.getOnlinePlayers() != null && Bukkit.getOnlinePlayers().size() != 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
              coordMap.put(player, player.getLocation());
            }
          }
        });
      }
    }.runTaskTimerAsynchronously(NoMobLag.getInstance(), 0, 20 * 5);

    //Task to remove old player entries
    BukkitTask task4 = new BukkitRunnable() {
      @Override
      public void run() {
        playerCoords.thenAcceptAsync(coordMap -> {
          if (Bukkit.getOnlinePlayers() != null && Bukkit.getOnlinePlayers().size() != 0) {
            if (coordMap.keySet() != null && coordMap.keySet().size() != 0) {
              for (Player player : coordMap.keySet()) {
                if (!Bukkit.getOnlinePlayers().contains(player)) {
                  coordMap.remove(player);
                }
              }
            }
          } else {
            coordMap.clear();
          }
        });
      }
    }.runTaskTimerAsynchronously(NoMobLag.getInstance(), 0, 20 * 10);
  }

  @Override
  public BukkitTask getTask() {
    return task;
  }

  public static TrackPlayerTask getInstance() {
    if (nmlTask == null) {
      nmlTask = new TrackPlayerTask();
    }
    return nmlTask;
  }
}
