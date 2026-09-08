package org.nkk.flow.service.impl;

import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.extension.id.FlowIdGenerator;
import org.nkk.flow.dao.FlowProcessDao;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.enums.core.FlowProcessEnum.ProcessState;
import org.nkk.flow.model.FlowModelValidator;
import org.nkk.flow.model.FlowProcessModel;
import org.nkk.flow.service.FlowProcessService;

import java.util.Date;
import java.util.List;

/**
 * 默认流程定义服务。
 */
public class FlowProcessServiceImpl implements FlowProcessService {

    private final FlowProcessDao processDao;

    private final FlowIdGenerator idGenerator;

    public FlowProcessServiceImpl(FlowProcessDao processDao, FlowIdGenerator idGenerator) {
        this.processDao = processDao;
        this.idGenerator = idGenerator;
    }

    @Override
    public Long deploy(String jsonString, FlowCreator creator, boolean repeat) {
        return deploy(jsonString, creator, repeat, false);
    }

    @Override
    public Long deploy(String jsonString, FlowCreator creator, boolean repeat, boolean saveAsDraft) {
        FlowProcessModel model = FlowContext.fromJson(jsonString, FlowProcessModel.class);
        if (model == null || model.getKey() == null) {
            throw new IllegalArgumentException("流程模型 key 不能为空");
        }
        model.setKey(model.getKey().trim());
        model.buildParentNode();
        if (!saveAsDraft) {
            FlowModelValidator.validate(model);
        }

        List<FlowProcess> exists = processDao.selectListByProcessKeyAndVersion(creator.getTenantId(), model.getKey(), null);
        FlowProcess latest = latestProcess(exists);
        FlowProcess draft = latestDraft(exists);
        if (saveAsDraft) {
            if (draft != null) {
                updateDraft(draft, model);
                return draft.getId();
            }
            return insertProcess(model, creator, nextVersion(latest), ProcessState.DRAFT);
        }

        if (draft != null) {
            updateDraft(draft, model);
            archiveCurrentProcesses(exists, draft.getId());
            FlowProcess update = new FlowProcess();
            update.setId(draft.getId());
            update.setProcessState(ProcessState.ENABLED.value());
            processDao.updateById(update);
            return draft.getId();
        }

        if (latest != null && !repeat) {
            FlowProcess update = new FlowProcess();
            update.setId(latest.getId());
            update.setProcessName(model.getName());
            update.setInstanceUrl(model.getInstanceUrl());
            update.setModelContent(FlowContext.toJson(model.cleanParentNode()));
            if (!processDao.updateById(update)) {
                throw new IllegalStateException("更新流程定义失败");
            }
            return latest.getId();
        }

        archiveCurrentProcesses(exists, null);
        return insertProcess(model, creator, nextVersion(latest), ProcessState.ENABLED);
    }

    private Long insertProcess(FlowProcessModel model, FlowCreator creator, int version, ProcessState state) {
        FlowProcess process = new FlowProcess();
        process.setId(idGenerator.nextId(null));
        process.setTenantId(creator.getTenantId());
        process.setCreateId(creator.getCreateId());
        process.setCreateBy(creator.getCreateBy());
        process.setCreateTime(new Date());
        process.setProcessKey(model.getKey());
        process.setProcessName(model.getName());
        process.setInstanceUrl(model.getInstanceUrl());
        process.setProcessVersion(version);
        process.setProcessState(state.value());
        process.setUseScope(0);
        process.setSort(0);
        process.setModelContent(FlowContext.toJson(model.cleanParentNode()));
        if (!processDao.insert(process)) {
            throw new IllegalStateException("保存流程定义失败");
        }
        return process.getId();
    }

    private void updateDraft(FlowProcess draft, FlowProcessModel model) {
        FlowProcess update = new FlowProcess();
        update.setId(draft.getId());
        update.setProcessName(model.getName());
        update.setInstanceUrl(model.getInstanceUrl());
        update.setModelContent(FlowContext.toJson(model.cleanParentNode()));
        if (!processDao.updateById(update)) {
            throw new IllegalStateException("保存流程草稿失败");
        }
    }

