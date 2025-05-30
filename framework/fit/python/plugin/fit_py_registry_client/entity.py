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


class FitableInfo(object):

    def __init__(self, genericableId: str, genericableVersion: str, fitableId: str, fitableVersion: str):
        self.genericableId = genericableId
        self.genericableVersion = genericableVersion
        self.fitableId = fitableId
        self.fitableVersion = fitableVersion

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class GenericableInfo:

    def __init__(self, genericableId: str, genericableVersion: str):
        self.genericableId = genericableId
        self.genericableVersion = genericableVersion

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class FitableMeta(object):

    def __init__(self, fitable: FitableInfo, aliases: List[str], formats: List[int32]):
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
        return hash(tuple(self.__dict__.values()))

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
        return hash(tuple(self.__dict__.values()))

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
        return hash(tuple(self.__dict__.values()))

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
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class FitableAddressInstance(object):

    def __init__(self, applicationInstances: List[ApplicationInstance], fitable: FitableInfo):
        self.applicationInstances = applicationInstances
        self.fitable = fitable

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

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
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


