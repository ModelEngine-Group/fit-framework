# -- encoding: utf-8 --
# Copyright (c) 2025 Huawei Technologies Co., Ltd. All Rights Reserved.
# This file is a part of the ModelEngine Project.
# Licensed under the MIT License. See License.txt in the project root for license information.
# ======================================================================================================================
"""
Async executor for Nacos operations.

This module provides an async executor for handling Nacos operations
in a background thread with proper event loop management.
"""
import asyncio
import atexit
import threading
from concurrent.futures import Future
from fitframework import value

from v2.nacos import NacosNamingService, RegisterInstanceParam, ListInstanceParam, \
    DeregisterInstanceParam, SubscribeServiceParam, ListServiceParam

from fitframework.api.logging import sys_plugin_logger
from .config import build_nacos_config
@value("nacos.async.timeout",default_value=10, converter=int)
def get_nacos_async_timeout():
    pass

class AsyncExecutor:
    """Executor for handling asynchronous operations in a background thread."""

    def __init__(self):
        self._loop = None
        self._thread = None
        self._started = False
        self._shutdown = False
        self._nacos_client = None
        self._init_complete = threading.Event()

    def start(self):
        """Start the background event loop thread."""
        if self._started:
            return

        self._thread = threading.Thread(
            target=self._run_event_loop, 
            daemon=True, 
            name="NacosAsyncThread"
        )
        self._thread.start()

        # Wait for initialization to complete
        if not self._init_complete.wait(timeout=10):  # Max wait 10 seconds
            raise RuntimeError("Failed to initialize async executor within timeout")

        self._started = True

    def _run_event_loop(self):
        """Run the event loop in the background thread."""
        try:
            self._loop = asyncio.new_event_loop()
            asyncio.set_event_loop(self._loop)

            # Create Nacos client in this event loop
            async def init_nacos_client():
                try:
                    config = build_nacos_config()
                    self._nacos_client = await NacosNamingService.create_naming_service(config)
                    sys_plugin_logger.info("Nacos client initialized successfully")
                except Exception as e:
                    sys_plugin_logger.error(f"Failed to initialize Nacos client: {e}")
                    raise
                finally:
                    # Mark initialization complete
                    self._init_complete.set()

            self._loop.run_until_complete(init_nacos_client())

            # Run event loop until shutdown
            self._loop.run_forever()
        except Exception as e:
            sys_plugin_logger.error(f"Error in async executor event loop: {e}")
            self._init_complete.set()  # Set even on failure to avoid infinite wait
        finally:
            try:
                if self._nacos_client:
                    # Try to close the client if it has a close method
                    try:
                        self._nacos_client.shutdown()
                        sys_plugin_logger.info("Nacos client cleaned up")
                    except Exception as cleanup_error:
                        sys_plugin_logger.error(f"Error cleaning up Nacos client: {cleanup_error}")
                        
                if self._loop and not self._loop.is_closed():
                    self._loop.close()
            except Exception as e:
                sys_plugin_logger.error(f"Error during cleanup: {e}")

    def run_coroutine(self, coro):
        """
        Run a coroutine in the background event loop and return the result.
        
        Args:
            coro: The coroutine to run.
            
        Returns:
            The result of the coroutine.
            
        Raises:
            RuntimeError: If the executor is not properly initialized.
        """
        if not self._started:
            self.start()

        if self._loop is None or self._nacos_client is None:
            raise RuntimeError("Async executor not properly initialized")

        # Create a Future to get the result
        result_future = Future()

        async def wrapped_coro():
            try:
                result = await coro
                result_future.set_result(result)
            except Exception as e:
                result_future.set_exception(e)

        # Schedule the coroutine in the event loop
        self._loop.call_soon_threadsafe(asyncio.create_task, wrapped_coro())

        # Wait for result
        return result_future.result(timeout=get_nacos_async_timeout())

    def get_nacos_client(self):
        """
        Get the Nacos client instance.
        
        Returns:
            The Nacos client instance.
        """
        if not self._started:
            self.start()
        return self._nacos_client

    def shutdown(self):
        """Shutdown the async executor."""
        if self._loop and not self._loop.is_closed():
            self._loop.call_soon_threadsafe(self._loop.stop)
        self._shutdown = True


# Global async executor
_async_executor = AsyncExecutor()


def run_async_safely(coro):
    """
    Run an async operation safely using the dedicated executor.

    Args:
        coro: The coroutine to run.

    Returns:
        The result of the coroutine.
        
    Raises:
        Exception: If the async operation fails.
    """
    try:
        return _async_executor.run_coroutine(coro)
    except Exception as e:
        sys_plugin_logger.error(f"Error running async operation: {e}")
        raise


# Async wrapper functions
async def call_list_instances(param: ListInstanceParam):
    """List instances."""
    client = _async_executor.get_nacos_client()
    return await client.list_instances(param)


async def call_deregister_instance(param: DeregisterInstanceParam) -> bool:
    """Deregister instance."""
    client = _async_executor.get_nacos_client()
    return await client.deregister_instance(param)


async def call_subscribe(param: SubscribeServiceParam) -> None:
    """Subscribe to service."""
    client = _async_executor.get_nacos_client()
    await client.subscribe(param)


async def call_unsubscribe(param: SubscribeServiceParam) -> None:
    """Unsubscribe from service."""
    client = _async_executor.get_nacos_client()
    await client.unsubscribe(param)


async def call_list_services(param: ListServiceParam):
    """List services."""
    client = _async_executor.get_nacos_client()
    return await client.list_services(param)


async def call_register_instance(param: RegisterInstanceParam) -> None:
    """Register instance."""
    client = _async_executor.get_nacos_client()
    await client.register_instance(param)


def _cleanup_async_executor():
    """Cleanup the async executor."""
    try:
        _async_executor.shutdown()
    except Exception as e:
        sys_plugin_logger.error(f"Error during async executor cleanup: {e}")


# Register cleanup function
atexit.register(_cleanup_async_executor)
