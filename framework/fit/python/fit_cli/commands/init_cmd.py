from pathlib import Path

TEMPLATE_PLUGIN = '''from fitframework.api.decorators import fitable # 引入 Fit for Python 框架核心接口

# 修改 genericable_id_hello / fitable_id_hello 为唯一的 ID
# - 建议和插件功能相关，例如：
# - @fitable("genericable_id_concat", "fitable_id_concat")
@fitable("genericable_id_hello", "fitable_id_hello") # 指定可供调用函数的 genericable id 和 fitable id
def hello(name: str) -> str: # 定义可供调用的函数，特别注意需要提供函数类型签名，
                             # 可参照文档https://www.modelengine-ai.com/#/docs/zh_CN/ai-framework/fit/python/function-signature
    """
    一个简单的 FIT 插件示例函数

    修改函数名和参数
    - 函数名（hello）应根据功能调整，例如 concat, multiply
    - 参数（name: str）可以增加多个，类型也可以是 int, float 等
    """

    return f"Hello, {name}!" # 提供函数实现逻辑
    
# 关于插件开发其他内容可参考官方文档：https://github.com/ModelEngine-Group/fit-framework/tree/main/docs/framework/fit/python
'''

def create_directory(path: Path):
    """创建目录（如果不存在）"""
    if not path.exists():
        path.mkdir(parents=True)

    return path


def create_file(path: Path, content: str = "", overwrite: bool = False):
    """创建文件，支持写入内容"""
    if path.exists() and not overwrite:
        print(f"⚠️ 文件 {path} 已存在，未覆盖。")
        return
    path.write_text(content, encoding="utf-8") if content else path.touch()



def generate_plugin_structure(plugin_name: str):
    """生成插件目录和文件结构"""
    base_dir = Path("plugin") / plugin_name
    src_dir = base_dir / "src"

    # 创建目录
    create_directory(base_dir)
    create_directory(src_dir)

    # 创建 __init__.py
    init_file = src_dir / "__init__.py"
    create_file(init_file)

    # 创建 plugin.py
    plugin_file = src_dir / "plugin.py"
    create_file(plugin_file, TEMPLATE_PLUGIN)

    print(f"✅ 已创建目录 {base_dir} ")


def run(args):
    """命令入口"""
    generate_plugin_structure(args.name)