# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import traceback
from typing import  List

from fitframework import fit_logger, fitable
from llama_index.core.base.base_selector import SingleSelection
from llama_index.core.selectors import EmbeddingSingleSelector
from llama_index.embeddings.openai import OpenAIEmbedding

from .types.embedding_choice_selector import EmbeddingChoiceSelectorOptions


@fitable("llama.tools.embedding_choice_selector", "default")
def embedding_choice_selector(choice: List[str], query_str: str , options:EmbeddingChoiceSelectorOptions) -> List[SingleSelection]:
    """ Embedding selector that chooses one out of many options."""
    if len(choice) == 0:
        return []
    api_key = options.api_key
    model_name = options.model_name
    api_base = options.api_base

    embed_model = OpenAIEmbedding(model_name=model_name, api_base=api_base, api_key=api_key)
    selector = EmbeddingSingleSelector.from_defaults(embed_model=embed_model)
    try:
        return selector.select(choice, query_str).selections
    except BaseException:
        fit_logger.error("Invoke embedding choice selector failed.")
        traceback.print_exc()
        return []
