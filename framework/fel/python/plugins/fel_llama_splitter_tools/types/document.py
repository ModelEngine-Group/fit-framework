# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================

import typing

from .media import Media

class Document(object):
    def __init__(self, content: str, media: Media , metadata: typing.Dict[str, object] ):
        self.content = content
        self.media = media
        self.metadata = metadata
    
    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str((self.__dict__.values()))