# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import asyncio
import importlib
import inspect
import json
import multiprocessing
import platform
import re
from typing import Any, Dict, List, Tuple
from pydantic import BaseModel

if platform.system() == 'Windows':
    from enum import IntEnum

    class InternalErrorCode(IntEnum):
        EXCEPTION_FROM_USER_CODE_OCCURRED = 0x7F000105
        TIME_OUT_EXCEPTION_FROM_USER_CODE_OCCURRED = 0x7F000106  # java 不存在
else:
    from fitframework.core.exception.fit_exception import InternalErrorCode
try:
    import resource
except ImportError:
    resource = None
try:
    from .safe_global import safe_builtins
except ImportError as e:
    from safe_global import safe_builtins

_PYTHON_REPL_HEADER = '''
import json
from typing import Any

Output = Any


'''

GLOBAL_CONFIG = \
    {
        "header": _PYTHON_REPL_HEADER,
        "header_len": len(_PYTHON_REPL_HEADER.split('\n')),
        "entrypoint": 'main',
        "whitelist": ['json', 'typing'],
        "blacklist": ['os', 'sys', 'cmd', 'subprocess', 'multiprocessing', 'timeit', 'platform'],
        "timeout": 10,
        "max_pool": 4,
        "mem_limit": 181 * 1024 * 1024,
        "verbose": False
    }


class Result(BaseModel):
    isOk: bool
    value: Any = None
    error_code: int
    msg: str = None

    @staticmethod
    def ok(data: Any) -> 'Result':
        return Result(isOk=True, value=data, error_code=0)

    @staticmethod
    def err(err_code: int, err_msg: str) -> 'Result':
        return Result(isOk=False, error_code=err_code, msg=err_msg)


# 创建一个安全的执行环境
def _create_restricted_exec_env(config: Dict[str, object]):
    def safer_import(name, my_globals=None, my_locals=None, fromlist=(), level=0):
        if name not in config['whitelist'] or name in config['blacklist']:
            raise NameError(f'model {name} is not valid')
        return importlib.import_module(name)

    safe_globals = {
        '__builtins__': {
            **safe_builtins,
            '__import__': safer_import,
            'Args': Dict
        }
    }
    return safe_globals


# 获取内存使用（单位：kB）
def _get_current_memory_usage():
    with open('/proc/self/status') as f:
        mem_usage = f.read().split('VmPeak:')[1].split('\n')[0].strip()
    return int(mem_usage.split()[0].strip())


# 执行受限代码
def _execute_code_with_restricted_python(args: Dict[str, object], code: str, config: Dict[str, object]):
    if resource:
        resource.setrlimit(resource.RLIMIT_AS, (GLOBAL_CONFIG["mem_limit"], GLOBAL_CONFIG["mem_limit"]))
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    try:
        full_python_code = (f"{config['header']}"
                            f'{code}\n\n')

        safer_globals = _create_restricted_exec_env(config)
        exec(full_python_code, safer_globals)
        entrypoint = config['entrypoint']
        if (entrypoint not in safer_globals or
                not inspect.isfunction(safer_globals.get(entrypoint))):
            raise NameError("main function not defined")
        entrypoint = safer_globals.get(entrypoint)
        if inspect.iscoroutinefunction(entrypoint):
            ret = loop.run_until_complete(asyncio.wait_for(entrypoint(args), config['timeout']))
            return Result.ok(json.dumps(ret))
        else:
            return Result.err(InternalErrorCode.EXCEPTION_FROM_USER_CODE_OCCURRED.value,
                              "Unable to execute non-asynchronous function")
    except asyncio.TimeoutError:
        return Result.err(InternalErrorCode.TIME_OUT_EXCEPTION_FROM_USER_CODE_OCCURRED.value,
                          "[TimeoutError] Execution timed out")
    except Exception as err:
        return Result.err(InternalErrorCode.EXCEPTION_FROM_USER_CODE_OCCURRED.value, _get_except_msg(err, config))
    finally:
        loop.close()


def _get_except_msg(error: Any, config: Dict[str, object]) -> str:
    if isinstance(error, SyntaxError):
        error_msg = f"{error.msg} at line {error.lineno - config['header_len']}, column {error.offset}: {error.text}"
    elif isinstance(error, KeyError):
        error_msg = f"key {str(error)} do not exist"
    else:
        error_msg = str(error)
    return f"[{error.__class__.__name__}] {error_msg}"


def _get_free_process_pool(pools: List[Tuple[multiprocessing.Lock, multiprocessing.Pool]], index):
    lock = pools[index][0]
    if lock.acquire():
        return lock
    raise multiprocessing.TimeoutError()


def execute_node_impl(pools: List[Tuple[multiprocessing.Lock, multiprocessing.Pool]], index: int,
                      args: Dict[str, object], code: str, config: Dict[str, object]):
    match = _validate_escape(code)
    if match is not None:
        return Result.err(InternalErrorCode.EXCEPTION_FROM_USER_CODE_OCCURRED.value,
                          f'{match.group()} is not allowed in code node')
    lock = _get_free_process_pool(pools, index)
    pool = pools[index][1]
    try:
        result = pool.apply_async(_execute_code_with_restricted_python, args=[args, code, config])
        return result.get(config['timeout'])
    except multiprocessing.TimeoutError:
        index = pools.index((lock, pool))
        pool.terminate()
        pools[index] = (lock, multiprocessing.Pool(processes=1))
        return Result.err(InternalErrorCode.TIME_OUT_EXCEPTION_FROM_USER_CODE_OCCURRED.value,
                          "[TimeoutError] Execution timed out")
    finally:
        lock.release()


def _validate_escape(code: str) -> bool:
    # 校验代码中是否存在获取栈帧的字段，禁用可能用于沙箱逃逸的端
    pattern = r'.gi_frame|.tb_frame|__[a-zA-Z]+__'
    return re.search(pattern, code)