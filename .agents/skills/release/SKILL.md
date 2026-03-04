---
name: release
description: 执行标准化的版本发布流程（SNAPSHOT 替换、Release commit、Tag、发布分支、下一 SNAPSHOT）。当用户要求发布版本、执行 release、创建新版本时触发。参数为版本号（X.Y.Z 格式）。
---

# 版本发布

执行标准化的版本发布流程。不含构建验证，不含自动推送。

## 参数

- `<version>`: 发布版本号，格式为 X.Y.Z（必需）

## 执行步骤

1. **解析并验证参数**: 版本号必须匹配 X.Y.Z 格式，确认 SNAPSHOT 存在。

2. **确认工作区状态**: 工作区必须干净（无未提交更改）。

3. **全局替换 SNAPSHOT -> Release**: 将 `X.Y.Z-SNAPSHOT` 替换为 `X.Y.Z`。
   排除目录: `.agents/`, `.ai-workspace/`, `.claude/`, `.codex/`, `.gemini/`, `.opencode/`

4. **创建 Release commit**: `git add -A && git commit -m "Release X.Y.Z"`

5. **创建轻量标签**: `git tag vX.Y.Z`

6. **创建发布分支**: `git branch release-X.Y.Z`

7. **回退当前分支**: `git reset --hard HEAD~1`

8. **替换为下一 SNAPSHOT**: `X.Y.Z-SNAPSHOT` -> `X.Y.(Z+1)-SNAPSHOT`

9. **创建 SNAPSHOT commit**: `git add -A && git commit -m "Prepare the next SNAPSHOT version"`

10. **输出总结**: 包含发布信息和后续手动操作指引。

## 注意事项

- 工作区必须干净
- 不会自动推送，所有操作仅在本地
- 建议发布前已完成 `mvn clean install` 验证
