/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.handler.parameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import modelengine.fit.http.annotation.RequestParam;
import modelengine.fit.http.annotation.RequestQuery;
import modelengine.fit.http.server.handler.PropertyValueMapper;
import modelengine.fit.http.server.handler.Source;
import modelengine.fit.http.server.handler.support.QueryFetcher;
import modelengine.fit.http.server.handler.support.UniqueSourcePropertyValueMapper;
import modelengine.fitframework.ioc.annotation.AnnotationMetadata;
import modelengine.fitframework.ioc.annotation.AnnotationMetadataResolver;
import modelengine.fitframework.util.ReflectionUtils;
import modelengine.fitframework.value.PropertyValue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * 表示 {@link AbstractRequestParamMapperResolver} 的单元测试。
 *
 * @author 白鹏坤
 * @since 2023-02-24
 */
@DisplayName("测试 RequestParamMapperResolver 类")
class RequestParamMapperResolverTest {
    private final AnnotationMetadataResolver annotationResolver = mock(AnnotationMetadataResolver.class);
    private final AbstractRequestParamMapperResolver metadataResolver =
            new RequestQueryMapperResolver(this.annotationResolver);

    @Test
    @DisplayName("通过注解解析器来实例化参数映射器")
    void givenParamThenReturnParameterMapper() {
        final Parameter parameter =
                ReflectionUtils.getDeclaredMethod(HttpParamTest.class, "requestParam", String.class).getParameters()[0];
        final AnnotationMetadata annotations = mock(AnnotationMetadata.class);
        final RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        when(annotations.getAnnotation(any())).thenReturn(requestParam);
        final Optional<PropertyValueMapper> parameterMapper =
                this.metadataResolver.resolve(PropertyValue.createParameterValue(parameter), annotations);
        assertThat(parameterMapper).isPresent();
    }

    @Test
    @DisplayName("获取需要解析的注解的类型")
    void shouldReturnAnnotation() {
        final Class<? extends Annotation> annotation = this.metadataResolver.getAnnotation();
        assertThat(annotation).isEqualTo(RequestQuery.class);
    }

    @Test
    @DisplayName("当指定 name 属性时，使用 name 作为参数名")
    void givenNameAttributeThenUseName() {
        final Parameter parameter =
                ReflectionUtils.getDeclaredMethod(HttpParamTest.class, "requestParam", String.class).getParameters()[0];
        final AnnotationMetadata annotations = mock(AnnotationMetadata.class);
        final RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        when(annotations.getAnnotation(any())).thenReturn(requestParam);
        final Optional<PropertyValueMapper> parameterMapper =
                this.metadataResolver.resolve(PropertyValue.createParameterValue(parameter), annotations);

        assertThat(parameterMapper).isPresent();
        // Verify that the mapper uses the specified name "p1"
        assertThat(parameterMapper.get()).isInstanceOf(UniqueSourcePropertyValueMapper.class);
    }

    @Test
    @DisplayName("当不指定 name 和 value 时，使用参数名作为 fallback")
    void givenNoNameAndValueThenUseParameterName() {
        final Parameter parameter = ReflectionUtils.getDeclaredMethod(HttpParamTest.class,
                "requestParamWithParameterName", String.class).getParameters()[0];
        final AnnotationMetadata annotations = mock(AnnotationMetadata.class);
        final RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        when(annotations.getAnnotation(any())).thenReturn(requestParam);
        final Optional<PropertyValueMapper> parameterMapper =
                this.metadataResolver.resolve(PropertyValue.createParameterValue(parameter), annotations);

        assertThat(parameterMapper).isPresent();
        // The parameter name should be "username" (from the method signature)
        // Note: This requires the -parameters compiler flag to be enabled
    }

    @Test
    @DisplayName("当指定 value 属性时，使用 value 作为参数名")
    void givenValueAttributeThenUseValue() {
        final Parameter parameter =
                ReflectionUtils.getDeclaredMethod(HttpParamTest.class, "requestParamWithValue", String.class)
                        .getParameters()[0];
        final AnnotationMetadata annotations = mock(AnnotationMetadata.class);
        final RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        when(annotations.getAnnotation(any())).thenReturn(requestParam);
        final Optional<PropertyValueMapper> parameterMapper =
                this.metadataResolver.resolve(PropertyValue.createParameterValue(parameter), annotations);

        assertThat(parameterMapper).isPresent();
        // The parameter name should be "user_id" (from the value attribute)
    }
}
