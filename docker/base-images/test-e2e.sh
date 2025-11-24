#!/bin/bash
set -euo pipefail

# FIT Framework 端到端测试脚本
# 完整测试流程：构建基础镜像 → 推送本地仓库 → 启动运行 → 访问验证

# 显示帮助信息
show_help() {
    cat <<EOF
FIT Framework 端到端测试脚本

用途:
  自动化测试完整的镜像构建、推送、部署和验证流程

用法:
  $0 [OS_NAME]
  $0 --help

参数:
  OS_NAME          操作系统名称 [默认: alpine]
                   可选值: alpine, debian

环境变量:
  REGISTRY_PORT    本地 Registry 端口 [默认: 15000]
                   脚本会自动检测端口冲突并尝试 +1, +2
  FIT_VERSION      FIT Framework 版本号 [默认: 3.6.0]
  DEBUG            显示详细命令 (true|false) [默认: false]

测试流程:
  1. 启动本地 Docker Registry（端口 15000）
  2. 构建 FIT Framework 基础镜像
  3. 推送镜像到本地仓库
  4. 从仓库拉取并启动容器
  5. 验证功能（健康检查、插件加载、HTTP 服务）
  6. 自动清理测试资源（容器、镜像、悬空镜像）

示例:
  # 测试 Alpine（默认）
  $0

  # 测试指定操作系统
  $0 alpine
  $0 debian

  # 使用自定义端口
  REGISTRY_PORT=20000 $0 alpine

  # 使用不同版本
  FIT_VERSION=3.5.4 $0 alpine

  # 组合使用
  REGISTRY_PORT=20000 FIT_VERSION=3.5.4 $0 alpine

  # 调试模式（显示详细命令）
  DEBUG=true $0 ubuntu

清理说明:
  • 测试完成后自动清理所有测试镜像和容器
  • 保留 registry:latest 镜像供后续测试复用
  • 按 Ctrl+C 中断时也会自动清理

详细文档:
  README.md - 完整使用说明
  BUILD.md  - 构建和发布指南

EOF
    exit 0
}

# 检查帮助参数
if [[ "${1:-}" == "--help" ]] || [[ "${1:-}" == "-h" ]] || [[ "${1:-}" == "help" ]]; then
    show_help
fi

# 配置
REGISTRY_PORT="${REGISTRY_PORT:-15000}"
FIT_VERSION="${FIT_VERSION:-3.6.0}"
BUILD_OS="${1:-alpine}"  # 可选: alpine, debian
DEBUG="${DEBUG:-false}"  # 设置为 true 显示详细命令

# 验证操作系统参数
VALID_OS="alpine debian"
if [[ ! " $VALID_OS " =~ " $BUILD_OS " ]]; then
    echo "❌ 错误: 不支持的操作系统 '${BUILD_OS}'"
    echo ""
    echo "支持的操作系统:"
    echo "  • alpine       - Alpine Linux"
    echo "  • debian       - Debian 12"
    echo ""
    echo "使用 '$0 --help' 查看完整帮助"
    exit 1
fi

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

