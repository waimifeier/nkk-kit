package org.nkk.flow.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.nkk.flow.entity.FlowProcess;

import java.util.List;
import java.util.Optional;

/**
 * 流程定义 DAO。
 */
public interface FlowProcessDao {

    boolean insert(FlowProcess process);

    boolean updateById(FlowProcess process);

    boolean deleteById(Long id);

    FlowProcess selectById(Long id);

    List<FlowProcess> selectCurrentList(String tenantId);

    /**
     * 分页查询流程定义原始记录（包含草稿、历史版本）。
     *
     * @param page         分页参数
     * @param tenantId     租户 ID，为空时只查无租户数据
     * @param processName  流程名称，模糊匹配，为空不过滤
     * @param processKey   流程 key，精确匹配，为空不过滤
     * @param processType  流程分类，精确匹配，为空不过滤
     * @param processState 流程状态，精确匹配，为空不过滤
     * @param formSourceType 表单来源类型（form/business），精确匹配，为空不过滤
     * @return 分页结果
     */
    IPage<FlowProcess> selectPage(IPage<FlowProcess> page, String tenantId, String processName,
                                  String processKey, String processType, Integer processState,
                                  String formSourceType);

    List<FlowProcess> selectListByProcessKeyAndVersion(String tenantId, String processKey, Integer version);

    Optional<List<FlowProcess>> selectListByProcessKey(String tenantId, String processKey);
}

