# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import functools
import json
from typing import List, Any, Optional, Callable, Union
from langchain_core.runnables import RunnableConfig
from langchain_core.tools import BaseTool

from fitframework import fit_logger
from fitframework.core.repo.fitable_register import register_fitable


def register_function_tools(tools: List[BaseTool],
                            config: Optional[RunnableConfig] = None,
                            **kwargs: Any):
    """
    langchain 函数工具注册方法，注册无需 api key 的工具。

    Args:
        tools (List[BaseTool]): 表示 langchain 工具列表。
        config (Optional[RunnableConfig]): 表示 langchain runnable 配置信息。
        **kwargs (Any): 表示额外参数。
    """
    for tool in tools:
        register_api_tools(tool, [], tool.name, config, **kwargs)


def _pop_api_keys(input_args: dict, api_keys_name: List[str]) -> dict:
    if all(key in input_args.keys() for key in api_keys_name):
        api_keys_values = [input_args.pop(key) for key in api_keys_name]
        return dict(zip(api_keys_name, api_keys_values))
    else:
        raise ValueError(f"{input_args} not contain all api keys in {api_keys_name}")


def _invoke(input_args: dict, tool_builder: Union[Callable[[dict], BaseTool], BaseTool],
           extra_keys: List[str],
           config: Optional[RunnableConfig] = None,
           **kwargs: Any) -> str:
    api_keys = _pop_api_keys(input_args, extra_keys)
    if "__arg1" in input_args:
        _input_args = input_args["__arg1"]
    else:
        _input_args = input_args

    if isinstance(tool_builder, BaseTool):
        tool = tool_builder
    else:
        tool = tool_builder(api_keys)

    try:
        tool_ans = tool.invoke(_input_args, config, **kwargs)
        return _dump_ans_to_str(tool_ans)
    except BaseException:
        return ""


def _dump_ans_to_str(tool_ans):
    if not isinstance(tool_ans, str):
        try:
            content = json.dumps(tool_ans, ensure_ascii=False)
        except Exception:
            content = str(tool_ans)
    else:
        content = tool_ans
    return content


def register_api_tools(tool_builder: Union[Callable[[dict], BaseTool], BaseTool],
                       extra_keys: List[str],
                       tool_name: str,
                       config: Optional[RunnableConfig] = None,
                       **kwargs: Any):
    """
    langchain api 工具注册方法。

    Args:
        tool_builder (Callable[[dict], BaseTool]): 表示 api 工具构造器。
        extra_keys (List[str]): 表示工具的额外参数，如 api key。
        tool_name (str): 工具名称。
        config (Optional[RunnableConfig]): 表示 langchain runnable 配置信息。
        **kwargs (Any): 表示额外参数。
    """
    tool_invoke = functools.partial(_invoke, tool_builder=tool_builder, extra_keys=extra_keys,
                                    config=config, **kwargs)
    tool_invoke.__module__ = register_api_tools.__module__
    tool_invoke.__annotations__ = {
        'input_args': dict,
        'return': str
    }
    generic_id = 'langchain.tool'
    register_fitable(generic_id, tool_name, False, [], tool_invoke)
    fit_logger.info("register: generic_id = %s, fitable_id = %s", generic_id, tool_name, stacklevel=2)
