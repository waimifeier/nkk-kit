package org.nkk.core.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.nkk.core.beans.calendar.CalendarHolidayResp;

import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * 节假日查询工具
 * @author Nkks
 */
@UtilityClass
@Slf4j
public class HolidaysUtils {


    private final String sign = "t4w3bwbtdmqwbrm3h8h8d6xp";

    private final int keyId = 486;

    private final String requestURl = "http://api.moonapi.com/154";


    /**
     *
     * @param date 指定日期,格式为： <b>yyyy-MM-dd</b>, 不传默认是今天
     * @return 返回指定日期具体信息 {@link Pair}
     * key:日期类型(0=工作日 1=周末 2=节假日 3=调班),value：日期描述
     */
    public CalendarHolidayResp asDay(String date){

        String responseBody = HttpRequest.get(requestURl)
                .form("sign",sign)
                .form("keyid",keyId)
                .form("date", StrUtil.isEmpty(date) ? DateUtil.formatDate(new Date()): date)
                .execute()
                .body();

        JSONObject json = JSON.parseObject(responseBody);
        String status = json.getString("status");
        if(!StrUtil.equals("success",status)){
            log.error("获取节假日失败！");
            return null;
        }
        JSONObject data = json.getJSONObject("data");
        return data.toJavaObject(CalendarHolidayResp.class);
    }

    /**
     * <p>判断指定日期是否是工作日</p>
     *
     * @param date 指定日期,格式为： <b>yyyy-MM-dd</b>, 不传默认是今天
     * @return 返回指定日期是否是工作日
     */
    public Boolean isWork(String date) {
        CalendarHolidayResp hx = asDay(date);
        return !Objects.isNull(hx) && Objects.equals(hx.getType(), 0);
    }

    /**
     * <p>判断指定日期是否是节假日(法定节假日，不是周末假期)</p>
     *
     * @param date 指定日期,格式为： <b>yyyy-MM-dd</b>, 不传默认是今天
     * @return 返回指定日期是否是节假日
     */
    public Boolean isHoliday(String date) {
        CalendarHolidayResp hx = asDay(date);
        return !Objects.isNull(hx) && Objects.equals(hx.getType(), 2);
    }

    /**
     * <p>判断指定日期是否是调班</p>
     *
     * @param date 指定日期,格式为： <b>yyyy-MM-dd</b>, 不传默认是今天
     * @return 返回指定日期是否是调班日
     */
    public Boolean isShift(String date) {
        CalendarHolidayResp hx = asDay(date);
        return !Objects.isNull(hx) && Objects.equals(hx.getType(), 3);
    }


    /**
     *
     * @param begin 开始时间
     * @param end 结束时间
     * @return 返回指定时间范围的状态 {@link CalendarHolidayResp}
     */
    public List<CalendarHolidayResp> betweenRange(String begin, String end){
        String responseBody = HttpRequest.get(requestURl)
                .form("apicode","holidaybatch")
                .form("apid",155)
                .form("sign",sign)
                .form("keyid",keyId)
                .form("start",begin )
                .form("end",end )
                .execute()
                .body();

        JSONObject json = JSON.parseObject(responseBody);
        String status = json.getString("status");
        if(!StrUtil.equals("success",status)){
            log.error("获取节假日失败！");
            return null;
        }
        return json.getJSONArray("data").toJavaList(CalendarHolidayResp.class);
    }

}