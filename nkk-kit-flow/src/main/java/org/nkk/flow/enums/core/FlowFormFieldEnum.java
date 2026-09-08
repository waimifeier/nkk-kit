package org.nkk.flow.enums.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nkk.core.enums.common.IEnum;

/**
 * 流程表单字段元数据枚举容器。
 */
public final class FlowFormFieldEnum {

    private FlowFormFieldEnum() {
    }

    /**
     * 字段类型。
     */
    @Getter
    @AllArgsConstructor
    public enum FieldType implements IEnum<String> {

        STRING("string", "文本"),
        NUMBER("number", "数字"),
        BOOLEAN("boolean", "布尔"),
        DATE("date", "日期"),
        DATETIME("datetime", "日期时间"),
        SELECT("select", "单选"),
        MULTI_SELECT("multi_select", "多选"),
        RADIO("radio", "单选按钮"),
        CHECKBOX("checkbox", "复选框"),
        OBJECT("object", "对象"),
        ARRAY("array", "数组");

        private final String value;
        private final String label;

        @Override
        public String value() {
            return value;
        }

        @Override
        public String label() {
            return label;
        }

        public boolean eq(String value) {
            return value != null && this.value.equalsIgnoreCase(value);
        }

        public static FieldType of(String value) {
            return IEnum.resolveKeyOfNullable(FieldType.class, value);
        }
    }

    /**
     * 字段来源类型。
     */
    @Getter
    @AllArgsConstructor
    public enum SourceType implements IEnum<String> {

        CUSTOM("custom", "自定义表单"),
        BUSINESS("business", "业务表单");

        private final String value;
        private final String label;

        @Override
        public String value() {
            return value;
        }

        @Override
        public String label() {
            return label;
        }

        public boolean eq(String value) {
            return value != null && this.value.equalsIgnoreCase(value);
        }

        public static SourceType of(String value) {
            return IEnum.resolveKeyOfNullable(SourceType.class, value);
        }
    }
}
