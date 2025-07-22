/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.support;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fel.tool.ToolFactory;
import modelengine.fel.tool.ToolFactoryRepository;
import modelengine.fit.http.client.HttpClassicClientFactory;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fit;
import modelengine.fitframework.broker.client.BrokerClient;
import modelengine.fitframework.ioc.BeanFactory;
import modelengine.fitframework.plugin.Plugin;
import modelengine.fitframework.plugin.PluginStartedObserver;
import modelengine.fitframework.plugin.PluginStoppingObserver;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.value.ValueFetcher;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示 {@link ToolFactory} 的自动装配器。
 *
 * @since 2024-08-15
 */
@Component
public class ToolFactoryDiscoverer implements PluginStartedObserver, PluginStoppingObserver {
    private final ToolFactoryRepository factoryRepository;

    /**
     * 创建工具工厂的自动装配器实例。
     *
     * @param factoryRepository 表示工具工厂存储的 {@link ToolFactoryRepository}。
     * @param brokerClient 表示 FIT 调用的代理客户端的 {@link BrokerClient}。
     * @param serializer 表示 Json 序列化其的 {@link ObjectSerializer}。
     * @param factory 表示 Http 客户端工厂的 {@link HttpClassicClientFactory}。
     * @param valueFetcher 表示值的获取器的 {@link ValueFetcher}。
     */
    public ToolFactoryDiscoverer(ToolFactoryRepository factoryRepository, BrokerClient brokerClient,
            @Fit(alias = "json") ObjectSerializer serializer, HttpClassicClientFactory factory,
            ValueFetcher valueFetcher) {
        this.factoryRepository = notNull(factoryRepository, "The tool factory repository cannot be null.");
        this.factoryRepository.register(new FitToolFactory(brokerClient, serializer));
        this.factoryRepository.register(new HttpToolFactory(factory, serializer, valueFetcher));
    }

    @Override
    public void onPluginStarted(Plugin plugin) {
        scanToolFactory(plugin).forEach(this.factoryRepository::register);
    }

    @Override
    public void onPluginStopping(Plugin plugin) {
        scanToolFactory(plugin).forEach(this.factoryRepository::unregister);
    }

    private static List<ToolFactory> scanToolFactory(Plugin plugin) {
        return plugin.container()
                .factories(ToolFactory.class)
                .stream()
                .map(BeanFactory::<ToolFactory>get)
                .collect(Collectors.toList());
    }
}