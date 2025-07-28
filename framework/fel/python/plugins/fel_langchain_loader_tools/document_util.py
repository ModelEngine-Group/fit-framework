# -- encoding: utf-8 --
# Copyright (c) Huawei Technologies Co., Ltd. -. All rights reserved.
import langchain_core.documents
from .types.document import Document


def langchain_doc_to_document(doc: langchain_core.documents.Document) -> Document:
    return Document(content=doc.page_content, metadata=dict())