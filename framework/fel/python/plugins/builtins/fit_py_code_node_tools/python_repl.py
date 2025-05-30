# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================

import multiprocessing
import os
from typing import Dict
import threading

from fitframework.api.decorators import fitable, value as FitConfigValue
from fitframework.api.logging import fit_logger
from fitframework.core.exception.fit_exception import FitException, InternalErrorCode
from fitframework.utils.tools import to_list

from .python_repl_impl import execute_node_impl, GLOBAL_CONFIG


@FitConfigValue(key='user.function.entrypoint', default_value='main')
def _read_entrypoint_from_config():
    pass


@FitConfigValue(key='code.import.whitelist', default_value=['asyncio', 'json', 'numpy', 'typing'], converter=to_list)
def _read_import_whitelist_from_config():
    pass


@FitConfigValue(key='code.import.blacklist',
                default_value=['os', 'sys', 'cmd', 'subprocess', 'multiprocessing', 'timeit', 'platform'],
                converter=to_list)
def _read_import_blacklist_from_config():
    pass


@FitConfigValue(key='code.timeout', default_value=10, converter=int)
def _timeout():
    pass


@FitConfigValue(key='code.max_pool', default_value=4, converter=int)
def _max_pool():
    pass


@FitConfigValue(key='code.mem_limit', default_value=181*1024*1024, converter=int)
def _mem_limit():
    pass


@FitConfigValue(key='code.verbose', default_value=False, converter=bool)
def _verbose():
    pass


def _init_config():
    GLOBAL_CONFIG["entrypoint"] = _read_entrypoint_from_config()
    GLOBAL_CONFIG["whitelist"] = _read_import_whitelist_from_config()
    GLOBAL_CONFIG["blacklist"] = _read_import_blacklist_from_config()
    GLOBAL_CONFIG["timeout"] = _timeout()
    GLOBAL_CONFIG["max_pool"] = _max_pool()
    GLOBAL_CONFIG["mem_limit"] = _mem_limit()
    GLOBAL_CONFIG["verbose"] = _verbose()


class Singleton(type):
    _lock = threading.Lock()

    def __init__(cls, *args, **kwargs):
        cls._instance = None
        super().__init__(*args, **kwargs)

    def __call__(cls, *args, **kwargs):
        if cls._instance:
            return cls._instance

        with cls._lock:
            if not cls._instance:
                cls._instance = super().__call__(*args, **kwargs)

        return cls._instance


class CodeExecutor(metaclass=Singleton):
    def __init__(self):
        _init_config()
        self.pools = []
        for _ in range(GLOBAL_CONFIG["max_pool"]):
            lock = threading.Lock()
            pool = multiprocessing.Pool(processes=1)
            self.pools.append((lock, pool))
        self.index = 0
        self.index_lock = threading.Lock()
        self.config = GLOBAL_CONFIG

    def get_and_increment(self) -> int:
        with self.index_lock:
            i = self.index
            self.index = i + 1 if i < self.config["max_pool"] - 1 else 0
            return i


def _print_process_usage():
    import psutil
    # Get the current process ID
    pid = os.getpid()

    # Create a Process object for the current process
    process = psutil.Process(pid)

    # Get the CPU and memory usage of the current process
    cpu_usage = process.cpu_percent(interval=1.0)  # This returns the CPU usage as a percentage
    memory_info = process.memory_info()  # Returns memory usage as a named tuple (rss, vms)

    # rss (Resident Set Size) - the non-swapped physical memory the process has used
    # vms (Virtual Memory Size) - the total memory the process can access
    memory_usage = memory_info.rss / (1024 * 1024)  # Convert to MB
    virtual_memory = memory_info.vms / (1024 * 1024)  # Convert to MB

    # Print CPU and memory usage
    fit_logger.info(f"CPU Usage: {cpu_usage}%, Memory Usage (RSS): {memory_usage:.2f} MB, "
                    f"Virtual Memory Usage (VMS): {virtual_memory:.2f} MB")

    current_process = psutil.Process()
    children = current_process.children(recursive=True)
    for child in children:
        fit_logger.info('Child pid is {}'.format(child.pid))


@fitable("CodeNode.tool", "Python_REPL")
def execute_code(args: Dict[str, object], code: str) -> object:
    # 由于插件初始化时使用守护进程，无法拉起进程池中的进程，选择在初次调用时初始化进程池
    executor = CodeExecutor()
    if GLOBAL_CONFIG["verbose"]:
        _print_process_usage()
    res = execute_node_impl(executor.pools, executor.get_and_increment(), args, code, GLOBAL_CONFIG)
    if res.isOk:
        return res.value
    raise FitException(res.error_code, res.msg)