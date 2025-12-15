[toc]

# FIT for Python

## 框架简介

FIT for Python 是基于 FIT Framework 的 Python 运行时与插件开发套件，涵盖：
- 运行时：启动框架、加载插件、提供生命周期与健康检查。
- CLI 工具：初始化、构建、打包插件，提升开发效率。
- 插件样例与配置：便于本地快速体验与二次开发。

## 目录结构（节选）

- `fitframework/`：核心运行时代码与启动入口（`python -m fitframework`）。
- `fit_cli/`：命令行工具，支持插件的 init/build/package。
- `plugin/`：本地插件工程根目录，使用 CLI 生成与构建。
- `conf/`：框架及插件相关配置。
- `bootstrap/`：运行时启动与配置加载的底层实现。
- `requirements.txt`：运行时依赖列表。
- `fit_common_struct/`：框架通用数据结构与工具。
- `fit_flowable/`：流程/可流式组件相关实现。
- `fit_py_nacos_registry/`：Nacos 注册中心适配。
- `fit_test/`：示例与自测脚本。
- `third_party_cache/`：依赖缓存或临时文件。
- `fit_framework.log`：默认运行日志文件，便于排查。

## 配置说明

- 默认配置位于 `conf/`，包括 `application.yml`、`fit.yml`、`fit_startup.yml` 等。
- 注册中心：`conf/application.yml` 中 `registry-center` 配置为框架发现和加载插件的前置条件，需保证注册中心已启动（可参考 Java 框架的本地注册中心启动方式），并与 `server.addresses` 等参数保持一致。
- 如使用直连内存注册中心，默认 `mode: DIRECT`，地址示例 `localhost:8080`；如使用代理/Nacos，请按实际环境调整 `mode`、`addresses`、`protocol` 等字段。
- 启动前请根据本地环境核对端口、协议及上下文路径，必要时同步修改插件侧的配置文件。

## 源码准备

下载代码，其中 `framework/fit/python` 目录即为 FIT for Python 工程根目录，可将该目录作为 PyCharm 和 VS Code 的工程根目录打开。

## 环境准备

需要在 Python 3.9 及以上版本安装 `requirements.txt` 中的第三方依赖，当前依赖如下：

```python
numpy==1.25.2
PyYAML==6.0.1
requests==2.31.0
tornado==6.3.2
```

推荐在虚拟环境中安装依赖：
```bash
python -m venv .venv   # 创建虚拟环境
source .venv/bin/activate   # Windows 可执行 .\.venv\Scripts\activate
pip install -r requirements.txt
```

## 快速开始

1. 进入工程根目录：`cd framework/fit/python`。
2. 创建并激活虚拟环境，安装依赖：同上所示。
3. 按需修改 `conf/application.yml` 中的 `registry-center` 和端口配置。
4. 启动：`python -m fitframework`，观察终端或 `fit_framework.log` 是否有错误。
5. 健康检查：按下文 curl 示例确认返回 `OK`。

## 启动框架

在项目根目录执行：
```bash
python -m fitframework
```
默认会启动本地服务并按配置加载插件；进程前台运行，终端保持开启即可。

## 运行校验

启动后可通过健康检查确认框架与插件是否正常加载：

```bash
curl --request GET \
  --url http://localhost:9666/fit/health \
  --header 'FIT-Data-Format: 1' \
  --header 'FIT-Genericable-Version: 1.0.0' \
  --header 'FIT-Version: 2'
```

若返回 `OK` 表示框架已正常启动且插件加载成功。

## 插件开发与构建（简要）

1. 初始化插件工程（在项目根目录）：
   ```bash
   python -m fit_cli init your_plugin_name
   ```
2. 开发完成后构建与打包：
   ```bash
   python -m fit_cli build your_plugin_name
   python -m fit_cli package your_plugin_name
   ```
   生成的产物位于 `plugin/your_plugin_name/build/`。

更多 CLI 细节可参考 `fit_cli/readme.md`。

## 常见排查

- 启动报端口占用：调整 `conf/fit_startup.yml` 或 `application.yml` 中的端口后重启。
- 注册中心连通性：确认 `registry-center.addresses` 可达，必要时先用 curl/ping 验证。
- 重新安装依赖：在已激活虚拟环境中执行 `pip install -r requirements.txt --force-reinstall`。
- 停止服务：直接中断前台进程（Ctrl+C），或关闭终端会话。