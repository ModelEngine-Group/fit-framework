# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
class EmbeddingChoiceSelectorOptions(object):
    def __init__(self , model_name : str , api_key : str , api_base : str):
        self.model_name = model_name
        self.api_key = api_key
        self.api_base = api_base

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str((self.__dict__.values()))