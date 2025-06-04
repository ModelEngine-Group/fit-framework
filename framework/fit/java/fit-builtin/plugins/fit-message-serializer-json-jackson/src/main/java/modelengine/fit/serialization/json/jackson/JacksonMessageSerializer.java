/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.serialization.json.jackson;

import static java.nio.charset.StandardCharsets.UTF_8;
import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import modelengine.fit.serialization.MessageSerializer;
import modelengine.fit.serialization.util.MessageSerializerUtils;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.conf.Config;
import modelengine.fitframework.conf.runtime.SerializationFormat;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.serialization.SerializationException;
import modelengine.fitframework.util.ArrayUtils;
import modelengine.fitframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 表示 {@link MessageSerializer} 的 Jackson 的实现。
 *
 * @author 梁济时
 * @author 季聿阶
 * @since 2020-11-23
 */
@Component
public class JacksonMessageSerializer implements MessageSerializer {
    private final ObjectSerializer serializer;
    private final ObjectMapper mapper;
    private final Config config;

    /**
     * 构造一个新的 {@link JacksonMessageSerializer} 实例。
     *
     * @param serializer 表示用于序列化和反序列化实例的 {@link ObjectSerializer}。
     * @param config 表示配置的 {@link Config}。
     */
    public JacksonMessageSerializer(@Fit(alias = "jackson") ObjectSerializer serializer, Config config) {
        this.serializer = notNull(serializer, "The Jackson serializer cannot be null.");
        JacksonObjectSerializer jacksonObjectSerializer = cast(this.serializer);
        this.mapper = jacksonObjectSerializer.mapper();
        this.config = notNull(config, "The message serializer config cannot be null.");
    }

    @Override
    public byte[] serializeRequest(Type[] argumentTypes, Object[] arguments) {
        return this.serializer.serialize(arguments, UTF_8);
    }

    @Override
    public Object[] deserializeRequest(Type[] argumentTypes, byte[] serialized) {
        ArrayNode array;
        MessageSerializerUtils.isSupportedLength(serialized.length, this.config);
        try {
            array = this.mapper.readValue(serialized, ArrayNode.class);
        } catch (IOException e) {
            throw new SerializationException("Failed to read JSON request from serialized bytes.", e);
        }
        if (array.size() != argumentTypes.length) {
            throw new SerializationException(StringUtils.format("Total {0} arguments supplied but {1} required.",
                    array.size(),
                    argumentTypes.length));
        }
        Object[] arguments = new Object[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            arguments[i] = this.mapper.convertValue(array.get(i), this.mapper.constructType(argumentTypes[i]));
        }
        return arguments;
    }

    @Override
    public <T> byte[] serializeResponse(Type returnType, T returnData) {
        return this.serializer.serialize(returnData, UTF_8);
    }

    @Override
    public <T> T deserializeResponse(Type returnType, byte[] serialized) {
        if (ArrayUtils.isEmpty(serialized)) {
            return null;
        }
        MessageSerializerUtils.isSupportedLength(serialized.length, this.config);
        return this.serializer.deserialize(serialized, UTF_8, returnType);
    }

    @Override
    public boolean isSupported(Method method) {
        return true;
    }

    @Override
    public int getFormat() {
        return SerializationFormat.JSON.code();
    }
}
