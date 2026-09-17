package org.nkk.flow.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.nkk.flow.entity.FlowHisInstance;

import java.util.Collection;
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

    /**
     * 按发起人 ID 分页查询历史实例（我发起的）。
     *
     * @param page          分页参数
     * @param createId      发起人 ID（create_id），必填
     * @param instanceState 审批状态，为空不过滤；0-审批中、1-审批通过、2-审批拒绝、3-已撤销
     * @param startTime     发起时间下限（create_time，含），为空不过滤
     * @param endTime       发起时间上限（create_time，含），为空不过滤
     * @return 分页结果
     */
    IPage<FlowHisInstance> selectPageByCreateId(IPage<FlowHisInstance> page, String createId,
                                                Integer instanceState, Date startTime, Date endTime);

    /**
     * 按实例 ID 集合分页查询历史实例（待我审批 / 抄送给我）。
     *
     * @param page         分页参数
     * @param instanceIds  实例 ID 集合（IN 过滤），为 null 或空时返回空页
     * @param instanceId   实例 ID 精确过滤，为空不过滤
     * @param starterName  发起人名称模糊过滤（create_by LIKE），为空不过滤
     * @param startTime    发起时间下限（create_time，含），为空不过滤
     * @param endTime      发起时间上限（create_time，含），为空不过滤
     * @param processIds   流程定义 ID 集合（IN 过滤，用于 sourceType/processType 间接过滤），为 null 或空不过滤
     * @return 分页结果
     */
    IPage<FlowHisInstance> selectPageByInstanceIds(IPage<FlowHisInstance> page,
                                                    Collection<Long> instanceIds, Long instanceId,
                                                    String starterName, Date startTime, Date endTime,
                                                    Collection<Long> processIds);

    /**
     * 按发起人名称模糊 + 流程定义 ID 集合查询实例 ID 列表（审批记录的前置过滤）。
     *
     * @param createByLike 发起人名称模糊匹配（create_by LIKE），为空不过滤
     * @param processIds   流程定义 ID 集合（IN 过滤），为 null 或空不过滤
     * @return 实例 ID 列表，无匹配时返回空列表
     */
    List<Long> selectIdsByCreateByLikeAndProcessIds(String createByLike, Collection<Long> processIds);
}

