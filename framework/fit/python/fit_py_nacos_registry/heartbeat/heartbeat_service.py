# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：服务上线相关功能。
"""

from typing import List
from fitframework import fitable, const
from fit_common_struct.entity import HeartBeatInfo, HeartBeatAddress

@fitable(const.SEND_HEART_BEAT_GEN_ID, const.SEND_HEART_BEAT_FIT_ID)
def send_heartbeat(heartbeatInfo: List[HeartBeatInfo], address: HeartBeatAddress) -> bool:
    """
    发送心跳信息。

    @param heartbeatInfo: 表示待停止心跳信息列表
    @param address: 表示待停止心跳信息列表。
    """
    return True

@fitable(const.STOP_HEART_BEAT_GEN_ID, const.STOP_HEART_BEAT_FIT_ID)
def stop_Heartbeat(heartbeatInfo: List[HeartBeatInfo], address: HeartBeatAddress) -> bool:
    """
    发送停止心跳信息。

    @param heartbeatInfo: 表示待停止心跳信息列表。
    @param address: 表示待停止心跳信息列表。
    """
    return True