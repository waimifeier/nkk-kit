package org.nkk.web.conf.json;

import com.fasterxml.jackson.databind.cfg.PackageVersion;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.nkk.core.enums.common.BaseEnum;
//import org.nkk.web.conf.deSerializer.JacksonEnumDeSerializer;
import org.nkk.web.conf.serializer.JacksonEnumSerializer;

/**
 * 添加 {@link BaseEnum} 的序列化和返序列化模块。
 *
 */
public class CustomModule extends SimpleModule {

    public CustomModule() {
        super(PackageVersion.VERSION);
        this.addSerializer(BaseEnum.class,new JacksonEnumSerializer());
        //this.addDeserializer(Enum.class,new JacksonEnumDeSerializer());
    }



}
