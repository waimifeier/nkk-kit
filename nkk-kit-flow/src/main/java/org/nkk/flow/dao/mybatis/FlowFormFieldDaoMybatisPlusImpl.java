package org.nkk.flow.dao.mybatis;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.nkk.flow.dao.FlowFormFieldDao;
import org.nkk.flow.entity.FlowFormField;
import org.nkk.flow.mapper.FlowFormFieldMapper;

import java.util.List;

/**
 * 流程表单字段元数据 MyBatis-Plus DAO。
 */
public class FlowFormFieldDaoMybatisPlusImpl implements FlowFormFieldDao {

    private final FlowFormFieldMapper mapper;

    public FlowFormFieldDaoMybatisPlusImpl(FlowFormFieldMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insert(FlowFormField field) {
        return mapper.insert(field) > 0;
    }

    @Override
    public boolean updateById(FlowFormField field) {
        return mapper.updateById(field) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return mapper.deleteById(id) > 0;
    }

    @Override
    public FlowFormField selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<FlowFormField> selectListByFormKey(String tenantId, String formKey) {
        return selectListByFormKeyAndVersion(tenantId, formKey, null, null);
    }

    @Override
    public List<FlowFormField> selectListByFormKeyAndVersion(String tenantId, String formKey, Integer formVersion,
                                                            String sourceType) {
        QueryWrapper<FlowFormField> wrapper = new QueryWrapper<>();
        appendTenantCondition(wrapper, tenantId);
        wrapper.eq("form_key", normalize(formKey));
        if (formVersion != null) {
            wrapper.eq("form_version", formVersion);
        }
        if (StrUtil.isNotBlank(sourceType)) {
            wrapper.eq("source_type", normalize(sourceType));
        }
        wrapper.orderByDesc("form_version");
        wrapper.orderByAsc("sort");
        wrapper.orderByAsc("field_key");
        return mapper.selectList(wrapper);
    }

    @Override
    public boolean deleteByFormKeyAndVersion(String tenantId, String formKey, Integer formVersion, String sourceType) {
        QueryWrapper<FlowFormField> wrapper = new QueryWrapper<>();
        appendTenantCondition(wrapper, tenantId);
        wrapper.eq("form_key", normalize(formKey));
        if (formVersion != null) {
            wrapper.eq("form_version", formVersion);
        }
        if (StrUtil.isNotBlank(sourceType)) {
            wrapper.eq("source_type", normalize(sourceType));
        }
        return mapper.delete(wrapper) >= 0;
    }

    private String normalize(String value) {
        return StrUtil.trimToNull(value);
    }

    private void appendTenantCondition(QueryWrapper<FlowFormField> wrapper, String tenantId) {
        String actualTenantId = normalize(tenantId);
        if (actualTenantId == null) {
            wrapper.and(item -> item.isNull("tenant_id").or().eq("tenant_id", ""));
        } else {
            wrapper.eq("tenant_id", actualTenantId);
        }
    }
}
