# macOS AI 编程沙箱环境

> 基于 Colima + Docker + Git Worktree，将 AI TUI 工具（Claude Code、Codex 等）运行在容器内，物理隔离保护宿主机。
> 支持多容器并发，每个容器工作在独立分支上互不干扰。

## 架构

```
┌──────────────────────────────────────────┐
│  macOS 宿主机                             │
│                                          │
│  ~/.fit-worktrees/                       │
│  ├── feat-xxx/      (git worktree)       │
│  ├── fix-bug-123/   (git worktree)       │
│  └── ...                                 │
│                                          │
│  ┌─────────────────────────────────────┐ │
│  │ Colima VM                           │ │
│  │                                     │ │
│  │  ┌─────────────────────┐            │ │
│  │  │ fit-dev-feat-xxx    │            │ │
│  │  │  claude / codex     │  ← 挂载 feat-xxx worktree
│  │  │  java / mvn / python│            │ │
│  │  └─────────────────────┘            │ │
│  │                                     │ │
│  │  ┌─────────────────────┐            │ │
│  │  │ fit-dev-fix-bug-123 │            │ │
│  │  │  claude / codex     │  ← 挂载 fix-bug-123 worktree
│  │  │  java / mvn / python│            │ │
│  │  └─────────────────────┘            │ │
│  │                                     │ │
│  └─────────────────────────────────────┘ │
│                                          │
│  主仓库: ~/.../fit-framework              │
│  （不要求被容器挂载，保持干净）               │
└──────────────────────────────────────────┘
```

- 每个沙箱容器挂载独立的 git worktree，工作在不同分支上
- 容器之间完全隔离，文件修改互不影响
- 即使 AI 执行了破坏性命令，影响范围仅限该容器的 worktree
- SSH 密钥只读挂载，支持 git 操作
- 多容器共享同一个 Colima VM 和 Docker 镜像

## 前置条件

- macOS（Intel 或 Apple Silicon）
- [Homebrew](https://brew.sh/)
## 快速开始

### 1. 启动沙箱

```bash
cd docker/sandbox

# 创建并启动一个工作在 feat-xxx 分支的沙箱
./sandbox.sh create feat-xxx

# 基于 main 分支创建沙箱
./sandbox.sh create fix-bug-123 main
```

首次运行会自动安装 Colima、构建镜像，耗时几分钟。后续启动很快。

### 2. 进入沙箱

```bash
./sandbox.sh exec feat-xxx
```

### 3. 使用 AI 工具

```bash
# 进入容器后，直接使用
claude              # Claude Code
codex               # OpenAI Codex

# 也可以直接开发
mvn clean install
python3 script.py
```

## 多容器并发工作流

```bash
# 1. 同时启动多个沙箱，各自处理不同任务
./sandbox.sh create feat-new-api main
./sandbox.sh create fix-issue-42 main

# 2. 查看所有运行中的沙箱
./sandbox.sh ls

# 3. 分别进入不同沙箱
./sandbox.sh exec feat-new-api     # 终端 1
./sandbox.sh exec fix-issue-42     # 终端 2

# 4. 各沙箱内的修改互不影响

# 5. 完成后清理
./sandbox.sh rm feat-new-api
./sandbox.sh rm fix-issue-42
```

## 日常工作流

```bash
# 1. 启动沙箱（如果容器已停止）
docker start fit-dev-feat-xxx

# 2. 进入容器
./sandbox.sh exec feat-xxx

# 3. 使用 AI 工具进行开发
claude

# 4. 结束后退出容器（容器继续后台运行）
exit

# 5. 不用时停止容器释放资源
docker stop fit-dev-feat-xxx
```

## 沙箱管理

```bash
# 查看所有沙箱状态
./sandbox.sh ls

# 停止/启动指定沙箱
docker stop fit-dev-feat-xxx
docker start fit-dev-feat-xxx

# 清理指定沙箱（容器 + worktree + Claude 配置）
./sandbox.sh rm feat-xxx

# 清理所有沙箱
./sandbox.sh rm --all

# 强制重建 Docker 镜像
./sandbox.sh rebuild
```

## Colima VM 管理

```bash
./sandbox.sh vm status              # 查看状态
./sandbox.sh vm stop                # 停止（释放 CPU/内存）
./sandbox.sh vm start               # 启动
./sandbox.sh vm start --cpu 6 --memory 8  # 自定义资源启动
```

## Worktree 目录规划

```
~/.fit-worktrees/              # 所有 worktree 统一放这里
├── feat-xxx/                  # 分支 feat-xxx 的独立工作目录
├── fix-bug-123/               # 分支 fix-bug-123 的独立工作目录
└── ...

主仓库: ~/projects/.../fit-framework/  （不变，不被容器挂载）
```

选择 `~/.fit-worktrees/` 的原因：
- 不污染主仓库
- 路径简短清晰
- 不在项目目录内，天然被 `.gitignore` 排除

## 高级配置

### 调整 VM 资源

```bash
./sandbox.sh create --cpu 6 --memory 8 feat-xxx
```

### 重建镜像（如需更新 AI 工具版本）

```bash
./sandbox.sh rebuild
```

### 容器内安装额外工具

```bash
docker exec -it fit-dev-feat-xxx bash

# 示例：安装 opencode
go install github.com/opencode-ai/opencode@latest

# 示例：配置 Maven 镜像加速
mkdir -p ~/.m2 && cat > ~/.m2/settings.xml <<'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF
```

## 故障排查

| 问题          | 解决方法                                                         |
|-------------|--------------------------------------------------------------|
| 容器无法启动      | `colima status` 检查 VM，`colima stop && colima start` 重启       |
| 文件权限问题      | 删除镜像重新构建，确保 UID 映射正确                                         |
| AI 工具找不到    | 进入容器后用 `bash -l` 确保加载环境变量                                    |
| 磁盘不足        | `docker system prune -a` 清理                                  |
| Worktree 冲突 | `cd 主仓库 && git worktree list` 查看，`git worktree prune` 清理失效记录 |
| 分支名含 `/`    | 自动替换为 `-`，如 `feat/xxx` → 容器名 `fit-dev-feat-xxx`              |

## 文件说明

```
docker/sandbox/
├── README.md                          # 本文件
├── sandbox.sh                         # CLI 入口（thin wrapper）
├── Dockerfile.runtime-only            # 镜像定义（运行时 + AI 工具）
├── package.json                       # Node.js 依赖（commander + clack）
├── tsconfig.json
└── src/
    ├── cli.ts                         # Commander 子命令分发
    ├── constants.ts                   # 共享常量 + 工具函数
    ├── shell.ts                       # 安全的命令执行封装
    └── commands/                      # 各子命令实现
        ├── create.ts
        ├── rm.ts
        ├── ls.ts
        ├── enter.ts
        ├── vm.ts
        └── rebuild.ts
```
