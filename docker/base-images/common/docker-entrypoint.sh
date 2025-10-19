#!/bin/bash
set -eo pipefail

# FIT Framework Universal Docker Entrypoint
# 适用于所有操作系统的通用入口脚本

# 默认配置
FIT_HOME="${FIT_HOME:-/opt/fit-framework}"
FIT_JAVA_DIR="${FIT_HOME}/java"
FIT_CONFIG_FILE="${FIT_JAVA_DIR}/conf/fitframework.yml"
FIT_LOG_LEVEL="${FIT_LOG_LEVEL:-info}"
FIT_REGISTRY_HOST="${FIT_REGISTRY_HOST:-localhost}"
FIT_REGISTRY_PORT="${FIT_REGISTRY_PORT:-8080}"
# 使用容器ID作为fallback，避免依赖hostname命令
FIT_WORKER_ID="${FIT_WORKER_ID:-fit-worker-$(hostname 2>/dev/null || echo ${HOSTNAME:-$(cat /etc/hostname 2>/dev/null || echo 'container')})}"
FIT_HTTP_PORT="${FIT_HTTP_PORT:-8080}"

# 日志函数
log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [FIT-FRAMEWORK] $*"
}

# 错误处理
error() {
    log "ERROR: $*" >&2
    exit 1
}

# 检查FIT Framework是否正确安装
check_installation() {
    log "检查FIT Framework安装..."
    
    if [ ! -d "${FIT_JAVA_DIR}" ]; then
        error "FIT Framework未找到: ${FIT_JAVA_DIR}"
    fi
    
    if [ ! -f "${FIT_JAVA_DIR}/bin/fit" ]; then
        error "FIT命令未找到: ${FIT_JAVA_DIR}/bin/fit"
    fi
    
    if [ ! -x "${FIT_JAVA_DIR}/bin/fit" ]; then
        log "设置FIT命令执行权限..."
        chmod +x "${FIT_JAVA_DIR}/bin/fit"
    fi
    
    log "✅ FIT Framework安装检查完成"
}

# 初始化配置
init_config() {
    log "初始化FIT Framework配置..."
    
    # 创建必要目录
    mkdir -p "${FIT_JAVA_DIR}"/{logs,data,dynamic-plugins}
    
    # 生成配置文件（如果不存在）
    if [ ! -f "${FIT_CONFIG_FILE}" ]; then
        log "生成默认配置文件: ${FIT_CONFIG_FILE}"
        mkdir -p "$(dirname "${FIT_CONFIG_FILE}")"
        cat > "${FIT_CONFIG_FILE}" <<EOF
application:
  name: 'fit-application'

worker:
  id: '${FIT_WORKER_ID}'
  host: '0.0.0.0'
  environment: 'prod'
  environment-sequence: 'prod'
  exit:
    graceful: true

matata:
  registry:
    host: '${FIT_REGISTRY_HOST}'
    port: ${FIT_REGISTRY_PORT}
    protocol: 2
    environment: 'prod'

fit:
  beans:
    packages:
    - 'modelengine.fitframework'
    - 'modelengine.fit'

server:
  http:
    port: ${FIT_HTTP_PORT}
EOF
    fi
    
    # 设置环境变量
    export LOG_HOME="${FIT_JAVA_DIR}/logs"
    export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk}"
    export PATH="${FIT_JAVA_DIR}/bin:${JAVA_HOME}/bin:${PATH}"
    
    log "✅ 配置初始化完成"
}

# 等待依赖服务
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local timeout=${4:-30}
    
    log "等待 ${service_name} 服务 (${host}:${port}) 启动..."
    
    for i in $(seq 1 $timeout); do
        if command -v nc >/dev/null 2>&1; then
            if nc -z $host $port 2>/dev/null; then
                log "✅ ${service_name} 服务已就绪"
                return 0
            fi
        elif command -v telnet >/dev/null 2>&1; then
            if echo "quit" | telnet $host $port 2>/dev/null | grep -q "Connected"; then
                log "✅ ${service_name} 服务已就绪"
                return 0
            fi
        else
            log "警告: 无可用的网络检测工具，跳过服务等待"
            return 0
        fi
        sleep 1
    done
    
    log "警告: 等待 ${service_name} 服务超时，继续启动"
    return 1
}

