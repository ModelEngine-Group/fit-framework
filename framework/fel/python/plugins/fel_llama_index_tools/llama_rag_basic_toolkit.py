# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import os
import traceback
from enum import Enum, unique
from typing import List

from fitframework import fit_logger, fitable
from llama_index.core.base.base_selector import SingleSelection
from llama_index.core.postprocessor import SimilarityPostprocessor, SentenceEmbeddingOptimizer, LLMRerank, \
    LongContextReorder, FixedRecencyPostprocessor
from llama_index.core.postprocessor.types import BaseNodePostprocessor
from llama_index.core.prompts import PromptType, PromptTemplate
from llama_index.core.selectors import LLMSingleSelector, LLMMultiSelector
from llama_index.core.selectors.prompts import DEFAULT_SINGLE_SELECT_PROMPT_TMPL, DEFAULT_MULTI_SELECT_PROMPT_TMPL
from llama_index.embeddings.openai import OpenAIEmbedding
from llama_index.llms.openai import OpenAI

from .types.document import Document
from .types.llm_rerank_options import LLMRerankOptions
from .types.embedding_options import EmbeddingOptions
from .types.retriever_options import RetrieverOptions
from .types.llm_choice_selector_options import LLMChoiceSelectorOptions
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


@fitable("llama.tools.similarity_filter", "default")
def similarity_filter(nodes: List[Document], query_str: str, options: RetrieverOptions) -> List[Document]:
    """Remove documents that are below a similarity score threshold."""
    if options is None:
        options = RetrieverOptions()
    postprocessor = SimilarityPostprocessor(similarity_cutoff=options.similarity_cutoff)
    return __invoke_postprocessor(postprocessor, nodes, query_str)


@fitable("llama.tools.sentence_embedding_optimizer", "default")
def sentence_embedding_optimizer(nodes: List[Document], query_str: str, options: EmbeddingOptions) -> List[Document]:
    """Optimization of a text chunk given the query by shortening the input text."""
    if options is None:
        options = EmbeddingOptions()
    api_base = options.api_base
    embed_model = OpenAIEmbedding(model_name=options.model_name, api_base=api_base, api_key=options.api_key)
    optimizer = SentenceEmbeddingOptimizer(embed_model=embed_model, percentile_cutoff=options.percentile_cutoff,
                                           threshold_cutoff=options.threshold_cutoff)
    return __invoke_postprocessor(optimizer, nodes, query_str)


@fitable("llama.tools.llm_rerank", "default")
def llm_rerank(nodes: List[Document], query_str: str, options: LLMRerankOptions) -> List[Document]:
    """
    Re-order nodes by asking the LLM to return the relevant documents and a score of how relevant they are.
    Returns the top N ranked nodes.
    """
    if options is None:
        options = LLMRerankOptions()

    api_base = options.api_base

    prompt = options.prompt

    llm = OpenAI(model=options.model_name, api_base=api_base, api_key=options.api_key)
    choice_select_prompt = PromptTemplate(prompt, prompt_type=PromptType.CHOICE_SELECT)
    llm_rerank_obj = LLMRerank(llm=llm, choice_select_prompt=choice_select_prompt,
                               choice_batch_size=options.choice_batch_size,
                               top_n=options.top_n)
    return __invoke_postprocessor(llm_rerank_obj, nodes, query_str)


@fitable("llama.tools.long_context_rerank", "default")
def long_context_rerank(nodes: List[Document], query_str: str) -> List[Document]:
    """Re-order the retrieved nodes, which can be helpful in cases where a large top-k is needed."""
    return __invoke_postprocessor(LongContextReorder(), nodes, query_str)


@unique
class SelectorMode(Enum):
    SINGLE = "single"
    MULTI = "multi"


@fitable("llama.tools.llm_choice_selector", "default")
def llm_choice_selector(choice: List[str], query_str: str, options: LLMChoiceSelectorOptions) -> List[SingleSelection]:
    """LLM-based selector that chooses one or multiple out of many options."""
    if len(choice) == 0:
        return []
    if options is None:
        options = LLMChoiceSelectorOptions()
    api_base = options.api_base
    if options.mode.lower() not in [m.value for m in SelectorMode]:
        raise ValueError(f"Invalid mode {options.mode}.")

    llm = OpenAI(model=options.model_name, api_base=api_base, api_key=options.api_key, max_tokens=4096)
    if options.mode.lower() == SelectorMode.SINGLE.value:
        selector_prompt = options.prompt or DEFAULT_SINGLE_SELECT_PROMPT_TMPL
        selector = LLMSingleSelector.from_defaults(llm=llm, prompt_template_str=selector_prompt)
    else:
        multi_selector_prompt = options.prompt or DEFAULT_MULTI_SELECT_PROMPT_TMPL
        selector = LLMMultiSelector.from_defaults(llm=llm, prompt_template_str=multi_selector_prompt)
    try:
        return selector.select(choice, query_str).selections
    except BaseException:
        fit_logger.error("Invoke choice selector failed.")
        traceback.print_exc()
        return []


@fitable("llama.tools.fixed_recency", "default")
def fixed_recency(nodes: List[Document], top_k: int, date_key: str, query_str: str) -> List[Document]:
    """This postprocessor returns the top K nodes sorted by date"""
    postprocessor = FixedRecencyPostprocessor(
        top_k=top_k, date_key=date_key if date_key else "date"
    )
    return __invoke_postprocessor(postprocessor, nodes, query_str)
