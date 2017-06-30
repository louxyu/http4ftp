package com.youdao.tasks;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by YW on 2017/6/9.
 */
public class TaskManage {
    private static ScheduledExecutorService scheduldExecutorService = Executors.newScheduledThreadPool(10);

    private static Logger log = LogManager.getLogger(TaskManage.class);
    public static void addTask(BaseTask baseTask, long initialDelay, long period){
        scheduldExecutorService.scheduleAtFixedRate(baseTask,initialDelay,period, TimeUnit.MINUTES);
        log.info("-------------------------------addTask");
    }

    public static void shutdownNow() {
        scheduldExecutorService.shutdownNow();
        log.info("-------------------------------shutdownNow");
    }

    public static void shutdown() {
        scheduldExecutorService.shutdown();
        log.info("-------------------------------shutdown");
    }
}
