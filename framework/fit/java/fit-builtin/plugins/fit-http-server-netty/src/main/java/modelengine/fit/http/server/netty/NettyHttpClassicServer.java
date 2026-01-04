/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.netty;

import static modelengine.fitframework.inspection.Validation.greaterThanOrEquals;
import static modelengine.fitframework.inspection.Validation.isTrue;
import static modelengine.fitframework.inspection.Validation.lessThanOrEquals;
import static modelengine.fitframework.inspection.Validation.notNull;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.GlobalEventExecutor;
import modelengine.fit.http.Serializers;
import modelengine.fit.http.protocol.util.SslUtils;
import modelengine.fit.http.server.HttpClassicServer;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fit.http.server.HttpDispatcher;
import modelengine.fit.http.server.HttpServerStartupException;
import modelengine.fit.http.server.netty.websocket.ProtocolUpgrader;
import modelengine.fit.http.websocket.server.WebSocketDispatcher;
import modelengine.fit.security.Decryptor;
import modelengine.fit.server.http.HttpConfig;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Value;
import modelengine.fitframework.conf.runtime.ServerConfig;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.thread.DefaultThreadFactory;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.LockUtils;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.StringUtils;
import modelengine.fitframework.util.ThreadUtils;
import modelengine.fitframework.value.ValueFetcher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;

/**
 * {@link HttpClassicServer} 的 Netty 实现。
 *
 * @author 季聿阶
 * @since 2022-07-08
 */
@Component
public class NettyHttpClassicServer implements HttpClassicServer {
    private static final Logger log = Logger.get(NettyHttpClassicServer.class);
    private static final String SECURE_DEFAULT_PROTOCOL = "TLSv1.2";

    private final BeanContainer container;
    private final HttpDispatcher dispatcher;
    private final WebSocketDispatcher webSocketDispatcher;
    private final Serializers serializers;
    private final ValueFetcher valueFetcher;

    private final int coreThreadNum;
    private final int maxThreadNum;
    private final int queueCapacity;
    private final long largeBodySize;
    private final NettyHttpServerConfig nettyConfig;
    private final ServerConfig.Secure httpsConfig;

    private final ThreadPoolExecutor startServerExecutor =
            ThreadUtils.singleThreadPool(new DefaultThreadFactory("netty-http-server", false, (thread, exception) -> {
                log.error("Failed to start netty http server.", exception);
            }));
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private volatile int httpPort = 0;
    private volatile int httpsPort = 0;
    private volatile boolean httpBound = false;
    private volatile boolean httpsBound = false;
    private final boolean isGracefulExit;
    private volatile boolean isStarted = false;
    private final Lock lock = LockUtils.newReentrantLock();

    NettyHttpClassicServer(BeanContainer container, Map<String, ObjectSerializer> serializers,
            ValueFetcher valueFetcher, NettyHttpServerConfig nettyConfig, HttpConfig httpConfig,
            @Value("${worker.exit.graceful}") boolean isGracefulExit) {
        this.container = notNull(container, "The bean container cannot be null.");
        this.dispatcher = HttpDispatcher.create();
        this.webSocketDispatcher = WebSocketDispatcher.create();
        notNull(serializers, "The serializers cannot be null.");
        this.serializers = Serializers.create(serializers);
        this.valueFetcher = notNull(valueFetcher, "The value fetcher cannot be null.");
        this.nettyConfig = notNull(nettyConfig, "The netty http server config cannot be null.");
        this.coreThreadNum = this.nettyConfig.getCoreThreadNum() > 0
                ? this.nettyConfig.getCoreThreadNum()
                : Math.max(NettyRuntime.availableProcessors() * 2, 10);
        this.maxThreadNum = Math.max(this.nettyConfig.getMaxThreadNum(), this.coreThreadNum);
        this.queueCapacity = Math.max(this.nettyConfig.getQueueCapacity(), 0);
        this.isGracefulExit = isGracefulExit;
        this.httpsConfig = notNull(httpConfig, "The http config cannot be null.").secure().orElse(null);
        this.largeBodySize = httpConfig.largeBodySize();
    }

