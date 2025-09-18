import zipfile
from pathlib import Path

def package_to_zip(plugin_name: str):
    """将 build 生成的文件打包为 zip"""
    base_dir = Path("plugin") / plugin_name

    # 待打包的文件列表
    files_to_zip = [
        base_dir / f"{plugin_name}.tar",
        base_dir / "tools.json",
        base_dir / "plugin.json"
    ]

    # 检查文件是否存在
    missing_files = [str(f) for f in files_to_zip if not f.exists()]
    if missing_files:
        print(f"❌ 缺少以下文件，请先执行 build 命令：{', '.join(missing_files)}")
        return None

    # 打包文件
    zip_path = base_dir.parent / f"{plugin_name}.zip"
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for file in files_to_zip:
            zipf.write(file, arcname=file.name)

    print(f"✅ 已生成打包文件：{zip_path}")
    return zip_path


def run(args):
    """package 命令入口"""
    plugin_name = args.name
    base_dir = Path("plugin") / plugin_name

    if not base_dir.exists():
        print(f"❌ 插件目录 {base_dir} 不存在，请先运行 init 和 build 命令")
        return

    package_to_zip(plugin_name)