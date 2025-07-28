/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.server.netty;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static modelengine.fit.http.HttpClassicRequestAttribute.HTTP_HANDLER;
import static modelengine.fit.http.HttpClassicRequestAttribute.PATH_PATTERN;
import static modelengine.fit.http.protocol.MessageHeaderNames.CONTENT_LENGTH;
import static modelengine.fit.http.protocol.MessageHeaderNames.CONTENT_TYPE;
import static modelengine.fit.http.protocol.MimeType.APPLICATION_JSON;
import static modelengine.fit.http.protocol.MimeType.TEXT_PLAIN;
import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.cast;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;
import modelengine.fit.http.protocol.HttpResponseStatus;
import modelengine.fit.http.server.ErrorResponse;
import modelengine.fit.http.server.HttpClassicServer;
import modelengine.fit.http.server.HttpClassicServerRequest;
import modelengine.fit.http.server.HttpClassicServerResponse;
import modelengine.fit.http.server.HttpHandler;
import modelengine.fit.http.server.HttpServerFilterChain;
import modelengine.fit.http.server.HttpServerResponseException;
import modelengine.fit.http.server.support.DefaultHttpServerFilterChain;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.pattern.builder.BuilderFactory;
import modelengine.fitframework.schedule.Task;
import modelengine.fitframework.schedule.ThreadPoolExecutor;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

/**
 * {@link HttpClassicServerRequest} 的组装器。
 *
 * @author 季聿阶
 * @since 2022-07-14
 */