    @Override
    public HttpClassicServer bind(int port) {
        return this.bind(port, false);
    }

    @Override
    public HttpClassicServer bind(int port, boolean isSecure) {
        if (this.isStarted) {
            return this;
        }
        if (isSecure) {
            this.httpsPort = greaterThanOrEquals(port,
                    0,
                    "The port to bind to netty http server cannot be negative. [port={0}, isSecure={1}]",
                    port,
                    true);
            this.httpsPort = lessThanOrEquals(port,
                    65535,
                    "The port to bind to netty http server cannot be more than 65535. [port={0}, isSecure={1}]",
                    port,
                    true);
            this.httpsBound = true;
        } else {
            this.httpPort = greaterThanOrEquals(port,
                    0,
                    "The port to bind to netty http server cannot be negative. [port={0}, isSecure={1}]",
                    port,
                    false);
            this.httpPort = lessThanOrEquals(port,
                    65535,
                    "The port to bind to netty http server cannot be more than 65535. [port={0}, isSecure={1}]",
                    port,
                    false);
            this.httpBound = true;
        }
        return this;
    }

    @Override
    public void start() {
        if (this.isStarted) {
            return;
        }
        isTrue(this.httpBound || this.httpsBound,
                "At least 1 port should be bound to netty http server. [httpPort={0}, httpsPort={1}]",
                this.httpPort,
                this.httpsPort);
        LockUtils.synchronize(this.lock, () -> {
            if (!this.isStarted) {
                this.startServerExecutor.execute(this::startServer);
            }
        });
    }

    @Override
    public boolean isStarted() {
        return this.isStarted;
    }

    @Override
    public int getActualHttpPort() {
        if (!this.isStarted || !this.httpBound) {
            return 0;
        }
        return Math.max(this.httpPort, 0);
    }

    @Override
    public int getActualHttpsPort() {
        if (!this.isStarted || !this.httpsBound) {
            return 0;
        }
        return Math.max(this.httpsPort, 0);
    }

    @Override
    public void stop() {
        if (!this.isStarted) {
            return;
        }
        LockUtils.synchronize(this.lock, () -> {
            if (!this.isStarted) {
                return;
            }
            try {
                this.channelGroup.close().sync();
            } catch (InterruptedException e) {
                // ignored
            }
            this.startServerExecutor.shutdownNow();
            log.info("Terminate http server successfully.");
            this.isStarted = false;
        });
    }

    private void startServer() {
        EventLoopGroup bossGroup = createBossGroup();
        EventLoopGroup workerGroup = this.createWorkerGroup();
        try {
            SSLContext sslContext = null;
            if (this.httpsBound && this.httpsConfig != null && this.httpsConfig.isSslEnabled()) {
                sslContext = this.createSslContext();
            }
            ChannelHandler channelHandler =
                    new ChannelInitializerHandler(this, this.getAssemblerConfig(), sslContext, this.httpsConfig);
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelHandler);
            if (this.httpBound) {
                Channel channel = serverBootstrap.bind(this.httpPort).sync().channel();
                this.channelGroup.add(channel);
                if (this.httpPort == 0) {
                    this.httpPort = ((InetSocketAddress) channel.localAddress()).getPort();
                    log.info("HTTP server bound to auto-assigned port: {}", this.httpPort);
                }
            }
            if (this.httpsBound) {
                Channel channel = serverBootstrap.bind(this.httpsPort).sync().channel();
                this.channelGroup.add(channel);
                if (this.httpsPort == 0) {
                    this.httpsPort = ((InetSocketAddress) channel.localAddress()).getPort();
                    log.info("HTTPS server bound to auto-assigned port: {}", this.httpsPort);
                }
            }
            this.logServerStarted();
            ChannelGroupFuture channelFutures = this.channelGroup.newCloseFuture();
            this.isStarted = true;
            channelFutures.sync();
        } catch (InterruptedException | GeneralSecurityException | IOException e) {
            throw new HttpServerStartupException("Netty http server is interrupted.", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            log.info("Http server has been terminated.");
        }
    }

