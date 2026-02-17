# 沙箱开发者指南

> 本文档面向维护者和开发者，说明 AI 工具注册表的设计原理和扩展方法。使用指南请参阅 [README.md](README.md)。

## 为什么每个沙箱使用独立的 AI 工具配置？

Claude Code、Codex、OpenCode、Gemini CLI 都在各自的配置目录（`~/.claude/`、`~/.codex/`、`~/.local/share/opencode/`、`~/.gemini/`）中存储会话历史、项目记忆、锁文件等状态数据。如果多个沙箱容器共享同一个配置目录，会导致：

1. **并发写入冲突** — 多个容器同时写入 `history.jsonl`、`session-env/` 等文件会导致数据竞争和文件损坏
2. **会话/记忆交叉污染** — 不同分支的项目上下文和会话历史会互相干扰
3. **清理困难** — `sandbox rm` 无法从共享目录中安全地只删除某个沙箱的数据
4. **宿主机风险** — 容器内的破坏性操作可能损坏宿主机的凭据和配置

因此每个沙箱拥有独立的配置目录（`~/.{tool}-sandboxes/{branch}/`），实现完全隔离。

## AI 工具认证机制

各 AI 工具在宿主机上使用不同的凭据存储方式，导致沙箱内的认证体验有所差异：

| | 宿主机凭据存储 | 沙箱认证方式 | 首次使用 |
|---|---|---|---|
| **Codex** | 文件（`~/.codex/auth.json`） | 实时挂载（live mount）宿主机 `auth.json` | 无需登录，宿主机刷新后自动生效 |
| **OpenCode** | 文件（`~/.local/share/opencode/auth.json`） | 实时挂载（live mount）宿主机 `auth.json` | 无需登录，宿主机刷新后自动生效 |
| **Gemini CLI** | 文件（`~/.gemini/oauth_creds.json`） | 实时挂载 `oauth_creds.json` + 预植入 `settings.json` | 无需登录，宿主机刷新后自动生效 |
| **Claude Code** | macOS Keychain（`Claude Code-credentials`） | 容器内 OAuth 登录，凭据存入 `.credentials.json` | 需在容器内登录一次 |

### 为什么 Claude Code 不能预植入？

Claude Code 在 macOS 上将 OAuth token 存储在系统 Keychain 中，宿主机的 `~/.claude/` 目录内没有凭据文件。Docker 容器无法访问 macOS Keychain，因此 Claude Code 在容器内会回退到基于文件的凭据存储（`~/.claude/.credentials.json`），需要首次在容器内完成 OAuth 登录。

登录后凭据持久化在 `~/.claude-sandboxes/{branch}/` 中，后续使用**无需再次登录**。

### Codex / OpenCode / Gemini CLI 为什么可以？

Codex、OpenCode 和 Gemini CLI 始终使用文件存储凭据（分别为 `~/.codex/auth.json`、`~/.local/share/opencode/auth.json` 和 `~/.gemini/oauth_creds.json`）。这些认证文件通过 Docker bind mount（`hostLiveMounts`）直接从宿主机实时挂载到容器内，宿主机刷新 token 后容器自动生效，无需重建沙箱。

> **为什么使用实时挂载而非复制？** OAuth token 通常有过期时间（如 OpenAI token 有效期约 7 天），一次性复制的 token 过期后需要手动重新同步。实时挂载使宿主机和容器始终共享同一份文件，彻底消除 token 过期问题。

Gemini CLI 还会额外预植入（一次性复制）`settings.json` 和 `google_accounts.json`，确保容器内的模型选项和用户设置与宿主机一致。这些配置文件不含过期 token，无需实时同步。

## AI 工具注册表

AI 工具的安装与运行配置以 `src/tools.ts` 中的 `AI_TOOLS` 注册表为唯一来源：

- `sandbox create` / `sandbox rebuild` 自动把注册表中的 `npmPackage` 列表作为 `AI_TOOL_PACKAGES` 传给 Docker build
- `sandbox create` 把注册表中的 `envVars` 作为 `docker run -e` 注入容器
- `Dockerfile.runtime-only` 不需要硬编码工具包名，只消费 `AI_TOOL_PACKAGES`

