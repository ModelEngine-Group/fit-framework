/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.core.model.http;

import modelengine.fitframework.pattern.builder.BuilderFactory;

/**
 * 表示 http 请求的安全相关配置。
 *
 * @author 宋永坦
 * @since 2025-03-30
 */
public interface SecureConfig {
    /**
     * 获取客户端 Http 是否忽略服务器根证书。
     *
     * @return 表示是否忽略服务器根证书的 {@link String}。
     */
    Boolean ignoreTrust();

    /**
     * 获取客户端 Http 是否忽略对服务器主机名的身份认证。
     *
     * @return 表示是否忽略对服务器主机名的身份认证的 {@link String}。
     */
    Boolean ignoreHostName();

    /**
     * 获取客户端 Http 的秘钥库的文件地址。
     *
     * @return 表示秘钥库的文件地址的 {@link String}。
     */
    String trustStoreFile();

    /**
     * 获取客户端 Http 的秘钥库的密码。
     *
     * @return 表示秘钥库的密码的 {@link String}。
     */
    String trustStorePassword();

    /**
     * 获取客户端 Http 的秘钥库的秘钥项的文件地址。
     *
     * @return 表示秘钥库的秘钥项的文件地址的 {@link String}。
     */
    String keyStoreFile();

    /**
     * 获取客户端 Http 的秘钥库的秘钥项的密码。
     *
     * @return 表示秘钥库的秘钥项的密码的 {@link String}。
     */
    String keyStorePassword();

    /**
     * {@link SecureConfig} 的构建器。
     */
    interface Builder {
        /**
         * 设置客户端 Http 是否忽略服务器根证书。
         *
         * @param ignoreTrust 表示是否忽略服务器根证书的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder ignoreTrust(Boolean ignoreTrust);

        /**
         * 设置客户端 Http 是否忽略对服务器主机名的身份认证。
         *
         * @param ignoreHostName 表示是否忽略对服务器主机名的身份认证的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder ignoreHostName(String ignoreHostName);

        /**
         * 设置客户端 Http 的秘钥库的文件地址。
         *
         * @param trustStoreFile 表示秘钥库的文件地址的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder trustStoreFile(String trustStoreFile);

        /**
         * 设置客户端 Http 的秘钥库的密码。
         *
         * @param trustStorePassword 表示秘钥库的密码的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder trustStorePassword(String trustStorePassword);

        /**
         * 设置客户端 Http 的秘钥库的秘钥项的文件地址。
         *
         * @param keyStoreFile 表示秘钥库的秘钥项的文件地址的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder keyStoreFile(String keyStoreFile);

        /**
         * 设置客户端 Http 的秘钥库的秘钥项的密码。
         *
         * @param keyStorePassword 表示秘钥库的秘钥项的密码的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder keyStorePassword(String keyStorePassword);

        /**
         * 构建对象。
         *
         * @return 表示构建出来的对象的 {@link SecureConfig}。
         */
        SecureConfig build();
    }

    /**
     * 获取 {@link SecureConfig} 的构建器。
     *
     * @return 表示 {@link SecureConfig} 的构建器的 {@link Builder}。
     */
    static Builder custom() {
        return BuilderFactory.get(SecureConfig.class, Builder.class).create(null);
    }
}