    private HttpClassicRequestAssembler.Config getAssemblerConfig() {
        return HttpClassicRequestAssembler.Config.custom()
                .shouldDisplayError(this.nettyConfig.isDisplayError())
                .largeBodySize(this.largeBodySize)
                .coreThreadNum(this.coreThreadNum)
                .maxThreadNum(this.maxThreadNum)
                .queueCapacity(this.queueCapacity)
                .isGracefulExit(this.isGracefulExit)
                .build();
    }

    private void logServerStarted() {
        if (this.httpPort > 0 && this.httpsPort > 0) {
            log.info("Start netty http server successfully. [httpPort={}, httpsPort={}]",
                    this.httpPort,
                    this.httpsPort);
        } else if (this.httpPort > 0) {
            log.info("Start netty http server successfully. [httpPort={}]", this.httpPort);
        } else {
            log.info("Start netty http server successfully. [httpsPort={}]", this.httpsPort);
        }
    }

    private static EventLoopGroup createBossGroup() {
        return new NioEventLoopGroup(1, new DefaultThreadFactory("netty-boss-group", false, (thread, exception) -> {
            log.error("Netty boss group occurs exception.", exception);
        }));
    }

    private EventLoopGroup createWorkerGroup() {
        boolean isDaemon = !this.isGracefulExit;
        return new NioEventLoopGroup(this.coreThreadNum,
                new DefaultThreadFactory("netty-worker-group", isDaemon, (thread, exception) -> {
                    log.error("Netty worker group occurs exception.", exception);
                }));
    }

    private SSLContext createSslContext() throws GeneralSecurityException, IOException {
        notNull(this.httpsConfig, "Https server enabled by default, but https is not configured correctly.");
        String trustStorePassword = this.httpsConfig.trustStorePassword().orElse(StringUtils.EMPTY);
        String keyStorePassword = this.httpsConfig.keyStorePassword().orElse(StringUtils.EMPTY);
        boolean isSecureRandomEnabled = this.httpsConfig.secureRandomEnabled();
        String secureProtocol = this.httpsConfig.secureProtocol().orElse(SECURE_DEFAULT_PROTOCOL);
        if (this.httpsConfig.encrypted()) {
            Decryptor decryptor =
                    notNull(this.container.beans().lookup(Decryptor.class), "The decryptor cannot be null.");
            trustStorePassword = decryptor.decrypt(trustStorePassword);
            keyStorePassword = decryptor.decrypt(keyStorePassword);
        }
        String trustStoreFile = this.httpsConfig.trustStoreFile().orElse(StringUtils.EMPTY);
        String keyStoreFile = this.httpsConfig.keyStoreFile().orElse(StringUtils.EMPTY);
        KeyManager[] keyManagers = SslUtils.getKeyManagers(keyStoreFile, keyStorePassword);
        TrustManager[] trustManagers = SslUtils.getTrustManagers(trustStoreFile, trustStorePassword);
        return SslUtils.getSslContext(keyManagers, trustManagers, isSecureRandomEnabled, secureProtocol);
    }

    @Override
    public HttpDispatcher httpDispatcher() {
        return this.dispatcher;
    }

    @Override
    public WebSocketDispatcher webSocketDispatcher() {
        return this.webSocketDispatcher;
    }

    @Override
    public void send(HttpClassicServerResponse response) {
        notNull(response, "The http classic response to send cannot be null.");
        response.send();
    }

    @Override
    public Serializers serializers() {
        return this.serializers;
    }

    @Override
    public ValueFetcher valueFetcher() {
        return this.valueFetcher;
    }

