# Codex 项目级 Prompts

本目录提供仓库内的 Codex prompts（用于生成/管理 slash 命令的文档版本）。
由于 Codex CLI 的自定义 prompts 只会从用户目录读取（默认 `~/.codex/prompts/`），
这里的内容需要手动安装到本地目录后才会生效。

## 安装到本地

运行安装脚本，将本目录的 prompts 复制到 `~/.codex/prompts/`：

```bash
bash .codex/scripts/install-prompts.sh
```

安装完成后，使用 `/prompts:<name>` 调用（例如：`/prompts:analyze-issue`）。

## 使用提示

- 这些 prompts 以“仓库根目录”为默认上下文。
- 作为全局 prompts 使用时，请先切换到目标仓库或显式指定路径（详见各命令文件中的“使用前：选择目标仓库”）。
