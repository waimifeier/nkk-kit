package org.nkk.web.beans.controller;

import com.github.pagehelper.PageInfo;
import org.nkk.core.beans.common.BaseQuery;
import org.nkk.core.beans.common.Result;
import org.nkk.web.beans.vos.PageResult;
import org.nkk.web.utils.PageUtils;

import java.util.List;
import java.util.function.Function;

/**
 * 基础控制器，提供分页相关方法
 */
public class BaseController {

    /**
     * 自动从请求参数中获取分页信息并启动分页
     */
    protected void startPage(BaseQuery query) {
        PageUtils.startPage(query);
    }

    /**
     * 将查询结果封装为分页响应数据
     */
    protected <T> Result<PageResult<T>> getPageResult(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        return Result.ok(new PageResult<>(pageInfo));
    }

    protected <E,T> Result<PageResult<E>> getPageResult(List<T> list, Function<List<T>, List<E>> function) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        return Result.ok(new PageResult<>(pageInfo, function));
    }

    protected <E,T> Result<PageResult<E>> getPageResult(List<T> list, Class<E> clazz) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        return Result.ok(new PageResult<>(pageInfo,clazz));
    }

}
