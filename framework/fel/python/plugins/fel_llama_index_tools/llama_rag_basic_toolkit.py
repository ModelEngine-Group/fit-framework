# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import functools
import os
import traceback
from enum import Enum, unique
from inspect import signature
from typing import List, Callable, Any, Tuple

from fitframework import fit_logger
from fitframework.core.repo.fitable_register import register_fitable
from llama_index.core import PromptTemplate
from llama_index.core.base.base_selector import SingleSelection
from llama_index.core.postprocessor import SimilarityPostprocessor, SentenceEmbeddingOptimizer, LLMRerank, \
    LongContextReorder, FixedRecencyPostprocessor
from llama_index.core.postprocessor.types import BaseNodePostprocessor
from llama_index.core.prompts import PromptType
from llama_index.core.prompts.default_prompts import DEFAULT_CHOICE_SELECT_PROMPT_TMPL
from llama_index.core.selectors import LLMSingleSelector, LLMMultiSelector
from llama_index.core.selectors.prompts import DEFAULT_SINGLE_SELECT_PROMPT_TMPL, DEFAULT_MULTI_SELECT_PROMPT_TMPL
from llama_index.embeddings.openai import OpenAIEmbedding
from llama_index.legacy.llms import OpenAILike

from .callable_registers import register_callable_tool
from .types.document import Document
from .node_utils import document_to_query_node, query_node_to_document

os.environ["no_proxy"] = "*"


def __invoke_postprocessor(postprocessor: BaseNodePostprocessor, nodes: List[Document],
                           query_str: str) -> List[Document]:
    if len(nodes) == 0:
        return []
    try:
        postprocess_nodes = postprocessor.postprocess_nodes([document_to_query_node(node) for node in nodes],
                                                            query_str=query_str)
        return [query_node_to_document(node) for node in postprocess_nodes]
    except BaseException:
        fit_logger.error("Invoke postprocessor failed.")
        traceback.print_exc()
        return nodes


def similarity_filter(nodes: List[Document], query_str: str, **kwargs) -> List[Document]:
    """Remove documents that are below a similarity score threshold."""
    similarity_cutoff = float(kwargs.get("similarity_cutoff") or 0.3)
    postprocessor = SimilarityPostprocessor(similarity_cutoff=similarity_cutoff)
    return __invoke_postprocessor(postprocessor, nodes, query_str)


def sentence_embedding_optimizer(nodes: List[Document], query_str: str, **kwargs) -> List[Document]:
    """Optimization of a text chunk given the query by shortening the input text."""
    api_key = kwargs.get("api_key") or "EMPTY"
    model_name = kwargs.get("model_name") or "bce-embedding-base_v1"
    api_base = kwargs.get("api_base") or ("http://51.36.139.24:8010/v1" if api_key == "EMPTY" else None)
    percentile_cutoff = kwargs.get("percentile_cutoff")
    threshold_cutoff = kwargs.get("threshold_cutoff")
    percentile_cutoff = percentile_cutoff if percentile_cutoff is None else float(percentile_cutoff)
    threshold_cutoff = threshold_cutoff if threshold_cutoff is None else float(threshold_cutoff)

    embed_model = OpenAIEmbedding(model_name=model_name, api_base=api_base, api_key=api_key)
    optimizer = SentenceEmbeddingOptimizer(embed_model=embed_model, percentile_cutoff=percentile_cutoff,
                                           threshold_cutoff=threshold_cutoff)
    return __invoke_postprocessor(optimizer, nodes, query_str)


def llm_rerank(nodes: List[Document], query_str: str, **kwargs) -> List[Document]:
    """
    Re-order nodes by asking the LLM to return the relevant documents and a score of how relevant they are.
    Returns the top N ranked nodes.
    """
    api_key = kwargs.get("api_key") or "EMPTY"
    model_name = kwargs.get("model_name") or "Qwen1.5-14B-Chat"
    api_base = kwargs.get("api_base") or ("http://80.11.128.62:8000/v1" if api_key == "EMPTY" else None)
    prompt = kwargs.get("prompt") or DEFAULT_CHOICE_SELECT_PROMPT_TMPL
    choice_batch_size = int(kwargs.get("choice_batch_size") or 10)
    top_n = int(kwargs.get("top_n") or 10)

    llm = OpenAILike(model=model_name, api_base=api_base, api_key=api_key, max_tokens=4096)
    choice_select_prompt = PromptTemplate(prompt, prompt_type=PromptType.CHOICE_SELECT)
    llm_rerank_obj = LLMRerank(llm=llm, choice_select_prompt=choice_select_prompt, choice_batch_size=choice_batch_size,
                               top_n=top_n)
    return __invoke_postprocessor(llm_rerank_obj, nodes, query_str)


