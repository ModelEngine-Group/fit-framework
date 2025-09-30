/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.example.auth;

import modelengine.fit.http.client.proxy.Authorization;
import modelengine.fit.http.client.proxy.RequestBuilder;
import modelengine.fit.http.client.proxy.auth.AuthProvider;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 自定义签名鉴权提供器示例。
 * <p>演示如何实现复杂的自定义鉴权逻辑，如签名算法。
 *
 * @author 季聿阶
 * @since 2025-09-30
 */
@Component
public class CustomSignatureProvider implements AuthProvider {
    @Override
    public Authorization provide() {
        return new CustomSignatureAuthorization();
    }

    /**
     * 自定义签名鉴权实现。
     * 在每次请求时生成时间戳和签名。
     */
    private static class CustomSignatureAuthorization implements Authorization {
        private static final String SECRET_KEY = "my-secret-key";

        @Override
        public void set(String key, Object value) {
            // 自定义鉴权不需要外部设置参数
        }

        @Override
        public void assemble(RequestBuilder builder) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String signature = generateSignature(timestamp);

            builder.header("X-Timestamp", timestamp);
            builder.header("X-Signature", signature);
            builder.header("X-App-Id", "fit-example-app");
        }

        private String generateSignature(String timestamp) {
            try {
                String data = timestamp + SECRET_KEY;
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

                // 简单的十六进制转换
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to generate signature", e);
            }
        }
    }
}