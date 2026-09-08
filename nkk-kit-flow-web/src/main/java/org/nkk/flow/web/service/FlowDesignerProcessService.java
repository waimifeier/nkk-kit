package org.nkk.flow.web.service;

import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowContext;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.extension.identity.FlowCreatorProvider;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.enums.core.FlowProcessEnum.ProcessState;
import org.nkk.flow.model.FlowNodeModel;
import org.nkk.flow.model.FlowProcessModel;
import org.nkk.flow.service.NkkFlowEngine;
import org.nkk.flow.web.model.FlowProcessCategoryResponse;
import org.nkk.flow.web.model.FlowFormBindingRequest;
import org.nkk.flow.web.model.FlowProcessInfoUpdateRequest;
import org.nkk.flow.web.model.FlowProcessPublishRequest;
import org.nkk.flow.web.model.FlowProcessVO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程设计器流程定义服务。
 */
public class FlowDesignerProcessService {

    private final NkkFlowEngine flowEngine;

    private final FlowCreatorProvider creatorProvider;

    private final FlowDesignerFormService formService;

    public FlowDesignerProcessService(NkkFlowEngine flowEngine, FlowCreatorProvider creatorProvider,
                                      FlowDesignerFormService formService) {
        this.flowEngine = flowEngine;
        this.creatorProvider = creatorProvider;
        this.formService = formService;
    }

    /**
     * 发布流程定义。
     *
     * @param request 发布请求
     * @return 发布结果
     */
    public FlowProcess publish(FlowProcessPublishRequest request) {
        assertPublishRequest(request);
        String modelContent = modelContent(request.getModelContent());
        FlowProcessModel model = resolveProcessModel(modelContent);
        boolean saveAsDraft = Boolean.TRUE.equals(request.getSaveAsDraft());
        if (model == null || (!saveAsDraft && model.getNodeConfig() == null)) {
            throw new IllegalArgumentException("流程 JSON 解析失败");
        }
        model.setKey(StrUtil.trim(request.getProcessKey()));
        model.setName(StrUtil.trim(request.getProcessName()));
        model.setInstanceUrl(StrUtil.trimToNull(request.getInstanceUrl()));
        applyFormBinding(model, request.getFormBinding());
        syncFormFields(request.getFormBinding());

        FlowCreator creator = currentCreator();
        Long processId = flowEngine.processService().deploy(
                FlowContext.toJson(model),
                creator,
                !Boolean.FALSE.equals(request.getRepeat()),
                saveAsDraft);

        FlowProcess update = new FlowProcess();
        update.setId(processId);
        update.setProcessName(StrUtil.trim(request.getProcessName()));
        update.setProcessIcon(StrUtil.trimToNull(request.getProcessIcon()));
        update.setProcessType(StrUtil.trimToNull(request.getProcessType()));
        update.setRemark(StrUtil.trimToNull(request.getRemark()));
        update.setInstanceUrl(StrUtil.trimToNull(request.getInstanceUrl()));
        flowEngine.processService().updateProcessInfo(update);

        return flowEngine.processService().getProcessById(processId);
    }

    /**
     * 查询流程定义详情。
     *
     * @param processId 流程定义 ID
     * @return 流程定义
     */
    public FlowProcess getProcess(Long processId) {
        return flowEngine.processService().getProcessById(processId);
    }

    /**
     * 按流程分类查询当前流程定义。
     *
     * @param tenantId 租户 ID
     * @param mode 查询模式，latest 表示最新编辑版本，published 表示已发布版本
     * @return 分类分组流程列表
     */
    public List<FlowProcessCategoryResponse> listByCategory(String tenantId, String mode) {
        List<FlowProcess> processes = flowEngine.processService().listCurrentProcesses(StrUtil.trimToNull(tenantId));
        processes = filterProcesses(processes, mode);
        Map<String, List<FlowProcessVO>> groupMap = new LinkedHashMap<>();
        if (processes != null) {
            for (FlowProcess process : processes) {
                String processType = StrUtil.trimToNull(process.getProcessType());
                groupMap.computeIfAbsent(processType, key -> new ArrayList<>()).add(FlowProcessVO.of(process));
            }
        }
        List<FlowProcessCategoryResponse> groups = new ArrayList<>();
        for (Map.Entry<String, List<FlowProcessVO>> entry : groupMap.entrySet()) {
            groups.add(FlowProcessCategoryResponse.of(entry.getKey(), entry.getValue()));
        }
        return groups;
    }

    private List<FlowProcess> filterProcesses(List<FlowProcess> processes, String mode) {
        if (processes == null || processes.isEmpty()) {
            return new ArrayList<>();
        }
        String actualMode = StrUtil.blankToDefault(StrUtil.trim(mode), "latest");
        if ("published".equalsIgnoreCase(actualMode)) {
            return publishedProcesses(processes);
        }
        if ("latest".equalsIgnoreCase(actualMode)) {
            return latestProcesses(processes);
        }
        throw new IllegalArgumentException("流程列表查询模式不支持，mode=" + mode);
    }

    private List<FlowProcess> publishedProcesses(List<FlowProcess> processes) {
        List<FlowProcess> records = new ArrayList<>();
        for (FlowProcess process : processes) {
            if (ProcessState.DRAFT.value().equals(process.getProcessState())
                    || ProcessState.HISTORY.value().equals(process.getProcessState())) {
                continue;
            }
            records.add(process);
        }
        return records;
    }

