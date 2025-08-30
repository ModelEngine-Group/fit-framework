# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
Service for providing Nacos registry center functionality.

@author 董智豪  
@since 2025-06-04
"""
import asyncio
import re
import threading
from concurrent.futures import ThreadPoolExecutor, Future
from typing import List, Dict, Set

from v2.nacos import NacosNamingService, ClientConfigBuilder, RegisterInstanceParam, ListInstanceParam, \
    DeregisterInstanceParam, SubscribeServiceParam, ClientConfig, Instance, ListServiceParam, ServiceList

from fitframework import fitable, const, value
from fitframework.api.logging import plugin_logger
from fitframework.utils.json_serialize_utils import json_serialize, json_deserialize
from fit_common_struct.entity import Worker, FitableMeta, Application, FitableAddressInstance, \
    FitableMetaInstance, ApplicationInstance, Address, Endpoint
from fit_common_struct.core import Fitable,Genericable

@value('nacos.serverAddr', default_value=None)
def _get_nacos_server_addr() -> str:
    """
    获取 Nacos 服务器地址。
    :return: Nacos 服务器地址。
    """
    pass


@value('nacos.username', default_value=None)
def _get_nacos_username() -> str:
    """
    获取 Nacos 用户名。
    :return: Nacos 用户名。
    """
    pass


@value('nacos.password', default_value=None)
def _get_nacos_password() -> str:
    """
    获取 Nacos 密码。
    :return: Nacos 密码。
    """
    pass


@value('nacos.accessKey', default_value=None)
def _get_nacos_access_key() -> str:
    """
    获取 Nacos Access Key。
    :return: Nacos Access Key。
    """
    pass


@value('nacos.secretKey', default_value=None)
def _get_nacos_secret_key() -> str:
    """
    获取 Nacos Secret Key。
    :return: Nacos Secret Key。
    """
    pass


@value('nacos.namespace', default_value="")
def _get_nacos_namespace() -> str:
    """
    获取 Nacos 命名空间。
    :return: Nacos 命名空间。
    """
    pass


@value('nacos.isEphemeral', default_value=True, converter=bool)
def _get_heartbeat_isEphemeral() -> bool:
    """
    获取 Nacos 心跳是否为临时实例。
    :return: 是否为临时实例。
    """
    pass


@value('nacos.heartBeatInterval', default_value=5000, converter=int)
def _get_heartbeat_interval() -> int:
    """
    获取 Nacos 心跳间隔时间。
    :return: 心跳间隔时间，单位为毫秒。
    """
    pass


@value('nacos.heartBeatTimeout', default_value=15000, converter=int)
def _get_heartbeat_timeout() -> int:
    """
    获取 Nacos 心跳超时时间。
    :return: 心跳超时时间，单位为毫秒。
    """
    pass


@value('nacos.weight', default_value=1.0, converter=float)
def _get_heartbeat_weight() -> float:
    """
    获取 Nacos 心跳权重。
    :return: 心跳权重。
    """
    pass


# 对async 第三方函数进行封装
async def call_create_naming_service(config: ClientConfig) -> 'NacosNamingService':
    return await NacosNamingService.create_naming_service(config)


async def call_list_instances(param: ListInstanceParam) -> List[Instance]:
    client = _async_executor.get_nacos_client()
    return await client.list_instances(param)


async def call_deregister_instance(param: DeregisterInstanceParam) -> bool:
    client = _async_executor.get_nacos_client()
    return await client.deregister_instance(param)


async def call_subscribe(param: SubscribeServiceParam) -> None:
    client = _async_executor.get_nacos_client()
    await client.subscribe(param)


async def call_unsubscribe(param: SubscribeServiceParam) -> None:
    client = _async_executor.get_nacos_client()
    await client.unsubscribe(param)


async def call_list_services(param: ListServiceParam) -> ServiceList:
    client = _async_executor.get_nacos_client()
    return await client.list_services(param)


async def call_register_instance(param: RegisterInstanceParam) -> None:
    client = _async_executor.get_nacos_client()
    await client.register_instance(param)


# 初始化 Nacos 客户端配置
config = (ClientConfigBuilder()
          .server_address(_get_nacos_server_addr())
          .namespace_id(_get_nacos_namespace() or 'local')
          .username(_get_nacos_username() or None)
          .password(_get_nacos_password() or None)
          .access_key(_get_nacos_access_key() or None)
          .secret_key(_get_nacos_secret_key() or None)
          .build())

# 不在这里直接创建客户端，而是在需要时动态创建
_nacos_client = None
_nacos_config = config

# 常量
CLUSTER_DOMAIN_KEY = "cluster.domain"
CLUSTER_PORT_PATTERN = re.compile(r"cluster\.(.*?)\.port")
WORKER_KEY = "worker"
APPLICATION_KEY = "application"
FITABLE_META_KEY = "fitable-meta"
SEPARATOR = "::"

# 全局变量
_service_subscriptions: Dict[str, any] = {}
_executor = ThreadPoolExecutor(max_workers=10)


# 线程池执行器，用于线程安全的异步调用
_executor = ThreadPoolExecutor(max_workers=10)


class AsyncExecutor:
    """专门用于处理异步操作的执行器，在后台线程中维护事件循环"""
    
    def __init__(self):
        self._loop = None
        self._thread = None
        self._started = False
        self._shutdown = False
        self._nacos_client = None
        self._init_complete = threading.Event()
    
    def start(self):
        """启动后台事件循环线程"""
        if self._started:
            return
            
        self._thread = threading.Thread(target=self._run_event_loop, daemon=True, name="NacosAsyncThread")
        self._thread.start()
        
        # 等待初始化完成
        if not self._init_complete.wait(timeout=10):  # 最多等待10秒
            raise RuntimeError("Failed to initialize async executor within timeout")
            
        self._started = True
    
    def _run_event_loop(self):
        """在后台线程中运行事件循环"""
        try:
            self._loop = asyncio.new_event_loop()
            asyncio.set_event_loop(self._loop)
            
            # 在这个事件循环中创建 Nacos 客户端
            async def init_nacos_client():
                try:
                    self._nacos_client = await NacosNamingService.create_naming_service(_nacos_config)
                    plugin_logger.info("Nacos client initialized successfully")
                except Exception as e:
                    plugin_logger.error(f"Failed to initialize Nacos client: {e}")
                    raise
                finally:
                    # 标记初始化完成
                    self._init_complete.set()
            
            self._loop.run_until_complete(init_nacos_client())
            
            # 运行事件循环直到被关闭
            self._loop.run_forever()
        except Exception as e:
            plugin_logger.error(f"Error in async executor event loop: {e}")
            self._init_complete.set()  # 即使失败也要设置，避免无限等待
        finally:
            try:
                if self._nacos_client:
                    # 清理 Nacos 客户端
                    pass
                self._loop.close()
            except:
                pass
    
    def run_coroutine(self, coro):
        """在后台事件循环中运行协程，返回结果"""
        if not self._started:
            self.start()
        
        if self._loop is None or self._nacos_client is None:
            raise RuntimeError("Async executor not properly initialized")
        
        # 创建一个Future来获取结果
        result_future = Future()
        
        async def wrapped_coro():
            try:
                result = await coro
                result_future.set_result(result)
            except Exception as e:
                result_future.set_exception(e)
        
        # 在事件循环中调度协程
        self._loop.call_soon_threadsafe(asyncio.create_task, wrapped_coro())
        
        # 等待结果
        return result_future.result(timeout=30)  # 30秒超时
    
    def get_nacos_client(self):
        """获取 Nacos 客户端实例"""
        if not self._started:
            self.start()
        return self._nacos_client
    
    def shutdown(self):
        """关闭异步执行器"""
        if self._loop and not self._loop.is_closed():
            self._loop.call_soon_threadsafe(self._loop.stop)
        self._shutdown = True


# 全局异步执行器
_async_executor = AsyncExecutor()


def _run_async_safely(coro):
    """
    线程安全地运行异步函数
    使用专门的异步执行器在后台线程中处理
    """
    try:
        return _async_executor.run_coroutine(coro)
    except Exception as e:
        plugin_logger.error(f"Error running async operation: {e}")
        raise


# 辅助函数
def build_service_key(group_name: str, service_name: str) -> str:
    """
    Builds a unique key in the format <groupName>::<serviceName> for service subscriptions.
    
    @param group_name: The group name as string.
    @param service_name: The service name as string.
    @return: A concatenated key like groupName::serviceName.
    """
    return f"{group_name}{SEPARATOR}{service_name}"


def get_service_name(fitable: Fitable) -> str:
    """获取服务名称"""
    return f"{fitable.fitableId}{SEPARATOR}{fitable.fitableVersion}"


def get_group_name_from_fitable(fitable: Fitable) -> str:
    """从FitableInfo获取组名"""
    return f"{fitable.genericableId}{SEPARATOR}{fitable.genericableVersion}"


def get_group_name_from_genericable(genericable: Genericable) -> str:
    """从GenericableInfo获取组名"""
    return f"{genericable.genericableId}{SEPARATOR}{genericable.genericableVersion}"


def create_instances(worker: Worker, application: Application, meta: FitableMeta) -> List[Dict]:
    """
    创建实例信息
    
    @param worker: Worker node object.
    @param application: Application object.
    @param meta: FitableMeta metadata object.
    @return: List of instance dictionaries.
    """
    plugin_logger.debug(f"Creating instance for worker. [worker={worker.id}, application={application.nameVersion}, meta={meta}]")
    instances = []

    for address in worker.addresses:
        for endpoint in address.endpoints:
            # 准备元数据
            metadata = build_instance_metadata(worker, application, meta)

            # 构建实例
            instance = {
                "ip": address.host,
                "port": endpoint.port,
                "weight": _get_heartbeat_weight(),
                "ephemeral": _get_heartbeat_isEphemeral(),
                "metadata": metadata
            }
            instances.append(instance)

    return instances


def build_instance_metadata(worker: Worker, application: Application, meta: FitableMeta) -> Dict[str, str]:
    """
    Build metadata for service instance, including worker, application and FitableMeta information.
    
    @param worker: The worker node object.
    @param application: The application object.
    @param meta: The FitableMeta metadata object.
    @return: A dict containing all serialized metadata.
    """
    metadata = {}
    
    # 添加心跳配置
    metadata["preserved.heart.beat.interval"] = str(_get_heartbeat_interval())
    metadata["preserved.heart.beat.timeout"] = str(_get_heartbeat_timeout())
    
    try:
        metadata[WORKER_KEY] = json_serialize(worker)
        metadata[APPLICATION_KEY] = json_serialize(application)
        metadata[FITABLE_META_KEY] = json_serialize(meta)
    except Exception as e:
        plugin_logger.error(f"Failed to serialize metadata for worker: {e}")
    
    return metadata


def parse_fitable_meta(metadata: Dict) -> FitableMeta:
    """解析FitableMeta"""
    try:
        meta_json = metadata.get(FITABLE_META_KEY)
        if meta_json:
            return json_deserialize(FitableMeta, meta_json)
    except Exception as e:
        plugin_logger.error(f"Failed to parse fitable meta for instance: {e}")
    
    # 返回默认值
    default_fitable = Fitable("unknown", "1.0", "unknown", "1.0")
    meta = FitableMeta(default_fitable, [], [])
    return meta


def parse_application(metadata: Dict) -> Application:
    """解析Application"""
    try:
        app_json = metadata.get(APPLICATION_KEY)
        if app_json:
            return json_deserialize(Application, app_json)
    except Exception as e:
        plugin_logger.error(f"Failed to parse application metadata for instance: {e}")
    
    # 返回默认值
    app = Application("unknown", "unknown")
    return app


def parse_worker(instance_or_metadata) -> Worker:
    """解析Worker"""
    try:
        # 处理不同的输入类型
        if hasattr(instance_or_metadata, 'metadata'):
            metadata = instance_or_metadata.metadata
            ip = getattr(instance_or_metadata, 'ip', 'unknown')
            port = getattr(instance_or_metadata, 'port', 0)
        else:
            metadata = instance_or_metadata
            ip = 'unknown'
            port = 0
            
        worker_json = metadata.get(WORKER_KEY)
        if worker_json:
            return json_deserialize(Worker, worker_json)
    except Exception as e:
        plugin_logger.error(f"Failed to parse worker metadata for instance: {e}")

    # 降级处理 - 创建基本worker信息
    worker = Worker([], "unknown", "", {})
    
    # 如果有IP和端口信息，创建基本地址
    if ip != 'unknown' and port != 0:
        endpoint = Endpoint(port, 1)  # 默认协议
        address = Address(ip, [endpoint])
        worker.addresses = [address]
    
    return worker


# def replace_addresses(workers: Set[Worker], application: Application) -> None:
#     """
#     Extract all workers corresponding to instances and adjust addresses based on application extension information.
#
#     @param workers: Set of workers to modify.
#     @param application: The application object.
#     """
#     if not application.extensions or CLUSTER_DOMAIN_KEY not in application.extensions:
#         return
#
#     cluster_domain = application.extensions.get(CLUSTER_DOMAIN_KEY)
#     if not cluster_domain:
#         return
#
#     # 构建端点
#     endpoints = build_endpoints(application.extensions)
#
#     # 创建统一地址
#     address = Address()
#     address.host = cluster_domain
#     address.endpoints = endpoints
#
#     # 更新所有worker的地址
#     for worker in workers:
#         worker.addresses = [address]


