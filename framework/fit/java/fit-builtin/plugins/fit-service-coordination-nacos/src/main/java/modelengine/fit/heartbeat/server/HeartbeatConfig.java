/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.heartbeat.server;

import lombok.Data;
import modelengine.fitframework.annotation.AcceptConfigValues;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.conf.Config;

/**
 * 
 * @author mikeaaaaaa
 * @description
 * @created 2025/6/14
 */
@Component
@AcceptConfigValues("nacos.heartbeat")
@Data
public class HeartbeatConfig {

    /**
     * 是否为临时实例，默认为 true。
     * 临时实例在服务注销后会自动从注册中心移除。
     */
    private Boolean isEphemeral = true;

    /**
     * 服务权重，默认为 1.0。
     * 用于负载均衡时的权重计算。
     */
    private Float weight = 1.0F;

    /**
     * 心跳间隔时间（单位：秒）。
     * 定义服务发送心跳的时间间隔。
     */
    private Integer heartBeatInterval;

    /**
     * 心跳超时时间（单位：秒）。
     * 定义服务在未收到心跳后判定为超时的时间。
     */
    private Integer heartBeatTimeout;
}
