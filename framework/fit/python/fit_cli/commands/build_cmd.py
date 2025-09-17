import json
import shutil
from pathlib import Path
from fit_cli.utils.build import calculate_checksum, parse_python_file

def generate_tools_json(base_dir: Path, plugin_name: str):
    """生成 tools.json"""
    src_dir = base_dir / plugin_name / "src"
    if not src_dir.exists():
        print(f"❌ 未找到插件目录 {src_dir}")
        return None

    tools_json = {
        "version": "1.0.0",
        "definitionGroups": [],
        "toolGroups": []
    }
    # 遍历src目录下的所有.py文件
    for py_file in src_dir.glob("**/*.py"):
        # 跳过__init__.py文件
        if py_file.name == "__init__.py":
            continue
        # 解析 Python 文件
        definition_group, tool_groups = parse_python_file(py_file)
        if definition_group is not None:
            tools_json["definitionGroups"].append(definition_group)
        if len(tool_groups) > 0:
            tools_json["toolGroups"].extend(tool_groups)

    path = base_dir / "tools.json"
    path.write_text(json.dumps(tools_json, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"✅ 已生成 {path}")
    return tools_json


def generate_plugin_json(base_dir: Path, plugin_name: str):
    """生成 plugin.json"""
    tar_path = base_dir / f"{plugin_name}.tar"
    if not tar_path.exists():
        print(f"❌ TAR 文件 {tar_path} 不存在，请先打包源代码")
        return None
    # 计算 TAR 文件的 SHA256
    checksum = calculate_checksum(tar_path)
    plugin_json = {
        "checksum": checksum,
        "name": plugin_name,
        "description": f"{plugin_name} 插件",
        "type": "python",
        "uniqueness": {
            "name": plugin_name
        }
    }
    path = base_dir / "plugin.json"
    path.write_text(json.dumps(plugin_json, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"✅ 已生成 {path}")
    return plugin_json


def make_plugin_tar(base_dir: Path, plugin_name: str):
    """打包源代码为 tar 格式"""
    tar_path = base_dir / f"{plugin_name}.tar"
    plugin_dir = base_dir / plugin_name

    shutil.make_archive(str(tar_path.with_suffix("")), "tar", plugin_dir)
    print(f"✅ 已生成打包文件 {tar_path}")


def run(args):
    """build 命令入口"""
    base_dir = Path("plugin") / args.name
    plugin_name = args.name
    
    if not base_dir.exists():
        print(f"❌ 插件目录 {base_dir} 不存在，请先运行 fit_cli init {args.name}")
        return

    # 打包源代码
    make_plugin_tar(base_dir, plugin_name)

    # 生成 JSON
    generate_tools_json(base_dir, plugin_name)
    generate_plugin_json(base_dir, plugin_name)