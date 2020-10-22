package com.example.tools.task;

import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class UpdateNodeLibProcessor {
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    public void submit(UpdateNodeLibTask task) {
        threadPoolExecutor.submit(task);
    }
}
