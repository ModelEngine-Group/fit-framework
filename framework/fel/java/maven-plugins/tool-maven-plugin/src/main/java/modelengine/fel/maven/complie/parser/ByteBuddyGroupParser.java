/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.complie.parser;

import modelengine.fel.tool.info.entity.DefinitionGroupEntity;
import modelengine.fel.tool.info.entity.ToolGroupEntity;
import modelengine.fel.tool.info.entity.ToolJsonEntity;
import modelengine.fitframework.jvm.classfile.ClassFile;
import modelengine.fitframework.util.FileUtils;
import modelengine.fitframework.util.StringUtils;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.pool.TypePool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 提供对工具组信息的解析。
 *
 * @since 2024-10-26
 */
public class ByteBuddyGroupParser implements GroupParser {
    private static final String VERSION = "1.0.0";
    private static final String DOT = ".";

    private final TypePool typePool;
    private final String rootPath;

    public ByteBuddyGroupParser(TypePool typePool, String rootPath) {
        this.typePool = typePool;
        this.rootPath = Paths.get(rootPath).normalize().toString();
    }

    @Override
    public ToolJsonEntity parseJson(String outputDirectory) {
        ToolJsonEntity jsonEntity = new ToolJsonEntity();
        jsonEntity.setVersion(VERSION);
        List<File> files = this.scanFiles(outputDirectory);
        List<ToolGroupEntity> toolGroups = new LinkedList<>();
        List<DefinitionGroupEntity> definitionGroups = new LinkedList<>();
        for (File classFile : files) {
            TypeDescription typeDescription = this.getTypeDescription(classFile);
            if (!typeDescription.isInterface()) {
                List<ToolGroupEntity> toolGroupEntity = ByteBuddyToolsParser.parseToolGroup(typeDescription);
                toolGroups.addAll(toolGroupEntity);
                for (TypeDescription.Generic interfaceTypeGeneric : typeDescription.getInterfaces()) {
                    TypeDescription interfaceType = interfaceTypeGeneric.asErasure();
                    parseDefinitionGroup(interfaceType, definitionGroups);
                }
            }
        }
        jsonEntity.setDefinitionGroups(definitionGroups);
        jsonEntity.setToolGroups(toolGroups);
        return jsonEntity;
    }

    private static void parseDefinitionGroup(TypeDescription typeDescription,
            List<DefinitionGroupEntity> definitionGroups) {
        DefinitionGroupEntity definitionGroupEntity = ByteBuddyDefinitionsParser.parseDefinitionGroup(typeDescription);
        if (definitionGroupEntity.getName() != null && !hasDefinitionGroup(definitionGroups, definitionGroupEntity)) {
            definitionGroups.add(definitionGroupEntity);
        }
    }

    private static boolean hasDefinitionGroup(List<DefinitionGroupEntity> definitionGroups,
            DefinitionGroupEntity definitionGroupEntity) {
        for (DefinitionGroupEntity definitionGroup : definitionGroups) {
            if (Objects.equals(definitionGroupEntity.getName(), definitionGroup.getName())) {
                return true;
            }
        }
        return false;
    }

    private List<File> scanFiles(String outputDirectory) {
        try (Stream<Path> paths = Files.walk(Paths.get(outputDirectory))) {
            return paths.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(FileUtils::isClass)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(StringUtils.format("Failed to get class canonical path. [directory={0}]",
                    outputDirectory));
        }
    }

    private TypeDescription getTypeDescription(File classFile) {
        String normalizedPath;
        try {
            normalizedPath = classFile.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalStateException(StringUtils.format("Failed to get class canonical path. [classFile={0}]",
                    classFile));
        }
        if (!normalizedPath.startsWith(rootPath)) {
            throw new IllegalStateException(StringUtils.format("The class is not in root directory. [directory={0}]",
                    rootPath));
        }
        String classFullName = normalizedPath.substring(rootPath.length() + 1,
                normalizedPath.length() - ClassFile.FILE_EXTENSION.length()).replace(File.separator, DOT);
        return this.typePool.describe(classFullName).resolve();
    }
}
