/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.http.client.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import modelengine.fit.http.client.HttpClassicClientRequest;
import modelengine.fit.http.client.HttpClassicClientResponse;
import modelengine.fit.http.entity.TextEventStreamEntity;
import modelengine.fitframework.flowable.Choir;
import modelengine.fitframework.flowable.Emitter;
import modelengine.fitframework.flowable.Subscriber;
import modelengine.fitframework.flowable.Subscription;
import modelengine.fitframework.flowable.emitter.DefaultEmitter;
import modelengine.fitframework.flowable.subscriber.EmptySubscriber;
import modelengine.fitframework.util.ThreadUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 为 {@link TextStreamChoir} 提供单元测试。
 *
 * @author 何天放
 * @since 2024-11-04
 */
@DisplayName("测试 TextStreamChoir")
class TextStreamChoirTest {
    private static class TestSubscriber<T> extends EmptySubscriber<T> {
        private final List<String> records;

        public TestSubscriber(List<String> records) {
            this.records = records;
        }

        @Override
        protected void onSubscribed0(Subscription subscription) {
            subscription.request(1);
        }

        @Override
        protected void consume(Subscription subscription, T data) {
            this.records.add(data.toString());
            subscription.request(1);
        }

        @Override
        protected void complete(Subscription subscription) {
            super.complete(subscription);
            this.records.add("complete");
        }

        @Override
        protected void fail(Subscription subscription, Exception cause) {
            super.fail(subscription, cause);
            this.records.add("fail");
        }
    }

    private static TextStreamChoir<String> createStringTextStreamChoir(Choir<String> convertedChoir, int statusCode) {
        Choir mockedTextEventStream = mock(Choir.class);
        when(mockedTextEventStream.map(any())).thenReturn(convertedChoir);

        TextEventStreamEntity mockTextEventStreamEntity = mock(TextEventStreamEntity.class);
        when(mockTextEventStreamEntity.stream()).thenReturn(mockedTextEventStream);

        HttpClassicClientResponse<Object> mockHttpClassicClientResponse = mock(HttpClassicClientResponse.class);
        when(mockHttpClassicClientResponse.textEventStreamEntity()).thenReturn(Optional.of(mockTextEventStreamEntity));
        when(mockHttpClassicClientResponse.statusCode()).thenReturn(statusCode);

        HttpClassicClientRequest mockHttpClassicClientRequest = mock(HttpClassicClientRequest.class);
        when(mockHttpClassicClientRequest.exchange(any())).thenReturn(mockHttpClassicClientResponse);

        return new TextStreamChoir<>(mockHttpClassicClientRequest, String.class);
    }

    @Test
    @DisplayName("当在完整订阅流程结束前发送完数据时，结果符合预期")
    void shouldReturnDataAndCompleteWhenDataEmittedBeforeSubscribeCompleted() {
        Choir<String> convertedChoir = Choir.create(stringEmitter -> {
            for (int i = 0; i < 3; i++) {
                stringEmitter.emit("value " + i);
            }
            stringEmitter.complete();
        });
        TextStreamChoir<String> textStreamChoir = createStringTextStreamChoir(convertedChoir, 200);
        List<String> records = Collections.synchronizedList(new ArrayList<>());
        Subscriber<String> mySubscriber = new TestSubscriber<>(records);
        textStreamChoir.subscribe(mySubscriber);
        records.add("after subscribe");
        assertThat(records).isEqualTo(new ArrayList<>(Arrays.asList("value 0",
                "value 1",
                "value 2",
                "complete",
                "after subscribe")));
    }

    @Test
    @DisplayName("当在订阅流程结束后发送数据时，结果符合预期")
    void shouldReturnDataAndCompleteWhenDataEmittedAfterSubscribeCompleted() {
        Emitter<String> emitter = new DefaultEmitter<>();
        Choir<String> convertedChoir = Choir.fromEmitter(emitter);
        TextStreamChoir<String> textStreamChoir = createStringTextStreamChoir(convertedChoir, 200);
        List<String> records = Collections.synchronizedList(new ArrayList<>());
        Subscriber<String> mySubscriber = new TestSubscriber<>(records);
        textStreamChoir.subscribe(mySubscriber);
        records.add("after subscribe");
        emitter.emit("value 0");
        emitter.emit("value 1");
        emitter.emit("value 2");
        emitter.complete();
        assertThat(records).isEqualTo(new ArrayList<>(Arrays.asList("after subscribe",
                "value 0",
                "value 1",
                "value 2",
                "complete")));
    }

    @Test
    @DisplayName("当使用额外的后台线程在订阅流程结束后发送数据时，结果符合预期")
    void shouldReturnDataAndCompleteWhenDataEmittedAfterSubscribeCompletedWithNewThread() {
        Choir<String> convertedChoir = Choir.create(stringEmitter -> {
            new Thread(() -> {
                ThreadUtils.sleep(5);
                for (int i = 0; i < 3; i++) {
                    stringEmitter.emit("value " + i);
                }
                stringEmitter.complete();
            }).start();
        });
        TextStreamChoir<String> textStreamChoir = createStringTextStreamChoir(convertedChoir, 200);
        List<String> records = Collections.synchronizedList(new ArrayList<>());
        Subscriber<String> mySubscriber = new TestSubscriber<>(records);
        textStreamChoir.subscribe(mySubscriber);
        records.add("after subscribe");
        while (records.size() < 4) {
            ThreadUtils.sleep(1);
        }
        assertThat(records).isEqualTo(new ArrayList<>(Arrays.asList("after subscribe",
                "value 0",
                "value 1",
                "value 2",
                "complete")));
    }

