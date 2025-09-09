# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
Service for providing Nacos registry center functionality.
"""
from concurrent.futures import ThreadPoolExecutor
from typing import List, Dict
import weakref
import atexit

from v2.nacos import RegisterInstanceParam, ListInstanceParam, \
    DeregisterInstanceParam, SubscribeServiceParam, Instance, ListServiceParam

from fitframework import fitable, const
from fitframework.api.logging import sys_plugin_logger
from fit_common_struct.entity import Worker, FitableMeta, Application, FitableAddressInstance, \
    FitableMetaInstance, ApplicationInstance
from fit_common_struct.core import Fitable, Genericable

from .config import get_nacos_namespace
from .utils import build_service_key, get_service_name, get_group_name_from_fitable, \
    get_group_name_from_genericable, create_instances
from .parsers import parse_fitable_meta, parse_application, parse_worker, \
    group_instances_by_application, extract_workers
from .async_executor import run_async_safely, call_list_instances, call_deregister_instance, \
    call_subscribe, call_unsubscribe, call_list_services, call_register_instance

# Global variables
_service_subscriptions: weakref.WeakValueDictionary = weakref.WeakValueDictionary()
_executor = ThreadPoolExecutor(max_workers=10)


def _cleanup_executor():
    """Cleanup the thread pool executor."""
    try:
        if _executor:
            _executor.shutdown(wait=True)
            sys_plugin_logger.info("Thread pool executor shut down successfully")
    except Exception as e:
        sys_plugin_logger.error(f"Error shutting down thread pool executor: {e}")


# Register cleanup function to ensure executor is properly closed
atexit.register(_cleanup_executor)


def on_service_changed(fitable_info: Fitable, worker_id: str) -> None:
    """
    Handle service change events, query and notify updates to Fitables instance information.

    Args:
        fitable_info: The changed Fitables information.
        worker_id: The worker ID.
    """
    try:
        # Query current instances
        instances = query_fitable_addresses([fitable_info], worker_id)
        # notify_fitables(instances)
        sys_plugin_logger.debug(
            f"Service changed for fitable: {fitable_info}, instances: {len(instances)}"
        )
    except Exception as e:
        sys_plugin_logger.error(f"Service change handling failed: {e}")


def extract_workers(app_instances: List[Instance], application: Application) -> List[Worker]:
    """
    Extract all workers corresponding to instances.

    Args:
        app_instances: The list of application instances.
        application: The application object.

    Returns:
        List of workers.
    """
    workers = []
    for instance in app_instances:
        worker = parse_worker(instance)
        workers.append(worker)

    return workers


@fitable(const.REGISTER_FIT_SERVICE_GEN_ID, const.REGISTER_FIT_SERVICE_FIT_ID)
def register_fitables(fitable_metas: List[FitableMeta], worker: Worker, application: Application) -> None:
    """
    Register Fitable service implementations to the registry center.

    Args:
        fitable_metas: List of Fitable metadata to register.
        worker: Current FIT process information.
        application: Current application information.

    Raises:
        Exception: If registration fails due to registry error.
    """
    try:
        sys_plugin_logger.debug(
            f"Registering fitables. [fitableMetas={fitable_metas}, "
            f"worker={worker.id}, application={application.nameVersion}]"
        )

        for meta in fitable_metas:
            fitable = meta.fitable
            group_name = get_group_name_from_fitable(fitable)
            service_name = get_service_name(fitable)

            instances = create_instances(worker, application, meta)
            for instance in instances:
                param = RegisterInstanceParam(
                    service_name=service_name,
                    group_name=group_name,
                    ip=instance["ip"],
                    port=instance["port"],
                    weight=instance["weight"],
                    ephemeral=instance["ephemeral"],
                    metadata=instance["metadata"]
                )
                run_async_safely(call_register_instance(param))

        sys_plugin_logger.info(f"Successfully registered fitables for worker {worker.id}")
    except Exception as e:
        sys_plugin_logger.error(f"Failed to register fitables due to registry error: {e}")
        raise

@fitable(const.UNREGISTER_FIT_SERVICE_GEN_ID, const.UNREGISTER_FIT_SERVICE_FIT_ID)
def unregister_fitables(fitables: List[Fitable], worker_id: str) -> None:
    """
    Unregister service implementations from the registry center.

    Args:
        fitables: List of Fitable implementations to unregister.
        worker_id: Unique identifier of the process where service implementations reside.
    """
    sys_plugin_logger.debug(
        f"Unregistering fitables for worker. [fitables={fitables}, workerId={worker_id}]"
    )

    for fitable in fitables:
        unregister_single_fitable(fitable, worker_id)


def unregister_single_fitable(fitable: Fitable, worker_id: str) -> None:
    """
    Unregister a single Fitable implementation.

    Args:
        fitable: The Fitable implementation to unregister.
        worker_id: The worker ID.
    """
    group_name = get_group_name_from_fitable(fitable)
    service_name = get_service_name(fitable)

    try:
        # Get all instances for the service
        param = ListInstanceParam(
            service_name=service_name,
            group_name=group_name,
            healthy_only=True
        )
        instances = run_async_safely(call_list_instances(param))
        unregister_matching_instances(instances, worker_id, service_name, group_name)
    except Exception as e:
        sys_plugin_logger.error(f"Failed to unregister fitable due to registry error: {e}")


def unregister_matching_instances(instances: List[Instance], worker_id: str, service_name: str, group_name: str) -> None:
    """
    Unregister matching instances.

    Args:
        instances: List of instances to check.
        worker_id: The worker ID to match.
        service_name: The service name.
        group_name: The group name.
    """
    for instance in instances:
        try:
            worker = parse_worker(instance)
            if worker and worker.id == worker_id:
                param = DeregisterInstanceParam(
                    service_name=service_name,
                    group_name=group_name,
                    ip=instance.ip,
                    port=instance.port
                )
                run_async_safely(call_deregister_instance(param))
                sys_plugin_logger.debug(f"Successfully deregistered instance {instance.ip}:{instance.port}")
        except Exception as e:
            sys_plugin_logger.error(f"Failed to deregister instance: {e}")


@fitable(const.QUERY_FIT_SERVICE_GEN_ID, const.QUERY_FIT_SERVICE_FIT_ID)
def query_fitable_addresses(fitables: List[Fitable], worker_id: str) -> List[FitableAddressInstance]:
    """
    Query instance information for Fitable implementations (pull mode).

    Args:
        fitables: List of Fitable implementation information.
        worker_id: Current FIT process identifier.

    Returns:
        List of obtained instance information.
    """
    sys_plugin_logger.debug(
        f"Querying fitables for worker. [fitables={fitables}, workerId={worker_id}]"
    )
    result_map = {}

    for fitable in fitables:
        try:
            instances = query_instances(fitable)
            if not instances:
                continue
            process_application_instances(result_map, fitable, instances)
        except Exception as e:
            sys_plugin_logger.error(f"Failed to query fitables for genericableId: {e}")

    return list(result_map.values())


def query_instances(fitable: Fitable) -> List[Instance]:
    """
    Query instances for a specific Fitable.

    Args:
        fitable: The Fitable to query instances for.

    Returns:
        List of instances.
    """
    group_name = get_group_name_from_fitable(fitable)
    service_name = get_service_name(fitable)

    param = ListInstanceParam(
        service_name=service_name,
        group_name=group_name,
        healthy_only=True
    )
    return run_async_safely(call_list_instances(param))


def process_application_instances(result_map: Dict, fitable: Fitable, instances: List[Instance]) -> None:
    """
    Process application instances and group them.

    Args:
        result_map: Dictionary to store results.
        fitable: The Fitable being processed.
        instances: List of instances to process.
    """
    app_instances_map = group_instances_by_application(instances)
    
    for app, app_instances in app_instances_map.items():
        meta = parse_fitable_meta(app_instances[0].metadata)
        workers = extract_workers(app_instances, app)
        
        fai = result_map.get(fitable)
        if fai is None:
            fai = FitableAddressInstance(applicationInstances=[], fitable=fitable)
            result_map[fitable] = fai
        
        app_instance = ApplicationInstance(workers=list(workers), application=app, formats=meta.formats if meta.formats else [])
        fai.applicationInstances.append(app_instance)


@fitable(const.SUBSCRIBE_FIT_SERVICE_GEN_ID, const.SUBSCRIBE_FIT_SERVICE_FIT_ID)
def subscribe_fit_service(fitables: List[Fitable], worker_id: str, callback_fitable_id: str) -> List[FitableAddressInstance]:
    """
    Subscribe to Fitable service instance information (push mode).

    Args:
        fitables: List of Fitable implementation information.
        worker_id: Current FIT process identifier.
        callback_fitable_id: Identifier for callback Fitable implementation.

    Returns:
        Queried instance information.
    """
    sys_plugin_logger.debug(
        f"Subscribing to fitables for worker. [fitables={fitables}, "
        f"workerId={worker_id}, callbackFitableId={callback_fitable_id}]"
    )

    # Register subscriptions
    for fitable in fitables:
        try:
            group_name = get_group_name_from_fitable(fitable)
            service_name = get_service_name(fitable)
            service_key = build_service_key(group_name, service_name)

            if service_key in _service_subscriptions:
                sys_plugin_logger.debug(
                    f"Already subscribed to service. [groupName={group_name}, serviceName={service_name}]"
                )
                continue

            # Create event listener
            def create_event_listener(fitable_ref: Fitable, worker_id_ref: str):
                def event_listener(event):
                    _executor.submit(on_service_changed, fitable_ref, worker_id_ref)
                return event_listener

            event_listener = create_event_listener(fitable, worker_id)
            _service_subscriptions[service_key] = event_listener

            # Register subscription
            param = SubscribeServiceParam(
                service_name=service_name,
                group_name=group_name,
                subscribe_callback=event_listener
            )
            run_async_safely(call_subscribe(param))
            sys_plugin_logger.debug(
                f"Subscribed to service. [groupName={group_name}, serviceName={service_name}]"
            )

        except Exception as e:
            sys_plugin_logger.error(f"Failed to subscribe to Nacos service: {e}")

    return query_fitable_addresses(fitables, worker_id)


@fitable(const.UNSUBSCRIBE_FIT_SERVICE_GEN_ID, const.UNSUBSCRIBE_FIT_SERVICE_FIT_ID)
def unsubscribe_fitables(fitables: List[Fitable], worker_id: str, callback_fitable_id: str) -> None:
    """
    Unsubscribe from specified Fitable service instance information.

    Args:
        fitables: List of specified Fitable implementations.
        worker_id: Unique identifier of the specified process.
        callback_fitable_id: Unique identifier for unsubscribe callback Fitable implementation.
    """
    sys_plugin_logger.debug(f"Unsubscribing from fitables for worker. [fitables={fitables}, workerId={worker_id}, callbackFitableId={callback_fitable_id}]")
    
    for fitable in fitables:
        try:
            group_name = get_group_name_from_fitable(fitable)
            service_name = get_service_name(fitable)
            service_key = build_service_key(group_name, service_name)

            # Use pop with default to avoid KeyError if listener was garbage collected
            listener = _service_subscriptions.pop(service_key, None)
            if listener is not None:
                param = SubscribeServiceParam(
                    service_name=service_name,
                    group_name=group_name,
                    subscribe_callback=listener
                )
                run_async_safely(call_unsubscribe(param))
                sys_plugin_logger.debug(f"Unsubscribed from service. [groupName={group_name}, serviceName={service_name}]")
            else:
                sys_plugin_logger.debug(f"Listener already cleaned up for service. [groupName={group_name}, serviceName={service_name}]")
        except Exception as e:
            sys_plugin_logger.error(f"Failed to unsubscribe from Nacos service: {e}")


@fitable(const.QUERY_FITABLE_METAS_GEN_ID, const.QUERY_FITABLE_METAS_FIT_ID)
def query_fitable_metas(genericable_infos: List[Genericable]) -> List[FitableMetaInstance]:
    """
    Query Fitable metadata from the registry center.

    Args:
        genericable_infos: List of Genericable information.

    Returns:
        List of queried Fitable metadata.
    """
    sys_plugin_logger.debug(
        f"Querying fitable metas for genericables. [genericables={genericable_infos}]"
    )
    meta_environments = {}

    for genericable in genericable_infos:
        process_genericable_services(genericable, meta_environments)

    return build_fitable_meta_instances(meta_environments)


def process_genericable_services(genericable: Genericable, meta_environments: Dict) -> None:
    """
    Process service list for a Genericable.

    Args:
        genericable: The Genericable to process.
        meta_environments: Dictionary to collect metadata environments.
    """
    group_name = get_group_name_from_genericable(genericable)

    try:
        # Get all services under the group
        param = ListServiceParam(
            namespace_id=get_nacos_namespace(),
            group_name=group_name,
            page_no=1,
            page_size=1000  # Assume fetching enough services at once
        )
        service_list = run_async_safely(call_list_services(param))

        for service_name in service_list.services:
            process_service_instances(service_name, group_name, meta_environments)
    except Exception as e:
        sys_plugin_logger.error(f"Failed to query fitable metas: {e}")


def process_service_instances(service_name: str, group_name: str, meta_environments: Dict) -> None:
    """
    Process service instances.

    Args:
        service_name: The service name.
        group_name: The group name.
        meta_environments: Dictionary to collect metadata environments.
    """
    try:
        # Get service instances
        param = ListInstanceParam(
            service_name=service_name,
            group_name=group_name,
            healthy_only=True
        )
        instances = run_async_safely(call_list_instances(param))

        if not instances:
            return

        meta = parse_fitable_meta(instances[0].metadata)
        collect_environments_from_instances(instances, meta, meta_environments)
    except Exception as e:
        sys_plugin_logger.error(f"Failed to select instances for service {service_name}: {e}")


def collect_environments_from_instances(instances: List[Instance], meta: FitableMeta, meta_environments: Dict) -> None:
    """
    Collect environment information from instances.

    Args:
        instances: List of instances to process.
        meta: FitableMeta object.
        meta_environments: Dictionary to collect environments.
    """
    for instance in instances:
        try:
            worker = parse_worker(instance)
            if worker and worker.environment:
                if meta not in meta_environments:
                    meta_environments[meta] = set()
                meta_environments[meta].add(worker.environment)
        except Exception as e:
            sys_plugin_logger.error(f"Failed to parse worker metadata: {e}")


def build_fitable_meta_instances(meta_environments: Dict) -> List[FitableMetaInstance]:
    """
    Build FitableMetaInstance list.

    Args:
        meta_environments: Dictionary mapping metadata to environments.

    Returns:
        List of FitableMetaInstance objects.
    """
    results = []
    for meta, envs in meta_environments.items():
        instance = FitableMetaInstance(meta, list(envs))
        results.append(instance)
    return results