### 字段参考

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `name` | `string` | 是 | 显示名称，如 `"Claude Code"` |
| `npmPackage` | `string` | 是 | npm 包名，用于 `npm install -g` |
| `sandboxBase` | `string` | 是 | 宿主机上的沙箱配置根目录，如 `~/.codex-sandboxes` |
| `containerMount` | `string` | 是 | 容器内挂载路径（绝对路径），如 `/home/devuser/.codex` |
| `versionCmd` | `string` | 是 | 验证安装的命令，通过 `bash -lc` 执行 |
| `noAuthHint` | `string` | 是 | 未预植入认证时的提示信息 |
| `hostAuthFile` | `string` | 否 | 宿主机认证文件路径，与 `authFileName` 成对使用（一次性复制） |
| `authFileName` | `string` | 否 | 沙箱内认证文件名（相对于 `sandboxBase/{branch}/`） |
| `hostPreSeedFiles` | `Array<{hostPath, sandboxName}>` | 否 | 额外需要预植入的宿主机文件（如设置、账户信息） |
| `hostPreSeedDirs` | `Array<{hostDir, sandboxSubdir}>` | 否 | 递归复制宿主机目录到沙箱（如插件目录） |
| `pathRewriteFiles` | `string[]` | 否 | 预植入后需要路径重写的文件（宿主机路径 → 容器路径） |
| `hostLiveMounts` | `Array<{hostPath, containerSubpath}>` | 否 | 实时挂载宿主机文件到容器（双向同步，用于认证 token） |
| `postSetupCmds` | `string[]` | 否 | 容器启动后执行的 shell 命令（如创建符号链接） |
| `envVars` | `Record<string, string>` | 否 | 注入容器的额外环境变量 |

**校验规则**（`validateTools` 启动时检查）：
- `name` 不能重复
- `npmPackage` 不能为空
- `containerMount` 必须是绝对路径
- `hostAuthFile` 和 `authFileName` 必须同时存在或同时缺省

### `sandbox create` 执行流程

```
1. 创建沙箱配置目录           sandboxBase/{branch}/
2. 预植入认证文件（一次性）     hostAuthFile → sandboxBase/{branch}/authFileName
3. 预植入额外配置文件          hostPreSeedFiles[].hostPath → sandboxBase/{branch}/sandboxName
4. 递归复制宿主机目录          hostPreSeedDirs[].hostDir → sandboxBase/{branch}/sandboxSubdir
5. 路径重写                  pathRewriteFiles[] 中的宿主机路径 → 容器路径
6. 挂载配置目录到容器          sandboxBase/{branch}/ → containerMount
7. 实时挂载认证文件            hostLiveMounts[].hostPath → containerMount/containerSubpath
8. 注入环境变量               envVars → docker run -e
9. 容器启动后执行命令          postSetupCmds → docker exec bash -lc
10. 验证安装                 versionCmd → docker exec bash -lc
```

- **一次性操作**（步骤 2–5）遵循"仅首次"策略：宿主机文件存在且沙箱中不存在时才复制，不会覆盖已有配置。
- **实时挂载**（步骤 7）通过 Docker bind mount 将宿主机文件直接映射到容器内，文件始终保持同步，无需重建沙箱。适用于会过期的认证 token。

## 添加新工具

以 Gemini CLI 为例，说明添加一个新工具的完整步骤：

### 步骤 1：在注册表中追加描述符

编辑 `src/tools.ts`，在 `AI_TOOLS` 数组末尾追加：

```typescript
{
  name: 'Gemini CLI',
  npmPackage: '@google/gemini-cli',
  sandboxBase: path.join(HOME, '.gemini-sandboxes'),
  containerMount: '/home/devuser/.gemini',
  versionCmd: 'gemini --version',
  noAuthHint: '首次使用需在容器内运行 gemini 完成认证。',
  // 认证文件实时挂载（token 会过期，需与宿主机保持同步）
  hostLiveMounts: [
    { hostPath: path.join(HOME, '.gemini', 'oauth_creds.json'), containerSubpath: 'oauth_creds.json' },
  ],
  // 配置文件一次性预植入（不含过期 token，无需实时同步）
  hostPreSeedFiles: [
    { hostPath: path.join(HOME, '.gemini', 'settings.json'), sandboxName: 'settings.json' },
    { hostPath: path.join(HOME, '.gemini', 'google_accounts.json'), sandboxName: 'google_accounts.json' },
  ],
},
```

### 步骤 2：编译验证

```bash
cd docker/sandbox
npm run build    # 确认无语法错误
```

### 步骤 3：重建镜像 + 重建沙箱

```bash
sandbox rebuild                  # 重建镜像（安装新工具的 npm 包）
sandbox rm <branch>              # 删除旧沙箱
sandbox create <branch>          # 重新创建（触发预植入和 postSetupCmds）
```

### 步骤 4：验证

```bash
sandbox exec <branch>
gemini --version                 # 确认工具可用
```

**通常只需修改 `src/tools.ts` 一个文件。** 只有当工具需要注册表尚未支持的新能力时，才需要扩展 `AiTool` 接口和 `create.ts`。
