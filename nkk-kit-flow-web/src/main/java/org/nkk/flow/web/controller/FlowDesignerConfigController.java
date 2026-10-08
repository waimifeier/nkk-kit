package org.nkk.flow.web.controller;

import org.nkk.core.beans.common.Result;
import org.nkk.flow.web.model.FlowDesignerConfig;
import org.nkk.flow.web.service.FlowDesignerConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程设计器节点配置接口。
 *
 * <p>统一提供设计器节点注册表，同时驱动添加节点弹窗（picker）与节点配置抽屉（properties），
 * 取代旧的 {@code /designer/node-picker} 单一节点列表接口。</p>
 */
@RestController
@RequestMapping("${flow.web.api-prefix:/flow}/designer/config")
public class FlowDesignerConfigController {

    private final FlowDesignerConfigService configService;

    public FlowDesignerConfigController(FlowDesignerConfigService configService) {
        this.configService = configService;
    }

    /**
     * 获取设计器节点配置。
     *
     * <p>返回 {@link FlowDesignerConfig} 节点注册表，前端注入 useFlow 的 designerConfig 后，
     * 由库内部完成字典校验、能力表过滤与缺省回退。</p>
     *
     * @return 设计器节点配置
     */
    @GetMapping
    public Result<FlowDesignerConfig> config() {
        return Result.ok(configService.getDesignerConfig());
    }
}
