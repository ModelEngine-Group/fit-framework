/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.service.server;

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
import modelengine.fit.server.FitServer;
import modelengine.fit.service.RegistryService;
import modelengine.fit.service.entity.*;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Fitable;
import modelengine.fitframework.conf.runtime.CommunicationProtocol;
import modelengine.fitframework.conf.runtime.WorkerConfig;
import modelengine.fitframework.log.Logger;
import modelengine.fitframework.util.StringUtils;

import javax.naming.event.NamingEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static modelengine.fitframework.inspection.Validation.notBlank;
import static modelengine.fitframework.inspection.Validation.notNull;

/**
 * 用于提供Nacos注册中心的服务。
 *
 * @author 董智豪
 * @since 2025-06-04
 */
@Component
public class RegistryServer implements RegistryService {
    private static final Logger log = Logger.get(RegistryServer.class);

    private static final String CLUSTER_DOMAIN_KEY = "cluster.domain";
    private static final Pattern CLUSTER_PORT_PATTERN = Pattern.compile("cluster\\.(.*?)\\.port");
    private static final String WORKER_KEY = "worker";
    private static final String APPLICATION_KEY = "application";
    private static final String FITABLE_META_KEY = "fitable-meta";
    private HeartbeatConfig heartbeatConfig;


    private final NamingService namingService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FitServer fitServer;
    private final Notify notify;
    private final NacosConfig nacosConfig;
    private final WorkerConfig worker;

    private final Map<String, com.alibaba.nacos.api.naming.listener.EventListener> serviceSubscriptions = new ConcurrentHashMap<>();

    public RegistryServer(
            HeartbeatConfig heartbeatConfig, FitServer fitServer, Notify notify, NacosConfig nacosConfig, WorkerConfig worker) throws NacosException {
        notNull(notify, "The registry listener cannot be null.");
        notNull(fitServer, "The fit server cannot be null.");
        notNull(heartbeatConfig, "The heartbeat config cannot be null.");
        notNull(nacosConfig, "The nacos config cannot be null.");
        notBlank(nacosConfig.getServerAddr(), "The nacos address cannot be blank.");
        notNull(worker, "The worker config cannot be null.");
        this.notify = notify;
        this.nacosConfig = nacosConfig;
        this.namingService = NamingFactory.createNamingService(getNacosProperties(nacosConfig));
        this.heartbeatConfig = heartbeatConfig;
        this.fitServer = fitServer;
        this.worker = worker;
    }

    private Properties getNacosProperties(NacosConfig nacosConfig) {
        Properties properties = new Properties();
        properties.put("serverAddr", nacosConfig.getServerAddr());
        properties.put("username", Objects.toString(nacosConfig.getUsername(), ""));
        properties.put("password", Objects.toString(nacosConfig.getPassword(), ""));
        properties.put("namespace", Objects.toString(nacosConfig.getNamespace(), ""));
        properties.put("accessKey", Objects.toString(nacosConfig.getAccessKey(), ""));
        properties.put("secretKey", Objects.toString(nacosConfig.getSecretKey(), ""));
        return properties;
    }

    // 构建服务键: groupName::serviceName
    private String buildServiceKey(String groupName, String serviceName) {
        return groupName + "::" + serviceName;
    }

