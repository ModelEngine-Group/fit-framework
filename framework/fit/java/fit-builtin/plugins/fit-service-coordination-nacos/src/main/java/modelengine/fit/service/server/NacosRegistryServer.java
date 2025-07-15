/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.service.server;

import static com.alibaba.nacos.api.naming.PreservedMetadataKeys.HEART_BEAT_INTERVAL;
import static com.alibaba.nacos.api.naming.PreservedMetadataKeys.HEART_BEAT_TIMEOUT;
import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notNull;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.client.naming.listener.NamingChangeEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import modelengine.fit.heartbeat.server.HeartbeatConfig;
import modelengine.fit.service.RegistryService;
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
import modelengine.fitframework.conf.runtime.CommunicationProtocol;
import modelengine.fitframework.conf.runtime.WorkerConfig;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.ObjectUtils;
import modelengine.fitframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.event.NamingEvent;

/**
 * 用于提供 Nacos 注册中心的服务。
 *
 * @author 董智豪
 * @since 2025-06-04
 */
@Component
public class NacosRegistryServer implements RegistryService {
    private static final Logger log = Logger.get(NacosRegistryServer.class);
    private static final String CLUSTER_DOMAIN_KEY = "cluster.domain";
    private static final Pattern CLUSTER_PORT_PATTERN = Pattern.compile("cluster\\.(.*?)\\.port");
    private static final String WORKER_KEY = "worker";
    private static final String APPLICATION_KEY = "application";
    private static final String FITABLE_META_KEY = "fitable-meta";

    private final HeartbeatConfig heartbeatConfig;
    private final NamingService namingService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Notify notify;
    private final NacosConfig nacosConfig;
    private final WorkerConfig worker;
    private final Map<String, com.alibaba.nacos.api.naming.listener.EventListener> serviceSubscriptions =
            new ConcurrentHashMap<>();

    public NacosRegistryServer(HeartbeatConfig heartbeatConfig, Notify notify, NacosConfig nacosConfig,
            WorkerConfig worker) throws NacosException {
        this.notify = notNull(notify, "The registry listener cannot be null.");
        this.heartbeatConfig = notNull(heartbeatConfig, "The heartbeat config cannot be null.");
        this.nacosConfig = notNull(nacosConfig, "The nacos config cannot be null.");
        this.worker = notNull(worker, "The worker config cannot be null.");
        this.namingService = NamingFactory.createNamingService(getNacosProperties());
        notBlank(this.nacosConfig.getServerAddr(), "The nacos address cannot be blank.");
    }

    private Properties getNacosProperties() {
        Properties properties = new Properties();
        properties.put("serverAddr", this.nacosConfig.getServerAddr());
        properties.put("username", ObjectUtils.nullIf(this.nacosConfig.getUsername(), StringUtils.EMPTY));
        properties.put("password", ObjectUtils.nullIf(this.nacosConfig.getPassword(), StringUtils.EMPTY));
        properties.put("namespace", ObjectUtils.nullIf(this.nacosConfig.getNamespace(), StringUtils.EMPTY));
        properties.put("accessKey", ObjectUtils.nullIf(this.nacosConfig.getAccessKey(), StringUtils.EMPTY));
        properties.put("secretKey", ObjectUtils.nullIf(this.nacosConfig.getSecretKey(), StringUtils.EMPTY));
        return properties;
    }

    /**
     * Builds a unique key in the format {@code <groupName>::<serviceName>} for {@code <serviceSubscriptions>}
     *
     * @param groupName group name
     * @param serviceName service name
     * @return a concatenated key like {@code groupName::serviceName}
     */
    private String buildServiceKey(String groupName, String serviceName) {
        return groupName + "::" + serviceName;
    }

