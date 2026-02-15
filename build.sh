#!/bin/bash
# FIT Framework - 全局编译构建脚本
# 包含 Java（Maven）和 Sandbox CLI（npm）的完整构建，默认跳过测试。
#
# Usage:
#   ./build.sh              # 构建全部（跳过测试）
#   ./build.sh --with-test  # 构建全部（包含测试）

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SANDBOX_DIR="${SCRIPT_DIR}/docker/sandbox"
BUILD_BIN="${SCRIPT_DIR}/build/bin"

WITH_TEST=false
if [ "$1" = "--with-test" ]; then
    WITH_TEST=true
fi

# ========== 1. Maven Build ==========
echo "========== [1/2] Maven Build =========="
if [ "$WITH_TEST" = true ]; then
    mvn clean install
else
    mvn clean install -DskipTests
fi

# ========== 2. Sandbox CLI ==========
echo ""
echo "========== [2/2] Sandbox CLI =========="
if ! command -v npm &> /dev/null; then
    echo "WARNING: npm not found, skipping sandbox CLI."
    echo "         Install Node.js 18+ and re-run to enable sandbox command."
    exit 0
fi

(cd "${SANDBOX_DIR}" && npm install --no-fund --no-audit && npm run build)
cp "${SANDBOX_DIR}/dist/sandbox.cjs" "${BUILD_BIN}/sandbox.cjs"
chmod +x "${BUILD_BIN}/sandbox.cjs"

# 创建 sandbox 可执行入口
cat > "${BUILD_BIN}/sandbox" << 'EOF'
#!/usr/bin/env node
require('./sandbox.cjs');
EOF
chmod +x "${BUILD_BIN}/sandbox"

echo "Sandbox CLI ready."

echo ""
echo "========== Build Complete =========="
