package com.webank.wecross.polling;

import org.quartz.Job;

public interface TaskFactory {
    /**
     * load tasks for quartz scheduler
     *
     * @param contexts context for each task
     * @param dataKey key to store data
     * @param jobType job type
     * @return task list
     */
    <T extends Job> Task[] load(Object[] contexts, String dataKey, Class<T> jobType);
}
