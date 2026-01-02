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
    private static final String TIMEOUT_PROPERTY_KEY = "fit.test.mockmvc.startup.timeout";
    private static final long DEFAULT_STARTUP_TIMEOUT = 30_000L;
    private static final long MIN_STARTUP_TIMEOUT = 1_000L;
    private static final long MAX_STARTUP_TIMEOUT = 600_000L;

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
        long timeout = this.getStartupTimeout();
        long startTime = System.currentTimeMillis();
        boolean started = this.isStarted(mockMvc);
        while (!started) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timeout) {
                throw new IllegalStateException(this.buildTimeoutErrorMessage(elapsed, this.port));
            }
            ThreadUtils.sleep(100);
            started = this.isStarted(mockMvc);
        }
    }

    private long getStartupTimeout() {
        String timeoutStr = System.getProperty(TIMEOUT_PROPERTY_KEY);
        if (StringUtils.isNotBlank(timeoutStr)) {
            try {
                long timeout = Long.parseLong(timeoutStr);
                if (timeout < MIN_STARTUP_TIMEOUT) {
                    return DEFAULT_STARTUP_TIMEOUT;
                }
                if (timeout > MAX_STARTUP_TIMEOUT) {
                    return MAX_STARTUP_TIMEOUT;
                }
                return timeout;
            } catch (NumberFormatException e) {
                return DEFAULT_STARTUP_TIMEOUT;
            }
        }
        return DEFAULT_STARTUP_TIMEOUT;
    }

    private String buildTimeoutErrorMessage(long elapsed, int port) {
        return StringUtils.format("""
                        Mock MVC server failed to start within {0}ms. [port={1}]
                        
                        Possible causes:
                        1. Port {1} is already in use by another process
                        2. Network configuration issues
                        3. Server startup is slower than expected in this environment
                        
                        Troubleshooting steps:
                        - Check if port {1} is in use:
                          * macOS/Linux: lsof -i :{1}
                          * Windows: netstat -ano | findstr :{1}
                        - Check server logs for detailed error messages
                        - If running in a slow environment, increase timeout:
                          mvn test -D{2}=60000""",
                elapsed,
                port,
                TIMEOUT_PROPERTY_KEY);
    }

    protected boolean isStarted(MockMvc mockMvc) {
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
