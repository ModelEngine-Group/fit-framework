# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import json
import os
import re
import stat
from inspect import Parameter
from inspect import signature
from typing import List, Tuple, Any, Callable, Optional

from llama_index.core.bridge.pydantic import FieldInfo, create_model
from llama_index.core.tools import FunctionTool


def __get_ref_item(value: dict, definitions: dict) -> dict:
    sub_properties_name = re.findall("^#/definitions/(.+)$", value.get("$ref"))
    if len(sub_properties_name) == 0:
        raise ValueError(f"Invalid reference properties {value.get('$ref')}.")
    ref_item = definitions.get(sub_properties_name[0])
    ref_item["properties"] = __flat_properties(ref_item.get("properties"), definitions)
    return ref_item


def __flat_properties(properties: dict, definitions: dict) -> dict:
    if definitions is None:
        return properties
    flat_properties = dict()
    for key, value in properties.items():
        if value.__contains__("$ref"):
            flat_properties[key] = __get_ref_item(value, definitions)
            continue
        array_item = value.get("items")
        if array_item is not None and array_item.__contains__("$ref"):
            value["items"] = __get_ref_item(array_item, definitions)
            flat_properties[key] = value
            continue
        else:
            flat_properties[key] = value
    return flat_properties


def __get_return_properties(func: Callable[..., Any], return_description: str) -> dict:
    func_signature = signature(func)
    param_type = func_signature.return_annotation
    if param_type is Parameter.empty:
        param_type = Any

    fields = {return_description: (param_type, FieldInfo())}
    field_model = create_model(return_description, **fields)
    parameters = field_model.schema()
    parameters = {
        key: value
        for key, value in parameters.items()
        if key in ["type", "properties", "required", "definitions"]
    }
    properties = __flat_properties(parameters.get("properties"), parameters.get("definitions"))
    if return_description in properties:
        return properties[return_description]
    else:
        return dict()


def __get_llama_rag_tool_schema(tool: Tuple[Callable[..., Any], List[str], str]) -> dict:
    func = tool[0]
    metadata = FunctionTool.from_defaults(fn=func).metadata
    parameters_dict = metadata.get_parameters_dict()
    property_key = "properties"
    parameters_dict.get(property_key).pop("kwargs")

    dynamic_args = tool[1]
    dynamic_args_dict = dict()
    for arg in dynamic_args:
        dynamic_args_dict[arg] = {"type": "string", "description": arg}

    definition = __get_param_definition(parameters_dict)
    flat_properties = __flat_properties(parameters_dict.get(property_key), definition)
    parameters_dict[property_key] = {**flat_properties, **dynamic_args_dict}
    tool_schema = {
        "name": metadata.name,
        "description": func.__doc__,
        "parameters": parameters_dict,
        "return": __get_return_properties(func, tool[2]),
    }
    if len(dynamic_args_dict) != 0:
        tool_schema["parameterExtensions"] = {"config": list(dynamic_args_dict.keys())}
    return tool_schema


def __get_param_definition(parameters_dict: dict) -> Optional[dict]:
    if parameters_dict.__contains__("definitions"):
        return parameters_dict.pop("definitions")
    return None


def dump_llama_schema(llama_toolkit: List[Tuple[Callable[..., Any], List[str], str]], file_path: str):
    """
    导出 LlamaIndex 函数工具 schema 的工具方法。

    Args:
        llama_toolkit (List[Tuple[Callable[..., Any], List[str]]]): 表示 llama_index rag 工具列表。
        file_path (str): 表示 schema 文件的导出路径。
    """
    tools_schema = [{
        "runnables": {"LlamaIndex": {"genericableId": "llama_index.rag.toolkit", "fitableId": f"{tool[0].__name__}"}},
        "schema": {**__get_llama_rag_tool_schema(tool)}
    } for tool in llama_toolkit]

    fd = os.open(file_path, os.O_RDWR | os.O_CREAT, stat.S_IWUSR | stat.S_IRUSR)
    with os.fdopen(fd, "w") as file:
        json.dump({"tools": tools_schema}, file)
