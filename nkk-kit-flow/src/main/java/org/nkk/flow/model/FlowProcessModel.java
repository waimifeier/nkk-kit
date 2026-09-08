package org.nkk.flow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 流程定义 JSON 模型。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowProcessModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程定义唯一编码，同一流程多版本共享该编码。
     */
    private String key;

    /**
     * 流程定义名称。
     */
    private String name;

    /**
     * 流程实例详情页地址。
     */
    private String instanceUrl;

    /**
     * 流程根节点配置。
     */
    private FlowNodeModel nodeConfig;

    /**
     * 流程级扩展配置，业务侧可放置流程分类、表单配置等自定义数据。
     */
    private Map<String, Object> extendConfig;

    /**
     * 根据节点编码查找流程节点。
     *
     * @param nodeKey 节点编码
     * @return 匹配的节点，未找到时返回 null
     */
    public FlowNodeModel getNode(String nodeKey) {
        return nodeConfig == null ? null : nodeConfig.getNode(nodeKey);
    }

    /**
     * 构建节点父子引用，供运行时查找后续节点和父级节点使用。
     */
    public void buildParentNode() {
        if (nodeConfig != null) {
            buildParentNode(nodeConfig);
        }
    }

    /**
     * 清理节点父级引用，避免流程模型序列化时产生循环引用。
     *
     * @return 当前流程模型
     */
    public FlowProcessModel cleanParentNode() {
        if (nodeConfig != null) {
            cleanParentNode(nodeConfig);
        }
        return this;
    }

    /**
     * 递归构建当前节点及子节点的父级引用。
     *
     * @param rootNode 当前根节点
     */
    private void buildParentNode(FlowNodeModel rootNode) {
        buildParentConditionNodes(rootNode, rootNode.getConditionNodes());
        buildParentConditionNodes(rootNode, rootNode.getParallelNodes());
        buildParentConditionNodes(rootNode, rootNode.getInclusiveNodes());
        buildParentConditionNodes(rootNode, rootNode.getRouteNodes());
        if (rootNode.getChildNode() != null) {
            rootNode.getChildNode().setParentNode(rootNode);
            buildParentNode(rootNode.getChildNode());
        }
    }

    /**
     * 构建分支节点下子节点的父级引用。
     *
     * @param parent 分支所属父节点
     * @param nodes 分支节点列表
     */
    private void buildParentConditionNodes(FlowNodeModel parent, java.util.List<FlowConditionNode> nodes) {
        if (nodes == null) {
            return;
        }
        for (FlowConditionNode conditionNode : nodes) {
            if (conditionNode.getChildNode() != null) {
                conditionNode.getChildNode().setParentNode(parent);
                buildParentNode(conditionNode.getChildNode());
            }
        }
    }

    /**
     * 递归清理当前节点及子节点的父级引用。
     *
     * @param node 当前节点
     */
    private void cleanParentNode(FlowNodeModel node) {
        node.setParentNode(null);
        cleanParentConditionNodes(node.getConditionNodes());
        cleanParentConditionNodes(node.getParallelNodes());
        cleanParentConditionNodes(node.getInclusiveNodes());
        cleanParentConditionNodes(node.getRouteNodes());
        if (node.getChildNode() != null) {
            cleanParentNode(node.getChildNode());
        }
    }

    /**
     * 清理分支节点下子节点的父级引用。
     *
     * @param nodes 分支节点列表
     */
    private void cleanParentConditionNodes(java.util.List<FlowConditionNode> nodes) {
        if (nodes == null) {
            return;
        }
        for (FlowConditionNode conditionNode : nodes) {
            if (conditionNode.getChildNode() != null) {
                cleanParentNode(conditionNode.getChildNode());
            }
        }
    }
}

