# Docker 镜像测试指南

本文档说明如何使用不同的测试脚本来验证 Docker 基础镜像。

## 测试脚本对比

### test-build.sh - 快速构建测试

**用途**: 快速验证镜像是否能正常构建和运行

**特点**:
- ✅ 简单快速，只需几分钟
- ✅ 不需要 Docker Registry
- ✅ 适合本地开发调试
- ❌ 不测试推送/拉取流程

**使用场景**:
1. 修改 Dockerfile 后快速验证
2. 本地开发测试
3. 检查镜像大小和基本功能

**使用方法**:
```bash
# 必须在 base-images 目录下运行
cd docker/base-images

# 测试 Ubuntu 镜像
./test-build.sh ubuntu

# 测试 Alpine 镜像
./test-build.sh alpine

# 指定版本
FIT_VERSION=3.5.4 ./test-build.sh ubuntu
```

**注意**: 脚本必须在 `docker/base-images` 目录下运行，因为需要访问 `common/` 目录中的脚本文件。

**执行流程**:
```
1. 构建基础镜像
   ↓
2. 显示镜像大小
   ↓
3. 测试基本命令 (fit --version)
   ↓
4. (可选) 构建测试应用
```

---

### test-local-registry.sh - 完整发布测试

**用途**: 测试完整的构建、推送、拉取流程

**特点**:
- ✅ 完整模拟生产发布流程
- ✅ 自动管理本地 Registry
- ✅ 测试推送和拉取功能
- ✅ 详细的测试报告
- ⏱️ 需要更多时间（约 5-10 分钟）

**使用场景**:
1. **准备发布新版本前的验证**
2. 测试镜像是否可以正常推送到仓库
3. 验证从仓库拉取的镜像是否正常工作
4. 模拟生产环境的完整流程

**使用方法**:
```bash
# 必须在 base-images 目录下运行
cd docker/base-images

# 使用默认端口 5001
./test-local-registry.sh ubuntu

# 使用自定义端口
REGISTRY_PORT=5002 ./test-local-registry.sh ubuntu

# 测试不同操作系统
./test-local-registry.sh alpine
./test-local-registry.sh debian

# 指定版本
FIT_VERSION=3.5.4 ./test-local-registry.sh ubuntu
```

**注意**: 脚本必须在 `docker/base-images` 目录下运行，因为需要访问 `common/` 目录和各 OS 子目录。

**执行流程**:
```
1. 检查 Docker 环境
   ↓
2. 启动/检查本地 Registry (localhost:5001)
   ↓
3. 构建基础镜像
   ↓
4. 推送到本地仓库
   ↓
5. 清理本地镜像（模拟从其他机器）
   ↓
6. 从仓库拉取镜像
   ↓
7. 测试镜像功能（3 项测试）
   ↓
8. 生成测试报告
```

---

## 端口说明

### 为什么默认使用 5001？

在 macOS 系统上，端口 5000 通常被系统服务占用（如 ControlCenter），因此 `test-local-registry.sh` 默认使用 5001 端口。

### 检查端口占用

```bash
# 检查 5000 端口
lsof -i :5000

# 检查 5001 端口
lsof -i :5001
```

### 使用其他端口

如果 5001 也被占用，可以使用任何其他端口：

```bash
REGISTRY_PORT=5002 ./test-local-registry.sh ubuntu
REGISTRY_PORT=6000 ./test-local-registry.sh ubuntu
```

---

## 使用建议

### 场景 1: 日常开发调试

**推荐**: 使用 `test-build.sh`

```bash
# 修改 Dockerfile 后
cd docker/base-images
./test-build.sh ubuntu

# 快速验证更改是否正确
```

### 场景 2: 准备发布新版本

**推荐**: 使用 `test-local-registry.sh`

```bash
# 完整测试发布流程
cd docker/base-images
./test-local-registry.sh ubuntu

# 如果测试通过，再发布到生产仓库
cd ubuntu
PUSH_IMAGE=true ./build.sh 3.5.3 registry.example.com/
```

