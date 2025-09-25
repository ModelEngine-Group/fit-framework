/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.source.support;

import modelengine.fel.core.document.Document;
import modelengine.fel.core.source.AbstractFileSource;
import modelengine.fel.core.source.JsonMetadataExtractor;
import modelengine.fel.core.template.StringTemplate;
import modelengine.fitframework.exception.FitException;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表示 json 文件加载器的实体。
 *
 * @author 易文渊
 * @since 2024-08-08
 */
public class JsonFileSource extends AbstractFileSource {
    private final ObjectSerializer objectSerializer;
    private final StringTemplate stringTemplate;
    private final JsonMetadataExtractor metadataExtractor;

    /**
     * 使用默认的元数据萃取器创建 {@link JsonFileSource} 的实例。
     *
     * @param objectSerializer 表示对象序列化器的 {@link ObjectSerializer}。
     * @param stringTemplate 表示字符串模板的 {@link StringTemplate}。
     */
    public JsonFileSource(ObjectSerializer objectSerializer, StringTemplate stringTemplate) {
        this(objectSerializer, stringTemplate, m -> new HashMap<>());
    }

    /**
     * 根据元数据萃取器创建 {@link JsonFileSource} 的实例。
     *
     * @param objectSerializer 表示对象序列化器的 {@link ObjectSerializer}。
     * @param stringTemplate 表示字符串模板的 {@link StringTemplate}。
     * @param metadataExtractor 表示元数据萃取器的 {@link JsonMetadataExtractor}。
     */
    public JsonFileSource(ObjectSerializer objectSerializer, StringTemplate stringTemplate,
            JsonMetadataExtractor metadataExtractor) {
        this.objectSerializer = objectSerializer;
        this.stringTemplate = stringTemplate;
        this.metadataExtractor = metadataExtractor;
    }

    @Override
    protected List<Document> parse(File file) {
        try {
            String content = FileUtils.content(file);
            List<Map<String, Object>> jsonList = objectSerializer.deserialize(content, Object.class);
            return jsonList.stream().map(json -> {
                Map<String, String> values = new HashMap<>();
                for (String key : stringTemplate.placeholder()) {
                    String value = Validation.notNull(json.get(key), "The key {0} is missing.", key).toString();
                    values.put(key, value);
                }
                String text = stringTemplate.render(values);
                Map<String, Object> metadata = metadataExtractor.apply(json);
                return Document.custom().text(text).metadata(metadata).build();
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new FitException(e);
        }
    }
}