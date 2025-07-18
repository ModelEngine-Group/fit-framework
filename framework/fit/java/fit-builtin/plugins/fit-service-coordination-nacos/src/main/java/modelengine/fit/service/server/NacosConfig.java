/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.service.server;

import lombok.Data;
import modelengine.fitframework.annotation.AcceptConfigValues;
import modelengine.fitframework.annotation.Component;

/**
 * 表示配置项 {@code 'nacos'} 下的配置。
 *
 * @author 董智豪
 * @since 2025-06-14
 */
@Component
@AcceptConfigValues("nacos")
@Data
public class NacosConfig {
    /**
     * Nacos 服务器地址
     */
    private String serverAddr;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 秘钥
     */
    private String secretKey;

    /**
     * 命名空间
     */
    private String namespace;
}