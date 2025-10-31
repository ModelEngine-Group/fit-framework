# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：系统健康监控插件
"""
import time
from typing import Dict, Any

import psutil

from fitframework.api.decorators import fitable, fit, value
from fitframework.api.decorators import register_event
from fitframework.api.enums import FrameworkEvent
from fitframework.api.logging import sys_plugin_logger


@value('health-monitor.check-interval', 30, converter=int)
def get_check_interval():
    """健康检查间隔（秒）"""
    pass


@value('health-monitor.memory-threshold', 80.0, converter=float)
def get_memory_threshold():
    """内存使用率阈值（百分比）"""
    pass


@value('health-monitor.cpu-threshold', 90.0, converter=float)
def get_cpu_threshold():
    """CPU使用率阈值（百分比）"""
    pass


@value('health-monitor.disk-threshold', 90.0, converter=float)
def get_disk_threshold():
    """磁盘使用率阈值（百分比）"""
    pass


class HealthMonitor:
    """系统健康监控器"""
    
    def __init__(self):
        self.last_check_time = time.time()
        self.health_status = {}
        self.alert_count = 0
        self.max_alerts = 10
        
    def check_system_health(self) -> Dict[str, Any]:
        """检查系统健康状态"""
        try:
            # 内存使用率
            memory = psutil.virtual_memory()
            memory_percent = memory.percent
            
            # CPU使用率
            cpu_percent = psutil.cpu_percent(interval=1)
            
            # 磁盘使用率
            disk = psutil.disk_usage('/')
            disk_percent = (disk.used / disk.total) * 100
            
            # 进程信息
            current_process = psutil.Process()
            process_memory = current_process.memory_info().rss / 1024 / 1024  # MB
            process_cpu = current_process.cpu_percent()
            
            health_status = {
                'timestamp': time.time(),
                'memory': {
                    'total': memory.total,
                    'available': memory.available,
                    'percent': memory_percent,
                    'healthy': memory_percent < get_memory_threshold()
                },
                'cpu': {
                    'percent': cpu_percent,
                    'healthy': cpu_percent < get_cpu_threshold()
                },
                'disk': {
                    'total': disk.total,
                    'used': disk.used,
                    'free': disk.free,
                    'percent': disk_percent,
                    'healthy': disk_percent < get_disk_threshold()
                },
                'process': {
                    'memory_mb': process_memory,
                    'cpu_percent': process_cpu,
                    'pid': current_process.pid,
                    'status': current_process.status()
                }
            }
            
            # 检查是否健康
            is_healthy = (
                health_status['memory']['healthy'] and
                health_status['cpu']['healthy'] and
                health_status['disk']['healthy']
            )
            
            health_status['overall_healthy'] = is_healthy
            
            if not is_healthy:
                self.alert_count += 1
                self._log_health_alert(health_status)
            else:
                self.alert_count = 0  # 重置告警计数
                
            self.health_status = health_status
            self.last_check_time = time.time()
            
            return health_status
            
        except Exception as e:
            sys_plugin_logger.error(f"Error checking system health: {e}")
            return {'error': str(e), 'overall_healthy': False}
    
    def _log_health_alert(self, health_status: Dict[str, Any]):
        """记录健康告警"""
        if self.alert_count > self.max_alerts:
            return  # 避免日志过多
            
        alerts = []
        if not health_status['memory']['healthy']:
            alerts.append(f"Memory usage: {health_status['memory']['percent']:.1f}%")
        if not health_status['cpu']['healthy']:
            alerts.append(f"CPU usage: {health_status['cpu']['percent']:.1f}%")
        if not health_status['disk']['healthy']:
            alerts.append(f"Disk usage: {health_status['disk']['percent']:.1f}%")
            
        if alerts:
            sys_plugin_logger.warning(f"System health alert #{self.alert_count}: {', '.join(alerts)}")
    
    def get_health_summary(self) -> Dict[str, Any]:
        """获取健康状态摘要"""
        return {
            'last_check': self.last_check_time,
            'alert_count': self.alert_count,
            'status': self.health_status.get('overall_healthy', False),
            'details': self.health_status
        }


# 全局健康监控器实例
_health_monitor = HealthMonitor()


@fitable("modelengine.fit.health.check", "local-worker")
def check_health() -> Dict[str, Any]:
    """执行健康检查"""
    return _health_monitor.check_system_health()


@fit("modelengine.fit.health.status")
def get_health_status() -> Dict[str, Any]:
    """获取健康状态"""
    return _health_monitor.get_health_summary()


@register_event(FrameworkEvent.APPLICATION_STARTED)
def start_health_monitoring():
    """启动健康监控"""
    sys_plugin_logger.info("Health monitoring started")
    # 执行初始健康检查
    _health_monitor.check_system_health()


@register_event(FrameworkEvent.FRAMEWORK_STOPPING)
def stop_health_monitoring():
    """停止健康监控"""
    sys_plugin_logger.info("Health monitoring stopped")
