/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.heartbeat.server;

import lombok.Data;
import modelengine.fitframework.annotation.AcceptConfigValues;
import modelengine.fitframework.annotation.Component;

/**
 * Represents configuration under the {@code 'nacos.heartbeat'} configuration item.
 *
 * @author 董智豪
 * @since 2025-06-21
 */
@Component
@AcceptConfigValues("nacos.heartbeat")
@Data
public class HeartbeatConfig {
    /**
     * Whether it is an ephemeral instance, default is true.
     * Ephemeral instances will be automatically removed from the registry after service deregistration.
     */
    private Boolean isEphemeral = true;

    /**
     * Service weight, default is 1.0.
     * Used for weight calculation during load balancing.
     */
    private Float weight = 1.0F;

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