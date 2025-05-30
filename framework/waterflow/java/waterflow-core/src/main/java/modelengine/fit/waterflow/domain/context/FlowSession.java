/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.context;

import lombok.Setter;
import modelengine.fit.waterflow.domain.utils.IdGenerator;
import modelengine.fitframework.util.MapBuilder;
import modelengine.fitframework.util.ObjectUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * FlowSession 类在数据流（flow）中充当数据边界的角色。
 * 当数据流在 flow 中流转并遇到 reduce 操作时，数据的隔离基于 session。
 * session 内的数据隔离通过 session.window 实现。
 * 每个 session 对应一个 window，window 控制数据聚合操作时的隔离。
 * 当多个 session 在 flow 中流转时，数据保持隔离，直到遇到 unify 关键字，
 * 此时所有 session 会统一成一个 session，完成跨 session 的数据聚合。
 *
 * @author 宋永坦
 * @since 1.0
 */
public class FlowSession extends IdGenerator implements StateContext {
    /**
     * 用户自定义上下文map,用户可以在流操作中通过process关键字操作用户自定义状态
     */
    private static final String CUSTOM_STATE = "custom_state";

    /**
     * 流程内置上下文 map。
     */
    private static final String INNER_STATE = "inner_state";

    /**
     * 会话上下文状态数据。
     */
    private final Map<String, Map<String, Object>> states = MapBuilder.<String, Map<String, Object>>get()
            .put(CUSTOM_STATE, new ConcurrentHashMap<>())
            .put(INNER_STATE, new ConcurrentHashMap<>())
            .build();

    /**
     * 与该 session 关联的window，用于控制数据的聚合和隔离。
     */
    private Window window;

    @Setter
    private Object keyBy = null;

    /**
     * 标识该 session 是否需要保序处理。
     */
    private final Boolean preserved;

    /**
     * 标识该 session 是否为 accumulator（累加器）。
     * 该数据在reduce操作符时赋值
     */
    private boolean isAccumulator;

    /**
     * session 失败时的通知回调。
     */
    private Consumer<Exception> errorHandler;

    /**
     * 构造方法，使用随机 UUID 作为 session ID，并设置保序标识。
     *
     * @param preserved 是否保序
     */
    public FlowSession(boolean preserved) {
        this(UUID.randomUUID().toString(), preserved);
    }

    /**
     * 默认构造方法，创建一个不保序的 session。
     */
    public FlowSession() {
        this(false);
    }

    /**
     * 构造一个 {@link FlowSession} 实例，拷贝运行实例的状态数据。
     *
     * @param id 表示被拷贝的运行实例信息唯一标识的 {@link String}。
     */
    public FlowSession(String id) {
        this(id, false);
    }

    /**
     * 构造方法，使用指定的 ID 和保序标识。
     *
     * @param id        session 的唯一标识
     * @param preserved 是否保序
     */
    public FlowSession(String id, boolean preserved) {
        super(id);
        this.preserved = preserved;
    }

    /**
     * from构造方法，基于已有的 session 创建一个新的 session，
     * 并复制其状态和窗口信息。
     * 设置window的from，建立session在每一个操作符间流转时的流转链
     *
     * @param session from FlowSession 实例
     */
    public FlowSession(FlowSession session) {
        this(session.getId(), session.preserved);
        this.copyState(session);
        this.keyBy = session.keyBy;
        this.begin();
        this.window.setFrom(session.getWindow());
        session.getWindow().addTo(this.window);
    }

    /**
     * Creates a new FlowSession based on an existing session and window configuration.
     * The new session will inherit the ID, preserved state, keyBy value, and other state
     * from the original session, but with the specified window setting.
     *
     * @param session the original {@link FlowSession} to copy properties from.
     * @param window the {@link Window} configuration to apply to the new session.
     * @return a new {@link FlowSession} instance with properties copied from the original session.
     *         and the specified window configuration applied
     */
    public static FlowSession from(FlowSession session, Window window) {
        FlowSession newSession = new FlowSession(session.getId(), session.preserved);
        newSession.copyState(session);
        newSession.keyBy = session.keyBy;
        newSession.setWindow(window);
        return newSession;
    }