    @Override
    @Fitable(id = "dedaa28cfb2742819a9b0271bc34f72a")
    public void registerFitables(List<FitableMeta> fitableMetas, Worker worker, Application application) {
        try {
            for (FitableMeta meta : fitableMetas) {
                FitableInfo fitable = meta.getFitable();
                String groupName = fitable.getGenericableId() + fitable.getGenericableVersion();
                String serviceName = fitable.getFitableId() + fitable.getFitableVersion();

                List<Instance> instances = createInstance(worker, application, meta);
                for (Instance instance : instances) {
                    namingService.registerInstance(serviceName, groupName, instance);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Nacos registration failed", e);
        }
    }

    private List<Instance> createInstance(Worker worker, Application application, FitableMeta meta) {
        List<Instance> instances = new ArrayList<>();

        for (Address address : worker.getAddresses()) {
            List<Endpoint> endpoints = address.getEndpoints();
            for (Endpoint endpoint : endpoints) {
                Instance instance = new Instance();
                instance.setIp(address.getHost());
                instance.setPort(endpoint.getPort());
                HashMap<String, String> metadata = new HashMap<>();
                if (!heartbeatConfig.getIsEphemeral()) {
                    // 设置为非临时实例,默认ehphemeral为true
                    instance.setEphemeral(false);
                }
                if (heartbeatConfig.getWeight() != null) {
                    instance.setWeight(heartbeatConfig.getWeight());
                }
                if (heartbeatConfig.getHeartBeatInterval() != null) {
                    instance.setMetadata(Collections.singletonMap("preserved.heart.beat.interval", String.valueOf(heartbeatConfig.getHeartBeatInterval())));
                }
                if (heartbeatConfig.getHeartBeatTimeout() != null) {
                    instance.setMetadata(Collections.singletonMap("preserved.heart.beat.timeout", String.valueOf(heartbeatConfig.getHeartBeatTimeout())));
                }
                try {
                    metadata.put(WORKER_KEY, objectMapper.writeValueAsString(worker));
                    metadata.put(APPLICATION_KEY, objectMapper.writeValueAsString(application));
                    metadata.put(FITABLE_META_KEY, objectMapper.writeValueAsString(meta));
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize metadata for worker: {}, application: {}, fitableMeta: {}, error: {}",
                            worker, application, meta, e);
                }
                instance.setMetadata(metadata);
                instances.add(instance);
            }
        }
        return instances;
    }

    @Override
    public void unregisterFitables(List<FitableInfo> fitables, String workerId) {
        for (FitableInfo fitable : fitables) {
            String groupName = fitable.getGenericableId() + fitable.getGenericableVersion();
            String serviceName = fitable.getFitableId() + fitable.getFitableVersion();

            try {
                List<Instance> instances = namingService.selectInstances(serviceName, groupName, true);
                for (Instance instance : instances) {
                    try {
                        Worker worker = objectMapper.readValue(
                                instance.getMetadata().get(WORKER_KEY), Worker.class);

                        if (worker != null && workerId.equals(worker.getId())) {
                            namingService.deregisterInstance(serviceName, groupName, instance);
                        }
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse worker metadata for fitable: {}, error: {}", fitable.getFitableId(), e);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to unregister fitable: {}, error: {}", fitable.getFitableId(), e);
            }
        }
    }

    @Override
    @Fitable(id = "5807f06a3a704708b264ea3c6cfbbd53")
    public List<FitableAddressInstance> queryFitables(List<FitableInfo> fitables, String workerId) {
        Map<FitableInfo, FitableAddressInstance> resultMap = new HashMap<>();

        for (FitableInfo fitable : fitables) {
            String groupName = fitable.getGenericableId() + fitable.getGenericableVersion();
            String serviceName = fitable.getFitableId() + fitable.getFitableVersion();

            try {
                // 1. 查询所有提供此方法的实例
                List<Instance> instances = namingService.selectInstances(serviceName, groupName, true);
                if (instances.isEmpty()) continue;

                // 2. 按应用分组实例
                Map<Application, List<Instance>> appInstancesMap = new HashMap<>();
                for (Instance instance : instances) {
                    Application application = parseApplication(instance);
                    appInstancesMap.computeIfAbsent(application, k -> new ArrayList<>()).add(instance);
                }

                // 3. 为每个应用创建 ApplicationInstance
                for (Map.Entry<Application, List<Instance>> entry : appInstancesMap.entrySet()) {
                    Application application = entry.getKey();
                    List<Instance> appInstances = entry.getValue();

                    // 4. 解析方法元数据（取第一个实例的元数据）
                    FitableMeta meta = parseFitableMeta(appInstances.get(0));

                    // 5. 收集该应用的所有Worker
                    Set<Worker> workers = new HashSet<>();
                    for (Instance instance : appInstances) {
                        Worker worker = parseWorker(instance);
                        workers.add(worker);
                    }

                    // 6. 处理集群域名逻辑
                    if (application.getExtensions().containsKey(CLUSTER_DOMAIN_KEY)) {
                        replaceAddresses(workers, application);
                    }

                    // 7. 构建或获取 FitableAddressInstance
                    FitableAddressInstance fai = resultMap.computeIfAbsent(
                            fitable,
                            k -> {
                                FitableAddressInstance newFai = new FitableAddressInstance();
                                newFai.setFitable(fitable);
                                newFai.setApplicationInstances(new ArrayList<>());
                                return newFai;
                            }
                    );

                    // 8. 创建 ApplicationInstance
                    ApplicationInstance appInstance = new ApplicationInstance();
                    appInstance.setApplication(application);
                    appInstance.setFormats(meta.getFormats());
                    appInstance.setWorkers(new ArrayList<>(workers));

                    // 9. 添加到结果
                    fai.getApplicationInstances().add(appInstance);
                }

            } catch (Exception e) {
                log.error("Failed to query fitables for genericableId: {}, fitableId: {}, error: {}",
                        fitable.getGenericableId(), fitable.getFitableId(), e);
            }
        }

        return new ArrayList<>(resultMap.values());
    }

    private FitableMeta parseFitableMeta(Instance instance) {
        try {
            return objectMapper.readValue(
                    instance.getMetadata().get(FITABLE_META_KEY), FitableMeta.class);
        } catch (JsonProcessingException e) {
            FitableMeta meta = new FitableMeta();
            meta.setFitable(new FitableInfo());
            return meta;
        }
    }

    private Application parseApplication(Instance instance) {
        try {
            return objectMapper.readValue(
                    instance.getMetadata().get(APPLICATION_KEY), Application.class);
        } catch (JsonProcessingException e) {
            Application app = new Application();
            app.setNameVersion("unknown");
            return app;
        }
    }

    private Worker parseWorker(Instance instance) {
        try {
            return objectMapper.readValue(
                    instance.getMetadata().get(WORKER_KEY), Worker.class);
        } catch (JsonProcessingException e) {
            // 降级处理
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
    public List<FitableAddressInstance> subscribeFitables(List<FitableInfo> fitables, String workerId, String callbackFitableId) {
        // 记录订阅关系
        for (FitableInfo fitable : fitables) {
            // 添加Nacos监听器
            try {
                String groupName = fitable.getGenericableId() + fitable.getGenericableVersion();
                String serviceName = fitable.getFitableId() + fitable.getFitableVersion();


                EventListener eventListener = serviceSubscriptions.computeIfAbsent(buildServiceKey(groupName, serviceName), k -> {
                    EventListener listener = event -> {
                        if (event instanceof NamingEvent || event instanceof NamingChangeEvent) {
                            onServiceChanged(fitable);
                        }
                    };
                    return listener;
                });
                namingService.subscribe(serviceName, groupName, eventListener);

            } catch (Exception e) {
                log.error("Failed to subscribe to Nacos service, fitableId:{},error:{}", fitable.getFitableId(), e);
            }
        }
        // 返回当前实例信息
        return queryFitables(fitables, workerId);
    }

    @Override
    public void unsubscribeFitables(List<FitableInfo> fitables, String workerId, String callbackFitableId) {
        // 记录订阅关系
        for (FitableInfo fitable : fitables) {
            // 添加Nacos监听器
            try {
                String groupName = fitable.getGenericableId() + fitable.getGenericableVersion();
                String serviceName = fitable.getFitableId() + fitable.getFitableVersion();
                EventListener listener = serviceSubscriptions.get(buildServiceKey(groupName, serviceName));
                namingService.unsubscribe(serviceName, groupName, listener);
                serviceSubscriptions.remove(buildServiceKey(groupName, serviceName));
            } catch (Exception e) {
                // 处理异常
                log.error("Failed to unsubscribe from Nacos service, fitableId:{}, error:{}", fitable.getFitableId(), e);
            }
        }
    }

    // 服务变更处理
    private void onServiceChanged(FitableInfo fitableInfo) {
        List<FitableAddressInstance> fitableAddressInstances = this.queryFitables(Collections.singletonList(fitableInfo), worker.id());
        // 通知注册中心监听器
        notify.notifyFitables(fitableAddressInstances);
    }

    @Override
    @Fitable(id = "33b1f9b8f1cc49d19719a6536c96e854")
    public List<FitableMetaInstance> queryFitableMetas(List<GenericableInfo> genericables) {
        List<FitableMetaInstance> results = new ArrayList<>();
        Map<FitableMeta, Set<String>> metaEnvironments = new HashMap<>();

        for (GenericableInfo genericable : genericables) {
            String groupName = genericable.getGenericableId() + genericable.getGenericableVersion();

            try {
                // 获取组内所有服务
                ListView<String> services = namingService.getServicesOfServer(1, Integer.MAX_VALUE, groupName);

                for (String serviceName : services.getData()) {
                    List<Instance> instances = namingService.selectInstances(serviceName, groupName, true);
                    if (instances.isEmpty()) continue;

                    // 解析方法元数据
                    FitableMeta meta = parseFitableMeta(instances.get(0));

                    // 收集环境信息
                    for (Instance instance : instances) {
                        try {
                            Worker worker = objectMapper.readValue(
                                    instance.getMetadata().get(WORKER_KEY), Worker.class);

                            metaEnvironments.computeIfAbsent(meta, k -> new HashSet<>())
                                    .add(worker.getEnvironment());
                        } catch (JsonProcessingException e) {
                            log.error("Failed to parse worker metadata for genericableId: {}, fitableId: {}, error: {}",
                                    genericable.getGenericableId(), meta.getFitable().getFitableId(), e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to query fitable metas for genericableId: {}, error: {}",
                        genericable.getGenericableId(), e);
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
