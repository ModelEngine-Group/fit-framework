/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.domain.stream.reactive;

import modelengine.fit.waterflow.domain.flow.Flow;

/**
 * 数据处理器，既是发布者，也是接收者，处理完数据后再发给下一个接受者。
 *
 * @param <T> 表示接收的数据类型。
 * @param <R> 表示处理后的数据类型。
 * @since 1.0
 */
public interface Processor<T, R> extends Publisher<R>, Subscriber<T, R> {
    /**
     * 关闭流程，生成结束节点。
     *
     * @return 表示数据接受者的 {@link Subscriber}{@code <}{@link R}{@code , }{@link R}{@code >}。
     */
    Subscriber<R, R> close();

    /**
     * 设置数据处理器的名称。
     *
     * @param name 表示名称的 {@link String}。
     * @return 表示数据处理器自身的 {@link Processor}{@code <}{@link T}{@code , }{@link R}{@code >}。
     * @throws IllegalArgumentException 当 {@code name} 为 {@code null} 、空字符串或只有空白字符的字符串时。
     */
    Processor<T, R> displayAs(String name);

    /**
     * 设置数据处理器的名称，及其需要展示的子数据源。
     *
     * @param name 表示名称的 {@link String}。
     * @param displayFlow 表示子流程的 {@link Flow}{@code <}{@link T}{@code >}。
     * @param nodeId 表示 {@code displayFlow} 流程节点名称的 {@link String}。
     * @return 表示数据处理器自身的 {@link Processor}{@code <}{@link T}{@code , }{@link R}{@code >}。
     * @throws IllegalArgumentException
     * <ol>
     *     <li>当 {@code displayFlow} 为 {@code null} 时。</li>
     *     <li>当 {@code name} 为 {@code null} 、空字符串或只有空白字符的字符串时。</li>
     *     <li>当 {@code nodeId} 为 {@code null} 、空字符串或只有空白字符的字符串时。</li>
     * </ol>
     */
    Processor<T, R> displayAs(String name, Flow<T> displayFlow, String nodeId);

    /**
     * 获取节点展示信息。
     *
     * @return 表示展示信息的 {@link NodeDisplay}。
     */
    NodeDisplay display();
}
