#!/bin/bash
# AI Coding Sandbox CLI - thin wrapper around TypeScript implementation
# Usage: ./sandbox.sh <command> [options] [args]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec npx --yes tsx "${SCRIPT_DIR}/src/cli.ts" "$@"
