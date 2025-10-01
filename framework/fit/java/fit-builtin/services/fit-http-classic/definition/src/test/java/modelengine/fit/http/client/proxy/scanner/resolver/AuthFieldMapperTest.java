/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.proxy.scanner.resolver;

import modelengine.fit.http.client.proxy.auth.AuthType;
import modelengine.fit.http.client.proxy.support.authorization.ApiKeyAuthorization;
import modelengine.fit.http.client.proxy.support.authorization.BasicAuthorization;
import modelengine.fit.http.client.proxy.support.authorization.BearerAuthorization;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthFieldMapper 的单元测试。
 * <p>验证参数级别鉴权的字段映射逻辑是否正确。</p>
 *
 * @author 季聿阶
 * @since 2025-10-01
 */
@DisplayName("AuthFieldMapper 测试")
class AuthFieldMapperTest {

    @Test
    @DisplayName("Bearer 鉴权应该映射到 token 字段")
    void testBearerAuthField() {
        String field = AuthFieldMapper.getParameterAuthField(AuthType.BEARER);

        // 验证返回的字段名正确
        assertEquals("token", field);
    }

    @Test
    @DisplayName("Basic 鉴权应该映射到 username 字段")
    void testBasicAuthField() {
        String field = AuthFieldMapper.getParameterAuthField(AuthType.BASIC);

        // 验证返回的字段名正确
        assertEquals("username", field);
    }

    @Test
    @DisplayName("API Key 鉴权应该映射到 value 字段（而非 key 字段）")
    void testApiKeyAuthField() {
        String field = AuthFieldMapper.getParameterAuthField(AuthType.API_KEY);

        // 验证返回的字段名正确
        // 重要：应该是 "value" 而不是 "key"
        assertEquals("value", field);

        // 确保不是错误地返回了 "key"
        assertNotEquals("key", field);
    }

    @Test
    @DisplayName("CUSTOM 鉴权类型应该抛出异常")
    void testCustomAuthTypeThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> AuthFieldMapper.getParameterAuthField(AuthType.CUSTOM)
        );

        assertTrue(exception.getMessage().contains("CUSTOM"));
        assertTrue(exception.getMessage().contains("AuthProvider"));
    }

    @Test
    @DisplayName("验证字段名与 Authorization 实现的 setValue 方法兼容")
    void testFieldCompatibilityWithAuthorizationImplementations() {
        // Bearer: 确保字段名能被 BearerAuthorization.setValue() 识别
        String bearerField = AuthFieldMapper.getParameterAuthField(AuthType.BEARER);
        assertEquals("token", bearerField);

        // Basic: 确保字段名能被 BasicAuthorization.setValue() 识别
        String basicField = AuthFieldMapper.getParameterAuthField(AuthType.BASIC);
        assertTrue("username".equals(basicField) || "password".equals(basicField));

        // API Key: 确保字段名能被 ApiKeyAuthorization.setValue() 识别
        String apiKeyField = AuthFieldMapper.getParameterAuthField(AuthType.API_KEY);
        assertTrue("key".equals(apiKeyField) || "value".equals(apiKeyField));
        // 并且应该是 "value"，因为参数级别更新的是 API Key 的值
        assertEquals("value", apiKeyField);
    }
}
