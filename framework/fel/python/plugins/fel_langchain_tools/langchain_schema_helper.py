# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import json
import os
import stat
from typing import List

from langchain_core.tools import BaseTool
from langchain_core.utils.function_calling import convert_to_openai_function


def dump_schema(function_tools: List[BaseTool], file_path: str):
    """
    导出 langchain 函数工具 schema 的工具方法。

    Args:
        function_tools (List[BaseTool]): 表示 langchain 工具列表。
        file_path (str): 表示 schema 文件的导出路径。
    """
    tools_schema = [{
        "runnables": {"langchain": {"genericableId": "langchain.tool", "fitableId": f"{tool.name}"}},
        "schema": {**convert_to_openai_function(tool), "return": {"type": "string"}}
    } for tool in function_tools]

    fd = os.open(file_path, os.O_RDWR | os.O_CREAT, stat.S_IWUSR | stat.S_IRUSR)
    with os.fdopen(fd, "w") as file:
        json.dump({"tools": tools_schema}, file)