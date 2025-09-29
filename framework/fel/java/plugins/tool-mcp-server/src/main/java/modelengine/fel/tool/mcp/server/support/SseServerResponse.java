package modelengine.fel.tool.mcp.server.support;

import modelengine.fit.http.protocol.HttpResponseStatus;
import modelengine.fitframework.inspection.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

final class SseServerResponse extends AbstractServerResponse {

    private final Consumer<SseBuilder> sseConsumer;

    @Nullable
    private final Duration timeout;


    private SseServerResponse(Consumer<SseBuilder> sseConsumer, @Nullable Duration timeout) {
        super(HttpResponseStatus.OK.statusCode(), createHeaders(), emptyCookies());
        this.sseConsumer = sseConsumer;
        this.timeout = timeout;
    }

    private static HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_EVENT_STREAM);
        headers.setCacheControl(CacheControl.noCache());
        return headers;
    }

    private static MultiValueMap<String, Cookie> emptyCookies() {
        return CollectionUtils.toMultiValueMap(Collections.emptyMap());
    }


    @Nullable
    @Override
    protected ModelAndView writeToInternal(HttpServletRequest request, HttpServletResponse response,
                                           Context context) throws ServletException, IOException {

        DeferredResult<?> result;
        if (this.timeout != null) {
            result = new DeferredResult<>(this.timeout.toMillis());
        }
        else {
            result = new DeferredResult<>();
        }

        DefaultAsyncServerResponse.writeAsync(request, response, result);
        this.sseConsumer.accept(new DefaultSseBuilder(response, context, result, this.headers()));
        return null;
    }


    public static ServerResponse create(Consumer<SseBuilder> sseConsumer, @Nullable Duration timeout) {
        Assert.notNull(sseConsumer, "SseConsumer must not be null");

        return new SseServerResponse(sseConsumer, timeout);
    }


    private static final class SseBuilder {

        private static final byte[] NL_NL = new byte[]{'\n', '\n'};


        private final ServerHttpResponse outputMessage;

        private final DeferredResult<?> deferredResult;

        private final List<HttpMessageConverter<?>> messageConverters;

        private final HttpHeaders httpHeaders;

        private final StringBuilder builder = new StringBuilder();

        private boolean sendFailed;


        public SseBuilder(HttpServletResponse response, Context context, DeferredResult<?> deferredResult,
                                 HttpHeaders httpHeaders) {
            this.outputMessage = new ServletServerHttpResponse(response);
            this.deferredResult = deferredResult;
            this.messageConverters = context.messageConverters();
            this.httpHeaders = httpHeaders;
        }

        @Override
        public void send(Object object) throws IOException {
            data(object);
        }

        @Override
        public void send() throws IOException {
            this.builder.append('\n');
            try {
                OutputStream body = this.outputMessage.getBody();
                body.write(builderBytes());
                body.flush();
            }
            catch (IOException ex) {
                this.sendFailed = true;
                throw ex;
            }
            finally {
                this.builder.setLength(0);
            }
        }

        @Override
        public SseBuilder id(String id) {
            Assert.hasLength(id, "Id must not be empty");
            return field("id", id);
        }

        @Override
        public SseBuilder event(String eventName) {
            Assert.hasLength(eventName, "Name must not be empty");
            return field("event", eventName);
        }

        @Override
        public SseBuilder retry(Duration duration) {
            Assert.notNull(duration, "Duration must not be null");
            String millis = Long.toString(duration.toMillis());
            return field("retry", millis);
        }

        @Override
        public SseBuilder comment(String comment) {
            String[] lines = comment.split("\n");
            for (String line : lines) {
                field("", line);
            }
            return this;
        }

        private SseBuilder field(String name, String value) {
            this.builder.append(name).append(':').append(value).append('\n');
            return this;
        }

        @Override
        public void data(Object object) throws IOException {
            Assert.notNull(object, "Object must not be null");

            if (object instanceof String text) {
                writeString(text);
            }
            else {
                writeObject(object);
            }
        }

        private void writeString(String string) throws IOException {
            String[] lines = string.split("\n");
            for (String line : lines) {
                field("data", line);
            }
            this.send();
        }

        @SuppressWarnings("unchecked")
        private void writeObject(Object data) throws IOException {
            this.builder.append("data:");
            try {
                this.outputMessage.getBody().write(builderBytes());

                Class<?> dataClass = data.getClass();
                for (HttpMessageConverter<?> converter : this.messageConverters) {
                    if (converter.canWrite(dataClass, MediaType.APPLICATION_JSON)) {
                        HttpMessageConverter<Object> objectConverter = (HttpMessageConverter<Object>) converter;
                        ServerHttpResponse response = new MutableHeadersServerHttpResponse(this.outputMessage, this.httpHeaders);
                        objectConverter.write(data, MediaType.APPLICATION_JSON, response);
                        this.outputMessage.getBody().write(NL_NL);
                        this.outputMessage.flush();
                        return;
                    }
                }
            }
            catch (IOException ex) {
                this.sendFailed = true;
                throw ex;
            }
            finally {
                this.builder.setLength(0);
            }
        }

        private byte[] builderBytes() {
            return this.builder.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void error(Throwable t) {
            if (this.sendFailed) {
                return;
            }
            this.deferredResult.setErrorResult(t);
        }

        @Override
        public void complete() {
            if (this.sendFailed) {
                return;
            }
            try {
                this.outputMessage.flush();
                this.deferredResult.setResult(null);
            }
            catch (IOException ex) {
                this.deferredResult.setErrorResult(ex);
            }
        }

        @Override
        public SseBuilder onTimeout(Runnable onTimeout) {
            this.deferredResult.onTimeout(onTimeout);
            return this;
        }

        @Override
        public SseBuilder onError(Consumer<Throwable> onError) {
            this.deferredResult.onError(onError);
            return this;
        }

        @Override
        public SseBuilder onComplete(Runnable onCompletion) {
            this.deferredResult.onCompletion(onCompletion);
            return this;
        }


        /**
         * Wrap to silently ignore header changes HttpMessageConverter's that would
         * otherwise cause HttpHeaders to raise exceptions.
         */
        private static final class MutableHeadersServerHttpResponse extends DelegatingServerHttpResponse {

            private final HttpHeaders mutableHeaders = new HttpHeaders();

            public MutableHeadersServerHttpResponse(ServerHttpResponse delegate, HttpHeaders headers) {
                super(delegate);
                this.mutableHeaders.putAll(delegate.getHeaders());
                this.mutableHeaders.putAll(headers);
            }

            @Override
            public HttpHeaders getHeaders() {
                return this.mutableHeaders;
            }

        }

    }
}