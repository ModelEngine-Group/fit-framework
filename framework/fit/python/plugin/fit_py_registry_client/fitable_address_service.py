# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：泛服务实现地址查询相关功能。
"""
from pprint import pformat
from typing import List, Dict

from fit_common_struct.core import Address, Fitable
from fitframework import const
from fitframework.api.decorators import fitable, fit, scheduled_executor, value
from fitframework.api.logging import sys_plugin_logger
from fitframework.utils import tools
from fit_common_struct.entity import FitableAddressInstance
from .registry_address_service import get_cache_aware_registry_address

_PUSH_MODE = 'push'
_PULL_MODE = 'pull'

_CONTEXT_PATH_KEY_IN_EXTENSIONS = "http.context-path"

_FITABLE_ADDRESS_CACHE: Dict[Fitable, List[Address]] = {}


def _get_addresses_from_cache(fitable_: Fitable):
    return _FITABLE_ADDRESS_CACHE.get(fitable_)


def _update_addresses_in_cache(fitable_: Fitable, addresses: List[Address]):
    _FITABLE_ADDRESS_CACHE[fitable_] = addresses


def _get_all_fitable_in_cache() -> List[Fitable]:
    return list(_FITABLE_ADDRESS_CACHE.keys())


@value('registry-center.client.mode')
def _registry_client_mode():
    pass


@value('registry-center.client.pull-frequency', default_value=60)
def _get_pull_frequency():
    pass


@value('registry-center.service_ids', tools.to_list)
def _get_registry_server_generic_ids() -> list:
    pass


@fit(const.RUNTIME_GET_WORKER_ID_GEN_ID)
def get_runtime_worker_id() -> str:
    pass


@fit(const.SUBSCRIBE_FIT_SERVICE_GEN_ID)
def subscribe_fit_service(fitables: List[Fitable], worker_id: str, callback_fitable_id: str) \
        -> List[FitableAddressInstance]:
    """
    注册中心所提供接口，用于订阅某个泛服务实现的实例信息，并且也会返回查询到的实例信息，在推模式下使用。

    @param fitables: 泛服务实现信息列表。
    @param worker_id: 当前 FIT 进程标识。
    @param callback_fitable_id: 用于回调的泛服务实现的标识。
    @return: 所查询到的实例信息。
    """
    pass


@fit(const.QUERY_FIT_SERVICE_GEN_ID)
def query_fitable_addresses(fitables: List[Fitable], worker_id: str) -> List[FitableAddressInstance]:
    """
    注册中心所提供接口，用于查询某个泛服务实现的实例信息，在拉模式下使用。

    @param fitables: 泛服务实现信息列表。
    @param worker_id: 当前 FIT 进程标识。
    @return: 所获取的实例信息。
    """
    pass


def _convert_fitable_to_fitable_info(fitable_: Fitable) -> Fitable:
    return Fitable(fitable_.genericableId, fitable_.genericableVersion, fitable_.fitableId,
                   fitable_.fitableVersion)


def _convert_fitable_address_instance_to_addresses(fitable_inst: FitableAddressInstance) -> \
        List[Address]:
    addresses: List[Address] = []
    for application_instance in fitable_inst.applicationInstances:
        for worker in application_instance.workers:
            for addr in worker.addresses:
                for endpoint in addr.endpoints:
                    addresses.append(
                        Address(addr.host, endpoint.port, worker.id, endpoint.protocol, application_instance.formats,
                                worker.environment, worker.extensions.get(_CONTEXT_PATH_KEY_IN_EXTENSIONS, "")))
    return addresses


@fitable(const.NOTIFY_FIT_SERVICE_GEN_ID, const.NOTIFY_FIT_SERVICE_FITABLE_ID)
def notify_fitable_changes(fitable_instances: List[FitableAddressInstance]) -> None:
    """
    供注册中心调用的回调接口，注册中心通知客户端依赖的泛服务实现发生变更，客户端收到后更新本地缓存，在推模式下使用。

    @param fitable_instances: 发生变更的泛服务地址实例。
    """
    sys_plugin_logger.info(
        "receive new fitable changes %s" % {','.join([str(address) for address in fitable_instances])})
    if fitable_instances:
        for fitable_instance in fitable_instances:
            fitable_ = Fitable(fitable_instance.fitable.genericableId,
                               fitable_instance.fitable.genericableVersion,
                               fitable_instance.fitable.fitableId,
                               fitable_instance.fitable.fitableVersion)
            addresses = _convert_fitable_address_instance_to_addresses(fitable_instance)
            _update_addresses_in_cache(fitable_, addresses)


def _get_fitable_address_instances(fitable_infos: List[Fitable]) -> List[FitableAddressInstance]:
    if _registry_client_mode() == _PULL_MODE:
        return query_fitable_addresses(fitable_infos, get_runtime_worker_id())
    else:
        return subscribe_fit_service(fitable_infos, get_runtime_worker_id(), const.NOTIFY_FIT_SERVICE_FITABLE_ID)


def _get_fitable_address_from_registry_server_and_update_cache(fitable_: Fitable) -> List[Address]:
    try:
        instances = _get_fitable_address_instances([_convert_fitable_to_fitable_info(fitable_)])
    except Exception as e:
        sys_plugin_logger.error(f"subscribe_fit_service {fitable_} error: {type(e)}")
        return []
    filtered_instances = filter(lambda instance: len(instance.applicationInstances) != 0, instances)
    addresses_list = [_convert_fitable_address_instance_to_addresses(instance) for instance in filtered_instances]
    if addresses_list is None or len(addresses_list) == 0:
        addresses = []
    else:
        if len(addresses_list) > 1:
            sys_plugin_logger.warning(f"subscribe_fit_service {fitable_} get result more than one")
        addresses = addresses_list[0]
    sys_plugin_logger.debug(f"subscribe fit service {fitable_} and get: \n{pformat(addresses)}")
    _update_addresses_in_cache(fitable_, addresses)
    return addresses


@fitable(const.GET_FIT_SERVICE_ADDRESS_LIST_GEN_ID, const.GET_FIT_SERVICE_ADDRESS_LIST_FITABLE_ID)
def get_fit_service_address_list(fitable_: Fitable) -> List[Address]:
    """
    获取某个泛服务实现的实例信息，按照如下优先级获取：
    1. 如果为注册中心接口，则通过注册中心相关配置获取；
    2. 如果本地缓存中有，则从本地缓存获取；
    3. 如果本地缓存中没有，则先从注册中心获取并添加到本地缓存，之后再返回结果。

    @param fitable_: 待查询的泛服务实例信息。
    @return: 所查询到的该泛服务的实例列表。
    """
    sys_plugin_logger.debug(f"get fit service address list: gid{fitable_.genericableId}")
    if fitable_.genericableId in _get_registry_server_generic_ids():
        sys_plugin_logger.debug(f"get fit service address list: gid{fitable_.genericableId}, is registry server api")
        return get_cache_aware_registry_address()
    addresses = _get_addresses_from_cache(fitable_)
    if addresses:
        return addresses
    return _get_fitable_address_from_registry_server_and_update_cache(fitable_)


@scheduled_executor(_get_pull_frequency() if _registry_client_mode() == 'pull' else 0)
def flush_registry_address():
    sys_plugin_logger.debug('flush fitable address')
    fitables = _get_all_fitable_in_cache()
    for each_fitable in fitables:
        _get_fitable_address_from_registry_server_and_update_cache(each_fitable)
