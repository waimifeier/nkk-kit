package org.nkk.flow.web.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.nkk.flow.core.context.FlowCreator;
import org.nkk.flow.core.extension.id.FlowIdGenerator;
import org.nkk.flow.core.extension.identity.FlowCreatorProvider;
import org.nkk.flow.dao.FlowFormFieldDao;
import org.nkk.flow.entity.FlowFormField;
import org.nkk.flow.enums.core.FlowFormFieldEnum;
import org.nkk.flow.web.model.FlowFormBindingRequest;
import org.nkk.flow.web.model.FlowFormFieldSaveItemRequest;
import org.nkk.flow.web.model.FlowFormFieldSaveRequest;
import org.nkk.flow.web.model.FlowFormFieldVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 流程设计器表单字段查询服务。
 */
public class FlowDesignerFormService {

    private final FlowFormFieldDao formFieldDao;
    private final FlowCreatorProvider creatorProvider;
    private final FlowIdGenerator idGenerator;

    public FlowDesignerFormService(FlowFormFieldDao formFieldDao, FlowCreatorProvider creatorProvider,
                                   FlowIdGenerator idGenerator) {
        this.formFieldDao = formFieldDao;
        this.creatorProvider = creatorProvider;
        this.idGenerator = idGenerator;
    }

    /**
     * 查询指定表单的字段元数据。
     *
     * <p>version 为空时默认返回最新版本的字段列表。</p>
     *
     * @param tenantId 租户 ID
     * @param formKey 表单编码
     * @param formVersion 表单版本，空则取最新版本
     * @return 字段元数据列表
     */
    public List<FlowFormFieldVO> list(String tenantId, String formKey, Integer formVersion) {
        return list(tenantId, formKey, formVersion, null);
    }

    /**
     * 查询指定表单的字段元数据。
     *
     * <p>version 为空时默认返回最新版本的字段列表。</p>
     *
     * @param tenantId 租户 ID
     * @param formKey 表单编码
     * @param formVersion 表单版本，空则取最新版本
     * @param sourceType 表单来源类型，custom/business
     * @return 字段元数据列表
     */
    public List<FlowFormFieldVO> list(String tenantId, String formKey, Integer formVersion, String sourceType) {
        String actualTenantId = resolveTenantId(tenantId);
        if (StrUtil.isBlank(formKey)) {
            return Collections.emptyList();
        }
        List<FlowFormField> fields = formFieldDao.selectListByFormKeyAndVersion(actualTenantId, StrUtil.trim(formKey), formVersion, sourceType);
        if (CollUtil.isEmpty(fields)) {
            return Collections.emptyList();
        }
        List<FlowFormField> records = filterByVersion(fields, formVersion);
        List<FlowFormFieldVO> result = new ArrayList<>();
        for (FlowFormField field : records) {
            result.add(FlowFormFieldVO.of(field));
        }
        return result;
    }

