提交当前变更到 Git。

**此命令已迁移到官方插件，将调用 `commit-commands` 插件。**

**用法：**
- `/commit` - 创建 Git 提交
- `/commit-commands:commit` - 直接使用插件命令
- `/commit-commands:commit-push-pr` - 一键提交+推送+创建PR

**实际执行：**
调用 `/commit-commands:commit` 插件命令

**插件功能：**
- 自动分析变更内容
- 生成符合规范的提交消息
- 支持交互式和直接提交模式
- 添加 Co-Authored-By 签名
- 自动检测敏感信息

**扩展用法：**
如需一键完成提交→推送→创建PR，使用：
```
/commit-commands:commit-push-pr
```

**注意事项：**
- 不要提交包含敏感信息的文件（.env, credentials 等）
- 确保提交消息清晰描述了变更内容
- 遵循项目的 commit message 规范
