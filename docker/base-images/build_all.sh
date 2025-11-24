#!/bin/bash
set -euo pipefail

# FIT Framework 批量镜像构建脚本
# 构建所有支持的操作系统镜像

# 配置
DEFAULT_FIT_VERSION="3.6.0"
DEFAULT_REGISTRY=""
BUILD_LOG_DIR="./build-logs"

# 支持的操作系统列表
OS_LIST=(
    "alpine"
    "debian"
)



# 显示帮助信息
show_help() {
    cat <<EOF
FIT Framework 批量镜像构建脚本

用法:
  $0 [COMMAND] [OPTIONS]

命令:
  build [VERSION] [REGISTRY]    构建所有镜像
  list                         列出支持的操作系统
  clean                        清理构建日志
  help                         显示帮助信息

选项:
  VERSION     FIT Framework版本 [默认: ${DEFAULT_FIT_VERSION}]
  REGISTRY    镜像仓库前缀 [默认: 无前缀]

环境变量:
  PUSH_IMAGE     是否推送镜像 (true|false) [默认: false]
  PARALLEL       并行构建数量 [默认: 2]
  SKIP_OS        跳过的操作系统列表，逗号分隔
  ONLY_OS        仅构建的操作系统列表，逗号分隔

示例:
  $0 build                              # 构建所有镜像
  $0 build 3.5.1                       # 指定版本构建
  $0 build 3.5.1 registry.example.com/ # 指定版本和仓库
  $0 list                               # 列出支持的OS
  
  # 环境变量示例
  ONLY_OS=alpine,debian $0 build
  PARALLEL=4 PUSH_IMAGE=true $0 build

EOF
}

# 列出支持的操作系统
list_os() {
    echo "支持的操作系统列表:"
    echo "===================="
    for os in "${OS_LIST[@]}"; do
        echo "  - $os"
    done
    echo "===================="
    echo "总计: ${#OS_LIST[@]} 个操作系统"
}

# 清理构建日志
clean_logs() {
    if [[ -d "${BUILD_LOG_DIR}" ]]; then
        echo "🧹 清理构建日志目录: ${BUILD_LOG_DIR}"
        rm -rf "${BUILD_LOG_DIR}"
        echo "✅ 清理完成"
    else
        echo "💡 没有发现构建日志目录"
    fi
}

# 检查环境
check_environment() {
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        echo "❌ 错误: 请先安装Docker"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        echo "❌ 错误: Docker服务未启动或无权限访问"
        exit 1
    fi
    
    # 检查并创建日志目录
    mkdir -p "${BUILD_LOG_DIR}"
    
    echo "✅ 环境检查通过"
}

# 过滤操作系统列表
filter_os_list() {
    local filtered_list=()
    
    # 如果设置了ONLY_OS，只构建指定的OS
    if [[ -n "${ONLY_OS:-}" ]]; then
        IFS=',' read -ra ONLY_ARRAY <<< "${ONLY_OS}"
        for os in "${ONLY_ARRAY[@]}"; do
            if [[ " ${OS_LIST[*]} " =~ " $os " ]]; then
                filtered_list+=("$os")
            else
                echo "⚠️  警告: 不支持的操作系统 '$os'，已跳过"
            fi
        done
    else
        # 否则使用全部OS列表
        filtered_list=("${OS_LIST[@]}")
    fi
    
    # 如果设置了SKIP_OS，移除指定的OS
    if [[ -n "${SKIP_OS:-}" ]]; then
        IFS=',' read -ra SKIP_ARRAY <<< "${SKIP_OS}"
        local temp_list=()
        for os in "${filtered_list[@]}"; do
            if [[ ! " ${SKIP_ARRAY[*]} " =~ " $os " ]]; then
                temp_list+=("$os")
            else
                echo "⏭️  跳过操作系统: $os"
            fi
        done
        filtered_list=("${temp_list[@]}")
    fi
    
    # 返回过滤后的列表
    printf '%s\n' "${filtered_list[@]}"
}

# 构建单个镜像
build_single_image() {
    local os_name=$1
    local fit_version=$2
    local registry=$3
    local log_file="${BUILD_LOG_DIR}/${os_name}.log"
    
    echo "🏗️  [${os_name}] 开始构建..."
    
    # 检查目录是否存在
    if [[ ! -d "${os_name}" ]]; then
        echo "❌ [${os_name}] 错误: 找不到目录 ${os_name}/"
        return 1
    fi
    
    # 进入目录并执行构建
    (
        cd "${os_name}"
        export PUSH_IMAGE="${PUSH_IMAGE:-false}"
        
        # 执行构建脚本
        if [[ -f "build.sh" ]]; then
            bash build.sh "${fit_version}" "${registry}" 2>&1
        else
            # 直接使用docker build
            docker build \
                --build-arg FIT_VERSION="${fit_version}" \
                --tag "${registry}fit-framework:${fit_version}-${os_name}" \
                --tag "${registry}fit-framework:${os_name}" \
                . 2>&1
        fi
    ) > "${log_file}" 2>&1
    
    if [[ $? -eq 0 ]]; then
        echo "✅ [${os_name}] 构建成功"
        return 0
    else
        echo "❌ [${os_name}] 构建失败，查看日志: ${log_file}"
        return 1
    fi
}

