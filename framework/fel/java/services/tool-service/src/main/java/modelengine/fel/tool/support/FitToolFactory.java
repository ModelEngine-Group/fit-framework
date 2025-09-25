/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.Tool;
import modelengine.fel.tool.ToolFactory;
import modelengine.fitframework.broker.client.BrokerClient;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.serialization.ObjectSerializer;

/**
 * 表示创建参数工具的工厂。
 *
 * @author 王攀博
 * @since 2024-04-23
 */
public class FitToolFactory implements ToolFactory {
    private final BrokerClient brokerClient;
    private final ObjectSerializer serializer;

    /**
     * 创建 FIT 工厂工厂的实例。
     *
     * @param brokerClient 表示服务调用代理客户端的 {@link BrokerClient}。
     * @param serializer 表示对象序列化器的 {@link ObjectSerializer}。
     * @throws IllegalArgumentException 当 {@code brokerClient}、{@code serializer} 为 {@code null} 时。
     */
    public FitToolFactory(BrokerClient brokerClient, ObjectSerializer serializer) {
        this.brokerClient = notNull(brokerClient, "The broker client cannot be null.");
        this.serializer = notNull(serializer, "The serializer cannot be null.");
    }

    @Nonnull
    @Override
    public String type() {
        return FitTool.TYPE;
    }

    @Override
    public Tool create(Tool.Info itemInfo, Tool.Metadata metadata) {
        return new FitTool(this.brokerClient, this.serializer, itemInfo, metadata);
    }
}
