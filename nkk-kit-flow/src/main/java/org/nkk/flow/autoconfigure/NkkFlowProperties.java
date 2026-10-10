package org.nkk.flow.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 审批流 starter 配置。
 */
@Data
@ConfigurationProperties(prefix = "flow")
public class NkkFlowProperties {

    /**
     * 是否打印启动标识，预留配置。
     */
    private boolean banner = true;

    /**
     * 事件发布配置。
     */
    private Eventing eventing = new Eventing();

    /**
     * 流程实例相关配置。
     */
    private Instance instance = new Instance();

    @Data
    public static class Eventing {

        /**
         * 是否发布任务事件。
         */
        private boolean task = false;

        /**
         * 是否发布实例事件。
         */
        private boolean instance = false;
    }

    @Data
    public static class Instance {

        /**
         * 自定义表单的默认实例详情页地址模板。
         * <p>当发布的流程 {@code formSourceType=form} 且未显式传入 instanceUrl 时，
         * 引擎自动用此值填充。支持 {instanceId} 占位符。
         */
        private String formUrl;
    }
}


