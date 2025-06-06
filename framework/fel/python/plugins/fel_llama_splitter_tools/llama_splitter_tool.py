# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import traceback
from typing import Tuple, List, Any, Callable

from fitframework import fit_logger
from llama_index.core.node_parser import (
    SentenceSplitter,
    TokenTextSplitter,
    SemanticSplitterNodeParser,
    SentenceWindowNodeParser
)
from llama_index.core.schema import BaseNode
from llama_index.core.schema import Document as LDocument
from llama_index.embeddings.openai import OpenAIEmbedding

from .callable_registers import register_callable_tool
from .node_utils import to_llama_index_document


def sentence_splitter(text: str, separator: str, chunk_size: int, chunk_overlap: int, **kwargs) -> List[str]:
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


def token_text_splitter(text: str, separator: str, chunk_size: int, chunk_overlap: int, **kwargs) -> List[str]:
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


def semantic_splitter(buffer_size: int, breakpoint_percentile_threshold: int, docs: List[LDocument], **kwargs) \
        -> List[BaseNode]:
    """Splitting text that looks at word tokens."""
    if len(docs) == 0:
        return []
    api_key = kwargs.get("api_key")
    model_name = kwargs.get("model_name")
    api_base = kwargs.get("api_base")

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


def sentence_window_node_parser(window_size: int, window_metadata_key: str, original_text_metadata_key: str,
                                docs: List[LDocument], **kwargs) -> List[BaseNode]:
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


# Tuple 结构： (tool_func, config_args, return_description)
splitter_basic_toolkit: List[Tuple[Callable[..., Any], List[str], str]] = [
    (sentence_splitter, ["text", "separator", "chunk_size", "chunk_overlap"], "Split sentences by sentence."),
    (token_text_splitter, ["text", "separator", "chunk_size", "chunk_overlap"], "Split sentences by token."),
    (semantic_splitter,
     ["docs", "buffer_size", "breakpoint_percentile_threshold", "chunk_overlap", "model_name", "api_key", "api_base"],
     "Split sentences by semantic."),
    (sentence_window_node_parser, ["docs", "window_size", "window_metadata_key", "original_text_metadata_key"],
     "Splits all documents into individual sentences")
]

for tool in splitter_basic_toolkit:
    register_callable_tool(tool, sentence_splitter.__module__, "llama_index.rag.toolkit")

if __name__ == '__main__':
    import time
    from .llama_schema_helper import dump_llama_schema

    current_timestamp = time.strftime('%Y%m%d%H%M%S')
    dump_llama_schema(splitter_basic_toolkit, f"./llama_tool_schema-{str(current_timestamp)}.json")
