package com.projectmonitor.jenkins;

import org.springframework.stereotype.Component;

@Component
class ThreadSleepService {

    public void sleep(long millisToSleep) throws InterruptedException {
        Thread.sleep(millisToSleep);
    }
}
