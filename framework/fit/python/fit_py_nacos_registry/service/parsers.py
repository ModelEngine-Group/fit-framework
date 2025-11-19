# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
Parsing functions for Nacos registry server.
"""
from typing import Dict, List, Set

from v2.nacos import Instance

from fit_common_struct.entity import Worker, Application, FitableMeta, Endpoint, Address
from fit_common_struct.core import Fitable
from fitframework.utils.json_serialize_utils import json_deserialize
from fitframework.api.logging import sys_plugin_logger

from .constants import WORKER_KEY, APPLICATION_KEY, FITABLE_META_KEY


def parse_fitable_meta(metadata: Dict) -> FitableMeta:
    """
    Parse FitableMeta from metadata.
    
    Args:
        metadata: The metadata dictionary.
        
    Returns:
        Parsed FitableMeta object or default if parsing fails.
    """
    try:
        meta_json = metadata.get(FITABLE_META_KEY)
        if meta_json:
            return json_deserialize(FitableMeta, meta_json)
    except Exception as e:
        sys_plugin_logger.error(f"Failed to parse fitable meta for instance: {e}")

    # Return default value
    default_fitable = Fitable("unknown", "1.0", "unknown", "1.0")
    meta = FitableMeta(default_fitable, [], [])
    return meta


def parse_application(metadata: Dict) -> Application:
    """
    Parse Application from metadata.
    
    Args:
        metadata: The metadata dictionary.
        
    Returns:
        Parsed Application object or default if parsing fails.
    """
    try:
        app_json = metadata.get(APPLICATION_KEY)
        if app_json:
            return json_deserialize(Application, app_json)
    except Exception as e:
        sys_plugin_logger.error(f"Failed to parse application metadata for instance: {e}")

    # Return default value
    return Application("unknown", "unknown")


def parse_worker(instance_or_metadata) -> Worker:
    """
    Parse Worker from instance or metadata.
    
    Args:
        instance_or_metadata: Either an Instance object or metadata dictionary.
        
    Returns:
        Parsed Worker object or default if parsing fails.
    """
    try:
        # Handle different input types
        if hasattr(instance_or_metadata, 'metadata'):
            metadata = instance_or_metadata.metadata
            ip = getattr(instance_or_metadata, 'ip', 'unknown')
            port = getattr(instance_or_metadata, 'port', 0)
        else:
            metadata = instance_or_metadata
            ip = 'unknown'
            port = 0

        worker_json = metadata.get(WORKER_KEY)
        if worker_json:
            return json_deserialize(Worker, worker_json)
    except Exception as e:
        sys_plugin_logger.error(f"Failed to parse worker metadata for instance: {e}")

    # Fallback - create basic worker information
    worker = Worker([], "unknown", "", {})

    # If IP and port info available, create basic address
    if ip != 'unknown' and port != 0:
        endpoint = Endpoint(port, 1)  # Default protocol
        address = Address(ip, [endpoint])
        worker.addresses = [address]

    return worker


def group_instances_by_application(instances: List[Instance]) -> Dict[Application, List[Instance]]:
    """
    Group instances by application.
    
    Args:
        instances: List of instances to group.
        
    Returns:
        Dictionary mapping applications to their instances.
    """
    app_instances_map = {}
    for instance in instances:
        metadata = instance.metadata
        app = parse_application(metadata)
        app_instances_map.setdefault(app, []).append(instance)
    return app_instances_map


def extract_workers(app_instances: List[Instance], application: Application) -> Set[Worker]:
    """
    Extract all workers corresponding to instances.

    Args:
        app_instances: The list of application instances.
        application: The application object.

    Returns:
        Set of workers.
    """
    workers = set()
    for instance in app_instances:
        worker = parse_worker(instance)
        workers.add(worker)
    return workers
