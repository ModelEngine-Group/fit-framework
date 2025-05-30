/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.waterflow.entity;

/**
 * 操作人相关上下文。
 *
 * @author 陈镕希
 * @since 2023-08-28
 */
public interface OperationContext {
    /**
     * 获取正在操作的租户的唯一标识。
     *
     * @return 表示正在操作的租户的唯一标识的 {@link String}。
     */
    String tenantId();

    /**
     * 获取操作人的名称。
     *
     * @return 表示操作人名称的 {@link String}。
     */
    String operator();

    /**
     * 获取操作方的 IP 地址。
     *
     * @return 表示 IP 地址的 {@link String}。
     */
    String operatorIp();

    /**
     * 获取操作方的语言。
     *
     * @return 表示 语言 {@link String}。
     */
    String language();

    /**
     * 获取操作方的标识。
     *
     * @return 表示 操作方标识 {@link String}。
     */
    String sourcePlatform();

    /**
     * 为 {@link OperationContext} 提供构建器。
     *
     * @author 梁济时
     * @since 2023-08-08
     */
    interface Builder {
        /**
         * 设置正在操作的租户的唯一标识。
         *
         * @param tenantId 表示正在操作的租户的唯一标识的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder tenantId(String tenantId);

        /**
         * 设置操作人的名称。
         *
         * @param operator 表示操作人名称的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder operator(String operator);

        /**
         * 设置操作方的 IP 地址。
         *
         * @param operatorIp 表示 IP 地址的 {@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder operatorIp(String operatorIp);

        /**
         * 设置操作方的语言。
         *
         * @param langage 表示 语言{@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder langage(String langage);

        /**
         * 设置操作方的标识。
         *
         * @param sourcePlatform 表示 操作标识{@link String}。
         * @return 表示当前构建器的 {@link Builder}。
         */
        Builder sourcePlatform(String sourcePlatform);

        /**
         * 构建操作上下文的新实例。
         *
         * @return 表示操作上下文新实例的 {@link OperationContext}。
         */
        OperationContext build();
    }

    /**
     * 返回一个构建器，用以构建操作上下文的新实例。
     *
     * @return 表示用以构建操作上下文新实例的构建器的 {@link Builder}。
     */
    static Builder custom() {
        return new DefaultOperationContext.Builder();
    }

    /**
     * 获取空的上下文信息。
     *
     * @return 表示空的上下文信息的 {@link OperationContext}。
     */
    static OperationContext empty() {
        return DefaultOperationContext.EMPTY;
    }
}