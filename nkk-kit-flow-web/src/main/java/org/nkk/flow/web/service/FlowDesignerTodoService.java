package org.nkk.flow.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.extension.identity.FlowCreatorProvider;
import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.dao.FlowHisTaskActorDao;
import org.nkk.flow.dao.FlowHisTaskDao;
import org.nkk.flow.dao.FlowProcessDao;
import org.nkk.flow.dao.FlowTaskActorDao;
import org.nkk.flow.dao.FlowTaskDao;
import org.nkk.flow.entity.FlowHisInstance;
import org.nkk.flow.entity.FlowHisTask;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskType;
import org.nkk.flow.service.NkkFlowEngine;
import org.nkk.flow.web.model.FlowTodoRecordResponse;
import org.nkk.flow.web.model.FlowTodoTypeEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 流程待办中心查询服务。
 */
public class FlowDesignerTodoService {

    private final NkkFlowEngine flowEngine;
    private final FlowCreatorProvider creatorProvider;
    private final FlowProcessDao processDao;
    private final FlowHisInstanceDao hisInstanceDao;
    private final FlowTaskDao taskDao;
    private final FlowTaskActorDao taskActorDao;
    private final FlowHisTaskDao hisTaskDao;
    private final FlowHisTaskActorDao hisTaskActorDao;

    public FlowDesignerTodoService(NkkFlowEngine flowEngine, FlowCreatorProvider creatorProvider,
                                   FlowProcessDao processDao, FlowHisInstanceDao hisInstanceDao,
                                   FlowTaskDao taskDao, FlowTaskActorDao taskActorDao,
                                   FlowHisTaskDao hisTaskDao, FlowHisTaskActorDao hisTaskActorDao) {
        this.flowEngine = flowEngine;
        this.creatorProvider = creatorProvider;
        this.processDao = processDao;
        this.hisInstanceDao = hisInstanceDao;
        this.taskDao = taskDao;
        this.taskActorDao = taskActorDao;
        this.hisTaskDao = hisTaskDao;
        this.hisTaskActorDao = hisTaskActorDao;
    }

    /**
     * 查询待办中心记录。
     *
     * @param type 查询类型
     * @param userId 用户 ID，为空时从 {@link FlowCreatorProvider} 获取
     * @return 待办中心记录
     */
    public List<FlowTodoRecordResponse> list(Integer type, String userId) {
        FlowTodoTypeEnum todoType = FlowTodoTypeEnum.of(type);
        String actualUserId = resolveUserId(userId);
        if (todoType == FlowTodoTypeEnum.MY_APPLY) {
            return myApply(todoType, actualUserId);
        }
        if (todoType == FlowTodoTypeEnum.PENDING) {
            return pending(todoType, actualUserId);
        }
        if (todoType == FlowTodoTypeEnum.APPROVED) {
            return approved(todoType, actualUserId);
        }
        return copy(todoType, actualUserId);
    }

    private List<FlowTodoRecordResponse> myApply(FlowTodoTypeEnum todoType, String userId) {
        List<FlowHisInstance> instances = hisInstanceDao.selectListByCreateId(userId);
        if (CollUtil.isEmpty(instances)) {
            return Collections.emptyList();
        }
        Map<Long, FlowProcess> processMap = new LinkedHashMap<>();
        List<FlowTodoRecordResponse> records = new ArrayList<>();
        for (FlowHisInstance instance : instances) {
            records.add(recordWithCurrentTask(todoType, instance, processMap));
        }
        return records;
    }

    private List<FlowTodoRecordResponse> pending(FlowTodoTypeEnum todoType, String userId) {
        List<FlowTask> tasks = allowedActiveTasks(userId);
        List<FlowTodoRecordResponse> records = new ArrayList<>();
        Map<Long, FlowProcess> processMap = new LinkedHashMap<>();
        for (FlowTask task : tasks) {
            if (TaskType.COPY.value().equals(task.getTaskType())) {
                continue;
            }
            FlowHisInstance instance = hisInstanceDao.selectById(task.getInstanceId());
            if (instance == null) {
                continue;
            }
            records.add(recordWithTask(todoType, instance, task, processMap));
        }
        return sortByTaskTime(records);
    }

