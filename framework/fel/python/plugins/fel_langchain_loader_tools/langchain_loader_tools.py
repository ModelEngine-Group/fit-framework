# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import traceback
from typing import List, Callable
from urllib.parse import urlparse, parse_qs

from langchain_community.document_loaders import PyPDFLoader, PDFPlumberLoader, PyMuPDFLoader, PyPDFDirectoryLoader, \
    PyPDFium2Loader, PDFMinerLoader
from langchain_core.document_loaders import BaseLoader

from fitframework.api.decorators import fitable
from fitframework.api.logging import sys_plugin_logger
from .document_util import langchain_doc_to_document
from .types.document import Document


@fitable("langchain.tool.py_pdf_loader", "default")
def py_pdf_loader(file_path: str) -> List[Document]:
    """Load PDF using pypdf into list of documents."""
    return __loader_handler(lambda nfs_file_path: PyPDFLoader(nfs_file_path), file_path)

@fitable("langchain.tool.pdfplumber_loader", "default")
def pdfplumber_loader(file_path: str) -> List[Document]:
    """Load PDF using pdfplumber into list of documents"""
    return __loader_handler(lambda nfs_file_path: PDFPlumberLoader(nfs_file_path), file_path)

@fitable("langchain.tool.py_mupdf_loader", "default")
def py_mupdf_loader(file_path: str) -> List[Document]:
    """Load PDF using PyMuPDF into list of documents"""
    return __loader_handler(lambda nfs_file_path: PyMuPDFLoader(nfs_file_path), file_path)

@fitable("langchain.tool.py_pdfium2_loader", "default")
def py_pdfium2_loader(file_path: str) -> List[Document]:
    """Load PDF using pypdfium2 into list of documents"""
    return __loader_handler(lambda nfs_file_path: PyPDFium2Loader(nfs_file_path), file_path)

@fitable("langchain.tool.py_miner_loader", "default")
def py_miner_loader(file_path: str) -> List[Document]:
    """Load PDF using PDFMiner into list of documents"""
    return __loader_handler(lambda nfs_file_path: PDFMinerLoader(nfs_file_path), file_path)

@fitable("langchain.tool.py_pdf_directory_loader", "default")
def py_pdf_directory_loader(directory: str) -> List[Document]:
    """Load a directory with `PDF` files using `pypdf` and chunks at character level"""
    return __loader_handler(lambda nfs_file_dir: PyPDFDirectoryLoader(nfs_file_dir), directory)


def __loader_handler(loader_builder: Callable[[str], BaseLoader], file_url: str) -> List[Document]:
    try:
        # 解析文件路径
        sys_plugin_logger.info("file_url: " + file_url)
        nfs_file_path = get_file_path(file_url)
        sys_plugin_logger.info("nfs_file_path: " + nfs_file_path)
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
        sys_plugin_logger.error("Invoke file loader failed.")
        sys_plugin_logger.exception("Invoke file loader failed.")
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
            sys_plugin_logger.error(msg)
            raise ValueError(msg)
        else:
            return file_query_param[0]
    except BaseException:
        sys_plugin_logger.error("Parse file path failed.")
        return file_url