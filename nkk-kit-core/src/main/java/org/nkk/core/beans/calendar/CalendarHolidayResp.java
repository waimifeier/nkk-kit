package org.nkk.core.beans.calendar;

import lombok.Data;

@Data
public class CalendarHolidayResp {

    /**
     * 分类：0=工作日 2=节假日 3=值班
     */
    private Integer type;
    /**
     * 名称
     */
    private String name;
    /**
     * 日期
     */
    private String date;
}
