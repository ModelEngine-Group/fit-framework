# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：FIT框架初始化启动加载入口：加载引导插件，启动bootstrap
"""
import os
import platform
import sys
import time
import traceback
from multiprocessing import Process

sys.path.insert(0, os.path.realpath('third_party'))

import argparse
import configparser
from functools import reduce
from typing import List
import yaml
from fitframework import const
from fitframework.api.decorators import fit, fitable, local_context, private_fit, timer, shutdown_on_error, \
    state_broadcast, value
from fitframework.api.logging import fit_logger
from fitframework.domain.plugin import Plugin
from fitframework.api.enums import PluginType, FrameworkState
from fitframework.utils import load_module
from fitframework.utils.context import runtime_context
from fitframework.utils.tools import to_list, to_bool, dash_style, multi_maps_exhaust, get_memory_usage
from fitframework.utils.plugin_utils import get_all_plugins_info

# 日志配置文件
LOGGING_CONF_FILE_NAME = 'logging.conf'

# 所有目录参数的默认值配置文件
STARTUP_YML = 'fit_startup.yml'

# Key为：Fit公共结构体目录, Fit依赖的三方包目录的参数名
_STARTUP_PLUGINS_BOOTSTRAP_KEY = 'bootstrap'
_STARTUP_PLUGINS_USER_LOCATION_FULL_KEY = \
    f"{const.STARTUP_PLUGINS_KEY}.{const.STARTUP_PLUGINS_USER_KEY}.{const.STARTUP_PLUGINS_LOCATION_KEY}"

# 框架版本
_FIT_FRAMEWORK_VERSION = "3.6.1.dev"

_LOGO = """
-----------------------------------------
      ___                                
     /  /\        ___             ___    
    /  / /_      /  /\           /  /\   
   /  / / /\    /  / /          /  / /   
  /  / / / /   /__/  \         /  / /    
 /__/ / / /    \__\/\ \__     /  /  \    
 \  \ \/ /        \  \ \/\   /__/ /\ \   
  \  \  /          \__\  /   \__\/  \ \  
   \  \ \          /__/ /         \  \ \ 
    \  \ \         \__\/           \__\/ 
     \__\/                               
