# Repository Guidelines

## Project Structure & Module Organization

This repo hosts the FIT framework and related engines.
- `framework/` contains the core implementations (`framework/fit/java`, `framework/fit/python`, `framework/waterflow`, `framework/fel`).
- `docs/` holds user guides, quick starts, and white papers.
- `examples/` contains runnable samples for FIT and FEL.
- `docker/` provides container build assets.
- `build/` is the Maven build output (generated).

## Build, Test, and Development Commands

- `mvn clean install` (repo root): builds all modules and runs tests; outputs to `build/`.
- `cd framework/fit/java && mvn clean install`: builds the Java FIT framework only.
- `./build/bin/fit start`: starts the Java runtime (uses Node.js); default port `8080`.
- `./.claude/run-test.sh`: full CI-like flow (build, start, health checks).

## Coding Style & Naming Conventions

- Java formatting uses the IntelliJ profile `CodeFormatterFromIdea.xml`.
- Public/protected Java APIs require Javadoc with `@param`/`@return` tags.
- Class headers must include `@author` and `@since yyyy-MM-dd`.
- When modifying any file with a copyright header, you **must first obtain the current year using the system command `date +%Y`**, then update the copyright year accordingly (e.g., if current year is 2026: `2024-2025` → `2024-2026`, `2024` → `2024-2026`).
- Branch naming follows module prefixes and intent, e.g. `fit-feature-xxx`, `waterflow-bugfix-yyy`.

## Testing Guidelines

- Java tests run via Maven (Surefire); `mvn clean install` is the baseline.
- Run targeted module tests from their module directory when iterating.
- Keep tests close to source modules; name tests with `*Test` where applicable.

## Commit & Pull Request Guidelines

- Commit messages use a module tag in brackets and a short Chinese summary, e.g. `[fit] 修复某问题`.
- Keep commit subjects concise (around 20 characters); add details in the body if needed.
- PRs should be based on the correct module/version branch and normally include a single commit.

## Security & Configuration Tips

- Do not report vulnerabilities via public issues; follow `SECURITY.md`.
- Ensure git hooks are enabled for encoding checks (`git-hooks/check-utf8-encoding.sh`).

## Multi-AI Agent Collaboration

This project supports collaboration among multiple AI tools (Claude, GPT, Cursor, etc.).

### Collaboration Configuration

- **`.ai-agents/`** - AI agent configurations (version controlled)
  - `workflows/` - Workflow definitions (recommended processes)
  - `templates/` - Task and documentation templates
  - `claude/` - Claude-specific config (points to `.claude/`)
  - `chatgpt/` - ChatGPT-specific config
  - `gemini/` - Gemini-specific config
  - `cursor/` - Cursor-specific config

- **`.ai-workspace/`** - Collaboration workspace (temporary, gitignored)
  - `tasks/active/` - Tasks in progress
  - `tasks/blocked/` - Blocked tasks
  - `tasks/completed/` - Completed tasks
  - `context/{task-id}/` - Task context files

### Standard Collaboration Flow

Recommended (but not mandatory) workflow:

1. **Requirement Analysis** (Recommended: Claude)
   - Understand requirements, analyze code, assess impact
   - Output: `context/{task-id}/analysis.md`

2. **Technical Design** (Recommended: Claude)
   - Design technical solution, create implementation plan
   - Output: `context/{task-id}/plan.md`
   - ⚠️ **Human Checkpoint**: Review the plan

3. **Implementation** (Recommended: ChatGPT/Gemini/Cursor)
   - Write code and unit tests
   - Output: `context/{task-id}/implementation.md`

4. **Code Review** (Recommended: Claude)
   - Review quality, security, performance
   - Output: `context/{task-id}/review.md`

5. **Refinement** (Any AI)
   - Address review comments

6. **Finalize** (Human confirmation)
   - ⚠️ **Human Checkpoint**: Confirm before commit

### Task Tracking

Create a task:
```bash
cp .ai-agents/templates/task.md .ai-workspace/tasks/active/TASK-$(date +%Y%m%d-%H%M%S).md
```

AI picks up task:
- Read task file: `.ai-workspace/tasks/active/TASK-*.md`
- Read context: `.ai-workspace/context/{task-id}/`
- Complete task checklist
- Create output files

AI handoff: Any AI can take over by reading the context directory.

### AI Capabilities Reference

- **Claude**: Architecture design, code review, security analysis, complex reasoning
- **ChatGPT**: Code implementation, test writing, bug fixing, documentation
- **Gemini**: Code implementation, multimodal understanding, fast responses
- **Cursor**: Code editing, rapid implementation (powered by various models)

**Important**: These are recommendations only. Humans decide which AI handles which step.

### Detailed Documentation

- Collaboration Guide: `.ai-agents/README.md`
- Workflow Definitions: `.ai-agents/workflows/`
- Claude Config: `.claude/README.md`
- ChatGPT Config: `.ai-agents/chatgpt/README.md`
- Gemini Config: `.ai-agents/gemini/README.md`

### Based on Standards

This collaboration approach is based on the [AGENTS.md standard](https://www.infoq.com/news/2025/08/agents-md/) (Linux Foundation AAIF).
