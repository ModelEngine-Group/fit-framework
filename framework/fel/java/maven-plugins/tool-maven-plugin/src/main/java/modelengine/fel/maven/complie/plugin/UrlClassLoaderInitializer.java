/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.maven.complie.plugin;

import static modelengine.fit.serialization.json.jackson.JacksonObjectSerializer.DEFAULT_DATE_FORMAT;
import static modelengine.fit.serialization.json.jackson.JacksonObjectSerializer.DEFAULT_DATE_TIME_FORMAT;

import com.fasterxml.jackson.databind.ObjectMapper;

import modelengine.fel.maven.complie.util.JsonUtils;
import modelengine.fel.tool.ToolSchema;
import modelengine.fel.tool.info.entity.ToolJsonEntity;
import modelengine.fit.serialization.json.jackson.JacksonObjectSerializer;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.protocol.jar.Jar;
import modelengine.fitframework.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 提供初始化类加载器和输出配置文件的能力。
 *
 * @author 曹嘉美
 * @author 杭潇
 * @since 2024-11-01
 */
public class UrlClassLoaderInitializer {
    private static final Logger log = Logger.get(UrlClassLoaderInitializer.class);

    /**
     * 初始化 URLClassLoader。
     *
     * @param outputDirectory 表示输出目录的 {@link String}。
     * @param fitRootDirectory 表示 FIT 根目录的 {@link String}。
     * @return 返回初始化后的 {@link URLClassLoader}。
     * @throws IOException 当读取文件或者创建 URLClassLoader 发生输入输出异常时。
     */
    public URLClassLoader initUrlClassLoader(String outputDirectory, String fitRootDirectory) throws IOException {
        List<URL> urls = new LinkedList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(outputDirectory, fitRootDirectory))) {
            List<File> files = paths.filter(Files::isRegularFile).map(Path::toFile).toList();
            for (File file : files) {
                if (FileUtils.isJar(file)) {
                    urls.add(Jar.from(file).location().toUrl());
                }
            }
            urls.add(Paths.get(outputDirectory).toUri().toURL());
        }
        return new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
    }

    /**
     * 输出工具配置文件。
     *
     * @param outputDirectory 表示输出目录的 {@link String}。
     * @param toolJsonEntity 表示工具配置的 {@link ToolJsonEntity}。
     * @throws IOException 当读取文件或者创建 URLClassLoader 发生输入输出异常时。
     */
    public void outputToolManifest(String outputDirectory, ToolJsonEntity toolJsonEntity) throws IOException {
        if (toolJsonEntity == null) {
            return;
        }
        File jsonFile = Paths.get(outputDirectory, ToolSchema.TOOL_MANIFEST).toFile();
        JacksonObjectSerializer serializer = new JacksonObjectSerializer(
                DEFAULT_DATE_TIME_FORMAT,
                DEFAULT_DATE_FORMAT,
                "Asia/Shanghai"
        );
        serializer.getMapper().writerWithDefaultPrettyPrinter().writeValue(jsonFile, toolJsonEntity);
        log.info("Write tool json successfully. [file={}]", jsonFile.getName());
    }
}
