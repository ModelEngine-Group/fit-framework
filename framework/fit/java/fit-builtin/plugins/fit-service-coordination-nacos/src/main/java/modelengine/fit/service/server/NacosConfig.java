/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.service.server;

import lombok.Data;

/**
 * Represents the configuration prefixed with {@code 'matata.registry.nacos.'}.
 *
 * @author 董智豪
 * @since 2025-08-06
 */
@Data
public class NacosConfig {
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
     * Whether it is an ephemeral instance.
     * Ephemeral instances will be automatically removed from the registry after service deregistration.
     */
    private Boolean isEphemeral;

    /**
     * Service weight.
     * Used for weight calculation during load balancing.
     */
    private Float weight;

    /**
     * Heartbeat interval time (unit: milliseconds).
     * Defines the time interval for services to send heartbeats.
     */
    private Long heartbeatInterval;

    /**
     * Heartbeat timeout time (unit: milliseconds).
     * Defines the time after which a service is considered timed out when no heartbeat is received.
     */
    private Long heartbeatTimeout;
}