    /**
     * 保存指定表单的字段元数据。
     *
     * <p>同一个 formKey + formVersion 会先清空旧数据，再保存新数据。</p>
     *
     * @param formKey 表单编码
     * @param request 保存请求
     * @return 保存后的字段元数据
     */
    public List<FlowFormFieldVO> save(String formKey, FlowFormFieldSaveRequest request) {
        FlowCreator creator = currentCreator();
        if (StrUtil.isBlank(formKey)) {
            throw new IllegalArgumentException("表单编码不能为空");
        }
        if (request == null) {
            throw new IllegalArgumentException("表单字段保存请求不能为空");
        }
        Integer formVersion = request.getFormVersion();
        if (formVersion == null) {
            throw new IllegalArgumentException("表单版本号不能为空");
        }
        String actualTenantId = resolveTenantId(request.getTenantId());
        String actualFormKey = StrUtil.trim(formKey);
        String actualSourceType = resolveSourceType(request.getSourceType(), null).value();
        formFieldDao.deleteByFormKeyAndVersion(actualTenantId, actualFormKey, formVersion, actualSourceType);

        List<FlowFormFieldSaveItemRequest> items = request.getFields();
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<FlowFormFieldVO> result = new ArrayList<>();
        int index = 0;
        for (FlowFormFieldSaveItemRequest item : items) {
            index++;
            FlowFormField field = toEntity(actualTenantId, actualFormKey, formVersion, request.getFormId(),
                    actualSourceType, item, creator, index);
            if (!formFieldDao.insert(field)) {
                throw new IllegalStateException("保存表单字段元数据失败，fieldKey=" + field.getFieldKey());
            }
            result.add(FlowFormFieldVO.of(field));
        }
        result.sort(Comparator
                .comparing(FlowFormFieldVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(FlowFormFieldVO::getFieldKey, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    /**
     * 保存表单绑定内携带的字段元数据。
     *
     * @param binding 表单绑定信息
     * @return 保存后的字段元数据
     */
    public List<FlowFormFieldVO> saveByBinding(FlowFormBindingRequest binding) {
        if (binding == null) {
            return Collections.emptyList();
        }
        FlowFormFieldSaveRequest request = new FlowFormFieldSaveRequest();
        request.setTenantId(null);
        request.setFormId(binding.getFormId());
        request.setFormVersion(binding.getFormVersion());
        request.setSourceType(binding.getSourceType());
        request.setFields(binding.getFields());
        return saveBindingFields(binding.getFormKey(), request);
    }

    private List<FlowFormField> sortFields(List<FlowFormField> fields) {
        fields.sort(Comparator
                .comparing(FlowFormField::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(FlowFormField::getFieldKey, Comparator.nullsLast(String::compareTo)));
        return fields;
    }

    private List<FlowFormFieldVO> saveBindingFields(String formKey, FlowFormFieldSaveRequest request) {
        FlowCreator creator = currentCreator();
        if (StrUtil.isBlank(formKey)) {
            throw new IllegalArgumentException("表单编码不能为空");
        }
        if (request == null) {
            throw new IllegalArgumentException("表单字段保存请求不能为空");
        }
        Integer formVersion = request.getFormVersion();
        if (formVersion == null) {
            throw new IllegalArgumentException("表单版本号不能为空");
        }
        String actualTenantId = resolveTenantId(request.getTenantId());
        String actualFormKey = StrUtil.trim(formKey);
        formFieldDao.deleteByFormKeyAndVersion(actualTenantId, actualFormKey, formVersion);

        List<FlowFormFieldSaveItemRequest> items = request.getFields();
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<FlowFormFieldVO> result = new ArrayList<>();
        int index = 0;
        for (FlowFormFieldSaveItemRequest item : items) {
            index++;
            FlowFormField field = toEntity(actualTenantId, actualFormKey, formVersion, request.getFormId(),
                    request.getSourceType(), item, creator, index);
            if (!formFieldDao.insert(field)) {
                throw new IllegalStateException("保存表单字段元数据失败，fieldKey=" + field.getFieldKey());
            }
            result.add(FlowFormFieldVO.of(field));
        }
        result.sort(Comparator
                .comparing(FlowFormFieldVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(FlowFormFieldVO::getFieldKey, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    private List<FlowFormField> filterByVersion(List<FlowFormField> fields, Integer formVersion) {
        if (CollUtil.isEmpty(fields)) {
            return Collections.emptyList();
        }
        if (formVersion != null) {
            List<FlowFormField> records = new ArrayList<>();
            for (FlowFormField field : fields) {
                if (field != null && formVersion.equals(field.getFormVersion())) {
                    records.add(field);
                }
            }
            return sortFields(records);
        }
        Integer latestVersion = null;
        for (FlowFormField field : fields) {
            if (field == null || field.getFormVersion() == null) {
                continue;
            }
            if (latestVersion == null || field.getFormVersion() > latestVersion) {
                latestVersion = field.getFormVersion();
            }
        }
        if (latestVersion == null) {
            return sortFields(fields);
        }
        List<FlowFormField> records = new ArrayList<>();
        for (FlowFormField field : fields) {
            if (field != null && latestVersion.equals(field.getFormVersion())) {
                records.add(field);
            }
        }
        return sortFields(records);
    }

    private String resolveTenantId(String tenantId) {
        if (StrUtil.isNotBlank(tenantId)) {
            return StrUtil.trimToNull(tenantId);
        }
        FlowCreator creator = creatorProvider == null ? null : creatorProvider.getCurrentCreator();
        if (creator == null) {
            return null;
        }
        return StrUtil.trimToNull(creator.getTenantId());
    }

    private FlowCreator currentCreator() {
        FlowCreator creator = creatorProvider == null ? null : creatorProvider.getCurrentCreator();
        if (creator == null || StrUtil.isBlank(creator.getCreateId())) {
            throw new IllegalStateException("未配置 FlowCreatorProvider，无法获取当前保存人");
        }
        return creator;
    }

    private FlowFormField toEntity(String tenantId, String formKey, Integer formVersion, Long formId,
                                   String sourceType, FlowFormFieldSaveItemRequest item, FlowCreator creator, int index) {
        if (item == null) {
            throw new IllegalArgumentException("表单字段项不能为空");
        }
        if (StrUtil.isBlank(item.getFieldKey())) {
            throw new IllegalArgumentException("字段编码不能为空");
        }
        if (StrUtil.isBlank(item.getFieldName())) {
            throw new IllegalArgumentException("字段名称不能为空");
        }
        if (StrUtil.isBlank(item.getFieldType())) {
            throw new IllegalArgumentException("字段类型不能为空");
        }
        FlowFormFieldEnum.FieldType fieldType = resolveFieldType(item.getFieldType());
        FlowFormFieldEnum.SourceType actualSourceType = resolveSourceType(item.getSourceType(), sourceType);
        FlowFormField field = new FlowFormField();
        field.setId(idGenerator.nextId(null));
        field.setTenantId(tenantId);
        field.setCreateId(creator.getCreateId());
        field.setCreateBy(creator.getCreateBy());
        field.setCreateTime(new Date());
        field.setFormId(formId);
        field.setFormKey(formKey);
        field.setFormVersion(formVersion);
        field.setFieldKey(StrUtil.trim(item.getFieldKey()));
        field.setFieldName(StrUtil.trim(item.getFieldName()));
        field.setFieldType(fieldType.value());
        field.setFieldPath(StrUtil.blankToDefault(StrUtil.trim(item.getFieldPath()), StrUtil.trim(item.getFieldKey())));
        field.setSourceType(actualSourceType.value());
        field.setRequired(item.getRequired() == null ? 0 : item.getRequired());
        field.setOptionsJson(item.getOptionsJson());
        field.setSort(item.getSort() == null ? index : item.getSort());
        field.setRemark(StrUtil.trimToNull(item.getRemark()));
        return field;
    }

    private FlowFormFieldEnum.FieldType resolveFieldType(String fieldType) {
        FlowFormFieldEnum.FieldType type = FlowFormFieldEnum.FieldType.of(StrUtil.trim(fieldType));
        if (type == null) {
            throw new IllegalArgumentException("不支持的字段类型，fieldType=" + fieldType);
        }
        return type;
    }

    private FlowFormFieldEnum.SourceType resolveSourceType(String sourceType, String defaultValue) {
        String actualValue = StrUtil.blankToDefault(StrUtil.trim(sourceType),
                StrUtil.blankToDefault(StrUtil.trim(defaultValue), FlowFormFieldEnum.SourceType.CUSTOM.value()));
        FlowFormFieldEnum.SourceType type = FlowFormFieldEnum.SourceType.of(actualValue);
        if (type == null) {
            throw new IllegalArgumentException("不支持的字段来源类型，sourceType=" + sourceType);
        }
        return type;
    }
}
