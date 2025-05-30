# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import json
from urllib.parse import quote_plus
import psycopg2
from langchain.agents import AgentExecutor

from langchain_community.utilities.sql_database import SQLDatabase
from langchain_community.agent_toolkits import create_sql_agent
from langchain_community.tools.sql_database.tool import (
    InfoSQLDatabaseTool,
    ListSQLDatabaseTool,
    QuerySQLCheckerTool,
    QuerySQLDataBaseTool,
)
from langchain_core.tools import BaseTool
from langchain_openai import ChatOpenAI, OpenAI
from langchain_community.utilities.requests import TextRequestsWrapper
from langchain_community.agent_toolkits import JsonToolkit, create_json_agent
from langchain_community.tools.json.tool import JsonSpec
from langchain_community.tools.requests.tool import (
    RequestsDeleteTool,
    RequestsGetTool,
    RequestsPatchTool,
    RequestsPostTool,
    RequestsPutTool,
)
from .langchain_registers import register_function_tools, register_api_tools


# 从app_engine加密传输敏感信息
def get_db(sql_url: str, sql_table: str, sql_name: str, sql_pwd: str) -> SQLDatabase:
    return SQLDatabase.from_uri(
        "postgresql+psycopg2://%s:%s@%s/%s" % (quote_plus(sql_name), quote_plus(sql_pwd), sql_url,
                                               quote_plus(sql_table)))


def langchain_sql_query(kwargs) -> BaseTool:
    db = get_db(kwargs.get("sql_url"), kwargs.get("sql_table"), kwargs.get("sql_name"), kwargs.get("sql_pwd"))

    query_sql_database_tool_description = (
        "Input to this tool is a detailed and correct SQL query, output is a "
        "result from the database. If the query is not correct, an error message "
        "will be returned. If an error is returned, rewrite the query, check the "
        "query, and try again. If you encounter an issue with Unknown column "
        "'xxxx' in 'field list', use sql_db_schema "
        "to query the correct table fields."
    )

    query_sql_database_tool = QuerySQLDataBaseTool(
        db=db, description=query_sql_database_tool_description
    )
    return query_sql_database_tool


def langchain_sql_info(kwargs) -> BaseTool:
    db = get_db(kwargs.get("sql_url"), kwargs.get("sql_table"), kwargs.get("sql_name"), kwargs.get("sql_pwd"))

    info_sql_database_tool_description = (
        "Input to this tool is a comma-separated list of tables, output is the "
        "schema and sample rows for those tables. "
        "Be sure that the tables actually exist by calling "
        "sql_db_list_tables first! "
        "Example Input: table1, table2, table3"
    )
    info_sql_database_tool = InfoSQLDatabaseTool(
        db=db, description=info_sql_database_tool_description
    )
    return info_sql_database_tool


def langchain_sql_list(kwargs) -> BaseTool:
    db = get_db(kwargs.get("sql_url"), kwargs.get("sql_table"), kwargs.get("sql_name"), kwargs.get("sql_pwd"))

    list_sql_database_tool = ListSQLDatabaseTool(db=db)
    return list_sql_database_tool


def langchain_sql_checker(kwargs) -> BaseTool:
    api_key = kwargs.get("api_key") or "EMPTY"
    model_name = kwargs.get("model_name")
    api_base = kwargs.get("api_base")
    temperature = kwargs.get("temperature") or 0

    db = get_db(kwargs.get("sql_url"), kwargs.get("sql_table"), kwargs.get("sql_name"), kwargs.get("sql_pwd"))
    llm = ChatOpenAI(model_name=model_name, openai_api_base=api_base, openai_api_key=api_key, temperature=temperature)

    query_sql_checker_tool_description = (
        "Use this tool to double check if your query is correct before executing "
        "it. Always use this tool before executing a query with "
        "sql_db_query!"
    )
    query_sql_checker_tool = QuerySQLCheckerTool(
        db=db, llm=llm, description=query_sql_checker_tool_description
    )
    return query_sql_checker_tool


