#!/bin/bash
# FIT Framework Health Check Script

FIT_HTTP_PORT="${FIT_HTTP_PORT:-8080}"
HEALTH_ENDPOINT="http://localhost:${FIT_HTTP_PORT}/actuator/plugins"

# 检查进程是否运行
check_process() {
    if pgrep -f "fit.*start" > /dev/null 2>&1; then
        return 0
    fi
    return 1
}

# 检查端口是否监听
check_port() {
    if command -v nc >/dev/null 2>&1; then
        nc -z localhost "${FIT_HTTP_PORT}" 2>/dev/null
    elif command -v ss >/dev/null 2>&1; then
        ss -tuln | grep -q ":${FIT_HTTP_PORT} "
    elif command -v netstat >/dev/null 2>&1; then
        netstat -tuln | grep -q ":${FIT_HTTP_PORT} "
    else
        # 如果没有网络工具，只检查进程
        check_process
    fi
}

# 检查HTTP接口（如果有curl）
check_http() {
    if command -v curl >/dev/null 2>&1; then
        curl -f -s "${HEALTH_ENDPOINT}" >/dev/null 2>&1
    else
        return 0  # 如果没有curl，跳过HTTP检查
    fi
}

# 主健康检查
main() {
    # 检查进程
    if ! check_process; then
        echo "FIT process not running"
        exit 1
    fi
    
    # 检查端口
    if ! check_port; then
        echo "FIT port ${FIT_HTTP_PORT} not listening"
        exit 1
    fi
    
    # 尝试HTTP检查
    if ! check_http; then
        echo "FIT HTTP health check failed"
        exit 1
    fi
    
    echo "FIT Framework is healthy"
    exit 0
}

main "$@"