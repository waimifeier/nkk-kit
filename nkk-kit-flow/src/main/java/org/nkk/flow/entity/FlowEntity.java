package org.nkk.flow.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程基础实体。
 */
@Data
public class FlowEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID，由流程引擎的 ID 生成器写入。
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 租户 ID，用于多租户场景下的数据隔离。
     */
    private String tenantId;

    /**
     * 创建人 ID。
     */
    private String createId;

    /**
     * 创建人名称。
     */
    private String createBy;

    /**
     * 创建时间。
     */
    private Date createTime;
}


