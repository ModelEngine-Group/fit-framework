/*
 * Copyright (c) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fel.tool.eco;

import static modelengine.fitframework.util.ObjectUtils.cast;

import modelengine.fel.tool.Tool;
import modelengine.fel.tool.support.AbstractTool;
import modelengine.fitframework.broker.client.BrokerClient;
import modelengine.fitframework.broker.client.filter.route.FitableIdFilter;
import modelengine.fitframework.conf.runtime.SerializationFormat;
import modelengine.fitframework.inspection.Nonnull;
import modelengine.fitframework.inspection.Validation;
import modelengine.fitframework.serialization.ObjectSerializer;
import modelengine.fitframework.util.MapUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 表示入参为键值对结构工具的抽象实现。
 *
 * @since 2024-06-24
 */
public abstract class AbstractKvTool extends AbstractTool {
    private static final String GENERICBLE = "genericableId";
    private static final String FITABLE = "fitableId";
    private static final int INVOKE_TIMEOUT = 30000;

    private final BrokerClient brokerClient;

    /**
     * 通过 Json 序列化器、工具的基本信息和工具元数据来初始化 {@link AbstractKvTool} 的新实例。
     *
     * @param brokerClient 表示服务调用的代理客户端的 {@link BrokerClient}。
     * @param serializer 表示 Json 序列化器的 {@link ObjectSerializer}。
     * @param itemInfo 表示工具的基本信息的 {@link Tool.Info}。
     * @param metadata 表示工具的元数据的 {@link Tool.Metadata}。
     */
    protected AbstractKvTool(BrokerClient brokerClient, ObjectSerializer serializer, Tool.Info itemInfo,
            Tool.Metadata metadata) {
        super(serializer, itemInfo, metadata);
        this.brokerClient = Validation.notNull(brokerClient, "The broker client cannot be null.");
    }

    @Override
    public Object execute(Object... args) {
        List<Object> argsValue = Arrays.asList(args);
        List<String> argsName = this.metadata().parameterOrder();
        Validation.equals(argsValue.size(), argsName.size(), "The size of names and values must be equal.");

        Map<String, Object> runnable = this.getToolRunnable();
        String genericableId = cast(runnable.get(GENERICBLE));
        Validation.notBlank(genericableId, "No genericable id in runnable info.");
        String fitableId = cast(runnable.get(FITABLE));
        Validation.notBlank(fitableId, "No fitable id in runnable info.");

        Map<String, Object> paramsMap = new HashMap<>();
        for (int index = 0; index < argsName.size(); index++) {
            Object value = argsValue.get(index);
            if (value == null) {
                continue;
            }
            paramsMap.put(argsName.get(index), value);
        }
        return this.brokerClient.getRouter(genericableId)
                .route(new FitableIdFilter(fitableId))
                .format(SerializationFormat.JSON)
                .timeout(INVOKE_TIMEOUT, TimeUnit.MILLISECONDS)
                .invoke(paramsMap);
    }

    private Map<String, Object> getToolRunnable() {
        String toolType = StringUtils.toUpperCase(this.type());
        Map<String, Object> runnable = cast(this.info().runnables().get(toolType));
        if (MapUtils.isEmpty(runnable)) {
            throw new IllegalStateException(StringUtils.format("No runnable info. [type={0}]", toolType));
        }
        return runnable;
    }

    /**
     * 获取工具类型。
     *
     * @return 表示工具类型的 {@link String}。
     */
    @Nonnull
    protected abstract String type();
}
