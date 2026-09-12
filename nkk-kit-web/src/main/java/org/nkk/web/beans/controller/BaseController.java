package org.nkk.web.beans.controller;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageInfo;
import org.nkk.core.beans.common.BaseQuery;
import org.nkk.core.beans.common.PageResult;
import org.nkk.core.beans.common.Result;
import org.nkk.web.utils.PageUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础控制器，提供分页相关方法。
 *
 * <p>分页数据统一使用 {@link PageResult}（nkk-kit-core），
 * PageHelper 的适配逻辑仅保留在 web 层。</p>
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
        return Result.ok(PageResult.of(pageInfo.getTotal(), pageInfo.getPageNum(),
                pageInfo.getSize(), pageInfo.getList()));
    }

    protected <E, T> Result<PageResult<E>> getPageResult(List<T> list, Function<List<T>, List<E>> function) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        return Result.ok(PageResult.of(pageInfo.getTotal(), pageInfo.getPageNum(),
                pageInfo.getSize(), function.apply(pageInfo.getList())));
    }

    @SuppressWarnings("unchecked")
    protected <E, T> Result<PageResult<E>> getPageResult(List<T> list, Class<E> clazz) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        List<T> source = pageInfo.getList();
        List<E> records;
        if (CollectionUtils.isEmpty(source)) {
            records = Collections.emptyList();
        } else {
            records = source.stream().map(item -> {
                E target;
                try {
                    target = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    throw new IllegalStateException("分页结果对象实例化失败: " + clazz.getName(), ex);
                }
                if (target instanceof Map) {
                    return (E) BeanUtil.beanToMap(item);
                }
                BeanUtil.copyProperties(item, target);
                return target;
            }).collect(Collectors.toList());
        }
        return Result.ok(PageResult.of(pageInfo.getTotal(), pageInfo.getPageNum(),
                pageInfo.getSize(), records));
    }

}