    private List<FlowProcess> latestProcesses(List<FlowProcess> processes) {
        Map<String, FlowProcess> latestMap = new LinkedHashMap<>();
        for (FlowProcess process : processes) {
            if (ProcessState.HISTORY.value().equals(process.getProcessState())) {
                continue;
            }
            String processKey = process.getProcessKey();
            FlowProcess exists = latestMap.get(processKey);
            if (exists == null || compareProcessVersion(process, exists) > 0) {
                latestMap.put(processKey, process);
            }
        }
        return new ArrayList<>(latestMap.values());
    }

    private int compareProcessVersion(FlowProcess left, FlowProcess right) {
        int leftVersion = left.getProcessVersion() == null ? 0 : left.getProcessVersion();
        int rightVersion = right.getProcessVersion() == null ? 0 : right.getProcessVersion();
        if (leftVersion != rightVersion) {
            return leftVersion - rightVersion;
        }
        long leftId = left.getId() == null ? 0L : left.getId();
        long rightId = right.getId() == null ? 0L : right.getId();
        return Long.compare(leftId, rightId);
    }

    /**
     * 查询流程定义版本记录。
     *
     * @param tenantId 租户 ID
     * @param processKey 流程 key
     * @return 版本记录
     */
    public List<FlowProcessVO> listVersions(String tenantId, String processKey) {
        if (StrUtil.isBlank(processKey)) {
            throw new IllegalArgumentException("流程 key 不能为空");
        }
        List<FlowProcess> processes = flowEngine.processService()
                .getProcessVersions(StrUtil.trimToNull(tenantId), StrUtil.trim(processKey));
        List<FlowProcessVO> records = new ArrayList<>();
        if (processes != null) {
            for (FlowProcess process : processes) {
                records.add(FlowProcessVO.of(process));
            }
        }
        return records;
    }

    /**
     * 启用流程定义。
     *
     * @param processId 流程定义 ID
     * @return 更新后的流程定义
     */
    public FlowProcess enable(Long processId) {
        flowEngine.processService().enableProcess(processId);
        return flowEngine.processService().getProcessById(processId);
    }

    /**
     * 禁用流程定义。
     *
     * @param processId 流程定义 ID
     * @return 更新后的流程定义
     */
    public FlowProcess disable(Long processId) {
        flowEngine.processService().disableProcess(processId);
        return flowEngine.processService().getProcessById(processId);
    }

    /**
     * 修改流程基础信息。
     *
     * @param processId 流程定义 ID
     * @param request 修改请求
     * @return 更新后的流程定义
     */
    public FlowProcess updateInfo(Long processId, FlowProcessInfoUpdateRequest request) {
        if (processId == null) {
            throw new IllegalArgumentException("流程定义 ID 不能为空");
        }
        if (request == null) {
            throw new IllegalArgumentException("流程基础信息不能为空");
        }
        FlowProcess update = new FlowProcess();
        update.setId(processId);
        update.setProcessName(StrUtil.trimToNull(request.getProcessName()));
        update.setProcessIcon(StrUtil.trimToNull(request.getProcessIcon()));
        update.setProcessType(StrUtil.trimToNull(request.getProcessType()));
        update.setRemark(StrUtil.trimToNull(request.getRemark()));
        flowEngine.processService().updateProcessInfo(update);
        return flowEngine.processService().getProcessById(processId);
    }

    private FlowCreator currentCreator() {
        FlowCreator creator = creatorProvider == null ? null : creatorProvider.getCurrentCreator();
        if (creator == null || StrUtil.isBlank(creator.getCreateId())) {
            throw new IllegalStateException("未配置 FlowCreatorProvider，无法获取当前发布人");
        }
        return creator;
    }

    private void assertPublishRequest(FlowProcessPublishRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("流程发布信息不能为空");
        }
        if (StrUtil.isBlank(request.getProcessName())) {
            throw new IllegalArgumentException("流程名称不能为空");
        }
        if (StrUtil.isBlank(request.getProcessKey())) {
            throw new IllegalArgumentException("流程 key 不能为空");
        }
        if (request.getModelContent() == null || StrUtil.isBlank(modelContent(request.getModelContent()))) {
            throw new IllegalArgumentException("流程 JSON 不能为空");
        }
    }

    private String modelContent(Object modelContent) {
        if (modelContent instanceof String) {
            return (String) modelContent;
        }
        return FlowContext.toJson(modelContent);
    }

    private void applyFormBinding(FlowProcessModel model, FlowFormBindingRequest formBinding) {
        if (model == null || formBinding == null) {
            return;
        }
        if (model.getExtendConfig() == null) {
            model.setExtendConfig(new LinkedHashMap<String, Object>());
        }
        model.getExtendConfig().put("formBinding", formBinding);
    }

    private void syncFormFields(FlowFormBindingRequest formBinding) {
        if (formBinding == null || formService == null) {
            return;
        }
        if (formBinding.getFormKey() == null || formBinding.getFields() == null) {
            return;
        }
        formService.saveByBinding(formBinding);
    }

    private FlowProcessModel resolveProcessModel(String modelContent) {
        FlowProcessModel model = FlowContext.fromJson(modelContent, FlowProcessModel.class);
        if (model != null && model.getNodeConfig() != null) {
            return model;
        }

        FlowNodeModel nodeConfig = FlowContext.fromJson(modelContent, FlowNodeModel.class);
        if (nodeConfig == null || StrUtil.isBlank(nodeConfig.getNodeKey())) {
            return null;
        }

        FlowProcessModel processModel = new FlowProcessModel();
        processModel.setNodeConfig(nodeConfig);
        return processModel;
    }
}