    @Test
    @DisplayName("当使用 HTTP 获取数据发生异常时，结果符合预期")
    void shouldReturnDataAndFailWhenHttpExchangeFailAndChoirCreateByCreate() {
        Choir<String> convertedChoir = Choir.create(stringEmitter -> {
            for (int i = 0; i < 3; i++) {
                stringEmitter.emit("value " + i);
            }
            stringEmitter.complete();
        });
        TextStreamChoir<String> textStreamChoir = createStringTextStreamChoir(convertedChoir, 500);
        List<String> records = Collections.synchronizedList(new ArrayList<>());
        Subscriber<String> mySubscriber = new TestSubscriber<>(records);
        textStreamChoir.subscribe(mySubscriber);
        records.add("after subscribe");
        assertThat(records).isEqualTo(new ArrayList<>(Arrays.asList("fail", "after subscribe")));
    }

    @Test
    @DisplayName("当使用 HTTP 获取数据发生异常并且不含任何数据发送逻辑时，结果符合预期")
    void shouldReturnDataAndFailWhenHttpExchangeFailAndChoirCreateByFromEmitter() {
        Emitter<String> emitter = new DefaultEmitter<>();
        Choir<String> convertedChoir = Choir.fromEmitter(emitter);
        TextStreamChoir<String> textStreamChoir = createStringTextStreamChoir(convertedChoir, 400);
        List<String> records = Collections.synchronizedList(new ArrayList<>());
        Subscriber<String> mySubscriber = new TestSubscriber<>(records);
        textStreamChoir.subscribe(mySubscriber);
        records.add("after subscribe");
        assertThat(records).isEqualTo(new ArrayList<>(Arrays.asList("fail", "after subscribe")));
    }

    @Test
    @DisplayName("当在完整订阅流程未完全结束时有异常终结信号发送时，结果符合预期")
    void shouldReturnDataAndFailWhenDataEmittedBeforeSubscribeCompleted() {
        Choir<String> convertedChoir = Choir.create(stringEmitter -> {
            for (int i = 0; i < 3; i++) {
                stringEmitter.emit("value " + i);
            }
            stringEmitter.fail(new RuntimeException(""));
        });
        TextStreamChoir<String> textStreamChoir = createStringTextStreamChoir(convertedChoir, 200);
        List<String> records = Collections.synchronizedList(new ArrayList<>());
        Subscriber<String> mySubscriber = new TestSubscriber<>(records);
        textStreamChoir.subscribe(mySubscriber);
        records.add("after subscribe");
        assertThat(records).isEqualTo(new ArrayList<>(Arrays.asList("value 0",
                "value 1",
                "value 2",
                "fail",
                "after subscribe")));
    }

    @Test
    @DisplayName("当在完整订阅流程结束后有异常终结信号发送时，结果符合预期")
    void shouldReturnDataAndFailWhenDataEmittedAfterSubscribeCompleted() {
        Emitter<String> emitter = new DefaultEmitter<>();
        Choir<String> convertedChoir = Choir.fromEmitter(emitter);
        TextStreamChoir<String> textStreamChoir = createStringTextStreamChoir(convertedChoir, 200);
        List<String> records = Collections.synchronizedList(new ArrayList<>());
        Subscriber<String> mySubscriber = new TestSubscriber<>(records);
        textStreamChoir.subscribe(mySubscriber);
        records.add("after subscribe");
        emitter.emit("value 0");
        emitter.emit("value 1");
        emitter.emit("value 2");
        emitter.fail(new Exception(""));
        assertThat(records).isEqualTo(new ArrayList<>(Arrays.asList("after subscribe",
                "value 0",
                "value 1",
                "value 2",
                "fail")));
    }

    @Test
    @DisplayName("当使用额外线程在完整订阅流程结束后发送异常终结信号时，结果符合预期")
    void shouldReturnDataAndFailWhenDataEmittedAfterSubscribeCompletedWithNewThread() {
        Choir<String> convertedChoir = Choir.create(stringEmitter -> {
            new Thread(() -> {
                ThreadUtils.sleep(5);
                for (int i = 0; i < 3; i++) {
                    stringEmitter.emit("value " + i);
                }
                stringEmitter.fail(new Exception(""));
            }).start();
        });
        TextStreamChoir<String> textStreamChoir = createStringTextStreamChoir(convertedChoir, 200);
        List<String> records = Collections.synchronizedList(new ArrayList<>());
        Subscriber<String> mySubscriber = new TestSubscriber<>(records);
        textStreamChoir.subscribe(mySubscriber);
        records.add("after subscribe");
        while (records.size() < 4) {
            ThreadUtils.sleep(1);
        }
        assertThat(records).isEqualTo(new ArrayList<>(Arrays.asList("after subscribe",
                "value 0",
                "value 1",
                "value 2",
                "fail")));
    }
}
