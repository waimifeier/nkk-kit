package org.nkk.flow.core.context;

import lombok.Getter;
import lombok.Setter;
import org.nkk.flow.entity.FlowInstance;
import org.nkk.flow.entity.FlowTask;
import org.nkk.flow.entity.FlowTaskActor;
import org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState;
import org.nkk.flow.exception.FlowException;
import org.nkk.flow.model.node.FlowNodeModel;
import org.nkk.flow.model.FlowProcessModel;
import org.nkk.flow.model.node.task.TaskNodeModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 流程执行对象，贯穿单次流转过程。
 */
@Getter
@Setter
public class FlowExecution implements Serializable {

    private static final long serialVersionUID = 1L;

    private FlowContext context;

    private FlowProcessModel processModel;

    private FlowCreator flowCreator;

    private FlowInstance flowInstance;

    private FlowTask flowTask;

    private Map<String, Object> args;

    private List<FlowTask> flowTasks = new ArrayList<>();

    private Integer nextTaskType;

    public FlowExecution(FlowContext context, FlowProcessModel processModel, FlowCreator flowCreator,
                         FlowInstance flowInstance, Map<String, Object> args) {
        this.context = context;
        this.processModel = processModel;
        this.flowCreator = flowCreator;
        this.flowInstance = flowInstance;
        this.args = args;
    }

    public void addTasks(List<FlowTask> tasks) {
        if (tasks != null) {
            flowTasks.addAll(tasks);
        }
    }

    public List<FlowTaskActor> getProviderTaskActors(TaskNodeModel nodeModel) {
        return context.getTaskActorProvider().getTaskActors(nodeModel, this);
    }

    public Integer consumeNextTaskType() {
        Integer taskType = nextTaskType;
        nextTaskType = null;
        return taskType;
    }

    public boolean executeNodeModel(String nodeKey) {
        FlowNodeModel nodeModel = processModel.getNode(nodeKey);
        if (nodeModel == null) {
            throw new FlowException("流程模型中不存在节点，nodeKey=" + nodeKey);
        }
        return nodeModel.nextNode()
                .map(next -> next.execute(context, this))
                .orElseGet(() -> endInstance(nodeModel, InstanceState.COMPLETED));
    }

    public boolean endInstance(FlowNodeModel endNode, InstanceState state) {
        return context.getRuntimeService().endInstance(this, flowInstance.getId(), endNode, state);
    }
}

