# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import traceback
from typing import List, Callable, Tuple, Any
from urllib.parse import urlparse, parse_qs

from fitframework import fit_logger
from langchain_community.document_loaders import PyPDFLoader, PDFPlumberLoader, PyMuPDFLoader, PyPDFDirectoryLoader, \
    PyPDFium2Loader, PDFMinerLoader
from langchain_core.document_loaders import BaseLoader

from .types.document import Document
from .document_util import langchain_doc_to_document
from .callable_registers import register_callable_tool


def py_pdf_loader(file_path: str, **kwargs) -> List[Document]:
    """Load PDF using pypdf into list of documents."""
    return __loader_handler(lambda nfs_file_path: PyPDFLoader(nfs_file_path), file_path)


def pdfplumber_loader(file_path: str, **kwargs) -> List[Document]:
    """Load PDF using pdfplumber into list of documents"""
    return __loader_handler(lambda nfs_file_path: PDFPlumberLoader(nfs_file_path), file_path)


def py_mupdf_loader(file_path: str, **kwargs) -> List[Document]:
    """Load PDF using PyMuPDF into list of documents"""
    return __loader_handler(lambda nfs_file_path: PyMuPDFLoader(nfs_file_path), file_path)


def py_pdfium2_loader(file_path: str, **kwargs) -> List[Document]:
    """Load PDF using pypdfium2 into list of documents"""
    return __loader_handler(lambda nfs_file_path: PyPDFium2Loader(nfs_file_path), file_path)


def py_miner_loader(file_path: str, **kwargs) -> List[Document]:
    """Load PDF using PDFMiner into list of documents"""
    return __loader_handler(lambda nfs_file_path: PDFMinerLoader(nfs_file_path), file_path)


def py_pdf_directory_loader(directory: str, **kwargs) -> List[Document]:
    """Load a directory with `PDF` files using `pypdf` and chunks at character level"""
    return __loader_handler(lambda nfs_file_dir: PyPDFDirectoryLoader(nfs_file_dir), directory)


def __loader_handler(loader_builder: Callable[[str], BaseLoader], file_url: str) -> List[Document]:
    try:
        # 解析文件路径
        fit_logger.info("file_url: " + file_url)
        nfs_file_path = get_file_path(file_url)
        fit_logger.info("nfs_file_path: " + nfs_file_path)
        pdf_loader = loader_builder(nfs_file_path)
        iterator = pdf_loader.lazy_load()
        res = []
        max_page = 300
        for doc in iterator:
            if len(res) > max_page:
                return res
            res.append(langchain_doc_to_document(doc))
        return res
    except BaseException:
        fit_logger.error("Invoke file loader failed.")
        fit_logger.exception("Invoke file loader failed.")
        traceback.print_exc()
        return []


def get_file_path(file_url: str):
    try:
        parsed_url = urlparse(file_url)
        if not all([parsed_url.scheme, parsed_url.netloc]):
            return file_url
        file_query_param = parse_qs(parsed_url.query).get('filePath')
        if file_query_param is None or len(file_query_param) == 0:
            msg = "Invalid file url. missing query parameter [filePath]"
            fit_logger.error(msg)
            raise ValueError(msg)
        else:
            return file_query_param[0]
    except BaseException:
        fit_logger.error("Parse file path failed.")
        return file_url


DOCUMENT_RETURN_DESC = "a piece of text and associated metadata."

# 普通callable注册方式
# Tuple 结构： (tool_func, config_args, return_description)
loader_toolkit: List[Tuple[Callable[..., Any], List[str], str]] = [
    (py_pdf_loader, [], DOCUMENT_RETURN_DESC),
    (pdfplumber_loader, [], DOCUMENT_RETURN_DESC),
    (py_mupdf_loader, [], DOCUMENT_RETURN_DESC),
    (py_pdfium2_loader, [], DOCUMENT_RETURN_DESC),
    (py_miner_loader, [], DOCUMENT_RETURN_DESC),
    (py_pdf_directory_loader, [], DOCUMENT_RETURN_DESC),
]

for tool in loader_toolkit:
    register_callable_tool(tool, get_file_path.__module__, 'langchain.tool')
