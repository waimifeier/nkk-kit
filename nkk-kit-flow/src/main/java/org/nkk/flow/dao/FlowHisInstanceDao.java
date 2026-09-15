package org.nkk.flow.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.nkk.flow.entity.FlowHisInstance;

import java.util.Date;
import java.util.List;

/**
 * 历史实例 DAO。
 */
public interface FlowHisInstanceDao {

    boolean insert(FlowHisInstance instance);

    boolean updateById(FlowHisInstance instance);

    boolean deleteById(Long id);

    boolean deleteByIds(List<Long> ids);

    FlowHisInstance selectById(Long id);

    List<FlowHisInstance> selectListByCreateId(String createId);

    List<FlowHisInstance> selectListByProcessId(Long processId);

    List<FlowHisInstance> selectListByParentInstanceId(Long parentInstanceId);

    List<FlowHisInstance> selectListByBusinessKey(String businessKey);

    /**
     * 按流程定义 ID 分页查询历史实例。
     *
     * <p>历史实例表在实例创建时同步插入，覆盖实例全生命周期，故仅查此表即可。</p>
     *
     * @param page           分页参数
     * @param processId      流程定义 ID，必填
     * @param starterName    发起人名称，模糊匹配，为空不过滤
     * @param startTime      发起开始时间（createTime 下限，含），为空不过滤
     * @param endTime        发起结束时间（createTime 上限，含），为空不过滤
     * @param instanceState  实例状态，精确匹配，为空不过滤；取值见
     *                      {@link org.nkk.flow.enums.core.FlowInstanceEnum.InstanceState}：
     *                      0-审批中、1-审批通过、2-审批驳回、3-已撤销
     * @param instanceId     实例 ID，精确匹配，为空不过滤
     * @return 分页结果
     */
    IPage<FlowHisInstance> selectPage(IPage<FlowHisInstance> page, Long processId, String starterName,
                                      Date startTime, Date endTime, Integer instanceState, Long instanceId);
}

