package com.zenya.nomoblag.scheduler;

import java.util.HashMap;

public class TaskManager {
    private static TaskManager taskManager;
    private HashMap<TaskKey, NMLTask> taskMap = new HashMap<TaskKey, NMLTask>();

    public TaskManager() {
        registerTask(TrackTPSTask.getInstance().getKey(), TrackTPSTask.getInstance());
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

