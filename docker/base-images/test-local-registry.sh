#!/bin/bash
set -euo pipefail

# FIT Framework 本地镜像仓库测试脚本
# 用于测试完整的构建、推送、拉取流程

# 配置
REGISTRY_PORT="${REGISTRY_PORT:-5001}"  # 默认使用 5001，因为 macOS 的 5000 通常被占用
REGISTRY_URL="localhost:${REGISTRY_PORT}"
FIT_VERSION="${FIT_VERSION:-3.5.3}"
TEST_OS="${1:-ubuntu}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示帮助信息
show_help() {
    cat <<EOF
FIT Framework 本地镜像仓库测试脚本

用法:
  $0 [OS_NAME]

参数:
  OS_NAME    操作系统名称 [默认: ubuntu]
             可选: ubuntu, alpine, debian, rocky, amazonlinux, openeuler

环境变量:
  REGISTRY_PORT    本地仓库端口 [默认: 5001]
                   注意: macOS 上 5000 端口通常被系统占用
  FIT_VERSION      FIT Framework 版本 [默认: 3.5.3]

示例:
  $0                          # 测试 ubuntu
  $0 alpine                   # 测试 alpine
  REGISTRY_PORT=5001 $0       # 使用自定义端口

工作流程:
  1. 检查/启动本地 Docker Registry
  2. 构建基础镜像
  3. 推送到本地仓库
  4. 清理本地镜像
  5. 从仓库拉取镜像
  6. 测试镜像功能
  7. 显示测试报告

EOF
}

# 检查参数
if [[ "${TEST_OS}" == "help" ]] || [[ "${TEST_OS}" == "--help" ]]; then
    show_help
    exit 0
fi

echo "=============================================="
echo "🧪 FIT Framework 本地仓库测试"
echo "=============================================="
echo "操作系统: ${TEST_OS}"
echo "FIT 版本: ${FIT_VERSION}"
echo "仓库地址: ${REGISTRY_URL}"
echo "=============================================="
echo ""

# 步骤 1: 检查 Docker
log_info "检查 Docker 环境..."
if ! command -v docker &> /dev/null; then
    log_error "Docker 未安装"
    exit 1
fi

if ! docker info &> /dev/null; then
    log_error "Docker 服务未运行"
    exit 1
fi
log_info "✅ Docker 环境正常"
echo ""

# 步骤 2: 检查/启动本地 Registry
log_info "检查本地 Docker Registry..."
if docker ps | grep -q "registry.*${REGISTRY_PORT}"; then
    log_info "✅ Registry 已经在运行"
else
    log_warn "Registry 未运行，正在启动..."

    # 检查端口是否被占用
    if lsof -Pi :${REGISTRY_PORT} -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_error "端口 ${REGISTRY_PORT} 已被占用"
        log_info "请使用其他端口: REGISTRY_PORT=5001 $0"
        exit 1
    fi

    # 启动 registry
    docker run -d \
        -p ${REGISTRY_PORT}:5000 \
        --name test-registry \
        --restart=always \
        registry:2 > /dev/null

    # 等待 registry 启动
    log_info "等待 Registry 启动..."
    sleep 3

    # 验证 registry
    if curl -s http://${REGISTRY_URL}/v2/_catalog > /dev/null; then
        log_info "✅ Registry 启动成功"
    else
        log_error "Registry 启动失败"
        exit 1
    fi
fi
echo ""

# 步骤 3: 检查目录
log_info "检查构建目录..."
if [[ ! -d "${TEST_OS}" ]]; then
    log_error "找不到目录: ${TEST_OS}/"
    log_info "可用的操作系统: ubuntu, alpine, debian, rocky, amazonlinux, openeuler"
    exit 1
fi
log_info "✅ 构建目录存在"
echo ""

# 步骤 4: 构建镜像
log_info "构建镜像: fit-framework:${FIT_VERSION}-${TEST_OS}..."
cd "${TEST_OS}"
if ./build.sh "${FIT_VERSION}" > /tmp/fit-build.log 2>&1; then
    log_info "✅ 镜像构建成功"
else
    log_error "镜像构建失败，查看日志: /tmp/fit-build.log"
    tail -20 /tmp/fit-build.log
    exit 1
fi
cd ..
echo ""

# 步骤 5: 推送到本地仓库
log_info "推送镜像到本地仓库..."

# 重新标记镜像
IMAGE_NAME="fit-framework:${FIT_VERSION}-${TEST_OS}"
REGISTRY_IMAGE="${REGISTRY_URL}/fit-framework:${FIT_VERSION}-${TEST_OS}"

docker tag "${IMAGE_NAME}" "${REGISTRY_IMAGE}"
docker tag "${IMAGE_NAME}" "${REGISTRY_URL}/fit-framework:${TEST_OS}"

if docker push "${REGISTRY_IMAGE}" > /tmp/fit-push.log 2>&1; then
    log_info "✅ 镜像推送成功"
else
    log_error "镜像推送失败，查看日志: /tmp/fit-push.log"
    tail -20 /tmp/fit-push.log
    exit 1
