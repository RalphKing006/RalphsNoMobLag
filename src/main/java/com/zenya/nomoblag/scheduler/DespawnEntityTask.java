package com.zenya.nomoblag.scheduler;

import com.zenya.nomoblag.NoMobLag;
import com.zenya.nomoblag.file.ConfigManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DespawnEntityTask implements NMLTask {

    private BukkitTask bukkitTask;
    private LivingEntity entity;

    public DespawnEntityTask(LivingEntity entity) {
        this.entity = entity;
        runTask();
    }

    @Override
    public TaskKey getKey() {
        return TaskKey.DESPAWN_ENTITY_TASK;
    }

    //Task cannot be async since 1.17
    @Override
    public void runTask() {
        int rate = ConfigManager.getInstance().getInt("spawners.mob-despawn-rate");

        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                entity.remove();
            }
        }.runTaskLater(NoMobLag.instance(), 20 * rate);
    }

    @Override
    public BukkitTask getTask() {
        return bukkitTask;
    }
}
