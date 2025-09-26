# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
Utility functions for Nacos registry server.
"""
from typing import List, Dict

from fit_common_struct.core import Fitable, Genericable
from fit_common_struct.entity import Worker, Application, FitableMeta, Endpoint

from fitframework.api.logging import sys_plugin_logger
from fitframework.utils.json_serialize_utils import json_serialize

from .constants import SEPARATOR, CLUSTER_PORT_PATTERN, PROTOCOL_CODE_MAP, \
    WORKER_KEY, APPLICATION_KEY, FITABLE_META_KEY
from .config import get_heartbeat_weight, get_heartbeat_is_ephemeral, \
    get_heartbeat_interval, get_heartbeat_timeout


def build_service_key(group_name: str, service_name: str) -> str:
    """
    Build a unique key in the format <groupName>::<serviceName> for service subscriptions.

    Args:
        group_name: The group name as string.
        service_name: The service name as string.

    Returns:
        A concatenated key like groupName::serviceName.
    """
    return f"{group_name}{SEPARATOR}{service_name}"


def get_service_name(fitable: Fitable) -> str:
    """
    Get the service name from Fitable.
    
    Args:
        fitable: The Fitable object.
        
    Returns:
        The service name.
    """
    return f"{fitable.fitableId}{SEPARATOR}{fitable.fitableVersion}"


def get_group_name_from_fitable(fitable: Fitable) -> str:
    """
    Get the group name from Fitable.
    
    Args:
        fitable: The Fitable object.
        
    Returns:
        The group name.
    """
    return f"{fitable.genericableId}{SEPARATOR}{fitable.genericableVersion}"


def get_group_name_from_genericable(genericable: Genericable) -> str:
    """
    Get the group name from Genericable.
    
    Args:
        genericable: The Genericable object.
        
    Returns:
        The group name.
    """
    return f"{genericable.genericableId}{SEPARATOR}{genericable.genericableVersion}"


def create_instances(worker: Worker, application: Application, meta: FitableMeta) -> List[Dict]:
    """
    Create instance information.

    Args:
        worker: Worker node object.
        application: Application object.
        meta: FitableMeta metadata object.

    Returns:
        List of instance dictionaries.
    """
    sys_plugin_logger.debug(
        f"Creating instance for worker. [worker={worker.id}, "
        f"application={application.nameVersion}, meta={meta}]"
    )
    instances = []

    for address in worker.addresses:
        for endpoint in address.endpoints:
            # Prepare metadata
            metadata = build_instance_metadata(worker, application, meta)

            # Build instance
            instance = {
                "ip": address.host,
                "port": endpoint.port,
                "weight": get_heartbeat_weight(),
                "ephemeral": get_heartbeat_is_ephemeral(),
                "metadata": metadata
            }
            instances.append(instance)

    return instances


def build_instance_metadata(worker: Worker, application: Application, meta: FitableMeta) -> Dict[str, str]:
    """
    Build metadata for service instance, including worker, application and FitableMeta information.

    Args:
        worker: The worker node object.
        application: The application object.
        meta: The FitableMeta metadata object.

    Returns:
        A dict containing all serialized metadata.
    """
    metadata = {}

    # Add heartbeat configuration
    metadata["preserved.heart.beat.interval"] = str(get_heartbeat_interval())
    metadata["preserved.heart.beat.timeout"] = str(get_heartbeat_timeout())

    try:
        metadata[WORKER_KEY] = json_serialize(worker)
        metadata[APPLICATION_KEY] = json_serialize(application)
        metadata[FITABLE_META_KEY] = json_serialize(meta)
    except Exception as e:
        sys_plugin_logger.error(f"Failed to serialize metadata for worker: {e}")

    return metadata


def build_endpoints(extensions: Dict[str, str]) -> List[Endpoint]:
    """
    Build endpoint list from extensions.
    
    Args:
        extensions: Extension configuration dictionary.
        
    Returns:
        List of endpoints.
    """
    endpoints = []

    for key, value in extensions.items():
        match = CLUSTER_PORT_PATTERN.match(key)
        if match:
            protocol_name = match.group(1).lower()
            if protocol_name in PROTOCOL_CODE_MAP:
                endpoint = Endpoint(int(value), PROTOCOL_CODE_MAP[protocol_name])
                endpoints.append(endpoint)
            else:
                sys_plugin_logger.error(f"Unknown protocol: {protocol_name}")

    return endpoints
