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
     * Nacos server address.
     * Specifies the connection address for the Nacos server, typically in the format "host:port".
     */
    private String serverAddr;

    /**
     * Login username for Nacos authentication.
     * Required when Nacos server has authentication enabled.
     */
    private String username;

    /**
     * Login password for Nacos authentication.
     * Used together with username for authentication when connecting to secured Nacos server.
     */
    private String password;

    /**
     * Access key for Nacos authentication.
     * Used for access control in cloud environments or when using AK/SK authentication.
     */
    private String accessKey;

    /**
     * Secret key for Nacos authentication.
     * Used together with access key for AK/SK authentication mechanism.
     */
    private String secretKey;

    /**
     * Namespace for logical isolation in Nacos.
     * Used to isolate different environments or tenants within the same Nacos server.
     */
    private String namespace;
}