def build_endpoints(extensions: Dict[str, str]) -> List[Endpoint]:
    """构建端点列表"""
    endpoints = []
    
    # 协议代码映射
    protocol_code_map = {
        "rsocket": 0,
        "socket": 1,
        "http": 2,
        "grpc": 3,
        "uc": 10,
        "shareMemory": 11
    }
    
    for key, value in extensions.items():
        match = CLUSTER_PORT_PATTERN.match(key)
        if match:
            protocol_name = match.group(1).lower()
            if protocol_name in protocol_code_map:
                endpoint = Endpoint(int(value), protocol_code_map[protocol_name])
                endpoints.append(endpoint)
            else:
                plugin_logger.error(f"Unknown protocol: {protocol_name}")
    
    return endpoints


def on_service_changed(fitable_info: Fitable, worker_id: str) -> None:
    """
    Handle service change events, query and notify updates to Fitables instance information.
    
    @param fitable_info: The changed Fitables information.
    @param worker_id: The worker ID.
    """
    try:
        # 查询当前实例
        instances = query_fitable_addresses([fitable_info], worker_id)
        # 这里需要实现通知机制 - 调用notify服务
        # notify_fitables(instances)
        plugin_logger.debug(f"Service changed for fitable: {fitable_info}, instances: {len(instances)}")
    except Exception as e:
        plugin_logger.error(f"Service change handling failed: {e}")


