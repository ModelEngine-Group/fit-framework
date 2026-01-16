#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$SCRIPT_DIR/../commands"
DEST_DIR="$HOME/.codex/prompts"

echo "📦 Codex Prompts 安装脚本"
echo "源目录: $SRC_DIR"
echo "目标目录: $DEST_DIR"
echo ""

# 创建目标目录
mkdir -p "$DEST_DIR"

# 检查源目录是否存在
if [ ! -d "$SRC_DIR" ]; then
  echo "❌ 错误: 源目录不存在: $SRC_DIR" >&2
  exit 1
fi

# 检查是否有 .md 文件
if ! ls "$SRC_DIR"/*.md >/dev/null 2>&1; then
  echo "❌ 错误: 在源目录中未找到 .md 文件" >&2
  exit 1
fi

# 统计文件数量
FILE_COUNT=$(ls -1 "$SRC_DIR"/*.md 2>/dev/null | wc -l | tr -d ' ')
echo "📝 发现 $FILE_COUNT 个 prompt 文件"

# 检查是否有文件会被覆盖
OVERWRITE_COUNT=0
for file in "$SRC_DIR"/*.md; do
  filename=$(basename "$file")
  if [ -f "$DEST_DIR/$filename" ]; then
    OVERWRITE_COUNT=$((OVERWRITE_COUNT + 1))
  fi
done

if [ $OVERWRITE_COUNT -gt 0 ]; then
  echo "⚠️  将覆盖 $OVERWRITE_COUNT 个已存在的文件"
fi

# 复制文件（强制覆盖）
echo ""
echo "🚀 开始安装..."
cp -fv "$SRC_DIR"/*.md "$DEST_DIR"/ 2>&1 | sed 's/^/  /'

echo ""
echo "✅ 安装完成！共安装 $FILE_COUNT 个命令"
echo ""
echo "📍 Prompts 已安装到: $DEST_DIR"
echo "💡 使用方式: /prompts:<name>"
echo ""
echo "📋 已安装的命令列表:"
echo ""

# 读取每个命令的描述并显示
for file in "$DEST_DIR"/*.md; do
  filename=$(basename "$file" .md)
  # 尝试从文件中提取 description（YAML frontmatter 中的 description 字段）
  description=$(grep -m 1 '^description:' "$file" 2>/dev/null | sed 's/^description: *//; s/^"//; s/"$//' || true)

  if [ -n "$description" ]; then
    printf "  /prompts:%-20s - %s\n" "$filename" "$description"
  else
    printf "  /prompts:%s\n" "$filename"
  fi
done | sort

# 确保脚本成功退出
exit 0
