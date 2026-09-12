package org.nkk.flow.dao.mybatis;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.nkk.flow.dao.FlowProcessDao;
import org.nkk.flow.entity.FlowProcess;
import org.nkk.flow.enums.core.FlowProcessEnum.ProcessState;
import org.nkk.flow.mapper.FlowProcessMapper;

import java.util.List;
import java.util.Optional;

/**
 * 流程定义 MyBatis-Plus DAO。
 */
public class FlowProcessDaoMybatisPlusImpl implements FlowProcessDao {

    private final FlowProcessMapper mapper;

    public FlowProcessDaoMybatisPlusImpl(FlowProcessMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(FlowProcess process) {
        return mapper.insert(process) > 0;
    }

    @Override
    public boolean updateById(FlowProcess process) {
        return mapper.updateById(process) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return mapper.deleteById(id) > 0;
    }

    @Override
    public FlowProcess selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<FlowProcess> selectCurrentList(String tenantId) {
        QueryWrapper<FlowProcess> wrapper = new QueryWrapper<>();
        appendTenantCondition(wrapper, tenantId);
        wrapper.ne("process_state", ProcessState.HISTORY.value());
        wrapper.orderByAsc("process_type");
        wrapper.orderByAsc("sort");
        wrapper.orderByAsc("process_name");
        wrapper.orderByDesc("process_version");
        return mapper.selectList(wrapper);
    }

    @Override
    public IPage<FlowProcess> selectPage(IPage<FlowProcess> page, String tenantId, String processName,
                                         String processKey, String processType, Integer processState,
                                         String formSourceType) {
        QueryWrapper<FlowProcess> wrapper = new QueryWrapper<>();
        appendTenantCondition(wrapper, tenantId);
        wrapper.like(StrUtil.isNotBlank(processName), "process_name", StrUtil.trim(processName));
        wrapper.eq(StrUtil.isNotBlank(processKey), "process_key", StrUtil.trim(processKey));
        wrapper.eq(StrUtil.isNotBlank(processType), "process_type", StrUtil.trim(processType));
        wrapper.eq(processState != null, "process_state", processState);
        wrapper.eq(StrUtil.isNotBlank(formSourceType), "form_source_type", StrUtil.trim(formSourceType));
        wrapper.orderByAsc("process_type");
        wrapper.orderByAsc("sort");
        wrapper.orderByAsc("process_name");
        wrapper.orderByDesc("process_version");
        wrapper.orderByDesc("id");
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public List<FlowProcess> selectListByProcessKeyAndVersion(String tenantId, String processKey, Integer version) {
        QueryWrapper<FlowProcess> wrapper = new QueryWrapper<>();
        appendTenantCondition(wrapper, tenantId);
        wrapper.eq("process_key", normalize(processKey));
        if (version != null) {
            wrapper.eq("process_version", version);
        }
        wrapper.orderByDesc("process_version");
        return mapper.selectList(wrapper);
    }

    @Override
    public Optional<List<FlowProcess>> selectListByProcessKey(String tenantId, String processKey) {
        return Optional.of(selectListByProcessKeyAndVersion(tenantId, processKey, null));
    }

    private String normalize(String value) {
        return StrUtil.trimToNull(value);
    }

    private void appendTenantCondition(QueryWrapper<FlowProcess> wrapper, String tenantId) {
        String actualTenantId = normalize(tenantId);
        if (actualTenantId == null) {
            wrapper.and(item -> item.isNull("tenant_id").or().eq("tenant_id", ""));
        } else {
            wrapper.eq("tenant_id", actualTenantId);
        }
    }
}

