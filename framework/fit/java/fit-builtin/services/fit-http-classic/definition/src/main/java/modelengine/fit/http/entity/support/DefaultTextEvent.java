/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.http.entity.support;

import modelengine.fit.http.entity.TextEvent;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.StringUtils;

import java.time.Duration;

/**
 * 表示 {@link TextEvent} 的默认实现。
 *
 * @author 易文渊
 * @author 季聿阶
 * @since 2024-07-17
 */
public class DefaultTextEvent implements TextEvent {
    private final String id;
    private final String event;
    private final Duration retry;
    private final String comment;
    private final Object data;

    private DefaultTextEvent(String id, String event, Duration retry, String comment, Object data) {
        this.id = id;
        this.event = event;
        this.retry = retry;
        this.comment = comment;
        this.data = data;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String event() {
        return this.event;
    }

    @Override
    public Duration retry() {
        return this.retry;
    }

    @Override
    public String comment() {
        return this.comment;
    }

    @Override
    public Object data() {
        return this.data;
    }

    @Override
    public String serialize(ObjectSerializer objectSerializer) {
        Validation.notNull(objectSerializer, "The serializer cannot be null.");
        StringBuilder sb = new StringBuilder();
        appendField(sb, EVENT_ID, this.id);
        appendField(sb, EVENT_NAME, this.event);
        appendField(sb, EVENT_RETRY, this.retry == null ? null : String.valueOf(this.retry.toMillis()));
        appendComment(sb, this.comment);
        appendData(sb, objectSerializer, this.data);
        return sb.toString();
    }

    private static void appendField(StringBuilder sb, String fieldName, String fieldValue) {
        if (fieldValue == null) {
            return;
        }
        sb.append(fieldName).append(COLON).append(fieldValue.trim()).append(LF);
    }

    private static void appendComment(StringBuilder sb, String comment) {
        if (comment == null) {
            return;
        }
        sb.append(COLON).append(StringUtils.replace(comment, LF, LF + COLON)).append(LF);
    }

    private static void appendData(StringBuilder sb, ObjectSerializer objectSerializer, Object data) {
        if (data == null) {
            if (sb.isEmpty()) {
                sb.append(LF);
            }
            sb.append(LF);
            return;
        }
        sb.append(EVENT_DATA).append(COLON);
        String actual;
        if (data instanceof CharSequence) {
            actual = ((CharSequence) data).toString();
        } else {
            actual = objectSerializer.serialize(data);
        }
        sb.append(StringUtils.replace(actual, LF, LF + EVENT_DATA + COLON));
        sb.append(LF).append(LF);
    }

    /**
     * 表示 {@link TextEvent} 的构建器。
     */
    public static class Builder implements TextEvent.Builder {
        private String id;
        private String event;
        private Duration retry;
        private String comment;
        private Object data;

        /**
         * 默认创建 {@link TextEvent.Builder}。
         */
        public Builder() {}

        /**
         * 根据事件内容创建 {@link TextEvent.Builder}。
         *
         * @param data 表示事件内容的 {@link Object}。
         */
        public Builder(Object data) {
            this.data = data;
        }

        /**
         * 向当前构建器中设置事件编号。
         *
         * @param id 表示事件编号的 {@link String}.
         * @return 表示当前构建器的 {@link TextEvent.Builder}。
         */
        public TextEvent.Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * 向当前构建器中设置事件名称。
         *
         * @param event 表示事件名称的 {@link String}.
         * @return 表示当前构建器的 {@link TextEvent.Builder}。
         */
        public TextEvent.Builder event(String event) {
            this.event = event;
            return this;
        }

        /**
         * 向当前构建器中设置客户端重试时间。
         *
         * @param retry 表示客户端重试时间的 {@link Duration}.
         * @return 表示当前构建器的 {@link TextEvent.Builder}。
         */
        public TextEvent.Builder retry(Duration retry) {
            this.retry = retry;
            return this;
        }

        /**
         * 向当前构建器中设置事件注释。
         *
         * @param comment 表示事件注释的 {@link String}.
         * @return 表示当前构建器的 {@link TextEvent.Builder}。
         */
        public TextEvent.Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * 向当前构建器中设置事件内容。
         *
         * @param data 表示事件内容的 {@link Object}.
         * @return 表示当前构建器的 {@link TextEvent.Builder}。
         */
        public TextEvent.Builder data(Object data) {
            this.data = data;
            return this;
        }

        /**
         * 构建对象。
         *
         * @return 表示构建出来对象的 {@link TextEvent}。
         */
        public TextEvent build() {
            return new DefaultTextEvent(this.id, this.event, this.retry, this.comment, this.data);
        }
    }
}