### 场景 3: 测试所有操作系统

**推荐**: 批量测试脚本

```bash
# 快速测试所有 OS
for os in ubuntu alpine debian rocky amazonlinux openeuler; do
    echo "Testing $os..."
    ./test-build.sh $os
done

# 完整测试所有 OS
for os in ubuntu alpine debian rocky amazonlinux openeuler; do
    echo "Testing $os with local registry..."
    ./test-local-registry.sh $os
    sleep 5
done
```

---

## 测试流程示例

### 完整的发布前测试流程

```bash
# 1. 快速验证构建
./test-build.sh ubuntu

# 2. 完整测试推送/拉取
./test-local-registry.sh ubuntu

# 3. 测试关键的操作系统
./test-local-registry.sh alpine
./test-local-registry.sh debian

# 4. 如果都通过，发布到生产
cd ubuntu
PUSH_IMAGE=true ./build.sh 3.5.3 modelengine/

# 5. 验证生产镜像
docker pull modelengine/fit-framework:3.5.3-ubuntu
docker run --rm modelengine/fit-framework:3.5.3-ubuntu fit help
```

---

## 故障排除

### test-build.sh 失败

**问题**: 构建失败
```
❌ 基础镜像构建失败
```

**解决**:
1. 检查 Dockerfile 语法
2. 查看详细错误信息
3. 确认网络连接（下载 FIT Framework）

```bash
# 手动构建查看详细错误
cd ubuntu
docker build -t fit-framework:ubuntu-test .
```

### test-local-registry.sh 失败

**问题 1**: 端口被占用
```
[ERROR] 端口 5001 已被占用
```

**解决**:
```bash
# 使用其他端口
REGISTRY_PORT=5002 ./test-local-registry.sh ubuntu
```

**问题 2**: Registry 启动失败
```
[ERROR] Registry 启动失败
```

**解决**:
```bash
# 清理已存在的 registry 容器
docker stop test-registry
docker rm test-registry

# 重新运行测试
./test-local-registry.sh ubuntu
```

**问题 3**: 推送失败
```
[ERROR] 镜像推送失败
```

**解决**:
```bash
# 检查 registry 是否运行
docker ps | grep registry

# 检查 registry 日志
docker logs test-registry

# 确认 registry 可访问
curl http://localhost:5001/v2/_catalog
```

---

## 清理测试环境

### 清理本地镜像

```bash
# 删除测试镜像
docker rmi fit-framework:ubuntu-test
docker rmi localhost:5001/fit-framework:3.5.3-ubuntu

# 清理所有 fit-framework 镜像
docker images | grep fit-framework | awk '{print $3}' | xargs docker rmi
```

### 停止本地 Registry

```bash
# 停止并删除 registry
docker stop test-registry
docker rm test-registry

# 查看运行中的 registry
docker ps -a | grep registry
```

### 完全清理

```bash
# 清理所有测试资源
docker stop test-registry 2>/dev/null || true
docker rm test-registry 2>/dev/null || true
docker rmi $(docker images -q 'fit-framework:*-test') 2>/dev/null || true
docker rmi $(docker images -q 'localhost:*/fit-framework') 2>/dev/null || true

echo "清理完成"
```

---

## 最佳实践

1. **日常开发**: 使用 `test-build.sh` 快速验证
2. **发布前**: 使用 `test-local-registry.sh` 完整测试
3. **持续集成**: 在 CI/CD 中使用 `test-local-registry.sh`
4. **版本更新**: 测试所有操作系统的镜像
5. **清理环境**: 定期清理测试镜像释放空间

---

## 相关文档

- [BUILD.md](BUILD.md) - 完整的构建和发布指南
- [README.md](README.md) - 基础镜像使用说明
- [Dockerfile](ubuntu/Dockerfile) - 镜像定义示例

---

**最后更新**: 2025-10-14
**维护者**: FIT Framework Team
