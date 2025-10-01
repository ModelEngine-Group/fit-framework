/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import modelengine.fit.http.client.HttpClassicClient;
import modelengine.fit.http.client.HttpClassicClientFactory;
import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fit.http.client.HttpClientException;
import modelengine.fit.http.client.proxy.PropertyValueApplier;
import modelengine.fit.http.client.proxy.RequestBuilder;
import modelengine.fit.http.client.proxy.scanner.entity.Address;
import modelengine.fit.http.client.proxy.scanner.entity.HttpInfo;
import modelengine.fit.http.entity.TextEntity;
import modelengine.fit.http.protocol.HttpRequestMethod;
import modelengine.fitframework.ioc.BeanContainer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 表示 {@link HttpInvocationHandler} 的单元测试。
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
class HttpInvocationHandlerTest {
    @Mock
    private HttpClassicClientFactory mockFactory;

    @Mock
    private BeanContainer mockContainer;

    @Mock
    private HttpClassicClient mockClient;

    @Mock
    private HttpClassicClientResponse<Object> mockResponse;

    @Mock
    private TextEntity mockTextEntity;

    @Mock
    private PropertyValueApplier mockStaticApplier;

    @Mock
    private PropertyValueApplier mockParamApplier;

    private HttpInvocationHandler handler;
    private Map<Method, HttpInfo> httpInfoMap;
    private Method noParamMethod;
    private Method oneParamMethod;
    private Method twoParamMethod;
    private AutoCloseable mockCloseable;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // 初始化标有 @Mock 注解的字段，返回 AutoCloseable 用于资源清理
        this.mockCloseable = MockitoAnnotations.openMocks(this);

        when(this.mockFactory.create()).thenReturn(this.mockClient);
        when(this.mockClient.exchange(any(), any())).thenReturn(this.mockResponse);
        when(this.mockResponse.statusCode()).thenReturn(200);
        when(this.mockResponse.textEntity()).thenReturn(Optional.of(this.mockTextEntity));
        when(this.mockTextEntity.content()).thenReturn("test response");

        this.httpInfoMap = new HashMap<>();
        this.handler = new HttpInvocationHandler(this.httpInfoMap, this.mockContainer, this.mockFactory);

        // 创建测试方法
        this.noParamMethod = TestInterface.class.getMethod("noParamMethod");
        this.oneParamMethod = TestInterface.class.getMethod("oneParamMethod", String.class);
        this.twoParamMethod = TestInterface.class.getMethod("twoParamMethod", String.class, String.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (this.mockCloseable != null) {
            this.mockCloseable.close();
        }
    }

    /**
     * 测试核心Bug修复：args为null时不再导致NullPointerException
     */
    @Test
    void testInvokeWithNoParametersAndNullArgs() throws Throwable {
        HttpInfo httpInfo = createHttpInfo();
        httpInfo.setStaticAppliers(List.of(this.mockStaticApplier));
        httpInfo.setParamAppliers(new ArrayList<>());
        this.httpInfoMap.put(this.noParamMethod, httpInfo);

        // 这个测试验证核心Bug修复：args为null不再导致NullPointerException
        try {
            Object result = this.handler.invoke(null, this.noParamMethod, null);
            assertEquals("test response", result);
        } catch (NullPointerException e) {
            if (e.getMessage().contains("Cannot read the array length because \"args\" is null")) {
                fail("核心Bug未修复：args为null时仍然导致NullPointerException");
            }
            // 其他NPE可能是Mock问题，不是我们测试的核心
            throw e;
        }

        // 验证静态应用器被调用
        verify(this.mockStaticApplier).apply(any(RequestBuilder.class), eq(null));
        verify(this.mockParamApplier, never()).apply(any(), any());
    }

    /**
     * 测试参数数量不匹配异常
     */
    @Test
    void testInvokeWithParameterCountMismatch() {
        HttpInfo httpInfo = createHttpInfo();
        httpInfo.setStaticAppliers(List.of(this.mockStaticApplier));
        httpInfo.setParamAppliers(List.of(this.mockParamApplier)); // 期望1个参数
        this.httpInfoMap.put(this.twoParamMethod, httpInfo);

        // 传入2个参数，但期望1个参数应用器
        Object[] args = {"param1", "param2"};

        HttpClientException exception =
                assertThrows(HttpClientException.class, () -> this.handler.invoke(null, this.twoParamMethod, args));

        assertEquals("Args length not equals to param appliers size.", exception.getMessage());
    }

