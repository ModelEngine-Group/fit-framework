# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：服务上线相关功能。
"""
import hashlib
import sys
import traceback
from collections import defaultdict
from typing import List

from fit_common_struct.core import FitableAliasesInfo
from fitframework import fitable, const, value, fit
from fitframework.api.decorators import scheduled_executor
from fitframework.api.logging import sys_plugin_logger
from fitframework.core.network.enums import SerializingStructureEnum
from fit_common_struct.entity import Worker, FitableMeta, Address as AddressRegistry, Endpoint, \
    Application, Address
from fit_common_struct.core import Fitable


@value('worker-environment.env')
def _get_worker_env():
    pass


@value('app.name')
def _get_app_name():
    pass


@value("context-path", default_value="")
def context_path():
    pass


@value("registry-center.client.registry_fitable_frequency", default_value=300, converter=int)
def _get_registry_fitable_frequency():
    pass


@value("registry-center.client.registered-fitables-expire-interval",
       default_value=_get_registry_fitable_frequency() * 3, converter=int)
def _get_registered_fitables_expire_interval():
    pass


@fit(const.RUNTIME_GET_WORKER_ID_GEN_ID)
def get_runtime_worker_id() -> str:
    pass


@fit(const.SERVICE_DB_GET_ALL_ADDRESS_GEN_ID)
def get_server_addresses() -> List[Address]:
    pass


@fit(const.SERVICE_DB_GET_REGISTER_FORMATS_GEN_ID)
def get_registered_formats() -> List[SerializingStructureEnum]:
    pass


@fit(const.SERVICE_DB_REGISTER_ALL_FIT_SERVICE_GEN_ID)
def register_all_fit_services() -> None:
    pass


@fit(const.REGISTER_FIT_SERVICE_GEN_ID)
def register_fitables(fitable_metas: List[FitableMeta], worker: Worker, application: Application) -> None:
    """
    注册中心所提供接口，注册泛服务实现的信息。

    @param fitable_metas: 泛服务实现元数据列表。
    @param worker: 当前 FIT 进程信息。
    @param application: 当前应用信息。
    """
    pass


def _convert_fitable_aliases_info_to_fitable_meta(fitable_aliases_info: FitableAliasesInfo) -> FitableMeta:
    local_formats = [each_format.value for each_format in get_registered_formats()]
    fitable_info = Fitable(fitable_aliases_info.fitable.genericableId,
                           fitable_aliases_info.fitable.genericableVersion,
                           fitable_aliases_info.fitable.fitableId,
                           fitable_aliases_info.fitable.fitableVersion)
    return FitableMeta(fitable_info, fitable_aliases_info.aliases, local_formats)


def _fetch_all_addresses() -> List[AddressRegistry]:
    addresses_dict = defaultdict(lambda: [])
    for address in get_server_addresses():
        addresses_dict[address.host].append(Endpoint(port=address.port, protocol=address.protocol))
    result: List[AddressRegistry] = []
    for host in addresses_dict.keys():
        result.append(AddressRegistry(host, addresses_dict[host]))
    sys_plugin_logger.debug(f"_fetch_all_addresses, result: {result}")
    return result


def _fetch_fitable_meta_info(fitable_meta: FitableMeta) -> tuple:
    fitable_info: Fitable = fitable_meta.fitable
    return fitable_info.genericableId, fitable_info.fitableVersion, fitable_info.fitableId, fitable_info.fitableVersion


def _sort_fitable_metas(fitable_metas: List[FitableMeta]):
    _ = [fitable_meta.formats.sort() for fitable_meta in fitable_metas]
    fitable_metas.sort(key=_fetch_fitable_meta_info)


def _calc_fitable_meta_summary(fitable_meta: FitableMeta) -> str:
    def _calc_format_str():
        return ','.join(map(str, fitable_meta.formats))

    return ':'.join(_fetch_fitable_meta_info(fitable_meta) + (_calc_format_str(),))


def _create_application(app_name: str, fitable_metas: List[FitableMeta]) -> Application:
    _sort_fitable_metas(fitable_metas)
    _summaries = map(_calc_fitable_meta_summary, fitable_metas)
    _final_str = ";".join(_summaries)
    return Application(app_name, hashlib.sha256(_final_str.encode()).hexdigest())


@fitable(const.ONLINE_FIT_SERVICE_GEN_ID, const.ONLINE_FIT_SERVICE_FITABLE_ID)
def online_fitable_services(fitable_aliases_infos: List[FitableAliasesInfo]) -> None:
    """
    发布泛服务实现信息。

    @param fitable_aliases_infos: 泛服务实现信息。
    """
    if not fitable_aliases_infos:
        return
    fitable_metas = [_convert_fitable_aliases_info_to_fitable_meta(info) for info in fitable_aliases_infos]
    worker = Worker(addresses=_fetch_all_addresses(),
                    id=get_runtime_worker_id(),
                    environment=_get_worker_env(),
                    extensions={"expire": f"{_get_registered_fitables_expire_interval()}",
                                "http.context-path": f"{context_path()}"})
    application = _create_application(_get_app_name(), fitable_metas)
    register_fitables(fitable_metas, worker, application)


@scheduled_executor(_get_registry_fitable_frequency())
def _registry_fitable_addresses():
    """
    向注册中心周期性注册。
    """
    try:
        register_all_fit_services()
        sys_plugin_logger.debug("registry all fitable address success.")
    except:
        sys_plugin_logger.warning(f"registry all fitable address failed.")
        except_type, except_value, except_traceback = sys.exc_info()
        sys_plugin_logger.warning(f"registry all fitable address error type: {except_type}")
        sys_plugin_logger.warning(f"registry all fitable address error value: {except_value}")
        sys_plugin_logger.warning(f"registry all fitable address error trace back:\n"
                                  f"{''.join(traceback.format_tb(except_traceback))}")
