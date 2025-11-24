#!/bin/bash
set -euo pipefail

# FIT Framework 镜像构建脚本 - Debian版本
OS_NAME="debian"

# 配置
DEFAULT_FIT_VERSION="3.5.3"
DEFAULT_REGISTRY=""

# 显示帮助信息
show_help() {
    cat <<EOF
FIT Framework Debian 镜像构建脚本

用法:
  $0 [FIT_VERSION] [REGISTRY]

参数:
  FIT_VERSION    FIT Framework版本 [默认: ${DEFAULT_FIT_VERSION}]
  REGISTRY       镜像仓库前缀 [默认: 无前缀]

示例:
  $0                                    # 使用默认版本构建
  $0 3.5.1                             # 指定版本构建
  $0 3.5.1 registry.example.com/       # 指定版本和仓库

环境变量:
  PUSH_IMAGE     是否推送镜像 (true|false) [默认: false]
  BUILD_ARGS     额外的docker build参数

EOF
}

# 检查Docker环境
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo "❌ 错误: 请先安装Docker"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        echo "❌ 错误: Docker服务未启动或无权限访问"
        exit 1
    fi
}

# 准备FIT Framework制品
prepare_artifact() {
    local version=$1
    local script_dir=$(dirname "$0")
    local project_root=$(cd "$script_dir/.." && pwd)
    
    # 调用公共下载脚本获取缓存路径
    local cache_path
    cache_path=$("$project_root/common/download.sh" "$version")
    
    if [[ $? -ne 0 ]] || [[ ! -f "${cache_path}" ]]; then
        echo "❌ 错误: 准备FIT Framework制品失败"
        exit 1
    fi
    
    echo "✅ FIT Framework ${version} 已就绪: ${cache_path}"
    
    # 复制到构建上下文（父目录）
    # 使用trap确保脚本退出时清理
    cp "${cache_path}" "${project_root}/${version}.zip"
}

# 构建镜像
build_image() {
    local fit_version=$1
    local registry=$2
    local image_name="fit-framework"
    local full_image_name="${registry}${image_name}"
    
    # 构建参数
    local build_args=(
        "--build-arg" "FIT_VERSION=${fit_version}"
        "--tag" "${full_image_name}:${fit_version}-${OS_NAME}"
        "--tag" "${full_image_name}:${OS_NAME}"
    )
    
    # 如果是默认版本，添加latest标签
    if [[ "${fit_version}" == "${DEFAULT_FIT_VERSION}" ]]; then
        build_args+=(
            "--tag" "${full_image_name}:latest-${OS_NAME}"
        )
    fi
    
    # 添加额外构建参数
    if [[ -n "${BUILD_ARGS:-}" ]]; then
        IFS=' ' read -ra EXTRA_ARGS <<< "${BUILD_ARGS}"
        build_args+=("${EXTRA_ARGS[@]}")
    fi
    
    echo "🏗️  构建FIT Framework Debian 镜像..."
    echo "   版本: ${fit_version}"
    echo "   镜像: ${full_image_name}:${fit_version}-${OS_NAME}"
    
    # 执行构建
    # 使用父目录作为构建上下文，以便访问common目录和制品
    docker build "${build_args[@]}" -f Dockerfile ..
    
    if [[ $? -eq 0 ]]; then
        echo "✅ 镜像构建成功"
    else
        echo "❌ 镜像构建失败"
        exit 1
    fi
    
    # 显示镜像信息
    echo "📊 镜像信息:"
    docker images "${full_image_name}" --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
}

# 测试镜像
test_image() {
    local image_tag=$1
    
    echo "🧪 测试镜像: ${image_tag}"
    
    # 测试基本启动
    local version_output
    if version_output=$(docker run --rm "${image_tag}" fit version 2>&1); then
        echo "✅ 镜像测试通过"
        echo "   FIT 版本信息:"
        echo "${version_output}" | sed 's/^/   /'
    else
        echo "❌ 镜像测试失败"
        echo "   错误信息:"
        echo "${version_output}" | sed 's/^/   /'
        return 1
    fi
}

# 推送镜像
push_image() {
    local image_name=$1
    local fit_version=$2
    
    if [[ "${PUSH_IMAGE:-false}" == "true" ]]; then
        echo "🚀 推送镜像到仓库..."
        
        docker push "${image_name}:${fit_version}-${OS_NAME}"
        docker push "${image_name}:${OS_NAME}"
        
        if [[ "${fit_version}" == "${DEFAULT_FIT_VERSION}" ]]; then
            docker push "${image_name}:latest-${OS_NAME}"
        fi
        
        echo "✅ 镜像推送完成"
    else
        echo "💡 提示: 设置 PUSH_IMAGE=true 可自动推送镜像"
    fi
}

# 清理函数
cleanup() {
    local version=$1
    local script_dir=$(dirname "$0")
    local project_root=$(cd "$script_dir/.." && pwd)
    
    if [[ -n "${version}" && -f "${project_root}/${version}.zip" ]]; then
        # echo "🧹 清理临时文件..."
        rm -f "${project_root}/${version}.zip"
    fi
}

# 主函数
main() {
    local fit_version=${1:-$DEFAULT_FIT_VERSION}
    local registry=${2:-$DEFAULT_REGISTRY}
    
    # ... (help check)
    
    # 注册清理钩子
    trap "cleanup ${fit_version}" EXIT
    
    # ... (rest of main)
    
    # 显示帮助
    if [[ "${fit_version}" == "help" ]] || [[ "${fit_version}" == "--help" ]]; then
        show_help
        exit 0
    fi
    
    # 规范化registry（确保以/结尾）
    if [[ -n "${registry}" && "${registry}" != */ ]]; then
        registry="${registry}/"
    fi
    
    local full_image_name="${registry}fit-framework"
    
    echo "=============================================="
    echo "🚀 FIT Framework Debian 镜像构建"
    echo "=============================================="
    echo "FIT版本: ${fit_version}"
    echo "操作系统: ${OS_NAME}"
    echo "镜像名称: ${full_image_name}:${fit_version}-${OS_NAME}"
    echo "=============================================="
    
    # 执行构建流程
    check_docker
    prepare_artifact "${fit_version}"
    build_image "${fit_version}" "${registry}"
    test_image "${full_image_name}:${fit_version}-${OS_NAME}"
    push_image "${full_image_name}" "${fit_version}"
    
    echo "=============================================="
    echo "🎉 构建完成!"
    echo "可用镜像:"
    echo "  - ${full_image_name}:${fit_version}-${OS_NAME}"
    echo "  - ${full_image_name}:${OS_NAME}"
    if [[ "${fit_version}" == "${DEFAULT_FIT_VERSION}" ]]; then
        echo "  - ${full_image_name}:latest-${OS_NAME}"
    fi
    echo "=============================================="
}

# 执行主函数
main "$@"