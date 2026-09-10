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
}


