import pc from 'picocolors';
import { assertValidBranchName, containerNameCandidates } from '../constants.js';
import { runInteractive, runSafe } from '../shell.js';

export function enter(branch: string, cmd: string[]) {
  assertValidBranchName(branch);

  // Verify container is running
  const running = runSafe('docker', ['ps', '--format', '{{.Names}}']).split('\n');
  const container = containerNameCandidates(branch).find((name) => running.includes(name));
  if (!container) {
    console.error(pc.red(`No running container found for branch '${branch}'`));
    process.exit(1);
  }

  if (cmd.length === 0) {
    // Default: open interactive bash shell
    const code = runInteractive('docker', ['exec', '-it', container, 'bash']);
    process.exit(code);
  } else {
    const code = runInteractive('docker', ['exec', '-it', container, ...cmd]);
    process.exit(code);
  }
}
