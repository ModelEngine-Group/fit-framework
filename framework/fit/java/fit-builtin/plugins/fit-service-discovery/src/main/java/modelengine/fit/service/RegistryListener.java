/*
 * Copyright (c) 2024-2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.service;

import static modelengine.fitframework.inspection.Validation.greaterThan;
import static modelengine.fitframework.inspection.Validation.lessThanOrEquals;
import static modelengine.fitframework.inspection.Validation.notNull;
import static modelengine.fitframework.util.ObjectUtils.getIfNull;

import modelengine.fit.http.annotation.DocumentIgnored;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.RequestQuery;
import modelengine.fit.service.entity.Address;
import modelengine.fit.service.entity.Application;
import modelengine.fit.service.entity.ApplicationInstance;
import modelengine.fit.service.entity.Endpoint;
import modelengine.fit.service.entity.FitableAddressInstance;
import modelengine.fit.service.entity.FitableInfo;
import modelengine.fit.service.entity.Worker;
import modelengine.fit.service.util.FitableInstanceUtils;
import modelengine.fit.service.util.WorkerUtils;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fitable;
import modelengine.fitframework.annotation.Value;
import modelengine.fitframework.broker.FitableMetadata;
import modelengine.fitframework.broker.Genericable;
import modelengine.fitframework.broker.GenericableMetadata;
import modelengine.fitframework.broker.Target;
import modelengine.fitframework.broker.UniqueFitableId;
import modelengine.fitframework.conf.runtime.ApplicationConfig;
import modelengine.fitframework.conf.runtime.CommunicationProtocol;
import modelengine.fitframework.conf.runtime.WorkerConfig;
import modelengine.fitframework.ioc.BeanContainer;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.schedule.ExecutePolicy;
import modelengine.fitframework.schedule.Task;
import modelengine.fitframework.schedule.ThreadPoolScheduler;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 注册中心的监听器，用于接收注册中心回调。
 * <p>注册中心监听器有三种工作模式，分别为 {@link RegistryListenerMode#PULL 拉模式}、{@link RegistryListenerMode#PUSH
 * 推模式} 和 {@link RegistryListenerMode#PUSH_AND_PULL 推拉结合模式}。</p>
 * <ul>
 *     <li>拉模式：注册中心监听器会开启一个定时任务，用于定时调用 {@link RegistryService#queryFitables
 *     注册中心服务端的查询接口} 来获取本地缓存中的服务地址列表，并更新本地缓存。</li>
 *     <li>推模式：注册中心监听器会在第一次获取某个服务地址列表时，调用 {@link RegistryService#subscribeFitables
 *     注册中心服务端的订阅接口} 来获取服务地址列表，而在后续获取其服务地址列表时，会调用 {@link
 *     RegistryService#queryFitables 注册中心服务端的查询接口}。</li>
 *     <li>推拉结合模式：以上两种特性会同时开启。</li>
 * </ul>
 * <p>注册中心监听器本地会缓存必要的 {@link #fitableInstancesCache 服务地址列表}，其缓存策略如下：</p>
 * <b>添加缓存的方法</b>
 * <ul>
 *     <li>{@link Registry#subscribeFitables(List)}</li>
 *     <li>{@link Registry#getFitableTargets(UniqueFitableId)}</li>
 * </ul>
 * <b>缓存的过期时间</b>
 * <ul>
 *     <li>无（直到主动删除缓存为止）：在上述添加缓存的方法中，如果参数 {@link Genericable#type()} 不为
 *     {@code null} 时，表示该服务在当前进程中存在引用，需要<b>永久缓存</b>。</li>
 *     <li>指定缓存过期时间：相反，如果参数 {@link Genericable#type()} 为 {@code null}
 *     时，表示该服务实现为泛化调用的服务实现，其服务地址列表只需要临时缓存，因此可以超时删除缓存。</li>
 * </ul>
 * <b>删除缓存的方法</b>
 * <ul>
 *     <li>超时：开启一个定时任务，周期性地检测本地缓存中的过期时间，达到过期时间时需要调用 {@link
 *     RegistryService#unsubscribeFitables 注册中心服务端的注销接口}，同时删除本地缓存。</li>
 * </ul>
 * <p>注册中心监听器的本地服务缓存和工作模式的关系：</p>
 * <ul>
 *     <li>拉模式：定时获取地址的服务信息来源于本地缓存，更新本地缓存时，如果本地缓存没有指定的服务，则忽略。</li>
 *     <li>推模式：本地缓存中没有指定服务时，代表是第一次查询，需要订阅注册中心服务端。</li>
 * </ul>
 *
 * @author 夏斐
 * @author 季聿阶
 * @since 2020-08-19
 */