def group_instances_by_application(instances: List[Instance]) -> Dict[Application, List[Instance]]:
    """按应用分组实例"""
    app_instances_map = {}
    for instance in instances:
        metadata = instance.metadata
        app = parse_application(metadata)
        app_instances_map.setdefault(app, []).append(instance)
    return app_instances_map


def extract_workers(app_instances: List[Instance], application: Application) -> Set[Worker]:
    """
    Extract all workers corresponding to instances and adjust addresses based on application extension information.
    
    @param app_instances: The list of application instances.
    @param application: The application object.
    @return: Set of workers.
    """
    workers = set()
    for instance in app_instances:
        worker = parse_worker(instance)
        workers.add(worker)
    
    # 如果应用有集群域名配置，替换地址
    # if application.extensions and CLUSTER_DOMAIN_KEY in application.extensions:
    #     replace_addresses(workers, application)
    
    return workers


@fitable(const.REGISTER_FIT_SERVICE_GEN_ID, const.REGISTER_FIT_SERVICE_FIT_ID)
def register_fitables(fitable_metas: List[FitableMeta], worker: Worker, application: Application) -> None:
    """
    注册中心所提供接口，注册泛服务实现的信息。

    @param fitable_metas: 泛服务实现元数据列表。
    @param worker: 当前 FIT 进程信息。
    @param application: 当前应用信息。
    """
    try:
        plugin_logger.debug(f"Registering fitables. [fitableMetas={fitable_metas}, worker={worker.id}, application={application.nameVersion}]")
        
        for meta in fitable_metas:
            fitable = meta.fitable
            group_name = get_group_name_from_fitable(fitable)
            service_name = get_service_name(fitable)
            
            instances = create_instances(worker, application, meta)
            for instance in instances:
                param = RegisterInstanceParam(
                    service_name=service_name,
                    group_name=group_name,
                    ip=instance["ip"],
                    port=instance["port"],
                    weight=instance["weight"],
                    ephemeral=instance["ephemeral"],
                    metadata=instance["metadata"]
                )
                _run_async_safely(call_register_instance(param))

        plugin_logger.info(f"Successfully registered fitables for worker {worker.id}")
    except Exception as e:
        plugin_logger.error(f"Failed to register fitables due to registry error: {e}")
        raise

