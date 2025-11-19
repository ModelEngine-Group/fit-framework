# FIT CLI 工具

FIT CLI 工具是基于 **FIT Framework** 的命令行开发工具，提供插件初始化、构建、打包等功能，帮助用户快速开发和管理 FIT 插件。

---

## 使用方式

FIT CLI 支持 3 个核心子命令：init（初始化）、build（构建）、package（打包），以下是详细说明。

### init

以 framework/fit/python 为项目根目录，运行：

```bash
python -m fit_cli init %{your_plugin_name}
```
· 参数：``%{your_plugin_name}``: 自定义插件名称

会在 ``plugin`` 目录中创建如下结构：

    └── plugin/
        └──%{your_plugin_name}/
            └── src/
                ├── __init__.py
                └── plugin.py  # 插件源码模板

### build

在完成插件的开发后，执行
```bash
python -m fit_cli build %{your_plugin_name}
```
· 参数：``%{your_plugin_name}``: 插件目录名称

在 ``%{your_plugin_name}`` 目录生成:

    └──%{your_plugin_name}/
        └── build/
            ├── %{your_plugin_name}.tar # 插件源码打包文件（工具包）。
            ├── tools.json # 工具的元数据。
            └── plugin.json # 插件的完整性校验与唯一性校验以及插件的基本信息。

开发者可根据自己的需要，修改完善``tools.json`` 和 ``plugin.json`` 文件，比如修改 ``description`` 、 ``uniqueness``等条目。

### package

在完成插件的构建后，执行
```bash
python -m fit_cli package %{your_plugin_name}
```
· 参数：``%{your_plugin_name}``: 插件目录名称

在 ``plugin/%{your_plugin_name}/build/`` 目录生成最终打包文件: ``%{your_plugin_name}_package.zip``

---

## pip 安装

FIT CLI 工具可通过 pip 进行安装：
```bash
pip install fit-plugin-cli
```
安装后，可以简化命令为：
```bash
fit init %{your_plugin_name}
fit_cli init %{your_plugin_name}

fit build %{your_plugin_name}
fit_cli build %{your_plugin_name}

fit package %{your_plugin_name}
fit_cli package %{your_plugin_name}
```

## 注意事项

1. 运行命令前，请切换至 framework/fit/python 项目根目录。
2. 更多详细信息和使用说明，可参考 https://github.com/ModelEngine-Group/fit-framework 官方仓库。