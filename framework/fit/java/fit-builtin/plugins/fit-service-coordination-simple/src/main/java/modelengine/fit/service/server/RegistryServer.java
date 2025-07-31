/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2024 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.fit.service.server;

import static modelengine.fitframework.inspection.Validation.notNull;

import modelengine.fit.service.RegistryService;
import modelengine.fit.service.WorkerCache;
import modelengine.fit.service.entity.Address;
import modelengine.fit.service.entity.Application;
import modelengine.fit.service.entity.ApplicationInstance;
import modelengine.fit.service.entity.Endpoint;
import modelengine.fit.service.entity.FitableAddressInstance;
import modelengine.fit.service.entity.FitableInfo;
import modelengine.fit.service.entity.FitableMeta;
import modelengine.fit.service.entity.FitableMetaInstance;
import modelengine.fit.service.entity.GenericableInfo;
import modelengine.fit.service.entity.Worker;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fitable;
import modelengine.fitframework.annotation.Value;
import modelengine.fitframework.conf.runtime.CommunicationProtocol;
import modelengine.fitframework.util.CollectionUtils;
import modelengine.fitframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用于提供本地注册中心的服务。
 * <p><b>注意：该注册中心服务为内存版实现，仅提供于非生产环境测试使用。</b></p>
 *
 * @author 邬涨财
 * @author 季聿阶
 * @since 2022-04-11
 */
@Component
public class RegistryServer implements RegistryService {
    /** 表示最大允许的进程数量。 */
    public static final int MAX_WORKER_NUM = 500;
    private static final int MAX_APP_NUM = 20;
    private static final String CLUSTER_DOMAIN_KEY = "cluster.domain";
    private static final Pattern CLUSTER_PORT_PATTERN = Pattern.compile("cluster\\.(.*?)\\.port");

    private final Map<String, Worker> workers = new ConcurrentHashMap<>();
    private final Map<String, Application> applications = new ConcurrentHashMap<>();
    private final Map<String, Application> workerApplications = new ConcurrentHashMap<>();
    private final Map<String, Set<FitableMeta>> applicationMetas = new ConcurrentHashMap<>();

    private final int defaultExpireTime;
    private final WorkerCache cache;

    public RegistryServer(@Value("${default-expire-time}") int defaultExpireTime, WorkerCache cache) {
        this.defaultExpireTime = defaultExpireTime;
        this.cache = notNull(cache, "The worker cache cannot be null.");
    }

    public Map<String, Worker> getWorkers() {
        return this.workers;
    }

    public Map<String, Application> getApplications() {
        return this.applications;
    }

    public Map<String, Application> getWorkerApplications() {
        return this.workerApplications;
    }

    public Map<String, Set<FitableMeta>> getApplicationMetas() {
        return this.applicationMetas;
    }

    @Override
    @Fitable(id = "register-fitables")
    public void registerFitables(List<FitableMeta> fitableMetas, Worker worker, Application application) {
        if (!this.workers.containsKey(worker.getId()) && this.workers.size() >= MAX_WORKER_NUM) {
            throw new IllegalStateException("Too many workers.");
        }
        if (!this.applications.containsKey(application.getNameVersion()) && this.applications.size() >= MAX_APP_NUM) {
            throw new IllegalStateException("Too many applications.");
        }
        this.workers.put(worker.getId(), worker);
        this.applications.put(application.getNameVersion(), application);
        this.workerApplications.put(worker.getId(), application);
        this.applicationMetas.put(application.getNameVersion(), new HashSet<>(fitableMetas));
        Instant expireTime = Instant.now().plus(this.getActualExpire(worker), ChronoUnit.SECONDS);
        this.cache.refreshWorker(worker.getId(), expireTime);
        this.expire();
    }

    private int getActualExpire(Worker worker) {
        String expireValue = worker.getExtensions().get("expire");
        int actualExpire;
        if (expireValue == null) {
            actualExpire = this.defaultExpireTime;
        } else {
            try {
                actualExpire = Integer.parseInt(expireValue);
            } catch (NumberFormatException e) {
                actualExpire = this.defaultExpireTime;
            }
        }
        return actualExpire;
    }

    @Override
    @Fitable(id = "unregister-fitables")
    public void unregisterFitables(List<FitableInfo> fitables, String workerId) {}

    @Override
    @Fitable(id = "query-fitables-addresses")
    public List<FitableAddressInstance> queryFitables(List<FitableInfo> fitables, String workerId) {
        List<FitableAddressInstance> instances = new ArrayList<>();
        for (Map.Entry<String, Set<FitableMeta>> entry : this.applicationMetas.entrySet()) {
            Application application = this.applications.get(entry.getKey());
            if (application == null) {
                continue;
            }
            for (FitableMeta meta : entry.getValue()) {
                if (!this.isMetaInFitables(meta, fitables)) {
                    continue;
                }
                Set<Worker> filteredWorkers = this.workerApplications.entrySet()
                        .stream()
                        .filter(appEntry -> Objects.equals(appEntry.getValue(), application))
                        .map(Map.Entry::getKey)
                        .filter(curWorkerId -> !this.cache.isExpired(curWorkerId))
                        .map(this.workers::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(filteredWorkers)) {
                    continue;
                }
                if (application.getExtensions().containsKey(CLUSTER_DOMAIN_KEY)) {
                    filteredWorkers = Collections.singleton(new ArrayList<>(filteredWorkers).get(0));
                    this.replaceAddresses(filteredWorkers, application);
                }
                FitableAddressInstance instance = this.getOrCreate(meta, instances);
                List<ApplicationInstance> applicationInstances = instance.getApplicationInstances();
                ApplicationInstance applicationInstance = new ApplicationInstance();
                applicationInstance.setApplication(application);
                applicationInstance.setFormats(meta.getFormats());
                applicationInstance.setWorkers(new ArrayList<>(filteredWorkers));
                applicationInstances.add(applicationInstance);
            }
        }
        return instances;
    }

