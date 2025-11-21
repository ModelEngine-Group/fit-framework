# FIT Framework Docker 基础镜像

FIT Framework 官方 Docker 基础镜像，提供多种操作系统选择，让您可以快速构建基于 FIT 的 AI 应用。

## 🚀 快速开始

### 选择合适的基础镜像

我们提供 2 种操作系统的基础镜像，您可以根据需求选择：

| 镜像标签                        | 操作系统                | 特点         | 推荐场景         |
|-----------------------------|---------------------|------------|--------------|
| `fit-framework:alpine`      | Alpine Linux        | 轻量级，安全性高   | 云原生、微服务、生产环境 |
| `fit-framework:debian`      | Debian 12           | 稳定可靠，长期支持  | 稳定性要求高的场景    |

### 基本使用

```bash
# 拉取镜像（以Alpine为例）
docker pull fit-framework:alpine

# 启动容器
docker run -d --name fit-server \
  -p 8080:8080 \
  fit-framework:alpine

# 查看日志
docker logs fit-server

# 进入容器
docker exec -it fit-server bash
```

### 环境变量配置

```bash
# 自定义配置启动
docker run -d --name fit-server \
  -p 8080:8080 \
  -e FIT_REGISTRY_HOST=registry.example.com \
  -e FIT_REGISTRY_PORT=8080 \
  -e FIT_WORKER_ID=my-worker-001 \
  -e FIT_LOG_LEVEL=debug \
  -e JAVA_OPTS="-Xms512m -Xmx2048m" \
  fit-framework:alpine
```

### 挂载目录

```bash
# 挂载插件和数据目录
docker run -d --name fit-server \
  -p 8080:8080 \
  -v $(pwd)/plugins:/opt/fit-framework/java/dynamic-plugins \
  -v $(pwd)/logs:/opt/fit-framework/java/logs \
  -v $(pwd)/data:/opt/fit-framework/java/data \
  -v $(pwd)/conf:/opt/fit-framework/java/conf \
  fit-framework:alpine
```

## 🏗️ 基于基础镜像构建应用

### 示例1：简单AI应用

```dockerfile
# 选择适合的基础镜像
FROM fit-framework:alpine

# 复制应用插件
COPY --chown=fit:fit my-ai-plugins/ /opt/fit-framework/java/plugins/

# 复制应用配置
COPY --chown=fit:fit app-config.yml /opt/fit-framework/java/conf/fitframework.yml

# 设置应用环境变量
ENV FIT_WORKER_ID=my-ai-app
ENV APP_NAME=intelligent-chat

# 暴露应用端口
EXPOSE 8090

CMD ["fit", "start"]
```

### 示例2：多阶段构建

```dockerfile
# 构建阶段
FROM maven:3.9-openjdk-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段 - 使用FIT基础镜像
FROM fit-framework:alpine

# 复制构建产物
COPY --from=builder --chown=fit:fit /app/target/*.jar /opt/fit-framework/java/plugins/

# 复制配置文件
COPY --chown=fit:fit config/ /opt/fit-framework/java/conf/

# 设置应用信息
ENV FIT_WORKER_ID=my-service
ENV FIT_HTTP_PORT=8080

EXPOSE 8080
CMD ["fit", "start"]
```

### 示例3：Python + Java 混合应用

```dockerfile
FROM fit-framework:alpine

# 切换到root安装Python依赖
USER root

# 安装Python环境
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-venv \
    && rm -rf /var/lib/apt/lists/*

# 创建Python虚拟环境
RUN python3 -m venv /opt/ai-env
ENV PATH="/opt/ai-env/bin:$PATH"

# 安装Python依赖
COPY requirements.txt /tmp/
RUN pip install --no-cache-dir -r /tmp/requirements.txt

# 复制应用文件
COPY --chown=fit:fit java-plugins/ /opt/fit-framework/java/plugins/
COPY --chown=fit:fit python-scripts/ /opt/ai-scripts/
COPY --chown=fit:fit config/ /opt/fit-framework/java/conf/

# 切换回fit用户
USER fit

# 设置环境变量
ENV AI_SCRIPTS_PATH=/opt/ai-scripts
ENV PYTHON_PATH=/opt/ai-env/bin/python

CMD ["fit", "start"]
```

## 📋 配置说明

### 环境变量

