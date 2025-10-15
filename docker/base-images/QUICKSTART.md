# FIT Framework Docker 快速开始

本文档提供最简单、最直观的测试流程，帮助你快速验证 FIT Framework Docker 基础镜像。

## 🎯 测试目标

完整的端到端流程：

```
构建基础镜像 → 推送到本地仓库 → 启动运行 → 访问验证
```

## ⚡ 一键测试

### 前置要求

- Docker Desktop 正在运行
- 有网络连接（下载 FIT Framework）

### 快速开始

```bash
# 1. 进入目录
cd docker/base-images

# 2. 运行端到端测试
./test-e2e.sh ubuntu

# 等待约 3-5 分钟，脚本会自动完成所有步骤
```

### 测试流程

脚本会自动执行以下步骤：

**步骤 1**: 启动本地 Docker Registry（端口 5001）
```
✓ 本地镜像仓库启动在 localhost:5001
```

**步骤 2**: 构建 FIT 基础镜像
```
✓ 构建 fit-framework:ubuntu 镜像（约 1GB）
✓ 包含 FIT Framework 3.5.3
```

**步骤 3**: 推送到本地仓库
```
✓ 推送镜像到 localhost:5001/fit-framework:ubuntu
```

**步骤 4**: 启动基础镜像容器
```
✓ 从本地仓库拉取镜像
✓ 启动容器 fit-e2e-app
✓ 映射端口 8080:8080
✓ 使用基础镜像的默认配置
```

**步骤 5**: 验证基础镜像功能
```
✓ 容器状态检查
✓ 健康检查
✓ HTTP 服务访问
✓ 日志查看
```

### 测试成功标志

当你看到以下输出时，表示测试成功：

```
==============================================
✅ 端到端测试完成！
==============================================

📊 测试摘要:
  • 基础镜像: fit-framework:ubuntu (3.5.3)
  • 本地仓库: localhost:5001
  • 运行镜像: localhost:5001/fit-framework:ubuntu
  • 容器名称: fit-e2e-app
  • 访问地址: http://localhost:8080
```

---

## 🔍 查看和验证

### 1. 查看 Docker Desktop

打开 Docker Desktop，你可以看到：

**Images（镜像）**:
- `fit-framework:ubuntu` - 基础镜像
- `localhost:5001/fit-framework:ubuntu` - 推送到仓库的镜像

**Containers（容器）**:
- `fit-e2e-app` - 正在运行的示例应用
- `test-registry` - 本地镜像仓库

### 2. 使用命令行验证

```bash
# 查看所有 FIT 相关镜像
docker images | grep fit

# 查看运行的容器
docker ps | grep fit

# 查看本地仓库内容
curl http://localhost:5001/v2/_catalog | jq
# 输出: {"repositories":["fit-framework"]}

# 查看 fit-framework 的标签
curl http://localhost:5001/v2/fit-framework/tags/list | jq
# 输出: {"name":"fit-framework","tags":["3.5.3-ubuntu","ubuntu"]}

# 查看容器日志
docker logs fit-e2e-app

# 访问 actuator 端点
curl http://localhost:8080/actuator/plugins

# 查看 FIT Framework 版本
docker exec fit-e2e-app fit version
```

### 3. 进入容器查看

```bash
# 进入容器
docker exec -it fit-e2e-app bash

# 在容器内查看 FIT 安装
ls -la /opt/fit-framework/java/

# 查看配置
cat /opt/fit-framework/java/conf/fitframework.yml

# 退出容器
exit
```

---

## 🧹 清理环境

测试完成后，清理资源：

```bash
# 方式 1: 快速清理（推荐）
docker stop fit-e2e-app test-registry
docker rm fit-e2e-app test-registry

# 方式 2: 完全清理（包括镜像）
docker stop fit-e2e-app test-registry
docker rm fit-e2e-app test-registry
docker rmi localhost:5001/fit-framework:ubuntu
docker rmi fit-framework:ubuntu

# 方式 3: 使用脚本清理
# 在 test-e2e.sh 中按 Ctrl+C 会自动清理容器和仓库
```

---

## 🎨 测试其他操作系统

```bash
# 测试 Alpine（轻量级）
./test-e2e.sh alpine

# 测试 Debian（稳定）
./test-e2e.sh debian

# 测试 Rocky Linux（企业级）
./test-e2e.sh rocky

# 测试 Amazon Linux（AWS 优化）
./test-e2e.sh amazonlinux

# 测试 OpenEuler（国产化）
./test-e2e.sh openeuler
```

---

## ⚙️ 自定义配置

### 使用不同的端口

如果 5001 端口被占用：

```bash
REGISTRY_PORT=5002 ./test-e2e.sh ubuntu
```

### 使用不同的版本

```bash
FIT_VERSION=3.5.4 ./test-e2e.sh ubuntu
```

### 组合使用

```bash
REGISTRY_PORT=5002 FIT_VERSION=3.5.4 ./test-e2e.sh alpine
```

---

## ❓ 常见问题

### Q1: 端口 5001 被占用怎么办？

```bash
# 检查端口占用
lsof -i :5001

# 使用其他端口
REGISTRY_PORT=5002 ./test-e2e.sh ubuntu
```

### Q2: 镜像构建很慢？

这是正常的，原因：
- 首次需要下载基础镜像（Ubuntu ~70MB）
- 下载 FIT Framework（~40MB）
- 安装 OpenJDK 17

后续构建会使用缓存，速度会快很多。

### Q3: 应用启动失败？

查看日志：
```bash
docker logs fit-e2e-app
```

常见原因：
- 端口 8080 被占用
- FIT Framework 配置问题
- 内存不足

### Q4: 如何完全重新测试？

```bash
# 清理所有相关资源
docker stop fit-e2e-app test-registry 2>/dev/null || true
docker rm fit-e2e-app test-registry 2>/dev/null || true
docker rmi fit-demo-app:3.5.3 2>/dev/null || true
docker rmi fit-framework:ubuntu 2>/dev/null || true

# 重新运行测试
./test-e2e.sh ubuntu
```

---

## 📚 下一步

测试成功后，你可以：

1. **查看详细文档**
   - [BUILD.md](BUILD.md) - 完整构建指南
   - [TESTING.md](TESTING.md) - 测试说明
   - [README.md](README.md) - 使用指南

2. **构建自己的应用**
   - 基于 `fit-framework:ubuntu` 创建你的应用镜像
   - 添加你的插件和配置文件
   - 示例 Dockerfile:
     ```dockerfile
     FROM localhost:5001/fit-framework:ubuntu
     USER root
     COPY my-plugins/ /opt/fit-framework/java/plugins/
     COPY my-config.yml /opt/fit-framework/java/conf/fitframework.yml
     USER fit
     CMD ["fit", "start"]
     ```

3. **发布到生产仓库**
   ```bash
   # 登录 Docker Hub
   docker login

   # 构建并推送
   cd ubuntu
   PUSH_IMAGE=true ./build.sh 3.5.3 yourusername/
   ```

4. **批量构建所有镜像**
   ```bash
   # 构建所有 6 个操作系统的镜像
   ./build_all.sh build

   # 推送到仓库
   PUSH_IMAGE=true ./build_all.sh build 3.5.3 registry.example.com/
   ```

---

## 🆘 获取帮助

- **查看脚本帮助**: `./test-e2e.sh --help`
- **GitHub Issues**: https://github.com/ModelEngine-Group/fit-framework/issues
- **查看日志**: `docker logs fit-e2e-app`

---

**享受 FIT Framework 的 Docker 化部署吧！** 🚀
