package org.nkk.flow.dao;

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

    List<FlowProcess> selectListByProcessKeyAndVersion(String tenantId, String processKey, Integer version);

    Optional<List<FlowProcess>> selectListByProcessKey(String tenantId, String processKey);
}

