# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：基于fit元数据的服务调用代理。
本地fit.yml示例：
genericables:
  'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
    name: 'ABC'
    load_balance: 'balance fitable_id'
    tags:
      'trustIgnored,localOnly'
    route:
      default: 'default fitable_id for the genericable'
      dynamic: 'route fitable_id'
      rule:
        id: 'rule <id>'
        type: 'T,P'
    params:
      '<name>':
        index: 0,1,2...
        type: 'int32/string/map<string, int32>...'
        taggers:
          'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
            tag: <tag name>
            rule_id: <rule_id>
          'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
            tag: <tag name>
            rule_id: <rule_id>
    return_type: 'int32/string/map<string, int32>...
    trust:
      validate: 'validate fitable_id'
      before: 'before fitable_id'
      after: 'after fitable_id'
      error: 'error fitable_id'
    fitables:
      'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
        aliases:
          - 'fitable alias'
        degradation: 'degradation fitable_id'

rule.type
P: input parameter
T: tag
"""
from typing import Any, List, Optional, Dict, Callable

from fit_common_struct.core import Address, Fitable, Genericable
from fitframework import const
from fitframework.api.decorators import fit, private_fit, local_context
from fitframework.api.logging import fit_logger
from fitframework.core.broker.broker import BrokerTemplate
from fitframework.core.broker.broker_utils import FitableIdentifier, IdType, get_priority_index, Singleton, \
    GenericableTagEnum
from fitframework.core.broker.select_broker import BrokerBuilder
from fitframework.core.broker.trace import print_trace
from fitframework.core.exception.fit_exception import FitException, InternalErrorCode
from fitframework.core.repo import service_repo
from fitframework.utils.json_serialize_utils import json_serialize
from fitframework.utils.serialize_utils import get_parameter_types, validate_arguments
from fitframework.utils.tools import to_list

_FIT_META_GEN_PARAMS_TAGGERS_KEY = 'taggers'
_FIT_META_GEN_PARAMS_TAGS_VALUE_SEP = ','
_FIT_META_GEN_RULE_ID_KEY = 'id'
_FIT_META_GEN_RULE_TYPE_KEY = 'type'
_FIT_META_GEN_RULE_TYPE_PARAM_VALUE = 'P'
_FIT_META_GEN_RULE_TYPE_TAG_VALUE = 'T'
_FIT_META_GEN_RULE_TYPE_CONTEXT_VALUE = 'C'


@local_context('local_ip')
def local_ip():
    pass


@local_context(key='worker-environment.env')
def worker_env():
    pass


@local_context(key='context-path')
def context_path():
    pass


@local_context(key='worker-environment.default-envs')
def default_envs():
    pass


@local_context(key='worker-environment.reserved-env')
def reserved_env():
    pass


@fit(const.SERVICE_DB_GET_FIT_FFP_ALIAS_GEN_ID)
def get_fit_ffp_fitable_id(generic_id: str, func_name: str) -> str:
    pass


@fit(const.SERVICE_DB_GET_GENERICABLE_TAGS_GEN_ID)
def get_genericable_tags(generic_id: str) -> None:
    pass


@fit(const.GET_FIT_SERVICE_ADDRESS_LIST_GEN_ID, 'get_service_address_list_python')
def get_fit_service_address_list(fitable: Fitable) -> List[Address]:
    pass


@fit(const.SERVICE_DB_GET_FITABLE_UUID_BY_ALIAS_GEN_ID)
def get_fitable_id_by_alias(generic_id: str, alias: str) -> Optional[str]:
    pass


@fit(const.SERVICE_DB_GET_REGISTER_CLIENTS_GEN_ID)
def get_registered_clients() -> List[int]:
    pass


@fit(const.GET_ALL_FITABLES_FROM_CONFIG_GEN_ID)
def get_all_fitables_from_config(generic_id: str) -> List[str]:
    pass


@fit(const.GET_FITABLES_OF_GENERICABLE_GEN_ID)
def get_all_fitables_from_registry(genericable: Genericable) -> List[str]:
    pass


@fit("fd3244e74b044cf0af5a43c045601160", alias="py_impl")
def get_all_route_context() -> Dict[str, str]:
    pass


@fit(const.RULE_ENGINE_EXECUTE_GEN_ID)
def routing_rule_execute(env: str, rule_id: str, json_params: str) -> str:
    """
    执行路由规则
    :param env: 当前环境
    :param rule_id: 规则Id
    :param json_params:
    {
        'P': {
           'arg0': arg_0_value,
           'arg1': arg_1_value,
           ...
        }
        'T': {
           arg0: ['Tag1', 'tag2', ...]
           arg1: ['Tag3', 'tag4', ...]
        }
        'C': {
        key: value
    }
    :return: 返回fitable Id
    """
    pass


@fit(const.RESOLVE_TAGS_GEN_ID)
def resolve_tags(env: str, tagger_ids: List[str], param_value_json: str) -> List[str]:
    """
    解析接口输入参数值关联的tags
    :param env: 当前环境
    :param tagger_ids: 当前参数所有关联的tagger Ids
    :param param_value_json: Json参数值
    {
        'P': {
            'arg0': arg0_value
        }
    }
    :return: 和当前参数值关联的tags
    """
    pass


@fit(const.SERVICE_DB_GET_GENERICABLE_RULE_GEN_ID)
def get_genericable_rule(generic_id: str) -> Dict[str, str]:
    """
    :param generic_id: 泛服务的id
    :return: 泛服务的规则路由信息，含义Dict[`name/type`, 规则名称/规则类型]
    """
    pass


@fit(const.RECYCLE_GEN_ID)
def recycle(generic_id: str, fitable_id: str) -> None:
    pass


@private_fit
def get_genericable_params(generic_id: str) -> Dict[str, Dict[str, str]]:
    """
    :param generic_id: 泛服务的id
    :return:  泛服务的参数信息，含义：Dict[参数名, Dict[`type/tags`, 参数类型/参数标签]]
    """
    pass


@private_fit
def get_degradation(gen_id: str, fitable_id: str) -> Optional[str]:
    pass


@local_context("worker.id")
def _worker_id() -> str:
    pass


class ConfigureBasedBroker(BrokerTemplate, metaclass=Singleton):
    """ 一般fitFFP调用代理。基于Fit元数据 """

    @staticmethod
    def fiter_fitables_by_route_filter(generic_id: str, fitable_ids: List[str],
                                       route_filter: Callable[[str], bool]):
        try:
            fitable_ids = [fitable_id for fitable_id in fitable_ids if route_filter(fitable_id)]
        except Exception as exception:
            fit_logger.error(f'routing filter error, msg: {exception}')
            raise FitException(InternalErrorCode.ADDRESS_FILTER_ERROR.value,
                               f"routing filtering for the generic_id: {generic_id} fail") from None
        return fitable_ids

    def aop_before_trust(self, generic_id, fit_ref, *args):
        validate_arguments(fit_ref, *args)

    def on_validate(self, generic_id, fit_ref, *args):
        fitable_id = get_fit_ffp_fitable_id(generic_id, 'trust.validate')
        if fitable_id:
            fit_invoke_info = (generic_id, fitable_id, fit_ref)
            _ffp_invoke(fit_invoke_info, True, None, '_validation', *args)

    def on_before(self, generic_id, fit_ref, *args):
        fitable_id = get_fit_ffp_fitable_id(generic_id, 'trust.before')
        if fitable_id:
            fit_invoke_info = (generic_id, fitable_id, fit_ref)
            _ffp_invoke(fit_invoke_info, True, None, '_before', *args)

    def on_after(self, generic_id, fit_ref, *args):
        fitable_id = get_fit_ffp_fitable_id(generic_id, 'trust.after')
        if fitable_id:
            fit_invoke_info = (generic_id, fitable_id, fit_ref)
            _ffp_invoke(fit_invoke_info, True, None, '_after', *args)

    def on_error(self, generic_id, fit_ref, *args):
        fitable_id = get_fit_ffp_fitable_id(generic_id, 'trust.error')
        if fitable_id:
            fit_invoke_info = (generic_id, fitable_id, fit_ref)
            _ffp_invoke(fit_invoke_info, True, None, '_error', *args)

    def routing(self, generic_id, fitable_identifier, fit_ref, *args, route_filter=None):
        if route_filter:
            fitable_ids = self.custom_routing(generic_id, route_filter)
        else:
            fitable_ids = self.default_routing(generic_id, fitable_identifier, fit_ref, *args)

        return fitable_ids

    def default_routing(self, generic_id, fitable_identifier, fit_ref, *args) -> List[str]:
        if _is_invoke_all(generic_id):
            return get_all_fitables_from_config(generic_id)

        if fitable_identifier is not None:
            return to_list(_get_designated_fitable_id(generic_id, fitable_identifier, fit_ref, *args))
        fitable_id = _routing_by_rule(generic_id, fit_ref, *args)
        if fitable_id is not None:
            return [fitable_id]
        fitable_id = _routing_by_route_fitable(generic_id, fit_ref, *args)
        if fitable_id is not None:
            return [fitable_id]
        fitable_id = get_fit_ffp_fitable_id(generic_id, 'route.default')  # default fitable
        if fitable_id is not None:
            return [fitable_id]
        # 只有在无法通过配置文件获取到默认 fitable 时才从注册中心获取
        fitable_ids = get_all_fitables_from_registry(Genericable(generic_id, const.FIXED_GENERICABLE_VERSION))
        if len(fitable_ids) == 0:
            raise FitException(InternalErrorCode.AVAILABLE_FITABLE_NOT_FOUND_BY_DEFAULT_OR_REGISTRY,
                               f"default fitable id not found, generic id: {generic_id}", degradable=False)
        return fitable_ids

    def load_balancing(self, generic_id, fitable_id, address_filter=None):
        """ 不支持指定地址调用，目前python没有这个需求 """
        # 判断本地是否有服务，是否强制使用本地服务
        fitable: Fitable = Fitable(generic_id, const.FIXED_GENERICABLE_VERSION,
                                   fitable_id, const.FIXED_FITABLE_VERSION)
        fit_logger.debug(
            f"start to load balancing, gid: {generic_id}, fid: {fitable_id}, is filter none: {address_filter is None}")
        if address_filter:
            address = self.custom_load_balancing(address_filter, fitable)
            if address is None:
                fit_logger.warning(f"cannot get any address by custom load balancing. "
                                   f"[genericable_id={generic_id}, fitable_id={fitable_id}]")
            return address
        else:
            address = self.default_load_balancing(generic_id, fitable_id, fitable)
            if address is None:
                fit_logger.warning(f"cannot get any address by default load balancing. "
                                   f"[genericable_id={generic_id}, fitable_id={fitable_id}]")
            return address

    def default_load_balancing(self, generic_id, fitable_id, fitable: Fitable):
        """ 不支持指定地址调用，目前python没有这个需求 """
        # 判断本地是否有服务，是否强制使用本地服务

        address = service_repo.get_fitable_ref(generic_id, fitable_id)
        fit_logger.debug(f"load balance by default, gid: {generic_id}, fid: {fitable_id}")
        if address or _is_force_local(generic_id):
            fit_logger.debug(f"load balance by default, use local target, gid: {generic_id}, fid: {fitable_id}, "
                             f"is local none: {address is None}")
            return address
        addresses: List[Address] = self.get_fit_service_addresses(fitable)
        fit_logger.debug(f"load balance by default, get remote target, gid: {generic_id}, fid: {fitable_id}, "
                         f"addresses count: {len(addresses)}")
        if len(addresses) == 0:
            fit_logger.warning(f"cannot get any address can use in this worker. "
                               f"[genericable_id={fitable.genericableId}, fitable_id={fitable.fitableId}]")
            return None
        # no choice!
        if len(addresses) == 1:
            return addresses[0]

        return _load_balancing(fitable, addresses)

    def get_fit_service_addresses(self, fitable: Fitable) -> List[Address]:
        """
            获得所有fitable地址列表
            先根据支持的客户端通讯规约/协议过滤
            再根据环境标和环境链过滤
        Args:
            fitable ():

        Returns:

        """
        addresses: List[Address] = _get_fit_service_address_with_priorities(fitable)
        if not addresses:
            fit_logger.warning(f"cannot get any endpoint after checking format and protocol. "
                               f"[genericable_id={fitable.genericableId}, fitable_id={fitable.fitableId}]")
            return []

        addresses: List[Address] = _load_balance_env_filtering(addresses)
        if not addresses:
            fit_logger.warning(f"cannot get any endpoint after filtering by environment. "
                               f"[genericable_id={fitable.genericableId}, fitable_id={fitable.fitableId}]")
            return []

        return addresses

    @print_trace
    def fit_execute(self, address, generic_id: str, fitable_id: str, fit_ref, *args, timeout, is_async) -> Any:
        fit_logger.debug("execute on %s for generic_id: %s, fitable_id: %s",
                         str(address), generic_id, fitable_id)
        if isinstance(address, Address):
            from fitframework.core.broker import remote_invoker
            fit_invoke_info = (generic_id, fitable_id, fit_ref)
            return remote_invoker.call(address, fit_invoke_info, args, timeout, is_async)
        else:
            return address(*args)

    def get_degradation(self, generic_id: str, fitable_id: str) -> str:
        return get_degradation(generic_id, fitable_id)

    def recycle(self, generic_id: str, fitable_id: str) -> bool:
        # 根据配置判断是否指定recycleid调用，或者默认实现，通过broker调用
        if not _is_config_recycle(generic_id):
            return False

        recycle_id = get_fit_ffp_fitable_id(generic_id, 'recycle')
        if recycle_id is not None:
            fit_invoke_info = (const.RECYCLE_GEN_ID, recycle_id, recycle_template)
            _ffp_invoke(fit_invoke_info, False, None, None,
                        generic_id, fitable_id)
        else:
            # default recycle
            recycle(generic_id, fitable_id)
        return True

    def custom_routing(self, generic_id, route_filter) -> List[str]:
        # 先从配置文件 fit.yml 中取候选的 fitable
        fitable_ids = get_all_fitables_from_config(generic_id)
        fitable_ids = self.fiter_fitables_by_route_filter(generic_id, fitable_ids, route_filter)
        if len(fitable_ids) != 0:  # 如果 fit.yml 中配置的 fitable 中有符合要求的，那么就直接返回。
            return fitable_ids

        # 如果 fit.yml 中没有合适的候选，再从注册中心取候选的 fitable
        fitable_ids = get_all_fitables_from_registry(Genericable(generic_id, const.FIXED_GENERICABLE_VERSION))
        fitable_ids = self.fiter_fitables_by_route_filter(generic_id, fitable_ids, route_filter)
        return fitable_ids

    def custom_load_balancing(self, address_filter: Callable[[Address], bool],
                              fitable: Fitable) -> Optional[Address]:
        # 获得所有fitable地址列表
        # 先根据支持的客户端通讯规约/协议过滤
        addresses: List[Address] = self.get_fit_service_addresses(fitable)
        if len(addresses) == 0:
            fit_logger.warning(f"cannot get any address can use in this worker. "
                               f"[genericable_id={fitable.genericableId}, fitable_id={fitable.fitableId}]")
            return None
        try:
            addresses = [address for address in addresses if address_filter(address)]
        except Exception as exception:
            fit_logger.error(f'filter address error, msg: {exception}')
            return None
        if not addresses:
            fit_logger.warning(f"cannot get any address after custom load balancing. "
                               f"[genericable_id={fitable.genericableId}, fitable_id={fitable.fitableId}]")
            return None
        if len(addresses) > 1:
            fit_logger.warning(f"get more than one address after custom load balancing. "
                               f"[genericable_id={fitable.genericableId}, fitable_id={fitable.fitableId}]")
            return addresses[0]

        if addresses[0].id == _worker_id():
            return service_repo.get_fitable_ref(fitable.genericableId, fitable.fitableId)

        return addresses[0]


def _is_force_local(generic_id: str) -> bool:
    return GenericableTagEnum.LOCAL_ONLY.value in get_genericable_tags(generic_id)


def _is_invoke_all(generic_id: str) -> bool:
    return GenericableTagEnum.INVOKE_ALL.value in get_genericable_tags(generic_id)


def _is_config_recycle(generic_id: str) -> bool:
    return GenericableTagEnum.RECYCLE.value in get_genericable_tags(generic_id)


def _ffp_invoke(fit_invoke_info: tuple, return_type_override: bool,
                return_type: type, ffp_stage, *args):
    generic_id, fitable_id, fit_ref = fit_invoke_info
    from fitframework.core.broker.ffp_brokerimpl import FfpFitableBroker
    ffp_broker = FfpFitableBroker(return_type_override, return_type)
    return BrokerBuilder(ffp_broker, generic_id) \
        .trace_hop_id(ffp_stage) \
        .fitable_identifier(FitableIdentifier(fitable_id, IdType.id)) \
        .fit_ref(fit_ref).fit_selector_invoke(*args)


def recycle_template(generic_id: str, fitable_id: str) -> None:
    pass


def _load_balance_env_filtering(addresses: List[Address]) -> List[Address]:
    """ 负载均衡环境筛选 """

    def lb_filter_template(_endpoints: List[Address]) -> List[Address]:
        pass

    args = (addresses,)
    fit_invoke_info = (const.LOAD_BALANCING_FILTER_GEN_ID, const.LOAD_BALANCING_FILTER_FIT_ID, lb_filter_template)
    return _ffp_invoke(fit_invoke_info, False, None, None, *args)


def _load_balancing(fitable: Fitable, addresses: List[Address]) -> Address:
    """ 负载均衡 """

    def lb_call_template(fitable_info: Fitable, target_addresses: List[Address]) -> \
            Address:
        pass

    args = fitable, addresses
    lb_fitable_id = get_fit_ffp_fitable_id(fitable.genericableId, 'load_balance')
    if lb_fitable_id:
        fit_invoke_info = (fitable.genericableId, lb_fitable_id, lb_call_template)
        return _ffp_invoke(fit_invoke_info, False, None, None, *args)
    else:
        fit_invoke_info = (const.LOAD_BALANCING_GEN_ID, const.LOAD_BALANCING_RANDOM_FIT_ID, lb_call_template)
        return _ffp_invoke(fit_invoke_info, False, None, None, *args)


def _routing_by_rule(generic_id, fit_ref, *arg_val_s) -> Optional[str]:
    """
    进行规则路由，返回fitable id.
    目前逻辑，调动只有三种结果：
        - 返回None，表明未查询到规则（交由上层逻辑处理）
        - 报错，表明查询到规则，但规则执行的结果得到的fitable id为空
        - 成功返回fitable id，字符串类型
    """
    rule_id, rule_type = _get_rule_info(generic_id)
    if not rule_id:
        return None
    env = worker_env() if worker_env() in default_envs() else reserved_env()
    json_parameters = _assemble_rule_json_args(env, rule_type, generic_id, fit_ref, *arg_val_s)
    try:
        fitable_id = routing_rule_execute(env, rule_id, json_parameters)
        if not fitable_id:
            raise FitException(InternalErrorCode.AVAILABLE_FITABLE_NOT_FOUND_BY_RULE,
                               f"cannot find fitable id giving rule info with id: {rule_id}, type {rule_type}; "
                               f"generic id: {generic_id}.", degradable=False)
        return fitable_id
    except FitException as err:
        fit_logger.exception(err)
        raise


def _routing_by_route_fitable(generic_id, fit_ref, *args) -> Optional[str]:
    routing_fitable_id = get_fit_ffp_fitable_id(generic_id, 'route.dynamic')
    if not routing_fitable_id:
        return None
    try:
        fit_invoke_info = (generic_id, routing_fitable_id, fit_ref)
        fitable_id = _ffp_invoke(fit_invoke_info, True, str, None, *args)
        if not fitable_id:
            raise FitException(InternalErrorCode.AVAILABLE_FITABLE_NOT_FOUND_BY_DYNAMIC_ROUTING,
                               f"fitable id not found by dynamic routing, generic id: {generic_id} "
                               f"routing fitable id: {routing_fitable_id}", degradable=False)
        return fitable_id
    except FitException as err:
        fit_logger.exception(err)
        raise


def _get_rule_info(generic_id):
    rule_info = get_genericable_rule(generic_id)
    rule_id, rule_type = '', ''
    try:
        if rule_info is None:
            raise ValueError
        rule_id = rule_info[_FIT_META_GEN_RULE_ID_KEY]
        rule_type = rule_info[_FIT_META_GEN_RULE_TYPE_KEY]
    except (KeyError, ValueError):
        pass
    return rule_id, rule_type


def _assemble_rule_json_args(env, rule_type, generic_id, fit_ref, *arg_val_s) -> str:
    rule_json_args = {}
    if _FIT_META_GEN_RULE_TYPE_PARAM_VALUE in rule_type:
        rule_json_args[_FIT_META_GEN_RULE_TYPE_PARAM_VALUE] = _assemble_call_args(*arg_val_s)
    if _FIT_META_GEN_RULE_TYPE_TAG_VALUE in rule_type:
        rule_json_args[_FIT_META_GEN_RULE_TYPE_TAG_VALUE] = _assemble_tag_args(env, generic_id,
                                                                               fit_ref, *arg_val_s)
    if _FIT_META_GEN_RULE_TYPE_CONTEXT_VALUE in rule_type:
        rule_json_args[_FIT_META_GEN_RULE_TYPE_CONTEXT_VALUE] = get_all_route_context()
    return json_serialize(rule_json_args)


def _assemble_call_args(*arg_val_s) -> Dict[str, Any]:
    return dict((f'arg{i}', val) for i, val in enumerate(arg_val_s))


def _assemble_tag_args(env, generic_id: str, fit_ref, *arg_val_s) -> Dict[str, List[str]]:
    arg_tag_s = {}
    param_info = get_genericable_params(generic_id)

    for arg_name, (i, arg_val) in zip(get_parameter_types(fit_ref, with_name=True), enumerate(arg_val_s)):
        try:
            tagger_id_s = list(param_info[arg_name][_FIT_META_GEN_PARAMS_TAGGERS_KEY])
            if not tagger_id_s:
                raise ValueError
            json_parameters = json_serialize(
                {_FIT_META_GEN_RULE_TYPE_PARAM_VALUE: {f'arg{i}': arg_val}})
            filtered_tag_s = resolve_tags(env, tagger_id_s, json_parameters)
            arg_tag_s[f'arg{i}'] = filtered_tag_s
        except (KeyError, TypeError, ValueError):
            continue
    return arg_tag_s


def _get_designated_fitable_id(generic_id, fitable_identifier: FitableIdentifier, fit_ref, *args):
    try:
        if fitable_identifier.id_type == IdType.rule:
            fitable_id = _routing_by_rule(generic_id, fit_ref, *args)
            if fitable_id is None:
                raise FitException(InternalErrorCode.ROUTING_RULE_NOT_FOUND,
                                   f"cannot find rule info, generic id: {generic_id}.", degradable=False)
        elif fitable_identifier.id_type == IdType.alias:
            fitable_id = get_fitable_id_by_alias(generic_id, fitable_identifier.value)
            if not fitable_id:
                raise FitException(InternalErrorCode.ROUTING_ALIAS_NOT_FOUND,
                                   f"cannot find fitable alias, generic id: {generic_id}.", degradable=False)
        else:
            fitable_id = fitable_identifier.value
        return fitable_id
    except FitException as err:
        fit_logger.error(err)
        raise


def _get_fit_service_address_with_priorities(fitable: Fitable) -> List[Address]:
    endpoints = [addr for addr in _get_fit_service_address_and_convert(fitable) if _is_supported_address(addr)]
    return _remove_duplicated_ids(endpoints)


def _get_fit_service_address_and_convert(fitable: Fitable) -> List[Address]:
    addresses: List[Address] = get_fit_service_address_list(fitable)
    fit_logger.debug(f"got address, gid: {fitable.genericableId}, count: {len(addresses)}")
    return addresses


def _is_supported_address(address: Address) -> bool:
    from fitframework.core.broker import remote_invoker
    return address.protocol in get_registered_clients() and remote_invoker.get_supported_formats(
        address.formats)


def _build_address_id(address: Address) -> str:
    return f"{address.host}:{address.port}"


def _remove_duplicated_ids(addresses: List[Address]) -> List[Address]:
    from fitframework.core.broker import remote_invoker

    def sort_rule(_address: Address):
        return (_build_address_id(_address),
                get_priority_index(_address.protocol, _address.formats, remote_invoker.protocol_priors()))

    addresses.sort(key=sort_rule)
    result = []
    current_id = None
    for address in addresses:
        if _build_address_id(address) != current_id:
            result.append(address)
            current_id = address.id
    return result
