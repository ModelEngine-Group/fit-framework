# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
from llama_index.core.multi_modal_llms.generic_utils import encode_image
from llama_index.core.schema import ImageNode, TextNode, NodeWithScore

from fel_core.types.document import Document
from fel_core.types.media import Media


def document_to_query_node(doc_input: Document):
    if isinstance(doc_input, dict):
        doc = Document(**doc_input)
    else:
        doc = doc_input

    if doc.media is not None:
        node = ImageNode(image=doc.media.data, image_mimetype=doc.media.mime)
    else:
        node = TextNode()
    node.set_content(doc.content)
    node.metadata = doc.metadata
    return NodeWithScore(node=node, score=doc.metadata["score"])


def query_node_to_document(node_with_score: NodeWithScore) -> Document:
    node = node_with_score.node
    metadata = node.metadata or {}
    metadata['score'] = node_with_score.score
    content = None
    image = None
    file_path_key = "file_path"
    if isinstance(node, ImageNode):
        mime = node.image_mimetype or "image/jpeg"
        data = None
        if node.image and node.image != "":
            data = node.image
        elif node.image_url and node.image_url != "":
            data = node.image_url
        elif node.image_path and node.image_path != "":
            data = encode_image(node.image_path)
        elif file_path_key in node.metadata and node.metadata[file_path_key] != "":
            data = encode_image(node.metadata[file_path_key])
        image = Media(mime=mime, data=data)
    if isinstance(node, TextNode):
        content = node.get_content()
    return Document(content=content, media=image, metadata=metadata)