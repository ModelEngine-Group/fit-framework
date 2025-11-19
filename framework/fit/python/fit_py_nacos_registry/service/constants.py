# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
Constants for Nacos registry server.
"""
import re

# Metadata keys
CLUSTER_DOMAIN_KEY = "cluster.domain"
WORKER_KEY = "worker"
APPLICATION_KEY = "application"
FITABLE_META_KEY = "fitable-meta"

# Patterns and separators
CLUSTER_PORT_PATTERN = re.compile(r"cluster\.(.*?)\.port")
SEPARATOR = "::"

# Protocol code mapping
PROTOCOL_CODE_MAP = {
    "rsocket": 0,
    "socket": 1,
    "http": 2,
    "grpc": 3,
    "uc": 10,
    "share_memory": 11
}