@Component
public class RegistryListener implements Registry, Notify {
    private static final Logger log = Logger.get(RegistryListener.class);
    private static final String NOTIFY_FITABLE_ID = "notify-fitables";
    private static final long INITIAL_DELAY = 10L;
    private static final int BATCH_NUM = 10;
    private static final Pattern CLUSTER_PORT_PATTERN = Pattern.compile("cluster\\.(.*?)\\.port");
    private static final String CLUSTER_CONTEXT_PATH_KEY = "cluster.context-path";
    private static final String CLUSTER_DOMAIN_KEY = "cluster.domain";

    private final BeanContainer container;
    private final LocalFitableCache localFitableCache;
    private final RegistryService registryService;

    private final Map<UniqueFitableId, Optional<FitableAddressInstance>> fitableInstancesCache =
            new ConcurrentHashMap<>();
    private final ApplicationConfig application;
    private final WorkerConfig worker;
    private final RegistryListenerMode listenerMode;

    /**
     * 创建注册中心的监听器对象
     *
     * @param container 表示 Bean 容器的 {@link BeanContainer}。
     * @param localFitableCache 表示本地服务实现信息的缓存的 {@link LocalFitableCache}。
     * @param registryService 表示注册中心的服务的 {@link RegistryService}。
     * @param application 表示应用配置的 {@link ApplicationConfig}。
     * @param worker 表示进程配置的 {@link WorkerConfig}。
     * @param listenerMode 表示监听器模式的 {@link String}。
     * @param pullPeriod 表示拉数据的周期时间的 {@code long}。
     */
    public RegistryListener(BeanContainer container, LocalFitableCache localFitableCache,
            RegistryService registryService, ApplicationConfig application, WorkerConfig worker,
            @Value("${plugin.registry.listener.mode}") String listenerMode,
            @Value("${plugin.registry.listener.pull.period}") long pullPeriod) {
        this.container = notNull(container, "The bean container cannot be null.");
        this.localFitableCache = notNull(localFitableCache, "The local fitable cache cannot be null.");
        this.registryService = notNull(registryService, "The registry service cannot be null.");

        this.application = notNull(application, "The application config cannot be null.");
        this.worker = notNull(worker, "The worker config cannot be null.");

        RegistryListenerMode mode = RegistryListenerMode.fromMode(listenerMode);
        if (mode == null) {
            log.warn("No registry listener mode, use default mode instead. "
                            + "[plugin.registry.listener.mode={}, defaultMode={}]",
                    listenerMode,
                    RegistryListenerMode.PULL.name());
            mode = RegistryListenerMode.PULL;
        }
        this.listenerMode = mode;
        log.debug("The registry listener mode is {}.", this.listenerMode.name());
        greaterThan(pullPeriod,
                0,
                "The registry listener pull period must be positive. [plugin.registry.listener.pull.period={0}]",
                pullPeriod);
        log.debug("The registry listener pull period is {} seconds.", pullPeriod);

        ThreadPoolScheduler syncListenerScheduledExecutor = ThreadPoolScheduler.custom()
                .corePoolSize(1)
                .isDaemonThread(true)
                .threadPoolName("registry-listener")
                .build();
        if (this.listenerMode.isPullEnabled()) {
            syncListenerScheduledExecutor.schedule(Task.builder()
                    .runnable(this::syncListener)
                    .policy(ExecutePolicy.fixedDelay(pullPeriod * 1000))
                    .uncaughtExceptionHandler((thread, exception) -> this.fallbackForSyncListener(exception))
                    .build(), INITIAL_DELAY * 1000);
            log.debug("Start scheduled task to sync fitable instances. [initialDelay={} seconds, period={} seconds]",
                    INITIAL_DELAY,
                    pullPeriod);
        } else {
            log.debug("Not start scheduled task to sync fitable instances, because pull mode is not enabled.");
        }
    }

