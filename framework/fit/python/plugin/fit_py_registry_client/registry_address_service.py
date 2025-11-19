# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：注册中心地址相关功能。
"""
from typing import List

from fit_common_struct.core import Address
from fitframework import to_bool
from fitframework.api.decorators import scheduled_executor, value
from fitframework.api.logging import sys_plugin_logger
from fitframework.utils import tools

_REGISTRY_ADDRESS_CACHE = []


@value('worker-environment.env')
def _get_worker_env():
    pass


@value('registry-pull-frequency', default_value=300)
def _get_registry_pull_frequency():
    pass


@value('registry-center.server.addresses', converter=tools.to_list)
def _get_registry_server_addresses() -> list:
    pass

@value('registry-center.server.mode',default_value='DIRECT')
def _get_registry_server_mode():
    pass


@value('registry-center.server.protocol', converter=int)
def _get_registry_server_protocol():
    pass


@value('registry-center.server.formats', tools.to_list)
def _get_registry_server_formats() -> list:
    pass


@value('registry-center.server.context-path')
def _get_registry_context_path():
    pass


def get_cache_aware_registry_address() -> List[Address]:
    if not _REGISTRY_ADDRESS_CACHE:
        _flush_registry_address()
    return _REGISTRY_ADDRESS_CACHE


def _build_address(addr: str) -> Address:
    ip, port = addr.split(':')
    return Address(host=ip, port=int(port), protocol=_get_registry_server_protocol(), environment=_get_worker_env(),
                   formats=_get_registry_server_formats(), worker_id='', context_path=_get_registry_context_path())

@value('http.server.enabled', False, converter=to_bool)
def _get_http_enabled():
    pass

@value('http.server.address.port', converter=int)
def _get_http_server_port():
    pass

@value('https.server.enabled', default_value=False, converter=to_bool)
def _get_https_enabled():
    pass

@value('local_ip')
def _get_host():
    pass

@value('https.server.address.port', converter=int)
def _get_https_server_port():
    pass


def _process_address_by_mode() -> list:
    if _get_registry_server_mode() == 'PROXY':
        if _get_http_enabled():
            port = _get_http_server_port()
        elif _get_https_enabled():
            port = _get_https_server_port()
        else:
            port = ""
        return [_get_host() + port]
    elif _get_registry_server_mode() == 'DIRECT':
        return _get_registry_server_addresses()
    else:
        raise RuntimeError(f"unsupported registry server mode: {_get_registry_server_mode()}")



def _get_registry_addresses_from_configuration():
    return [_build_address(addr) for addr in _process_address_by_mode()]


@scheduled_executor(_get_registry_pull_frequency())
def _flush_registry_address():
    """
    周期性刷新注册中心地址。
    """
    sys_plugin_logger.debug('flush registry address')
    addresses = _get_registry_addresses_from_configuration()
    if addresses:
        global _REGISTRY_ADDRESS_CACHE
        _REGISTRY_ADDRESS_CACHE = addresses
        sys_plugin_logger.info(f"fetched registry address: {addresses}")