| 变量名                 | 默认值                   | 说明                           |
|---------------------|-----------------------|------------------------------|
| `FIT_REGISTRY_HOST` | localhost             | 注册中心主机地址                     |
| `FIT_REGISTRY_PORT` | 8080                  | 注册中心端口                       |
| `FIT_WORKER_ID`     | fit-worker-{hostname} | 工作节点唯一标识                     |
| `FIT_HTTP_PORT`     | 8080                  | HTTP服务端口                     |
| `FIT_LOG_LEVEL`     | info                  | 日志级别 (debug/info/warn/error) |
| `JAVA_OPTS`         | -Xms256m -Xmx1024m    | JVM启动参数                      |

### 目录结构

```
/opt/fit-framework/java/          # FIT框架主目录
├── bin/                          # 可执行文件
│   └── fit                       # FIT主命令
├── conf/                         # 配置文件目录
│   ├── fitframework.yml          # 主配置文件
│   └── log4j2.xml                # 日志配置文件
├── lib/                          # 框架核心库
├── plugins/                      # 插件目录
├── dynamic-plugins/              # 动态插件目录 (推荐挂载)
├── logs/                         # 日志目录 (推荐挂载)
└── data/                         # 数据目录 (推荐挂载)
```

### 端口说明

| 端口   | 用途          | 协议   |
|------|-------------|------|
| 8080 | FIT框架HTTP服务 | HTTP |
| 8090 | 业务应用端口      | HTTP |

## 🐳 Docker Compose 示例

### 基础开发环境

```yaml
version: '3.8'

services:
  fit-framework:
    image: fit-framework:alpine
    ports:
      - "8080:8080"
    environment:
      - FIT_WORKER_ID=dev-worker
      - FIT_LOG_LEVEL=debug
    volumes:
      - ./plugins:/opt/fit-framework/java/dynamic-plugins
      - ./logs:/opt/fit-framework/java/logs
    restart: unless-stopped
```

### 生产环境集群

```yaml
version: '3.8'

services:
  # 注册中心
  registry:
    image: fit-registry:latest
    ports:
      - "8081:8080"
    restart: unless-stopped

  # FIT工作节点1
  fit-worker-1:
    image: fit-framework:alpine
    ports:
      - "8080:8080"
    environment:
      - FIT_REGISTRY_HOST=registry
      - FIT_WORKER_ID=worker-001
      - JAVA_OPTS=-Xms512m -Xmx2048m
    volumes:
      - ./plugins:/opt/fit-framework/java/dynamic-plugins
      - ./logs/worker1:/opt/fit-framework/java/logs
    depends_on:
      - registry
    restart: unless-stopped

  # FIT工作节点2
  fit-worker-2:
    image: fit-framework:alpine
    ports:
      - "8082:8080"
    environment:
      - FIT_REGISTRY_HOST=registry
      - FIT_WORKER_ID=worker-002
      - JAVA_OPTS=-Xms512m -Xmx2048m
    volumes:
      - ./plugins:/opt/fit-framework/java/dynamic-plugins
      - ./logs/worker2:/opt/fit-framework/java/logs
    depends_on:
      - registry
    restart: unless-stopped

  # 数据库
  database:
    image: postgres:15
    environment:
      POSTGRES_DB: fitdb
      POSTGRES_USER: fit
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data:
```

## 🛠️ 构建自定义镜像

### 从源码构建

如果您需要自定义FIT Framework版本或添加特定配置：

```bash
# 克隆项目
git clone https://github.com/ModelEngine-Group/fit-framework.git
cd fit-framework/docker/base-images

# 构建特定操作系统镜像
cd alpine
./build.sh ${fit-version}

# 或批量构建所有镜像
./build-all.sh ${fit-version}

# 推送到私有仓库
PUSH_IMAGE=true ./build-all.sh ${fit-version} registry.mycompany.com/
```

### 自定义配置

您可以通过以下方式自定义镜像：

1. **修改默认配置**：编辑各OS目录中的Dockerfile
2. **添加系统依赖**：在RUN指令中安装额外软件包
3. **预装应用插件**：将插件复制到镜像中
4. **设置默认环境变量**：在Dockerfile中添加ENV指令

## 🧪 测试镜像

### 一键端到端测试

我们提供了完整的端到端测试脚本，自动完成：构建镜像 → 推送到本地仓库 → 启动运行 → 验证功能

