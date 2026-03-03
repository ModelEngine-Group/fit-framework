# -- encoding: utf-8 --
# Copyright (c) 2024-2026 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import importlib
import os
import sys
import types
import unittest
from unittest.mock import patch


class _DummyTextRequestsWrapper:
    def __init__(self, headers=None):
        self.headers = headers or {}

    def get(self, url: str, **kwargs):
        return f"get:{url}"

    def post(self, url: str, data: dict, **kwargs):
        return f"post:{url}:{data}"

    def patch(self, url: str, data: dict, **kwargs):
        return f"patch:{url}:{data}"

    def put(self, url: str, data: dict, **kwargs):
        return f"put:{url}:{data}"

    def delete(self, url: str, **kwargs):
        return f"delete:{url}"


class _DummyRequestsTool:
    def __init__(self, requests_wrapper=None, allow_dangerous_requests=False, **kwargs):
        self.requests_wrapper = requests_wrapper
        self.allow_dangerous_requests = allow_dangerous_requests


class _DummySQLDatabase:
    @classmethod
    def from_uri(cls, _uri):
        return cls()


def _build_stub_modules():
    langchain_pkg = types.ModuleType("langchain")
    langchain_pkg.__path__ = []
    langchain_agents = types.ModuleType("langchain.agents")
    langchain_agents.AgentExecutor = type("AgentExecutor", (), {})

    community_pkg = types.ModuleType("langchain_community")
    community_pkg.__path__ = []
    community_agent_toolkits = types.ModuleType("langchain_community.agent_toolkits")
    community_agent_toolkits.JsonToolkit = type("JsonToolkit", (), {})
    community_agent_toolkits.create_json_agent = lambda **kwargs: kwargs
    community_agent_toolkits.create_sql_agent = lambda *args, **kwargs: (args, kwargs)

    community_tools_pkg = types.ModuleType("langchain_community.tools")
    community_tools_pkg.__path__ = []
    community_tools_json_pkg = types.ModuleType("langchain_community.tools.json")
    community_tools_json_pkg.__path__ = []
    community_tools_json_tool = types.ModuleType("langchain_community.tools.json.tool")
    community_tools_json_tool.JsonSpec = type("JsonSpec", (), {})

    community_tools_requests_pkg = types.ModuleType("langchain_community.tools.requests")
    community_tools_requests_pkg.__path__ = []
    community_tools_requests_tool = types.ModuleType("langchain_community.tools.requests.tool")
    community_tools_requests_tool.RequestsGetTool = _DummyRequestsTool
    community_tools_requests_tool.RequestsPostTool = _DummyRequestsTool
    community_tools_requests_tool.RequestsPatchTool = _DummyRequestsTool
    community_tools_requests_tool.RequestsPutTool = _DummyRequestsTool
    community_tools_requests_tool.RequestsDeleteTool = _DummyRequestsTool

    community_tools_sql_pkg = types.ModuleType("langchain_community.tools.sql_database")
    community_tools_sql_pkg.__path__ = []
    community_tools_sql_tool = types.ModuleType("langchain_community.tools.sql_database.tool")
    community_tools_sql_tool.InfoSQLDatabaseTool = type("InfoSQLDatabaseTool", (), {})
    community_tools_sql_tool.ListSQLDatabaseTool = type("ListSQLDatabaseTool", (), {})
    community_tools_sql_tool.QuerySQLCheckerTool = type("QuerySQLCheckerTool", (), {})
    community_tools_sql_tool.QuerySQLDataBaseTool = type("QuerySQLDataBaseTool", (), {})

    community_utilities_pkg = types.ModuleType("langchain_community.utilities")
    community_utilities_pkg.__path__ = []
    community_utilities_requests = types.ModuleType("langchain_community.utilities.requests")
    community_utilities_requests.TextRequestsWrapper = _DummyTextRequestsWrapper
    community_utilities_sql = types.ModuleType("langchain_community.utilities.sql_database")
    community_utilities_sql.SQLDatabase = _DummySQLDatabase

    core_pkg = types.ModuleType("langchain_core")
    core_pkg.__path__ = []
    core_tools = types.ModuleType("langchain_core.tools")
    core_tools.BaseTool = object

    langchain_openai = types.ModuleType("langchain_openai")
    langchain_openai.ChatOpenAI = type("ChatOpenAI", (), {})

    registers_module = types.ModuleType("plugins.fel_langchain_tools.langchain_registers")
    registers_module.register_function_tools = lambda *args, **kwargs: None
    registers_module.register_api_tools = lambda *args, **kwargs: None

    return {
        "langchain": langchain_pkg,
        "langchain.agents": langchain_agents,
        "langchain_community": community_pkg,
        "langchain_community.agent_toolkits": community_agent_toolkits,
        "langchain_community.tools": community_tools_pkg,
        "langchain_community.tools.json": community_tools_json_pkg,
        "langchain_community.tools.json.tool": community_tools_json_tool,
        "langchain_community.tools.requests": community_tools_requests_pkg,
        "langchain_community.tools.requests.tool": community_tools_requests_tool,
        "langchain_community.tools.sql_database": community_tools_sql_pkg,
        "langchain_community.tools.sql_database.tool": community_tools_sql_tool,
        "langchain_community.utilities": community_utilities_pkg,
        "langchain_community.utilities.requests": community_utilities_requests,
        "langchain_community.utilities.sql_database": community_utilities_sql,
        "langchain_core": core_pkg,
        "langchain_core.tools": core_tools,
        "langchain_openai": langchain_openai,
        "plugins.fel_langchain_tools.langchain_registers": registers_module,
    }


