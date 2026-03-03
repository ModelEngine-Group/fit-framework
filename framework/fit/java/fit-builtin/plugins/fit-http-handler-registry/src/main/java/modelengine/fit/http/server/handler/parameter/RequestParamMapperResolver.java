/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.handler.parameter;

import modelengine.fit.http.annotation.RequestParam;
import modelengine.fit.http.server.handler.PropertyValueMapperResolver;
import modelengine.fit.http.server.handler.Source;
import modelengine.fit.http.server.handler.SourceFetcher;
import modelengine.fit.http.server.handler.support.CookieFetcher;
import modelengine.fit.http.server.handler.support.FormUrlEncodedEntityFetcher;
import modelengine.fit.http.server.handler.support.HeaderFetcher;
import modelengine.fit.http.server.handler.support.ObjectEntityFetcher;
import modelengine.fit.http.server.handler.support.ParamValue;
import modelengine.fit.http.server.handler.support.PathVariableFetcher;
import modelengine.fit.http.server.handler.support.QueryFetcher;
import modelengine.fitframework.ioc.annotation.AnnotationMetadataResolver;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.value.PropertyValue;

import java.util.Map;
import java.util.function.Function;

/**
 * 表示解析带有 {@link RequestParam} 注解的参数的 {@link PropertyValueMapperResolver}。
 *
 * @author 季聿阶
 * @since 2022-08-29
 */
public class RequestParamMapperResolver extends AbstractRequestParamMapperResolver {
    private static final Map<Source, Function<ParamValue, SourceFetcher>> SOURCE_FETCHER_MAPPING =
            MapBuilder.<Source, Function<ParamValue, SourceFetcher>>get()
                    .put(Source.QUERY, QueryFetcher::new)
                    .put(Source.HEADER, HeaderFetcher::new)
                    .put(Source.COOKIE, CookieFetcher::new)
                    .put(Source.PATH, PathVariableFetcher::new)
                    .put(Source.BODY, ObjectEntityFetcher::new)
                    .put(Source.FORM, FormUrlEncodedEntityFetcher::new)
                    .build();

    /**
     * 通过注解解析器来实例化 {@link RequestParamMapperResolver}。
     *
     * @param annotationResolver 表示注解解析器的 {@link AnnotationMetadataResolver}。
     * @throws IllegalArgumentException 当 {@code annotationResolver} 为 {@code null} 时。
     */
    public RequestParamMapperResolver(AnnotationMetadataResolver annotationResolver) {
        super(annotationResolver);
    }

    @Override
    protected SourceFetcher createSourceFetcher(RequestParam requestParam, PropertyValue propertyValue) {
        // 获取参数名，优先级：name > value > 参数名
        String name = this.resolveParamName(requestParam, propertyValue);

        Function<ParamValue, SourceFetcher> function = SOURCE_FETCHER_MAPPING.get(requestParam.in());
        return function.apply(ParamValue.custom()
                .name(name)
                .in(requestParam.in())
                .defaultValue(requestParam.defaultValue())
                .required(requestParam.required())
                .build());
    }
}