    /**
     * Creates a new root-level FlowSession based on an existing session with preservation control.
     * The new session inherits state and key configuration from the original session,
     * initializes as a new session, and incorporates the original session's window settings.
     *
     * @param session the original {@link FlowSession} to copy state from.
     * @param preserved {@code boolean} indicates whether the new session should be created as a preserved session.
     * @return a new root-level {@link FlowSession} initialized with the specified preservation state.
     *         and containing copied state from the original session
     */
    public static FlowSession newRootSession(FlowSession session, boolean preserved) {
        FlowSession newSession = new FlowSession(preserved);
        newSession.copyState(session);
        newSession.keyBy = session.keyBy;
        newSession.begin();
        return newSession;
    }

    /**
     * 将本context设置为accumulator
     */
    public void setAsAccumulator() {
        this.isAccumulator = true;
    }

    /**
     * 判定是否是accumulator
     *
     * @return true/false
     */
    public boolean isAccumulator() {
        return this.isAccumulator;
    }

    /**
     * 获取当前 session 的window对象。
     *
     * @return 当前的 Window 实例
     */
    public Window getWindow() {
        return this.window;
    }

    /**
     * 设置当前 session 的window对象，并确保window关联到当前 session。
     *
     * @param window 要设置的 Window 实例
     */
    public <I> void setWindow(Window window) {
        this.window = window;
        if (this.window.getSession() != this) {
            this.window.setSession(this);
        }
    }

    /**
     * 获取当前实例的数据是否全部处理完成。
     *
     * @return 表示前实例的数据是否全部处理完成的 {@code boolean}。
     */
    public boolean isCompleted() {
        return this.window.getRootWindow().isAllDone();
    }

    /**
     * 判断两个会话是否相同。
     *
     * @param session 表示待判定的 {@link FlowSession}。
     * @return 表示会话是否相同的 {@code boolean}。
     */
    public boolean equals(FlowSession session) {
        return this.id.equals(session.id);
    }

    /**
     * 获取会话的关键标识。
     *
     * @return 表示关键标识的 {@link Object}。
     */
    public Object keyBy() {
        return this.keyBy;
    }

    /**
     * 判断当前 session 是否需要保序处理。
     *
     * @return 如果需要保序返回 true，否则返回 false
     */
    public boolean preserved() {
        return this.preserved;
    }

    /**
     * 获取指定key的自定义上下文数据
     *
     * @param key 表示键的 {@link String}。
     * @return 表示上下文数据的 {@link R}。
     */
    @Override
    public <R> R getState(String key) {
        return ObjectUtils.cast(this.states.get(CUSTOM_STATE).get(key));
    }

    /**
     * 设置自定义上下文数据。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示待设置的上下文数据的 {@link Object}。
     */
    @Override
    public void setState(String key, Object value) {
        this.states.get(CUSTOM_STATE).put(key, value);
    }

    /**
     * 获取指定键的内置上下文数据。
     *
     * @param key 表示键的 {@link String}。
     * @return 上下文数据 {@link R}。
     */
    public <R> R getInnerState(String key) {
        return ObjectUtils.cast(this.states.get(INNER_STATE).get(key));
    }

    /**
     * 设置内置上下文数据。
     *
     * @param key 表示键的 {@link String}。
     * @param value 表示待设置的上下文数据的 {@link Object}。
     */
    public void setInnerState(String key, Object value) {
        this.states.get(INNER_STATE).put(key, value);
    }

    /**
     * 拷贝运行实例的状态数据。
     *
     * @param session 表示被拷贝的运行实例信息的 {@link FlowSession}。
     */
    public void copySessionState(FlowSession session) {
        this.copyState(session);
    }

    /**
     * 设置监听失败的回调，需要在 session 工作前设置。
     *
     * @param errorHandler 回调。
     */
    public void onError(Consumer<Exception> errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * 触发失败，通知回调。
     *
     * @param exception 异常对象。
     */
    public void fail(Exception exception) {
        if (this.errorHandler != null) {
            this.errorHandler.accept(exception);
        }
    }

    /**
     * 拷贝运行实例的状态数据。
     *
     * @param session 表示被拷贝的运行实例信息的 {@link FlowSession}。
     */
    private void copyState(FlowSession session) {
        if (session != null) {
            this.states.get(CUSTOM_STATE).putAll(session.states.get(CUSTOM_STATE));
            this.states.get(INNER_STATE).putAll(session.states.get(INNER_STATE));
        }
    }

    /**
     * 开始当前 session 的窗口，如果窗口尚未初始化，则创建一个新的 Window 实例并关联到当前 session。
     *
     * @return 当前的 Window 实例
     */
    public <I> Window begin() {
        if (this.window == null) {
            this.window = new Window();
            this.window.setSession(this);
        }
        return this.window;
    }
}