@fitable(const.UNREGISTER_FIT_SERVICE_GEN_ID, const.UNREGISTER_FIT_SERVICE_FIT_ID)
def unregister_fitables(fitables: List[Fitable], worker_id: str) -> None:
    """
    向注册中心服务端取消注册服务实现列表。

    @param fitables: 表示待取消注册的服务实现列表。
    @param worker_id: 表示服务实现所在的进程的唯一标识。
    """
    plugin_logger.debug(f"Unregistering fitables for worker. [fitables={fitables}, workerId={worker_id}]")
    
    for fitable in fitables:
        unregister_single_fitable(fitable, worker_id)


def unregister_single_fitable(fitable: Fitable, worker_id: str) -> None:
    """取消注册单个Fitable"""
    group_name = get_group_name_from_fitable(fitable)
    service_name = get_service_name(fitable)
    
    try:
        # 获取服务所有实例
        param = ListInstanceParam(
            service_name=service_name,
            group_name=group_name,
            healthy_only=True
        )
        instances = _run_async_safely(call_list_instances(param))
        unregister_matching_instances(instances, worker_id, service_name, group_name)
    except Exception as e:
        plugin_logger.error(f"Failed to unregister fitable due to registry error: {e}")


def unregister_matching_instances(instances: List[Instance], worker_id: str, service_name: str, group_name: str) -> None:
    """取消注册匹配的实例"""
    for instance in instances:
        try:
            worker = parse_worker(instance)
            if worker and worker.id == worker_id:
                param = DeregisterInstanceParam(
                    service_name=service_name,
                    group_name=group_name,
                    ip=instance.ip,
                    port=instance.port
                )
                _run_async_safely(call_deregister_instance(param))
                plugin_logger.debug(f"Successfully deregistered instance {instance.ip}:{instance.port}")
        except Exception as e:
            plugin_logger.error(f"Failed to deregister instance: {e}")


