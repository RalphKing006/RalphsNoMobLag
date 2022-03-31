package com.zenya.nomoblag.scheduler;

import com.zenya.nomoblag.NoMobLag;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class TrackTPSTask implements NMLTask {

    private static TrackTPSTask nmlTask;
    private BukkitTask bukkitTask;
    private float instTps = 0;
    private float avgTps = 0;

    public TrackTPSTask() {
        runTask();
    }

    @Override
    public TaskKey getKey() {
        return TaskKey.TRACK_TPS_TASK;
    }

    @Override
    public void runTask() {
        bukkitTask = new BukkitRunnable() {
            long start = 0;
            long now = 0;

            @Override
            public void run() {
                start = now;
                now = System.currentTimeMillis();
                long tdiff = now - start;

                if (tdiff > 0) {
                    instTps = (float) (1000 / tdiff);
                }
            }
        }.runTaskTimer(NoMobLag.instance(), 0, 1);

        //Task to populate avgTps
        new BukkitRunnable() {
            ArrayList<Float> tpsList = new ArrayList<>();

            @Override
            public void run() {
                Float totalTps = 0f;

                tpsList.add(instTps);
                //Remove old tps after 15s
                if (tpsList.size() >= 15) {
                    tpsList.remove(0);
                }
                for (Float f : tpsList) {
                    totalTps += f;
                }
                avgTps = totalTps / tpsList.size();
            }
        }.runTaskTimerAsynchronously(NoMobLag.instance(), 20, 20);
    }

    @Override
    public BukkitTask getTask() {
        return bukkitTask;
    }

    public float getInstantTps() {
        if (instTps > 20.0f) {
            instTps = 20.0f;
        }
        return instTps;
    }

    public float getAverageTps() {
        if (avgTps > 20.0f) {
            avgTps = 20.0f;
        }
        return avgTps;
    }

    public static TrackTPSTask getInstance() {
        if (nmlTask == null) {
            nmlTask = new TrackTPSTask();
        }
        return nmlTask;
    }
}
