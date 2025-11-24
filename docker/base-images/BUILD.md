# FIT Framework Docker 基础镜像构建与发布指南

本文档说明如何构建和发布 FIT Framework 的 Docker 基础镜像。

## 目录

- [前置要求](#前置要求)
- [快速开始](#快速开始)
- [构建单个镜像](#构建单个镜像)
- [批量构建所有镜像](#批量构建所有镜像)
- [测试镜像](#测试镜像)
- [发布镜像](#发布镜像)
- [本地测试环境](#本地测试环境)
- [生产发布流程](#生产发布流程)
- [故障排除](#故障排除)

---

## 前置要求

### 必需工具

- **Docker**: 20.10 或更高版本
- **Bash**: 4.0 或更高版本
- **网络连接**: 能够访问 GitHub Releases

### 权限要求

- 本地 Docker 执行权限
- 推送到镜像仓库的权限（如果需要发布）

### 验证环境

```bash
# 检查 Docker 版本
docker --version

# 检查 Docker 是否正常运行
docker info

# 检查磁盘空间（至少需要 10GB）
df -h
```

### 构建加速机制

本项目采用了**本地缓存机制**来加速构建：
1. 构建脚本会自动将 `fit-framework.zip` 下载到系统临时目录（如 `/tmp/fit-framework-cache/`）。
2. `Dockerfile` 使用 `COPY` 指令直接使用本地文件，避免在容器内重复下载。
3. 构建过程中会自动将制品复制到构建上下文，并在构建完成后自动清理。

---

## 快速开始

### 1. 进入构建目录

```bash
cd docker/base-images
```

### 2. 构建单个镜像（以 Alpine 为例）

```bash
cd alpine
./build.sh
```

### 3. 查看构建的镜像

```bash
docker images | grep fit-framework
```

输出示例：
```
fit-framework    3.6.0-alpine    abc123def456    2 minutes ago    700MB
fit-framework    alpine          abc123def456    2 minutes ago    700MB
fit-framework    latest-alpine   abc123def456    2 minutes ago    700MB
```

---

## 构建单个镜像

### 基本用法

每个操作系统目录下都有独立的 `build.sh` 脚本：

```bash
cd <os-name>    # alpine, debian
./build.sh [FIT_VERSION] [REGISTRY]
```

### 参数说明

| 参数 | 说明 | 默认值 | 示例 |
|------|------|--------|------|
| `FIT_VERSION` | FIT Framework 版本号 | `3.6.0` | `3.6.1` |
| `REGISTRY` | 镜像仓库前缀（带结尾斜杠）| 无前缀 | `localhost:5000/` |

### 示例

**1. 使用默认版本构建**
```bash
cd alpine
./build.sh
```

**2. 指定版本构建**
```bash
cd alpine
./build.sh 3.6.1
```

**3. 构建并推送到私有仓库**
```bash
cd alpine
PUSH_IMAGE=true ./build.sh 3.6.0 registry.mycompany.com/fit/
```

**4. 使用本地镜像仓库**
```bash
cd alpine
PUSH_IMAGE=true ./build.sh 3.6.0 localhost:5000/
```

### 环境变量

| 变量名 | 说明 | 默认值 | 示例 |
|--------|------|--------|------|
| `PUSH_IMAGE` | 是否推送镜像到仓库 | `false` | `true` |
| `BUILD_ARGS` | 额外的 docker build 参数 | 空 | `--no-cache` |

### 生成的镜像标签

构建脚本会自动生成多个标签：

```
${REGISTRY}fit-framework:${VERSION}-${OS}      # 例如: fit-framework:3.6.0-alpine
${REGISTRY}fit-framework:${OS}                 # 例如: fit-framework:alpine
${REGISTRY}fit-framework:latest-${OS}          # 仅默认版本，例如: fit-framework:latest-alpine
```

---

## 批量构建所有镜像

### 使用批量构建脚本

在 `base-images` 目录下提供了 `build_all.sh` 脚本：

```bash
cd docker/base-images
./build_all.sh build [VERSION] [REGISTRY]
```

### 示例

**1. 构建所有操作系统的镜像**
```bash
./build_all.sh build
```

**2. 构建指定版本**
```bash
./build_all.sh build 3.6.1
```

**3. 构建并推送到仓库**
```bash
PUSH_IMAGE=true ./build_all.sh build 3.6.0 registry.mycompany.com/fit/
```

**4. 并行构建（加速）**
```bash
PARALLEL=4 ./build_all.sh build
```

### 选择性构建

**只构建特定的操作系统**
```bash
ONLY_OS=alpine,debian ./build_all.sh build
```

**跳过某些操作系统**（当前无跳过操作，只有2个操作系统）
```bash
# 示例（如果需要将来扩展）
# SKIP_OS=<os-name> ./build_all.sh build
```

### 环境变量

| 变量名 | 说明 | 默认值 | 示例 |
|--------|------|--------|------|
| `PARALLEL` | 并行构建数量 | `2` | `4` |
| `ONLY_OS` | 仅构建的 OS 列表（逗号分隔） | 全部 | `alpine,debian` |
| `SKIP_OS` | 跳过的 OS 列表（逗号分隔） | 无 | `openeuler` |
| `PUSH_IMAGE` | 是否推送镜像 | `false` | `true` |

### 查看支持的操作系统

```bash
./build_all.sh list
```

输出：
```
支持的操作系统列表:
====================
alpine       - Alpine Linux (轻量级云原生)
debian       - Debian 12 (稳定可靠)
====================
总计: 2 个操作系统
```

---

## 测试镜像

### 使用端到端测试脚本

我们提供了完整的端到端测试脚本，自动完成构建、推送、启动和验证：

```bash
cd docker/base-images

# 测试单个操作系统（默认 Alpine）
./test-e2e.sh

# 测试其他操作系统
./test-e2e.sh alpine
./test-e2e.sh debian
```

测试脚本会自动：
1. 启动本地 Docker Registry（端口 15000，自动检测冲突）
2. 构建基础镜像
3. 推送到本地仓库
4. 从仓库拉取并启动容器
5. 验证功能（健康检查、插件加载、HTTP 服务）

**自定义配置**：
```bash
# 使用不同端口
REGISTRY_PORT=20000 ./test-e2e.sh alpine

# 使用不同版本
FIT_VERSION=3.6.1 ./test-e2e.sh alpine
```

详细的测试说明请参考 [README.md](README.md#-测试镜像)。

### 手动测试

**1. 测试基础镜像**
```bash
# 运行 FIT 命令
docker run --rm fit-framework:alpine fit help

# 启动容器
docker run -d -p 8080:8080 --name fit-test fit-framework:alpine

# 查看日志
docker logs fit-test

# 进入容器
docker exec -it fit-test bash

# 停止容器
docker stop fit-test
docker rm fit-test
```

**2. 测试健康检查**
```bash
# 启动容器
docker run -d --name fit-test fit-framework:alpine

# 等待几秒后检查健康状态
docker inspect fit-test --format='{{.State.Health.Status}}'
```

**3. 测试环境变量**
```bash
docker run --rm \
  -e FIT_WORKER_ID=test-worker \
  -e FIT_LOG_LEVEL=debug \
  fit-framework:alpine fit help
```

---

## 发布镜像

### 发布到 Docker Hub

**1. 登录 Docker Hub**
```bash
docker login
```

**2. 构建并推送**
```bash
cd alpine
PUSH_IMAGE=true ./build.sh 3.6.0 modelengine/
```

**3. 批量推送所有镜像**
```bash
PUSH_IMAGE=true ./build_all.sh build 3.6.0 modelengine/
```

### 发布到私有镜像仓库

**1. 登录私有仓库**
```bash
docker login registry.mycompany.com
```

**2. 构建并推送**
```bash
cd alpine
PUSH_IMAGE=true ./build.sh 3.6.0 registry.mycompany.com/fit/
```

**3. 验证推送成功**
```bash
# 在另一台机器上拉取
docker pull registry.mycompany.com/fit/fit-framework:3.6.0-alpine
```

### 发布到阿里云容器镜像服务

**1. 登录阿里云镜像服务**
```bash
docker login --username=your_username registry.cn-hangzhou.aliyuncs.com
```

**2. 构建并推送**
```bash
cd alpine
PUSH_IMAGE=true ./build.sh 3.6.0 registry.cn-hangzhou.aliyuncs.com/fit-framework/
```

---

## 本地测试环境

### 启动本地镜像仓库

**方法一：使用 Docker Registry**

```bash
# 1. 启动本地 registry
docker run -d \
  -p 5000:5000 \
  --name registry \
  --restart=always \
  registry:2

# 2. 验证 registry 运行
curl http://localhost:5000/v2/_catalog
```

**方法二：使用 Docker Registry UI**

```bash
# 1. 创建 docker-compose.yml
cat > /tmp/registry-compose.yml <<EOF
version: '3.8'

services:
  registry:
    image: registry:2
    ports:
      - "5000:5000"
    environment:
      REGISTRY_STORAGE_FILESYSTEM_ROOTDIRECTORY: /data
    volumes:
      - registry-data:/data
    restart: always

  registry-ui:
    image: joxit/docker-registry-ui:latest
    ports:
      - "8081:80"
    environment:
      REGISTRY_TITLE: Local FIT Registry
      REGISTRY_URL: http://registry:5000
      SINGLE_REGISTRY: true
    depends_on:
      - registry
    restart: always

volumes:
  registry-data:
EOF

# 2. 启动服务
docker-compose -f /tmp/registry-compose.yml up -d

# 3. 访问 UI
open http://localhost:8081
```

### 测试完整流程

**1. 构建并推送到本地仓库**
```bash
cd docker/base-images/alpine
PUSH_IMAGE=true ./build.sh 3.6.0 localhost:5000/
```

**2. 查看本地仓库的镜像**
```bash
# 列出所有镜像
curl http://localhost:5000/v2/_catalog

# 查看特定镜像的标签
curl http://localhost:5000/v2/fit-framework/tags/list
```

**3. 拉取测试**
```bash
# 删除本地镜像
docker rmi localhost:5000/fit-framework:3.6.0-alpine

# 重新拉取
docker pull localhost:5000/fit-framework:3.6.0-alpine

# 测试运行
docker run --rm localhost:5000/fit-framework:3.6.0-alpine fit help
```

**4. 批量测试所有镜像**
```bash
# 构建并推送所有镜像到本地仓库
cd docker/base-images
PUSH_IMAGE=true PARALLEL=4 ./build_all.sh build 3.6.0 localhost:5000/

# 检查所有镜像
for os in alpine debian; do
  echo "Testing ${os}..."
  docker pull localhost:5000/fit-framework:${os}
  docker run --rm localhost:5000/fit-framework:${os} fit help
done
```

### 清理本地仓库

```bash
# 停止并删除 registry
docker stop registry
docker rm registry

# 使用 docker-compose 启动的清理
docker-compose -f /tmp/registry-compose.yml down -v
```

---

## 生产发布流程

### 发布检查清单

在正式发布前，请确认以下事项：

- [ ] 确认 FIT Framework 版本号正确
- [ ] 验证该版本的 Release 已在 GitHub 上发布
- [ ] 本地构建测试通过
- [ ] 镜像大小合理（< 1.5GB）
- [ ] 健康检查功能正常
- [ ] 已在本地 registry 测试推送流程
- [ ] 已准备好生产仓库的登录凭证
- [ ] 已更新 README 中的版本号引用

### 标准发布步骤

**步骤 1: 更新版本号**

如果需要发布新版本，先更新默认版本号：

```bash
# 更新所有 Dockerfile
for dockerfile in */Dockerfile; do
  sed -i '' 's/ARG FIT_VERSION=.*/ARG FIT_VERSION=3.6.1/g' "$dockerfile"
done

# 更新所有 build.sh
for buildsh in */build.sh build_all.sh; do
  sed -i '' 's/DEFAULT_FIT_VERSION=".*/DEFAULT_FIT_VERSION="3.6.1"/g' "$buildsh"
done
```

**步骤 2: 本地测试**

```bash
# 测试构建单个镜像
cd alpine
./build.sh

# 测试镜像功能
docker run --rm fit-framework:alpine fit help
```

**步骤 3: 推送到本地 registry 验证**

```bash
# 启动本地 registry
docker run -d -p 5000:5000 --name registry registry:2

# 推送到本地
PUSH_IMAGE=true ./build.sh 3.6.1 localhost:5000/

# 验证
docker pull localhost:5000/fit-framework:3.6.1-alpine
```

**步骤 4: 登录生产仓库**

```bash
# Docker Hub
docker login

# 或私有仓库
docker login registry.mycompany.com
```

**步骤 5: 批量构建和推送**

```bash
cd docker/base-images

# 构建并推送所有镜像
PUSH_IMAGE=true PARALLEL=4 ./build_all.sh build 3.6.1 modelengine/

# 或推送到私有仓库
PUSH_IMAGE=true PARALLEL=4 ./build_all.sh build 3.6.1 registry.mycompany.com/fit/
```

**步骤 6: 验证发布**

```bash
# 在干净的环境中测试
docker rmi fit-framework:3.6.1-alpine

# 从仓库拉取
docker pull modelengine/fit-framework:3.6.1-alpine

# 验证版本和功能
docker run --rm modelengine/fit-framework:3.6.1-alpine fit help
```

**步骤 7: 更新文档**

更新 README.md 中的版本号引用和示例。

**步骤 8: 提交代码**

```bash
git add .
git commit -m "chore(docker): Bump base images to FIT Framework v3.6.1"
git push
```

### 自动化发布（可选）

可以考虑使用 GitHub Actions 自动化发布流程。创建 `.github/workflows/docker-release.yml`：

```yaml
name: Build and Push Docker Images

on:
  release:
    types: [published]

jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        os: [alpine, debian]

    steps:
      - uses: actions/checkout@v3

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2

      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Extract version
        id: version
        run: echo "version=${GITHUB_REF#refs/tags/v}" >> $GITHUB_OUTPUT

      - name: Build and push
        run: |
          cd docker/base-images/${{ matrix.os }}
          PUSH_IMAGE=true ./build.sh ${{ steps.version.outputs.version }} modelengine/
```

---

## 故障排除

### 常见问题

**1. 下载 FIT Framework 失败**

```
ERROR: wget failed to download https://github.com/...
```

**解决方案**：
- 检查网络连接
- 确认 GitHub Release 存在
- 尝试手动下载验证 URL

```bash
wget -O /tmp/test.zip "https://github.com/ModelEngine-Group/fit-framework/releases/download/v3.6.0/3.6.0.zip"
unzip -t /tmp/test.zip
```

**2. 推送到仓库失败**

```
ERROR: denied: requested access to the resource is denied
```

**解决方案**：
- 确认已登录：`docker login`
- 检查仓库权限
- 验证镜像名称格式正确

**3. 构建缓存问题**

**解决方案**：
```bash
# 清理构建缓存
docker builder prune -a

# 使用 --no-cache 重新构建
BUILD_ARGS="--no-cache" ./build.sh
```

**4. 磁盘空间不足**

```
ERROR: no space left on device
```

**解决方案**：
```bash
# 清理未使用的镜像
docker image prune -a

# 清理构建缓存
docker builder prune -a

# 清理所有未使用的资源
docker system prune -a --volumes
```

**5. ARG 变量未生效**

如果构建时提示下载 URL 不正确，检查 Dockerfile 中的 ARG 声明：

```dockerfile
ARG FIT_VERSION=3.6.0
FROM alpine:3.19

# 必须在 FROM 之后重新声明
ARG FIT_VERSION=3.6.0
```

### 日志和调试

**查看构建日志**
```bash
# 构建时保存日志
./build.sh 2>&1 | tee build.log

# 批量构建的日志保存在
ls -la build-logs/
```

**调试 Dockerfile**
```bash
# 注意：手动构建前需要先下载制品
./common/download.sh 3.6.0

# 交互式调试
docker run -it --rm alpine:3.19 sh

# 逐步执行 Dockerfile 命令
# 然后手动执行每一步
```

**检查镜像内容**
```bash
# 查看镜像层
docker history fit-framework:alpine

# 导出镜像检查
docker save fit-framework:alpine | tar -xv
```

### 获取帮助

- **GitHub Issues**: https://github.com/ModelEngine-Group/fit-framework/issues
- **邮件支持**: support@fit-framework.org
- **构建脚本帮助**: `./build.sh help`

---

## 附录

### A. 镜像大小参考

| 操作系统 | 预期大小 | 说明 |
|----------|----------|------|
| Alpine 3.19 | ~700 MB | 最小化镜像 |
| Debian 12 | ~900 MB | 稳定可靠 |

### B. 相关资源

- [FIT Framework 文档](../../docs/)
- [Dockerfile 最佳实践](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
- [Docker 多阶段构建](https://docs.docker.com/build/building/multi-stage/)
- [容器镜像安全](https://docs.docker.com/scout/)

### C. 版本兼容性

| FIT Version | JDK Version | 最低 Docker 版本 |
|-------------|-------------|------------------|
| 3.5.x       | 17          | 20.10           |
| 3.6.x       | 17          | 20.10           |

---

**最后更新**: 2025-11-23
**维护者**: FIT Framework Team
