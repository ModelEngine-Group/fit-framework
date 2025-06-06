# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import traceback
from typing import Tuple, List, Any, Callable

from fitframework import fit_logger
from llama_index.core.base.base_selector import SingleSelection
from llama_index.core.selectors import EmbeddingSingleSelector
from llama_index.embeddings.openai import OpenAIEmbedding

from .callable_registers import register_callable_tool


def embedding_choice_selector(choice: List[str], query_str: str, **kwargs) -> List[SingleSelection]:
    """ Embedding selector that chooses one out of many options."""
    if len(choice) == 0:
        return []
    api_key = kwargs.get("api_key") or "EMPTY"
    model_name = kwargs.get("model_name") or "bge-large-zh"
    api_base = kwargs.get("api_base") or None

    embed_model = OpenAIEmbedding(model_name=model_name, api_base=api_base, api_key=api_key)
    selector = EmbeddingSingleSelector.from_defaults(embed_model=embed_model)
    try:
        return selector.select(choice, query_str).selections
    except BaseException:
        fit_logger.error("Invoke embedding choice selector failed.")
        traceback.print_exc()
        return []


# Tuple 结构： (tool_func, config_args, return_description)
selector_toolkit: List[Tuple[Callable[..., Any], List[str], str]] = [
    (embedding_choice_selector, ["model_name", "api_key", "api_base", "prompt", "mode"], "The selected choice."),
]

for tool in selector_toolkit:
    register_callable_tool(tool, embedding_choice_selector.__module__, "llama_index.rag.toolkit")

if __name__ == '__main__':
    import time
    from .llama_schema_helper import dump_llama_schema

    current_timestamp = time.strftime('%Y%m%d%H%M%S')
    dump_llama_schema(selector_toolkit, f"./llama_tool_schema-{str(current_timestamp)}.json")
