# macOS AI 编程沙箱环境

> 基于 Colima + Docker + Git Worktree，将 AI TUI 工具（Claude Code、Codex、OpenCode、Gemini CLI 等）运行在容器内，物理隔离保护宿主机。
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
│  │  ┌────────────────────────────┐     │ │
│  │  │ fit-dev-feat-xxx           │     │ │
│  │  │  claude / codex / opencode │ ← 挂载 feat-xxx worktree
│  │  │  gemini                    │     │ │
│  │  │  java / mvn / python       │     │ │
│  │  └────────────────────────────┘     │ │
│  │                                     │ │
│  │  ┌────────────────────────────┐     │ │
│  │  │ fit-dev-fix-bug-123        │     │ │
│  │  │  claude / codex / opencode │ ← 挂载 fix-bug-123 worktree
│  │  │  gemini                    │     │ │
│  │  │  java / mvn / python       │     │ │
│  │  └────────────────────────────┘     │ │
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

## 平台要求

> **仅支持 macOS**。本方案依赖 [Colima](https://github.com/abiosoft/colima)（macOS 专属的轻量级容器运行时），不支持 Linux / Windows。

- macOS（Intel 或 Apple Silicon）
- [Homebrew](https://brew.sh/)
- Node.js 18+

## 编译构建

```bash
cd docker/sandbox
npm install          # 安装依赖
npm run build        # 使用 esbuild 打包为单文件 dist/sandbox.cjs
```

`npm run build` 通过 esbuild 将 TypeScript 源码和所有依赖打包为单个 `dist/sandbox.cjs` 文件，可独立运行，不依赖 `node_modules/`。

将 `dist/sandbox` 和 `dist/sandbox.cjs` 拷贝到系统 PATH 目录下即可全局使用 `sandbox` 命令。注意需要在 fit-framework 的 git 仓库目录内运行，因为 sandbox 依赖 git worktree 和仓库内的 Dockerfile 来创建容器。

> 开发调试时也可跳过编译，直接通过 `./sandbox.sh` 运行源码（依赖 tsx 即时编译）。

## 快速开始

### 1. 创建沙箱

```bash
# 创建并启动一个工作在 feat-xxx 分支的沙箱
sandbox create feat-xxx

# 基于 main 分支创建沙箱
sandbox create fix-bug-123 main
```

首次运行会自动安装 Colima、构建镜像，耗时几分钟。后续启动很快。

### 2. 进入沙箱

```bash
sandbox exec feat-xxx
```

### 3. 使用 AI 工具

```bash
# 进入容器后，直接使用
claude              # Claude Code（首次需容器内 OAuth 登录）
codex               # OpenAI Codex（实时同步宿主机凭据）
opencode            # OpenCode（实时同步宿主机凭据）
gemini              # Gemini CLI（实时同步宿主机凭据）

# 也可以直接开发
mvn clean install
python3 script.py
```

> **认证差异**：Codex、OpenCode、Gemini CLI 的认证文件通过实时挂载（live mount）与宿主机双向同步，宿主机刷新 token 后容器自动生效；Claude Code 首次需要在容器内完成一次 OAuth 登录（之后免登录）。详见[认证机制说明](#ai-工具认证机制)。

## 多容器并发工作流

```bash
# 1. 同时启动多个沙箱，各自处理不同任务
sandbox create feat-new-api main
sandbox create fix-issue-42 main

# 2. 查看所有运行中的沙箱
sandbox ls

# 3. 分别进入不同沙箱
sandbox exec feat-new-api     # 终端 1
sandbox exec fix-issue-42     # 终端 2

# 4. 各沙箱内的修改互不影响

# 5. 完成后清理
sandbox rm feat-new-api
sandbox rm fix-issue-42
```

## 日常工作流

```bash
# 1. 启动沙箱（如果容器已停止）
docker start fit-dev-feat-xxx

# 2. 进入容器
sandbox exec feat-xxx

# 3. 使用 AI 工具进行开发
claude

# 4. 结束后退出容器（容器继续后台运行）
exit

# 5. 不用时停止容器释放资源
docker stop fit-dev-feat-xxx
```

## 命令行模式（非交互式）

除了打开 TUI 界面，每个 AI 工具都支持直接在命令行传入 prompt 并获取输出，适合脚本自动化、快速提问和管道组合。

### Claude Code

```bash
# 单次提问（-p = print mode，不打开 TUI）
claude -p "解释一下这个项目的架构"

# 管道输入
cat src/main/java/App.java | claude -p "审查这段代码的安全性"

# 指定输出格式（text / json / stream-json）
claude -p "列出所有 TODO" --output-format json

# 指定模型
claude -p --model opus "设计一个缓存方案"

# 限制轮次和预算
claude -p --max-turns 3 --max-budget-usd 1.00 "重构数据库模块"

# 继续上一次对话
claude -c -p "为刚才的修改补充单元测试"

# 跳过所有权限确认（CI/脚本场景）
claude -p --dangerously-skip-permissions "运行所有测试并汇报结果"
```

### Codex

```bash
# 单次提问（exec 子命令 = 非交互模式）
codex exec "为 User 类生成单元测试"

# 管道输入
echo "解释这个报错信息" | codex exec -

# JSON 事件流输出
codex exec --json "列出所有 API 端点"

# 指定模型
codex exec -m o4-mini "优化这个排序算法"

# 全自动模式（无需确认，可写工作区）
codex exec --full-auto "给所有 API 路由添加错误处理"

# 将最终结果写入文件
codex exec -o result.txt "分析项目依赖关系"
```

### OpenCode

```bash
# 单次提问（run 子命令 = 非交互模式）
opencode run "解释 JavaScript 闭包的工作原理"

# 指定模型（格式：provider/model-name）
opencode run -m anthropic/claude-sonnet-4-20250514 "审查这段代码"

# 指定 Agent
opencode run --agent plan "分析项目结构并给出改进建议"

# JSON 输出
opencode run --format json "列出所有 API 端点"

# 附加文件到 prompt
opencode run --file src/auth.ts "审查这个文件的安全性"

# 继续上一次对话
opencode run --continue "为刚才的修改补充单元测试"
```

### Gemini CLI

```bash
# 单次提问（-p = prompt 模式，不打开 TUI）
gemini -p "解释一下 Docker 的工作原理"

# 管道输入
cat src/auth.py | gemini -p "审查这段代码的安全性"

# 指定输出格式（text / json / stream-json）
gemini -p "列出所有 TODO" --output-format json

# 指定模型
gemini -p -m gemini-2.5-flash "快速解释这段代码"

# 自动确认所有工具调用
gemini -p --yolo "运行测试并汇报结果"

# 将项目所有文件纳入上下文
gemini -p --all-files "分析这个项目的架构"
```

### 快速对比

| 功能 | Claude Code | Codex | OpenCode | Gemini CLI |
|------|------------|-------|----------|------------|
| 非交互标志 | `-p` | `exec` 子命令 | `run` 子命令 | `-p` |
| 管道输入 | `cat f \| claude -p` | `echo x \| codex exec -` | `--file` 附加文件 | `cat f \| gemini -p` |
| JSON 输出 | `--output-format json` | `--json` | `--format json` | `--output-format json` |
| 指定模型 | `--model opus` | `-m o4-mini` | `-m provider/model` | `-m gemini-2.5-flash` |
| 跳过确认 | `--dangerously-skip-permissions` | `--full-auto` | — | `--yolo` |

## 沙箱管理

```bash
# 查看所有沙箱状态
sandbox ls

# 停止/启动指定沙箱
docker stop fit-dev-feat-xxx
docker start fit-dev-feat-xxx

# 清理指定沙箱（容器 + worktree + AI 工具配置）
sandbox rm feat-xxx

# 清理所有沙箱
sandbox rm --all

# 强制重建 Docker 镜像
sandbox rebuild
```

## Colima VM 管理

```bash
sandbox vm status              # 查看状态
sandbox vm stop                # 停止（释放 CPU/内存）
sandbox vm start               # 启动
sandbox vm start --cpu 6 --memory 8  # 自定义资源启动
```

## 目录规划

```
~/.fit-worktrees/              # 所有 worktree 统一放这里
├── feat-xxx/                  # 分支 feat-xxx 的独立工作目录
├── fix-bug-123/               # 分支 fix-bug-123 的独立工作目录
└── ...

~/.claude-sandboxes/           # Claude Code 沙箱配置（每分支独立）
├── feat-xxx/                  # → 挂载到容器 /home/devuser/.claude
└── fix-bug-123/

~/.codex-sandboxes/            # Codex 沙箱配置（每分支独立）
├── feat-xxx/                  # → 挂载到容器 /home/devuser/.codex
└── fix-bug-123/

~/.opencode-sandboxes/         # OpenCode 沙箱配置（每分支独立）
├── feat-xxx/                  # → 挂载到容器 /home/devuser/.local/share/opencode
└── fix-bug-123/

~/.gemini-sandboxes/           # Gemini CLI 沙箱配置（每分支独立）
├── feat-xxx/                  # → 挂载到容器 /home/devuser/.gemini
└── fix-bug-123/

主仓库: ~/projects/.../fit-framework/  （不变，不被容器挂载）
```

选择 `~/.fit-worktrees/` 的原因：
- 不污染主仓库
- 路径简短清晰
- 不在项目目录内，天然被 `.gitignore` 排除

每个沙箱拥有独立的 AI 工具配置目录（如 `~/.codex-sandboxes/{branch}/`），避免并发冲突和会话污染。Codex、OpenCode、Gemini CLI 的认证文件通过实时挂载（live mount）与宿主机双向同步，宿主机刷新 token 后容器自动生效；Claude Code 首次需在容器内 OAuth 登录一次。

> 详细的认证机制说明、注册表字段参考和添加新工具指南请参阅 [DEVELOPMENT.md](DEVELOPMENT.md)。

## 高级配置

### 调整 VM 资源

```bash
sandbox create --cpu 6 --memory 8 feat-xxx
```

### 重建镜像（如需更新 AI 工具版本）

```bash
sandbox rebuild
```

### 容器内安装额外工具

```bash
docker exec -it fit-dev-feat-xxx bash

# 示例：安装 aider（额外 AI 工具）
pip install -U aider-chat

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
| 分支名含 `/`    | 自动将 `/` 替换为 `..`，如 `feat/xxx` → 容器名 `fit-dev-feat..xxx`      |

## 文件说明

```
docker/sandbox/
├── README.md                          # 用户指南（使用、管理、故障排查）
├── DEVELOPMENT.md                     # 开发者指南（注册表、认证机制、添加新工具）
├── sandbox.sh                         # CLI 入口（thin wrapper）
├── Dockerfile.runtime-only            # 镜像定义（运行时 + AI 工具）
├── package.json                       # Node.js 依赖（commander + clack）
├── tsconfig.json
└── src/
    ├── cli.ts                         # Commander 子命令分发
    ├── constants.ts                   # 共享常量 + 工具函数
    ├── tools.ts                       # AI 工具注册表（声明式，新增工具只改这里）
    ├── shell.ts                       # 安全的命令执行封装
    └── commands/                      # 各子命令实现
        ├── create.ts
        ├── rm.ts
        ├── ls.ts
        ├── enter.ts
        ├── vm.ts
        └── rebuild.ts
```
