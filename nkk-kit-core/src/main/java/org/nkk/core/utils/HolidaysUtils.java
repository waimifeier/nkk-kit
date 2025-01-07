package org.nkk.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.nkk.core.beans.calendar.CalendarHolidayResp;
import org.nkk.core.enums.common.BaseEnum;
import org.nkk.core.enums.common.HolidaysEnum;

import java.util.*;


/**
 * 节假日查询工具
 * @author Nkks
 * 接口地址： https://oneapi.coderbox.cn/doc/949712861251586
 */
@UtilityClass
@Slf4j
public class HolidaysUtils {

    private final String requestURl = "https://oneapi.coderbox.cn/openapi/public/holiday";


    /**
     *
     * @param date 要查询的日期： 格式<b>yyyy 、 yyyy-MM 、 yyyy-MM-dd</b>, 不传默认是今天
     * @param queryType 查询类型: 1仅节日，只返回节日和补班的日期；2日历，返回每一天的。默认为1。
     * @return 返回指定日期具体信息 {@link CalendarHolidayResp}
     *
     */
    public List<CalendarHolidayResp> ofQueryType(String date,Integer queryType){
        String responseBody = HttpRequest.get(requestURl)
                .form("date", StrUtil.isEmpty(date) ? DateUtil.formatDate(new Date()): date)
                .form("queryType", Objects.isNull(queryType) ? 1 : queryType)
                .execute()
                .body();

        JSONObject json = JSON.parseObject(responseBody);
        Integer code = json.getInteger("code");
        if(!Objects.equals(code, 0)){
            log.error("获取节假日失败！");
            return null;
        }
        JSONArray data = json.getJSONArray("data");
        if(data.isEmpty()){
            return Collections.emptyList();
        }

        List<CalendarHolidayResp> resps = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject object = data.getJSONObject(i);
            resps.add(buildHolidayResp(object));
        }
        return resps;
    }

    /**
     * 构建数据
     * @param object
     * @return 构建结果
     */
    private CalendarHolidayResp buildHolidayResp(JSONObject object){
        CalendarHolidayResp resp = new CalendarHolidayResp();
        String festival = object.getString("festival");
        resp.setName(StrUtil.isEmpty(festival) ? object.getString("description") : festival);

        Integer status = object.getInteger("status"); // 状态：1上班， 2放假
        Integer badDay = object.getInteger("badDay"); // 是否补班：1补班
        Integer statutory = object.getInteger("statutory");// 是否法定节假日：1是

        // // 0=工作日 1=周末 2=节假日 3=值班
        int type = status == 1 ? (Objects.nonNull(badDay) && badDay == 1 ? 3 : 0) : (Objects.nonNull(statutory) && statutory == 1 ? 2 : 1);

        resp.setType(type);
        resp.setName(StrUtil.isEmpty(resp.getName()) ? BaseEnum.resolveKey(HolidaysEnum.class,String.valueOf(type)).label() : resp.getName());
        resp.setWeekDay(object.getInteger("weekDay"));
        resp.setDate(object.getString("date"));
        resp.setLunarDate(object.getString("lunarDate"));
        return resp;
    }

    /**
     * 按照节假日查询 (只返回节假日)
     * @param date 要查询的日期： 格式<b>yyyy 、 yyyy-MM 、 yyyy-MM-dd</b>, 不传默认是今天
     * @return 返回指定日期具体信息 {@link CalendarHolidayResp}
     */
    public List<CalendarHolidayResp> ofHoliday(String date){
        return ofQueryType(date,1);
    }

    /**
     * 查询指定日期（格式必须是：yyyy-MM-dd）数据
     *
     * @param date 日期
     * @return {@link CalendarHolidayResp}
     */
    public CalendarHolidayResp ofDay(String date){
        try {
            DateUtil.parseDate(date);
        }catch (Exception ex){
            throw new IllegalArgumentException("日期格式必须是 yyyy-MM-dd");
        }

        List<CalendarHolidayResp> resps = ofCalendar(date);
        if(CollUtil.isEmpty(resps)){
            throw new RuntimeException("无查询结果");
        }
        return resps.get(0);
    }


    /**
     * 按照日历查询指(返回日历-每一天)
     * @param date 要查询的日期： 格式<b>yyyy 、 yyyy-MM 、 yyyy-MM-dd</b>, 不传默认是今天
     * @return 返回指定日期具体信息 {@link CalendarHolidayResp}
     */
    public List<CalendarHolidayResp> ofCalendar(String date){
        return ofQueryType(date,2);
    }

    /**
     * <p>判断指定日期是否是工作日</p>
     *
     * @param date 指定日期,格式为： <b>yyyy-MM-dd</b>, 不传默认是今天
     * @return 返回指定日期是否是工作日
     */
    public Boolean isWork(String date) {
        CalendarHolidayResp hx = ofDay(date);
        return !Objects.isNull(hx) && Objects.equals(hx.getType(), 0);
    }

    /**
     * <p>判断指定日期是否是节假日(法定节假日，不是周末假期)</p>
     *
     * @param date 指定日期,格式为： <b>yyyy-MM-dd</b>, 不传默认是今天
     * @return 返回指定日期是否是节假日
     */
    public Boolean isHoliday(String date) {
        CalendarHolidayResp hx = ofDay(date);
        return !Objects.isNull(hx) && Objects.equals(hx.getType(), 2);
    }

    /**
     * <p>判断指定日期是否是调班</p>
     *
     * @param date 指定日期,格式为： <b>yyyy-MM-dd</b>, 不传默认是今天
     * @return 返回指定日期是否是调班日
     */
    public Boolean isShift(String date) {
        CalendarHolidayResp hx = ofDay(date);
        return !Objects.isNull(hx) && Objects.equals(hx.getType(), 3);
    }

}