package org.nkk.beans.verify;

/**
 * 校验分组
 */
public interface VerifyGroup {

    /**
     * 新增组
     */
    interface Insert {}

    /**
     * 更新组
     */
    interface Update {}

    /**
     * 删除组
     */
    interface Deleted {}

    /**
     * 查询组
     */
    interface Select {}

}

