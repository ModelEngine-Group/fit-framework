# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
Nacos registry service package.
"""

from .nacos_registry_server import (
    register_fitables,
    unregister_fitables,
    query_fitable_addresses,
    subscribe_fit_service,
    unsubscribe_fitables,
    query_fitable_metas
)

__all__ = [
    'register_fitables',
    'unregister_fitables',
    'query_fitable_addresses',
    'subscribe_fit_service',
    'unsubscribe_fitables',
    'query_fitable_metas'
]