-----------------------------------------"""


@fit(const.RUNTIME_STATE_UPDATE_GEN_ID)
def update_state(new_state: str) -> None:
    pass


@state_broadcast(update_state, None, FrameworkState.RUNNING)
@fit(const.BOOTSTRAP_START_GEN_ID)
def bootstrap_start(plugins: List, config_folder: str) -> bool:
    pass


@fit(const.RUNTIME_SHUTDOWN_GEN_ID)
def shutdown() -> None:
    pass


def _init_log():
    import logging.config
    with open(os.path.join(runtime_context.get_item(const.CONFIG_FOLDER_ARG_NAME), LOGGING_CONF_FILE_NAME),
              encoding='utf-8') as f:
        config = configparser.ConfigParser()
        config.read_file(f)  # 日志相关对象、配置初始化
        dir_ = runtime_context.get_item('logging-out-dir')
        if dir_:
            os.makedirs(dir_, exist_ok=True)
            config["DEFAULT"]["logging-out-dir"] = dir_
        logging.config.fileConfig(config)


def _init_contexts():
    user_args = parse_arguments()
    _runtime_context_add_startup_context(user_args.pop(const.CONFIG_FOLDER_ARG_NAME))
    _runtime_context_add_and_consume_user_context(user_args)


def _runtime_context_add_startup_context(startup_conf_folder):
    def _consume_startup_conf():
        runtime_context.add_context(dict([
            (const.CONFIG_FOLDER_ARG_NAME, startup_conf_folder),
            (const.STARTUP_FRAMEWORK_FOLDER_KEY, _full_path(startup_conf.pop(
                const.STARTUP_FRAMEWORK_FOLDER_KEY))),
            *startup_conf.items(), ]))

    if not startup_conf_folder:
        startup_conf_folder = _full_path('conf')
    with open(os.path.join(startup_conf_folder, STARTUP_YML), encoding='utf-8') as f:
        startup_conf = yaml.safe_load(f)
    _consume_startup_conf()


def _full_path(*path_components):
    return os.path.realpath(os.path.join(os.path.dirname(__path__[0]), *path_components))


def _runtime_context_add_and_consume_user_context(user_args):
    runtime_context.add_context(dict(os.environ))
    plugin_dir_s = user_args.pop(_STARTUP_PLUGINS_USER_LOCATION_FULL_KEY)
    if plugin_dir_s:
        _runtime_context_add_user_plugin_context(plugin_dir_s)
    runtime_context.add_context({k: v for k, v in user_args.items() if v is not None})


def _runtime_context_add_user_plugin_context(plugin_dir_s):
    plugins_info = runtime_context.get_item(const.STARTUP_PLUGINS_KEY)
    # unpack an iterator, to avoid creating this list
    plugins_info[const.STARTUP_PLUGINS_USER_KEY] = \
        [*_cur_user_plugins_info(plugins_info), *_new_user_plugins_info(plugin_dir_s)]


def _new_user_plugins_info(plugin_dir_s):
    key = const.STARTUP_PLUGINS_LOCATION_KEY
    # 针对每一个处理dir1,dir2,...的格式
    return ({key: dir_} for dir_ in reduce(list.__add__, (_.strip('\'\"').split(',') for _ in plugin_dir_s)))


def _cur_user_plugins_info(plugins_info):
    user_plugins_info = plugins_info.get(const.STARTUP_PLUGINS_USER_KEY)
    if user_plugins_info:
        return user_plugins_info
    else:
        return []


def parse_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument(dash_style(_STARTUP_PLUGINS_USER_LOCATION_FULL_KEY), action='append',
                        help='Folders to specify extra user plugins. default is empty.')
    parser.add_argument(dash_style(const.CONFIG_FOLDER_ARG_NAME),
                        help='Folders for fitframework local configurations.')
    command_line_args, extra_args = parser.parse_known_args()
    return dict([*vars(command_line_args).items(), *(arg.split('=', 1) for arg in extra_args)])


@local_context("terminate-main.enabled", converter=to_bool)
def get_terminate_main_enabled():
    pass


@fit("modelengine.fit.get.should.terminate.main")
def get_should_terminate_main() -> bool:
    """
    判断是否需要终结主进程。
    特别注意：框架本身并不会实现该接口，如果希望实现按特定条件的重启，需要通过 fitable 的方式提供该实现并在其中添加判断逻辑。

    :return: 判断结果，True 表示需要重启，反之不需要。
    """
    pass


@fit("HEART_BEAT_EXIT_UNEXPECTEDLY_GEN_ID")
def heart_beat_exit_unexpectedly() -> bool:
    """
    判断心跳进程是否意外退出。

    :return: 判断结果，True 表示已经意外退出。
    """
    pass


def _safe_check(checker, desc: str) -> bool:
    """
    安全执行checker，异常时打印日志并返回False
    """
    try:
        return checker()
    except:
        except_type, except_value, except_traceback = sys.exc_info()
        fit_logger.warning(f"check {desc} error, error type: {except_type}, "
                           f"value: {except_value}, trace back:\n"
                           f"{''.join(traceback.format_tb(except_traceback))}")
        return False


@shutdown_on_error
@timer
def main():
    _init_contexts()
    _init_log()

    # 启动框架时先加载bootstrap目录下的模块
    bootstrap_plugins_info = runtime_context.get_item(const.STARTUP_PLUGINS_BOOTSTRAP_KEY)
    load_module.load_bootstraps([plugin[const.STARTUP_PLUGINS_LOCATION_KEY] for plugin in bootstrap_plugins_info])
    plugins_info = get_all_plugins_info()
    bootstrap_start(plugins_info, runtime_context.get_item(const.CONFIG_FOLDER_ARG_NAME))
    fit_logger.info(_LOGO)
    fit_logger.info(f"fit framework is now available in version {_FIT_FRAMEWORK_VERSION}.")
    if get_terminate_main_enabled():
        fit_logger.info("terminate main enabled.")
        while True:
            # 明确区分退出原因并打印日志
            hb_exit = _safe_check(heart_beat_exit_unexpectedly, "heart_beat_exit_unexpectedly")
            should_terminate = _safe_check(get_should_terminate_main, "get_should_terminate_main")
            if hb_exit:
                fit_logger.warning("main process will exit due to heartbeat background job exited unexpectedly.")
                break
            if should_terminate:
                # 详细原因已在 terminate_main 插件内部按条件分别打印，这里汇总打印一次
                fit_logger.info("main process will exit due to terminate-main condition matched.")
                break
            time.sleep(1)
        fit_logger.info("main process terminated.")
        shutdown()


if __package__ == 'fitframework' and sys.argv[0].find("pytest") == -1:  # 避免执行两次main
    if platform.system() in ('Windows', 'Darwin'):  # Windows 或 macOS
        main()
    else:  # Linux 及其他
        from fitframework.utils.restart_policy import create_default_restart_policy

        restart_policy = create_default_restart_policy()
        fit_logger.info(f"Starting process manager with restart policy: {restart_policy.get_status()}")

        while True:
            exit_code = None
            try:
                main_process = Process(target=main, name='MainProcess')
                main_process.start()
                fit_logger.info(f"Main process started with PID: {main_process.pid}")
                main_process.join()
                exit_code = main_process.exitcode
            except Exception as e:
                fit_logger.error(f"Error during process management: {e}")
                exit_code = -1

            fit_logger.info(f"Main process exited with code: {exit_code}")
            # 使用重启策略判断是否应该重启
            if not restart_policy.should_restart(exit_code):
                fit_logger.info("Restart policy indicates no restart needed, stopping")
                break

            # 获取重启延迟
            restart_delay = restart_policy.get_restart_delay()
            status = restart_policy.get_status()

            fit_logger.warning(f"Main process exited unexpectedly, restarting in {restart_delay:.2f} seconds... "
                               f"(attempt {status['current_attempt']}/{status['max_attempts']})")
            time.sleep(restart_delay)
