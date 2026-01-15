#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$SCRIPT_DIR/../commands"
DEST_DIR="$HOME/.codex/prompts"

mkdir -p "$DEST_DIR"

if [ ! -d "$SRC_DIR" ]; then
  echo "Source prompts not found: $SRC_DIR" >&2
  exit 1
fi

cp -f "$SRC_DIR"/*.md "$DEST_DIR"/

echo "Installed prompts to: $DEST_DIR"
echo "Use /prompts:<name> to invoke them (e.g., /prompts:analyze-issue)."
