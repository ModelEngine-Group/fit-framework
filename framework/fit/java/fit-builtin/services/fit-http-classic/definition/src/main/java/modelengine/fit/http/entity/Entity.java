/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.entity;

import modelengine.fit.http.HttpMessage;
import modelengine.fit.http.protocol.MimeType;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.model.MultiValueMap;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 表示消息体内的数据。
 *
 * @author 季聿阶
 * @since 2022-07-14
 */
public interface Entity extends Closeable {
    /**
     * 获取实体所属的 Http 消息。
     *
     * @return 表示实体所属的 Http 消息的 {@link HttpMessage}。
     */
    HttpMessage belongTo();

    /**
     * 获取实体的媒体文件类型。
     *
     * @return 表示实体的媒体文件类型的 {@link MimeType}。
     */
    @Nonnull
    MimeType resolvedMimeType();

    /**
     * 获取实体的 Content-Type 额外参数。
     * <p>例如，对于 multipart/form-data，需要返回包含 boundary 参数的 Map。</p>
     *
     * @return 表示实体的 Content-Type 额外参数的 {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >}。
     */
    @Nonnull
    Map<String, String> resolvedParameters();

    /**
     * 通过指定的字节数组，按照 {@link java.nio.charset.StandardCharsets#UTF_8} 创建文本消息体数据。
     *
     * @param httpMessage 表示消息体数据所属的 Http 消息的 {@link HttpMessage}。
     * @param bytes 表示指定字节数组的 {@code byte[]}。
     * @return 表示创建出来的文本消息体数据的 {@link TextEntity}。
     */
    static TextEntity createText(HttpMessage httpMessage, byte[] bytes) {
        return TextEntity.create(httpMessage, bytes);
    }

    /**
     * 通过指定的字节数组，按照指定编码格式创建文本消息体数据。
     *
     * @param httpMessage 表示消息体数据所属的 Http 消息的 {@link HttpMessage}。
     * @param bytes 表示指定字节数组的 {@code byte[]}。
     * @param charset 表示指定编码格式的 {@link Charset}。
     * @return 表示创建出来的文本消息体数据的 {@link TextEntity}。
     */
    static TextEntity createText(HttpMessage httpMessage, byte[] bytes, Charset charset) {
        return TextEntity.create(httpMessage, bytes, charset);
    }

    /**
     * 通过指定的文本内容创建文本消息体数据。
     *
     * @param httpMessage 表示消息体数据所属的 Http 消息的 {@link HttpMessage}。
     * @param content 表示指定文本内容的 {@link String}。
     * @return 表示创建出来的文本消息体数据的 {@link TextEntity}。
     */
    static TextEntity createText(HttpMessage httpMessage, String content) {
        return TextEntity.create(httpMessage, content);
    }

    /**
     * 通过指定的对象，创建对象消息体数据。
     *
     * @param httpMessage 表示消息体数据所属的 Http 消息的 {@link HttpMessage}。
     * @param obj 表示指定对象的 {@link Object}。
     * @return 表示创建出来的对象消息体数据的 {@link ObjectEntity}{@code <?>}。
     */
    static ObjectEntity<?> createObject(HttpMessage httpMessage, Object obj) {
        return ObjectEntity.create(httpMessage, obj);
    }

    /**
     * 通过指定的键值对映射，创建多值格式的消息体数据。
     *
     * @param httpMessage 表示消息体数据所属的 Http 消息的 {@link HttpMessage}。
     * @param map 表示指定的键值对映射的 {@link MultiValueMap}{@code <}{@link String}{@code , }{@link String}{@code >}。
     * @return 表示创建出来的多值格式的消息体数据的 {@link MultiValueEntity}。
     */
    static MultiValueEntity createMultiValue(HttpMessage httpMessage, MultiValueMap<String, String> map) {
        return MultiValueEntity.create(httpMessage, map);
    }

    /**
     * 创建带有附件类型的文件消息体数据，适用于文件下载。
     *
     * @param httpMessage 表示消息体数据所属的 Http 消息的 {@link HttpMessage}。
     * @param filename 表示消息体内容所属文件的名字的 {@link String}。
     * @param in 表示消息体内容所属文件的输入流的 {@link InputStream}。
     * @param length 表示消息体内容所属文件的大小的 {@code long}。
     * @return 表示创建出来的带有附件类型的文件消息体数据的 {@link FileEntity}。
     */
    static FileEntity createAttachedFile(HttpMessage httpMessage, String filename, InputStream in, long length) {
        return FileEntity.createAttachment(httpMessage, filename, in, length);
    }

    /**
     * 创建带有内联类型的文件消息体数据，适用于页面预览。
     *
     * @param httpMessage 表示消息体数据所属的 Http 消息的 {@link HttpMessage}。
     * @param filename 表示消息体内容所属文件的名字的 {@link String}。
     * @param in 表示消息体内容所属文件的输入流的 {@link InputStream}。
     * @param length 表示消息体内容所属文件的大小的 {@code long}。
     * @return 表示创建出来的带有附件类型的文件消息体数据的 {@link FileEntity}。
     */
    static FileEntity createInlineFile(HttpMessage httpMessage, String filename, InputStream in, long length) {
        return FileEntity.createInline(httpMessage, filename, in, length);
    }

    /**
     * 创建可读的二进制消息体数据。
     *
     * @param httpMessage 表示消息体数据所属的 Http 消息的 {@link HttpMessage}。
     * @param in 表示消息体内容的输入流的 {@link InputStream}。
     * @return 表示创建出来的可读的二进制消息体数据的 {@link ReadableBinaryEntity}。
     */
    static ReadableBinaryEntity createBinaryEntity(HttpMessage httpMessage, InputStream in) {
        return ReadableBinaryEntity.create(httpMessage, in);
    }
}
