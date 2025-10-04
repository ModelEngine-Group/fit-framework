/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.entity.serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import modelengine.fit.http.HttpMessage;
import modelengine.fit.http.entity.EntityReadException;
import modelengine.fit.http.entity.EntitySerializer;
import modelengine.fit.http.entity.EntityWriteException;
import modelengine.fit.http.entity.FileEntity;
import modelengine.fit.http.entity.NamedEntity;
import modelengine.fit.http.entity.PartitionedEntity;
import modelengine.fit.http.entity.TextEntity;
import modelengine.fit.http.entity.support.DefaultNamedEntity;
import modelengine.fit.http.entity.support.DefaultPartitionedEntity;
import modelengine.fit.http.entity.support.DefaultTextEntity;
import modelengine.fit.http.header.ContentType;
import modelengine.fit.http.header.HeaderValue;
import modelengine.fit.http.header.ParameterCollection;
import modelengine.fit.http.header.support.DefaultContentType;
import modelengine.fit.http.header.support.DefaultHeaderValue;
import modelengine.fit.http.header.support.DefaultParameterCollection;
import modelengine.fit.http.util.HttpUtils;
import modelengine.fitframework.util.IoUtils;
import modelengine.fitframework.util.StringUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 为 {@link MultiPartEntitySerializer} 提供单元测试。
 *
 * @author 杭潇
 * @author 季聿阶
 * @since 2023-02-21
 */
@DisplayName("测试 MultiPartEntitySerializer 类")
public class MultiPartEntitySerializerTest {
    private final EntitySerializer<PartitionedEntity> multiPartEntitySerializer = MultiPartEntitySerializer.INSTANCE;
    private final Charset charset = StandardCharsets.UTF_8;
    private final HttpMessage httpMessage = mock(HttpMessage.class);
    private PartitionedEntity entity;

    @AfterEach
    void teardown() throws IOException {
        if (this.entity != null) {
            this.entity.close();
        }
    }

    @Nested
    @DisplayName("测试 serializeEntity() 方法")
    class TestSerialize {
        private EntitySerializer<PartitionedEntity> getSerializer() {
            return MultiPartEntitySerializerTest.this.multiPartEntitySerializer;
        }

        @Test
        @DisplayName("没有设置 Content-Type 时，抛出异常")
        void givenNoContentTypeThenThrowException() {
            List<NamedEntity> list = new ArrayList<>();
            MultiPartEntitySerializerTest.this.entity = new DefaultPartitionedEntity(
                    MultiPartEntitySerializerTest.this.httpMessage, list);
            EntityWriteException entityWriteException = catchThrowableOfType(EntityWriteException.class,
                    () -> this.getSerializer().serializeEntity(MultiPartEntitySerializerTest.this.entity,
                            MultiPartEntitySerializerTest.this.charset));
            assertThat(entityWriteException).hasMessage("The boundary is not present in Content-Type.");
        }

        @Test
        @DisplayName("序列化空的分块实体，返回终止分隔符")
        void givenEmptyEntitiesThenReturnEndBoundary() {
            List<NamedEntity> list = new ArrayList<>();
            MultiPartEntitySerializerTest.this.entity = new DefaultPartitionedEntity(
                    MultiPartEntitySerializerTest.this.httpMessage, list);
            // Mock Content-Type with boundary
            when(MultiPartEntitySerializerTest.this.httpMessage.contentType())
                    .thenReturn(Optional.of(new DefaultContentType(
                            HttpUtils.parseHeaderValue("multipart/form-data; boundary=test-boundary"))));

            byte[] result = this.getSerializer()
                    .serializeEntity(MultiPartEntitySerializerTest.this.entity, MultiPartEntitySerializerTest.this.charset);
            String resultStr = new String(result, MultiPartEntitySerializerTest.this.charset);
            assertThat(resultStr).isEqualTo("----test-boundary--\r\n");
        }

