package org.nkk.flow.autoconfigure;

import org.mybatis.spring.annotation.MapperScan;
import org.nkk.flow.dao.FlowExtInstanceDao;
import org.nkk.flow.dao.FlowFormFieldDao;
import org.nkk.flow.dao.FlowHisInstanceDao;
import org.nkk.flow.dao.FlowHisTaskActorDao;
import org.nkk.flow.dao.FlowHisTaskDao;
import org.nkk.flow.dao.FlowInstanceDao;
import org.nkk.flow.dao.FlowProcessDao;
import org.nkk.flow.dao.FlowTaskActorDao;
import org.nkk.flow.dao.FlowTaskDao;
import org.nkk.flow.dao.mybatis.FlowExtInstanceDaoMybatisPlusImpl;
import org.nkk.flow.dao.mybatis.FlowFormFieldDaoMybatisPlusImpl;
import org.nkk.flow.dao.mybatis.FlowHisInstanceDaoMybatisPlusImpl;
import org.nkk.flow.dao.mybatis.FlowHisTaskActorDaoMybatisPlusImpl;
import org.nkk.flow.dao.mybatis.FlowHisTaskDaoMybatisPlusImpl;
import org.nkk.flow.dao.mybatis.FlowInstanceDaoMybatisPlusImpl;
import org.nkk.flow.dao.mybatis.FlowProcessDaoMybatisPlusImpl;
import org.nkk.flow.dao.mybatis.FlowTaskActorDaoMybatisPlusImpl;
import org.nkk.flow.dao.mybatis.FlowTaskDaoMybatisPlusImpl;
import org.nkk.flow.mapper.FlowExtInstanceMapper;
import org.nkk.flow.mapper.FlowFormFieldMapper;
import org.nkk.flow.mapper.FlowHisInstanceMapper;
import org.nkk.flow.mapper.FlowHisTaskActorMapper;
import org.nkk.flow.mapper.FlowHisTaskMapper;
import org.nkk.flow.mapper.FlowInstanceMapper;
import org.nkk.flow.mapper.FlowProcessMapper;
import org.nkk.flow.mapper.FlowTaskActorMapper;
import org.nkk.flow.mapper.FlowTaskMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 默认持久化配置。
 */
@Configuration
@ConditionalOnClass(MapperScan.class)
@MapperScan("org.nkk.flow.mapper")
public class NkkFlowMybatisPlusConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FlowProcessDao flowProcessDao(FlowProcessMapper mapper) {
        return new FlowProcessDaoMybatisPlusImpl(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowInstanceDao flowInstanceDao(FlowInstanceMapper mapper) {
        return new FlowInstanceDaoMybatisPlusImpl(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowHisInstanceDao flowHisInstanceDao(FlowHisInstanceMapper mapper) {
        return new FlowHisInstanceDaoMybatisPlusImpl(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowExtInstanceDao flowExtInstanceDao(FlowExtInstanceMapper mapper) {
        return new FlowExtInstanceDaoMybatisPlusImpl(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowFormFieldDao flowFormFieldDao(FlowFormFieldMapper mapper) {
        return new FlowFormFieldDaoMybatisPlusImpl(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowTaskDao flowTaskDao(FlowTaskMapper mapper) {
        return new FlowTaskDaoMybatisPlusImpl(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowHisTaskDao flowHisTaskDao(FlowHisTaskMapper mapper) {
        return new FlowHisTaskDaoMybatisPlusImpl(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowTaskActorDao flowTaskActorDao(FlowTaskActorMapper mapper) {
        return new FlowTaskActorDaoMybatisPlusImpl(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowHisTaskActorDao flowHisTaskActorDao(FlowHisTaskActorMapper mapper) {
        return new FlowHisTaskActorDaoMybatisPlusImpl(mapper);
    }
}


