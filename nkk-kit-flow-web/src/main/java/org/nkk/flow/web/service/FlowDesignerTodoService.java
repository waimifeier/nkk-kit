package org.nkk.flow.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.nkk.flow.entity.FlowHisTaskActor;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.enums.core.FlowTaskEnum.TaskType;
import org.nkk.flow.service.NkkFlowEngine;
import org.nkk.flow.web.model.FlowApprovedPageRequest;
import org.nkk.flow.web.model.FlowCopiedPageRequest;
import org.nkk.flow.web.model.FlowMyAppliedPageRequest;
import org.nkk.flow.web.model.FlowPendingPageRequest;
import org.nkk.flow.web.model.FlowTodoRecordResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程待办中心查询服务。
 *
 * <p>拆分为 4 个独立方法，每个方法返回分页结果，SQL 分页优先，仅在任务参与者权限筛选环节使用内存过滤。</p>
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

    // ==================== 1. 我发起的 ====================

    /**
     * 分页查询当前登录用户发起的流程实例。
     */
    public PageResult<FlowTodoRecordResponse> myApplied(FlowMyAppliedPageRequest request) {
        String userId = currentUserId();
        Page<FlowHisInstance> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<FlowHisInstance> result = hisInstanceDao.selectPageByCreateId(page, userId,
                request.getInstanceState(), request.getStartTime(), request.getEndTime());

        if (result == null || CollUtil.isEmpty(result.getRecords())) {
            return PageResult.empty(request.getCurrent(), request.getSize());
        }

        Map<Long, FlowProcess> processMap = batchFetchProcesses(result.getRecords());
        List<FlowTodoRecordResponse> records = new ArrayList<>(result.getRecords().size());
        for (FlowHisInstance instance : result.getRecords()) {
            records.add(baseRecord(instance, processMap));
        }
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    // ==================== 2. 待我审批 ====================

    /**
     * 分页查询当前登录人需要审批的活动任务对应的流程实例。
     */
    public PageResult<FlowTodoRecordResponse> pending(FlowPendingPageRequest request) {
        String userId = currentUserId();

        // Step1: 通过权限策略找到允许的任务 ID
        List<FlowTaskActor> actors = taskActorDao.selectListByActorId(userId);
        Map<Long, List<FlowTaskActor>> actorMap = groupActorsByTaskId(actors);
        if (actorMap.isEmpty()) {
            return PageResult.empty(request.getCurrent(), request.getSize());
        }
        LinkedHashSet<Long> allowedTaskIds = allowedTaskIds(userId, actorMap);
        if (allowedTaskIds.isEmpty()) {
            return PageResult.empty(request.getCurrent(), request.getSize());
        }

        // Step2: 查活动任务，排除 COPY，收集 instanceId + task 映射
        List<FlowTask> tasks = taskDao.selectListByIds(new ArrayList<>(allowedTaskIds));
        Map<Long, FlowTask> instanceToTask = new LinkedHashMap<>();
        LinkedHashSet<Long> instanceIds = new LinkedHashSet<>();
        for (FlowTask task : tasks) {
            if (TaskType.COPY.value().equals(task.getTaskType())) {
                continue;
            }
            if (task.getInstanceId() != null) {
                instanceToTask.put(task.getInstanceId(), task);
                instanceIds.add(task.getInstanceId());
            }
        }
        if (instanceIds.isEmpty()) {
            return PageResult.empty(request.getCurrent(), request.getSize());
        }

        // Step3: sourceType/processType → processIds
        List<Long> processIds = resolveProcessIds(request.getSourceType(), request.getProcessType());

        // Step4: SQL 分页查询实例
        Page<FlowHisInstance> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<FlowHisInstance> result = hisInstanceDao.selectPageByInstanceIds(page,
                instanceIds, request.getInstanceId(), request.getStartUserName(),
                request.getStartTime(), request.getEndTime(), processIds);

        if (result == null || CollUtil.isEmpty(result.getRecords())) {
            return PageResult.empty(request.getCurrent(), request.getSize());
        }

        Map<Long, FlowProcess> processMap = batchFetchProcesses(result.getRecords());
        List<FlowTodoRecordResponse> records = new ArrayList<>(result.getRecords().size());
        for (FlowHisInstance instance : result.getRecords()) {
            FlowTask task = instanceToTask.get(instance.getId());
            records.add(recordWithTask(instance, task, processMap));
        }
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    // ==================== 3. 抄送给我 ====================

    /**
     * 分页查询抄送给当前登录人的记录（活动抄送 + 历史抄送）。
     */
    public PageResult<FlowTodoRecordResponse> copied(FlowCopiedPageRequest request) {
        String userId = currentUserId();

        // Step1: 活动抄送任务
        List<FlowTaskActor> activeActors = taskActorDao.selectListByActorId(userId);
        LinkedHashSet<Long> activeTaskIds = new LinkedHashSet<>();
        for (FlowTaskActor actor : activeActors) {
            if (actor.getTaskId() != null) {
                activeTaskIds.add(actor.getTaskId());
            }
        }

        // Step2: 历史抄送任务
        List<FlowHisTaskActor> hisActors = hisTaskActorDao.selectListByActorId(userId);
        LinkedHashSet<Long> hisTaskIds = new LinkedHashSet<>();
        for (FlowHisTaskActor actor : hisActors) {
            if (actor.getTaskId() != null) {
                hisTaskIds.add(actor.getTaskId());
            }
        }

        // Step3: 查任务详情，过滤 COPY 类型 + 抄送时间范围，收集 instanceId + task 映射
        Map<Long, FlowTodoRecordResponse.Task> instanceToTask = new LinkedHashMap<>();
        LinkedHashSet<Long> instanceIds = new LinkedHashSet<>();

        // 活动抄送
        if (!activeTaskIds.isEmpty()) {
            List<FlowTask> tasks = taskDao.selectListByIds(new ArrayList<>(activeTaskIds));
            for (FlowTask task : tasks) {
                if (!TaskType.COPY.value().equals(task.getTaskType())) {
                    continue;
                }
                if (!inTimeRange(task.getCreateTime(), request.getStartTime(), request.getEndTime())) {
                    continue;
                }
                if (task.getInstanceId() != null) {
                    instanceToTask.put(task.getInstanceId(), toTaskData(task));
                    instanceIds.add(task.getInstanceId());
                }
            }
        }

        // 历史抄送
        if (!hisTaskIds.isEmpty()) {
            List<FlowHisTask> tasks = hisTaskDao.selectListByIds(new ArrayList<>(hisTaskIds));
            for (FlowHisTask task : tasks) {
                if (!TaskType.COPY.value().equals(task.getTaskType())) {
                    continue;
                }
                if (!inTimeRange(task.getCreateTime(), request.getStartTime(), request.getEndTime())) {
                    continue;
                }
                if (task.getInstanceId() != null) {
                    instanceToTask.putIfAbsent(task.getInstanceId(), toTaskData(task));
                    instanceIds.add(task.getInstanceId());
                }
            }
        }

        if (instanceIds.isEmpty()) {
            return PageResult.empty(request.getCurrent(), request.getSize());
        }

        // Step4: sourceType/processType → processIds
        List<Long> processIds = resolveProcessIds(request.getSourceType(), request.getProcessType());

        // Step5: SQL 分页查询实例（不传时间范围，时间已在任务层过滤）
        Page<FlowHisInstance> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<FlowHisInstance> result = hisInstanceDao.selectPageByInstanceIds(page,
                instanceIds, request.getInstanceId(), request.getStartUserName(),
                null, null, processIds);

        if (result == null || CollUtil.isEmpty(result.getRecords())) {
            return PageResult.empty(request.getCurrent(), request.getSize());
        }

        Map<Long, FlowProcess> processMap = batchFetchProcesses(result.getRecords());
        List<FlowTodoRecordResponse> records = new ArrayList<>(result.getRecords().size());
        for (FlowHisInstance instance : result.getRecords()) {
            FlowTodoRecordResponse record = baseRecord(instance, processMap);
            record.setTask(instanceToTask.get(instance.getId()));
            records.add(record);
        }
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    // ==================== 4. 审批记录 ====================

    /**
     * 分页查询当前登录人已经审批过的历史任务。
     */
    public PageResult<FlowTodoRecordResponse> approved(FlowApprovedPageRequest request) {
        String userId = currentUserId();

        // Step1: 前置过滤 — startUserName / sourceType / processType / instanceId
        List<Long> preFilterInstanceIds = null;
        boolean hasPreFilter = StrUtil.isNotBlank(request.getStartUserName())
                || StrUtil.isNotBlank(request.getSourceType())
                || StrUtil.isNotBlank(request.getProcessType())
                || request.getInstanceId() != null;

        if (hasPreFilter) {
            List<Long> processIds = resolveProcessIds(request.getSourceType(), request.getProcessType());
            if (request.getInstanceId() != null) {
                // 精确 instanceId 优先，直接使用
                preFilterInstanceIds = Collections.singletonList(request.getInstanceId());
            } else {
                preFilterInstanceIds = hisInstanceDao.selectIdsByCreateByLikeAndProcessIds(
                        request.getStartUserName(), processIds);
            }
            if (CollUtil.isEmpty(preFilterInstanceIds)) {
                return PageResult.empty(request.getCurrent(), request.getSize());
            }
        }

        // Step2: SQL 分页查询历史任务
        List<Integer> excludeTypes = CollUtil.newArrayList(TaskType.START.value(), TaskType.COPY.value());
        Page<FlowHisTask> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<FlowHisTask> result = hisTaskDao.selectPageByCreateId(page, userId,
                excludeTypes, request.getStartTime(), request.getEndTime(), preFilterInstanceIds);

        if (result == null || CollUtil.isEmpty(result.getRecords())) {
            return PageResult.empty(request.getCurrent(), request.getSize());
        }

        // Step3: 批量查实例 + 流程定义
        List<Long> instanceIds = result.getRecords().stream()
                .map(FlowHisTask::getInstanceId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, FlowHisInstance> instanceMap = new LinkedHashMap<>();
        if (!instanceIds.isEmpty()) {
            for (Long id : instanceIds) {
                FlowHisInstance instance = hisInstanceDao.selectById(id);
                if (instance != null) {
                    instanceMap.put(id, instance);
                }
            }
        }

        Map<Long, FlowProcess> processMap = batchFetchProcesses(new ArrayList<>(instanceMap.values()));

        // Step4: 构建 response
        List<FlowTodoRecordResponse> records = new ArrayList<>(result.getRecords().size());
        for (FlowHisTask task : result.getRecords()) {
            FlowHisInstance instance = instanceMap.get(task.getInstanceId());
            if (instance == null) {
                continue;
            }
            FlowTodoRecordResponse record = baseRecord(instance, processMap);
            record.setTask(toTaskData(task));
            record.setApproveTime(task.getFinishTime());
            records.add(record);
        }
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    // ==================== 公共方法 ====================

    private String currentUserId() {
        FlowCreator creator = creatorProvider == null ? null : creatorProvider.getCurrentCreator();
        if (creator == null || StrUtil.isBlank(creator.getCreateId())) {
            throw new IllegalStateException("未配置 FlowCreatorProvider 或当前用户为空");
        }
        return creator.getCreateId();
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

    private Map<Long, List<FlowTaskActor>> groupActorsByTaskId(List<? extends FlowTaskActor> actors) {
        Map<Long, List<FlowTaskActor>> map = new LinkedHashMap<>();
        if (CollUtil.isEmpty(actors)) {
            return map;
        }
        for (FlowTaskActor actor : actors) {
            if (actor == null || actor.getTaskId() == null) {
                continue;
            }
            map.computeIfAbsent(actor.getTaskId(), k -> new ArrayList<>()).add(actor);
        }
        return map;
    }

    private List<Long> resolveProcessIds(String sourceType, String processType) {
        if (StrUtil.isBlank(sourceType) && StrUtil.isBlank(processType)) {
            return null;
        }
        List<FlowProcess> processes = processDao.selectListByProcessTypeAndFormSourceType(processType, sourceType);
        if (CollUtil.isEmpty(processes)) {
            return Collections.emptyList();
        }
        return processes.stream().map(FlowProcess::getId).collect(Collectors.toList());
    }

    private Map<Long, FlowProcess> batchFetchProcesses(List<FlowHisInstance> instances) {
        List<Long> processIds = instances.stream()
                .map(FlowHisInstance::getProcessId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (processIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<FlowProcess> processes = processDao.selectListByIds(processIds);
        Map<Long, FlowProcess> map = new LinkedHashMap<>();
        for (FlowProcess p : processes) {
            map.put(p.getId(), p);
        }
        return map;
    }

    private boolean inTimeRange(Date time, Date startTime, Date endTime) {
        if (time == null) {
            return startTime == null && endTime == null;
        }
        if (startTime != null && time.before(startTime)) {
            return false;
        }
        if (endTime != null && time.after(endTime)) {
            return false;
        }
        return true;
    }

    private FlowTodoRecordResponse.Task toTaskData(FlowTask task) {
        FlowTodoRecordResponse.Task td = new FlowTodoRecordResponse.Task();
        td.setTaskId(task.getId());
        td.setTaskKey(task.getTaskKey());
        td.setTaskName(task.getTaskName());
        td.setTaskType(task.getTaskType());
        td.setTaskCreateTime(task.getCreateTime());
        td.setViewed(task.getViewed());
        return td;
    }

    private FlowTodoRecordResponse.Task toTaskData(FlowHisTask task) {
        FlowTodoRecordResponse.Task td = new FlowTodoRecordResponse.Task();
        td.setTaskId(task.getId());
        td.setTaskKey(task.getTaskKey());
        td.setTaskName(task.getTaskName());
        td.setTaskType(task.getTaskType());
        td.setTaskCreateTime(task.getCreateTime());
        td.setViewed(task.getViewed());
        return td;
    }

    private FlowTodoRecordResponse recordWithTask(FlowHisInstance instance, FlowTask task,
                                                  Map<Long, FlowProcess> processMap) {
        FlowTodoRecordResponse record = baseRecord(instance, processMap);
        if (task != null) {
            record.setTask(toTaskData(task));
        }
        return record;
    }

    private FlowTodoRecordResponse baseRecord(FlowHisInstance instance, Map<Long, FlowProcess> processMap) {
        FlowProcess process = instance.getProcessId() == null ? null : processMap.get(instance.getProcessId());
        FlowTodoRecordResponse record = new FlowTodoRecordResponse();
        record.setInstanceId(instance.getId());
        record.setBusinessKey(instance.getBusinessKey());
        record.setProcessId(instance.getProcessId());
        record.setProcessKey(process == null ? instance.getProcessKey() : process.getProcessKey());
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
}
