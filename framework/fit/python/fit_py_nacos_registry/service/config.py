# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
Configuration module for Nacos registry server.
"""
from v2.nacos import ClientConfigBuilder

from fitframework import value
from fitframework.utils import tools


@value('registry-center.server.addresses', converter=tools.to_list)
def get_registry_server_addresses() -> list:
    """Get the list of registry server addresses."""
    pass


@value('nacos.username', default_value=None)
def get_nacos_username() -> str:
    """
    Get the Nacos username.
    
    Returns:
        Nacos username.
    """
    pass


@value('nacos.password', default_value=None)
def get_nacos_password() -> str:
    """
    Get the Nacos password.
    
    Returns:
        Nacos password.
    """
    pass


@value('nacos.accessKey', default_value=None)
def get_nacos_access_key() -> str:
    """
    Get the Nacos access key.
    
    Returns:
        Nacos access key.
    """
    pass


@value('nacos.secretKey', default_value=None)
def get_nacos_secret_key() -> str:
    """
    Get the Nacos secret key.
    
    Returns:
        Nacos secret key.
    """
    pass


@value('nacos.namespace', default_value="")
def get_nacos_namespace() -> str:
    """
    Get the Nacos namespace.
    
    Returns:
        Nacos namespace.
    """
    pass


@value('nacos.isEphemeral', default_value=True, converter=bool)
def get_heartbeat_is_ephemeral() -> bool:
    """
    Get whether the heartbeat is ephemeral.
    
    Returns:
        Whether the heartbeat is ephemeral.
    """
    pass


@value('nacos.heartBeatInterval', default_value=5000, converter=int)
def get_heartbeat_interval() -> int:
    """
    Get the heartbeat interval in milliseconds.
    
    Returns:
        Heartbeat interval in milliseconds.
    """
    pass


@value('nacos.heartBeatTimeout', default_value=15000, converter=int)
def get_heartbeat_timeout() -> int:
    """
    Get the heartbeat timeout in milliseconds.
    
    Returns:
        Heartbeat timeout in milliseconds.
    """
    pass


@value('nacos.weight', default_value=1.0, converter=float)
def get_heartbeat_weight() -> float:
    """
    Get the heartbeat weight.
    
    Returns:
        Heartbeat weight.
    """
    pass


def build_nacos_config():
    """
    Build the Nacos client configuration.
    
    Returns:
        Configured Nacos client config.
    """
    return (ClientConfigBuilder()
            .server_address(get_registry_server_addresses()[0])
            .namespace_id(get_nacos_namespace() or 'local')
            .username(get_nacos_username())
            .password(get_nacos_password())
            .access_key(get_nacos_access_key())
            .secret_key(get_nacos_secret_key())
            .build())
