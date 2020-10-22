package com.example.tools.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
    @Autowired
    private UpdateNodeLibProcessor updateNodeLibProcessor;

    @Scheduled(fixedRate = 43200 * 1000)
    public void scheduleProcessing() {
        updateNodeLibProcessor.submit(new UpdateNodeLibTask());
    }
}
