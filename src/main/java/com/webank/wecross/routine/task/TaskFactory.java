package com.webank.wecross.routine.task;

public interface TaskFactory {
    /**
     * load tasks for quartz scheduler
     *
     * @param contexts context for each task
     * @return task list
     */
    Task[] load(Object[] contexts);
}