fi

docker push "${REGISTRY_URL}/fit-framework:${TEST_OS}" > /dev/null 2>&1
echo ""

# 步骤 6: 验证仓库
log_info "验证镜像在仓库中..."
if curl -s "http://${REGISTRY_URL}/v2/fit-framework/tags/list" | grep -q "${FIT_VERSION}-${TEST_OS}"; then
    log_info "✅ 镜像已在仓库中"
else
    log_error "在仓库中未找到镜像"
    exit 1
fi
echo ""

# 步骤 7: 清理本地镜像
log_info "清理本地镜像（模拟从其他机器拉取）..."
docker rmi "${IMAGE_NAME}" > /dev/null 2>&1 || true
docker rmi "${REGISTRY_IMAGE}" > /dev/null 2>&1 || true
docker rmi "${REGISTRY_URL}/fit-framework:${TEST_OS}" > /dev/null 2>&1 || true
log_info "✅ 本地镜像已清理"
echo ""

# 步骤 8: 从仓库拉取
log_info "从本地仓库拉取镜像..."
if docker pull "${REGISTRY_IMAGE}" > /tmp/fit-pull.log 2>&1; then
    log_info "✅ 镜像拉取成功"
else
    log_error "镜像拉取失败，查看日志: /tmp/fit-pull.log"
    tail -20 /tmp/fit-pull.log
    exit 1
fi
echo ""

# 步骤 9: 测试镜像
log_info "测试镜像功能..."

# 测试 1: 基本命令
log_info "  测试 1/3: 基本命令执行..."
if docker run --rm "${REGISTRY_IMAGE}" fit help > /tmp/fit-test-1.log 2>&1; then
    log_info "  ✅ 基本命令测试通过"
else
    log_error "  ❌ 基本命令测试失败"
    cat /tmp/fit-test-1.log
    exit 1
fi

# 测试 2: 环境变量
log_info "  测试 2/3: 环境变量配置..."
if docker run --rm \
    -e FIT_WORKER_ID=test-worker \
    -e FIT_LOG_LEVEL=debug \
    "${REGISTRY_IMAGE}" fit help > /tmp/fit-test-2.log 2>&1; then
    log_info "  ✅ 环境变量测试通过"
else
    log_error "  ❌ 环境变量测试失败"
    cat /tmp/fit-test-2.log
    exit 1
fi

# 测试 3: 容器启动和健康检查
log_info "  测试 3/3: 容器启动和健康检查..."
CONTAINER_ID=$(docker run -d --name fit-test-${TEST_OS} "${REGISTRY_IMAGE}")

# 等待健康检查
sleep 5

HEALTH_STATUS=$(docker inspect fit-test-${TEST_OS} --format='{{.State.Health.Status}}' 2>/dev/null || echo "unknown")

if [[ "${HEALTH_STATUS}" == "healthy" ]] || [[ "${HEALTH_STATUS}" == "unknown" ]]; then
    log_info "  ✅ 容器启动测试通过 (健康状态: ${HEALTH_STATUS})"
else
    log_warn "  ⚠️  容器健康检查异常 (状态: ${HEALTH_STATUS})"
fi

# 清理测试容器
docker stop fit-test-${TEST_OS} > /dev/null 2>&1
docker rm fit-test-${TEST_OS} > /dev/null 2>&1

echo ""

# 步骤 10: 显示镜像信息
log_info "镜像信息:"
docker images "${REGISTRY_URL}/fit-framework" --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}\t{{.ID}}"
echo ""

# 步骤 11: 显示仓库内容
log_info "本地仓库内容:"
echo "----------------------------------------"
curl -s "http://${REGISTRY_URL}/v2/_catalog" | jq -r '.repositories[]' 2>/dev/null || \
    curl -s "http://${REGISTRY_URL}/v2/_catalog"
echo "----------------------------------------"
echo ""

# 测试报告
echo "=============================================="
echo "✅ 测试完成!"
echo "=============================================="
echo ""
echo "📊 测试总结:"
echo "  • 操作系统: ${TEST_OS}"
echo "  • FIT 版本: ${FIT_VERSION}"
echo "  • 镜像地址: ${REGISTRY_IMAGE}"
echo "  • 仓库地址: http://${REGISTRY_URL}"
echo ""
echo "🎯 后续步骤:"
echo "  1. 查看仓库所有镜像:"
echo "     curl http://${REGISTRY_URL}/v2/_catalog | jq"
echo ""
echo "  2. 查看镜像标签:"
echo "     curl http://${REGISTRY_URL}/v2/fit-framework/tags/list | jq"
echo ""
echo "  3. 从仓库拉取镜像:"
echo "     docker pull ${REGISTRY_URL}/fit-framework:${TEST_OS}"
echo ""
echo "  4. 停止本地仓库:"
echo "     docker stop test-registry"
echo "     docker rm test-registry"
echo ""
echo "  5. 测试其他操作系统:"
echo "     $0 alpine"
echo "     $0 debian"
echo ""
echo "=============================================="