    /**
     * 查看本地缓存中的服务地址信息。
     *
     * @param genericableId 表示待查找的服务唯一标识的 {@link String}。
     * @param fitableId 表示待查找的服务实现的唯一标识的 {@link String}。
     * @return 表示指定服务实现的本地缓存信息的 {@link FitableAddressInstance}。
     */
    @DocumentIgnored
    @GetMapping(path = "/fit-service-discovery/fitables")
    public FitableAddressInstance get(@RequestQuery("genericableId") String genericableId,
            @RequestQuery("fitableId") String fitableId) {
        Optional<FitableAddressInstance> fitableAddressInstance = this.fitableInstancesCache.getOrDefault(
                UniqueFitableId.create(genericableId, fitableId),
                Optional.empty());
        return fitableAddressInstance.orElse(null);
    }

    private void syncListener() {
        List<FitableInfo> cachedFitables =
                this.fitableInstancesCache.keySet().stream().map(this::toFitable).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(cachedFitables)) {
            return;
        }
        int batch = (cachedFitables.size() - 1) / BATCH_NUM + 1;
        for (int i = 0; i < batch; i++) {
            int max = Math.min((i + 1) * BATCH_NUM, cachedFitables.size());
            List<FitableInfo> subFitables = cachedFitables.subList(i * BATCH_NUM, max);
            this.query(subFitables).ifPresent(this::updateLocalCacheIfPresent);
        }
    }

    private void fallbackForSyncListener(Throwable cause) {
        log.warn("Failed to sync listener. [cause={}]", cause.getMessage());
        log.debug("Failed to sync listener.", cause);
    }

    private FitableInfo toFitable(UniqueFitableId uniqueFitableId) {
        FitableInfo fitable = new FitableInfo();
        fitable.setGenericableId(uniqueFitableId.genericableId());
        fitable.setGenericableVersion(uniqueFitableId.genericableVersion());
        fitable.setFitableId(uniqueFitableId.fitableId());
        fitable.setFitableVersion(uniqueFitableId.fitableVersion());
        return fitable;
    }

    @Override
    public boolean subscribeFitables(List<UniqueFitableId> ids) {
        List<UniqueFitableId> actualFitableKeys = getIfNull(ids, Collections::emptyList);
        List<UniqueFitableId> toSubscribeFitableKeys = actualFitableKeys.stream()
                .filter(Objects::nonNull)
                .filter(fitableKey -> !this.fitableInstancesCache.containsKey(fitableKey))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(toSubscribeFitableKeys)) {
            log.info("No fitables need to subscribe, skip.");
            return true;
        }
        Optional<List<FitableAddressInstance>> opFitableInstances = this.loadFitableInstances(toSubscribeFitableKeys);
        if (opFitableInstances.isPresent()) {
            log.info("Fitables subscribed successfully. [fitableSize={}]", toSubscribeFitableKeys.size());
        }
        return opFitableInstances.isPresent();
    }

    @Override
    public List<Target> getFitableTargets(UniqueFitableId id) {
        FitableAddressInstance fitableInstance =
                ObjectUtils.<Optional<FitableAddressInstance>>nullIf(this.fitableInstancesCache.get(id),
                        Optional.empty()).orElseGet(() -> this.loadFitableInstance(id).orElse(null));
        if (fitableInstance == null) {
            this.fitableInstancesCache.put(id, Optional.empty());
            fitableInstance = new FitableAddressInstance();
            fitableInstance.setApplicationInstances(new ArrayList<>());
            log.debug("No fitable instance in cache. "
                            + "[genericableId={}, genericableVersion={}, fitableId={}, fitableVersion={}]",
                    id.genericableId(),
                    id.genericableVersion(),
                    id.fitableId(),
                    id.fitableVersion());
        }
        this.updateInstances(fitableInstance.getApplicationInstances());
        if (this.localFitableCache.contains(id)) {
            ApplicationInstance localApp = this.getOrCreateLocalApplication(fitableInstance);
            boolean isContains = this.isApplicationContainsLocalWorker(localApp);
            if (!isContains) {
                localApp.getWorkers().add(WorkerUtils.getLocalWorker(this.container, this.worker));
            }
        }
        List<Target> targets = FitableInstanceUtils.toTargets(fitableInstance);
        for (Target target : targets) {
            String workerId = target.workerId();
            fitableInstance.getApplicationInstances()
                    .stream()
                    .filter(instance -> this.isWorkerIdInApplicationInstance(workerId, instance))
                    .findFirst()
                    .ifPresent(instance -> this.updateContextPath(target, instance.getApplication()));
        }
        return targets;
    }

    private boolean isWorkerIdInApplicationInstance(String workerId, ApplicationInstance applicationInstance) {
        return applicationInstance.getWorkers().stream().anyMatch(worker -> Objects.equals(worker.getId(), workerId));
    }

    private void updateContextPath(Target target, Application application) {
        if (!application.getExtensions().containsKey(CLUSTER_CONTEXT_PATH_KEY)) {
            return;
        }
        target.extensions().put(CLUSTER_CONTEXT_PATH_KEY, application.getExtensions().get(CLUSTER_CONTEXT_PATH_KEY));
    }

    private void updateInstances(List<ApplicationInstance> applicationInstances) {
        applicationInstances.stream()
                .filter(instance -> instance.getApplication().getExtensions().containsKey(CLUSTER_DOMAIN_KEY))
                .forEach(instance -> this.replaceAddress(instance.getWorkers(), instance.getApplication()));
    }

    private void replaceAddress(List<Worker> workers, Application application) {
        Address address = new Address();
        address.setHost(application.getExtensions().get(CLUSTER_DOMAIN_KEY));
        address.setEndpoints(this.buildEndPoints(application.getExtensions()));
        workers.forEach(worker -> worker.setAddresses(Collections.singletonList(address)));
    }

    private List<Endpoint> buildEndPoints(Map<String, String> extensions) {
        List<Endpoint> endpoints = new ArrayList<>();
        for (Map.Entry<String, String> entry : extensions.entrySet()) {
            String key = entry.getKey();
            Matcher matcher = CLUSTER_PORT_PATTERN.matcher(key);
            if (matcher.matches()) {
                String protocolName = matcher.group(1);
                CommunicationProtocol protocol = CommunicationProtocol.from(protocolName);
                Endpoint endpoint = new Endpoint();
                endpoint.setProtocol(protocol.code());
                try {
                    endpoint.setPort(Integer.valueOf(entry.getValue()));
                } catch (NumberFormatException e) {
                    log.warn("Failed to get port from extensions. [extensionKey={}]", key);
                    continue;
                }
                endpoints.add(endpoint);
            }
        }
        return endpoints;
    }

    private ApplicationInstance getOrCreateLocalApplication(FitableAddressInstance fitableInstance) {
        return fitableInstance.getApplicationInstances()
                .stream()
                .filter(Objects::nonNull)
                .filter(app -> app.getApplication() != null)
                .filter(app -> Objects.equals(app.getApplication().getName(), this.application.name()))
                .findFirst()
                .orElseGet(() -> {
                    ApplicationInstance applicationInstance = new ApplicationInstance();
                    Application localApplication = new Application();
                    localApplication.setName(this.application.name());
                    localApplication.setExtensions(this.application.visualExtensions());
                    applicationInstance.setApplication(localApplication);
                    applicationInstance.setWorkers(new ArrayList<>());
                    applicationInstance.setFormats(new ArrayList<>());
                    fitableInstance.getApplicationInstances().add(applicationInstance);
                    return applicationInstance;
                });
    }

    private boolean isApplicationContainsLocalWorker(ApplicationInstance localApp) {
        return localApp.getWorkers()
                .stream()
                .filter(Objects::nonNull)
                .filter(worker -> Objects.equals(worker.getId(), this.worker.id()))
                .anyMatch(worker -> Objects.equals(worker.getEnvironment(), this.worker.environment()));
    }

    private Optional<FitableAddressInstance> loadFitableInstance(UniqueFitableId fitableKey) {
        Optional<List<FitableAddressInstance>> opFitableInstances =
                this.loadFitableInstances(Collections.singletonList(fitableKey));
        if (!opFitableInstances.isPresent()) {
            log.debug("Failed to load fitable instance from registry. "
                            + "[genericableId={}, genericableVersion={}, fitableId={}, fitableVersion={}]",
                    fitableKey.genericableId(),
                    fitableKey.genericableVersion(),
                    fitableKey.fitableId(),
                    fitableKey.fitableVersion());
            return Optional.empty();
        }
        List<FitableAddressInstance> fitableInstances = opFitableInstances.get();
        if (CollectionUtils.isEmpty(fitableInstances)) {
            log.debug("Load fitable instance from registry, but instance is null. "
                            + "[genericableId={}, genericableVersion={}, fitableId={}, fitableVersion={}]",
                    fitableKey.genericableId(),
                    fitableKey.genericableVersion(),
                    fitableKey.fitableId(),
                    fitableKey.fitableVersion());
            return Optional.empty();
        }
        lessThanOrEquals(fitableInstances.size(),
                1,
                "Get more than 1 fitable instances. "
                        + "[genericableId={0}, genericableVersion={1}, fitableId={2}, fitableVersion={3}]",
                fitableKey.genericableId(),
                fitableKey.genericableVersion(),
                fitableKey.fitableId(),
                fitableKey.fitableVersion());
        log.debug("Load fitable instance from registry. [fitableInstances={}]", fitableInstances);
        return Optional.ofNullable(fitableInstances.get(0));
    }

    private Optional<List<FitableAddressInstance>> loadFitableInstances(List<UniqueFitableId> fitableKeys) {
        List<FitableInfo> fitables = fitableKeys.stream().map(this::toFitable).collect(Collectors.toList());
        return this.getFitableInstances(fitables).map(fitableInstances -> {
            this.updateLocalCache(fitableKeys, fitableInstances);
            return fitableInstances;
        });
    }

    private Optional<List<FitableAddressInstance>> getFitableInstances(List<FitableInfo> fitables) {
        if (this.listenerMode.isPushEnabled()) {
            return this.subscribe(fitables);
        } else {
            return this.query(fitables);
        }
    }

    private void updateLocalCache(List<UniqueFitableId> fitableKeys, List<FitableAddressInstance> fitableInstances) {
        Map<UniqueFitableId, FitableAddressInstance> fitableInstanceMap = fitableInstances.stream()
                .filter(Objects::nonNull)
                .filter(fitableInstance -> fitableInstance.getFitable() != null)
                .collect(Collectors.toMap(fitableInstance -> this.getUniqueFitableId(fitableInstance.getFitable()),
                        Function.identity()));
        fitableKeys.forEach(fitableKey -> this.fitableInstancesCache.put(fitableKey,
                Optional.ofNullable(fitableInstanceMap.get(fitableKey))));
    }

    private UniqueFitableId getUniqueFitableId(FitableInfo fitable) {
        String actualGenericableVersion = StringUtils.isBlank(fitable.getGenericableVersion())
                ? GenericableMetadata.DEFAULT_VERSION
                : fitable.getGenericableVersion();
        String actualFitableVersion = StringUtils.isBlank(fitable.getFitableVersion())
                ? FitableMetadata.DEFAULT_VERSION
                : fitable.getFitableVersion();
        return UniqueFitableId.create(fitable.getGenericableId(),
                actualGenericableVersion,
                fitable.getFitableId(),
                actualFitableVersion);
    }

    private Optional<List<FitableAddressInstance>> subscribe(List<FitableInfo> fitables) {
        if (CollectionUtils.isEmpty(fitables)) {
            return Optional.of(Collections.emptyList());
        }
        try {
            log.debug("Prepare to subscribe fitable instances. [fitables={}]", fitables);
            List<FitableAddressInstance> instances =
                    this.registryService.subscribeFitables(fitables, this.worker.id(), NOTIFY_FITABLE_ID);
            log.debug("Subscribe fitable instances successfully. [fitables={}]", fitables);
            return Optional.of(getIfNull(instances, Collections::emptyList));
        } catch (Exception e) {
            log.warn("Failed to subscribe fitable instances. [fitables={}]", fitables);
            log.debug("Failed to subscribe fitable instances.", e);
            return Optional.empty();
        }
    }

    private Optional<List<FitableAddressInstance>> query(List<FitableInfo> fitables) {
        if (CollectionUtils.isEmpty(fitables)) {
            return Optional.of(Collections.emptyList());
        }
        try {
            log.debug("Prepare to query fitable instances. [fitables={}]", fitables);
            List<FitableAddressInstance> instances = this.registryService.queryFitables(fitables, this.worker.id());
            log.debug("Query fitable instances successfully. [fitables={}]", fitables);
            return Optional.of(getIfNull(instances, Collections::emptyList));
        } catch (Exception e) {
            log.warn("Failed to query fitable instances. [fitables={}]", fitables);
            log.debug("Failed to query fitable instances.", e);
            return Optional.empty();
        }
    }

    private void unsubscribe(List<FitableInfo> fitables) {
        if (CollectionUtils.isEmpty(fitables)) {
            return;
        }
        try {
            log.debug("Prepare to unsubscribe fitables. [fitables={}]", fitables);
            this.registryService.unsubscribeFitables(fitables, this.worker.id(), NOTIFY_FITABLE_ID);
            log.debug("Unsubscribe fitables successfully. [fitables={}]", fitables);
        } catch (Exception e) {
            log.warn("Failed to unsubscribe fitables. [fitables={}]", fitables);
            log.debug("Failed to unsubscribe fitables.", e);
        }
    }

    /**
     * 接收注册中心服务端推送过来的本地监听的服务的地址信息。
     *
     * @param fitableInstances 表示本地监听的服务地址的更新列表的 {@link List}{@code <}{@link FitableAddressInstance}
     * {@code >}。
     */
    @Override
    @Fitable(NOTIFY_FITABLE_ID)
    public void notifyFitables(List<FitableAddressInstance> fitableInstances) {
        if (CollectionUtils.isEmpty(fitableInstances)) {
            log.info("Received latest fitable instances, but ignored: no data.");
            return;
        }
        this.updateLocalCacheIfPresent(fitableInstances);
    }

    private void updateLocalCacheIfPresent(List<FitableAddressInstance> fitableInstances) {
        fitableInstances.stream().filter(Objects::nonNull).forEach(this::updateLocalCacheIfPresent);
    }

    private void updateLocalCacheIfPresent(FitableAddressInstance fitableInstance) {
        FitableInfo fitable = fitableInstance.getFitable();
        if (fitable == null) {
            return;
        }
        UniqueFitableId fitableKey = this.getUniqueFitableId(fitable);
        if (this.fitableInstancesCache.containsKey(fitableKey)) {
            this.fitableInstancesCache.put(fitableKey, Optional.of(fitableInstance));
        }
    }
}
