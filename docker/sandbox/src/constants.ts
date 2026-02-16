import { execFileSync } from 'node:child_process';
import path from 'node:path';

function detectRepoRoot(): string {
  try {
    return execFileSync('git', ['rev-parse', '--show-toplevel'], { encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe'] }).trim();
  } catch {
    throw new Error('sandbox: 当前目录不在 git 仓库内，请在 fit-framework 仓库目录下运行');
  }
}

export const IMAGE_NAME = 'fit-sandbox:latest';
export const MAIN_REPO = detectRepoRoot();
export const WORKTREE_BASE = path.join(process.env.HOME!, '.fit-worktrees');
export const CLAUDE_SANDBOX_BASE = path.join(process.env.HOME!, '.claude-sandboxes');
export const CODEX_SANDBOX_BASE = path.join(process.env.HOME!, '.codex-sandboxes');
export const DOCKERFILE = 'Dockerfile.runtime-only';
export const CONTAINER_PREFIX = 'fit-dev';
export const SCRIPTS_DIR = path.join(MAIN_REPO, 'docker', 'sandbox');
const validatedBranches = new Set<string>();

/**
 * Validate branch name for this sandbox CLI.
 * We intentionally limit to ASCII subset to keep Docker/container names stable.
 */
export function assertValidBranchName(branch: string): void {
  if (validatedBranches.has(branch)) return;

  if (!branch || branch.trim().length === 0) {
    throw new Error('分支名不能为空');
  }

  if (!/^[A-Za-z0-9._/-]+$/.test(branch)) {
    throw new Error(`非法分支名 '${branch}'：仅允许字母、数字、.、_、-、/`);
  }

  try {
    execFileSync('git', ['-C', MAIN_REPO, 'check-ref-format', '--branch', branch], { stdio: 'pipe' });
  } catch {
    throw new Error(`非法分支名 '${branch}'：不符合 git 分支命名规范`);
  }

  validatedBranches.add(branch);
}

/**
 * Collision-free branch encoding for container/worktree-safe names.
 * Git branch names cannot contain "..", so mapping "/" -> ".." is reversible.
 * We intentionally avoid "/" -> "--" because literal "--" can appear in branch names.
 */
export function sanitizeBranchName(branch: string): string {
  assertValidBranchName(branch);
  return branch.replace(/\//g, '..');
}

/**
 * Legacy mapping kept for backward compatibility with already-created sandboxes.
 * Previous versions replaced "/" with "-".
 */
export function legacySanitizeBranchName(branch: string): string {
  return branch.replace(/\//g, '-');
}

function dedupe<T>(items: T[]): T[] {
  return [...new Set(items)];
}

export function safeNameCandidates(branch: string): string[] {
  return dedupe([sanitizeBranchName(branch), legacySanitizeBranchName(branch)]);
}

/** Container name from branch */
export function containerName(branch: string): string {
  return `${CONTAINER_PREFIX}-${sanitizeBranchName(branch)}`;
}

export function containerNameCandidates(branch: string): string[] {
  return safeNameCandidates(branch).map((name) => `${CONTAINER_PREFIX}-${name}`);
}

/** Worktree directory from branch */
export function worktreeDir(branch: string): string {
  return path.join(WORKTREE_BASE, sanitizeBranchName(branch));
}

export function worktreeDirCandidates(branch: string): string[] {
  return safeNameCandidates(branch).map((name) => path.join(WORKTREE_BASE, name));
}

/** Claude config directory from branch */
export function claudeConfigDir(branch: string): string {
  return path.join(CLAUDE_SANDBOX_BASE, sanitizeBranchName(branch));
}

export function claudeConfigDirCandidates(branch: string): string[] {
  return safeNameCandidates(branch).map((name) => path.join(CLAUDE_SANDBOX_BASE, name));
}

/** Codex config directory from branch */
export function codexConfigDir(branch: string): string {
  return path.join(CODEX_SANDBOX_BASE, sanitizeBranchName(branch));
}

export function codexConfigDirCandidates(branch: string): string[] {
  return safeNameCandidates(branch).map((name) => path.join(CODEX_SANDBOX_BASE, name));
}

export function parsePositiveIntegerOption(value: string | undefined, optionName: string): number | undefined {
  if (value === undefined) return undefined;

  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    throw new Error(`${optionName} 必须是正整数，当前值: ${value}`);
  }

  return parsed;
}

/** Detect host CPU/memory, return sensible defaults for VM */
export function detectHostResources(): { cpu: number; memory: number } {
  const hostCpu = Number(execFileSync('sysctl', ['-n', 'hw.ncpu'], { encoding: 'utf8' }).trim());
  const hostMemBytes = Number(execFileSync('sysctl', ['-n', 'hw.memsize'], { encoding: 'utf8' }).trim());
  const hostMemGB = Math.floor(hostMemBytes / 1024 / 1024 / 1024);

  return {
    cpu: Math.max(1, hostCpu - 2),
    memory: Math.max(2, Math.floor(hostMemGB / 2)),
  };
}
