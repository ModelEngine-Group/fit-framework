# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import langchain_core.documents
from .types.document import Document


def langchain_doc_to_document(doc: langchain_core.documents.Document) -> Document:
    return Document(content=doc.page_content, metadata=dict())