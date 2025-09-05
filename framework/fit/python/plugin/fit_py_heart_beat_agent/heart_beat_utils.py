# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：心跳工具
"""
from concurrent.futures.thread import ThreadPoolExecutor
from functools import wraps
from concurrent.futures._base import TimeoutError as future_TimeoutError
from fitframework.api.logging import sys_plugin_logger


def timeout_or_exception_retry(timeout: int = 3, a_exception=Exception, max_retry: int = 1):
    """

    :param timeout: 一次函数调用的超时时间 单位 s
    :param a_exception: 函数调用可能抛出的异常
    :param max_retry: 重试次数
    :return:
    """

    def decorator(fun):
        executor = ThreadPoolExecutor(max_workers=10)

        @wraps(fun)
        def wrapper(*args, **kwargs):
            count = 0
            while count < max_retry:
                try:
                    submit = executor.submit(fun, *args, **kwargs)
                    return submit.result(timeout=timeout)
                except future_TimeoutError as e:
                    sys_plugin_logger.error(f"time out {count}")
                    count += 1

                except a_exception as e:
                    sys_plugin_logger.error(f"exception  {count}, message:{e}")
                    count += 1
            sys_plugin_logger.error(f'after {count} retry, fail')
            raise Exception(f'after {count} retry ,{fun.__name__} fail') from None

        return wrapper

    return decorator
