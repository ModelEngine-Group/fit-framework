---
name: create-release-note
description: 从 PR/commit 中自动生成结构化的 Release Notes，并可选创建 GitHub Draft Release。支持 x.y.0 版本合并前一 minor 系列的 release notes。当用户要求生成发布说明、创建 release notes 时触发。参数为版本号和可选的上一版本号。
---

# 生成 Release Notes

自动从 PR、commit 和 Issue 中收集变更信息，按模块和类型分类，生成符合项目格式的 Release Notes。

## 参数

- `<version>`: 当前发布版本号，格式为 X.Y.Z（必需）
- `<prev-version>`: 上一版本号（可选，不提供则自动推断）

## 执行流程

### 步骤 1-3：公共步骤
1. 解析参数并验证版本号格式（X.Y.Z）
2. 确定版本范围（当前 tag 和上一 tag）
3. 判断版本类型: PATCH == 0 走合并路径，PATCH > 0 走常规路径

### 合并路径（x.y.0 版本）
从 GitHub 已发布的 release 中合并前一 minor 系列的所有 release notes。

### 常规路径（x.y.z, z > 0）
4. 收集两个 tag 之间合并的 PR 和直接 commit
5. 收集关联的 Issue
6. 按模块分组（FIT/FEL/Waterflow/AI Config）
7. 按类型分组（Enhancement/Bugfix/Documentation）
8. 生成格式化的 Release Notes

### 公共步骤
9. 展示并确认，询问用户是否调整
10. 创建 GitHub Draft Release:
    ```bash
    gh release create v<version> --title "v<version>" --notes-file /tmp/release-notes-v<version>.md --draft
    ```

## 注意事项

- 需要 `gh` CLI 且已认证
- Tag 必须已存在（通常由 release skill 完成）
- 创建的是 Draft Release，需人工审核后发布
