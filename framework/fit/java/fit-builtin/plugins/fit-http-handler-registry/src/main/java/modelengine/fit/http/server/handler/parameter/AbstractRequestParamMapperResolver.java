/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.handler.parameter;

import modelengine.fit.http.annotation.RequestParam;
import modelengine.fit.http.server.handler.PropertyValueMapper;
import modelengine.fit.http.server.handler.PropertyValueMapperResolver;
import modelengine.fit.http.server.handler.SourceFetcher;
import modelengine.fit.http.server.handler.support.TypeTransformationPropertyValueMapper;
import modelengine.fit.http.server.handler.support.UniqueSourcePropertyValueMapper;
import modelengine.fitframework.ioc.annotation.AnnotationMetadata;
import modelengine.fitframework.ioc.annotation.AnnotationMetadataResolver;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.value.PropertyValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

/**
 * 表示解析带有 {@link RequestParam} 注解的参数的 {@link PropertyValueMapperResolver}。
 *
 * @author 季聿阶
 * @since 2022-08-29
 */
public abstract class AbstractRequestParamMapperResolver extends AbstractPropertyValueMapperResolver {
    /**
     * 通过注解解析器来实例化 {@link AbstractRequestParamMapperResolver}。
     *
     * @param annotationResolver 表示注解解析器的 {@link AnnotationMetadataResolver}。
     * @throws IllegalArgumentException 当 {@code annotationResolver} 为 {@code null} 时。
     */
    public AbstractRequestParamMapperResolver(AnnotationMetadataResolver annotationResolver) {
        super(annotationResolver);
    }

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return RequestParam.class;
    }

    @Override
    protected Optional<PropertyValueMapper> resolve(PropertyValue propertyValue, AnnotationMetadata annotations) {
        boolean isArray = this.isArray(propertyValue);
        RequestParam requestParam = annotations.getAnnotation(RequestParam.class);
        SourceFetcher sourceFetcher = this.createSourceFetcher(requestParam, propertyValue);
        PropertyValueMapper mapper = new UniqueSourcePropertyValueMapper(sourceFetcher, isArray);
        TypeTransformationPropertyValueMapper typeTransformationHttpMapper =
                new TypeTransformationPropertyValueMapper(mapper, propertyValue.getParameterizedType());
        return Optional.of(typeTransformationHttpMapper);
    }

    /**
     * 解析参数名，支持 fallback 机制。
     * <p>
     * 优先级：注解的 name > 注解的 value > 方法参数名
     *
     * @param requestParam 表示数据参数上的注解的 {@link RequestParam}。
     * @param propertyValue 表示属性值的 {@link PropertyValue}，用于获取参数名。
     * @return 表示解析后的参数名的 {@link String}。
     */
    protected String resolveParamName(RequestParam requestParam, PropertyValue propertyValue) {
        // 1. 优先使用 name 属性
        String name = requestParam.name();
        if (StringUtils.isNotBlank(name)) {
            return name;
        }

        // 2. 尝试 value 属性（name 和 value 通过@Forward 关联，通常是同一个值）
        name = requestParam.value();
        if (StringUtils.isNotBlank(name)) {
            return name;
        }

        // 3. 使用参数名作为 fallback
        if (propertyValue != null && propertyValue.getElement().isPresent()) {
            AnnotatedElement element = propertyValue.getElement().get();
            if (element instanceof Parameter) {
                name = ((Parameter) element).getName();
                if (StringUtils.isNotBlank(name)) {
                    return name;
                }
            }
        }

        // 4. 如果都为空，返回空字符串（由下游的 Fetcher 抛出更清晰的异常）
        return StringUtils.EMPTY;
    }

    /**
     * 判断当前的值是否为一个数组。
     *
     * @param propertyValue 表示当前的值得 {@link PropertyValue}。
     * @return 如果当前值是数组，则返回 {@code true}，否则，返回 {@code false}。
     */
    protected boolean isArray(PropertyValue propertyValue) {
        return List.class.isAssignableFrom(propertyValue.getType());
    }

    /**
     * 创建一个数据来源的获取器。
     *
     * @param requestParam 表示数据参数上的注解的 {@link RequestParam}。
     * @param propertyValue 表示属性值的 {@link PropertyValue}，用于获取参数名。
     * @return 表示创建出来的数据来源的获取器的 {@link SourceFetcher}。
     */
    protected abstract SourceFetcher createSourceFetcher(RequestParam requestParam, PropertyValue propertyValue);
}