    @Override
    @Fitable(id = "dedaa28cfb2742819a9b0271bc34f72a")
    public void registerFitables(List<FitableMeta> fitableMetas, Worker worker, Application application) {
        try {
            log.debug("Registering fitables. [fitableMetas={}, worker={}, aplication={}]",
                    fitableMetas,
                    worker.getId(),
                    application.getNameVersion());
            for (FitableMeta meta : fitableMetas) {
                FitableInfo fitable = meta.getFitable();
                String groupName = getGroupName(fitable);
                String serviceName = getServiceName(fitable);
                List<Instance> instances = createInstance(worker, application, meta);
                for (Instance instance : instances) {
                    this.namingService.registerInstance(serviceName, groupName, instance);
                }
            }
        } catch (NacosException e) {
            log.error("Failed to register fitables due to registry error.", e);
        }
    }

    private String getServiceName(FitableInfo fitable) {
        return fitable.getFitableId() + "::" + fitable.getFitableVersion();
    }

    private String getGroupName(FitableInfo fitable) {
        return fitable.getGenericableId() + "::" + fitable.getGenericableVersion();
    }

    private String getGroupName(GenericableInfo genericable) {
        return genericable.getGenericableId() + "::" + genericable.getGenericableVersion();
    }

    private List<Instance> createInstance(Worker worker, Application application, FitableMeta meta) {
        log.debug("Creating instance for worker. [worker={}, application={}, meta={}]",
                worker.getId(),
                application.getNameVersion(),
                meta);
        List<Instance> instances = new ArrayList<>();
        for (Address address : worker.getAddresses()) {
            List<Endpoint> endpoints = address.getEndpoints();
            for (Endpoint endpoint : endpoints) {
                Instance instance = new Instance();
                instance.setIp(address.getHost());
                instance.setPort(endpoint.getPort());
                HashMap<String, String> metadata = new HashMap<>();
                if (!heartbeatConfig.getIsEphemeral()) {
                    instance.setEphemeral(false);
                }
                if (heartbeatConfig.getWeight() != null) {
                    instance.setWeight(heartbeatConfig.getWeight());
                }
                if (heartbeatConfig.getHeartBeatInterval() != null) {
                    metadata.put(HEART_BEAT_INTERVAL, String.valueOf(heartbeatConfig.getHeartBeatInterval()));
                }
                if (heartbeatConfig.getHeartBeatTimeout() != null) {
                    metadata.put(HEART_BEAT_TIMEOUT, String.valueOf(heartbeatConfig.getHeartBeatTimeout()));
                }
                try {
                    metadata.put(WORKER_KEY, objectMapper.writeValueAsString(worker));
                    metadata.put(APPLICATION_KEY, objectMapper.writeValueAsString(application));
                    metadata.put(FITABLE_META_KEY, objectMapper.writeValueAsString(meta));
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize metadata for worker.", e);
                }
                instance.setMetadata(metadata);
                instances.add(instance);
            }
        }
        return instances;
    }

    @Override
    public void unregisterFitables(List<FitableInfo> fitables, String workerId) {
        log.debug("Unregistering fitables for worker. [fitables={}, workerId={}]", fitables, workerId);
        for (FitableInfo fitable : fitables) {
            String groupName = getGroupName(fitable);
            String serviceName = getServiceName(fitable);
            try {
                List<Instance> instances = namingService.selectInstances(serviceName, groupName, true);
                for (Instance instance : instances) {
                    try {
                        Worker worker = objectMapper.readValue(instance.getMetadata().get(WORKER_KEY), Worker.class);
                        if (Objects.equals(workerId, worker.getId())) {
                            namingService.deregisterInstance(serviceName, groupName, instance);
                        }
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse worker metadata for fitable.", e);
                    }
                }
            } catch (NacosException e) {
                log.error("Failed to unregister fitable due to registry error.", e);
            }
        }
    }

