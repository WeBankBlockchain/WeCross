package com.webank.wecross.polling;

import org.quartz.JobDetail;
import org.quartz.Trigger;

public class Task {
    Trigger trigger;
    JobDetail jobDetail;

    public Task(Trigger trigger, JobDetail jobDetail) {
        this.trigger = trigger;
        this.jobDetail = jobDetail;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public void setJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }
}
