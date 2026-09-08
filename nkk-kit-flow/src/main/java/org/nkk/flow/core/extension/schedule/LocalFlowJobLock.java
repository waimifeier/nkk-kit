package org.nkk.flow.core.extension.schedule;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 单 JVM 本地调度锁。
 */
public class LocalFlowJobLock implements FlowJobLock {

    private final Lock lock = new ReentrantLock();

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }
}

