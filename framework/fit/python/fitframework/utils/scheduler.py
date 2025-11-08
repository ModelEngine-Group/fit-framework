# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：任务定时执行器。配合@scheduled_executor使用
"""
import asyncio
import time
import threading
from contextlib import suppress

from fitframework.api.logging import fit_logger
from fitframework.api.decorators import register_event
from fitframework.api.enums import FrameworkEvent

# 定时任务定义表 - key: func_ref, value: period
_task_defs = {}

# 全局定时器类
_timer = None


class TimerDict:
    """
        提供可设置失效时间的dict
    """

    def __init__(self, hash_func=hash, valid_time=60):
        """

        Args:
            hash_func ():
            valid_time (): 单位s
        """
        self._dict = dict()
        self._hash_func = hash_func
        self._valid_time = valid_time

    def __contains__(self, item):
        hash_key = self._hash_func(item)
        put_time = self._dict.get(hash_key)
        if put_time is None:
            return False
        if self._is_expired(put_time):
            del self._dict[hash_key]
            return False
        return True

    def put(self, item):
        """
        将item放入缓存中
        Args:
            item ():

        Returns:

        """
        self._dict[self._hash_func(item)] = time.time()

    def clear(self):
        """
        清空dict
        Returns:

        """
        self._dict.clear()

    def remove(self, item):
        """
        移除该item
        Args:
            item ():

        Returns:

        """
        hash_key = self._hash_func(item)
        del self._dict[hash_key]

    def clear_expired_cache(self):
        """
        清空已超时的缓存
        Returns:

        """
        for key, put_time in self._dict.items():
            if self._is_expired(put_time):
                del self._dict[key]

    def _is_expired(self, put_time):
        return time.time() - put_time > self._valid_time


def register(func_ref, period: int):
    if period <= 0:
        return
    _task_defs[func_ref] = period


@register_event(FrameworkEvent.APPLICATION_STARTED)
def _start():
    global _timer
    _timer = _Timer()
    fit_logger.info("timer started")


@register_event(FrameworkEvent.FRAMEWORK_STOPPING)
def _stop():
    global _timer
    if _timer is not None:
        _timer.cancel()
        fit_logger.info("timer stopped")
    else:
        fit_logger.warning("timer was not initialized, skip stopping")


class _Timer:
    """ 独立线程定时器类，用于启动和结束定时任务 """

    def __init__(self):
        self._mark_cancel = False
        threading.Thread(target=self._scheduler_start, name='SchedulerThread').start()

    def cancel(self):
        self._mark_cancel = True

    def _scheduler_start(self):
        async def _start_all():
            for func_ref, period in _task_defs.items():
                _TimedTask(period, func_ref)
            while not self._mark_cancel:
                await asyncio.sleep(0.5)

        asyncio.run(_start_all())


class _TimedTask:
    """ 每隔period定时执行任务 """

    def __init__(self, period, func_ref):
        self._task = asyncio.create_task(self._job(func_ref, period))

    def cancel(self):
        self._task.cancel()

    async def _job(self, func_ref, timeout):
        await asyncio.sleep(timeout)
        with suppress(Exception):
            func_ref()
        self._task = asyncio.create_task(self._job(func_ref, timeout))
