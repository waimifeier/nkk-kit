package org.nkk.core.utils;

import java.text.ParseException;
import java.util.Date;

/**
*   日期工具类
* @author Nkks
* @class DateUtils
* @time 2022/3/23 0023 12:13
*/
public class ParseDateUtils extends org.apache.commons.lang3.time.DateUtils {

    /**
     *  支持转换的日期格式
     */
    public static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 日期型字符串转化为日期 格式 {@link ParseDateUtils#parsePatterns}
     *
     * @author Nkks
     * @time 2022/1/27 14:26
     * @param str str
     * @return {@link Date}
     * @throws ParseException  ParseException
     */
    public static Date parseDateWithException(Object str) throws ParseException {
        if (str == null){
            return null;
        }
        return parseDate(str.toString(), parsePatterns);
    }


}