    private List<FlowTodoRecordResponse> approved(FlowTodoTypeEnum todoType, String userId) {
        List<FlowHisTask> tasks = hisTaskDao.selectListByCreateId(userId);
        if (CollUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        List<FlowTodoRecordResponse> records = new ArrayList<>();
        Map<Long, FlowProcess> processMap = new LinkedHashMap<>();
        for (FlowHisTask task : tasks) {
            if (TaskType.START.value().equals(task.getTaskType()) || TaskType.COPY.value().equals(task.getTaskType())) {
                continue;
            }
            FlowHisInstance instance = hisInstanceDao.selectById(task.getInstanceId());
            if (instance == null) {
                continue;
            }
            records.add(recordWithCurrentTask(todoType, instance, processMap));
        }
        return sortByTaskTime(records);
    }

    private List<FlowTodoRecordResponse> copy(FlowTodoTypeEnum todoType, String userId) {
        List<FlowTodoRecordResponse> records = new ArrayList<>();
        Map<Long, FlowProcess> processMap = new LinkedHashMap<>();
        for (FlowTask task : allowedActiveTasks(userId)) {
            if (!TaskType.COPY.value().equals(task.getTaskType())) {
                continue;
            }
            FlowHisInstance instance = hisInstanceDao.selectById(task.getInstanceId());
            if (instance != null) {
                records.add(recordWithCurrentTask(todoType, instance, processMap));
            }
        }
        for (FlowHisTask task : allowedHisCopyTasks(userId)) {
            FlowHisInstance instance = hisInstanceDao.selectById(task.getInstanceId());
            if (instance != null) {
                records.add(recordWithCurrentTask(todoType, instance, processMap));
            }
        }
        return sortByTaskTime(records);
    }

    private List<FlowTask> allowedActiveTasks(String userId) {
        Map<Long, List<FlowTaskActor>> actorMap = groupActors(taskActorDao.selectList());
        if (actorMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> taskIds = new ArrayList<>(allowedTaskIds(userId, actorMap));
        List<FlowTask> tasks = taskDao.selectListByIds(taskIds);
        tasks.sort(Comparator
                .comparing(FlowTask::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(FlowTask::getId, Comparator.nullsLast(Comparator.reverseOrder())));
        return tasks;
    }

    private List<FlowHisTask> allowedHisCopyTasks(String userId) {
        Map<Long, List<FlowTaskActor>> actorMap = groupActors(new ArrayList<FlowTaskActor>(hisTaskActorDao.selectList()));
        if (actorMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> taskIds = new ArrayList<>(allowedTaskIds(userId, actorMap));
        List<FlowHisTask> tasks = hisTaskDao.selectListByIds(taskIds);
        List<FlowHisTask> copies = new ArrayList<>();
        for (FlowHisTask task : tasks) {
            if (TaskType.COPY.value().equals(task.getTaskType())) {
                copies.add(task);
            }
        }
        return copies;
    }

    private LinkedHashSet<Long> allowedTaskIds(String userId, Map<Long, List<FlowTaskActor>> actorMap) {
        LinkedHashSet<Long> taskIds = new LinkedHashSet<>();
        for (Map.Entry<Long, List<FlowTaskActor>> entry : actorMap.entrySet()) {
            if (flowEngine.getContext().getTaskAccessStrategy().isAllowed(userId, entry.getValue()) != null) {
                taskIds.add(entry.getKey());
            }
        }
        return taskIds;
    }

    private Map<Long, List<FlowTaskActor>> groupActors(List<? extends FlowTaskActor> actors) {
        Map<Long, List<FlowTaskActor>> actorMap = new LinkedHashMap<>();
        if (CollUtil.isEmpty(actors)) {
            return actorMap;
        }
        for (FlowTaskActor actor : actors) {
            if (actor == null || actor.getTaskId() == null) {
                continue;
            }
            actorMap.computeIfAbsent(actor.getTaskId(), key -> new ArrayList<>()).add(actor);
        }
        return actorMap;
    }

    private FlowTodoRecordResponse recordWithCurrentTask(FlowTodoTypeEnum type, FlowHisInstance instance,
                                                        Map<Long, FlowProcess> processMap) {
        return recordWithTask(type, instance, currentActiveTask(instance.getId()), processMap);
    }

    private FlowTodoRecordResponse recordWithTask(FlowTodoTypeEnum type, FlowHisInstance instance, FlowTask task,
                                              Map<Long, FlowProcess> processMap) {
        FlowTodoRecordResponse record = baseRecord(type, instance, processMap);
        if (task == null) {
            return record;
        }
        FlowTodoRecordResponse.Task taskData = new FlowTodoRecordResponse.Task();
        taskData.setTaskId(task.getId());
        taskData.setTaskKey(task.getTaskKey());
        taskData.setTaskName(task.getTaskName());
        taskData.setTaskType(task.getTaskType());
        taskData.setTaskCreateTime(task.getCreateTime());
        taskData.setViewed(task.getViewed());
        record.setTask(taskData);
        return record;
    }

    private FlowTask currentActiveTask(Long instanceId) {
        List<FlowTask> tasks = taskDao.selectListByInstanceId(instanceId);
        if (CollUtil.isEmpty(tasks)) {
            return null;
        }
        tasks.sort(Comparator
                .comparing(FlowTask::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(FlowTask::getId, Comparator.nullsLast(Comparator.reverseOrder())));
        return tasks.get(0);
    }

    private FlowTodoRecordResponse baseRecord(FlowTodoTypeEnum type, FlowHisInstance instance,
                                              Map<Long, FlowProcess> processMap) {
        FlowProcess process = process(instance.getProcessId(), processMap);
        FlowTodoRecordResponse record = new FlowTodoRecordResponse();
        record.setType(type.value());
        record.setTypeName(type.label());
        FlowTodoRecordResponse.Instance instanceData = new FlowTodoRecordResponse.Instance();
        instanceData.setInstanceId(instance.getId());
        instanceData.setBusinessKey(instance.getBusinessKey());
        instanceData.setProcessId(instance.getProcessId());
        instanceData.setProcessKey(process == null ? null : process.getProcessKey());
        instanceData.setProcessName(process == null ? null : process.getProcessName());
        instanceData.setProcessVersion(process == null ? null : process.getProcessVersion());
        instanceData.setInstanceState(instance.getInstanceState());
        instanceData.setInstanceStateName(instanceStateLabel(instance.getInstanceState()));
        instanceData.setStartUserId(instance.getCreateId());
        instanceData.setStartUserName(instance.getCreateBy());
        instanceData.setStartTime(instance.getCreateTime());
        instanceData.setCurrentNodeKey(instance.getCurrentNodeKey());
        instanceData.setCurrentNodeName(instance.getCurrentNodeName());
        instanceData.setDuration(duration(instance));
        record.setInstance(instanceData);
        return record;
    }

    private FlowProcess process(Long processId, Map<Long, FlowProcess> processMap) {
        if (processId == null) {
            return null;
        }
        if (!processMap.containsKey(processId)) {
            processMap.put(processId, processDao.selectById(processId));
        }
        return processMap.get(processId);
    }

    private String resolveUserId(String userId) {
        if (StrUtil.isNotBlank(userId)) {
            return StrUtil.trim(userId);
        }
        FlowCreator creator = creatorProvider == null ? null : creatorProvider.getCurrentCreator();
        if (creator == null || StrUtil.isBlank(creator.getCreateId())) {
            throw new IllegalStateException("用户 ID 不能为空，且未配置 FlowCreatorProvider 获取当前用户");
        }
        return creator.getCreateId();
    }

    private Long duration(FlowHisInstance instance) {
        if (instance == null || instance.getCreateTime() == null) {
            return null;
        }
        if (instance.getDuration() != null) {
            return instance.getDuration();
        }
        Date endTime = instance.getEndTime() == null ? new Date() : instance.getEndTime();
        return endTime.getTime() - instance.getCreateTime().getTime();
    }

    private String instanceStateLabel(Integer state) {
        if (state == null) {
            return null;
        }
        for (InstanceState item : InstanceState.values()) {
            if (item.value().equals(state)) {
                return item.label();
            }
        }
        return String.valueOf(state);
    }

    private List<FlowTodoRecordResponse> sortByTaskTime(List<FlowTodoRecordResponse> records) {
        records.sort(Comparator
                .comparing(this::taskCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(this::instanceId, Comparator.nullsLast(Comparator.reverseOrder())));
        return records;
    }

    private Date taskCreateTime(FlowTodoRecordResponse record) {
        return record == null || record.getTask() == null ? null : record.getTask().getTaskCreateTime();
    }

    private Long instanceId(FlowTodoRecordResponse record) {
        return record == null || record.getInstance() == null ? null : record.getInstance().getInstanceId();
    }
}
