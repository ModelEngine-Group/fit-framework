# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import typing

from fel_core.types.serializable import Serializable
from fel_core.types.media import Media


class Document(Serializable):
    """
    Document.
    """
    content: str
    media: Media = None
    metadata: typing.Dict[str, object]

    class Config:
        frozen = True
        smart_union = True
