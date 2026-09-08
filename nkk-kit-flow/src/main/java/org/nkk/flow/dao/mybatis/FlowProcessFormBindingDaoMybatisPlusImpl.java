package org.nkk.flow.dao.mybatis;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.nkk.flow.dao.FlowProcessFormBindingDao;
import org.nkk.flow.entity.FlowProcessFormBinding;
import org.nkk.flow.mapper.FlowProcessFormBindingMapper;

import java.util.Collections;
import java.util.List;

/**
 * 流程表单绑定 MyBatis-Plus DAO。
 */
public class FlowProcessFormBindingDaoMybatisPlusImpl implements FlowProcessFormBindingDao {

    private final FlowProcessFormBindingMapper mapper;

    public FlowProcessFormBindingDaoMybatisPlusImpl(FlowProcessFormBindingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(FlowProcessFormBinding binding) {
        return mapper.insert(binding) > 0;
    }

    @Override
    public boolean updateById(FlowProcessFormBinding binding) {
        return mapper.updateById(binding) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return mapper.deleteById(id) > 0;
    }

    @Override
    public FlowProcessFormBinding selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public FlowProcessFormBinding selectByProcessId(Long processId) {
        if (processId == null) {
            return null;
        }
        QueryWrapper<FlowProcessFormBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("process_id", processId);
        List<FlowProcessFormBinding> list = mapper.selectList(wrapper);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<FlowProcessFormBinding> selectListByFormKey(String tenantId, String formKey) {
        QueryWrapper<FlowProcessFormBinding> wrapper = new QueryWrapper<>();
        appendTenantCondition(wrapper, tenantId);
        wrapper.eq(StrUtil.isNotBlank(formKey), "form_key", StrUtil.trimToNull(formKey));
        wrapper.orderByDesc("create_time");
        return mapper.selectList(wrapper);
    }

    @Override
    public List<FlowProcessFormBinding> selectListByProcessIds(String tenantId, List<Long> processIds) {
        if (CollUtil.isEmpty(processIds)) {
            return Collections.emptyList();
        }
        QueryWrapper<FlowProcessFormBinding> wrapper = new QueryWrapper<>();
        appendTenantCondition(wrapper, tenantId);
        wrapper.in("process_id", processIds);
        return mapper.selectList(wrapper);
    }

    @Override
    public boolean deleteByProcessId(Long processId) {
        if (processId == null) {
            return false;
        }
        QueryWrapper<FlowProcessFormBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("process_id", processId);
        return mapper.delete(wrapper) >= 0;
    }

    private void appendTenantCondition(QueryWrapper<FlowProcessFormBinding> wrapper, String tenantId) {
        String actualTenantId = StrUtil.trimToNull(tenantId);
        if (actualTenantId == null) {
            wrapper.and(item -> item.isNull("tenant_id").or().eq("tenant_id", ""));
        } else {
            wrapper.eq("tenant_id", actualTenantId);
        }
    }
}