@fitable(const.QUERY_FIT_SERVICE_GEN_ID, const.QUERY_FIT_SERVICE_FIT_ID)
def query_fitable_addresses(fitables: List[Fitable], worker_id: str) -> List[FitableAddressInstance]:
    """
    注册中心所提供接口，用于查询某个泛服务实现的实例信息，在拉模式下使用。

    @param fitables: 泛服务实现信息列表。
    @param worker_id: 当前 FIT 进程标识。
    @return: 所获取的实例信息。
    """
    plugin_logger.debug(f"Querying fitables for worker. [fitables={fitables}, workerId={worker_id}]")
    result_map = {}

    for fitable in fitables:
        try:
            instances = query_instances(fitable)
            if not instances:
                continue
            process_application_instances(result_map, fitable, instances)
        except Exception as e:
            plugin_logger.error(f"Failed to query fitables for genericableId: {e}")

    return list(result_map.values())


def query_instances(fitable: Fitable) -> List[Instance]:
    """查询实例"""
    group_name = get_group_name_from_fitable(fitable)
    service_name = get_service_name(fitable)
    
    param = ListInstanceParam(
        service_name=service_name,
        group_name=group_name,
        healthy_only=True
    )
    return _run_async_safely(call_list_instances(param))


def process_application_instances(result_map: Dict, fitable: Fitable, instances: List[Instance]) -> None:
    """处理应用实例"""
    app_instances_map = group_instances_by_application(instances)
    
    for app, app_instances in app_instances_map.items():
        meta = parse_fitable_meta(app_instances[0].metadata)
        workers = extract_workers(app_instances, app)
        
        fai = result_map.get(fitable)
        if fai is None:
            fai = FitableAddressInstance(applicationInstances=[], fitable=fitable)
            result_map[fitable] = fai
        
        app_instance = ApplicationInstance(workers=list(workers), application=app, formats=meta.formats if meta.formats else [])
        fai.applicationInstances.append(app_instance)


