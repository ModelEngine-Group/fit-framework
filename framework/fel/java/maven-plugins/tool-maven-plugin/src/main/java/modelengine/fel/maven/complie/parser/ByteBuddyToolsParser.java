/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.complie.parser;

import static modelengine.fel.tool.info.schema.ToolsSchema.FIT;
import static modelengine.fel.tool.info.schema.ToolsSchema.FITABLE_ID;
import static modelengine.fel.tool.info.schema.ToolsSchema.GENERICABLE_ID;
import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.annotation.ToolMethod;
import modelengine.fel.tool.info.entity.ToolEntity;
import modelengine.fel.tool.info.entity.ToolGroupEntity;
import modelengine.fitframework.annotation.Fitable;
import modelengine.fitframework.annotation.Genericable;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 提供对工具信息的解析。
 *
 * @author 杭潇
 * @author 曹嘉美
 * @since 2024-10-30
 */
public class ByteBuddyToolsParser {
    /**
     * 解析工具组信息。
     *
     * @param typeDescription 表示类型的描述的 {@link TypeDescription}。
     * @return 返回工具组信息的 {@link List}{@code <}{@link ToolGroupEntity}{@code >}。
     */
    public static List<ToolGroupEntity> parseToolGroup(TypeDescription typeDescription) {
        List<ToolGroupEntity> toolGroupEntities = new LinkedList<>();
        AnnotationDescription.Loadable<Group> toolGroupAnnotation =
                typeDescription.getDeclaredAnnotations().ofType(Group.class);
        if (toolGroupAnnotation == null) {
            return toolGroupEntities;
        }
        for (TypeDescription.Generic interfaceTypeGeneric : typeDescription.getInterfaces()) {
            TypeDescription interfaceType = interfaceTypeGeneric.asErasure();
            AnnotationDescription.Loadable<Group> defGroupAnnotation =
                    interfaceType.getDeclaredAnnotations().ofType(Group.class);
            if (defGroupAnnotation == null) {
                continue;
            }
            toolGroupEntities.add(buildToolGroupEntity(typeDescription,
                    defGroupAnnotation,
                    toolGroupAnnotation,
                    interfaceType));
        }
        return toolGroupEntities;
    }

    private static ToolGroupEntity buildToolGroupEntity(TypeDescription typeDescription,
            AnnotationDescription.Loadable<Group> defGroupAnnotation,
            AnnotationDescription.Loadable<Group> toolGroupAnnotation, TypeDescription interfaceType) {
        ToolGroupEntity toolGroupEntity = new ToolGroupEntity();
        toolGroupEntity.setDefinitionGroupName(defGroupAnnotation.load().name());
        toolGroupEntity.setName(toolGroupAnnotation.load().name());
        toolGroupEntity.setSummary(toolGroupAnnotation.load().summary());
        toolGroupEntity.setDescription(toolGroupAnnotation.load().description());
        toolGroupEntity.setExtensions(ParserUtils.parseAttributes(toolGroupAnnotation.load().extensions()));
        List<ToolEntity> toolEntities = getToolEntities(typeDescription, interfaceType);
        toolGroupEntity.setTools(toolEntities);
        return toolGroupEntity;
    }

    private static List<ToolEntity> getToolEntities(TypeDescription typeDescription, TypeDescription interfaceType) {
        List<ToolEntity> toolEntities = new LinkedList<>();
        for (MethodDescription defMethodDescription : interfaceType.getDeclaredMethods()) {
            for (MethodDescription toolMethodDescription : typeDescription.getDeclaredMethods()) {
                if (isSameMethod(defMethodDescription, toolMethodDescription)) {
                    parseTool(defMethodDescription, toolMethodDescription).ifPresent(toolEntities::add);
                }
            }
        }
        return toolEntities;
    }

    private static boolean isSameMethod(MethodDescription defMethodDescription,
            MethodDescription toolMethodDescription) {
        return defMethodDescription.getName().equals(toolMethodDescription.getName())
                && defMethodDescription.getDescriptor().equals(toolMethodDescription.getDescriptor());
    }

    private static Optional<ToolEntity> parseTool(MethodDescription defMethodDescription,
            MethodDescription toolMethodDescription) {
        ToolEntity toolEntity = new ToolEntity();
        AnnotationDescription.Loadable<ToolMethod> defMethodAnnotation =
                defMethodDescription.getDeclaredAnnotations().ofType(ToolMethod.class);
        if (defMethodAnnotation == null) {
            return Optional.empty();
        }
        toolEntity.setDefinitionName(defMethodAnnotation.load().name());
        toolEntity.setNamespace(defMethodAnnotation.load().namespace());
        ByteBuddySchemaParser.parseMethodSchema(toolMethodDescription).ifPresent(toolEntity::setSchema);
        toolEntity.setRunnables(parserRunnables(defMethodDescription, toolMethodDescription));
        AnnotationDescription.Loadable<ToolMethod> toolMethodAnnotation =
                toolMethodDescription.getDeclaredAnnotations().ofType(ToolMethod.class);
        if (toolMethodAnnotation == null) {
            return Optional.empty();
        }
        toolEntity.setExtensions(ParserUtils.parseAttributes(toolMethodAnnotation.load().extensions()));
        return Optional.of(toolEntity);
    }

    private static Map<String, Object> parserRunnables(MethodDescription defMethodDescription,
            MethodDescription toolMethodDescription) {
        AnnotationDescription.Loadable<Genericable> genericableAnnotation =
                defMethodDescription.getDeclaredAnnotations().ofType(Genericable.class);
        notNull(genericableAnnotation, "The definition method must contain genericable.");
        AnnotationDescription.Loadable<Fitable> fitableAnnotation =
                toolMethodDescription.getDeclaredAnnotations().ofType(Fitable.class);
        notNull(fitableAnnotation, "The tool method must contain fitable.");
        Map<String, String> fit = new LinkedHashMap<>();
        fit.put(GENERICABLE_ID, genericableAnnotation.load().value());
        fit.put(FITABLE_ID, fitableAnnotation.load().value());
        return cast(Collections.singletonMap(FIT, fit));
    }
}
