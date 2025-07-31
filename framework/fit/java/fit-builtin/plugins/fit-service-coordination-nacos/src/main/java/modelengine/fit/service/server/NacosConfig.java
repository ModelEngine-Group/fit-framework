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
 * Represents configuration under the {@code 'nacos'} configuration item.
 *
 * @author 董智豪
 * @since 2025-06-14
 */
@Component
@AcceptConfigValues("nacos")
@Data
public class NacosConfig {
    /**
     * Nacos server address
     */
    private String serverAddr;

    /**
     * Login username
     */
    private String username;

    /**
     * Login password
     */
    private String password;

    /**
     * Access key
     */
    private String accessKey;

    /**
     * Secret key
     */
    private String secretKey;

    /**
     * Namespace
     */
    private String namespace;
}