@fitable(const.SUBSCRIBE_FIT_SERVICE_GEN_ID, const.SUBSCRIBE_FIT_SERVICE_FIT_ID)
def subscribe_fit_service(fitables: List[Fitable], worker_id: str, callback_fitable_id: str) -> List[FitableAddressInstance]:
    """
    注册中心所提供接口，用于订阅某个泛服务实现的实例信息，并且也会返回查询到的实例信息，在推模式下使用。

    @param fitables: 泛服务实现信息列表。
    @param worker_id: 当前 FIT 进程标识。
    @param callback_fitable_id: 用于回调的泛服务实现的标识。
    @return: 所查询到的实例信息。
    """
    plugin_logger.debug(f"Subscribing to fitables for worker. [fitables={fitables}, workerId={worker_id}, callbackFitableId={callback_fitable_id}]")
    
    # 注册订阅
    for fitable in fitables:
        try:
            group_name = get_group_name_from_fitable(fitable)
            service_name = get_service_name(fitable)
            service_key = build_service_key(group_name, service_name)
            
            if service_key in _service_subscriptions:
                plugin_logger.debug(f"Already subscribed to service. [groupName={group_name}, serviceName={service_name}]")
                continue
            
            # 创建事件监听器
            def create_event_listener(fitable_ref: Fitable, worker_id_ref: str):
                def event_listener(event):
                    _executor.submit(on_service_changed, fitable_ref, worker_id_ref)
                return event_listener
            
            event_listener = create_event_listener(fitable, worker_id)
            _service_subscriptions[service_key] = event_listener
            
            # 注册订阅
            param = SubscribeServiceParam(
                service_name=service_name,
                group_name=group_name,
                subscribe_callback=event_listener
            )
            _run_async_safely(call_subscribe(param))
            plugin_logger.debug(f"Subscribed to service. [groupName={group_name}, serviceName={service_name}]")
            
        except Exception as e:
            plugin_logger.error(f"Failed to subscribe to Nacos service: {e}")

    return query_fitable_addresses(fitables, worker_id)


