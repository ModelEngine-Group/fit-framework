/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.okhttp;

import static modelengine.fit.http.protocol.util.SslUtils.getKeyManagers;
import static modelengine.fit.http.protocol.util.SslUtils.getTrustManagers;
import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fit.client.http.HttpsConstants;
import modelengine.fit.http.client.HttpClassicClientFactory;
import modelengine.fit.http.protocol.util.SslUtils;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.ArrayUtils;
import modelengine.fitframework.util.StringUtils;
import okhttp3.OkHttpClient;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 创建 OkHttpClient.Builder 实例工厂。
 *
 * <p><strong>安全配置说明：</strong></p>
 * <p>本框架提供 {@link HttpsConstants#CLIENT_SECURE_IGNORE_TRUST} 配置项，允许忽略SSL证书验证。</p>
 * <p><strong>警告：</strong>启用此选项将使应用程序容易受到中间人攻击！</p>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>开发环境：使用自签名证书或内网测试</li>
 *   <li>测试环境：快速原型验证</li>
 *   <li><strong>生产环境：绝对不应启用此选项</strong></li>
 * </ul>
 *
 * @author 杭潇
 * @since 2024-04-15
 */
public class OkHttpClientBuilderFactory {
    private static final Logger log = Logger.get(OkHttpClientBuilderFactory.class);
    private static final String SECURE_DEFAULT_PROTOCOL = "TLSv1.2";

    private OkHttpClientBuilderFactory() {}

    /**
     * 根据配置获取工厂实例的 {@link OkHttpClient.Builder}。
     *
     * @param config 表示配置的 {@link HttpClassicClientFactory.Config}。
     * @return 表示工厂创建实例的 {@link OkHttpClient.Builder}。
     */
    public static OkHttpClient.Builder getOkHttpClientBuilder(HttpClassicClientFactory.Config config) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        setTimeout(clientBuilder, config);
        try {
            setSslConfig(clientBuilder, config);
        } catch (GeneralSecurityException e) {
            log.error("Failed to set https config.", e);
            throw new IllegalStateException("Failed to set https config.", e);
        }
        return clientBuilder;
    }

    private static void setTimeout(OkHttpClient.Builder clientBuilder, HttpClassicClientFactory.Config config) {
        if (config.connectTimeout() >= 0) {
            clientBuilder.connectTimeout(config.connectTimeout(), TimeUnit.MILLISECONDS);
        }
        if (config.socketTimeout() >= 0) {
            clientBuilder.readTimeout(config.socketTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(config.socketTimeout(), TimeUnit.MILLISECONDS);
        }
    }

    private static void setSslConfig(OkHttpClient.Builder clientBuilder, HttpClassicClientFactory.Config config)
            throws GeneralSecurityException {
        boolean isStrongRandom = Boolean.parseBoolean(String.valueOf(config.custom()
                .getOrDefault(HttpsConstants.CLIENT_SECURE_STRONG_RANDOM, true)));
        String secureProtocol = cast(config.custom()
                .getOrDefault(HttpsConstants.CLIENT_SECURE_SECURITY_PROTOCOL, SECURE_DEFAULT_PROTOCOL));
        boolean isIgnoreTrust = Boolean.parseBoolean(String.valueOf(config.custom()
                .getOrDefault(HttpsConstants.CLIENT_SECURE_IGNORE_TRUST, false)));

        KeyManager[] keyManagers = getKeyManagersConfig(config, isIgnoreTrust);
        TrustManager[] trustManagers = getTrustManagersConfig(config, isIgnoreTrust);

        SSLContext sslContext = SslUtils.getSslContext(keyManagers, trustManagers, isStrongRandom, secureProtocol);
        if (isTrustManagerSet(trustManagers)) {
            clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0]);
        }
        if (isIgnoreTrust || isHostnameVerificationIgnored(trustManagers, config)) {
            clientBuilder.hostnameVerifier((hostname, session) -> true);
        }
    }

    private static KeyManager[] getKeyManagersConfig(HttpClassicClientFactory.Config config, boolean isIgnoreTrust)
            throws GeneralSecurityException {
        String keyStoreFile = cast(config.custom().get(HttpsConstants.CLIENT_SECURE_KEY_STORE_FILE));
        String keyStorePassword = cast(config.custom().get(HttpsConstants.CLIENT_SECURE_KEY_STORE_PASSWORD));
        if (StringUtils.isNotBlank(keyStoreFile) && StringUtils.isNotBlank(keyStorePassword) && !isIgnoreTrust) {
            return getKeyManagers(keyStoreFile, keyStorePassword);
        }
        return null;
    }

    private static TrustManager[] getTrustManagersConfig(HttpClassicClientFactory.Config config, boolean isIgnoreTrust)
            throws GeneralSecurityException {
        if (isIgnoreTrust) {
            log.warn("========================================================");
            log.warn("SECURITY WARNING: SSL/TLS Certificate Validation DISABLED!");
            log.warn("This configuration is INSECURE and should NEVER be used in production!");
            log.warn("Your application is vulnerable to man-in-the-middle attacks!");
            log.warn("Current setting: {} = true", HttpsConstants.CLIENT_SECURE_IGNORE_TRUST);
            log.warn("========================================================");
            if (log.isDebugEnabled()) {
                log.debug("Certificate validation disabled at:", new Exception("Stack trace for debugging"));
            }
            return getTrustAllCerts();
        }
        String trustStoreFile = cast(config.custom().get(HttpsConstants.CLIENT_SECURE_TRUST_STORE_FILE));
        String trustStorePassword = cast(config.custom().get(HttpsConstants.CLIENT_SECURE_TRUST_STORE_PASSWORD));
        if (StringUtils.isNotBlank(trustStoreFile) && StringUtils.isNotBlank(trustStorePassword)) {
            return getTrustManagers(trustStoreFile, trustStorePassword);
        }
        return null;
    }

    private static boolean isTrustManagerSet(TrustManager[] trustManagers) {
        return trustManagers != null && trustManagers.length > 0 && trustManagers[0] instanceof X509TrustManager;
    }

    private static boolean isHostnameVerificationIgnored(TrustManager[] trustManagers,
            HttpClassicClientFactory.Config config) {
        return trustManagers != null && Boolean.parseBoolean(String.valueOf(config.custom()
                .getOrDefault(HttpsConstants.CLIENT_SECURE_IGNORE_HOSTNAME, false)));
    }

    /**
     * 创建一个接受所有证书的 {@link TrustManager}{@code []}，其中仅有一个 {@link TrustManager}。
     * <p>此方法是框架设计的一部分，用于支持开发环境的快速集成，安全风险已通过配置和日志机制向用户明确告知。</p>
     * <p><strong>安全警告：</strong>此 {@link TrustManager}
     * 不验证任何证书，会接受所有证书包括无效、过期或伪造的证书，仅应在开发环境中使用，生产环境使用将导致严重的安全风险。</p>
     *
     * @return 不验证任何证书的 {@link TrustManager}{@code []}。
     */
    private static TrustManager[] getTrustAllCerts() {
        X509TrustManager x509TrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // 记录客户端证书验证被跳过
                if (log.isDebugEnabled()) {
                    log.debug("Bypassing client certificate validation (INSECURE MODE). [authType={}]", authType);
                }
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // 记录服务器证书验证被跳过，包含证书信息便于调试
                if (log.isDebugEnabled() && ArrayUtils.isNotEmpty(chain)) {
                    X509Certificate cert = chain[0];
                    log.debug("Bypassing server certificate validation (INSECURE MODE):");
                    log.debug("  - Subject: {}", cert.getSubjectX500Principal());
                    log.debug("  - Issuer: {}", cert.getIssuerX500Principal());
                    log.debug("  - Serial Number: {}", cert.getSerialNumber());
                    log.debug("  - Valid from {} to {}", cert.getNotBefore(), cert.getNotAfter());
                    log.debug("  - Auth Type: {}", authType);
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }
        };
        return new TrustManager[] {x509TrustManager};
    }
}
