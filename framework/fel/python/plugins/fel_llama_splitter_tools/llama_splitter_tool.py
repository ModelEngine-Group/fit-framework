# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import traceback
from typing import Tuple, List, Any, Callable

from fitframework import fit_logger
from fitframework.api.decorators import fitable
from llama_index.core.node_parser import (
    SentenceSplitter,
    TokenTextSplitter,
    SemanticSplitterNodeParser,
    SentenceWindowNodeParser
)
from llama_index.core.schema import BaseNode
from llama_index.core.schema import Document as LDocument
from llama_index.embeddings.openai import OpenAIEmbedding

from .node_utils import to_llama_index_document
from .types.semantic_splitter_options import SemanticSplitterOptions

@fitable("llama.tools.sentence_splitter", "default")
def sentence_splitter(text: str, separator: str, chunk_size: int, chunk_overlap: int) -> List[str]:
    """Parse text with a preference for complete sentences."""
    if len(text) == 0:
        return []
    splitter = SentenceSplitter(
        separator=separator,
        chunk_size=chunk_size,
        chunk_overlap=chunk_overlap,
    )
    try:
        return splitter.split_text(text)
    except BaseException:
        fit_logger.error("Invoke sentence splitter failed.")
        traceback.print_exc()
        return []


@fitable("llama.tools.token_text_splitter", "default")
def token_text_splitter(text: str, separator: str, chunk_size: int, chunk_overlap: int) -> List[str]:
    """Splitting text that looks at word tokens."""
    if len(text) == 0:
        return []
    splitter = TokenTextSplitter(
        separator=separator,
        chunk_size=chunk_size,
        chunk_overlap=chunk_overlap,
    )
    try:
        return splitter.split_text(text)
    except BaseException:
        fit_logger.error("Invoke token text splitter failed.")
        traceback.print_exc()
        return []


# @fitable("llama.tools.semantic_splitter", "default")
def semantic_splitter(buffer_size: int, breakpoint_percentile_threshold: int, docs: List[LDocument], options: SemanticSplitterOptions) \
        -> List[BaseNode]:
    """Splitting text that looks at word tokens."""
    if len(docs) == 0:
        return []
    api_key = options.api_key
    model_name = options.model_name
    api_base = options.api_base

    embed_model = OpenAIEmbedding(model_name=model_name, api_base=api_base, api_key=api_key, max_tokens=4096)

    splitter = SemanticSplitterNodeParser(
        buffer_size=buffer_size,
        breakpoint_percentile_threshold=breakpoint_percentile_threshold,
        embed_model=embed_model
    )
    ldocs = [to_llama_index_document(doc) for doc in docs]
    try:
        return splitter.build_semantic_nodes_from_documents(documents=ldocs)
    except BaseException:
        fit_logger.error("Invoke semantic splitter failed.")
        traceback.print_exc()
        return []


# @fitable("llama.tools.sentence_window_node_parser", "default")
def sentence_window_node_parser(window_size: int, window_metadata_key: str, original_text_metadata_key: str,
                                docs: List[LDocument]) -> List[BaseNode]:
    """Splitting text that looks at word tokens."""
    if len(docs) == 0:
        return []

    node_parser = SentenceWindowNodeParser.from_defaults(
        window_size=window_size,
        window_metadata_key=window_metadata_key,
        original_text_metadata_key=original_text_metadata_key,
    )
    try:
        return node_parser.get_nodes_from_documents(docs)
    except BaseException:
        fit_logger.error("Invoke semantic splitter failed.")
        traceback.print_exc()
        return []