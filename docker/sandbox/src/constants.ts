import { execFileSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export const IMAGE_NAME = 'fit-sandbox:latest';
export const MAIN_REPO = path.resolve(__dirname, '..', '..', '..');
export const WORKTREE_BASE = path.join(process.env.HOME!, '.fit-worktrees');
export const CLAUDE_SANDBOX_BASE = path.join(process.env.HOME!, '.claude-sandboxes');
export const DOCKERFILE = 'Dockerfile.runtime-only';
export const CONTAINER_PREFIX = 'fit-dev';
export const SCRIPTS_DIR = path.resolve(__dirname, '..');

/** Replace / with - for container-safe naming */
export function sanitizeBranchName(branch: string): string {
  return branch.replace(/\//g, '-');
}

/** Container name from branch */
export function containerName(branch: string): string {
  return `${CONTAINER_PREFIX}-${sanitizeBranchName(branch)}`;
}

/** Worktree directory from branch */
export function worktreeDir(branch: string): string {
  return path.join(WORKTREE_BASE, sanitizeBranchName(branch));
}

/** Claude config directory from branch */
export function claudeConfigDir(branch: string): string {
  return path.join(CLAUDE_SANDBOX_BASE, sanitizeBranchName(branch));
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