def _load_module_with_stubs():
    fel_python_path = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    if fel_python_path not in sys.path:
        sys.path.insert(0, fel_python_path)

    sys.modules.pop("plugins.fel_langchain_tools.langchain_tools", None)
    with patch.dict(sys.modules, _build_stub_modules()):
        return importlib.import_module("plugins.fel_langchain_tools.langchain_tools")


class TestLangchainToolsSSRF(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.module = _load_module_with_stubs()

    def test_validate_url_blocks_ssrf_targets(self):
        blocked_urls = [
            "http://169.254.169.254/latest/meta-data/",
            "http://127.0.0.1:8080/admin",
            "http://10.0.0.1/internal",
            "http://192.168.1.1/config",
            "http://[::ffff:169.254.169.254]/latest/meta-data/",
            "http://[::ffff:127.0.0.1]/",
            "http://[::ffff:10.0.0.1]/",
            "http://localhost/health",
            "http://0.0.0.0/status",
            "http://[::1]/",
            "http://[fc00::1]/",
            "http://[fe80::1]/",
        ]

        for url in blocked_urls:
            with self.subTest(url=url):
                with self.assertRaisesRegex(ValueError, "Request blocked"):
                    self.module._validate_url(url)

    def test_validate_url_allows_external_targets(self):
        allowed_urls = [
            "https://api.example.com/data",
            "http://8.8.8.8/health",
            "https://httpbin.org/get",
        ]

        for url in allowed_urls:
            with self.subTest(url=url):
                self.module._validate_url(url)

    def test_validate_url_rejects_invalid_url(self):
        with self.assertRaisesRegex(ValueError, "Invalid URL"):
            self.module._validate_url("http:///path-only")

    def test_http_tool_builders_use_safe_wrapper(self):
        builders = [
            self.module.langchain_request_get,
            self.module.langchain_request_post,
            self.module.langchain_request_patch,
            self.module.langchain_request_delete,
            self.module.langchain_request_put,
        ]
        for builder in builders:
            with self.subTest(builder=builder.__name__):
                tool = builder({})
                self.assertIsInstance(tool.requests_wrapper, self.module.SafeRequestsWrapper)
                self.assertTrue(tool.allow_dangerous_requests)

    def test_safe_wrapper_blocks_and_allows_requests(self):
        wrapper = self.module.SafeRequestsWrapper(headers={})
        with self.assertRaisesRegex(ValueError, "Request blocked"):
            wrapper.get("http://127.0.0.1/api")

        result = wrapper.get("https://api.example.com/data")
        self.assertEqual(result, "get:https://api.example.com/data")


if __name__ == "__main__":
    unittest.main()