        @Test
        @DisplayName("序列化包含文本字段的分块实体")
        void givenTextFieldThenSerialize() {
            List<NamedEntity> list = new ArrayList<>();
            TextEntity textEntity = new DefaultTextEntity(MultiPartEntitySerializerTest.this.httpMessage, "test-content");
            NamedEntity namedEntity = new DefaultNamedEntity(MultiPartEntitySerializerTest.this.httpMessage,
                    "field-name", textEntity);
            list.add(namedEntity);
            MultiPartEntitySerializerTest.this.entity = new DefaultPartitionedEntity(
                    MultiPartEntitySerializerTest.this.httpMessage, list);

            when(MultiPartEntitySerializerTest.this.httpMessage.contentType())
                    .thenReturn(Optional.of(new DefaultContentType(
                            HttpUtils.parseHeaderValue("multipart/form-data; boundary=test-boundary"))));

            byte[] result = this.getSerializer()
                    .serializeEntity(MultiPartEntitySerializerTest.this.entity, MultiPartEntitySerializerTest.this.charset);
            String resultStr = new String(result, MultiPartEntitySerializerTest.this.charset);

            String expected = """
                    ----test-boundary\r
                    Content-Disposition: form-data; name="field-name"\r
                    \r
                    test-content\r
                    ----test-boundary--\r
                    """;
            assertThat(resultStr).isEqualTo(expected);
        }

        @Test
        @DisplayName("序列化包含文件的分块实体")
        void givenFileFieldThenSerialize() throws IOException {
            List<NamedEntity> list = new ArrayList<>();
            byte[] fileContent = "file content".getBytes(MultiPartEntitySerializerTest.this.charset);
            FileEntity fileEntity = FileEntity.createInline(MultiPartEntitySerializerTest.this.httpMessage,
                    "test.txt", new java.io.ByteArrayInputStream(fileContent), fileContent.length);
            NamedEntity namedEntity = new DefaultNamedEntity(MultiPartEntitySerializerTest.this.httpMessage,
                    "file-field", fileEntity);
            list.add(namedEntity);
            MultiPartEntitySerializerTest.this.entity = new DefaultPartitionedEntity(
                    MultiPartEntitySerializerTest.this.httpMessage, list);

            when(MultiPartEntitySerializerTest.this.httpMessage.contentType())
                    .thenReturn(Optional.of(new DefaultContentType(
                            HttpUtils.parseHeaderValue("multipart/form-data; boundary=test-boundary"))));

            byte[] result = this.getSerializer()
                    .serializeEntity(MultiPartEntitySerializerTest.this.entity, MultiPartEntitySerializerTest.this.charset);
            String resultStr = new String(result, MultiPartEntitySerializerTest.this.charset);

            String expected = """
                    ----test-boundary\r
                    Content-Disposition: form-data; name="file-field"; filename="test.txt"\r
                    Content-Type: text/plain\r
                    \r
                    file content\r
                    ----test-boundary--\r
                    """;
            assertThat(resultStr).isEqualTo(expected);
        }

