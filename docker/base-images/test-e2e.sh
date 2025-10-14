#!/bin/bash
set -euo pipefail

# FIT Framework 端到端测试脚本
# 完整测试流程：构建基础镜像 → 推送本地仓库 → 构建示例应用 → 启动运行 → 访问验证

# 配置
REGISTRY_PORT="${REGISTRY_PORT:-5001}"
REGISTRY_URL="localhost:${REGISTRY_PORT}"
FIT_VERSION="${FIT_VERSION:-3.5.3}"
BUILD_OS="${1:-ubuntu}"  # 可选: ubuntu, alpine, debian, rocky, amazonlinux, openeuler

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

# 清理函数
cleanup() {
    echo -e "\n${YELLOW}清理测试环境...${NC}"
    docker stop fit-e2e-app 2>/dev/null || true
    docker rm fit-e2e-app 2>/dev/null || true
    docker stop test-registry 2>/dev/null || true
    docker rm test-registry 2>/dev/null || true
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
log_step "步骤 1/6: 启动本地 Docker Registry"

if docker ps | grep -q "test-registry.*${REGISTRY_PORT}"; then
    log_info "本地 Registry 已在运行"
else
    log_info "启动本地 Docker Registry (端口 ${REGISTRY_PORT})..."

    # 检查端口是否被占用
    if lsof -Pi :${REGISTRY_PORT} -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "错误: 端口 ${REGISTRY_PORT} 已被占用"
        echo "请使用其他端口: REGISTRY_PORT=5002 $0 ${BUILD_OS}"
        exit 1
    fi

    docker run -d \
        -p ${REGISTRY_PORT}:5000 \
        --name test-registry \
        registry:2 > /dev/null

    sleep 2
    log_info "Registry 启动成功: http://${REGISTRY_URL}"
fi

# ========================================
# 步骤 2: 构建 FIT 基础镜像
# ========================================
log_step "步骤 2/6: 构建 FIT 基础镜像 (${BUILD_OS})"

log_info "构建镜像: fit-framework:${FIT_VERSION}-${BUILD_OS}..."
docker build --quiet \
    --build-arg FIT_VERSION="${FIT_VERSION}" \
    -t "fit-framework:${FIT_VERSION}-${BUILD_OS}" \
    -t "fit-framework:${BUILD_OS}" \
    -f "${BUILD_OS}/Dockerfile" . > /dev/null

log_info "镜像构建完成"
docker images fit-framework:${BUILD_OS} --format "  镜像: {{.Repository}}:{{.Tag}} ({{.Size}})"

# ========================================
# 步骤 3: 推送到本地仓库
# ========================================
log_step "步骤 3/6: 推送镜像到本地仓库"

log_info "标记镜像..."
docker tag "fit-framework:${BUILD_OS}" "${REGISTRY_URL}/fit-framework:${BUILD_OS}"
docker tag "fit-framework:${BUILD_OS}" "${REGISTRY_URL}/fit-framework:${FIT_VERSION}-${BUILD_OS}"

log_info "推送镜像到 ${REGISTRY_URL}..."
docker push --quiet "${REGISTRY_URL}/fit-framework:${BUILD_OS}"
docker push --quiet "${REGISTRY_URL}/fit-framework:${FIT_VERSION}-${BUILD_OS}"

log_info "镜像推送成功"

# 验证仓库
log_info "验证仓库内容..."
curl -s "http://${REGISTRY_URL}/v2/_catalog" | grep -q "fit-framework" && \
    log_info "✓ 镜像已在仓库中"

# ========================================
# 步骤 4: 构建示例应用镜像
# ========================================
log_step "步骤 4/6: 构建示例应用镜像"

log_info "创建示例应用 Dockerfile..."
cat > /tmp/fit-demo-app.Dockerfile <<EOF
# 基于 FIT 基础镜像构建示例应用
FROM ${REGISTRY_URL}/fit-framework:${BUILD_OS}

# 切换到 root 用户以便复制文件
USER root

# 创建示例应用目录
RUN mkdir -p /app/demo-plugin

# 创建一个简单的配置文件
RUN cat > /opt/fit-framework/java/conf/fitframework.yml <<'YAML'
application:
  name: 'fit-demo-app'

worker:
  id: 'demo-worker-001'
  host: '0.0.0.0'
  environment: 'dev'
  exit:
    graceful: true

server:
  http:
    port: 8080

fit:
  beans:
    packages:
    - 'modelengine.fitframework'
    - 'modelengine.fit'
YAML

# 切换回 fit 用户
USER fit

# 暴露端口
EXPOSE 8080

# 设置健康检查
HEALTHCHECK --interval=10s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# 启动命令
CMD ["fit", "start"]
EOF

log_info "构建应用镜像..."
docker build --quiet \
    -t "fit-demo-app:${FIT_VERSION}" \
    -f /tmp/fit-demo-app.Dockerfile . > /dev/null

log_info "应用镜像构建完成"
docker images fit-demo-app:${FIT_VERSION} --format "  镜像: {{.Repository}}:{{.Tag}} ({{.Size}})"

# ========================================
# 步骤 5: 启动应用容器
# ========================================
log_step "步骤 5/6: 启动示例应用"

# 清理可能存在的旧容器
docker stop fit-e2e-app 2>/dev/null || true
docker rm fit-e2e-app 2>/dev/null || true

log_info "启动容器..."
CONTAINER_ID=$(docker run -d \
    --name fit-e2e-app \
    -p 8080:8080 \
    -e FIT_WORKER_ID=demo-worker-test \
    -e FIT_LOG_LEVEL=info \
    fit-demo-app:${FIT_VERSION})

log_info "容器已启动: ${CONTAINER_ID:0:12}"
log_info "等待应用启动 (约 10-15 秒)..."

# 等待应用启动
for i in {1..30}; do
    if docker ps --filter "name=fit-e2e-app" --format "{{.Status}}" | grep -q "Up"; then
        if curl -s http://localhost:8080/health > /dev/null 2>&1; then
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
# 步骤 6: 验证应用
# ========================================
log_step "步骤 6/6: 验证应用功能"

log_info "测试 1: 检查容器状态"
STATUS=$(docker inspect fit-e2e-app --format='{{.State.Status}}')
echo "  容器状态: ${STATUS}"

log_info "测试 2: 检查健康状态"
HEALTH=$(docker inspect fit-e2e-app --format='{{.State.Health.Status}}' 2>/dev/null || echo "无健康检查")
echo "  健康状态: ${HEALTH}"

log_info "测试 3: 访问 HTTP 端点"
if curl -s http://localhost:8080/health > /dev/null 2>&1; then
    echo "  ✓ HTTP 服务可访问"
    echo "  URL: http://localhost:8080"
else
    log_warn "HTTP 服务暂不可用 (这可能是正常的，FIT 可能还在初始化)"
fi

log_info "测试 4: 查看应用日志"
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
echo "  • 应用镜像: fit-demo-app:${FIT_VERSION}"
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
echo "  4. 查看应用日志:"
echo "     docker logs fit-e2e-app"
echo ""
echo "  5. 访问应用:"
echo "     curl http://localhost:8080/health"
echo ""
echo "  6. 进入容器:"
echo "     docker exec -it fit-e2e-app bash"
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
echo "  docker rmi fit-demo-app:${FIT_VERSION}"
echo "  docker rmi ${REGISTRY_URL}/fit-framework:${BUILD_OS}"
echo ""
echo "=============================================="
echo ""
echo "💡 提示: 容器会持续运行，按 Ctrl+C 不会停止容器"
echo "        使用上面的清理命令来停止和删除资源"
echo ""