def long_context_rerank(nodes: List[Document], query_str: str, **kwargs) -> List[Document]:
    """Re-order the retrieved nodes, which can be helpful in cases where a large top-k is needed."""
    return __invoke_postprocessor(LongContextReorder(), nodes, query_str)


@unique
class SelectorMode(Enum):
    SINGLE = "single"
    MULTI = "multi"


def llm_choice_selector(choice: List[str], query_str: str, **kwargs) -> List[SingleSelection]:
    """LLM-based selector that chooses one or multiple out of many options."""
    if len(choice) == 0:
        return []
    api_key = kwargs.get("api_key") or "EMPTY"
    model_name = kwargs.get("model_name") or "Qwen1.5-14B-Chat"
    api_base = kwargs.get("api_base") or ("http://80.11.128.62:8000/v1" if api_key == "EMPTY" else None)
    prompt = kwargs.get("prompt")
    mode = str(kwargs.get("mode") or SelectorMode.SINGLE.value)
    if mode.lower() not in [m.value for m in SelectorMode]:
        raise ValueError(f"Invalid mode {mode}.")

    llm = OpenAILike(model=model_name, api_base=api_base, api_key=api_key, max_tokens=4096)
    if mode.lower() == SelectorMode.SINGLE.value:
        selector_prompt = prompt or DEFAULT_SINGLE_SELECT_PROMPT_TMPL
        selector = LLMSingleSelector.from_defaults(llm=llm, prompt_template_str=selector_prompt)
    else:
        multi_selector_prompt = prompt or DEFAULT_MULTI_SELECT_PROMPT_TMPL
        selector = LLMMultiSelector.from_defaults(llm=llm, prompt_template_str=multi_selector_prompt)
    try:
        return selector.select(choice, query_str).selections
    except BaseException:
        fit_logger.error("Invoke choice selector failed.")
        traceback.print_exc()
        return []


def fixed_recency(nodes: List[Document], tok_k: int, date_key: str, query_str: str, **kwargs) -> List[Document]:
    """This postprocessor returns the top K nodes sorted by date"""
    postprocessor = FixedRecencyPostprocessor(
        tok_k=tok_k, date_key=date_key if date_key else "date"
    )
    return __invoke_postprocessor(postprocessor, nodes, query_str)


# Tuple 结构： (tool_func, config_args, return_description)
rag_basic_toolkit: List[Tuple[Callable[..., Any], List[str], str]] = [
    (similarity_filter, ["similarity_cutoff"], "The filtered documents."),
    (sentence_embedding_optimizer, ["model_name", "api_key", "api_base", "percentile_cutoff", "threshold_cutoff"],
     "The optimized documents."),
    (llm_rerank, ["model_name", "api_key", "api_base", "prompt", "choice_batch_size", "top_n"],
     "The re-ordered documents."),
    (long_context_rerank, [], "The re-ordered documents."),
    (llm_choice_selector, ["model_name", "api_key", "api_base", "prompt", "mode"], "The selected choice."),
    (fixed_recency, ["nodes", "tok_k", "date_key", "query_str"], "The fixed recency postprocessor")
]


for tool in rag_basic_toolkit:
    register_callable_tool(tool, llm_choice_selector.__module__, "llama_index.rag.toolkit")


if __name__ == '__main__':
    import time
    from .llama_schema_helper import dump_llama_schema

    current_timestamp = time.strftime('%Y%m%d%H%M%S')
    dump_llama_schema(rag_basic_toolkit, f"./llama_tool_schema-{str(current_timestamp)}.json")