    private FlowProcess latestProcess(List<FlowProcess> processes) {
        return processes == null || processes.isEmpty() ? null : processes.get(0);
    }

    private FlowProcess latestDraft(List<FlowProcess> processes) {
        if (processes == null) {
            return null;
        }
        for (FlowProcess process : processes) {
            if (ProcessState.DRAFT.value().equals(process.getProcessState())) {
                return process;
            }
        }
        return null;
    }

    private int nextVersion(FlowProcess latest) {
        return latest == null || latest.getProcessVersion() == null ? 1 : latest.getProcessVersion() + 1;
    }

    private void archiveCurrentProcesses(List<FlowProcess> processes, Long excludeId) {
        if (processes == null) {
            return;
        }
        for (FlowProcess process : processes) {
            if (process.getId() == null || process.getId().equals(excludeId)
                    || ProcessState.HISTORY.value().equals(process.getProcessState())) {
                continue;
            }
            FlowProcess history = new FlowProcess();
            history.setId(process.getId());
            history.setProcessState(ProcessState.HISTORY.value());
            processDao.updateById(history);
        }
    }

    @Override
    public FlowProcess getProcessById(Long id) {
        FlowProcess process = processDao.selectById(id);
        if (process == null) {
            throw new IllegalArgumentException("流程定义不存在，id=" + id);
        }
        return process;
    }

    @Override
    public FlowProcess getProcessByVersion(String tenantId, String processKey, Integer version) {
        List<FlowProcess> list = processDao.selectListByProcessKeyAndVersion(tenantId, processKey, version);
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("流程定义不存在，processKey=" + processKey);
        }
        if (version == null) {
            for (FlowProcess process : list) {
                if (ProcessState.ENABLED.value().equals(process.getProcessState())) {
                    return process;
                }
            }
            throw new IllegalArgumentException("流程定义不存在可发起版本，processKey=" + processKey);
        }
        return list.get(0);
    }

    @Override
    public List<FlowProcess> listCurrentProcesses(String tenantId) {
        return processDao.selectCurrentList(tenantId);
    }

    @Override
    public List<FlowProcess> getProcessVersions(String tenantId, String processKey) {
        return processDao.selectListByProcessKeyAndVersion(tenantId, processKey, null);
    }

    @Override
    public boolean enableProcess(Long id) {
        FlowProcess process = getProcessById(id);
        if (ProcessState.HISTORY.value().equals(process.getProcessState())) {
            throw new IllegalStateException("历史版本不能直接启用，id=" + id);
        }
        if (ProcessState.DRAFT.value().equals(process.getProcessState())) {
            throw new IllegalStateException("草稿不能直接启用，请发布草稿，id=" + id);
        }
        FlowProcess update = new FlowProcess();
        update.setId(id);
        update.setProcessState(ProcessState.ENABLED.value());
        return processDao.updateById(update);
    }

    @Override
    public boolean disableProcess(Long id) {
        FlowProcess process = getProcessById(id);
        if (ProcessState.HISTORY.value().equals(process.getProcessState())) {
            throw new IllegalStateException("历史版本不能直接禁用，id=" + id);
        }
        if (ProcessState.DRAFT.value().equals(process.getProcessState())) {
            throw new IllegalStateException("草稿不能禁用，id=" + id);
        }
        FlowProcess update = new FlowProcess();
        update.setId(id);
        update.setProcessState(ProcessState.DISABLED.value());
        return processDao.updateById(update);
    }

    @Override
    public boolean updateProcessInfo(FlowProcess process) {
        if (process == null || process.getId() == null) {
            throw new IllegalArgumentException("流程定义 ID 不能为空");
        }
        FlowProcess update = new FlowProcess();
        update.setId(process.getId());
        update.setProcessName(process.getProcessName());
        update.setProcessIcon(process.getProcessIcon());
        update.setProcessType(process.getProcessType());
        update.setInstanceUrl(process.getInstanceUrl());
        update.setRemark(process.getRemark());
        return processDao.updateById(update);
    }
}

