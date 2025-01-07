package org.nkk.core.beans.calendar;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CalendarHolidayResp {


    /**
     * 名称
     */
    private String name;

    /**
     * 分类：0=工作日 1=周末 2=节假日 3=值班
     */
    private Integer type;

    /**
     * 周内天数（1-7）
     */
    private Integer weekDay;

    /**
     * 日期
     */
    private String date;

    /**
     * 农历日期
     */
    private String lunarDate;
}
