# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：进程重启策略配置
"""
import time
from typing import Dict, Any
from fitframework.api.logging import fit_logger


class RestartPolicy:
    """进程重启策略"""
    
    def __init__(self, 
                 max_attempts: int = 10,
                 base_delay: float = 5.0,
                 max_delay: float = 300.0,
                 backoff_multiplier: float = 1.5,
                 reset_after_success: bool = True):
        self.max_attempts = max_attempts
        self.base_delay = base_delay
        self.max_delay = max_delay
        self.backoff_multiplier = backoff_multiplier
        self.reset_after_success = reset_after_success
        
        self.current_attempt = 0
        self.current_delay = base_delay
        self.last_success_time = time.time()
        
    def should_restart(self, exit_code: int) -> bool:
        """判断是否应该重启"""
        # 正常退出不重启
        if exit_code == 0:
            if self.reset_after_success:
                self._reset()
            return False
            
        # 超过最大尝试次数不重启
        if self.current_attempt >= self.max_attempts:
            fit_logger.error(f"Maximum restart attempts ({self.max_attempts}) reached")
            return False
            
        return True
    
    def get_restart_delay(self) -> float:
        """获取重启延迟时间"""
        delay = min(self.current_delay, self.max_delay)
        self.current_attempt += 1
        
        # 指数退避
        if self.current_attempt < self.max_attempts:
            self.current_delay = min(self.current_delay * self.backoff_multiplier, self.max_delay)
            
        return delay
    
    def _reset(self):
        """重置策略状态"""
        self.current_attempt = 0
        self.current_delay = self.base_delay
        self.last_success_time = time.time()
        
    def get_status(self) -> Dict[str, Any]:
        """获取当前状态"""
        return {
            'current_attempt': self.current_attempt,
            'max_attempts': self.max_attempts,
            'current_delay': self.current_delay,
            'base_delay': self.base_delay,
            'max_delay': self.max_delay,
            'last_success_time': self.last_success_time
        }


def create_default_restart_policy() -> RestartPolicy:
    """创建默认重启策略"""
    return RestartPolicy(
        max_attempts=10,
        base_delay=5.0,
        max_delay=300.0,
        backoff_multiplier=1.5,
        reset_after_success=True
    )


def create_aggressive_restart_policy() -> RestartPolicy:
    """创建激进重启策略（快速重启）"""
    return RestartPolicy(
        max_attempts=20,
        base_delay=2.0,
        max_delay=60.0,
        backoff_multiplier=1.2,
        reset_after_success=True
    )


def create_conservative_restart_policy() -> RestartPolicy:
    """创建保守重启策略（慢速重启）"""
    return RestartPolicy(
        max_attempts=5,
        base_delay=10.0,
        max_delay=600.0,
        backoff_multiplier=2.0,
        reset_after_success=True
    )