@fitable(const.UNSUBSCRIBE_FIT_SERVICE_GEN_ID, const.UNSUBSCRIBE_FIT_SERVICE_FIT_ID)
def unsubscribe_fitables(fitables: List[Fitable], worker_id: str, callback_fitable_id: str) -> None:
    """
    向注册中心服务端取消订阅指定服务实现的实例信息。

    @param fitables: 表示指定服务实现列表的List<FitableInfo>。
    @param worker_id: 表示指定的进程的唯一标识的String。
    @param callback_fitable_id: 表示取消订阅回调服务实现的唯一标识的String。
    """
    plugin_logger.debug(f"Unsubscribing from fitables for worker. [fitables={fitables}, workerId={worker_id}, callbackFitableId={callback_fitable_id}]")
    
    for fitable in fitables:
        try:
            group_name = get_group_name_from_fitable(fitable)
            service_name = get_service_name(fitable)
            service_key = build_service_key(group_name, service_name)

            if service_key in _service_subscriptions:
                listener = _service_subscriptions.pop(service_key)
                
                param = SubscribeServiceParam(
                    service_name=service_name,
                    group_name=group_name,
                    subscribe_callback=listener
                )
                _run_async_safely(call_unsubscribe(param))
                plugin_logger.debug(f"Unsubscribed from service. [groupName={group_name}, serviceName={service_name}]")
        except Exception as e:
            plugin_logger.error(f"Failed to unsubscribe from Nacos service: {e}")


@fitable(const.QUERY_FITABLE_METAS_GEN_ID, const.QUERY_FITABLE_METAS_FIT_ID)
def query_fitable_metas(genericable_infos: List[Genericable]) -> List[FitableMetaInstance]:
    """
    注册中心所提供接口，用于查询泛服务的元数据。

    @param genericable_infos: 泛服务信息列表。
    @return: 所查询到的泛服务元数据列表。
    """
    plugin_logger.debug(f"Querying fitable metas for genericables. [genericables={genericable_infos}]")
    meta_environments = {}

    for genericable in genericable_infos:
        process_genericable_services(genericable, meta_environments)

    return build_fitable_meta_instances(meta_environments)


def process_genericable_services(genericable: Genericable, meta_environments: Dict) -> None:
    """处理泛服务的服务列表"""
    group_name = get_group_name_from_genericable(genericable)
    
    try:
        # 获取分组下所有服务
        param = ListServiceParam(
            namespace_id=_get_nacos_namespace(),
            group_name=group_name,
            page_no=1,
            page_size=1000  # 假设一次获取足够多的服务
        )
        service_list = _run_async_safely(call_list_services(param))
        
        for service_name in service_list.services:
            process_service_instances(service_name, group_name, meta_environments)
    except Exception as e:
        plugin_logger.error(f"Failed to query fitable metas: {e}")


def process_service_instances(service_name: str, group_name: str, meta_environments: Dict) -> None:
    """处理服务实例"""
    try:
        # 获取服务实例
        param = ListInstanceParam(
            service_name=service_name,
            group_name=group_name,
            healthy_only=True
        )
        instances = _run_async_safely(call_list_instances(param))

        if not instances:
            return
            
        meta = parse_fitable_meta(instances[0].metadata)
        collect_environments_from_instances(instances, meta, meta_environments)
    except Exception as e:
        plugin_logger.error(f"Failed to select instances for service {service_name}: {e}")


def collect_environments_from_instances(instances: List[Instance], meta: FitableMeta, meta_environments: Dict) -> None:
    """从实例中收集环境信息"""
    for instance in instances:
        try:
            worker = parse_worker(instance)
            if worker and worker.environment:
                if meta not in meta_environments:
                    meta_environments[meta] = set()
                meta_environments[meta].add(worker.environment)
        except Exception as e:
            plugin_logger.error(f"Failed to parse worker metadata: {e}")


def build_fitable_meta_instances(meta_environments: Dict) -> List[FitableMetaInstance]:
    """构建FitableMetaInstance列表"""
    results = []
    for meta, envs in meta_environments.items():
        instance = FitableMetaInstance(meta,list(envs))
        results.append(instance)
    return results


# 模块清理函数
import atexit

def _cleanup_async_executor():
    """清理异步执行器"""
    try:
        _async_executor.shutdown()
    except:
        pass

# 注册清理函数
atexit.register(_cleanup_async_executor)