    private void replaceAddresses(Set<Worker> workers, Application application) {
        Address address = new Address();
        address.setHost(application.getExtensions().get(CLUSTER_DOMAIN_KEY));
        address.setEndpoints(this.buildEndPoints(application.getExtensions()));
        workers.forEach(worker -> worker.setAddresses(Collections.singletonList(address)));
    }

    private List<Endpoint> buildEndPoints(Map<String, String> extensions) {
        List<Endpoint> endpoints = new ArrayList<>();
        for (Map.Entry<String, String> entry : extensions.entrySet()) {
            Matcher matcher = CLUSTER_PORT_PATTERN.matcher(entry.getKey());
            if (matcher.matches()) {
                String protocolName = matcher.group(1);
                CommunicationProtocol protocol = CommunicationProtocol.valueOf(StringUtils.toUpperCase(protocolName));
                Endpoint endpoint = new Endpoint();
                endpoint.setPort(Integer.valueOf(entry.getValue()));
                endpoint.setProtocol(protocol.code());
                endpoints.add(endpoint);
            }
        }
        return endpoints;
    }

    private FitableAddressInstance getOrCreate(FitableMeta meta, List<FitableAddressInstance> instances) {
        return instances.stream()
                .filter(instance -> Objects.equals(meta.getFitable(), instance.getFitable()))
                .findFirst()
                .orElseGet(() -> {
                    FitableAddressInstance instance = new FitableAddressInstance();
                    instance.setFitable(meta.getFitable());
                    instances.add(instance);
                    return instance;
                });
    }

    private boolean isMetaInFitables(FitableMeta meta, List<FitableInfo> fitables) {
        return fitables.stream().anyMatch(fitable -> Objects.equals(meta.getFitable(), fitable));
    }

    @Override
    @Fitable(id = "subscribe-fitables")
    public List<FitableAddressInstance> subscribeFitables(List<FitableInfo> fitables, String workerId,
            String callbackFitableId) {
        return this.queryFitables(fitables, workerId);
    }

    @Override
    @Fitable(id = "unsubscribe-fitables")
    public void unsubscribeFitables(List<FitableInfo> fitables, String workerId, String callbackFitableId) {}

    @Override
    @Fitable(id = "query-running-fitables")
    public List<FitableMetaInstance> queryFitableMetas(List<GenericableInfo> genericables) {
        Map<FitableMeta, FitableMetaInstance> instances = new HashMap<>();
        for (Map.Entry<String, Set<FitableMeta>> entry : this.applicationMetas.entrySet()) {
            Application application = this.applications.get(entry.getKey());
            if (application == null) {
                continue;
            }
            for (FitableMeta meta : entry.getValue()) {
                if (!this.isMetaInGenericables(meta, genericables)) {
                    continue;
                }
                Set<String> environments = this.workerApplications.entrySet()
                        .stream()
                        .filter(appEntry -> Objects.equals(appEntry.getValue(), application))
                        .map(Map.Entry::getKey)
                        .filter(curWorkerId -> !this.cache.isExpired(curWorkerId))
                        .map(this.workers::get)
                        .filter(Objects::nonNull)
                        .map(Worker::getEnvironment)
                        .collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(environments)) {
                    continue;
                }
                FitableMetaInstance instance = instances.computeIfAbsent(meta, key -> {
                    FitableMetaInstance metaInstance = new FitableMetaInstance();
                    metaInstance.setMeta(key);
                    return metaInstance;
                });
                environments.addAll(instance.getEnvironments());
                instance.setEnvironments(new ArrayList<>(environments));
            }
        }
        return new ArrayList<>(instances.values());
    }

    private boolean isMetaInGenericables(FitableMeta meta, List<GenericableInfo> genericables) {
        for (GenericableInfo genericable : genericables) {
            FitableInfo fitable = meta.getFitable();
            if (Objects.equals(fitable.getGenericableId(), genericable.getGenericableId())
                    && Objects.equals(fitable.getGenericableVersion(), genericable.getGenericableVersion())) {
                return true;
            }
        }
        return false;
    }

    private void expire() {
        List<String> workerIds = new ArrayList<>(this.workers.keySet());
        for (String workerId : workerIds) {
            boolean expired = this.cache.isExpired(workerId);
            if (!expired) {
                continue;
            }
            this.workers.remove(workerId);
            this.workerApplications.remove(workerId);
        }
        List<String> applicationNameVersions = new ArrayList<>(this.applications.keySet());
        Set<Application> workerApplicationItems = new HashSet<>(this.workerApplications.values());
        for (String applicationNameVersion : applicationNameVersions) {
            if (this.isInApplications(applicationNameVersion, workerApplicationItems)) {
                continue;
            }
            this.applications.remove(applicationNameVersion);
            this.applicationMetas.remove(applicationNameVersion);
        }
    }

    private boolean isInApplications(String applicationNameVersion, Set<Application> applications) {
        for (Application application : applications) {
            if (Objects.equals(applicationNameVersion, application.getNameVersion())) {
                return true;
            }
        }
        return false;
    }
}