def langchain_sql_agent(kwargs) -> AgentExecutor:
    api_key = kwargs.get("api_key") or "EMPTY"
    model_name = kwargs.get("model_name")
    api_base = kwargs.get("api_base")
    temperature = kwargs.get("temperature") or 0

    db = get_db(kwargs.get("sql_url"), kwargs.get("sql_table"), kwargs.get("sql_name"), kwargs.get("sql_pwd"))
    llm = ChatOpenAI(model_name=model_name, openai_api_base=api_base, openai_api_key=api_key, temperature=temperature)
    agent_executor = create_sql_agent(llm, db=db)
    return agent_executor


def langchain_request_get(kwargs) -> BaseTool:
    return RequestsGetTool(
        requests_wrapper=TextRequestsWrapper(headers={}),
        allow_dangerous_requests=True,
    )


def langchain_request_post(kwargs) -> BaseTool:
    return RequestsPostTool(
        requests_wrapper=TextRequestsWrapper(headers={}),
        allow_dangerous_requests=True,
    )


def langchain_request_patch(kwargs) -> BaseTool:
    return RequestsPatchTool(
        requests_wrapper=TextRequestsWrapper(headers={}),
        allow_dangerous_requests=True,
    )


def langchain_request_delete(kwargs) -> BaseTool:
    return RequestsDeleteTool(
        requests_wrapper=TextRequestsWrapper(headers={}),
        allow_dangerous_requests=True,
    )


def langchain_request_put(kwargs) -> BaseTool:
    return RequestsPutTool(
        requests_wrapper=TextRequestsWrapper(headers={}),
        allow_dangerous_requests=True,
    )


def langchain_json_agent(kwargs) -> AgentExecutor:
    json_str = kwargs.get("json_str")
    api_key = kwargs.get("api_key") or "EMPTY"
    model_name = kwargs.get("model_name")
    api_base = kwargs.get("api_base")
    temperature = kwargs.get("temperature") or 0
    llm = ChatOpenAI(openai_api_base=api_base, openai_api_key=api_key,
                     model=model_name, temperature=temperature)
    json_spec = JsonSpec(dict_=json.loads(json_str), max_value_length=4000)
    json_toolkit = JsonToolkit(spec=json_spec)
    json_agent_executor = create_json_agent(llm=llm, toolkit=json_toolkit, verbose=True)
    return json_agent_executor


# function tools
function_tools = []
register_function_tools(function_tools)

api_tools = [
    (langchain_sql_query, ["sql_url", "sql_table", "sql_name", "sql_pwd"], "sql_db_query"),
    (langchain_sql_info, ["sql_url", "sql_table", "sql_name", "sql_pwd"], "sql_db_schema"),
    (langchain_sql_list, ["sql_url", "sql_table", "sql_name", "sql_pwd"], "sql_db_list_tables"),
    (langchain_sql_checker,
     ["model_name", "api_key", "api_base", "sql_url", "sql_table", "sql_name", "sql_pwd", "temperature"],
     "sql_db_query_checker"),
    (langchain_sql_agent,
     ["model_name", "api_key", "api_base", "sql_url", "sql_table", "sql_name", "sql_pwd", "temperature"], "sql_agent"),
    (langchain_request_get, ["url"], "request_get"),
    (langchain_request_put, ["url"], "request_put"),
    (langchain_request_post, ["url"], "request_post"),
    (langchain_request_delete, ["url"], "request_delete"),
    (langchain_request_patch, ["url"], "request_patch"),
    (langchain_json_agent, ["model_name", "api_key", "api_base", "temperature", "json_str", "input"], "json_agent")
]
# api tools
for tool in api_tools:
    register_api_tools(tool[0], tool[1], tool[2])

if __name__ == "__main__":
    import time
    from .langchain_schema_helper import dump_schema

    current_timestamp = time.strftime('%Y%m%d%H%M%S')
    dump_schema(function_tools, f"./tool_schema-{str(current_timestamp)}.json")
