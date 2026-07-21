package org.nkk.web.beans.vos;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.pagehelper.PageInfo;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class PageResult<T> implements Serializable {

    /**
     * 查询总数
     */
    private Long total;

    /**
     * 当前条数
     */
    private Long size;

    /**
     * 总页数
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
    private Map<String,Object> ext;

    public PageResult(PageInfo<T> pageInfo) {
        this.total = pageInfo.getTotal();
        this.size = (long)pageInfo.getSize();
        this.current = (long)pageInfo.getPageNum();
        this.pages = (long)pageInfo.getPages();
        this.records = pageInfo.getList();
    }


    /**
     * 构建空的 PageResult
     * @param pageNum 页码
     * @param size 条数
     */
    public PageResult(Long pageNum,Long size) {
        this.total = 0L;
        this.size = size;
        this.current = pageNum;
        this.pages = 0L;
        this.records = Collections.emptyList();
    }

    public <E> PageResult(PageInfo<E> pageInfo, Function<List<E>, List<T>> function)
    {
        this.records = function.apply(pageInfo.getList());
        this.total = pageInfo.getTotal();
        this.size = (long)pageInfo.getSize();
        this.current = (long)pageInfo.getPageNum();
        this.pages = (long)pageInfo.getPages();
    }

    public <E> PageResult(PageInfo<E> pageInfo, Class<T> clazz) {
        if (!CollectionUtils.isEmpty(pageInfo.getList()))
        {
            this.records = pageInfo.getList().stream().map(item ->
            {
                T t = null;
                try
                {
                    t = clazz.newInstance();
                }
                catch (InstantiationException | IllegalAccessException ex)
                {
                    ex.printStackTrace();
                }
                if(t instanceof Map)
                {
                    t = (T) BeanUtil.beanToMap(item);
                }
                else
                {
                    BeanUtil.copyProperties(item,t);
                }

                return t;
            }).collect(Collectors.toList());
        }
        this.total = pageInfo.getTotal();
        this.size = (long)pageInfo.getSize();
        this.current = (long)pageInfo.getPageNum();
        this.pages = (long)pageInfo.getPages();
    }
}