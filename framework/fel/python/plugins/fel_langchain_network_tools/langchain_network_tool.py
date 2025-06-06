# -- encoding: utf-8 --
# Copyright (c) 2024 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
import os
import time
from typing import List, Any, Callable, Tuple

from langchain_community.retrievers import ArxivRetriever
from langchain_community.tools import WikipediaQueryRun, DuckDuckGoSearchRun, YouTubeSearchTool, GoogleSearchRun, \
    PubmedQueryRun, GooglePlacesTool, BraveSearch, MojeekSearch
from langchain_community.tools.google_jobs import GoogleJobsQueryRun
from langchain_community.tools.google_scholar import GoogleScholarQueryRun
from langchain_community.tools.reddit_search.tool import RedditSearchRun, RedditSearchSchema
from langchain_community.tools.wikidata.tool import WikidataQueryRun
from langchain_community.utilities import WikipediaAPIWrapper, GoogleSearchAPIWrapper, GoogleSerperAPIWrapper, \
    WolframAlphaAPIWrapper, GoogleJobsAPIWrapper, GoogleScholarAPIWrapper, BingSearchAPIWrapper, \
    GoldenQueryAPIWrapper, SearxSearchWrapper, SerpAPIWrapper, TwilioAPIWrapper
from langchain_community.utilities.reddit_search import RedditSearchAPIWrapper
from langchain_community.utilities.wikidata import WikidataAPIWrapper
from langchain_core.documents import Document

from .callable_registers import register_callable_tool


def langchain_network(**kwargs) -> str:
    time.sleep(5)
    return ""


def arxiv(arxiv_id: str, **kwargs) -> List[str]:
    retriever = ArxivRetriever(load_max_docs=2)
    docs: List[Document] = retriever.get_relevant_documents(query=arxiv_id)
    return [doc.page_content for doc in docs]


def bing_search(query: str, bing_subscription_key: str, bing_search_url: str, **kwargs) -> str:
    os.environ["BING_SUBSCRIPTION_KEY"] = bing_subscription_key
    os.environ["BING_SEARCH_URL"] = bing_search_url
    search = BingSearchAPIWrapper()
    return search.run(query)


def brave_search(query: str, count: int, api_key: str, **kwargs) -> str:
    brave_search_ = BraveSearch.from_api_key(api_key=api_key, search_kwargs={"count": count})
    return brave_search_.run(query)


def duck_duck_go_search(query: str, **kwargs) -> str:
    search = DuckDuckGoSearchRun()
    return search.invoke(query)


def google_jobs(query: str, serapi_api_key: str, **kwargs) -> str:
    os.environ["SERPAPI_API_KEY"] = serapi_api_key
    google_job_tool = GoogleJobsQueryRun(api_wrapper=GoogleJobsAPIWrapper())
    return google_job_tool.run(query)


def google_places(query: str, gplaces_api_key: str, **kwargs) -> str:
    os.environ["GPLACES_API_KEY"] = gplaces_api_key
    places = GooglePlacesTool()
    return places.run(query)


def google_scholar(query: str, serp_api_key: str, **kwargs) -> str:
    os.environ["SERP_API_KEY"] = serp_api_key
    google_job_tool = GoogleScholarQueryRun(api_wrapper=GoogleScholarAPIWrapper())
    return google_job_tool.run(query)


def google_search(query: str, google_api_key: str, google_cse_id: str, k: int, siterestrict: bool, **kwargs) -> str:
    wrapper = GoogleSearchAPIWrapper(google_api_key=google_api_key, google_cse_id=google_cse_id, k=k,
                                     siterestrict=siterestrict)
    search = GoogleSearchRun(api_wrapper=wrapper)
    return search.run(query)


def google_serper(query: str, k: int, gl: str, hl: str, serper_api_key: str, **kwargs) -> str:
    os.environ["SERPER_API_KEY"] = serper_api_key
    search = GoogleSerperAPIWrapper(k=k, gl=gl, hl=hl)
    return search.run(query)


def golden_query(query: str, golden_api_key: str, **kwargs) -> str:
    os.environ["GOLDEN_API_KEY"] = golden_api_key
    golden_query_api = GoldenQueryAPIWrapper()
    return golden_query_api.run(query)


