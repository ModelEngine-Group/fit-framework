#!/bin/bash
# FIT Framework 完整测试脚本

set -e  # 遇到错误立即退出

echo "=========================================="
echo "FIT Framework 测试流程"
echo "=========================================="

# 创建日志目录
mkdir -p .ai-workspace/logs

# 1. 清理构建产物
echo ""
echo "[1/6] 清理构建产物..."
rm -rf build

# 2. 执行单元测试和构建
echo ""
echo "[2/6] 执行 Maven 构建和单元测试..."
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
LOG_FILE=".ai-workspace/logs/maven-build-${TIMESTAMP}.log"
echo "构建日志保存到: $LOG_FILE"

cd framework
mvn clean install -pl '!ohscript' > "$LOG_FILE" 2>&1
BUILD_STATUS=$?
cd ..

# 只显示最后 100 行（包含构建摘要）
echo "========== 构建摘要 =========="
tail -100 "$LOG_FILE"

# 如果构建失败，显示错误信息并退出
if [ $BUILD_STATUS -ne 0 ]; then
    echo ""
    echo "========== 错误详情 =========="
    grep -A 5 "BUILD FAILURE\|COMPILATION ERROR\|ERROR\|FAILED" "$LOG_FILE" | head -50
    echo ""
    echo "完整日志已保存到: $LOG_FILE"
    exit $BUILD_STATUS
fi

echo "✓ Maven 构建成功"
echo "完整日志已保存到: $LOG_FILE"

# 3. 创建动态插件目录
echo ""
echo "[3/6] 创建动态插件目录..."
mkdir -p dynamic-plugins
echo "✓ 动态插件目录创建成功"

# 4. 启动 FIT 服务
echo ""
echo "[4/6] 启动 FIT 服务..."
FIT_LOG=".ai-workspace/logs/fit-server-${TIMESTAMP}.log"
build/bin/fit start --plugin-dir=dynamic-plugins > "$FIT_LOG" 2>&1 &
FIT_PID=$!
echo "FIT 服务进程 PID: $FIT_PID"
echo "FIT 服务日志: $FIT_LOG"

# 等待服务启动（最多 60 秒）
echo "等待服务启动..."
for i in {1..60}; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null)
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✓ FIT 服务启动成功 (HTTP $HTTP_CODE)"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "✗ FIT 服务启动超时 (最后 HTTP 状态码: $HTTP_CODE)"
        echo "========== 启动日志 =========="
        cat "$FIT_LOG"
        kill $FIT_PID 2>/dev/null || true
        exit 1
    fi
    sleep 1
done

# 5. 验证健康检查接口
echo ""
echo "[5/6] 验证健康检查接口..."
HTTP_CODE=$(curl -s -o /tmp/plugins_response.json -w "%{http_code}" http://localhost:8080/actuator/plugins)
if [ "$HTTP_CODE" = "200" ]; then
    echo "✓ 健康检查接口正常 (HTTP $HTTP_CODE)"
    echo "插件列表响应:"
    cat /tmp/plugins_response.json | head -20
    rm -f /tmp/plugins_response.json
else
    echo "✗ 健康检查接口失败 (HTTP $HTTP_CODE)"
    rm -f /tmp/plugins_response.json
    pkill -f fit-discrete-launcher || true
    exit 1
fi

# 6. 验证 Swagger 文档页面
echo ""
echo "[6/6] 验证 Swagger 文档页面..."
HTTP_CODE=$(curl -s -o /tmp/swagger_response.html -w "%{http_code}" http://localhost:8080/openapi.html)
if [ "$HTTP_CODE" = "200" ] && grep -qi "swagger\|openapi" /tmp/swagger_response.html 2>/dev/null; then
    echo "✓ Swagger 文档页面可访问 (HTTP $HTTP_CODE)"
else
    echo "⚠ Swagger 文档页面响应异常 (HTTP $HTTP_CODE)"
fi
rm -f /tmp/swagger_response.html

# 7. 清理测试环境
echo ""
echo "[7/7] 清理测试环境..."
echo "停止 FIT 服务..."
pkill -f fit-discrete-launcher || true
sleep 2

echo "删除构建产物和动态插件目录..."
rm -rf build
rm -rf dynamic-plugins
echo "测试日志已保存到: .ai-workspace/logs/"

echo ""
echo "=========================================="
echo "✓ 测试流程完成"
echo "=========================================="
