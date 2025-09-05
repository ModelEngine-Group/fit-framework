# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
from typing import List

from numpy import int32, uint32, uint64


class Genericable(object):

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


class Fitable(object):

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


class FitableAliasesInfo(object):

    def __init__(self, fitable: Fitable,
                 aliases: List[str]):
        self.fitable = fitable
        self.aliases = aliases

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class Address(object):

    def __init__(self, host: str, port: int32, worker_id: str, protocol: int32, formats: List[int32], environment: str,
                 context_path: str):
        self.host = host
        self.port = port

        # 地址所在进程唯一标识，正常ip网络下可用host:port拼接或uuid作为唯一标识，心跳时进程上报给心跳服务时应使用该标识，用于进程下线时更新对应服务的状态
        self.id = worker_id

        """ 
        rsocket------0
        socket-------1
        http---------2
        grpc---------3
        uc-----------10
        shareMemory--11
        """
        self.protocol = protocol

        """
        protobuf-----0
        jackson------1
        """
        self.formats = formats
        self.environment = environment
        self.context_path = context_path

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class BaseFitTrace(object):

    def __init__(self, traceTimestamp: uint64, traceId: str, span: str, traceType: uint32, host: str, port: uint32,
                 workerId: str, resultCode: str, timeCost: uint32, flowType: uint32, stage: uint32):
        self.traceTimestamp = traceTimestamp
        self.traceId = traceId
        self.span = span

        # 调用类型
        self.traceType = traceType

        # 主机
        self.host = host

        # 端口
        self.port = port
        self.workerId = workerId

        # 调用结果，ok/Timeout/ConnectError
        self.resultCode = resultCode

        # 时间花费
        self.timeCost = timeCost

        # 流量类型
        self.flowType = flowType

        # 阶段
        self.stage = stage

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str(tuple(self.__dict__.values()))


class FitableTrace(object):

    def __init__(self, baseFitTrace: BaseFitTrace, genericId: str, genericVersion: str, fitId: str, serviceName: str,
                 targetHost: str, targetPort: uint32, callType: uint32, fromFitId: str, trustStage: uint32):
        self.baseFitTrace = baseFitTrace
        self.genericId = genericId
        self.genericVersion = genericVersion
        self.fitId = fitId
        self.serviceName = serviceName
        self.targetHost = targetHost
        self.targetPort = targetPort

        # 调用类型  1 本地调用本地  2 本地调用远程  3 远程调用本地
        self.callType = callType
        self.fromFitId = fromFitId

        # 可信步骤 0: validation, 1: before, 2: process, 3: degredation, 4: after, 5: error
        self.trustStage = trustStage

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str(tuple(self.__dict__.values()))
