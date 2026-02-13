import { execFileSync, spawnSync } from 'node:child_process';

/** Run a command and return trimmed stdout. Throws on non-zero exit. */
export function run(cmd: string, args: string[], opts?: { cwd?: string }): string {
  return execFileSync(cmd, args, {
    encoding: 'utf8',
    cwd: opts?.cwd,
    stdio: ['pipe', 'pipe', 'pipe'],
  }).trim();
}

/** Run a command silently, return true if exit code is 0 */
export function runOk(cmd: string, args: string[]): boolean {
  const result = spawnSync(cmd, args, { stdio: 'pipe' });
  return result.status === 0;
}

/** Run a command with inherited stdio (visible to user) */
export function runInteractive(cmd: string, args: string[], opts?: { cwd?: string }): number {
  const result = spawnSync(cmd, args, {
    stdio: 'inherit',
    cwd: opts?.cwd,
  });
  return result.status ?? 1;
}

/** Run a command with stdout/stderr streamed to terminal. Throws on non-zero exit. */
export function runVerbose(cmd: string, args: string[], opts?: { cwd?: string }): void {
  const result = spawnSync(cmd, args, {
    stdio: 'inherit',
    cwd: opts?.cwd,
  });
  if (result.status !== 0) {
    throw new Error(`Command failed with exit code ${result.status}: ${cmd} ${args.join(' ')}`);
  }
}

/** Run a command, return stdout even if it fails (empty string on error) */
export function runSafe(cmd: string, args: string[]): string {
  const result = spawnSync(cmd, args, { encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe'] });
  return (result.stdout ?? '').trim();
}
