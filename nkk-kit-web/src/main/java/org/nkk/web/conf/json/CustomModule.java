package org.nkk.web.conf.json;

import com.fasterxml.jackson.databind.cfg.PackageVersion;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.nkk.web.conf.deSerializer.JacksonEnumDeSerializer;
import org.nkk.web.conf.serializer.JacksonEnumSerializer;
import org.nkk.core.enums.common.BaseEnum;

/**
 * 添加 {@link BaseEnum} 的序列化和返序列化模块。
 *
 * 参照：https://blog.csdn.net/weixin_42645678/article/details/129186806?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-129186806-blog-130738893.235%5Ev38%5Epc_relevant_anti_t3&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-129186806-blog-130738893.235%5Ev38%5Epc_relevant_anti_t3&utm_relevant_index=1
 */
public class CustomModule extends SimpleModule {

    public CustomModule() {
        super(PackageVersion.VERSION);
        this.addSerializer(BaseEnum.class,new JacksonEnumSerializer());
        this.addDeserializer(BaseEnum.class, new JacksonEnumDeSerializer());
    }


}
