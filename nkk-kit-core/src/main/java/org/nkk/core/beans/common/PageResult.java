package org.nkk.core.beans.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页结果。
 *
 * <p>纯数据对象，不依赖任何具体分页框架（PageHelper、MyBatis-Plus 等），
 * 由上层模块负责把框架分页对象转换成本类。</p>
 *
 * @param <T> 记录类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 查询总条数
     */
    private Long total;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 结果记录
     */
    private List<T> records;

    /**
     * 扩展字段
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> ext;

    public PageResult() {
    }

    public PageResult(Long total, Long current, Long size, Long pages, List<T> records) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = pages;
        this.records = records;
    }

    /**
     * 构建分页结果，总页数根据总条数和每页条数自动计算。
     *
     * @param total   总条数
     * @param current 当前页码
     * @param size    每页条数
     * @param records 当前页记录
     * @param <T>     记录类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(long total, long current, long size, List<T> records) {
        return new PageResult<>(total, current, size, resolvePages(total, size), records);
    }

    /**
     * 构建空的分页结果。
     *
     * @param current 当前页码
     * @param size    每页条数
     * @param <T>     记录类型
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(long current, long size) {
        return new PageResult<>(0L, current, size, 0L, Collections.<T>emptyList());
    }

    /**
     * 逐元素转换记录类型，保留原有分页信息（total/current/size/pages/ext）。
     *
     * @param mapper 记录转换函数
     * @param <E>    转换后的记录类型
     * @return 转换后的分页结果
     */
    public <E> PageResult<E> map(Function<T, E> mapper) {
        List<E> mappedRecords = records == null
                ? Collections.<E>emptyList()
                : records.stream().map(mapper).collect(Collectors.toList());
        PageResult<E> result = PageResult.of(total, current, size, mappedRecords);
        result.setExt(ext);
        return result;
    }

    private static long resolvePages(long total, long size) {
        if (total <= 0 || size <= 0) {
            return 0L;
        }
        return (total + size - 1) / size;
    }
}
