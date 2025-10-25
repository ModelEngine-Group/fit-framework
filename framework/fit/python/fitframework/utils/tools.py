# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：通用工具箱
"""
import base64
import platform
import socket
from functools import reduce
from itertools import groupby
from threading import Lock
from typing import Union, Optional

_LOCAL_HOST = '127.0.0.1'


def dash_style(underscore_style: str):
    return '--' + underscore_style.replace('_', '-')


def string_normalize(string: str) -> Optional[str]:
    """ 对字符串进行格式化：去掉两边的空格，再转换成全小写 """
    return string.strip().lower()


def remove_prefix(string: str, prefix: str) -> str:
    return string[len(prefix):] if string.startswith(prefix) else string


def to_list(values: Union[str, list]):
    """ 逗号分割字符串转列表 """
    if isinstance(values, list):
        return values
    return values.split(',') if values is not None else []


def to_bool(value: Union[str, int, bool]):
    """
    Convert an object to boolean.
        - possible true values: True, 'aaa', 'true', 'True', 100, ...
        - possible false values: False, '', 'false', 'False', 0, -3, None...
    """
    if isinstance(value, int):
        return value > 0
    elif isinstance(value, str):
        return value not in ('', 'false', 'False')
    else:
        return bool(value)


def get_free_tcp_port() -> int:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((_LOCAL_HOST, 0))
        return s.getsockname()[1]


def get_address(ip: str, port: str):
    return f"{ip}:{port}" if port else ip


def b64encode_to_str(bytes_: bytes) -> str:
    return base64.b64encode(bytes_).decode('ascii')


def b64decode_from_str(string: str) -> bytes:
    if not isinstance(string, str):
        raise TypeError
    return base64.b64decode(string.encode('ascii'))


def multi_maps_exhaust(iterable, *funcs):
    def _binary_compose(f, g):
        return lambda x: g(f(x))

    return list(map(reduce(_binary_compose, funcs), iterable))


def sort_and_groupby(iterable, key_func):
    return groupby(sorted(iter(iterable), key=key_func), key_func)


def get_memory_usage():
    if platform.system() == 'Windows':
        import psutil
        return psutil.Process().memory_info().rss
    import os
    pid = os.getpid()
    with open(f'/proc/{pid}/status', 'r') as f:
        for line in f:
            if line.startswith('VmRSS:'):
                return int(line.split()[1]) * 1024
    raise Exception("cannot get memory usage by pid.")


class AtomicInt:
    def __init__(self, value=0):
        self._value = value
        self._lock = Lock()

    def increase(self, value=1):
        with self._lock:
            self._value += value

    def decrease(self, value=1):
        with self._lock:
            self._value -= value

    def get_value(self):
        with self._lock:
            return self._value
