package com.zenya.nomoblag.scheduler;

import org.bukkit.scheduler.BukkitTask;

public interface NMLTask {

    TaskKey getKey();

    void runTask();

    BukkitTask getTask();

    static NMLTask getInstance() {
        return null;
    }
}
