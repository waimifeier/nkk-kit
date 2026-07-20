package org.nkk.web.utils;

import com.github.pagehelper.PageHelper;
import lombok.experimental.UtilityClass;
import org.nkk.core.beans.common.BaseQuery;

@UtilityClass
public class PageUtils extends PageHelper
{
    /**
     * 设置请求分页数据
     */
    public static void startPage(BaseQuery baseQuery)
    {
        Integer pageNum = baseQuery.getCurrent();
        Integer pageSize = baseQuery.getSize();
        PageHelper.startPage(pageNum, pageSize);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }
}
