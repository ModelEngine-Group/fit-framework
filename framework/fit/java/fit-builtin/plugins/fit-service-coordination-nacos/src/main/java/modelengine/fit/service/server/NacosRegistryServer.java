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
    private static final String SEPARATOR = "::";

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
     * Builds a unique key in the format {@code <groupName>::<serviceName>} for {@code <serviceSubscriptions>}.
     *
     * @param groupName The group name as {@link String}.
     * @param serviceName The service name as {@link String}.
     * @return A concatenated key like {@code groupName::serviceName}.
     */
    private String buildServiceKey(String groupName, String serviceName) {
        return groupName + SEPARATOR + serviceName;
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
        return fitable.getFitableId() + SEPARATOR + fitable.getFitableVersion();
    }

    private String getGroupName(FitableInfo fitable) {
        return fitable.getGenericableId() + SEPARATOR + fitable.getGenericableVersion();
    }

    private String getGroupName(GenericableInfo genericable) {
        return genericable.getGenericableId() + SEPARATOR + genericable.getGenericableVersion();
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
                HashMap<String, String> metadata = buildInstanceMetadata(worker, application, meta);
                instance.setMetadata(metadata);
                setInstanceProperties(instance);
                instances.add(instance);
            }
        }
        return instances;
    }

    /**
     * 构建服务实例的元数据，包括工作节点、应用和 FitableMeta 信息。
     *
     * @param worker 工作节点对象。
     * @param application 应用对象。
     * @param meta {@link FitableMeta} 元数据对象。
     * @return 包含所有序列化元数据的 {@link Map}。
     */
    private HashMap<String, String> buildInstanceMetadata(Worker worker, Application application, FitableMeta meta) {
        HashMap<String, String> metadata = new HashMap<>();
        if (this.heartbeatConfig.getHeartBeatInterval() != null) {
            metadata.put(HEART_BEAT_INTERVAL, String.valueOf(this.heartbeatConfig.getHeartBeatInterval()));
        }
        if (this.heartbeatConfig.getHeartBeatTimeout() != null) {
            metadata.put(HEART_BEAT_TIMEOUT, String.valueOf(this.heartbeatConfig.getHeartBeatTimeout()));
        }
        try {
            metadata.put(WORKER_KEY, this.objectMapper.writeValueAsString(worker));
            metadata.put(APPLICATION_KEY, this.objectMapper.writeValueAsString(application));
            metadata.put(FITABLE_META_KEY, this.objectMapper.writeValueAsString(meta));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize metadata for worker.", e);
        }
        return metadata;
    }

    /**
     * 设置服务实例的属性，权重和临时性。
     *
     * @param instance 服务实例对象。
     */
    private void setInstanceProperties(Instance instance) {
        if (!this.heartbeatConfig.getIsEphemeral()) {
            instance.setEphemeral(false);
        }
        if (this.heartbeatConfig.getWeight() != null) {
            instance.setWeight(this.heartbeatConfig.getWeight());
        }
    }

    @Override
    public void unregisterFitables(List<FitableInfo> fitables, String workerId) {
        log.debug("Unregistering fitables for worker. [fitables={}, workerId={}]", fitables, workerId);
        for (FitableInfo fitable : fitables) {
            unregisterSingleFitable(fitable, workerId);
        }
    }

    /**
     * 注销单个 Fitable 的所有匹配实例。
     *
     * @param fitable 要注销的 {@link Fitable} 信息
     * @param workerId 工作节点 ID
     */
    private void unregisterSingleFitable(FitableInfo fitable, String workerId) {
        String groupName = getGroupName(fitable);
        String serviceName = getServiceName(fitable);
        try {
            List<Instance> instances = this.namingService.selectInstances(serviceName, groupName, true);
            unregisterMatchingInstances(instances, workerId, serviceName, groupName);
        } catch (NacosException e) {
            log.error("Failed to unregister fitable due to registry error.", e);
        }
    }

    /**
     * 注销所有匹配指定工作节点 ID 的实例。
     *
     * @param instances 实例列表
     * @param workerId 工作节点 ID
     * @param serviceName 服务名称
     * @param groupName 组名称
     */
    private void unregisterMatchingInstances(List<Instance> instances, String workerId, String serviceName,
            String groupName) {
        for (Instance instance : instances) {
            try {
                Worker worker = this.objectMapper.readValue(instance.getMetadata().get(WORKER_KEY), Worker.class);
                if (Objects.equals(workerId, worker.getId())) {
                    this.namingService.deregisterInstance(serviceName, groupName, instance);
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to parse worker metadata for fitable.", e);
            } catch (NacosException e) {
                log.error("Failed to deregister instance.", e);
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
                processApplicationInstances(resultMap, fitable, instances);
            } catch (Exception e) {
                log.error("Failed to query fitables for genericableId.", e);
            }
        }
        return new ArrayList<>(resultMap.values());
    }

    private void processApplicationInstances(Map<FitableInfo, FitableAddressInstance> resultMap, FitableInfo fitable,
            List<Instance> instances) {
        Map<Application, List<Instance>> appInstancesMap = groupInstancesByApplication(instances);
        for (Map.Entry<Application, List<Instance>> entry : appInstancesMap.entrySet()) {
            Application application = entry.getKey();
            List<Instance> appInstances = entry.getValue();
            FitableMeta meta = parseFitableMeta(appInstances.get(0));
            Set<Worker> workers = extractWorkers(appInstances, application);
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
    }

    /**
     * 提取所有实例对应的 Worker，并根据应用扩展信息调整地址。
     *
     * @param appInstances 应用实例列表。
     * @param application 应用对象。
     * @return Worker 集合。
     */
    private Set<Worker> extractWorkers(List<Instance> appInstances, Application application) {
        Set<Worker> workers = new HashSet<>();
        for (Instance instance : appInstances) {
            Worker worker = parseWorker(instance);
            workers.add(worker);
        }
        if (application.getExtensions().containsKey(CLUSTER_DOMAIN_KEY)) {
            replaceAddresses(workers, application);
        }
        return workers;
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
        return this.namingService.selectInstances(serviceName, groupName, true);
    }

    private FitableMeta parseFitableMeta(Instance instance) {
        try {
            return this.objectMapper.readValue(instance.getMetadata().get(FITABLE_META_KEY), FitableMeta.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse fitable meta for instance.", e);
            FitableMeta meta = new FitableMeta();
            meta.setFitable(new FitableInfo());
            return meta;
        }
    }

    private Application parseApplication(Instance instance) {
        try {
            return this.objectMapper.readValue(instance.getMetadata().get(APPLICATION_KEY), Application.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse application metadata for instance.", e);
            Application app = new Application();
            app.setNameVersion("unknown");
            return app;
        }
    }

    private Worker parseWorker(Instance instance) {
        try {
            return this.objectMapper.readValue(instance.getMetadata().get(WORKER_KEY), Worker.class);
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
                if (this.serviceSubscriptions.containsKey(buildServiceKey(groupName, serviceName))) {
                    log.debug("Already subscribed to service. [groupName={}, serviceName={}]", groupName, serviceName);
                    continue;
                }
                EventListener eventListener =
                        this.serviceSubscriptions.computeIfAbsent(buildServiceKey(groupName, serviceName),
                                k -> event -> {
                                    if (event instanceof NamingEvent || event instanceof NamingChangeEvent) {
                                        onServiceChanged(fitable);
                                    }
                                });
                this.namingService.subscribe(serviceName, groupName, eventListener);
            } catch (NacosException e) {
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
                EventListener listener = this.serviceSubscriptions.get(buildServiceKey(groupName, serviceName));
                this.namingService.unsubscribe(serviceName, groupName, listener);
                this.serviceSubscriptions.remove(buildServiceKey(groupName, serviceName));
            } catch (NacosException e) {
                log.error("Failed to unsubscribe from Nacos service.", e);
            }
        }
    }

    /**
     * 处理服务变更事件，查询并通知更新 Fitables 实例信息。
     *
     * @param fitableInfo 变更的 Fitables 信息。
     */
    private void onServiceChanged(FitableInfo fitableInfo) {
        List<FitableAddressInstance> fitableAddressInstances =
                this.queryFitables(Collections.singletonList(fitableInfo), this.worker.id());
        this.notify.notifyFitables(fitableAddressInstances);
    }

    @Override
    @Fitable(id = "33b1f9b8f1cc49d19719a6536c96e854")
    public List<FitableMetaInstance> queryFitableMetas(List<GenericableInfo> genericables) {
        log.debug("Querying fitable metas for genericables. [genericables={}]", genericables);
        Map<FitableMeta, Set<String>> metaEnvironments = new HashMap<>();

        for (GenericableInfo genericable : genericables) {
            processGenericableServices(genericable, metaEnvironments);
        }

        return buildFitableMetaInstances(metaEnvironments);
    }

    private void processGenericableServices(GenericableInfo genericable,
            Map<FitableMeta, Set<String>> metaEnvironments) {
        String groupName = getGroupName(genericable);
        try {
            ListView<String> services = this.namingService.getServicesOfServer(1, Integer.MAX_VALUE, groupName);
            for (String serviceName : services.getData()) {
                processServiceInstances(serviceName, groupName, metaEnvironments);
            }
        } catch (NacosException e) {
            log.error("Failed to query fitable metas.", e);
        }
    }

    private void processServiceInstances(String serviceName, String groupName,
            Map<FitableMeta, Set<String>> metaEnvironments) {
        try {
            List<Instance> instances = this.namingService.selectInstances(serviceName, groupName, true);
            if (instances.isEmpty()) {
                return;
            }
            FitableMeta meta = parseFitableMeta(instances.get(0));
            collectEnvironmentsFromInstances(instances, meta, metaEnvironments);
        } catch (NacosException e) {
            log.error("Failed to select instances for service: " + serviceName, e);
        }
    }

    private void collectEnvironmentsFromInstances(List<Instance> instances, FitableMeta meta,
            Map<FitableMeta, Set<String>> metaEnvironments) {
        for (Instance instance : instances) {
            try {
                Worker worker = this.objectMapper.readValue(instance.getMetadata().get(WORKER_KEY), Worker.class);
                metaEnvironments.computeIfAbsent(meta, k -> new HashSet<>()).add(worker.getEnvironment());
            } catch (JsonProcessingException e) {
                log.error("Failed to parse worker metadata.", e);
            }
        }
    }

    private List<FitableMetaInstance> buildFitableMetaInstances(Map<FitableMeta, Set<String>> metaEnvironments) {
        List<FitableMetaInstance> results = new ArrayList<>();
        for (Map.Entry<FitableMeta, Set<String>> entry : metaEnvironments.entrySet()) {
            FitableMetaInstance instance = new FitableMetaInstance();
            instance.setMeta(entry.getKey());
            instance.setEnvironments(new ArrayList<>(entry.getValue()));
            results.add(instance);
        }
        return results;
    }
}
