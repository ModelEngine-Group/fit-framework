# 上传PyPI仓库流程

## 一、创建 PyPI 账号
- 访问 PyPI 官网（https://pypi.org/）并注册一个账号。（已注册可跳过）
- 建议同时注册一个 TestPyPI(https://test.pypi.org/) 账号，用于测试上传,避免污染正式仓库。
## 二、安装必要工具
需要安装打包和上传相关的 Python 工具：
```bash
pip install build twine
```
- `build` 用于构建 Python 项目。
- `twine` 用于上传构建的包到 PyPI。
## 三、配置打包文件
在项目的根目录下，创建一个 `pyproject.toml` 文件，并配置基本的项目信息。例如：
```toml
[build-system]
requires = ["setuptools>=61.0", "wheel"]
build-backend = "setuptools.build_meta"

[project]
name = "fit-plugin-cli"
version = "0.1.0"
authors = [
    { name = "Akeyiii", email = "abc@example.com" }
]
description = "CLI tool for FIT framework plugin development"
license = "MIT"
keywords = ["fit", "cli", "plugin"]
requires-python = ">=3.9"

dependencies = [
]

[project.scripts]
fit_cli = "fit_cli.main:main"
fit = "fit_cli.main:main"

[tool.setuptools]
packages = [
    "fit_cli",
    "fit_cli.commands",
    "fit_cli.utils",
]

```
## 四、构建包
在项目根目录运行以下命令来构建包：
```bash
python -m build
```
这将生成一个名为 `dist/` 的文件夹，里面包含了 `.tar.gz` 和 `.whl` 等格式的发布文件。
## 五、上传至仓库
### 1.上传至 TestPyPI （推荐测试）
首先，配置 Twine 使用 TestPyPI 的仓库地址：
```bash
twine upload --repository testpypi dist/*
```
- 根据提示输入 TestPyPI 账号设置中生成的API token。
- 测试安装：
```bash
pip install -i https://test.pypi.org/simple/ fit-plugin-cli
```
- 验证功能：执行 `fit --help` 或其他命令，确认命令可用。
### 2.上传至正式 PyPI
使用以下命令上传到 PyPI：
```bash
twine upload dist/*
```
- 根据提示输入 PyPI 账号设置中生成的API token。
- 测试安装：
```bash
pip install fit-plugin-cli
```
- 验证功能：执行 `fit --help` 或其他命令，确认命令可用。
## 注意事项
- 版本号规范：每次上传必须使用新的版本号（如 0.1.1、0.2.0），PyPI 不允许重复版本号。
- 依赖管理：确保 `pyproject.toml` 中的依赖项正确无误。