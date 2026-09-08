package org.nkk.flow.core.extension.schedule;

/**
 * 流程调度执行锁。使用方可以替换为 Redis、数据库等分布式锁。
 */
public interface FlowJobLock {

    /**
     * 尝试获取锁，不阻塞等待。
     */
    boolean tryLock();

    /**
     * 释放锁。
     */
    void unlock();
}

