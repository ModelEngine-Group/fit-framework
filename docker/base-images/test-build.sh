#!/bin/bash
set -euo pipefail

# FIT Framework 基础镜像测试脚本
# 用于快速验证基础镜像是否可以正常构建和运行

OS_NAME="${1:-ubuntu}"
FIT_VERSION="${FIT_VERSION:-3.5.3}"

echo "=============================================="
echo "🧪 FIT Framework 基础镜像测试"
echo "=============================================="
echo "操作系统: ${OS_NAME}"
echo "FIT版本: ${FIT_VERSION}"
echo "=============================================="

# 检查目录是否存在
if [[ ! -d "${OS_NAME}" ]]; then
    echo "❌ 错误: 找不到目录 ${OS_NAME}/"
    echo "支持的操作系统: ubuntu, alpine, rocky, debian, amazonlinux, openeuler"
    exit 1
fi

# 构建基础镜像
echo "📦 步骤 1/3: 构建基础镜像..."
# 需要从 base-images 目录构建，以便访问 common/ 目录
docker build --build-arg FIT_VERSION="${FIT_VERSION}" \
    -t "fit-framework:${OS_NAME}-test" \
    -f "${OS_NAME}/Dockerfile" .

if [[ $? -ne 0 ]]; then
    echo "❌ 基础镜像构建失败"
    exit 1
fi

echo "✅ 基础镜像构建成功"
echo ""

# 测试基础镜像
echo "🧪 步骤 2/3: 测试基础镜像..."
echo "检查镜像大小..."
docker images "fit-framework:${OS_NAME}-test" --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}"

echo ""
echo "测试基本命令..."
if docker run --rm "fit-framework:${OS_NAME}-test" fit help > /dev/null 2>&1; then
    echo "✅ 基础镜像可以正常运行"
else
    echo "❌ 基础镜像运行失败"
    exit 1
fi

echo ""
echo "🏗️  步骤 3/3: 构建测试应用..."
echo "注意：此步骤需要Maven和完整的项目源码"
echo ""

# 询问是否构建测试应用
read -p "是否构建测试应用镜像? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "构建测试应用镜像..."
    cd ../..
    docker build -f "docker/base-images/${OS_NAME}/Dockerfile.test" \
        -t "fit-example:simple-web-app-${OS_NAME}" .

    if [[ $? -eq 0 ]]; then
        echo "✅ 测试应用镜像构建成功"
        echo ""
        echo "可以使用以下命令启动测试应用:"
        echo "  docker run -d -p 8080:8080 --name fit-test fit-example:simple-web-app-${OS_NAME}"
        echo "  curl http://localhost:8080/user?name=test&age=25"
    else
        echo "❌ 测试应用镜像构建失败"
        exit 1
    fi
else
    echo "⏭️  跳过测试应用构建"
fi

echo ""
echo "=============================================="
echo "🎉 测试完成!"
echo "=============================================="
echo "基础镜像: fit-framework:${OS_NAME}-test"
echo ""
echo "下一步:"
echo "  1. 启动容器: docker run -d -p 8080:8080 fit-framework:${OS_NAME}-test"
echo "  2. 查看日志: docker logs <container-id>"
echo "  3. 进入容器: docker exec -it <container-id> bash"
echo "=============================================="