@ChannelHandler.Sharable
public class HttpClassicRequestAssembler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger log = Logger.get(HttpClassicRequestAssembler.class);
    private static final AttributeKey<NettyHttpServerRequest> REQUEST = AttributeKey.valueOf("request");

    private final HttpClassicServer server;
    private final boolean secure;
    private final Config config;
    private final ThreadPoolExecutor threadPoolExecutor;

    public HttpClassicRequestAssembler(HttpClassicServer server, boolean secure, Config config) {
        this.server = notNull(server, "The http server cannot be null.");
        this.secure = secure;
        this.config = notNull(config, "The assembler config cannot be null.");
        this.threadPoolExecutor = ThreadPoolExecutor.custom()
                .threadPoolName("netty-request-assembler")
                .awaitTermination(3, TimeUnit.SECONDS)
                .isImmediateShutdown(!this.config.isGracefulExit())
                .corePoolSize(config.coreThreadNum())
                .maximumPoolSize(config.maxThreadNum())
                .keepAliveTime(60, TimeUnit.SECONDS)
                .workQueueCapacity(config.queueCapacity())
                .isDaemonThread(!this.config.isGracefulExit())
                .exceptionHandler((thread, cause) -> log.error("Failed to handle http request by request assembler.",
                        cause))
                .rejectedExecutionHandler(new AbortPolicy())
                .build();
    }

    private static void setRequest(ChannelHandlerContext ctx, NettyHttpServerRequest serverRequest) {
        Attribute<NettyHttpServerRequest> attr = ((AttributeMap) ctx).attr(REQUEST);
        attr.set(serverRequest);
    }

    private static NettyHttpServerRequest getRequest(ChannelHandlerContext ctx) {
        return ((AttributeMap) ctx).attr(REQUEST).get();
    }

    private static void clearRequest(ChannelHandlerContext ctx) {
        NettyHttpServerRequest request = getRequest(ctx);
        if (request != null) {
            ((AttributeMap) ctx).attr(REQUEST).set(null);
            try {
                request.close();
            } catch (IOException e) {
                log.warn("Failed to close netty http server request, ignored.", e);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Failed to handle http request by netty worker.", cause);
        this.returnError(ctx, cause, getRequest(ctx));
    }

    private void exceptionCaught(ChannelHandlerContext ctx, Throwable cause, NettyHttpServerRequest request) {
        log.error("Failed to handle http request by request assembler.", cause);
        this.returnError(ctx, cause, request);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.stopExecution(ctx);
        super.channelInactive(ctx);
    }

    private void stopExecution(ChannelHandlerContext ctx) {
        NettyHttpServerRequest request = getRequest(ctx);
        if (request != null) {
            request.interruptExecution();
            clearRequest(ctx);
        }
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            clearRequest(ctx);
            this.handleHttpRequest(ctx, cast(msg));
            return;
        }
        if (msg instanceof HttpContent) {
            this.handleHttpContent(ctx, cast(msg));
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest request) {
        NettyHttpServerRequest serverRequest =
                new NettyHttpServerRequest(request, ctx, this.secure, this.config.largeBodySize());
        setRequest(ctx, serverRequest);
        if (HttpUtil.is100ContinueExpected(request)) {
            this.return100Continue(ctx);
        } else {
            this.threadPoolExecutor.execute(Task.builder()
                    .runnable(() -> this.doHttpRequest(ctx, serverRequest))
                    .uncaughtExceptionHandler((thread, cause) -> this.exceptionCaught(ctx, cause, serverRequest))
                    .buildDisposable());
        }
    }

    private void doHttpRequest(ChannelHandlerContext ctx, NettyHttpServerRequest request) {
        request.setExecuteThread(Thread.currentThread());
        try (HttpClassicServerRequest classicRequest = HttpClassicServerRequest.create(this.server, request);
             NettyHttpServerResponse response = new NettyHttpServerResponse(ctx, request);
             HttpClassicServerResponse classicResponse = HttpClassicServerResponse.create(this.server, response)) {
            HttpHandler handler = this.server.httpDispatcher().dispatch(classicRequest, classicResponse);
            classicRequest.attributes().set(PATH_PATTERN.key(), handler.pathPattern());
            classicRequest.attributes().set(HTTP_HANDLER.key(), handler);
            HttpServerFilterChain filterChain = new DefaultHttpServerFilterChain(handler);
            this.doFilterChain(filterChain, classicRequest, classicResponse);
        } catch (Exception cause) {
            this.exceptionCaught(ctx, cause, request);
        } finally {
            request.removeExecuteThread();
            try {
                request.tryClose();
            } catch (IOException e) {
                log.warn("Failed to close netty http server request when request finished, ignored.", e);
            }
        }
    }

    private void handleHttpContent(ChannelHandlerContext ctx, HttpContent content) {
        NettyHttpServerRequest request = getRequest(ctx);
        if (request == null) {
            String message = StringUtils.format(
                    "No request info. [remoteAddress={0}, channelIsActive={1}, channelIsOpen={2}]",
                    ctx.channel().remoteAddress(),
                    ctx.channel().isActive(),
                    ctx.channel().isOpen());
            throw new IllegalStateException(message);
        }
        this.receiveHttpContent(request, content);
    }

    private void receiveHttpContent(NettyHttpServerRequest serverRequest, HttpContent content) {
        try {
            if (content instanceof LastHttpContent) {
                serverRequest.receiveLastHttpContent(cast(content));
                try {
                    serverRequest.tryClose();
                } catch (IOException e) {
                    log.warn("Failed to close netty http server request when received last http content, ignored.", e);
                }
            } else {
                serverRequest.receiveHttpContent(content);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to receive http content.", e);
        }
    }

    private void doFilterChain(HttpServerFilterChain chain, HttpClassicServerRequest classicRequest,
            HttpClassicServerResponse classicResponse) {
        chain.doFilter(classicRequest, classicResponse);
        this.server.send(classicResponse);
    }

    private void return100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER);
        ctx.writeAndFlush(response);
    }

    private void returnError(ChannelHandlerContext ctx, Throwable cause, NettyHttpServerRequest request) {
        String path = request == null ? "unknown" : request.startLine().requestUri();
        String errorMessage = this.config.shouldDisplayError() && StringUtils.isNotBlank(cause.getMessage())
                ? cause.getMessage()
                : "Internal Server Error";
        FullHttpResponse response = this.server.serializers()
                .json()
                .map(objectSerializer -> this.returnErrorByJson(cause, path, errorMessage, objectSerializer))
                .orElseGet(() -> this.returnErrorByText(path, errorMessage));
        ctx.writeAndFlush(response);
        this.stopExecution(ctx);
    }

    private FullHttpResponse returnErrorByJson(Throwable cause, String path, String errorMessage,
            ObjectSerializer jsonSerializer) {
        ErrorResponse errorResponse;
        if (cause instanceof HttpServerResponseException) {
            HttpServerResponseException actualException = cast(cause);
            errorResponse = ErrorResponse.create(actualException.responseStatus(), errorMessage, path);
        } else {
            errorResponse = ErrorResponse.create(HttpResponseStatus.INTERNAL_SERVER_ERROR, errorMessage, path);
        }
        byte[] serialized = jsonSerializer.serialize(errorResponse, UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                io.netty.handler.codec.http.HttpResponseStatus.valueOf(errorResponse.getStatus()),
                Unpooled.copiedBuffer(serialized));
        response.headers().set(CONTENT_TYPE, APPLICATION_JSON.value());
        response.headers().set(CONTENT_LENGTH, serialized.length);
        return response;
    }

    private FullHttpResponse returnErrorByText(String path, String errorMessage) {
        byte[] serialized = StringUtils.format("message: {0}, path: {1}", errorMessage, path).getBytes(UTF_8);
        FullHttpResponse response =
                new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer(serialized));
        response.headers().set(CONTENT_TYPE, TEXT_PLAIN.value());
        response.headers().set(CONTENT_LENGTH, serialized.length);
        return response;
    }

    /**
     * 表示 {@link HttpClassicRequestAssembler} 的配置信息。
     */
    public interface Config {
        /**
         * 获取是否需要显示错误信息的标记。
         *
         * @return 表示是否需要显示错误信息的标记的 {@code boolean}。
         */
        boolean shouldDisplayError();

        /**
         * 获取巨大消息体的阈值。
         *
         * @return 表示巨大消息体的阈值的 {@code long}。
         */
        long largeBodySize();

        /**
         * 获取线程池的核心线程数量。
         *
         * @return 表示线程池的核心线程数量的 {@code int}。
         */
        int coreThreadNum();

        /**
         * 获取线程池的最大线程数量。
         *
         * @return 表示线程池的最大线程数量的 {@code int}。
         */
        int maxThreadNum();

        /**
         * 获取线程池的等待队列的大小。
         *
         * @return 表示线程池的等待队列大小的 {@code int}。
         */
        int queueCapacity();

        /**
         * 获取是否需要优雅退出的标记。
         *
         * @return 表示是否需要优雅退出的标记的 {@code boolean}。
         */
        boolean isGracefulExit();

        /**
         * 表示 {@link Config} 的构建器。
         */
        interface Builder {
            /**
             * 向当前构建器中设置是否需要显示错误信息的标记。
             *
             * @param displayError 表示待设置的是否需要显示错误信息的标记的 {@code boolean}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder shouldDisplayError(boolean displayError);

            /**
             * 向当前构建器中设置巨大消息体的阈值。
             *
             * @param largeBodySize 表示待设置的巨大消息体的阈值的 {@code long}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder largeBodySize(long largeBodySize);

            /**
             * 向当前构建器中设置核心线程池数量的 {@code int}。
             *
             * @param coreThreadNum 表示待设置的线程池核心线程数量的 {@code int}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder coreThreadNum(int coreThreadNum);

            /**
             * 向当前构建器中设置线程池的最大线程数量。
             *
             * @param maxThreadNum 表示待设置的线程池的最大线程数量的 {@code int}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder maxThreadNum(int maxThreadNum);

            /**
             * 向当前构建器中设置线程池的等待队列大小。
             *
             * @param queueCapacity 表示待设置的线程池等待队列大小的 {@code int}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder queueCapacity(int queueCapacity);

            /**
             * 向当前构建器中设置是否需要优雅退出的标记。
             *
             * @param isGracefulExit 表示待设置的是否需要优雅退出的标记的 {@code boolean}。
             * @return 表示当前构建器的 {@link Builder}。
             */
            Builder isGracefulExit(boolean isGracefulExit);

            /**
             * 构建对象。
             *
             * @return 表示构建出来的对象的 {@link Config}。
             */
            Config build();
        }

        /**
         * 获取 {@link Config} 的构建器。
         *
         * @return 表示 {@link Config} 的构建器的 {@link Builder}。
         */
        static Builder custom() {
            return custom(null);
        }

        /**
         * 获取 {@link Config} 的构建器，同时将指定对象的值进行填充。
         *
         * @param value 表示指定对象的 {@link Config}。
         * @return 表示 {@link Config} 的构建器的 {@link Builder}。
         */
        static Builder custom(Config value) {
            return BuilderFactory.get(Config.class, Builder.class).create(value);
        }
    }
}
