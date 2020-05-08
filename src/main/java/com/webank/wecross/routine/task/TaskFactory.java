package com.webank.wecross.routine.task;

import java.util.List;

public interface TaskFactory {
    List<Task> load(Object... args);
}
