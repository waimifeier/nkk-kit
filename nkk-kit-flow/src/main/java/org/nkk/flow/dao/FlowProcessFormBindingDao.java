package org.nkk.flow.dao;

import org.nkk.flow.entity.FlowProcessFormBinding;

import java.util.List;

/**
 * 流程表单绑定 DAO。
 */
public interface FlowProcessFormBindingDao {

    boolean insert(FlowProcessFormBinding binding);

    boolean updateById(FlowProcessFormBinding binding);

    boolean deleteById(Long id);

    FlowProcessFormBinding selectById(Long id);

    FlowProcessFormBinding selectByProcessId(Long processId);

    List<FlowProcessFormBinding> selectListByFormKey(String tenantId, String formKey);

    List<FlowProcessFormBinding> selectListByProcessIds(String tenantId, List<Long> processIds);

    boolean deleteByProcessId(Long processId);
}
