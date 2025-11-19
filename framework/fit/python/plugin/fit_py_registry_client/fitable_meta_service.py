# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：泛服务元数据查询相关功能。
"""
from typing import List

from fitframework import fitable, const, fit
from fit_common_struct.entity import FitableMetaInstance
from fit_common_struct.core import Genericable


@fit(const.QUERY_FITABLE_METAS_GEN_ID)
def query_fitable_metas(genericable_infos: List[Genericable]) -> List[FitableMetaInstance]:
    """
    注册中心所提供接口，用于查询泛服务的元数据。

    @param genericable_infos: 泛服务信息列表。
    @return: 所查询到的泛服务元数据列表。
    """
    pass


@fitable(const.GET_FITABLES_OF_GENERICABLE_GEN_ID, const.GET_FITABLES_OF_GENERICABLE_FIT_ID)
def get_all_fitables_from_registry(genericable: Genericable) -> List[str]:
    fitable_meta_instances: List[FitableMetaInstance] = query_fitable_metas(
        [Genericable(genericable.genericableId, genericable.genericableVersion)])
    return [instance.meta.fitable.fitableId for instance in fitable_meta_instances if
            instance.meta.fitable.genericableId == genericable.genericableId]
