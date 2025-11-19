import json
import shutil
import uuid
import tarfile
from datetime import datetime
from pathlib import Path
from fit_cli.utils.build import calculate_checksum, parse_python_file, type_errors

def generate_tools_json(base_dir: Path, plugin_name: str):
    """生成 tools.json"""
    global type_errors
    type_errors.clear()

    src_dir = base_dir / "src"
    if not src_dir.exists():
        print(f"❌ 未找到插件目录 {src_dir}")
        return None
    
    build_dir = base_dir / "build"
    if not build_dir.exists():
        build_dir.mkdir(exist_ok=True)

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

        if type_errors:
            print("❌ tools.json 类型校验失败：")
            for err in set(type_errors):
                print(f"  - {err}")
            print("请修改为支持的类型：int, float, str, bool, dict, list, tuple, set, bytes")
            return None  # 终止构建

    path = build_dir / "tools.json"
    path.write_text(json.dumps(tools_json, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"✅ 已生成 {path}")
    return tools_json


def generate_plugin_json(base_dir: Path, plugin_name: str):
    """生成 plugin.json"""
    build_dir = base_dir / "build"
    tar_path = build_dir / f"{plugin_name}.tar"
    if not tar_path.exists():
        print(f"❌ TAR 文件 {tar_path} 不存在，请先打包源代码")
        return None
    
    checksum = calculate_checksum(tar_path)
    timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
    short_uuid = str(uuid.uuid4())[:8]
    unique_name = f"{plugin_name}-{timestamp}-{short_uuid}"

    plugin_json = {
        "checksum": checksum,
        "name": plugin_name,
        "description": f"{plugin_name} 插件",
        "type": "python",
        "uniqueness": {
            "name": unique_name
        }
    }
    path = build_dir / "plugin.json"
    path.write_text(json.dumps(plugin_json, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"✅ 已生成 {path}")
    return plugin_json


def make_plugin_tar(base_dir: Path, plugin_name: str):
    """打包源代码为 tar 格式"""
    build_dir = base_dir / "build"
    if not build_dir.exists():
        build_dir.mkdir(exist_ok=True)

    tar_path = build_dir / f"{plugin_name}.tar"
    plugin_dir = base_dir

    with tarfile.open(tar_path, "w") as tar:
        # 遍历插件目录下的所有文件
        for item in plugin_dir.rglob("*"):
            # 排除build目录及其内容
            if "build" in item.parts:
                continue
            
            if item.is_file():
                arcname = Path(plugin_name) / item.relative_to(plugin_dir)
                tar.add(item, arcname=arcname)
    print(f"✅ 已打包源代码 {tar_path}")


def run(args):
    """build 命令入口"""
    base_dir = Path("plugin") / args.name
    plugin_name = args.name
    
    if not base_dir.exists():
        print(f"❌ 插件目录 {base_dir} 不存在，请先运行 fit_cli init {args.name}")
        return
    # 生成 tools.json
    tools_json = generate_tools_json(base_dir, plugin_name)
    if tools_json is not None:
        make_plugin_tar(base_dir, plugin_name) # 打包源代码
        generate_plugin_json(base_dir, plugin_name) # 生成 plugin.json