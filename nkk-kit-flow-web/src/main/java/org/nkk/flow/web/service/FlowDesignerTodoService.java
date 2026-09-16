package org.nkk.flow.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.nkk.core.beans.common.PageResult;
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
import org.nkk.flow.web.model.FlowTodoPageRequest;
import org.nkk.flow.web.model.FlowTodoRecordResponse;
import org.nkk.flow.enums.core.FlowTodoTypeEnum;

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
     * 分页查询待办中心记录。
     *
     * <p>待办数据涉及任务参与者权限策略的内存筛选，无法直接下推 SQL 分页，
     * 故先按现有规则查出当前用户可见记录并排序，再在内存中分页。</p>
     *
     * @param request 分页查询请求
     * @return 待办中心记录分页结果
     */
    public PageResult<FlowTodoRecordResponse> page(FlowTodoPageRequest request) {
        FlowTodoTypeEnum todoType = FlowTodoTypeEnum.of(request.getType());
        String actualUserId = resolveUserId(request.getUserId());

        List<FlowTodoRecordResponse> records;
        if (todoType == FlowTodoTypeEnum.MY_APPLY) {
            records = myApply(actualUserId, request.getInstanceState());
        } else if (todoType == FlowTodoTypeEnum.PENDING) {
            records = pending(actualUserId);
        } else if (todoType == FlowTodoTypeEnum.APPROVED) {
            records = approved(actualUserId);
        } else {
            records = copy(actualUserId);
        }
        return paginate(records, request.getCurrent(), request.getSize());
    }

    private List<FlowTodoRecordResponse> myApply(String userId, Integer instanceState) {
        List<FlowHisInstance> instances = hisInstanceDao.selectListByCreateId(userId);
        if (CollUtil.isEmpty(instances)) {
            return Collections.emptyList();
        }
        Map<Long, FlowProcess> processMap = new LinkedHashMap<>();
        List<FlowTodoRecordResponse> records = new ArrayList<>();
        for (FlowHisInstance instance : instances) {
            // instanceState 为空查询全部；非空时按审批状态精确过滤：0-审批中、1-审批通过、2-审批拒绝、3-已撤销
            if (instanceState != null && !instanceState.equals(instance.getInstanceState())) {
                continue;
            }
            records.add(recordWithCurrentTask(instance, processMap));
        }
        // 按发起时间倒序，保证分页顺序稳定
        records.sort(Comparator
                .comparing(FlowTodoRecordResponse::getStartTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(FlowTodoRecordResponse::getInstanceId, Comparator.nullsLast(Comparator.reverseOrder())));
        return records;
    }

    private List<FlowTodoRecordResponse> pending(String userId) {
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
            records.add(recordWithTask(instance, task, processMap));
        }
        return sortByTaskTime(records);
    }

    private List<FlowTodoRecordResponse> approved(String userId) {
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
            records.add(recordWithCurrentTask(instance, processMap));
        }
        return sortByTaskTime(records);
    }

    private List<FlowTodoRecordResponse> copy(String userId) {
        List<FlowTodoRecordResponse> records = new ArrayList<>();
        Map<Long, FlowProcess> processMap = new LinkedHashMap<>();
        for (FlowTask task : allowedActiveTasks(userId)) {
            if (!TaskType.COPY.value().equals(task.getTaskType())) {
                continue;
            }
            FlowHisInstance instance = hisInstanceDao.selectById(task.getInstanceId());
            if (instance != null) {
                records.add(recordWithCurrentTask(instance, processMap));
            }
        }
        for (FlowHisTask task : allowedHisCopyTasks(userId)) {
            FlowHisInstance instance = hisInstanceDao.selectById(task.getInstanceId());
            if (instance != null) {
                records.add(recordWithCurrentTask(instance, processMap));
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

    private FlowTodoRecordResponse recordWithCurrentTask(FlowHisInstance instance,
                                                         Map<Long, FlowProcess> processMap) {
        return recordWithTask(instance, currentActiveTask(instance.getId()), processMap);
    }

    private FlowTodoRecordResponse recordWithTask(FlowHisInstance instance, FlowTask task,
                                                  Map<Long, FlowProcess> processMap) {
        FlowTodoRecordResponse record = baseRecord(instance, processMap);
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

    private FlowTodoRecordResponse baseRecord(FlowHisInstance instance, Map<Long, FlowProcess> processMap) {
        FlowProcess process = process(instance.getProcessId(), processMap);
        FlowTodoRecordResponse record = new FlowTodoRecordResponse();
        record.setInstanceId(instance.getId());
        record.setBusinessKey(instance.getBusinessKey());
        record.setProcessId(instance.getProcessId());
        record.setProcessKey(process == null ? null : process.getProcessKey());
        record.setProcessName(process == null ? null : process.getProcessName());
        record.setProcessType(process == null ? null : process.getProcessType());
        record.setSourceType(process == null ? null : process.getFormSourceType());
        record.setProcessVersion(process == null ? null : process.getProcessVersion());
        record.setInstanceState(instance.getInstanceState());
        record.setInstanceStateName(instanceStateLabel(instance.getInstanceState()));
        record.setStartUserId(instance.getCreateId());
        record.setStartUserName(instance.getCreateBy());
        record.setStartTime(instance.getCreateTime());
        record.setCurrentNodeKey(instance.getCurrentNodeKey());
        record.setCurrentNodeName(instance.getCurrentNodeName());
        record.setDuration(duration(instance));
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
                .thenComparing(FlowTodoRecordResponse::getInstanceId, Comparator.nullsLast(Comparator.reverseOrder())));
        return records;
    }

    private Date taskCreateTime(FlowTodoRecordResponse record) {
        return record == null || record.getTask() == null ? null : record.getTask().getTaskCreateTime();
    }

    /**
     * 对已排序的记录做内存分页。
     *
     * @param records 已排序的全量记录
     * @param current 页码，从 1 开始
     * @param size    每页条数
     * @return 分页结果
     */
    private PageResult<FlowTodoRecordResponse> paginate(List<FlowTodoRecordResponse> records, int current, int size) {
        int total = records.size();
        int from = Math.min((current - 1) * size, total);
        int to = Math.min(from + size, total);
        return PageResult.of(total, current, size, new ArrayList<>(records.subList(from, to)));
    }
}
