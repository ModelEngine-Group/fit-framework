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
· 参数：%{your_plugin_name} - 自定义插件名称

会在 plugin 目录中创建 %{your_plugin_name} 目录，包含源代码目录、示例插件函数等。

### build

在完成插件的开发后，执行
```bash
python -m fit_cli build %{your_plugin_name}
```
· 参数：%{your_plugin_name} - 自定义插件名称

解析插件源代码，在 plugin 目录中生成 %{your_plugin_name}.tar 文件，包含插件的所有源代码,并生成 tools.json 和 plugin.json 文件。

开发者可根据自己的需要，修改完善tools.json 和 plugin.json 文件。

### package

在完成插件的构建后，执行
```bash
python -m fit_cli package %{your_plugin_name}
```
· 参数：%{your_plugin_name} - 自定义插件名称

将 %{your_plugin_name}.tar 文件、tools.json 和 plugin.json 文件打包为 zip 文件。

---

## 注意事项

1. 在运行 init, build 或 package 子命令前，请先切换至 framework/fit/python 项目根目录下。
2. 更多详细信息和使用说明，可参考 https://github.com/ModelEngine-Group/fit-framework 官方仓库。