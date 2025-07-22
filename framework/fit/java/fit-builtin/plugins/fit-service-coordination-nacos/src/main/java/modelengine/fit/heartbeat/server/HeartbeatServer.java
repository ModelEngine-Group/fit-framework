/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.heartbeat.server;

import modelengine.fit.heartbeat.HeartbeatService;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fitable;

import java.util.List;

/**
 * 用于提供心跳相关的服务。
 *
 * @author 董智豪
 * @since 2025-06-04
 */
@Component
public class HeartbeatServer implements HeartbeatService {
    @Override
    @Fitable(id = "send_heartbeat")
    public Boolean sendHeartbeat(List<HeartbeatInfo> heartbeatInfo, Address address) {
        return true;
    }

    @Override
    @Fitable(id = "stop_heartbeat")
    public Boolean stopHeartbeat(List<HeartbeatInfo> heartbeatInfo, Address address) {
        return true;
    }
}