package org.nkk.flow.service.impl;

import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.dao.FlowExtInstanceDao;
import org.nkk.flow.dao.FlowHisTaskActorDao;
import org.nkk.flow.dao.FlowHisTaskDao;
import org.nkk.flow.dao.FlowInstanceDao;
import org.nkk.flow.dao.FlowTaskActorDao;
import org.nkk.flow.dao.FlowTaskDao;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowExtInstance;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowHisTaskActor;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.service.FlowQueryService;

import java.util.Date;
import java.util.Collections;
import java.util.List;

/**
 * 默认流程查询服务。
 */
public class FlowQueryServiceImpl implements FlowQueryService {

    private final FlowInstanceDao instanceDao;
    private final FlowHisInstanceDao hisInstanceDao;
    private final FlowExtInstanceDao extInstanceDao;
    private final FlowTaskDao taskDao;
    private final FlowTaskActorDao taskActorDao;
    private final FlowHisTaskDao hisTaskDao;
    private final FlowHisTaskActorDao hisTaskActorDao;

    public FlowQueryServiceImpl(FlowInstanceDao instanceDao, FlowHisInstanceDao hisInstanceDao,
                                FlowExtInstanceDao extInstanceDao, FlowTaskDao taskDao, FlowTaskActorDao taskActorDao,
                                FlowHisTaskDao hisTaskDao, FlowHisTaskActorDao hisTaskActorDao) {
        this.instanceDao = instanceDao;
        this.hisInstanceDao = hisInstanceDao;
        this.extInstanceDao = extInstanceDao;
        this.taskDao = taskDao;
        this.taskActorDao = taskActorDao;
        this.hisTaskDao = hisTaskDao;
        this.hisTaskActorDao = hisTaskActorDao;
    }

    @Override
    public FlowInstance getInstance(Long instanceId) {
        return instanceDao.selectById(instanceId);
    }

    @Override
    public FlowHisInstance getHisInstance(Long instanceId) {
        return hisInstanceDao.selectById(instanceId);
    }

    @Override
    public FlowExtInstance getExtInstance(Long instanceId) {
        return extInstanceDao.selectById(instanceId);
    }

    @Override
    public boolean existActiveSubProcess(Long instanceId) {
        List<FlowInstance> list = instanceDao.selectListByParentInstanceId(instanceId);
        return list != null && !list.isEmpty();
    }

    @Override
    public boolean existActiveTask(Long instanceId) {
        List<FlowTask> list = taskDao.selectListByInstanceId(instanceId);
        return list != null && !list.isEmpty();
    }

    @Override
    public FlowTask getTask(Long taskId) {
        return taskDao.selectById(taskId);
    }

    @Override
    public FlowHisTask getHisTask(Long taskId) {
        return hisTaskDao.selectById(taskId);
    }

    @Override
    public List<FlowTask> getTasksByInstanceId(Long instanceId) {
        List<FlowTask> list = taskDao.selectListByInstanceId(instanceId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowTask> getTasksByInstanceIdAndTaskKey(Long instanceId, String taskKey) {
        List<FlowTask> list = taskDao.selectListByInstanceIdAndTaskKey(instanceId, taskKey);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowTaskActor> getTaskActorsByTaskId(Long taskId) {
        List<FlowTaskActor> list = taskActorDao.selectListByTaskId(taskId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowTaskActor> getTaskActorsByTaskIdAndActorId(Long taskId, String actorId) {
        List<FlowTaskActor> list = taskActorDao.selectListByTaskIdAndActorId(taskId, actorId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowTaskActor> getActiveTaskActorsByInstanceId(Long instanceId) {
        List<FlowTaskActor> list = taskActorDao.selectListByInstanceId(instanceId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowHisTask> getHisTasksByInstanceId(Long instanceId) {
        List<FlowHisTask> list = hisTaskDao.selectListByInstanceId(instanceId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowHisTaskActor> getHisTaskActorsByTaskId(Long taskId) {
        List<FlowHisTaskActor> list = hisTaskActorDao.selectListByTaskId(taskId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowHisTaskActor> getHisTaskActorsByInstanceId(Long instanceId) {
        List<FlowHisTaskActor> list = hisTaskActorDao.selectListByInstanceId(instanceId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowInstance> getInstancesByBusinessKey(String businessKey) {
        List<FlowInstance> list = instanceDao.selectListByBusinessKey(businessKey);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowHisInstance> getHisInstancesByBusinessKey(String businessKey) {
        List<FlowHisInstance> list = hisInstanceDao.selectListByBusinessKey(businessKey);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowInstance> getSubProcessByInstanceId(Long instanceId) {
        List<FlowInstance> list = instanceDao.selectListByParentInstanceId(instanceId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public List<FlowTask> getTimeoutOrRemindTasks(Date now) {
        List<FlowTask> list = taskDao.selectTimeoutOrRemindTasks(now);
        return list == null ? Collections.emptyList() : list;
    }
}

