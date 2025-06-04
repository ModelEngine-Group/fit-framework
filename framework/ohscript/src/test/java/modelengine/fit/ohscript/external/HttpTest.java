/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.ohscript.external;

import static modelengine.fitframework.util.ObjectUtils.cast;
import static org.assertj.core.api.Assertions.assertThat;

import modelengine.fit.http.client.HttpClassicClient;
import modelengine.fit.ohscript.script.errors.OhPanic;
import modelengine.fit.ohscript.script.interpreter.ASTEnv;
import modelengine.fit.ohscript.script.interpreter.Oh;
import modelengine.fit.ohscript.script.lexer.Lexer;
import modelengine.fit.ohscript.script.parser.AST;
import modelengine.fit.ohscript.script.parser.GrammarBuilder;
import modelengine.fit.ohscript.script.parser.ParserBuilder;
import modelengine.fit.ohscript.util.OhScriptReader;
import modelengine.fitframework.util.ObjectUtils;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * 用于对外发送 Http 请求的单元测试。
 *
 * @author 季聿阶
 * @since 2023-10-19
 */
@Disabled
@DisplayName("测试对外 Http 调用")
public class HttpTest {
    private ParserBuilder parserBuilder;

    private HttpClassicClient httpClient;

    @BeforeAll
    static void setupAll() {
        TestFitRuntime.INSTANCE.start();
    }

    @BeforeEach
    void setup() {
        GrammarBuilder grammarBuilder = new GrammarBuilder();
        Lexer lexer = new Lexer();
        this.parserBuilder = new ParserBuilder(grammarBuilder, lexer);
    }

    @AfterEach
    void teardown() {
        this.httpClient = null;
        this.parserBuilder = null;
    }

    @Test
    @DisplayName("通过 Get 方法连续获取对象和文本返回值，获取成功")
    void shouldGetTextSuccessfullyAfterGetJson() throws OhPanic {
        this.parserBuilder.addHttpOh("getJson", "get", "http://localhost:8080/oh/get/json?q1=v3");
        this.parserBuilder.addHttpOh("getText", "get", "http://localhost:8080/oh/get/text?q1=v1&q1=v2");
        String ohScript = OhScriptReader.read("http/get_json_and_get_text.oh");
        AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
        ASTEnv env = new ASTEnv(ast);
        env.setHttpClient(this.httpClient);
        Oh response = ObjectUtils.cast(env.execute());
        assertThat(response.get("body")).isEqualTo("v1,v2,v3,v4");
        assertThat(response.get("headers")).isNotNull();
    }

    @Nested
    @DisplayName("通过 Get 方法调用")
    class TestGet {
        @Test
        @DisplayName("获取文本返回值成功")
        void shouldGetTextSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("getText", "get", "http://localhost:8080/oh/get/text?q1=v1&q1=v2");
            String ohScript = OhScriptReader.read("http/get_text.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            assertThat(response.get("body")).isEqualTo("v1,v2,v3");
            assertThat(response.get("headers")).isNotNull();
        }

        @Test
        @DisplayName("获取对象返回值成功")
        void shouldGetJsonSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("getJson", "get", "http://localhost:8080/oh/get/json?q1=v3");
            String ohScript = OhScriptReader.read("http/get_json.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            Map<String, Object> actualEntity = cast(response.get("body"));
            assertThat(actualEntity).isNotNull().containsEntry("k1", "v3").containsEntry("k2", "v4");
            assertThat(response.get("headers")).isNotNull();
        }
    }

    @Nested
    @DisplayName("通过 Post 方法调用")
    class TestPost {
        @Test
        @DisplayName("提交文本消息体成功")
        void shouldPostTextSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("postText", "post", "http://localhost:8080/oh/post/text");
            String ohScript = OhScriptReader.read("http/post_text.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            assertThat(response.get("body")).isEqualTo("Hello World");
            assertThat(response.get("headers")).isNotNull();
        }

        @Test
        @DisplayName("提交 Json 消息体成功")
        void shouldPostJsonSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("postJson", "post", "http://localhost:8080/oh/post/json");
            String ohScript = OhScriptReader.read("http/post_json.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            Map<String, Object> body = cast(response.get("body"));
            assertThat(body).containsEntry("f1", "v1");
            assertThat(body.get("f2")).isNotNull().hasFieldOrPropertyWithValue("k", 1);
            assertThat(body.get("f3")).asInstanceOf(InstanceOfAssertFactories.LIST).contains(1, 2, 3);
            assertThat(response.get("headers")).isNotNull();
        }

