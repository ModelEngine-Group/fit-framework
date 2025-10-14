#!/bin/bash
set -euo pipefail

# FIT Framework 镜像构建脚本 - Ubuntu版本
OS_NAME="ubuntu"

# 配置
DEFAULT_FIT_VERSION="3.5.3"
DEFAULT_REGISTRY=""

# 显示帮助信息
show_help() {
    cat <<EOF
FIT Framework ${OS_NAME} 镜像构建脚本

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

# 验证FIT Framework版本是否存在
verify_fit_version() {
    local version=$1
    local url="https://github.com/ModelEngine-Group/fit-framework/releases/download/v${version}/${version}.zip"

    echo "🔍 验证FIT Framework版本 ${version}..."

    # 验证方式：使用 wget
    if ! wget --spider --timeout=10 --tries=1 "${url}" >/dev/null 2>&1; then
        echo "❌ 错误: FIT Framework版本 ${version} 不存在"
        echo "请检查版本号或访问: https://github.com/ModelEngine-Group/fit-framework/releases"
        exit 1
    fi

    echo "✅ 版本验证通过"
    return 0
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
    
    echo "🏗️  构建FIT Framework ${OS_NAME} 镜像..."
    echo "   版本: ${fit_version}"
    echo "   镜像: ${full_image_name}:${fit_version}-${OS_NAME}"

    # 执行构建（从上一级目录构建以访问common目录）
    docker build "${build_args[@]}" -f "${OS_NAME}/Dockerfile" ..
    
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
    if docker run --rm "${image_tag}" fit --version; then
        echo "✅ 镜像测试通过"
    else
        echo "❌ 镜像测试失败"
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

# 主函数
main() {
    local fit_version=${1:-$DEFAULT_FIT_VERSION}
    local registry=${2:-$DEFAULT_REGISTRY}
    
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
    echo "🚀 FIT Framework ${OS_NAME} 镜像构建"
    echo "=============================================="
    echo "FIT版本: ${fit_version}"
    echo "操作系统: ${OS_NAME}"
    echo "镜像名称: ${full_image_name}:${fit_version}-${OS_NAME}"
    echo "=============================================="
    
    # 执行构建流程
    check_docker
    verify_fit_version "${fit_version}"
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