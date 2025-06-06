/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.emitters;

/**
 * 除了waterflow自身的驱动，waterflow允许外部数据实现InterStream接口
 * 给waterflow响应式灌入外部数据
 * huizi 2024
 *
 * @since 1.0
 */
public interface Emitter<D, T> extends Completable {
    /**
     * 注册监听
     *
     * @param listener 监听器
     */
    void register(EmitterListener<D, T> listener);

    /**
     * 取消监听
     *
     * @param listener 监听器
     */
    void unregister(EmitterListener<D, T> listener);

    /**
     * 发布一个数据，并制定session
     *
     * @param data 待发布的数据
     * @param token 指定的session
     */
    void emit(D data, T token);

    /**
     * 发布一个数据，不指定session
     *
     * @param data 待发布的数据
     */
    default void emit(D data) {
        emit(data, null);
    }

    /**
     * 将持有的数据逐个发布出去
     *
     * @param token 发布数据时归属的session
     */
    default void start(T token) {
    }

    @Override
    default void complete() {
        throw new RuntimeException("Complete is not supported");
    }
}