    private static class ChannelInitializerHandler extends ChannelInitializer<SocketChannel> {
        private static final Map<String, List<String>> defaultCipherSuites = MapBuilder.<String, List<String>>get()
                .put("TLSv1.2",
                        Arrays.asList("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
                                "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
                                "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",
                                "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384",
                                "TLS_PSK_WITH_AES_256_GCM_SHA384",
                                "TLS_DHE_PSK_WITH_AES_128_GCM_SHA256",
                                "TLS_DHE_PSK_WITH_AES_256_GCM_SHA384",
                                "TLS_DHE_PSK_WITH_CHACHA20_POLY1305_SHA256",
                                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                                "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
                                "TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256",
                                "TLS_ECDHE_PSK_WITH_AES_128_GCM_SHA256",
                                "TLS_ECDHE_PSK_WITH_AES_256_GCM_SHA384",
                                "TLS_ECDHE_PSK_WITH_AES_128_CCM_SHA256",
                                "TLS_DHE_RSA_WITH_AES_128_CCM",
                                "TLS_DHE_RSA_WITH_AES_256_CCM",
                                "TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
                                "TLS_PSK_WITH_AES_256_CCM",
                                "TLS_DHE_PSK_WITH_AES_128_CCM",
                                "TLS_DHE_PSK_WITH_AES_256_CCM",
                                "TLS_ECDHE_ECDSA_WITH_AES_128_CCM",
                                "TLS_ECDHE_ECDSA_WITH_AES_256_CCM",
                                "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256"))
                .put("TLSv1.3",
                        Arrays.asList("TLS_AES_128_GCM_SHA256",
                                "TLS_AES_256_GCM_SHA384",
                                "TLS_CHACHA20_POLY1305_SHA256",
                                "TLS_AES_128_CCM_SHA256"))
                .build();

        private final NettyHttpClassicServer server;
        private final SSLContext sslContext;
        private final ServerConfig.Secure httpsConfig;
        private final ProtocolUpgrader upgrader;
        private final ProtocolUpgrader secureUpgrader;
        private final HttpClassicRequestAssembler assembler;
        private final HttpClassicRequestAssembler secureAssembler;

        ChannelInitializerHandler(NettyHttpClassicServer server, HttpClassicRequestAssembler.Config assemblerConfig,
                SSLContext sslContext, ServerConfig.Secure httpsConfig) {
            this.server = server;
            this.sslContext = sslContext;
            this.httpsConfig = httpsConfig;
            this.upgrader = new ProtocolUpgrader(server,
                    false,
                    assemblerConfig.largeBodySize(),
                    assemblerConfig.isGracefulExit());
            this.secureUpgrader = new ProtocolUpgrader(server,
                    true,
                    assemblerConfig.largeBodySize(),
                    assemblerConfig.isGracefulExit());
            this.assembler = new HttpClassicRequestAssembler(server, false, assemblerConfig);
            this.secureAssembler = new HttpClassicRequestAssembler(server, true, assemblerConfig);
        }

        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            int httpsPort = this.server.httpsPort;
            if (ch.localAddress().getPort() == httpsPort && this.sslContext != null && this.httpsConfig != null
                    && this.httpsConfig.isSslEnabled()) {
                pipeline.addLast(new SslHandler(this.buildSslEngine(this.sslContext, this.httpsConfig)));
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(this.secureUpgrader);
                pipeline.addLast(this.secureAssembler);
            } else {
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(this.upgrader);
                pipeline.addLast(this.assembler);
            }
        }

        private SSLEngine buildSslEngine(SSLContext sslContext, ServerConfig.Secure httpsConfig) {
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(httpsConfig.needClientAuth());
            List<String> configuredCipherSuite = CollectionUtils.isNotEmpty(httpsConfig.sslCiphers())
                    ? httpsConfig.sslCiphers()
                    : defaultCipherSuites.getOrDefault(sslContext.getProtocol(), Collections.emptyList());
            // 指定的加密套件与支持的加密套件取交集，保证可用。
            String[] enabledCipherSuite = CollectionUtils.intersect(configuredCipherSuite,
                    Arrays.asList(sslEngine.getSupportedCipherSuites())).toArray(new String[0]);
            sslEngine.setEnabledCipherSuites(enabledCipherSuite);
            return sslEngine;
        }
    }
}
