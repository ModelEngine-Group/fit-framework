# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
功 能：泛服务实现地址查询相关功能测试。
"""
import unittest
from typing import List
from unittest.mock import patch

from fit_common_struct.core import Address as AddressInner, Fitable
from fitframework.testing.test_support import FitTestSupport

_REGISTRY_ADDRESS = AddressInner("reg_host", 8003, "reg_worker_id", 2, [1], "ut", "")


class FitableAddressServiceTest(FitTestSupport):
    @classmethod
    def setUpClass(cls):
        super(FitableAddressServiceTest, cls).setUpClass()
        from plugin.fit_py_registry_client import fitable_address_service
        from fit_common_struct import entity
        global fitable_address_service
        global entity

        def query_fitable_addresses_side_effect(fitables: List[entity.Fitable], worker_id: str):
            if fitables[0].genericableId == "gid_ut":
                return [cls.build_fitable_address_instance("host_ut_1", 8000)]
            return []

        def subscribe_fit_service_side_effect(fitables: List[entity.Fitable], worker_id: str,
                                              callback_fitable_id: str):
            if fitables[0].genericableId == "gid_ut":
                return [cls.build_fitable_address_instance("host_ut_1", 8000)]
            return []

        cls.patchers = [patch.object(fitable_address_service, "_get_registry_server_generic_ids",
                                     return_value=["gid_registry_query"]),
                        patch.object(fitable_address_service, "get_cache_aware_registry_address",
                                     return_value=[_REGISTRY_ADDRESS]),
                        patch.object(fitable_address_service, "query_fitable_addresses",
                                     side_effect=query_fitable_addresses_side_effect),
                        patch.object(fitable_address_service, "subscribe_fit_service",
                                     side_effect=subscribe_fit_service_side_effect)]
        for patcher in cls.patchers:
            patcher.start()

    @classmethod
    def tearDownClass(cls):
        for patcher in cls.patchers:
            patcher.stop()

    @classmethod
    def build_fitable_address_instance(cls, host: str, port: int):
        endpoint = entity.Endpoint(port, 2)
        address = entity.Address(host, [endpoint])
        worker = entity.Worker([address], "worker_ut", "env_ut", {"http.context-path": "context-path-ut"})
        application = entity.Application("name_ut", "name_version_ut")
        application_instance = entity.ApplicationInstance([worker], application, [1])
        fitable_info = entity.Fitable("gid_ut", "1.0.0", "fid_ut", "1.0.0")
        fitable_address_instance = entity.FitableAddressInstance([application_instance], fitable_info)
        return fitable_address_instance

    def test_should_return_registry_address_when_fitable_is_registry_fitable(self):
        fitable = Fitable("gid_registry_query", "1.0.0", "fid_registry_query", "1.0.0")
        addresses = fitable_address_service.get_fit_service_address_list(fitable)
        self.assertEqual(addresses, [_REGISTRY_ADDRESS])

    @patch("plugin.fit_py_registry_client.fitable_address_service._registry_client_mode", return_value="pull")
    def test_should_update_cache_when_get_fitable(self, *_):
        fitable = Fitable("gid_ut", "1.0.0", "fid_ut", "1.0.0")
        addresses = fitable_address_service._get_fitable_address_from_registry_server_and_update_cache(fitable)
        self.assertEqual(fitable_address_service._FITABLE_ADDRESS_CACHE.get(fitable),
                         [AddressInner('host_ut_1', 8000, 'worker_ut', 2, [1], 'env_ut', 'context-path-ut')])

    @patch("plugin.fit_py_registry_client.fitable_address_service._registry_client_mode", return_value="push")
    def test_should_update_by_notify_when_use_push_mode(self, *_):
        fitable = Fitable("gid_ut", "1.0.0", "fid_ut", "1.0.0")
        addresses: List[AddressInner] = fitable_address_service.get_fit_service_address_list(fitable)
        self.assertEqual(addresses[0].host, "host_ut_1")

        fitable_address_instance = self.build_fitable_address_instance("host_ut_2", 8001)
        fitable_address_service.notify_fitable_changes([fitable_address_instance])
        addresses = fitable_address_service.get_fit_service_address_list(fitable)

        self.assertEqual(addresses[0].host, "host_ut_2")


if __name__ == '__main__':
    unittest.main()
