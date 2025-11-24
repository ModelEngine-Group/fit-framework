#!/bin/bash
set -euo pipefail

# 下载 FIT Framework 制品
# 用法: ./download.sh [VERSION]
# 返回: 下载文件的完整路径

download_fit_artifact() {
    local version=$1
    local filename="${version}.zip"
    
    # 使用系统临时目录作为缓存
    local cache_dir="${TMPDIR:-/tmp}/fit-framework-cache"
    local cache_path="${cache_dir}/${filename}"
    local url="https://github.com/ModelEngine-Group/fit-framework/releases/download/v${version}/${filename}"

    # 创建缓存目录
    mkdir -p "${cache_dir}"

    # 检查文件是否已存在于缓存
    if [[ -f "${cache_path}" ]]; then
        echo "${cache_path}"
        return 0
    fi

    # 下载到临时文件，成功后再移动到缓存路径（原子操作）
    local temp_download_path="${cache_path}.tmp"
    
    # echo "⬇️  正在下载 FIT Framework ${version}..." >&2
    # echo "   URL: ${url}" >&2
    # echo "   缓存路径: ${cache_path}" >&2

    # 下载文件
    if command -v wget &> /dev/null; then
        wget -q --show-progress -O "${temp_download_path}" "${url}" >&2
    elif command -v curl &> /dev/null; then
        curl -L --progress-bar -o "${temp_download_path}" "${url}" >&2
    else
        echo "❌ 错误: 未找到 wget 或 curl，无法下载文件" >&2
        exit 1
    fi

    if [[ $? -eq 0 ]]; then
        mv "${temp_download_path}" "${cache_path}"
        echo "${cache_path}"
    else
        echo "❌ 下载失败" >&2
        rm -f "${temp_download_path}"
        exit 1
    fi
}

# 如果直接运行脚本
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    if [[ $# -lt 1 ]]; then
        echo "用法: $0 [VERSION]" >&2
        exit 1
    fi
    download_fit_artifact "$1"
fi
