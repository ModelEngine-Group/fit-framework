# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import typing

from .media import Media

class LLMChoiceSelectorOptions(object):
    def __init__(self, api_key: str, model_name: str,api_base: str, prompt: str ,mode: str = 'single'):
        self.api_key = api_key
        self.model_name = model_name
        self.api_base = api_base
        self.prompt = prompt
        self.mode = mode

    def __eq__(self, other):
        if not isinstance(other, self.__class__):
            return False
        return self.__dict__ == other.__dict__

    def __hash__(self):
        return hash(tuple(self.__dict__.values()))

    def __repr__(self):
        return str((self.__dict__.values()))