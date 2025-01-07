package org.nkk.core.enums.common;


import lombok.AllArgsConstructor;

/**
 * 0=工作日 1=周末 2=节假日 3=值班
 */
@AllArgsConstructor
public enum HolidaysEnum implements BaseEnum{
    work(0,  "工作日"),
    weekend(1,  "周末"),
    holidays(2,  "节假日"),
    duty(3,  "值班"),
    ;
    private final Integer value;
    private final String label;

    @Override
    public String value() {
        return String.valueOf(value);
    }

    @Override
    public String label() {
        return label;
    }
}