```bash
# 查看帮助信息
./test-e2e.sh --help

# 运行端到端测试（默认测试 Alpine）
./test-e2e.sh

# 测试其他操作系统
./test-e2e.sh alpine
./test-e2e.sh debian
```

测试流程包括：
1. 自动启动本地 Docker Registry（端口 15000，自动检测冲突）
2. 构建 FIT Framework 基础镜像
3. 推送镜像到本地仓库
4. 启动容器（使用基础镜像的默认配置）
5. 验证功能（健康检查、插件加载、HTTP 服务）

### 自定义测试配置

```bash
# 使用不同的 Registry 端口
REGISTRY_PORT=20000 ./test-e2e.sh alpine

# 使用不同的 FIT 版本
FIT_VERSION=${fit-version} ./test-e2e.sh alpine

# 组合使用
REGISTRY_PORT=20000 FIT_VERSION=${fit-version} ./test-e2e.sh alpine
```

### 测试成功标志

测试完成后会显示：

```
==============================================
✅ 端到端测试完成！
==============================================

📊 测试摘要:
  • 基础镜像: fit-framework:ubuntu (${fit-version})
  • 本地仓库: localhost:15000
  • 运行镜像: localhost:15000/fit-framework:ubuntu
  • 容器名称: fit-e2e-app
  • 访问地址: http://localhost:8080
```

### 查看测试资源

```bash
# 查看所有镜像
docker images | grep fit

# 查看本地仓库内容
curl http://localhost:15000/v2/_catalog | jq

# 查看运行的容器
docker ps | grep fit

# 查看容器日志
docker logs fit-e2e-app

# 访问 actuator 端点
curl http://localhost:8080/actuator/plugins
```

### 清理测试环境

**自动清理**: 测试脚本会在退出时自动清理测试镜像和容器，包括：
- 停止并删除 `fit-e2e-app` 容器
- 停止并删除 `test-registry` 容器
- 删除所有测试镜像（`fit-framework:*` 和 `localhost:15000/fit-framework:*`）
- 清理悬空镜像（`<none>:<none>`，由重复构建产生）

**手动清理** (仅在需要时使用):

```bash
# 清理所有测试资源
docker stop fit-e2e-app test-registry 2>/dev/null
docker rm fit-e2e-app test-registry 2>/dev/null
docker rmi localhost:15000/fit-framework:ubuntu 2>/dev/null
docker rmi fit-framework:ubuntu 2>/dev/null

# 清理 registry:2 基础镜像（可选，通常保留以复用）
docker rmi registry:2
```

**注意**:
- `registry:2` 镜像不会被自动清理，可以复用于后续测试
- 如需完全清理，请手动删除 `registry:2` 镜像

## 🔧 故障排除

### 常见问题

**1. 容器启动失败**
```bash
# 查看详细日志
docker logs fit-server

# 以交互模式启动检查
docker run -it --rm fit-framework:ubuntu bash
```

**2. 端口冲突**
```bash
# 使用不同端口
docker run -d -p 8081:8080 fit-framework:ubuntu
```

**3. 内存不足**
```bash
# 调整JVM内存设置
docker run -d -e JAVA_OPTS="-Xms128m -Xmx512m" fit-framework:ubuntu
```

**4. 权限问题**
```bash
# 检查挂载目录权限
sudo chown -R 2000:2000 ./plugins ./logs ./data
```

### 健康检查

所有镜像都内置健康检查：

```bash
# 查看健康状态
docker inspect --format='{{.State.Health.Status}}' fit-server

# 手动执行健康检查
docker exec fit-server healthcheck.sh
```

### 调试模式

```bash
# 启用调试日志
docker run -d -e FIT_LOG_LEVEL=debug fit-framework:ubuntu

# 进入容器调试
docker exec -it fit-server bash

# 查看FIT进程状态
docker exec fit-server ps aux | grep fit
```

## 📚 更多资源

- [FIT Framework 官方文档](../../docs/)
- [GitHub Issues](https://github.com/ModelEngine-Group/fit-framework/issues)
- [示例项目](../examples/)
- [社区讨论](https://github.com/ModelEngine-Group/fit-framework/discussions)

## 🤝 贡献

欢迎提交Issue或Pull Request来改进Docker镜像：

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 发起 Pull Request

## 📄 许可证

本项目采用 MIT 许可证，详见 [LICENSE](../../LICENSE) 文件。
