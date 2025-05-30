# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
from fel_core.types.serializable import Serializable


class Media(Serializable):
    """
    Media.
    """
    mime: str
    data: str

    class Config:
        frozen = True
        smart_union = True
