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
- `pyproject.toml`：项目配置与运行时依赖列表。
- `fit_common_struct/`：框架通用数据结构与工具。
- `fit_flowable/`：流程/可流式组件相关实现。
- `fit_py_nacos_registry/`：Nacos 注册中心适配。
- `fit_test/`：示例与自测脚本。
- `third_party_cache/`：依赖缓存或临时文件。
- `fit_framework.log`：默认运行日志文件，便于排查。

## 配置说明

- 默认配置位于 `conf/`，包括 `application.yml`、`fit.yml`、`fit_startup.yml` 等。
- 注册中心：`conf/application.yml` 中 `registry-center` 配置为框架发现和加载插件的前置条件，需保证注册中心已启动并与 `server.addresses` 等参数保持一致。推荐先启动 Java 内存版注册中心，参考 `../registry-center.md`。
- 如使用直连内存注册中心，默认 `mode: DIRECT`，地址示例 `localhost:8080`；如使用代理/Nacos，请按实际环境调整 `mode`、`addresses`、`protocol` 等字段。
- 启动前请根据本地环境核对端口、协议及上下文路径，必要时同步修改插件侧的配置文件。

## 源码准备

下载代码，其中 `framework/fit/python` 目录即为 FIT for Python 工程根目录，可将该目录作为 PyCharm 和 VS Code 的工程根目录打开。

## 环境准备

### Python 版本要求

**当前测试版本：Python 3.9.25**

项目已配置 `.python-version` 文件指定版本。如使用 pyenv，会自动切换到对应版本。

**版本兼容性说明：**
- 依赖包（特别是 numpy>=1.25.2）已针对 Python 3.9 进行测试
- 更高版本（如 3.14+）可能存在兼容性问题
- 升级 Python 版本时，请同步更新 `pyproject.toml` 中的依赖版本

### 安装依赖

本项目使用 **uv** 作为依赖管理工具，提供更快的依赖解析和安装速度。当前依赖如下（定义在 `pyproject.toml` 中）：

```toml
numpy>=1.25.2,<2.0.0
pyyaml>=6.0.1,<7.0.0
requests>=2.32.4,<3.0.0
tornado>=6.5.0,<7.0.0
```

#### 安装 uv

如果还没有安装 uv，请先安装：

```bash
# macOS
brew install uv

# Linux
curl -LsSf https://astral.sh/uv/install.sh | sh

# Windows
powershell -c "irm https://astral.sh/uv/install.ps1 | iex"

# 或使用 pip 安装
pip install uv
```

#### 使用 uv 安装依赖

推荐使用 uv 管理虚拟环境和依赖：

```bash
# 使用 uv sync
# uv 会自动创建虚拟环境并安装所有依赖
uv sync
```

**注意：**
- uv 会自动管理虚拟环境，默认创建在 `.venv` 目录
- 使用 `uv sync` 时会自动激活虚拟环境并安装依赖
- 虚拟环境激活后，`python` 命令即可直接使用

## 快速开始

1. 进入工程根目录：`cd framework/fit/python`。
2. 安装依赖：执行 `uv sync` 自动创建虚拟环境并安装所有依赖。
3. 按需修改 `conf/application.yml` 中的 `registry-center` 和端口配置。
4. 启动：`uv run python -m fitframework`，观察终端或 `fit_framework.log` 是否有错误。
5. 健康检查：按下文 curl 示例确认返回 `OK`。

## 启动框架

在项目根目录执行：
```bash
# 使用 uv 运行
uv run python -m fitframework
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
   uv run python -m fit_cli init your_plugin_name
   ```
2. 开发完成后构建与打包：
   ```bash
   uv run python -m fit_cli build your_plugin_name
   uv run python -m fit_cli package your_plugin_name
   ```
   生成的产物位于 `plugin/your_plugin_name/build/`。

更多 CLI 细节可参考 `fit_cli/readme.md`。

## 常见排查

- 启动报端口占用：调整 `conf/fit_startup.yml` 或 `application.yml` 中的端口后重启。
- 注册中心连通性：确认 `registry-center.addresses` 可达，必要时先用 curl/ping 验证。
- 重新安装依赖：执行 `uv sync --reinstall` 重新安装所有依赖。
- 清理并重建环境：删除 `.venv` 目录后重新执行 `uv sync`。
- 停止服务：直接中断前台进程（Ctrl+C），或关闭终端会话。