        @Test
        @DisplayName("提交表单消息体信息成功")
        void shouldPostFormSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("postForm", "post", "http://localhost:8080/oh/post/form");
            String ohScript = OhScriptReader.read("http/post_form.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);

            Oh response = ObjectUtils.cast(env.execute());
            assertThat(response.get("body")).isEqualTo("v1");
            assertThat(response.get("headers")).isNotNull();
        }
    }

    @Nested
    @DisplayName("通过 Put 方法调用")
    class TestPut {
        @Test
        @DisplayName("提交文本消息体成功")
        void shouldPutTextSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("putText", "put", "http://localhost:8080/oh/put/text");
            String ohScript = OhScriptReader.read("http/put_text.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            assertThat(response.get("body")).isEqualTo("Hello World");
            assertThat(response.get("headers")).isNotNull();
        }

        @Test
        @DisplayName("提交 Json 消息体成功")
        void shouldPutJsonSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("putJson", "put", "http://localhost:8080/oh/put/json");
            String ohScript = OhScriptReader.read("http/put_json.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            Map<String, Object> body = cast(response.get("body"));
            assertThat(body).containsEntry("f1", "v1");
            assertThat(body.get("f2")).isNotNull().hasFieldOrPropertyWithValue("k", 1);
            assertThat(body.get("f3")).asInstanceOf(InstanceOfAssertFactories.LIST).contains(1, 2, 3);
            assertThat(response.get("headers")).isNotNull();
        }

        @Test
        @DisplayName("提交表单消息体信息成功")
        void shouldPutFormSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("putForm", "put", "http://localhost:8080/oh/put/form");
            String ohScript = OhScriptReader.read("http/put_form.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            assertThat(response.get("body")).isEqualTo("v1");
            assertThat(response.get("headers")).isNotNull();
        }
    }

    @Nested
    @DisplayName("通过 Patch 方法调用")
    class TestPatch {
        @Test
        @DisplayName("提交文本消息体成功")
        void shouldPatchTextSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("patchText", "patch", "http://localhost:8080/oh/patch/text");
            String ohScript = OhScriptReader.read("http/patch_text.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            assertThat(response.get("body")).isEqualTo("Hello World");
            assertThat(response.get("headers")).isNotNull();
        }

        @Test
        @DisplayName("提交 Json 消息体成功")
        void shouldPatchJsonSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("patchJson", "patch", "http://localhost:8080/oh/patch/json");
            String ohScript = OhScriptReader.read("http/patch_json.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            Map<String, Object> body = cast(response.get("body"));
            assertThat(body).containsEntry("f1", "v1");
            assertThat(body.get("f2")).isNotNull().hasFieldOrPropertyWithValue("k", 1);
            assertThat(body.get("f3")).asInstanceOf(InstanceOfAssertFactories.LIST).contains(1, 2, 3);
            assertThat(response.get("headers")).isNotNull();
        }

        @Test
        @DisplayName("提交表单消息体信息成功")
        void shouldPatchFormSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("patchForm", "patch", "http://localhost:8080/oh/patch/form");
            String ohScript = OhScriptReader.read("http/patch_form.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            assertThat(response.get("body")).isEqualTo("v1");
            assertThat(response.get("headers")).isNotNull();
        }
    }

    @Nested
    @DisplayName("通过 Delete 方法调用")
    class TestDelete {
        @Test
        @DisplayName("获取文本返回值成功")
        void shouldDeleteTextSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("deleteText",
                    "delete",
                    "http://localhost:8080/oh/delete/text?q1=v1&q1=v2");
            String ohScript = OhScriptReader.read("http/delete_text.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            assertThat(response.get("body")).isEqualTo("v1,v2,v3");
            assertThat(response.get("headers")).isNotNull();
        }

        @Test
        @DisplayName("获取对象返回值成功")
        void shouldDeleteJsonSuccessfully() throws OhPanic {
            HttpTest.this.parserBuilder.addHttpOh("deleteJson", "delete", "http://localhost:8080/oh/delete/json?q1=v3");
            String ohScript = OhScriptReader.read("http/delete_json.oh");
            AST ast = HttpTest.this.parserBuilder.parseString("", ohScript);
            ASTEnv env = new ASTEnv(ast);
            env.setHttpClient(HttpTest.this.httpClient);
            Oh response = ObjectUtils.cast(env.execute());
            Map<String, Object> actualEntity = cast(response.get("body"));
            assertThat(actualEntity).isNotNull().containsEntry("k1", "v3").containsEntry("k2", "v4");
            assertThat(response.get("headers")).isNotNull();
        }
    }
}