    /**
     * 测试没有HTTP信息的异常处理
     */
    @Test
    void testInvokeWithNoHttpInfo() {
        // 不设置HttpInfo
        HttpClientException exception =
                assertThrows(HttpClientException.class, () -> this.handler.invoke(null, this.noParamMethod, null));

        assertEquals("No method http info.", exception.getMessage());
    }

    /**
     * 测试单参数方法的正常调用
     */
    @Test
    void testInvokeWithOneParameter() throws Throwable {
        HttpInfo httpInfo = createHttpInfo();
        httpInfo.setStaticAppliers(List.of(this.mockStaticApplier));
        httpInfo.setParamAppliers(List.of(this.mockParamApplier));
        this.httpInfoMap.put(this.oneParamMethod, httpInfo);

        String param = "test-param";
        Object result = this.handler.invoke(null, this.oneParamMethod, new Object[] {param});

        assertEquals("test response", result);

        // 验证静态和参数应用器都被调用
        verify(this.mockStaticApplier).apply(any(RequestBuilder.class), eq(null));
        verify(this.mockParamApplier).apply(any(RequestBuilder.class), eq(param));
    }

    /**
     * 测试多参数方法的正常调用
     */
    @Test
    void testInvokeWithMultipleParameters() throws Throwable {
        HttpInfo httpInfo = createHttpInfo();
        PropertyValueApplier mockParamApplier2 = mock(PropertyValueApplier.class);

        httpInfo.setStaticAppliers(List.of(this.mockStaticApplier));
        httpInfo.setParamAppliers(List.of(this.mockParamApplier, mockParamApplier2));
        this.httpInfoMap.put(this.twoParamMethod, httpInfo);

        String param1 = "test-param1";
        String param2 = "test-param2";
        Object result = this.handler.invoke(null, this.twoParamMethod, new Object[] {param1, param2});

        assertEquals("test response", result);

        // 验证所有应用器被正确调用
        verify(this.mockStaticApplier).apply(any(RequestBuilder.class), eq(null));
        verify(this.mockParamApplier).apply(any(RequestBuilder.class), eq(param1));
        verify(mockParamApplier2).apply(any(RequestBuilder.class), eq(param2));
    }

    /**
     * 测试只有静态应用器的方法调用
     */
    @Test
    void testInvokeWithStaticAppliersOnly() throws Throwable {
        HttpInfo httpInfo = createHttpInfo();
        PropertyValueApplier mockStaticApplier2 = mock(PropertyValueApplier.class);

        httpInfo.setStaticAppliers(List.of(this.mockStaticApplier, mockStaticApplier2));
        httpInfo.setParamAppliers(new ArrayList<>());
        this.httpInfoMap.put(this.noParamMethod, httpInfo);

        Object result = this.handler.invoke(null, this.noParamMethod, null);

        assertEquals("test response", result);

        // 验证所有静态应用器被调用
        verify(this.mockStaticApplier).apply(any(RequestBuilder.class), eq(null));
        verify(mockStaticApplier2).apply(any(RequestBuilder.class), eq(null));
        verify(this.mockParamApplier, never()).apply(any(), any());
    }

    /**
     * 测试空参数数组的处理
     */
    @Test
    void testInvokeWithEmptyArgs() throws Throwable {
        HttpInfo httpInfo = createHttpInfo();
        httpInfo.setStaticAppliers(List.of(this.mockStaticApplier));
        httpInfo.setParamAppliers(new ArrayList<>());
        this.httpInfoMap.put(this.noParamMethod, httpInfo);

        Object result = this.handler.invoke(null, this.noParamMethod, new Object[0]);

        assertEquals("test response", result);
        verify(this.mockStaticApplier).apply(any(RequestBuilder.class), eq(null));
    }

    private HttpInfo createHttpInfo() {
        HttpInfo httpInfo = new HttpInfo();
        httpInfo.setMethod(HttpRequestMethod.GET);
        httpInfo.setPathPattern("/test");
        httpInfo.setAddress(new Address());
        httpInfo.setStaticAppliers(new ArrayList<>());
        httpInfo.setParamAppliers(new ArrayList<>());
        return httpInfo;
    }

    // 测试接口
    interface TestInterface {
        String noParamMethod();

        String oneParamMethod(String param);

        String twoParamMethod(String param1, String param2);
    }
}