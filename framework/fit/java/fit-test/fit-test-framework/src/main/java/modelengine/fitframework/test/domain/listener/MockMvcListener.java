/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fitframework.test.domain.listener;

import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fit.http.entity.TextEntity;
import modelengine.fitframework.exception.ClientException;
import modelengine.fitframework.test.annotation.EnableMockMvc;
import modelengine.fitframework.test.domain.TestContext;
import modelengine.fitframework.test.domain.mvc.MockController;
import modelengine.fitframework.test.domain.mvc.MockMvc;
import modelengine.fitframework.test.domain.mvc.request.MockMvcRequestBuilders;
import modelengine.fitframework.test.domain.mvc.request.MockRequestBuilder;
import modelengine.fitframework.test.domain.resolver.TestContextConfiguration;
import modelengine.fitframework.test.domain.util.AnnotationUtils;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.ThreadUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 用于注入 mockMvc 的监听器。
 *
 * @author 易文渊
 * @since 2024-07-21
 */
public class MockMvcListener implements TestListener {
    private static final Set<String> DEFAULT_SCAN_PACKAGES =
            new HashSet<>(Arrays.asList("modelengine.fit.server", "modelengine.fit.http"));

    private final int port;

    /**
     * 通过插件端口初始化 {@link MockMvcListener} 的实例。
     *
     * @param port 表示插件启动端口的 {code int}。
     */
    public MockMvcListener(int port) {
        this.port = port;
    }

    @Override
    public Optional<TestContextConfiguration> config(Class<?> clazz) {
        if (AnnotationUtils.getAnnotation(clazz, EnableMockMvc.class).isEmpty()) {
            return Optional.empty();
        }
        TestContextConfiguration configuration = TestContextConfiguration.custom()
                .testClass(clazz)
                .includeClasses(MapBuilder.<Class<?>, Supplier<Object>>get().put(MockController.class, null).build())
                .scannedPackages(DEFAULT_SCAN_PACKAGES)
                .build();
        return Optional.of(configuration);
    }

    @Override
    public void beforeTestClass(TestContext context) {
        Class<?> testClass = context.testClass();
        if (AnnotationUtils.getAnnotation(testClass, EnableMockMvc.class).isEmpty()) {
            return;
        }
        MockMvc mockMvc = new MockMvc(this.port);
        context.plugin().container().registry().register(mockMvc);
        boolean started = this.isStarted(mockMvc);
        while (!started) {
            ThreadUtils.sleep(100);
            started = this.isStarted(mockMvc);
        }
    }

    private boolean isStarted(MockMvc mockMvc) {
        MockRequestBuilder builder = MockMvcRequestBuilders.get(MockController.PATH).responseType(String.class);
        try (HttpClassicClientResponse<String> response = mockMvc.perform(builder)) {
            String content = response.textEntity()
                    .map(TextEntity::content)
                    .orElseThrow(() -> new IllegalStateException(StringUtils.format(
                            "Failed to start mock http server. [port={0}]",
                            this.port)));
            return Objects.equals(content, MockController.OK);
        } catch (IOException | ClientException e) {
            return false;
        }
    }
}