# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import functools
from inspect import signature
from typing import Callable, Any, Tuple, List

from fitframework import fit_logger
from fitframework.core.repo.fitable_register import register_fitable


def __invoke_tool(input_args: dict, tool_func: Callable[..., Any], **kwargs) -> Any:
    return tool_func(**input_args, **kwargs)


def register_callable_tool(tool: Tuple[Callable[..., Any], List[str], str], module: str, generic_id: str):
    func = tool[0]
    fitable_id = f"{func.__name__}"

    tool_invoke = functools.partial(__invoke_tool, tool_func=func)
    tool_invoke.__module__ = module
    tool_invoke.__annotations__ = {
        'input_args': dict,
        'return': signature(func).return_annotation
    }
    register_fitable(generic_id, fitable_id, False, [], tool_invoke)
    fit_logger.info("register: generic_id = %s, fitable_id = %s", generic_id, fitable_id, stacklevel=2)
