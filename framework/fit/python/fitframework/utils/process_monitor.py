# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：进程监控和健康检查工具
"""
import time
from typing import Optional

import psutil

from fitframework.api.logging import fit_logger


class ProcessMonitor:
    """进程监控器，用于监控主进程的健康状态"""
    
    def __init__(self, pid: int, max_memory_mb: int = 1024, max_cpu_percent: float = 80.0):
        self.pid = pid
        self.max_memory_mb = max_memory_mb
        self.max_cpu_percent = max_cpu_percent
        self.start_time = time.time()
        self.last_check_time = time.time()
        
    def is_process_alive(self) -> bool:
        """检查进程是否还活着"""
        try:
            return psutil.pid_exists(self.pid)
        except Exception as e:
            fit_logger.warning(f"Error checking process {self.pid}: {e}")
            return False
    
    def get_process_info(self) -> Optional[dict]:
        """获取进程信息"""
        try:
            if not self.is_process_alive():
                return None
                
            process = psutil.Process(self.pid)
            return {
                'pid': self.pid,
                'memory_mb': process.memory_info().rss / 1024 / 1024,
                'cpu_percent': process.cpu_percent(),
                'status': process.status(),
                'create_time': process.create_time(),
                'num_threads': process.num_threads()
            }
        except Exception as e:
            fit_logger.warning(f"Error getting process info for {self.pid}: {e}")
            return None
    
    def is_healthy(self) -> bool:
        """检查进程是否健康"""
        info = self.get_process_info()
        if not info:
            return False
            
        # 检查内存使用
        if info['memory_mb'] > self.max_memory_mb:
            fit_logger.warning(f"Process {self.pid} memory usage too high: {info['memory_mb']:.2f}MB > {self.max_memory_mb}MB")
            return False
            
        # 检查CPU使用（需要两次采样）
        cpu_percent = info['cpu_percent']
        if cpu_percent > self.max_cpu_percent:
            fit_logger.warning(f"Process {self.pid} CPU usage too high: {cpu_percent:.2f}% > {self.max_cpu_percent}%")
            return False
            
        return True
    
    def should_restart(self) -> bool:
        """判断是否应该重启进程"""
        if not self.is_process_alive():
            fit_logger.error(f"Process {self.pid} is not alive")
            return True
            
        if not self.is_healthy():
            fit_logger.warning(f"Process {self.pid} is unhealthy")
            return True
            
        return False


def create_process_monitor(pid: int) -> ProcessMonitor:
    """创建进程监控器"""
    return ProcessMonitor(pid)
