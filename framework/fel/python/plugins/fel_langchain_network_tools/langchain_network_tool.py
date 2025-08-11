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

from fitframework.api.decorators import fitable

def langchain_network() -> str:
    time.sleep(5)
    return ""

@fitable("langchain.tool.arxiv", "default")
def arxiv(arxiv_id: str) -> List[str]:
    retriever = ArxivRetriever(load_max_docs=2)
    docs: List[Document] = retriever.get_relevant_documents(query=arxiv_id)
    return [doc.page_content for doc in docs]

@fitable("langchain.tool.bing_search", "default")
def bing_search(query: str, bing_subscription_key: str, bing_search_url: str) -> str:
    os.environ["BING_SUBSCRIPTION_KEY"] = bing_subscription_key
    os.environ["BING_SEARCH_URL"] = bing_search_url
    search = BingSearchAPIWrapper()
    return search.run(query)

@fitable("langchain.tool.brave_search", "default")
def brave_search(query: str, count: int, api_key: str) -> str:
    brave_search_ = BraveSearch.from_api_key(api_key=api_key, search_kwargs={"count": count})
    return brave_search_.run(query)

@fitable("langchain.tool.duck_duck_go_search", "default")
def duck_duck_go_search(query: str) -> str:
    search = DuckDuckGoSearchRun()
    return search.invoke(query)

@fitable("langchain.tool.google_jobs", "default")
def google_jobs(query: str, serapi_api_key: str) -> str:
    os.environ["SERPAPI_API_KEY"] = serapi_api_key
    google_job_tool = GoogleJobsQueryRun(api_wrapper=GoogleJobsAPIWrapper())
    return google_job_tool.run(query)

@fitable("langchain.tool.google_places", "default")
def google_places(query: str, gplaces_api_key: str) -> str:
    os.environ["GPLACES_API_KEY"] = gplaces_api_key
    places = GooglePlacesTool()
    return places.run(query)

@fitable("langchain.tool.google_scholar", "default")
def google_scholar(query: str, serp_api_key: str) -> str:
    os.environ["SERP_API_KEY"] = serp_api_key
    google_job_tool = GoogleScholarQueryRun(api_wrapper=GoogleScholarAPIWrapper())
    return google_job_tool.run(query)

@fitable("langchain.tool.google_search", "default")
def google_search(query: str, google_api_key: str, google_cse_id: str, k: int, siterestrict: bool) -> str:
    wrapper = GoogleSearchAPIWrapper(google_api_key=google_api_key, google_cse_id=google_cse_id, k=k,
                                     siterestrict=siterestrict)
    search = GoogleSearchRun(api_wrapper=wrapper)
    return search.run(query)

@fitable("langchain.tool.google_serper", "default")
def google_serper(query: str, k: int, gl: str, hl: str, serper_api_key: str) -> str:
    os.environ["SERPER_API_KEY"] = serper_api_key
    search = GoogleSerperAPIWrapper(k=k, gl=gl, hl=hl)
    return search.run(query)

@fitable("langchain.tool.golden_query", "default")
def golden_query(query: str, golden_api_key: str) -> str:
    os.environ["GOLDEN_API_KEY"] = golden_api_key
    golden_query_api = GoldenQueryAPIWrapper()
    return golden_query_api.run(query)

@fitable("langchain.tool.pub_med", "default")
def pub_med(query: str) -> str:
    pub_med_tool: PubmedQueryRun = PubmedQueryRun()
    return pub_med_tool.invoke(query)

@fitable("langchain.tool.mojeek_query", "default")
def mojeek_query(query: str, api_key: str) -> str:
    search = MojeekSearch.config(api_key=api_key)
    return search.run(query)

@fitable("langchain.tool.reddit_search", "default")
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

@fitable("langchain.tool.searxng_search", "default")
def searxng_search(query: str, searx_host: str) -> str:
    search = SearxSearchWrapper(searx_host=searx_host)
    return search.run(query)

@fitable("langchain.tool.serp_api", "default")
def serp_api(query: str, serpapi_api_key: str) -> str:
    search = SerpAPIWrapper(serpapi_api_key=serpapi_api_key)
    return search.run(query)

@fitable("langchain.tool.twilio", "default")
def twilio(body: str, to: str, account_sid: str, auth_token: str, from_number: str) -> str:
    twilio_api = TwilioAPIWrapper(
        account_sid=account_sid,
        auth_token=auth_token,
        from_number=from_number
    )
    return twilio_api.run(body, to)

@fitable("langchain.tool.wikidata", "default")
def wikidata(query: str) -> str:
    wikidata_query = WikidataQueryRun(api_wrapper=WikidataAPIWrapper())
    return wikidata_query.run(query)

@fitable("langchain.tool.wikipedia", "default")
def wikipedia(query: str) -> str:
    wikipedia_query_run = WikipediaQueryRun(api_wrapper=WikipediaAPIWrapper())
    return wikipedia_query_run.run(query)

@fitable("langchain.tool.wolfram_alpha", "default")
def wolfram_alpha(query: str, wolfram_alpha_appid: str) -> str:
    wolfram = WolframAlphaAPIWrapper(wolfram_alpha_appid=wolfram_alpha_appid)
    return wolfram.run(query)

@fitable("langchain.tool.youtube_search", "default")
def youtube_search(query: str) -> str:
    youtube_search_tool = YouTubeSearchTool()
    return youtube_search_tool.run(query)
