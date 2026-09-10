package org.nkk.flow.core.context;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.io.Serializable;

/**
 * 流程操作人。
 */
@Getter
public class FlowCreator implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tenantId;

    private String createId;

    private String createBy;

    private FlowCreator(String createId, String createBy) {
        this.createId = createId;
        this.createBy = createBy;
    }

    public static FlowCreator of(String createId, String createBy) {
        return of(null, createId, createBy);
    }

    public static FlowCreator of(String tenantId, String createId, String createBy) {
        return new FlowCreator(createId, createBy).tenantId(normalize(tenantId));
    }

    public FlowCreator tenantId(String tenantId) {
        this.tenantId = normalize(tenantId);
        return this;
    }

    private static String normalize(String value) {
        return StrUtil.trimToNull(value);
    }
}