def pub_med(query: str) -> str:
    pub_med_tool: PubmedQueryRun = PubmedQueryRun()
    return pub_med_tool.invoke(query)


def mojeek_query(query: str, api_key: str) -> str:
    search = MojeekSearch.config(api_key=api_key)
    return search.run(query)


def reddit_search(query: str, sort: str, time_filter: str, subreddit: str, limit: str, client_id: str,
                  client_secret: str, user_agent: str) -> str:
    search = RedditSearchRun(
        api_wrapper=RedditSearchAPIWrapper(
            reddit_client_id=client_id,
            reddit_client_secret=client_secret,
            reddit_user_agent=user_agent,
        )
    )
    search_params = RedditSearchSchema(query=query, sort=sort, time_filter=time_filter, subreddit=subreddit,
                                       limit=limit)
    result = search.run(tool_input=search_params.dict())
    return result


def searxng_search(query: str, searx_host: str) -> str:
    search = SearxSearchWrapper(searx_host=searx_host)
    return search.run(query)


def serp_api(query: str, serpapi_api_key: str) -> str:
    search = SerpAPIWrapper(serpapi_api_key=serpapi_api_key)
    return search.run(query)


def twilio(body: str, to: str, account_sid: str, auth_token: str, from_number: str) -> str:
    twilio_api = TwilioAPIWrapper(
        account_sid=account_sid,
        auth_token=auth_token,
        from_number=from_number
    )
    return twilio_api.run(body, to)


def wikidata(query: str) -> str:
    wikidata_query = WikidataQueryRun(api_wrapper=WikidataAPIWrapper())
    return wikidata_query.run(query)


def wikipedia(query: str, **kwargs) -> str:
    wikipedia_query_run = WikipediaQueryRun(api_wrapper=WikipediaAPIWrapper())
    return wikipedia_query_run.run(query)


def wolfram_alpha(query: str, wolfram_alpha_appid: str) -> str:
    wolfram = WolframAlphaAPIWrapper(wolfram_alpha_appid=wolfram_alpha_appid)
    return wolfram.run(query)


def youtube_search(query: str, **kwargs) -> str:
    youtube_search_tool = YouTubeSearchTool()
    return youtube_search_tool.run(query)


# Tuple 结构： (tool_func, config_args, return_description)
network_toolkit: List[Tuple[Callable[..., Any], List[str], str]] = [
    (langchain_network, ["input"], "Youtube search."),
    (arxiv, ["arxiv_id"], "ArXiv search."),
    (bing_search, ["query", "bing_subscription_key", "bing_search_url"], "Bing search."),
    (brave_search, ["query", "count", "api_key"], "Brave search."),
    (duck_duck_go_search, ["query"], "DuckDuckGo Search."),
    (google_jobs, ["query", "serapi_api_key"], "Google Jobs."),
    (google_places, ["query", "gplaces_api_key"], "Google Places."),
    (google_scholar, ["query", "serp_api_key"], "Google Scholar."),
    (google_search, ["query", "google_api_key", "google_cse_id", "k", "siterestrict"], "Google Search."),
    (google_serper, ["query", "serper_api_key", "k", "gl", "hl"], "Google Serper."),
    (golden_query, ["query", "golden_api_key"], "Golden Query."),
    (pub_med, ["query"], "PubMed."),
    (mojeek_query, ["query", "api_key"], "Mojeek Search."),
    (reddit_search, ["query", "sort", "time_filter", "subreddit", "limit", "client_id", "client_secret", "user_agent"],
     "Reddit Search."),
    (searxng_search, ["query", "searx_host"], "SearxNG Search."),
    (serp_api, ["query", "serpapi_api_key"], "SerpAPI."),
    (twilio, ["body", "to", "account_sid", "auth_token", "from_number"], "Twilio."),
    (wikidata, ["query"], "Wikidata."),
    (wikipedia, ["query"], "Wikipedia."),
    (wolfram_alpha, ["query", "wolfram_alpha_appid"], "Wolfram Alpha."),
    (youtube_search, ["query"], "Youtube Search."),
]

for tool in network_toolkit:
    register_callable_tool(tool, langchain_network.__module__, "langchain.tool")