    @Override
    @Fitable(id = "5807f06a3a704708b264ea3c6cfbbd53")
    public List<FitableAddressInstance> queryFitables(List<FitableInfo> fitables, String workerId) {
        log.debug("Querying fitables for worker. [fitables={}, workerId={}]", fitables, workerId);
        Map<FitableInfo, FitableAddressInstance> resultMap = new HashMap<>();
        for (FitableInfo fitable : fitables) {
            try {
                List<Instance> instances = queryInstances(fitable);
                if (instances.isEmpty()) {
                    continue;
                }
                Map<Application, List<Instance>> appInstancesMap = groupInstancesByApplication(instances);
                for (Map.Entry<Application, List<Instance>> entry : appInstancesMap.entrySet()) {
                    Application application = entry.getKey();
                    List<Instance> appInstances = entry.getValue();
                    FitableMeta meta = parseFitableMeta(appInstances.get(0));
                    Set<Worker> workers = new HashSet<>();
                    for (Instance instance : appInstances) {
                        Worker worker = parseWorker(instance);
                        workers.add(worker);
                    }
                    if (application.getExtensions().containsKey(CLUSTER_DOMAIN_KEY)) {
                        replaceAddresses(workers, application);
                    }
                    FitableAddressInstance fai = resultMap.computeIfAbsent(fitable, k -> {
                        FitableAddressInstance newFai = new FitableAddressInstance();
                        newFai.setFitable(fitable);
                        newFai.setApplicationInstances(new ArrayList<>());
                        return newFai;
                    });
                    ApplicationInstance appInstance = new ApplicationInstance();
                    appInstance.setApplication(application);
                    appInstance.setFormats(meta.getFormats());
                    appInstance.setWorkers(new ArrayList<>(workers));
                    fai.getApplicationInstances().add(appInstance);
                }
            } catch (Exception e) {
                log.error("Failed to query fitables for genericableId.", e);
            }
        }
        return new ArrayList<>(resultMap.values());
    }

    private Map<Application, List<Instance>> groupInstancesByApplication(List<Instance> instances) {
        Map<Application, List<Instance>> map = new HashMap<>();
        for (Instance instance : instances) {
            Application app = parseApplication(instance);
            map.computeIfAbsent(app, k -> new ArrayList<>()).add(instance);
        }
        return map;
    }

    private List<Instance> queryInstances(FitableInfo fitable) throws NacosException {
        String groupName = getGroupName(fitable);
        String serviceName = getServiceName(fitable);
        return namingService.selectInstances(serviceName, groupName, true);
    }

