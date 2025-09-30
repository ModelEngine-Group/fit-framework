#!/bin/bash

# HTTP Client Authentication Test Script
# 用于批量执行 HTTP 客户端认证功能的测试用例

set -e  # 遇到错误时退出

# 配置
BASE_URL="http://localhost:8080/http-server/auth"
TIMEOUT=10
VERBOSE=false
TEST_TYPE=""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 帮助信息
show_help() {
    echo "HTTP Client Authentication Test Script"
    echo ""
    echo "Usage: $0 [OPTIONS] [TEST_TYPE]"
    echo ""
    echo "Options:"
    echo "  -h, --help     显示此帮助信息"
    echo "  -v, --verbose  详细输出模式"
    echo "  -t, --timeout  请求超时时间（秒），默认 10"
    echo "  -u, --url      服务器基础 URL，默认 http://localhost:8080/http-server/auth"
    echo ""
    echo "Test Types:"
    echo "  all      运行所有测试（默认）"
    echo "  bearer   只运行 Bearer Token 相关测试"
    echo "  basic    只运行 Basic 认证测试"
    echo "  apikey   只运行 API Key 相关测试"
    echo "  provider 只运行 Provider 相关测试"
    echo "  error    只运行错误场景测试"
    echo ""
    echo "Examples:"
    echo "  $0                    # 运行所有测试"
    echo "  $0 bearer            # 只运行 Bearer Token 测试"
    echo "  $0 -v apikey         # 详细模式运行 API Key 测试"
    echo "  $0 -t 30 provider    # 30秒超时运行 Provider 测试"
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -t|--timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        -u|--url)
            BASE_URL="$2"
            shift 2
            ;;
        all|bearer|basic|apikey|provider|error)
            TEST_TYPE="$1"
            shift
            ;;
        *)
            echo "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# 默认运行所有测试
if [ -z "$TEST_TYPE" ]; then
    TEST_TYPE="all"
fi

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# 显示服务器信息（不进行连接检查）
show_server_info() {
    log_info "目标服务器: $BASE_URL"
    log_info "如果测试失败，请确保服务器已启动：mvn spring-boot:run -pl plugin-http-server"
}

# 执行单个测试
run_test() {
    local test_name="$1"
    local curl_cmd="$2"
    local expected_pattern="$3"

    if [ "$VERBOSE" = true ]; then
        log_info "执行测试: $test_name"
        log_info "命令: $curl_cmd"
    else
        printf "%-40s" "$test_name"
    fi

    # 执行 curl 命令
    local response
    local exit_code
    response=$(eval "$curl_cmd" 2>&1)
    exit_code=$?

    if [ $exit_code -ne 0 ]; then
        if [ "$VERBOSE" = true ]; then
            log_error "请求失败: $response"
        else
            echo -e "${RED}FAIL${NC}"
        fi
        return 1
    fi

    # 检查响应
    if [[ "$response" == *"$expected_pattern"* ]]; then
        if [ "$VERBOSE" = true ]; then
            log_success "测试通过"
            log_info "响应: $response"
        else
            echo -e "${GREEN}PASS${NC}"
        fi
        return 0
    else
        if [ "$VERBOSE" = true ]; then
            log_error "响应不匹配期望模式"
            log_info "期望包含: $expected_pattern"
            log_info "实际响应: $response"
        else
            echo -e "${RED}FAIL${NC}"
        fi
        return 1
    fi
}

# Bearer Token 测试
run_bearer_tests() {
    log_info "运行 Bearer Token 测试..."

    run_test "Bearer Static Auth" \
        "curl -s --max-time $TIMEOUT -X GET \"$BASE_URL/bearer-static\" -H \"Authorization: Bearer static-bearer-token-12345\" -H \"X-Service-Key: service-default-key\"" \
        "Bearer Static Auth: Bearer static-bearer-token-12345"

    run_test "Bearer Dynamic Auth" \
        "curl -s --max-time $TIMEOUT -X GET \"$BASE_URL/bearer-dynamic\" -H \"Authorization: Bearer dynamic-bearer-token-67890\"" \
        "Bearer Dynamic Auth: Bearer dynamic-bearer-token-67890"
}

# Basic 认证测试
run_basic_tests() {
    log_info "运行 Basic 认证测试..."

    # admin:secret123 的 base64 编码
    run_test "Basic Static Auth" \
        "curl -s --max-time $TIMEOUT -X GET \"$BASE_URL/basic-static\" -H \"Authorization: Basic YWRtaW46c2VjcmV0MTIz\" -H \"X-Service-Key: service-default-key\"" \
        "Basic Static Auth: Basic YWRtaW46c2VjcmV0MTIz"
}

