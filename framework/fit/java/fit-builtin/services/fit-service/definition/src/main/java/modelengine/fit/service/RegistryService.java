/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.service;

import modelengine.fit.service.entity.Application;
import modelengine.fit.service.entity.FitableAddressInstance;
import modelengine.fit.service.entity.FitableInfo;
import modelengine.fit.service.entity.FitableMeta;
import modelengine.fit.service.entity.FitableMetaInstance;
import modelengine.fit.service.entity.GenericableInfo;
import modelengine.fit.service.entity.Worker;
import modelengine.fitframework.annotation.Genericable;

import java.util.List;

/**
 * 注册中心的服务。
 *
 * @author 季聿阶
 * @since 2023-05-06
 */
public interface RegistryService {
    /**
     * 向注册中心服务端注册服务实现列表。
     *
     * @param fitableMetas 表示待注册的服务实现元数据列表的 {@link List}{@code <}{@link FitableMeta}{@code >}。
     * @param worker 表示服务实现所在的进程信息的 {@link Worker}。
     * @param application 表示服务实现所在的应用信息的 {@link Application}。
     */
    @Genericable(id = "modelengine.fit.registry.registry-service.register-fitables")
    void registerFitables(List<FitableMeta> fitableMetas, Worker worker, Application application);

    /**
     * 向注册中心服务端取消注册服务实现列表。
     *
     * @param fitables 表示待取消注册的服务实现列表的 {@link List}{@code <}{@link FitableInfo}{@code >}。
     * @param workerId 表示服务实现所在的进程的唯一标识的 {@link String}。
     */
    @Genericable(id = "modelengine.fit.registry.registry-service.unregister-fitables")
    void unregisterFitables(List<FitableInfo> fitables, String workerId);

    /**
     * 向注册中心服务端查询指定服务实现的所有实例信息。
     *
     * @param fitables 表示指定服务实现列表的 {@link List}{@code <}{@link FitableInfo}{@code >}。
     * @param workerId 表示指定的进程的唯一标识的 {@link String}。
     * @return 表示指定服务实现的所有实例信息的 {@link List}{@code <}{@link FitableAddressInstance}{@code >}。
     */
    @Genericable(id = "modelengine.fit.registry.registry-service.query-fitables-addresses")
    List<FitableAddressInstance> queryFitables(List<FitableInfo> fitables, String workerId);

    /**
     * 向注册中心服务端订阅指定服务实现的实例信息。
     *
     * @param fitables 表示指定服务实现列表的 {@link List}{@code <}{@link FitableInfo}{@code >}。
     * @param workerId 表示指定的进程的唯一标识的 {@link String}。
     * @param callbackFitableId 表示订阅回调服务实现的唯一标识的 {@link String}。
     * @return 表示指定服务实现的所有实例信息的 {@link List}{@code <}{@link FitableAddressInstance}{@code >}。
     */
    @Genericable(id = "modelengine.fit.registry.registry-service.subscribe-fitables")
    List<FitableAddressInstance> subscribeFitables(List<FitableInfo> fitables, String workerId,
            String callbackFitableId);

    /**
     * 向注册中心服务端取消订阅指定服务实现的实例信息。
     *
     * @param fitables 表示指定服务实现列表的 {@link List}{@code <}{@link FitableInfo}{@code >}。
     * @param workerId 表示指定的进程的唯一标识的 {@link String}。
     * @param callbackFitableId 表示取消订阅回调服务实现的唯一标识的 {@link String}。
     */
    @Genericable(id = "modelengine.fit.registry.registry-service.unsubscribe-fitables")
    void unsubscribeFitables(List<FitableInfo> fitables, String workerId, String callbackFitableId);

    /**
     * 向注册中心服务端查询正在运行中的服务实现元数据列表。
     *
     * @param genericables 表示指定服务列表的 {@link List}{@code <}{@link GenericableInfo}{@code >}。
     * @return 表示正在运行的服务实现元数据列表的 {@link List}{@code <}{@link FitableMetaInstance}{@code >}。
     */
    @Genericable(id = "modelengine.fit.registry.registry-service.query-running-fitables")
    List<FitableMetaInstance> queryFitableMetas(List<GenericableInfo> genericables);
}
