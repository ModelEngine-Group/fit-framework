/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support.http.server;

import modelengine.fit.http.server.HttpClassicServer;
import modelengine.fitframework.ioc.BeanFactory;
import modelengine.fitframework.runtime.FitRuntime;
import modelengine.fitframework.runtime.FitStarter;
import modelengine.fitframework.util.ThreadUtils;

/**
 * 为测试 Http 提供的服务运行时。
 *
 * @since 2024-06-15
 */
public class RuntimeForServer {
    private volatile boolean isStarted = false;

    private final Class<?> entry;
    private FitRuntime runtime;

    public RuntimeForServer(Class<?> entry) {
        this.entry = entry;
    }

    /**
     * 表示启动 Http 客户端。
     *
     * @param port 表示可用的端口 {@link int}。
     */
    public void start(int port) {
        if (this.isStarted) {
            return;
        }
        this.runtime = FitStarter.start(this.entry, new String[] {"server.http.port=" + port});
        HttpClassicServer httpClassicServer = runtime.root()
                .container()
                .lookup(HttpClassicServer.class)
                .map(BeanFactory::<HttpClassicServer>get)
                .orElseThrow(() -> new IllegalStateException("Failed to start http server."));
        while (!httpClassicServer.isStarted()) {
            ThreadUtils.sleep(0);
        }
        this.isStarted = true;
    }

    /**
     * 表示用于关闭 Http 客户端。
     */
    public void stop() {
        HttpClassicServer httpClassicServer = this.runtime.root()
                .container()
                .lookup(HttpClassicServer.class)
                .map(BeanFactory::<HttpClassicServer>get)
                .orElseThrow(() -> new IllegalStateException("Failed to stop http server."));
        httpClassicServer.stop();
        this.isStarted = false;
    }
}
