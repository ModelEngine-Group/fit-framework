# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================

import time
from typing import List, Any, Optional, Callable, Union, Tuple

from .callable_registers import register_callable_tool


def llamaindex_network(**kwargs) -> str:
    time.sleep(5)
    return ""


# Tuple 结构： (tool_func, config_args, return_description)
network_toolkit: List[Tuple[Callable[..., Any], List[str], str]] = [
    (llamaindex_network, ["input"], "Youtube search.")
]


for tool in network_toolkit:
    register_callable_tool(tool, llamaindex_network.__module__, "llama_index.rag.toolkit")

