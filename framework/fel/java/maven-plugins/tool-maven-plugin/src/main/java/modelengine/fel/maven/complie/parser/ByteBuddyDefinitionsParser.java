/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.complie.parser;

import modelengine.fel.tool.annotation.Group;
import modelengine.fel.tool.info.entity.DefinitionEntity;
import modelengine.fel.tool.info.entity.DefinitionGroupEntity;
import modelengine.fel.tool.info.entity.SchemaEntity;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * 提供对定义信息的解析。
 *
 * @author 曹嘉美
 * @author 杭潇
 * @since 2024-10-30
 */
public class ByteBuddyDefinitionsParser {
    /**
     * 解析定义组。
     *
     * @param typeDescription 表示类型的描述的 {@link TypeDescription}。
     * @return 表示定义组实体的 {@link DefinitionGroupEntity}。
     */
    public static DefinitionGroupEntity parseDefinitionGroup(TypeDescription typeDescription) {
        DefinitionGroupEntity definitionGroupEntity = new DefinitionGroupEntity();
        AnnotationDescription.Loadable<Group> groupAnnotation =
                typeDescription.getDeclaredAnnotations().ofType(Group.class);
        if (groupAnnotation == null) {
            return definitionGroupEntity;
        }
        Group group = groupAnnotation.load();
        definitionGroupEntity.setName(group.name());
        definitionGroupEntity.setSummary(group.summary());
        definitionGroupEntity.setDescription(group.description());
        definitionGroupEntity.setExtensions(ParserUtils.parseAttributes(group.extensions()));
        List<DefinitionEntity> definitionEntities = new LinkedList<>();
        for (MethodDescription.InDefinedShape methodDescription : typeDescription.getDeclaredMethods()) {
            Optional<DefinitionEntity> definitionEntity = parseDefinition(methodDescription);
            definitionEntity.ifPresent(definitionEntities::add);
        }
        definitionGroupEntity.setDefinitions(definitionEntities);
        return definitionGroupEntity;
    }

    private static Optional<DefinitionEntity> parseDefinition(MethodDescription.InDefinedShape methodDescription) {
        Optional<SchemaEntity> schemaEntity = ByteBuddySchemaParser.parseMethodSchema(methodDescription);
        if (schemaEntity.isEmpty()) {
            return Optional.empty();
        }
        DefinitionEntity definitionEntity = new DefinitionEntity();
        definitionEntity.setSchema(schemaEntity.get());
        return Optional.of(definitionEntity);
    }
}
