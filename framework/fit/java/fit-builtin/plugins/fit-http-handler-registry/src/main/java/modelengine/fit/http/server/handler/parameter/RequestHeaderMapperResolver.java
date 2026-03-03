/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.handler.parameter;

import modelengine.fit.http.annotation.RequestHeader;
import modelengine.fit.http.annotation.RequestParam;
import modelengine.fit.http.server.handler.PropertyValueMapperResolver;
import modelengine.fit.http.server.handler.SourceFetcher;
import modelengine.fit.http.server.handler.support.HeaderFetcher;
import modelengine.fit.http.server.handler.support.ParamValue;
import modelengine.fitframework.ioc.annotation.AnnotationMetadataResolver;
import modelengine.fitframework.value.PropertyValue;

import java.lang.annotation.Annotation;

/**
 * 表示解析带有 {@link RequestHeader} 注解的参数的 {@link PropertyValueMapperResolver}。
 *
 * @author 季聿阶
 * @since 2022-08-30
 */
public class RequestHeaderMapperResolver extends AbstractRequestParamMapperResolver {
    /**
     * 通过注解解析器来实例化 {@link RequestHeaderMapperResolver}。
     *
     * @param annotationResolver 表示注解解析器的 {@link AnnotationMetadataResolver}。
     * @throws IllegalArgumentException 当 {@code annotationResolver} 为 {@code null} 时。
     */
    public RequestHeaderMapperResolver(AnnotationMetadataResolver annotationResolver) {
        super(annotationResolver);
    }

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return RequestHeader.class;
    }

    @Override
    protected SourceFetcher createSourceFetcher(RequestParam requestParam, PropertyValue propertyValue) {
        // 获取参数名，优先级：name > value > 参数名
        String name = this.resolveParamName(requestParam, propertyValue);

        return new HeaderFetcher(ParamValue.custom()
                .name(name)
                .in(requestParam.in())
                .defaultValue(requestParam.defaultValue())
                .required(requestParam.required())
                .build());
    }
}
