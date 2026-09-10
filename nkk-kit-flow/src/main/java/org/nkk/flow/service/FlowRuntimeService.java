package org.nkk.flow.service;

import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.context.FlowExecution;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.FlowProcessModel;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 流程实例服务。
 */
public interface FlowRuntimeService {

    default FlowInstance createInstance(FlowProcess process, FlowCreator creator, Map<String, Object> args, FlowNodeModel startNode) {
        return createInstance(process, creator, args, startNode, null);
    }

    FlowInstance createInstance(FlowProcess process, FlowCreator creator, Map<String, Object> args,
                                FlowNodeModel startNode, Long parentInstanceId);

    FlowInstance createInstance(FlowProcess process, FlowCreator creator, Map<String, Object> args,
                                FlowNodeModel startNode, Long parentInstanceId, boolean saveAsDraft,
                                Supplier<FlowInstance> supplier);

    FlowProcessModel getProcessModelByInstanceId(Long instanceId);

    boolean updateInstanceModelById(Long instanceId, FlowProcessModel processModel);

    void appendNodeModel(Long taskId, FlowNodeModel nodeModel, boolean beforeAfter);

    boolean removeNodeModel(Long instanceId, String nodeKey, Function<FlowNodeModel, Boolean> checkFunc);

    boolean endInstance(FlowExecution execution, Long instanceId, FlowNodeModel endNode, InstanceState state);

    boolean terminate(Long instanceId, FlowCreator creator);

    default boolean terminate(Long instanceId, FlowTask currentTask, FlowCreator creator) {
        return terminate(instanceId, creator);
    }

    boolean suspendInstanceById(Long instanceId, FlowCreator creator);

    boolean activeInstanceById(Long instanceId, FlowCreator creator);

    boolean reject(Long instanceId, FlowTask currentTask, FlowCreator creator);

    boolean revoke(Long instanceId, FlowTask currentTask, FlowCreator creator);

    boolean timeout(Long instanceId, FlowTask currentTask, FlowCreator creator);

    default boolean destroyByInstanceId(Long instanceId, Map<String, Object> args) {
        return destroyByInstanceId(instanceId, FlowCreator.ADMIN, args);
    }

    boolean destroyByInstanceId(Long instanceId, FlowCreator creator, Map<String, Object> args);

    boolean addVariable(Long instanceId, Map<String, Object> args, Function<FlowInstance, FlowInstance> function);

    void cascadeRemoveByInstanceId(Long instanceId, FlowCreator creator);

    void cascadeRemoveByProcessId(Long processId);

    void updateCurrentNode(FlowTask task);

    FlowInstance resume(Long instanceId, FlowCreator creator, Map<String, Object> args);
}

