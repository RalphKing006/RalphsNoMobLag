package com.zenya.nomoblag.scheduler;

import com.zenya.nomoblag.NoMobLag;
import com.zenya.nomoblag.file.ConfigManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class FreezeEntityTask implements NMLTask {

    private BukkitTask bukkitTask;
    private LivingEntity entity;

    public FreezeEntityTask(LivingEntity entity) {
        this.entity = entity;
        runTask();
    }

    @Override
    public TaskKey getKey() {
        return TaskKey.FREEZE_ENTITY_TASK;
    }

    @Override
    public void runTask() {
        int rate = ConfigManager.getInstance().getInt("mob-freezing.disable-ai-after");
        entity.setAI(true);

        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                entity.setAI(false);
            }
        }.runTaskLaterAsynchronously(NoMobLag.getInstance(), 20 * rate);
    }

    @Override
    public BukkitTask getTask() {
        return bukkitTask;
    }
}
