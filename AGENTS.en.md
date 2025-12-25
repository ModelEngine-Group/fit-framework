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
