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
        bukkitTask = new BukkitRunnable() {
            int limit = ConfigManager.getInstance().getInt("mob-freezing.disable-ai-after");
            int count = 0;
            @Override
            public void run() {
                if(count >= limit) {
                    entity.setAI(false);
                    this.cancel();
                }
                count++;
            }
        }.runTaskTimer(NoMobLag.getInstance(), 0, 20);
    }

    @Override
    public BukkitTask getTask() {
        return bukkitTask;
    }
}
