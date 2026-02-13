import * as p from '@clack/prompts';
import pc from 'picocolors';
import { detectHostResources } from '../constants.js';
import { run, runOk, runSafe } from '../shell.js';

interface VmStartOptions {
  cpu?: string;
  memory?: string;
}

export function vmStatus() {
  p.intro(pc.cyan('Colima VM 状态'));

  if (runOk('colima', ['status'])) {
    const status = runSafe('colima', ['status']);
    console.log(status);
  } else {
    p.log.warn('Colima VM is not running');
  }

}

export function vmStart(opts: VmStartOptions) {
  const defaults = detectHostResources();
  const cpu = opts.cpu ? Number(opts.cpu) : defaults.cpu;
  const memory = opts.memory ? Number(opts.memory) : defaults.memory;

  p.intro(pc.cyan('Starting Colima VM'));

  if (runOk('colima', ['status'])) {
    p.log.success('VM is already running');
      return;
  }

  const s = p.spinner();
  s.start(`Starting VM with ${cpu} CPU / ${memory} GB memory...`);

  const arch = runSafe('uname', ['-m']);
  const args = ['start', '--cpu', String(cpu), '--memory', String(memory), '--disk', '60'];
  if (arch === 'arm64') {
    args.push('--arch', 'aarch64', '--vm-type=vz', '--mount-type=virtiofs');
  } else {
    args.push('--arch', 'x86_64');
  }

  try {
    run('colima', args);
    s.stop(pc.green('VM started'));
  } catch (e) {
    s.stop(pc.red('Failed to start VM'));
    throw e;
  }

}

export function vmStop() {
  p.intro(pc.cyan('Stopping Colima VM'));

  const s = p.spinner();
  s.start('Stopping VM...');

  try {
    run('colima', ['stop']);
    s.stop(pc.green('VM stopped'));
  } catch {
    s.stop(pc.yellow('VM was not running or failed to stop'));
  }

}
