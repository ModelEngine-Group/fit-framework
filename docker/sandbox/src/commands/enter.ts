import pc from 'picocolors';
import { containerName } from '../constants.js';
import { runInteractive, runSafe } from '../shell.js';

export function enter(branch: string, cmd: string[]) {
  const container = containerName(branch);

  // Verify container is running
  const running = runSafe('docker', ['ps', '--format', '{{.Names}}']);
  if (!running.split('\n').includes(container)) {
    console.error(pc.red(`Container ${container} is not running`));
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
