from pathlib import Path

TEMPLATE_PLUGIN = '''from fitframework.api.decorators import fitable

@fitable("genericable_id_demo", "fitable_id_demo")
def hello(name: str) -> str:
    """一个简单的 FIT 插件示例函数"""
    return f"Hello, {name}!"
'''

def run(args):
    """生成插件模板"""
    base_dir = "plugin" / Path(args.name)
    src_dir = base_dir / "src"

    # 创建目录
    if not base_dir.exists():
        base_dir.mkdir(parents=True)
        print(f"✅ 已创建目录 {base_dir}")
    if not src_dir.exists():
        src_dir.mkdir(parents=True)

    # __init__.py
    init_file = src_dir / "__init__.py"
    if not init_file.exists():
        init_file.touch()
    else:
        print(f"⚠️ 文件 {init_file} 已存在，未覆盖。")

    # plugin.py
    plugin_file = src_dir / "plugin.py"
    if not plugin_file.exists():
        plugin_file.write_text(TEMPLATE_PLUGIN, encoding="utf-8")
    else:
        print(f"⚠️ 文件 {plugin_file} 已存在，未覆盖。")
