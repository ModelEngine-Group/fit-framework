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
- `./.ai-agents/scripts/run-test.sh`: full CI-like flow (build, start, health checks).

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

- Commit messages follow Conventional Commits: `<type>(<scope>): <subject>`, with a Chinese subject around 20 characters.
- Scope is the module name (e.g., `fit`, `waterflow`, `fel`) and can be omitted.
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
  - `active/` - Tasks in progress (with task files and context)
  - `blocked/` - Blocked tasks
  - `completed/` - Completed tasks
  - `logs/` - Collaboration logs and records

### Standard Collaboration Flow

Recommended (but not mandatory) workflow:

1. **Requirement Analysis** (Recommended: Claude)
   - Use `/analyze-issue <issue-number>` command
   - Understand requirements, analyze code, assess impact
   - Output: `active/{task-id}/analysis.md`

2. **Technical Design** (Recommended: Claude)
   - Use `/plan-task <task-id>` command
   - Design technical solution, create implementation plan
   - Output: `active/{task-id}/plan.md`
   - ⚠️ **Human Checkpoint**: Review the plan

3. **Implementation** (Recommended: ChatGPT/Gemini/Cursor)
   - Use `/implement-task <task-id>` command
   - Write code and unit tests
   - Output: `active/{task-id}/implementation.md`

4. **Code Review** (Recommended: Claude)
   - Use `/review-task <task-id>` command
   - Review quality, security, performance
   - Output: `active/{task-id}/review.md`

5. **Refinement** (Any AI)
   - Use `/refinement-task <task-id>` command
   - Address review comments
   - Output: `active/{task-id}/refinement-report.md`

6. **Task Completion** (Recommended: Claude)
   - Use `/complete-task <task-id>` command
   - ⚠️ **Human Checkpoint**: Confirm before archiving
   - Task moved to `completed/` directory

7. **Blocking Handling** (Special cases)
   - Use `/block-task <task-id> --reason <reason>` command
   - Task moved to `blocked/` directory
   - Record blocking reason and needed help

### Task Tracking

**Use Slash Commands to create and manage tasks**:
```bash
# Analyze Issue and create task
/analyze-issue <issue-number>

# View task status
/task-status <task-id>

# Sync to GitHub Issue
/sync-issue <issue-number>
```

**Task directory structure**:
```
.ai-workspace/
├── active/TASK-{timestamp}/      # Tasks in progress
│   ├── task.md                   # Task metadata
│   ├── analysis.md               # Requirements analysis
│   ├── plan.md                   # Technical design
│   ├── implementation.md         # Implementation report
│   └── review.md                 # Code review report
├── blocked/TASK-{timestamp}/     # Blocked tasks
└── completed/TASK-{timestamp}/   # Completed tasks
```

AI picks up task:
- Read task file: `.ai-workspace/active/{task-id}/task.md`
- Read context files: `active/{task-id}/`
- Complete task checklist
- Update task status (CRITICAL: see Rule 7)

AI handoff: Any AI can take over by reading the task directory.

### AI Capabilities Reference

- **Claude**: Architecture design, code review, security analysis, complex reasoning
- **ChatGPT**: Code implementation, test writing, bug fixing, documentation
- **Gemini**: Code implementation, multimodal understanding, fast responses
- **Cursor**: Code editing, rapid implementation (powered by various models)

**Important**: These are recommendations only. Humans decide which AI handles which step.

### Communication Language Guidelines

**All AI agents must follow these language guidelines**:

- **Response language matches input language**: AI should automatically adapt to the user's input language (adaptive strategy)
  - User asks in Chinese → AI responds in Chinese
  - User asks in English → AI responds in English
- **Project default language**: Chinese
  - Code comments: Chinese
  - Documentation: Chinese
  - Commit messages: Chinese (following Conventional Commits format)
- **Configuration**: Defined via `communication_language: "Adaptive (Match user's language)"` field in each AI's `preferences.yaml`

### Detailed Documentation

- Collaboration Guide: `.ai-agents/README.md`
- Quick Start: `.ai-agents/QUICKSTART.md`
- Workflow Definitions: `.ai-agents/workflows/`
- Claude Config: `.claude/README.md`
- Claude Project Rules: `.claude/project-rules.md` (includes Rule 7: Task Status Management)
- Claude Command Reference: `.claude/commands/`
- Codex Config: `.ai-agents/codex/README.md`
- Gemini Config: `.ai-agents/gemini/README.md`

### Based on Standards

This collaboration approach is based on the [AGENTS.md standard](https://www.infoq.com/news/2025/08/agents-md/) (Linux Foundation AAIF).