        @Test
        @DisplayName("序列化混合文本和文件的分块实体")
        void givenMixedFieldsThenSerialize() throws IOException {
            List<NamedEntity> list = new ArrayList<>();

            // Add text field
            TextEntity textEntity = new DefaultTextEntity(MultiPartEntitySerializerTest.this.httpMessage, "text-value");
            NamedEntity textNamedEntity = new DefaultNamedEntity(MultiPartEntitySerializerTest.this.httpMessage,
                    "text-field", textEntity);
            list.add(textNamedEntity);

            // Add file field
            byte[] fileContent = "file data".getBytes(MultiPartEntitySerializerTest.this.charset);
            FileEntity fileEntity = FileEntity.createInline(MultiPartEntitySerializerTest.this.httpMessage,
                    "document.pdf", new java.io.ByteArrayInputStream(fileContent), fileContent.length);
            NamedEntity fileNamedEntity = new DefaultNamedEntity(MultiPartEntitySerializerTest.this.httpMessage,
                    "file-field", fileEntity);
            list.add(fileNamedEntity);

            MultiPartEntitySerializerTest.this.entity = new DefaultPartitionedEntity(
                    MultiPartEntitySerializerTest.this.httpMessage, list);

            when(MultiPartEntitySerializerTest.this.httpMessage.contentType())
                    .thenReturn(Optional.of(new DefaultContentType(
                            HttpUtils.parseHeaderValue("multipart/form-data; boundary=test-boundary"))));

            byte[] result = this.getSerializer()
                    .serializeEntity(MultiPartEntitySerializerTest.this.entity, MultiPartEntitySerializerTest.this.charset);
            String resultStr = new String(result, MultiPartEntitySerializerTest.this.charset);

            String expected = """
                    ----test-boundary\r
                    Content-Disposition: form-data; name="text-field"\r
                    \r
                    text-value\r
                    ----test-boundary\r
                    Content-Disposition: form-data; name="file-field"; filename="document.pdf"\r
                    Content-Type: application/octet-stream\r
                    \r
                    file data\r
                    ----test-boundary--\r
                    """;
            assertThat(resultStr).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("测试 deserializeEntity() 方法")
    class TestDeserialize {
        private EntitySerializer<PartitionedEntity> getSerializer() {
            return MultiPartEntitySerializerTest.this.multiPartEntitySerializer;
        }

        @SuppressWarnings("resource")
        @Test
        @DisplayName("未指定分隔符，抛出异常")
        void givenNoBoundaryThenThrowException() {
            EntityReadException entityReadException = catchThrowableOfType(EntityReadException.class,
                    () -> this.getSerializer()
                            .deserializeEntity("testEntity".getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage));
            assertThat(entityReadException).hasMessage("The boundary is not present.");
        }

        @Nested
        @DisplayName("指定分隔符")
        class GivenBoundary {
            private EntitySerializer<PartitionedEntity> getSerializer() {
                return MultiPartEntitySerializerTest.this.multiPartEntitySerializer;
            }

            @BeforeEach
            void setup() {
                ParameterCollection parameterCollection = new DefaultParameterCollection();
                parameterCollection.set("boundary", "--token");
                HeaderValue headerValue = new DefaultHeaderValue("multipart/form-data", parameterCollection);
                ContentType contentType = new DefaultContentType(headerValue);
                Optional<ContentType> optionalContentType = Optional.of(contentType);
                when(MultiPartEntitySerializerTest.this.httpMessage.contentType()).thenReturn(optionalContentType);
            }

            @SuppressWarnings("resource")
            @Test
            @DisplayName("给定参数中没有任何内容，抛出异常")
            void givenNoAnyDataThenThrowException() {
                String content = "";
                EntityReadException entityReadException = catchThrowableOfType(EntityReadException.class,
                        () -> this.getSerializer()
                                .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                        MultiPartEntitySerializerTest.this.charset,
                                        MultiPartEntitySerializerTest.this.httpMessage));
                assertThat(entityReadException).hasMessage(
                        "Failed to deserialize message body. [mimeType='multipart/*']");
            }

            @SuppressWarnings("resource")
            @Test
            @DisplayName("给定参数中仅包含一个字节，抛出异常")
            void givenOnly1ByteThenThrowException() {
                String content = "1";
                EntityReadException entityReadException = catchThrowableOfType(EntityReadException.class,
                        () -> this.getSerializer()
                                .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                        MultiPartEntitySerializerTest.this.charset,
                                        MultiPartEntitySerializerTest.this.httpMessage));
                assertThat(entityReadException).hasMessage(
                        "Failed to deserialize message body. [mimeType='multipart/*']");
            }

            @SuppressWarnings("resource")
            @Test
            @DisplayName("给定参数中不包含第一个分隔符，抛出异常")
            void givenNoFirstBoundaryThenThrowException() {
                String content = "Content";
                EntityReadException entityReadException = catchThrowableOfType(EntityReadException.class,
                        () -> this.getSerializer()
                                .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                        MultiPartEntitySerializerTest.this.charset,
                                        MultiPartEntitySerializerTest.this.httpMessage));
                assertThat(entityReadException).hasMessage(
                        "Failed to deserialize message body. [mimeType='multipart/*']");
            }