log_step() {
    echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

log_info() {
    echo -e "${GREEN}✓${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

log_cmd() {
    if [[ "${DEBUG}" == "true" ]]; then
        echo -e "${CYAN}[命令]${NC} $1"
    fi
}

# 查找可用端口（最多尝试3次）
find_available_port() {
    local port=$1
    local max_attempts=3
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if ! lsof -Pi :${port} -sTCP:LISTEN -t >/dev/null 2>&1; then
            echo $port
            return 0
        fi
        # 输出到stderr避免污染返回值
        echo -e "${YELLOW}⚠${NC} 端口 ${port} 已被占用，尝试端口 $((port + 1))..." >&2
        port=$((port + 1))
        attempt=$((attempt + 1))
    done

    return 1
}

# 查找可用端口
AVAILABLE_PORT=$(find_available_port $REGISTRY_PORT)
if [ $? -ne 0 ]; then
    echo "❌ 错误: 无法找到可用端口（尝试了 ${REGISTRY_PORT} 到 $((REGISTRY_PORT + 2))）"
    echo "请手动指定端口: REGISTRY_PORT=20000 $0 ${BUILD_OS}"
    exit 1
fi

if [ "$AVAILABLE_PORT" != "$REGISTRY_PORT" ]; then
    echo "ℹ️  原始端口 ${REGISTRY_PORT} 被占用，使用端口 ${AVAILABLE_PORT}"
fi

REGISTRY_PORT=$AVAILABLE_PORT
REGISTRY_URL="localhost:${REGISTRY_PORT}"

# 清理函数
cleanup() {
    echo -e "\n${YELLOW}清理测试环境...${NC}"
    docker stop fit-e2e-app >/dev/null 2>&1 || true
    docker rm fit-e2e-app >/dev/null 2>&1 || true
    docker stop test-registry >/dev/null 2>&1 || true
    docker rm test-registry >/dev/null 2>&1 || true

    # 清理测试镜像
    log_info "清理测试镜像..."
    docker rmi "${REGISTRY_URL}/fit-framework:${BUILD_OS}" >/dev/null 2>&1 || true
    docker rmi "${REGISTRY_URL}/fit-framework:${FIT_VERSION}-${BUILD_OS}" >/dev/null 2>&1 || true
    docker rmi "fit-framework:${BUILD_OS}" >/dev/null 2>&1 || true
    docker rmi "fit-framework:${FIT_VERSION}-${BUILD_OS}" >/dev/null 2>&1 || true

    # 清理悬空镜像（<none>:<none>）
    log_info "清理悬空镜像..."
    docker image prune -f >/dev/null 2>&1 || true

    log_info "✓ 清理完成"
}

# 捕获退出信号
trap cleanup EXIT

echo "=============================================="
echo "🚀 FIT Framework 端到端测试"
echo "=============================================="
echo "操作系统: ${BUILD_OS}"
echo "FIT 版本: ${FIT_VERSION}"
echo "本地仓库: ${REGISTRY_URL}"
echo "=============================================="

# ========================================
# 步骤 1: 启动本地 Docker Registry
# ========================================
log_step "步骤 1/5: 启动本地 Docker Registry"

if docker ps | grep -q "test-registry"; then
    # 检查是否在正确的端口上运行
    RUNNING_PORT=$(docker port test-registry 5000 2>/dev/null | cut -d: -f2)
    if [ "$RUNNING_PORT" = "$REGISTRY_PORT" ]; then
        log_info "本地 Registry 已在端口 ${REGISTRY_PORT} 上运行"
    else
        log_warn "test-registry 容器正在运行但端口不匹配 (当前: ${RUNNING_PORT}, 期望: ${REGISTRY_PORT})"
        log_info "停止现有容器并重新启动..."
        docker stop test-registry >/dev/null 2>&1 || true
        docker rm test-registry >/dev/null 2>&1 || true

        log_info "启动本地 Docker Registry (端口 ${REGISTRY_PORT})..."
        docker run -d \
            -p ${REGISTRY_PORT}:5000 \
            --name test-registry \
            registry:latest > /dev/null
        sleep 2
        log_info "Registry 启动成功: http://${REGISTRY_URL}"
    fi
else
    log_info "启动本地 Docker Registry (端口 ${REGISTRY_PORT})..."
    log_cmd "docker run -d -p ${REGISTRY_PORT}:5000 --name test-registry registry:latest"
    docker run -d \
        -p ${REGISTRY_PORT}:5000 \
        --name test-registry \
        registry:latest > /dev/null

    sleep 2
    log_info "Registry 启动成功: http://${REGISTRY_URL}"
fi

# ========================================
# 步骤 2: 构建 FIT 基础镜像
# ========================================
log_step "步骤 2/5: 构建 FIT 基础镜像 (${BUILD_OS})"

# 准备制品
# 构建镜像
build_image() {
    log_info "构建 FIT Framework 基础镜像..."
    
    # 准备制品
    local script_dir=$(dirname "$0")
    local cache_path
    cache_path=$("$script_dir/common/download.sh" "$FIT_VERSION")
    
    if [[ $? -ne 0 ]] || [[ ! -f "${cache_path}" ]]; then
        log_error "准备 FIT Framework 制品失败"
        exit 1
    fi
    
    echo "✅ FIT Framework ${FIT_VERSION} 已就绪: ${cache_path}"
    
    # 复制到当前目录
    cp "${cache_path}" "${FIT_VERSION}.zip"
    trap "rm -f ${FIT_VERSION}.zip; cleanup" EXIT
    
    # 构建镜像
    if [[ "${DEBUG}" == "true" ]]; then
        if ! docker build \
            --build-arg FIT_VERSION="${FIT_VERSION}" \
            -t "fit-framework:${BUILD_OS}" \
            -t "fit-framework:${FIT_VERSION}-${BUILD_OS}" \
            -f "${BUILD_OS}/Dockerfile" \
            . ; then
            log_error "镜像构建失败"
            exit 1
        fi
    else
        if ! docker build --quiet \
            --build-arg FIT_VERSION="${FIT_VERSION}" \
            -t "fit-framework:${BUILD_OS}" \
            -t "fit-framework:${FIT_VERSION}-${BUILD_OS}" \
            -f "${BUILD_OS}/Dockerfile" \
            . > /dev/null; then
            log_error "镜像构建失败"
            exit 1
        fi
    fi
    
    log_info "✓ 镜像构建成功"
}

build_image

docker images fit-framework:${BUILD_OS} --format "  镜像: {{.Repository}}:{{.Tag}} ({{.Size}})"

# ========================================
# 步骤 3: 推送到本地仓库
# ========================================
log_step "步骤 3/5: 推送镜像到本地仓库"

log_info "标记镜像..."
log_cmd "docker tag \"fit-framework:${BUILD_OS}\" \"${REGISTRY_URL}/fit-framework:${BUILD_OS}\""
log_cmd "docker tag \"fit-framework:${BUILD_OS}\" \"${REGISTRY_URL}/fit-framework:${FIT_VERSION}-${BUILD_OS}\""
docker tag "fit-framework:${BUILD_OS}" "${REGISTRY_URL}/fit-framework:${BUILD_OS}"
docker tag "fit-framework:${BUILD_OS}" "${REGISTRY_URL}/fit-framework:${FIT_VERSION}-${BUILD_OS}"

log_info "推送镜像到 ${REGISTRY_URL}..."
log_cmd "docker push \"${REGISTRY_URL}/fit-framework:${BUILD_OS}\""
log_cmd "docker push \"${REGISTRY_URL}/fit-framework:${FIT_VERSION}-${BUILD_OS}\""
docker push --quiet "${REGISTRY_URL}/fit-framework:${BUILD_OS}"
docker push --quiet "${REGISTRY_URL}/fit-framework:${FIT_VERSION}-${BUILD_OS}"

log_info "镜像推送成功"

# 验证仓库
log_info "验证仓库内容..."
log_cmd "curl -s \"http://${REGISTRY_URL}/v2/_catalog\""
curl -s "http://${REGISTRY_URL}/v2/_catalog" | grep -q "fit-framework" && \
    log_info "✓ 镜像已在仓库中"

# ========================================
# 步骤 4: 启动基础镜像容器
# ========================================
log_step "步骤 4/5: 启动 FIT Framework 基础镜像"

# 清理可能存在的旧容器
log_cmd "docker stop fit-e2e-app"
log_cmd "docker rm fit-e2e-app"
docker stop fit-e2e-app >/dev/null 2>&1 || true
docker rm fit-e2e-app >/dev/null 2>&1 || true

log_info "从本地仓库拉取并启动镜像..."
log_cmd "docker run -d --name fit-e2e-app -p 8080:8080 \"${REGISTRY_URL}/fit-framework:${BUILD_OS}\""
CONTAINER_ID=$(docker run -d \
    --name fit-e2e-app \
    -p 8080:8080 \
    "${REGISTRY_URL}/fit-framework:${BUILD_OS}")

log_info "容器已启动: ${CONTAINER_ID:0:12}"
log_info "容器使用基础镜像的默认配置启动"
log_info "等待 FIT Framework 启动 (约 10-20 秒)..."

# 等待应用启动
for i in {1..30}; do
    if docker ps --filter "name=fit-e2e-app" --format "{{.Status}}" | grep -q "Up"; then
        # 使用 /actuator/plugins 作为健康检查端点
        if curl -s http://localhost:8080/actuator/plugins > /dev/null 2>&1; then
            log_info "✓ 应用已就绪"
            break
        fi
    fi

    if [ $i -eq 30 ]; then
        echo "错误: 应用启动超时"
        echo "查看日志:"
        docker logs fit-e2e-app --tail 50
        exit 1
    fi

    printf "."
    sleep 1
done
echo ""

# ========================================
# 步骤 5: 验证基础镜像
# ========================================
log_step "步骤 5/5: 验证基础镜像功能"

log_info "测试 1: 检查容器状态"
STATUS=$(docker inspect fit-e2e-app --format='{{.State.Status}}')
echo "  容器状态: ${STATUS}"

log_info "测试 2: 检查健康状态"
HEALTH=$(docker inspect fit-e2e-app --format='{{.State.Health.Status}}' 2>/dev/null || echo "无健康检查")
echo "  健康状态: ${HEALTH}"

log_info "测试 3: 访问 HTTP 端点"
if curl -s http://localhost:8080/actuator/plugins > /dev/null 2>&1; then
    echo "  ✓ HTTP 服务可访问"
    echo "  URL: http://localhost:8080/actuator/plugins"
    # 显示插件数量
    PLUGIN_COUNT=$(curl -s http://localhost:8080/actuator/plugins | jq '. | length' 2>/dev/null || echo "N/A")
    echo "  已加载插件数: ${PLUGIN_COUNT}"
else
    log_warn "HTTP 服务暂不可用 (这可能是正常的，FIT 可能还在初始化)"
fi

log_info "测试 4: 查看 FIT Framework 版本"
FIT_VERSION_OUTPUT=$(docker exec fit-e2e-app fit version 2>/dev/null || echo "N/A")
echo "  FIT 版本: ${FIT_VERSION_OUTPUT}"

log_info "测试 5: 查看容器日志"
echo "  最近日志:"
docker logs fit-e2e-app --tail 10 | sed 's/^/    /'

# ========================================
# 完成并显示信息
# ========================================
echo ""
echo "=============================================="
echo "✅ 端到端测试完成！"
echo "=============================================="
echo ""
echo "📊 测试摘要:"
echo "  • 基础镜像: fit-framework:${BUILD_OS} (${FIT_VERSION})"
echo "  • 本地仓库: ${REGISTRY_URL}"
echo "  • 运行镜像: ${REGISTRY_URL}/fit-framework:${BUILD_OS}"
echo "  • 容器名称: fit-e2e-app"
echo "  • 访问地址: http://localhost:8080"
echo ""
echo "🔍 查看资源:"
echo ""
echo "  1. 查看所有镜像:"
echo "     docker images | grep fit"
echo ""
echo "  2. 查看本地仓库:"
echo "     curl http://${REGISTRY_URL}/v2/_catalog | jq"
echo ""
echo "  3. 查看运行的容器:"
echo "     docker ps | grep fit"
echo ""
echo "  4. 查看容器日志:"
echo "     docker logs fit-e2e-app"
echo ""
echo "  5. 访问 actuator 端点:"
echo "     curl http://localhost:8080/actuator/plugins"
echo ""
echo "  6. 进入容器查看:"
echo "     docker exec -it fit-e2e-app bash"
echo ""
echo "  7. 查看 FIT 版本:"
echo "     docker exec fit-e2e-app fit version"
echo ""
echo "🧹 清理测试环境:"
echo ""
echo "  # 停止并删除容器"
echo "  docker stop fit-e2e-app"
echo "  docker rm fit-e2e-app"
echo ""
echo "  # 停止并删除本地仓库"
echo "  docker stop test-registry"
echo "  docker rm test-registry"
echo ""
echo "  # 删除测试镜像"
echo "  docker rmi ${REGISTRY_URL}/fit-framework:${BUILD_OS}"
echo "  docker rmi fit-framework:${BUILD_OS}"
echo ""
echo "=============================================="
echo ""
echo "💡 提示:"
echo "  • 测试完成后，脚本会自动清理测试镜像和容器"
echo "  • 如需手动清理，请使用上面的清理命令"
echo "  • registry:latest 镜像不会被清理（可复用）"
echo ""