# 并行构建镜像
build_images_parallel() {
    local fit_version=$1
    local registry=$2
    local -a os_array=("${@:3}")
    local parallel_count=${PARALLEL:-2}
    
    echo "🚀 开始并行构建 (并发数: ${parallel_count})"
    echo "构建列表: ${os_array[*]}"
    echo "=============================================="
    
    local -a pids=()
    local -a running_os=()
    local -a success_list=()
    local -a failure_list=()
    local active_jobs=0
    local os_index=0
    
    # 启动初始任务
    while [[ $active_jobs -lt $parallel_count && $os_index -lt ${#os_array[@]} ]]; do
        local os_name="${os_array[$os_index]}"
        build_single_image "$os_name" "$fit_version" "$registry" &
        pids+=($!)
        running_os+=("$os_name")
        ((active_jobs++))
        ((os_index++))
    done
    
    # 等待任务完成并启动新任务
    while [[ $active_jobs -gt 0 ]]; do
        for i in "${!pids[@]}"; do
            local pid="${pids[$i]}"
            if ! kill -0 "$pid" 2>/dev/null; then
                # 任务完成
                wait "$pid"
                local exit_code=$?
                local completed_os="${running_os[$i]}"
                
                if [[ $exit_code -eq 0 ]]; then
                    success_list+=("$completed_os")
                else
                    failure_list+=("$completed_os")
                fi
                
                # 移除已完成的PID和OS记录
                unset "pids[$i]"
                unset "running_os[$i]"
                ((active_jobs--))
                
                # 启动新任务（如果还有）
                if [[ $os_index -lt ${#os_array[@]} ]]; then
                    local next_os="${os_array[$os_index]}"
                    build_single_image "$next_os" "$fit_version" "$registry" &
                    pids+=($!)
                    running_os+=("$next_os")
                    ((active_jobs++))
                    ((os_index++))
                fi
                break
            fi
        done
        sleep 1
    done
    
    # 显示构建结果
    echo "=============================================="
    echo "📊 构建结果汇总"
    echo "=============================================="
    echo "✅ 成功 (${#success_list[@]}): ${success_list[*]:-}"
    echo "❌ 失败 (${#failure_list[@]}): ${failure_list[*]:-}"
    
    if [[ ${#failure_list[@]} -gt 0 ]]; then
        echo ""
        echo "📋 失败日志位置:"
        for failed_os in "${failure_list[@]}"; do
            echo "   ${failed_os}: ${BUILD_LOG_DIR}/${failed_os}.log"
        done
        return 1
    fi
    
    return 0
}

# 准备FIT Framework制品
prepare_artifact() {
    local version=$1
    local script_dir=$(dirname "$0")
    
    # 调用公共下载脚本获取缓存路径
    local cache_path
    cache_path=$("$script_dir/common/download.sh" "$version")
    
    if [[ $? -ne 0 ]] || [[ ! -f "${cache_path}" ]]; then
        echo "❌ 错误: 准备FIT Framework制品失败"
        exit 1
    fi
    
    echo "✅ FIT Framework ${version} 已就绪: ${cache_path}"
    
    # 复制到当前目录（构建上下文根目录）
    cp "${cache_path}" "${script_dir}/${version}.zip"
}

# 清理函数
cleanup_artifact() {
    local version=$1
    local script_dir=$(dirname "$0")
    
    if [[ -n "${version}" && -f "${script_dir}/${version}.zip" ]]; then
        # echo "🧹 清理临时文件..."
        rm -f "${script_dir}/${version}.zip"
    fi
}

# 主构建函数
build_all() {
    local fit_version=${1:-$DEFAULT_FIT_VERSION}
    local registry=${2:-$DEFAULT_REGISTRY}
    
    # 注册清理钩子
    trap "cleanup_artifact ${fit_version}" EXIT
    
    # ... (rest of build_all)
    
    # 规范化registry
    if [[ -n "${registry}" && "${registry}" != */ ]]; then
        registry="${registry}/"
    fi
    
    echo "=============================================="
    echo "🚀 FIT Framework 批量镜像构建"
    echo "=============================================="
    echo "FIT版本: ${fit_version}"
    echo "镜像仓库: ${registry:-无前缀}"
    echo "并发数: ${PARALLEL:-2}"
    echo "推送镜像: ${PUSH_IMAGE:-false}"
    echo "=============================================="
    
    # 准备制品
    prepare_artifact "${fit_version}"
    
    # 获取过滤后的OS列表
    local -a filtered_os=()
    while IFS= read -r line; do
        filtered_os+=("$line")
    done < <(filter_os_list)
    
    if [[ ${#filtered_os[@]} -eq 0 ]]; then
        echo "❌ 错误: 没有需要构建的操作系统"
        exit 1
    fi
    
    # 执行并行构建
    if build_images_parallel "$fit_version" "$registry" "${filtered_os[@]}"; then
        echo "=============================================="
        echo "🎉 所有镜像构建完成!"
        echo "=============================================="
        
        # 显示构建的镜像
        echo "📦 构建的镜像列表:"
        for os in "${filtered_os[@]}"; do
            echo "   ${registry}fit-framework:${fit_version}-${os}"
            echo "   ${registry}fit-framework:${os}"
        done
    else
        echo "=============================================="
        echo "❌ 部分镜像构建失败"
        echo "=============================================="
        exit 1
    fi
}

# 主函数
main() {
    local command=${1:-build}
    
    case "$command" in
        "build")
            check_environment
            build_all "${2:-}" "${3:-}"
            ;;
        "list")
            list_os
            ;;
        "clean")
            clean_logs
            ;;
        "help"|"--help")
            show_help
            ;;
        *)
            echo "❌ 未知命令: $command"
            echo "使用 '$0 help' 查看帮助信息"
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"