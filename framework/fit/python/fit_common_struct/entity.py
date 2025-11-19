# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：注册中心交互用数据结构。
"""
from typing import Dict, List
from numpy import int32

from fit_common_struct.core import Address as AddressInner
from fit_common_struct.core import Fitable


def safe_hash_dict(obj_dict):
    """安全地计算包含列表的字典的哈希值"""
    hashable_values = []
    for value in obj_dict.values():
        if isinstance(value, list):
            hashable_values.append(tuple(value))
        elif isinstance(value, dict):
            hashable_values.append(tuple(sorted(value.items())))
        else:
            hashable_values.append(value)
    return hash(tuple(hashable_values))


class FitableMeta(object):

    def __init__(self, fitable: Fitable, aliases: List[str], formats: List[int32]):
        self.fitable = fitable
        self.aliases = aliases

        """
        protobuf-----0
        json------1
        """
        self.formats = formats

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        # 使用安全的哈希函数处理包含列表的对象
        return safe_hash_dict(self.__dict__)

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class Application(object):

    def __init__(self, name: str, nameVersion: str):
        self.name = name
        self.nameVersion = nameVersion

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class Endpoint(object):

    def __init__(self, port: int32, protocol: int32):
        self.port = port

        """ 
        rsocket------0
        socket-------1
        http---------2
        grpc---------3
        uc-----------10
        shareMemory--11
        """
        self.protocol = protocol

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class Address(object):

    def __init__(self, host: str, endpoints: List[Endpoint]):
        self.host = host
        self.endpoints = endpoints

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return safe_hash_dict(self.__dict__)

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class Worker(object):

    def __init__(self, addresses: List[Address], id: str, environment: str, extensions: Dict[str, str]):
        self.addresses = addresses

        # 地址所在进程唯一标识，正常ip网络下可用host:port拼接或uuid作为唯一标识，心跳时进程上报给心跳服务时应使用该标识，用于进程下线时更新对应服务的状态
        self.id = id

        self.environment = environment
        self.extensions = extensions

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return safe_hash_dict(self.__dict__)

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class ApplicationInstance(object):

    def __init__(self, workers: List[Worker], application: Application, formats: List[int32]):
        self.workers = workers
        self.application = application

        """
        protobuf-----0
        json------1
        """
        self.formats = formats

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return safe_hash_dict(self.__dict__)

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class FitableAddressInstance(object):

    def __init__(self, applicationInstances: List[ApplicationInstance], fitable: Fitable):
        self.applicationInstances = applicationInstances
        self.fitable = fitable

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return safe_hash_dict(self.__dict__)

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class FitableMetaInstance(object):

    def __init__(self, meta: FitableMeta, environments: List[str]):
        self.meta = meta
        self.environments = environments

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return safe_hash_dict(self.__dict__)

    def __repr__(self):
        return str(tuple(self.__dict__.values()))

class HeartBeatInfo(object):

    def __init__(self, sceneType: str, aliveTime: int, initDelay: int):
        self.sceneType: str = sceneType
        self.aliveTime: int = aliveTime
        self.initDelay: int = initDelay

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return safe_hash_dict(self.__dict__)

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class HeartBeatAddress(object):
    def __init__(self, id_: str):
        self.id = id_

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return safe_hash_dict(self.__dict__)

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