# 显示帮助信息
show_help() {
    cat <<EOF
FIT Framework Docker 容器使用说明

用法:
  docker run fit-framework:latest [COMMAND] [OPTIONS]

命令:
  fit start              启动FIT框架服务
  fit stop               停止FIT框架服务  
  fit status             查看服务状态
  fit --version          显示版本信息
  bash/sh                进入交互式shell
  help                   显示此帮助信息

环境变量:
  FIT_REGISTRY_HOST      注册中心主机 (默认: localhost)
  FIT_REGISTRY_PORT      注册中心端口 (默认: 8080)
  FIT_WORKER_ID          工作节点ID (默认: fit-worker-<hostname>)
  FIT_HTTP_PORT          HTTP服务端口 (默认: 8080)
  FIT_LOG_LEVEL          日志级别 (默认: info)
  JAVA_OPTS              JVM参数 (默认: -Xms256m -Xmx1024m)

目录挂载:
  /opt/fit-framework/java/dynamic-plugins  # 动态插件目录
  /opt/fit-framework/java/logs            # 日志目录
  /opt/fit-framework/java/data            # 数据目录
  /opt/fit-framework/java/conf            # 配置目录

示例:
  # 启动基础服务
  docker run -d -p 8080:8080 fit-framework:latest
  
  # 自定义配置
  docker run -d -p 8080:8080 \\
    -e FIT_REGISTRY_HOST=registry.example.com \\
    -e FIT_WORKER_ID=my-worker-001 \\
    fit-framework:latest
  
  # 挂载插件目录
  docker run -d -p 8080:8080 \\
    -v /path/to/plugins:/opt/fit-framework/java/dynamic-plugins \\
    fit-framework:latest

EOF
}

# 信号处理
cleanup() {
    log "收到停止信号，正在优雅关闭..."
    if command -v "${FIT_JAVA_DIR}/bin/fit" >/dev/null 2>&1; then
        "${FIT_JAVA_DIR}/bin/fit" stop 2>/dev/null || true
    fi
    exit 0
}

# 主逻辑
main() {
    # 注册信号处理
    trap cleanup SIGTERM SIGINT
    
    # 如果是help命令
    if [[ "${1:-}" == "help" ]] || [[ "${1:-}" == "--help" ]]; then
        show_help
        exit 0
    fi
    
    # 如果不是fit命令且不为空，直接执行
    if [[ $# -gt 0 && "${1:-}" != "fit" ]]; then
        exec "$@"
    fi
    
    # 检查安装
    check_installation
    
    # 初始化配置
    init_config
    
    # 如果配置了注册中心且不是localhost，等待服务
    if [[ "${FIT_REGISTRY_HOST}" != "localhost" && "${FIT_REGISTRY_HOST}" != "127.0.0.1" ]]; then
        wait_for_service "${FIT_REGISTRY_HOST}" "${FIT_REGISTRY_PORT}" "Registry Center"
    fi
    
    # 显示启动信息
    log "启动FIT Framework服务..."
    log "Worker ID: ${FIT_WORKER_ID}"
    log "Registry: ${FIT_REGISTRY_HOST}:${FIT_REGISTRY_PORT}"
    log "HTTP Port: ${FIT_HTTP_PORT}"
    log "Java Home: ${JAVA_HOME}"
    log "FIT Home: ${FIT_JAVA_DIR}"
    
    # 设置工作目录
    cd "${FIT_JAVA_DIR}/dynamic-plugins"
    
    # 启动FIT服务
    if [[ $# -eq 0 ]]; then
        exec "${FIT_JAVA_DIR}/bin/fit" start
    else
        exec "${FIT_JAVA_DIR}/bin/$@"
    fi
}

# 入口点
main "$@"