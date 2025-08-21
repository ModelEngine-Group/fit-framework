/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fel.community.model.openai.config;

import modelengine.fitframework.annotation.AcceptConfigValues;
import modelengine.fitframework.annotation.Component;

/**
 * 表示 {@link OpenAiConfig} 的默认实现。
 *
 * @since 2024-08-12
 */
@Component
@AcceptConfigValues("fel.openai")
public class OpenAiConfig {
    private String apiBase = "https://api.openai.com/v1/";
    private String apiKey = "";
    private int connectTimeout = 30000;
    private int readTimeout = 30000;

    /**
     * 获取 openai 服务地址。
     *
     * @return 表示 openai 服务地址的 {@link String}。
     */
    public String getApiBase() {
        return this.apiBase;
    }

    /**
     * 设置 openai 服务地址。
     *
     * @param apiBase 表示 openai 服务地址的 {@link String}。
     */
    public void setApiBase(String apiBase) {
        this.apiBase = apiBase;
    }

    /**
     * 获取 openai 服务密钥。
     *
     * @return 表示 openai 服务密钥的 {@link String}。
     */
    public String getApiKey() {
        return this.apiKey;
    }

    /**
     * 设置 openai 服务密钥。
     *
     * @param apiKey 表示 openai 服务密钥的 {@link String}。
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 获取连接获取的超时时间。
     *
     * @return 表示连接获取的超时时间的 {@code int}。
     */
    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    /**
     * 设置连接获取的超时时间。
     *
     * @param connectTimeout 表示连接获取的超时时间的 {@code int}。
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * 获取读取的超时时间。
     *
     * @return 表示读取的超时时间的 {@code int}。
     */
    public int getReadTimeout() {
        return this.readTimeout;
    }

    /**
     * 设置读取超时时间。
     *
     * @param readTimeout 表示读取超时时间的 {@code int}。
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}