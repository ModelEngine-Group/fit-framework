/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.serialization.json.jackson.custom;

import static modelengine.fitframework.util.ObjectUtils.cast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.ext.Java7Support;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.util.NameTransformer;

import modelengine.fitframework.annotation.Aliases;
import modelengine.fitframework.annotation.Property;
import modelengine.fitframework.serialization.annotation.SerializeStrategy;
import modelengine.fitframework.serialization.annotation.Unwrapped;
import modelengine.fitframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 表示支持 FIT 注解的 jackson 注解解析器。
 *
 * @author 易文渊
 * @since 2024-08-11
 */
public class FitAnnotationIntrospector extends AnnotationIntrospector {
    // Jackson 提供的工具类，用以支持 javaBean 注解解析
    private static final Java7Support JAVA7_HELPER = Java7Support.instance();

    @Override
    public Version version() {
        return VersionUtil.parseVersion("3.6.1-SNAPSHOT", "org.fitframework.plugin", "fit-message-serializer-json-jackson");
    }

    @Override
    public JsonInclude.Value findPropertyInclusion(Annotated annotated) {
        SerializeStrategy serializeStrategy = this._findAnnotation(annotated, SerializeStrategy.class);
        JsonInclude.Value value = JsonInclude.Value.empty();
        if (serializeStrategy == null) {
            return value;
        }
        switch (serializeStrategy.include()) {
            case NON_NULL:
                return value.withValueInclusion(JsonInclude.Include.NON_NULL);
            case NON_EMPTY:
                return value.withValueInclusion(JsonInclude.Include.NON_EMPTY);
            case DEFAULT:
            default:
                return value;
        }
    }

    @Override
    public List<PropertyName> findPropertyAliases(Annotated annotated) {
        Aliases aliases = this._findAnnotation(annotated, Aliases.class);
        if (aliases == null) {
            return null;
        }
        return this.findAliases(aliases);
    }

    @Override
    public PropertyName findNameForSerialization(Annotated annotated) {
        return this.findName(annotated);
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated annotated) {
        return this.findName(annotated);
    }

    @Override
    public String[] findEnumValues(MapperConfig<?> config, AnnotatedClass annotatedClass, Enum<?>[] enumValues,
            String[] names) {
        Map<String, String> enumPropertyMap = new HashMap<>();
        for (AnnotatedField field : annotatedClass.fields()) {
            Property property = field.getAnnotation(Property.class);
            if (property != null) {
                String propName = property.name();
                if (StringUtils.isNotEmpty(propName)) {
                    enumPropertyMap.put(field.getName(), propName);
                }
            }
        }
        for (int i = 0; i < enumValues.length; ++i) {
            String enumName = enumValues[i].name();
            String propName = enumPropertyMap.get(enumName);
            if (propName != null) {
                names[i] = propName;
            }
        }
        return names;
    }

    @Override
    public JsonCreator.Mode findCreatorAnnotation(MapperConfig<?> config, Annotated annotated) {
        if (annotated instanceof AnnotatedConstructor && JAVA7_HELPER != null) {
            Boolean flag = JAVA7_HELPER.hasCreatorAnnotation(annotated);
            if (flag != null && flag) {
                return JsonCreator.Mode.PROPERTIES;
            }
        }
        return null;
    }

    @Override
    public String findImplicitPropertyName(AnnotatedMember member) {
        PropertyName name = null;
        if (member instanceof AnnotatedParameter) {
            AnnotatedParameter parameter = cast(member);
            AnnotatedWithParams constructor = parameter.getOwner();
            if (constructor != null && JAVA7_HELPER != null) {
                name = JAVA7_HELPER.findConstructorName(parameter);
            }
        }
        return name == null ? null : name.getSimpleName();
    }

    @Override
    public NameTransformer findUnwrappingNameTransformer(AnnotatedMember member) {
        Unwrapped ann = _findAnnotation(member, Unwrapped.class);
        if (ann == null) {
            return null;
        }
        return NameTransformer.simpleTransformer(ann.prefix(), ann.suffix());
    }

    private PropertyName findName(Annotated annotated) {
        Property property = this._findAnnotation(annotated, Property.class);
        if (property == null || StringUtils.isEmpty(property.name())) {
            return null;
        }
        return PropertyName.construct(property.name());
    }

    private List<PropertyName> findAliases(Aliases aliases) {
        return Arrays.stream(aliases.value())
                .filter(Objects::nonNull)
                .map(alias -> PropertyName.construct(alias.value()))
                .collect(Collectors.toList());
    }
}
