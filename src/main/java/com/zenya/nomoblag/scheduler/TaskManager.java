package com.zenya.nomoblag.scheduler;

import java.util.HashMap;

public class TaskManager {
    private static TaskManager taskManager;
    private HashMap<TaskKey, NMLTask> taskMap = new HashMap<TaskKey, NMLTask>();

    public TaskManager() {
        registerTask(TaskKey.TRACK_TPS_TASK, TrackTPSTask.getInstance());
        registerTask(TaskKey.TRACK_PLAYER_TASK, TrackPlayerTask.getInstance());
    }

    public NMLTask getTask(TaskKey key) {
        return taskMap.getOrDefault(key, null);
    }

    public void registerTask(TaskKey key, NMLTask task) {
        taskMap.put(key, task);
    }

    public void unregisterTask(TaskKey key) {
        taskMap.remove(key);
    }

    public static TaskManager getInstance() {
        if(taskManager == null) {
            taskManager = new TaskManager();
        }
        return taskManager;
    }
}