    private FitableMeta parseFitableMeta(Instance instance) {
        try {
            return objectMapper.readValue(instance.getMetadata().get(FITABLE_META_KEY), FitableMeta.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse fitable meta for instance.", e);
            FitableMeta meta = new FitableMeta();
            meta.setFitable(new FitableInfo());
            return meta;
        }
    }

    private Application parseApplication(Instance instance) {
        try {
            return objectMapper.readValue(instance.getMetadata().get(APPLICATION_KEY), Application.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse application metadata for instance.", e);
            Application app = new Application();
            app.setNameVersion("unknown");
            return app;
        }
    }

    private Worker parseWorker(Instance instance) {
        try {
            return objectMapper.readValue(instance.getMetadata().get(WORKER_KEY), Worker.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse worker metadata for instance.", e);
            Worker worker = new Worker();
            Address address = new Address();
            address.setHost(instance.getIp());

            Endpoint endpoint = new Endpoint();
            endpoint.setPort(instance.getPort());
            endpoint.setProtocol(1);

            address.setEndpoints(Collections.singletonList(endpoint));
            worker.setAddresses(Collections.singletonList(address));
            return worker;
        }
    }

    private void replaceAddresses(Set<Worker> workers, Application application) {
        Address address = new Address();
        address.setHost(application.getExtensions().get(CLUSTER_DOMAIN_KEY));
        address.setEndpoints(buildEndPoints(application.getExtensions()));
        workers.forEach(w -> w.setAddresses(Collections.singletonList(address)));
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

    @Override
    @Fitable(id = "ee0a8337d3654a22a548d5d5abe1d5f3")
    public List<FitableAddressInstance> subscribeFitables(List<FitableInfo> fitables, String workerId,
            String callbackFitableId) {
        log.debug("Subscribing to fitables for worker. [fitables={}, workerId={}, callbackFitableId={}]",
                fitables,
                workerId,
                callbackFitableId);
        for (FitableInfo fitable : fitables) {
            try {
                String groupName = getGroupName(fitable);
                String serviceName = getServiceName(fitable);
                if( serviceSubscriptions.containsKey(buildServiceKey(groupName, serviceName))) {
                    log.debug("Already subscribed to service. [groupName={}, serviceName={}]", groupName, serviceName);
                    continue;
                }
                EventListener eventListener =
                        serviceSubscriptions.computeIfAbsent(buildServiceKey(groupName, serviceName), k -> {
                            EventListener listener = event -> {
                                if (event instanceof NamingEvent || event instanceof NamingChangeEvent) {
                                    onServiceChanged(fitable);
                                }
                            };
                            return listener;
                        });
                namingService.subscribe(serviceName, groupName, eventListener);
            } catch (Exception e) {
                log.error("Failed to subscribe to Nacos service.", e);
            }
        }
        return queryFitables(fitables, workerId);
    }

    @Override
    public void unsubscribeFitables(List<FitableInfo> fitables, String workerId, String callbackFitableId) {
        log.debug("Unsubscribing from fitables for worker. [fitables={}, workerId={}, callbackFitableId={}]",
                fitables,
                workerId,
                callbackFitableId);
        for (FitableInfo fitable : fitables) {
            try {
                String groupName = getGroupName(fitable);
                String serviceName = getServiceName(fitable);
                EventListener listener = serviceSubscriptions.get(buildServiceKey(groupName, serviceName));
                namingService.unsubscribe(serviceName, groupName, listener);
                serviceSubscriptions.remove(buildServiceKey(groupName, serviceName));
            } catch (Exception e) {
                log.error("Failed to unsubscribe from Nacos service.", e);
            }
        }
    }

    // 服务变更处理
    private void onServiceChanged(FitableInfo fitableInfo) {
        List<FitableAddressInstance> fitableAddressInstances =
                this.queryFitables(Collections.singletonList(fitableInfo), worker.id());
        notify.notifyFitables(fitableAddressInstances);
    }

    @Override
    @Fitable(id = "33b1f9b8f1cc49d19719a6536c96e854")
    public List<FitableMetaInstance> queryFitableMetas(List<GenericableInfo> genericables) {
        log.debug("Querying fitable metas for genericables. [genericables={}]", genericables);
        List<FitableMetaInstance> results = new ArrayList<>();
        Map<FitableMeta, Set<String>> metaEnvironments = new HashMap<>();

        for (GenericableInfo genericable : genericables) {
            String groupName = getGroupName(genericable);
            try {
                ListView<String> services = namingService.getServicesOfServer(1, Integer.MAX_VALUE, groupName);
                for (String serviceName : services.getData()) {
                    List<Instance> instances = namingService.selectInstances(serviceName, groupName, true);
                    if (instances.isEmpty()) {
                        continue;
                    }
                    FitableMeta meta = parseFitableMeta(instances.get(0));
                    for (Instance instance : instances) {
                        try {
                            Worker worker =
                                    objectMapper.readValue(instance.getMetadata().get(WORKER_KEY), Worker.class);
                            metaEnvironments.computeIfAbsent(meta, k -> new HashSet<>()).add(worker.getEnvironment());
                        } catch (JsonProcessingException e) {
                            log.error("Failed to parse worker metadata.", e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to query fitable metas.", e);
            }
        }
        for (Map.Entry<FitableMeta, Set<String>> entry : metaEnvironments.entrySet()) {
            FitableMetaInstance instance = new FitableMetaInstance();
            instance.setMeta(entry.getKey());
            instance.setEnvironments(new ArrayList<>(entry.getValue()));
            results.add(instance);
        }
        return results;
    }

}