/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.server.http;

import static modelengine.fitframework.inspection.Validation.greaterThanOrEquals;
import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.http.server.HttpClassicServer;
import modelengine.fit.http.server.HttpServerStartupException;
import modelengine.fit.server.FitServer;
import modelengine.fit.server.StartServerException;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.broker.Endpoint;
import modelengine.fitframework.conf.runtime.ServerConfig;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.runtime.FitRuntime;
import modelengine.fitframework.runtime.FitRuntimeStartedObserver;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Http 服务器。
 *
 * @author 季聿阶
 * @since 2022-09-16
 */
@Component(name = "http")
public class FitHttpServer implements FitServer, FitRuntimeStartedObserver {
    private static final Logger log = Logger.get(FitHttpServer.class);
    private static final int PROTOCOL_CODE_HTTP = 2;
    private static final int PROTOCOL_CODE_HTTPS = 4;
    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final int DEFAULT_HTTPS_PORT = 8443;

    private final HttpClassicServer httpServer;

    private final boolean httpOpen;
    private final int httpPort;
    private final int toRegisterHttpPort;

    private final boolean httpsOpen;
    private final int httpsPort;
    private final int toRegisterHttpsPort;
    private final FitHttpHandlerRegistry registry;

    /**
     * 创建 FIT 的 Http 服务器对象。
     *
     * @param httpServer 表示 Http 服务器的 {@link HttpClassicServer}。
     * @param registry 表示 FIT 通信的处理器的注册器的 {@link FitHttpHandlerRegistry}。
     * @param httpConfig 表示 Http 配置的 {@link HttpConfig}。
     */
    public FitHttpServer(HttpClassicServer httpServer, FitHttpHandlerRegistry registry, HttpConfig httpConfig) {
        this.httpServer = notNull(httpServer, "The http server cannot be null.");
        this.registry = notNull(registry, "The http handler registry cannot be null.");
        this.httpsOpen = httpConfig.secure().map(ServerConfig.Secure::isProtocolEnabled).orElse(false);
        if (this.httpsOpen) {
            Optional<ServerConfig.Secure> secure = httpConfig.secure();
            this.httpsPort = secure.flatMap(ServerConfig.Secure::port).orElse(DEFAULT_HTTPS_PORT);
            greaterThanOrEquals(this.httpsPort, 0, "The server https port must be non-negative.");
            log.debug("Config 'server.https.port' is {}.", this.httpsPort);
            this.toRegisterHttpsPort = secure.flatMap(ServerConfig.Secure::toRegisterPort).orElse(this.httpsPort);
            log.debug("Config 'server.https.to-register-port' is {}.", this.toRegisterHttpsPort);
        } else {
            this.httpsPort = 0;
            this.toRegisterHttpsPort = 0;
        }
        this.httpOpen = httpConfig.isProtocolEnabled() || !this.httpsOpen;
        if (this.httpOpen) {
            this.httpPort = httpConfig.port().orElse(DEFAULT_HTTP_PORT);
            greaterThanOrEquals(this.httpPort, 0, "The server http port must be non-negative.");
            log.debug("Config 'server.http.port' is {}.", this.httpPort);
            this.toRegisterHttpPort = httpConfig.toRegisterPort().orElse(this.httpPort);
            log.debug("Config 'server.http.to-register-port' is {}.", this.toRegisterHttpPort);
        } else {
            this.httpPort = 0;
            this.toRegisterHttpPort = 0;
        }
    }

    @Override
    public void onRuntimeStarted(FitRuntime runtime) {
        this.start();
    }

    @Override
    public void start() throws StartServerException {
        try {
            if (this.httpOpen) {
                this.httpServer.bind(this.httpPort, false);
            }
            if (this.httpsOpen) {
                this.httpServer.bind(this.httpsPort, true);
            }
            this.httpServer.start();
        } catch (HttpServerStartupException e) {
            throw new StartServerException("Failed to start http server.", e);
        }
    }

    @Override
    public void stop() {
        this.httpServer.stop();
    }

    @Override
    public List<Endpoint> endpoints() {
        List<Endpoint> endpoints = new ArrayList<>();
        if (this.httpOpen) {
            endpoints.add(Endpoint.custom().protocol("http", PROTOCOL_CODE_HTTP).port(this.toRegisterHttpPort).build());
        }
        if (this.httpsOpen) {
            endpoints.add(Endpoint.custom()
                    .protocol("https", PROTOCOL_CODE_HTTPS)
                    .port(this.toRegisterHttpsPort)
                    .build());
        }
        return endpoints;
    }

    @Override
    public Map<String, String> extensions() {
        String contextPath = this.registry.getContextPath();
        if (StringUtils.isBlank(contextPath)) {
            return Collections.emptyMap();
        } else {
            return MapBuilder.<String, String>get().put("http.context-path", this.registry.getContextPath()).build();
        }
    }
}
