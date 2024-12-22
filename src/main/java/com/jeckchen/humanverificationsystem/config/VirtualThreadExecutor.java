package com.jeckchen.humanverificationsystem.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadExecutor {

    public static final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public static ExecutorService getInstance() {
        return executorService;
    }

}
