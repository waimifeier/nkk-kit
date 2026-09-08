package org.nkk.flow.dao;

import org.nkk.flow.entity.FlowFormField;

import java.util.List;

/**
 * 流程表单字段元数据 DAO。
 */
public interface FlowFormFieldDao {

    boolean insert(FlowFormField field);

    boolean updateById(FlowFormField field);

    boolean deleteById(Long id);

    FlowFormField selectById(Long id);

    List<FlowFormField> selectListByFormKey(String tenantId, String formKey);

    List<FlowFormField> selectListByFormKeyAndVersion(String tenantId, String formKey, Integer formVersion, String sourceType);

    boolean deleteByFormKeyAndVersion(String tenantId, String formKey, Integer formVersion);

    boolean deleteByFormKeyAndVersion(String tenantId, String formKey, Integer formVersion, String sourceType);
}
