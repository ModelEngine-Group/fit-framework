#!/bin/bash
#
# FIT 服务管理公共脚本
# 提供服务启动、健康检查、验证和清理功能
#

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# 初始化日志目录
init_log_dir() {
    mkdir -p .ai-workspace/logs
    log_info "日志目录初始化完成: .ai-workspace/logs"
}

# 创建动态插件目录
init_plugin_dir() {
    mkdir -p dynamic-plugins
    log_info "动态插件目录创建完成: dynamic-plugins"
}

# 启动 FIT 服务
# 参数: $1 - 日志文件路径（可选，默认自动生成）
# 返回: FIT_PID（通过全局变量）和 FIT_LOG（日志文件路径）
start_fit_service() {
    local log_file="${1:-}"
    
    if [ -z "$log_file" ]; then
        local timestamp=$(date +%Y%m%d-%H%M%S)
        log_file=".ai-workspace/logs/fit-server-${timestamp}.log"
    fi
    
    export FIT_LOG="$log_file"
    
    log_info "正在启动 FIT 服务..."
    log_info "日志文件: $FIT_LOG"
    
    # 检查构建产物是否存在
    if [ ! -d "build" ]; then
        log_error "构建产物不存在，请先运行 Maven 构建"
        return 1
    fi
    
    # 检查端口是否被占用
    if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warn "端口 8080 已被占用，尝试清理..."
        pkill -f fit-discrete-launcher || true
        sleep 2
    fi
    
    # 启动服务
    nohup build/bin/fit start --plugin-dir=dynamic-plugins > "$FIT_LOG" 2>&1 &
    export FIT_PID=$!
    
    log_info "FIT 服务已启动 (PID: $FIT_PID)"
    
    # 等待服务启动（最多 60 秒）
    log_info "等待服务健康检查..."
    for i in {1..60}; do
        local http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null)
        if [ "$http_code" = "200" ]; then
            log_info "FIT 服务启动成功！"
            return 0
        fi

        # 检查进程是否还存在
        if ! kill -0 $FIT_PID 2>/dev/null; then
            log_error "FIT 服务进程已退出"
            cat "$FIT_LOG"
            return 1
        fi

        if [ $i -eq 60 ]; then
            log_error "服务启动超时（60秒）"
            cat "$FIT_LOG"
            kill $FIT_PID 2>/dev/null || true
            return 1
        fi

        sleep 1
    done
}

# 验证健康检查接口
# 返回: 0 成功, 1 失败
verify_health() {
    log_info "验证健康检查接口: /actuator/health"

    local http_code=$(curl -s -o /tmp/health_response.json -w "%{http_code}" http://localhost:8080/actuator/health)
    if [ "$http_code" = "200" ]; then
        log_info "健康检查通过 (HTTP $http_code)"
        cat /tmp/health_response.json
        rm -f /tmp/health_response.json
        return 0
    else
        log_error "健康检查失败 (HTTP $http_code)"
        rm -f /tmp/health_response.json
        return 1
    fi
}

# 验证插件列表接口
# 返回: 0 成功, 1 失败
verify_plugins() {
    log_info "验证插件列表接口: /actuator/plugins"

    local http_code=$(curl -s -o /tmp/plugins_response.json -w "%{http_code}" http://localhost:8080/actuator/plugins)
    if [ "$http_code" = "200" ]; then
        log_info "插件列表接口验证通过 (HTTP $http_code)"
        cat /tmp/plugins_response.json
        rm -f /tmp/plugins_response.json
        return 0
    else
        log_error "插件列表接口验证失败 (HTTP $http_code)"
        rm -f /tmp/plugins_response.json
        return 1
    fi
}

# 验证 Swagger 文档页面
# 返回: 0 成功, 1 失败
verify_swagger() {
    log_info "验证 Swagger 文档页面: /openapi.html"

    local http_code=$(curl -s -o /tmp/swagger_response.html -w "%{http_code}" http://localhost:8080/openapi.html)
    if [ "$http_code" = "200" ] && grep -qi "swagger\|openapi" /tmp/swagger_response.html 2>/dev/null; then
        log_info "Swagger 文档页面验证通过 (HTTP $http_code)"
        rm -f /tmp/swagger_response.html
        return 0
    else
        log_error "Swagger 文档页面验证失败 (HTTP $http_code)"
        rm -f /tmp/swagger_response.html
        return 1
    fi
}

# 执行所有验证
# 返回: 0 全部成功, 1 任意失败
verify_all() {
    local failed=0
    
    verify_health || failed=1
    echo ""
    
    verify_plugins || failed=1
    echo ""
    
    verify_swagger || failed=1
    
    if [ $failed -eq 0 ]; then
        log_info "所有验证通过！"
    else
        log_error "部分验证失败"
    fi
    
    return $failed
}

# 停止 FIT 服务
stop_fit_service() {
    log_info "正在停止 FIT 服务..."
    
    pkill -f fit-discrete-launcher || true
    sleep 2
    
    log_info "FIT 服务已停止"
}

# 清理测试环境
# 参数: $1 - 是否清理构建产物 (true/false, 默认 false)
cleanup() {
    local clean_build="${1:-false}"
    
    log_info "正在清理测试环境..."
    
    stop_fit_service
    
    # 清理动态插件目录
    if [ -d "dynamic-plugins" ]; then
        rm -rf dynamic-plugins
        log_info "已清理: dynamic-plugins"
    fi
    
    # 可选：清理构建产物
    if [ "$clean_build" = "true" ] && [ -d "build" ]; then
        rm -rf build
        log_info "已清理: build"
    fi
    
    # 显示日志文件
    if [ -d ".ai-workspace/logs" ]; then
        log_info "测试日志保留在: .ai-workspace/logs/"
        ls -lh .ai-workspace/logs/ | tail -5
    fi
    
    log_info "清理完成"
}

# 主函数 - 用于直接运行此脚本
main() {
    local command="${1:-help}"
    
    case "$command" in
        start)
            init_log_dir
            init_plugin_dir
            start_fit_service
            ;;
        verify)
            verify_all
            ;;
        stop)
            stop_fit_service
            ;;
        cleanup)
            cleanup "${2:-false}"
            ;;
        help|*)
            echo "用法: $0 {start|verify|stop|cleanup} [选项]"
            echo ""
            echo "命令:"
            echo "  start       - 启动 FIT 服务"
            echo "  verify      - 验证服务接口"
            echo "  stop        - 停止 FIT 服务"
            echo "  cleanup [clean_build]  - 清理环境 (clean_build=true 时清理构建产物)"
            echo ""
            echo "示例:"
            echo "  $0 start              # 启动服务"
            echo "  $0 verify             # 验证接口"
            echo "  $0 cleanup            # 清理环境（保留构建产物）"
            echo "  $0 cleanup true       # 清理环境（包含构建产物）"
            ;;
    esac
}

# 如果直接运行此脚本，执行主函数
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main "$@"
fi