# API Key 测试
run_apikey_tests() {
    log_info "运行 API Key 测试..."

    run_test "API Key Header Static" \
        "curl -s --max-time $TIMEOUT -X GET \"$BASE_URL/apikey-header-static\" -H \"X-API-Key: static-api-key-67890\" -H \"X-Service-Key: service-default-key\"" \
        "API Key Header Static: static-api-key-67890"

    run_test "API Key Query Static" \
        "curl -s --max-time $TIMEOUT -X GET \"$BASE_URL/apikey-query-static?api_key=query-api-key-111\" -H \"X-Service-Key: service-default-key\"" \
        "API Key Query Static: query-api-key-111"

    run_test "API Key Dynamic" \
        "curl -s --max-time $TIMEOUT -X GET \"$BASE_URL/apikey-dynamic\" -H \"X-Dynamic-Key: dynamic-api-key-999\" -H \"X-Service-Key: service-default-key\"" \
        "API Key Dynamic: dynamic-api-key-999"
}

# Provider 测试
run_provider_tests() {
    log_info "运行 Provider 测试..."

    run_test "Dynamic Provider Auth" \
        "curl -s --max-time $TIMEOUT -X GET \"$BASE_URL/dynamic-provider\" -H \"Authorization: Bearer provider-generated-token-123\" -H \"X-Service-Key: service-default-key\"" \
        "Dynamic Provider Auth: Bearer provider-generated-token-123"

    run_test "Custom Provider Auth" \
        "curl -s --max-time $TIMEOUT -X GET \"$BASE_URL/custom-provider\" -H \"X-Timestamp: 1640995200000\" -H \"X-Signature: custom-signature-abc123\" -H \"X-App-Id: test-app-001\" -H \"X-Service-Key: service-default-key\"" \
        "Custom Provider Auth"

    run_test "Method Override Auth" \
        "curl -s --max-time $TIMEOUT -X GET \"$BASE_URL/method-override\" -H \"X-API-Key: method-override-key-456\"" \
        "Method Override Auth: method-override-key-456"

    run_test "Combined Auth" \
        "curl -s --max-time $TIMEOUT -X GET \"$BASE_URL/combined-auth\" -H \"Authorization: Bearer combined-auth-token-789\" -H \"X-User-Context: user-context-key-abc\" -H \"X-Service-Key: service-default-key\"" \
        "Combined Auth"
}

# 错误场景测试
run_error_tests() {
    log_info "运行错误场景测试..."

    # 这些测试期望返回错误状态码
    log_warning "注意：错误场景测试可能会显示预期的失败结果"

    run_test "Missing Authorization Header" \
        "curl -s --max-time $TIMEOUT -w '%{http_code}' -X GET \"$BASE_URL/bearer-static\"" \
        "400\\|401\\|403"

    run_test "Missing API Key Header" \
        "curl -s --max-time $TIMEOUT -w '%{http_code}' -X GET \"$BASE_URL/apikey-header-static\" -H \"X-Service-Key: service-default-key\"" \
        "400\\|401\\|403"
}

# 主执行函数
main() {
    echo "=========================================="
    echo "HTTP Client Authentication Test Suite"
    echo "=========================================="
    echo "服务器: $BASE_URL"
    echo "超时时间: ${TIMEOUT}s"
    echo "测试类型: $TEST_TYPE"
    echo "详细模式: $VERBOSE"
    echo "=========================================="

    # 显示服务器信息
    show_server_info

    # 统计变量
    local total_tests=0
    local passed_tests=0

    # 根据测试类型运行测试
    case $TEST_TYPE in
        "all")
            run_bearer_tests
            run_basic_tests
            run_apikey_tests
            run_provider_tests
            ;;
        "bearer")
            run_bearer_tests
            ;;
        "basic")
            run_basic_tests
            ;;
        "apikey")
            run_apikey_tests
            ;;
        "provider")
            run_provider_tests
            ;;
        "error")
            run_error_tests
            ;;
    esac

    echo "=========================================="
    echo "测试完成！"
    echo "=========================================="

    if [ "$TEST_TYPE" = "error" ]; then
        log_warning "错误场景测试完成，某些失败是预期的"
    fi
}

# 执行主函数
main "$@"