            @SuppressWarnings("resource")
            @Test
            @DisplayName("给定参数中不包含终止分隔符，抛出异常")
            void givenNoEndBoundaryThenThrowException() {
                String content = "----token\r\n" + "Content";
                EntityReadException entityReadException = catchThrowableOfType(EntityReadException.class,
                        () -> this.getSerializer()
                                .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                        MultiPartEntitySerializerTest.this.charset,
                                        MultiPartEntitySerializerTest.this.httpMessage));
                assertThat(entityReadException).hasMessage(
                        "Failed to deserialize message body. [mimeType='multipart/*']");
            }

            @SuppressWarnings("resource")
            @Test
            @DisplayName("给定参数中分隔符后包含2字符后意外终止，抛出异常")
            void given2ExtraBytesAfterBoundaryThenThrowException() {
                String content = "----token  ";
                EntityReadException entityReadException = catchThrowableOfType(EntityReadException.class,
                        () -> this.getSerializer()
                                .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                        MultiPartEntitySerializerTest.this.charset,
                                        MultiPartEntitySerializerTest.this.httpMessage));
                assertThat(entityReadException).hasMessage(
                        "Failed to deserialize message body. [mimeType='multipart/*']");
            }

            @SuppressWarnings("resource")
            @Test
            @DisplayName("给定参数中不包含回车换行符，抛出异常")
            void givenNoCrlfThenThrowException() {
                String content = "----tokenContent----token--";
                EntityReadException entityReadException = catchThrowableOfType(EntityReadException.class,
                        () -> this.getSerializer()
                                .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                        MultiPartEntitySerializerTest.this.charset,
                                        MultiPartEntitySerializerTest.this.httpMessage));
                assertThat(entityReadException).hasMessage(
                        "Failed to deserialize message body. [mimeType='multipart/*']");
            }

            @SuppressWarnings("resource")
            @Test
            @DisplayName("给定参数中包含小型缓冲大小的元数据头，然后异常中断，抛出异常")
            void givenSmallBufferHeaderAndExitThenThrowException() {
                String content = "----token\r\n" + "1".repeat(MultiPartEntitySerializer.SMALL_BUFFER);
                EntityReadException entityReadException = catchThrowableOfType(EntityReadException.class,
                        () -> this.getSerializer()
                                .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                        MultiPartEntitySerializerTest.this.charset,
                                        MultiPartEntitySerializerTest.this.httpMessage));
                assertThat(entityReadException).hasMessage(
                        "Failed to deserialize message body. [mimeType='multipart/*']");
            }

            @Test
            @DisplayName("给定参数中只有终止分隔符，返回空列表")
            void givenOnlyEndBoundaryThenReturnEmptyEntities() {
                String content = "----token--";
                MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                        .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                MultiPartEntitySerializerTest.this.charset,
                                MultiPartEntitySerializerTest.this.httpMessage);
                assertThat(MultiPartEntitySerializerTest.this.entity.entities()).isEmpty();
            }

            @Nested
            @DisplayName("测试文本场景")
            class TestText {
                private EntitySerializer<PartitionedEntity> getSerializer() {
                    return MultiPartEntitySerializerTest.this.multiPartEntitySerializer;
                }

                @Test
                @DisplayName("给定参数中包含前置信息，返回忽略前置信息的文本")
                void givenPreambleThenReturnActualText() {
                    String content = """
                            Preamble\r
                            ----token\r
                            Content-Disposition: form-data\r
                            \r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns(StringUtils.EMPTY, NamedEntity::name)
                            .returns(true, NamedEntity::isText);
                    assertThat(namedEntity.asText().content()).isEqualTo("Content");
                }

                @Test
                @DisplayName("给定参数中包含超长前置信息，返回忽略前置信息的文本")
                void givenLongPreambleThenReturnActualText() {
                    String content = """
                            This is the preamble. It is to be ignored, though it is a handy place for composition \
                            agents to include an explanatory note to non-MIME conformant readers.\r
                            ----token\r
                            Content-Disposition: form-data\r
                            \r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns(StringUtils.EMPTY, NamedEntity::name)
                            .returns(true, NamedEntity::isText);
                    assertThat(namedEntity.asText().content()).isEqualTo("Content");
                }

                @Test
                @DisplayName("给定参数中包含后置信息，返回忽略后置信息的文本")
                void givenEpilogueThenReturnActualText() {
                    String content = """
                            ----token\r
                            Content-Disposition: form-data\r
                            \r
                            Content\r
                            ----token--\r
                            epilogue""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns(StringUtils.EMPTY, NamedEntity::name)
                            .returns(true, NamedEntity::isText);
                    assertThat(namedEntity.asText().content()).isEqualTo("Content");
                }

                @Test
                @DisplayName("给定参数中的分隔符后包含空白信息，返回忽略空白信息的文本")
                void givenExtraTextAfterBoundaryThenReturnActualText() {
                    String content = """
                            ----token  \r
                            Content-Disposition: form-data\r
                            \r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns(StringUtils.EMPTY, NamedEntity::name)
                            .returns(true, NamedEntity::isText);
                    assertThat(namedEntity.asText().content()).isEqualTo("Content");
                }

                @Test
                @DisplayName("给定参数中不包含变量名信息，返回对应的文本")
                void givenNoNameThenReturnActualText() {
                    String content = """
                            ----token\r
                            Content-Disposition: form-data\r
                            \r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns(StringUtils.EMPTY, NamedEntity::name)
                            .returns(true, NamedEntity::isText);
                    assertThat(namedEntity.asText().content()).isEqualTo("Content");
                }

                @Test
                @DisplayName("给定参数中包含类似分隔符的内容，返回对应的文本")
                void givenLikeBoundaryThenReturnActualText() {
                    String content = """
                            ----token\r
                            Content-Disposition: form-data\r
                            \r
                            ----tokem\r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns(StringUtils.EMPTY, NamedEntity::name)
                            .returns(true, NamedEntity::isText);
                    assertThat(namedEntity.asText().content()).isEqualTo("----tokem\r\nContent");
                }

                @Test
                @DisplayName("给定参数中不存在显示位置，返回对应的文本")
                void givenNoContentDispositionThenReturnActualText() {
                    String content = """
                            ----token\r
                            \r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns(StringUtils.EMPTY, NamedEntity::name)
                            .returns(true, NamedEntity::isText);
                    assertThat(namedEntity.asText().content()).isEqualTo("Content");
                }

                @Test
                @DisplayName("给定参数中文件信息为空，返回对应的文本")
                void givenNoFileNameThenReturnActualText() {
                    String content = """
                            ----token\r
                            Content-disposition: form-data; name="key"\r
                            \r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns("key", NamedEntity::name).returns(true, NamedEntity::isText);
                    assertThat(namedEntity.asText().content()).isEqualTo("Content");
                }

                @Test
                @DisplayName("给定参数中包含错误格式的头，忽略，返回对应的文本")
                void givenWrongHeaderThenReturnActualText() {
                    String content = """
                            ----token\r
                            Wrong\r
                            Content-disposition: form-data; name="key"\r
                            \r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns("key", NamedEntity::name).returns(true, NamedEntity::isText);
                    assertThat(namedEntity.asText().content()).isEqualTo("Content");
                }

                @Test
                @DisplayName("给定参数中包含无用的头，忽略，返回对应的文本")
                void givenUnusedHeaderThenReturnActualText() {
                    String content = """
                            ----token\r
                            Unused: value\r
                            Content-disposition: form-data; name="key"\r
                            \r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns("key", NamedEntity::name).returns(true, NamedEntity::isText);
                    assertThat(namedEntity.asText().content()).isEqualTo("Content");
                }
            }

            @Nested
            @DisplayName("测试文件场景")
            class TestFile {
                private EntitySerializer<PartitionedEntity> getSerializer() {
                    return MultiPartEntitySerializerTest.this.multiPartEntitySerializer;
                }

                @Test
                @DisplayName("给定参数中包含文件名，返回对应的文件流")
                void givenFileNameThenReturnActualFile() throws IOException {
                    String content = """
                            ----token\r
                            Content-Disposition: form-data; name="key"; filename="test.txt"\r
                            \r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns("key", NamedEntity::name).returns(true, NamedEntity::isFile);
                    FileEntity fileEntity = namedEntity.asFile();
                    assertThat(IoUtils.content(fileEntity.getInputStream())).isEqualTo("Content");
                    assertThat(fileEntity.filename()).isEqualTo("test.txt");
                }

                @Test
                @DisplayName("给定参数中包含超长文件名，返回对应的文件流")
                void givenLongFileNameThenReturnActualFile() throws IOException {
                    String fileName = "abcdefghijklmnopqrstuvwxyz0123456789.txt";
                    String content = "----token\r\n" + "Content-Disposition: form-data; name=\"long-file\"; filename=\""
                            + fileName + "\"\r\n" + "\r\n" + "Content\r\n" + "----token--";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns("long-file", NamedEntity::name).returns(true, NamedEntity::isFile);
                    FileEntity fileEntity = namedEntity.asFile();
                    assertThat(IoUtils.content(fileEntity.getInputStream())).isEqualTo("Content");
                    assertThat(fileEntity.filename()).isEqualTo(fileName);
                }

                @Test
                @DisplayName("给定参数中换行符处于缓存边界，返回对应的文件流")
                void givenNewlineCharAtCacheBoundaryThenReturnActualFile() throws IOException {
                    String content = """
                            ----token\r
                            Content-Disposition: form-data; name="key"; filename="test.txt"\r
                            \r
                            1234567890\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(1);
                    NamedEntity namedEntity = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity).returns("key", NamedEntity::name).returns(true, NamedEntity::isFile);
                    FileEntity fileEntity = namedEntity.asFile();
                    assertThat(IoUtils.content(fileEntity.getInputStream())).isEqualTo("1234567890");
                    assertThat(fileEntity.filename()).isEqualTo("test.txt");
                }
            }

            @Nested
            @DisplayName("测试混合场景")
            class TestMixed {
                private EntitySerializer<PartitionedEntity> getSerializer() {
                    return MultiPartEntitySerializerTest.this.multiPartEntitySerializer;
                }

                @Test
                @DisplayName("给定参数中包含文件名和文本，返回对应的文件和文本")
                void givenFileNameThenReturnActualFile() throws IOException {
                    String content = """
                            ----token\r
                            Content-Disposition: form-data; name="key"; filename="test.txt"\r
                            \r
                            Content\r
                            ----token\r
                            Content-Disposition: form-data; name="another"\r
                            \r
                            Content\r
                            ----token--""";
                    MultiPartEntitySerializerTest.this.entity = this.getSerializer()
                            .deserializeEntity(content.getBytes(StandardCharsets.UTF_8),
                                    MultiPartEntitySerializerTest.this.charset,
                                    MultiPartEntitySerializerTest.this.httpMessage);
                    assertThat(MultiPartEntitySerializerTest.this.entity.entities()).hasSize(2);
                    NamedEntity namedEntity1 = MultiPartEntitySerializerTest.this.entity.entities().get(0);
                    assertThat(namedEntity1).returns("key", NamedEntity::name).returns(true, NamedEntity::isFile);
                    assertThat(IoUtils.content(namedEntity1.asFile().getInputStream())).isEqualTo("Content");
                    NamedEntity namedEntity2 = MultiPartEntitySerializerTest.this.entity.entities().get(1);
                    assertThat(namedEntity2).returns("another", NamedEntity::name).returns(true, NamedEntity::isText);
                    assertThat(namedEntity2.asText().content()).isEqualTo("Content");
                }
            }
        }
    }
}
