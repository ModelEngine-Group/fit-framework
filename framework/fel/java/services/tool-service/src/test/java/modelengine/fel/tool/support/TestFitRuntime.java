/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support;

import modelengine.fit.http.server.HttpClassicServer;
import modelengine.fitframework.ioc.BeanFactory;
import modelengine.fitframework.runtime.FitRuntime;
import modelengine.fitframework.runtime.FitStarter;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.ThreadUtils;

/**
 * 用于测试的统一 FIT 运行时。
 *
 * @since 2024-06-15
 */
public class TestFitRuntime {
    /** 表示 FIT 统一测试运行时的单例。 */
    public static final TestFitRuntime INSTANCE = new TestFitRuntime();

    private FitRuntime runtime;

    private volatile boolean started = false;

    private TestFitRuntime() {}

    public boolean started() {
        return this.started;
    }

    /**
     * 启动 FIT 运行时。
     *
     * @param port 表示端口的 {@code int}。
     */
    public void start(int port) {
        if (this.started()) {
            return;
        }
        this.runtime =
                FitStarter.start(HttpToolTest.class, new String[] {StringUtils.format("server.http.port={0}", port)});
        HttpClassicServer httpClassicServer = runtime.root()
                .container()
                .lookup(HttpClassicServer.class)
                .map(BeanFactory::<HttpClassicServer>get)
                .orElseThrow(() -> new IllegalStateException("Failed to start http server."));
        while (!httpClassicServer.isStarted()) {
            ThreadUtils.sleep(0);
        }
        this.started = true;
    }

    /**
     * 终止 FIT 运行时。
     */
    public void stop() {
        HttpClassicServer httpClassicServer = this.runtime.root()
                .container()
                .lookup(HttpClassicServer.class)
                .map(BeanFactory::<HttpClassicServer>get)
                .orElseThrow(() -> new IllegalStateException("Failed to stop http server."));
        httpClassicServer.stop();
        this.started = false;
    }
}

