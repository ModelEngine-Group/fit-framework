import fs from 'node:fs';
import * as p from '@clack/prompts';
import pc from 'picocolors';
import {
  IMAGE_NAME, MAIN_REPO, WORKTREE_BASE, CLAUDE_SANDBOX_BASE,
  containerNameCandidates, worktreeDirCandidates, claudeConfigDirCandidates,
  sanitizeBranchName, assertValidBranchName,
} from '../constants.js';
import { run, runOk, runSafe } from '../shell.js';

export async function rmOne(branch: string) {
  assertValidBranchName(branch);

  const safeName = sanitizeBranchName(branch);
  let effectiveBranch = branch;
  let worktreeCandidates = worktreeDirCandidates(effectiveBranch);
  let claudeDirCandidates = claudeConfigDirCandidates(effectiveBranch);

  p.intro(pc.cyan(`清理沙箱: ${safeName}`));

  // Stop and remove container
  const existing = runSafe('docker', ['ps', '-a', '--format', '{{.Names}}']).split('\n');
  const matchedContainers = containerNameCandidates(branch).filter((name) => existing.includes(name));
  if (matchedContainers.length > 0) {
    const resolvedBranch = runSafe('docker', [
      'inspect',
      '-f',
      '{{ index .Config.Labels "fit-sandbox.branch" }}',
      matchedContainers[0],
    ]);
    if (resolvedBranch) {
      effectiveBranch = resolvedBranch;
      worktreeCandidates = worktreeDirCandidates(effectiveBranch);
      claudeDirCandidates = claudeConfigDirCandidates(effectiveBranch);
    }

    const s = p.spinner();
    s.start(`Stopping container(s): ${matchedContainers.join(', ')}...`);
    for (const name of matchedContainers) {
      runSafe('docker', ['stop', name]);
      runSafe('docker', ['rm', name]);
    }
    s.stop(pc.green(`Container removed: ${matchedContainers.join(', ')}`));
  } else {
    p.log.warn(`Container not found for branch '${branch}'`);
  }

  // Clean worktree
  const existingWorktrees = worktreeCandidates.filter((dir) => fs.existsSync(dir));
  if (existingWorktrees.length > 0) {
    const shouldRemoveWorktree = await p.confirm({
      message: `Remove worktree(s): ${existingWorktrees.join(', ')}?`,
      initialValue: false,
    });
    if (p.isCancel(shouldRemoveWorktree)) { p.outro('Cancelled'); return; }

    if (shouldRemoveWorktree) {
      for (const worktree of existingWorktrees) {
        try {
          run('git', ['-C', MAIN_REPO, 'worktree', 'remove', worktree, '--force']);
        } catch {
          fs.rmSync(worktree, { recursive: true, force: true });
        }
      }
      p.log.success('Worktree removed');

      // Optionally delete branch
      const shouldDeleteBranch = await p.confirm({
        message: `Also delete branch '${effectiveBranch}'?`,
        initialValue: false,
      });
      if (!p.isCancel(shouldDeleteBranch) && shouldDeleteBranch) {
        if (runOk('git', ['-C', MAIN_REPO, 'branch', '-D', effectiveBranch])) {
          p.log.success('Branch deleted');
        } else {
          p.log.warn(`Branch '${effectiveBranch}' was not deleted (not found or delete failed)`);
        }
      }
    }
  }

  // Clean claude config directory
  const existingClaudeDirs = claudeDirCandidates.filter((dir) => fs.existsSync(dir));
  if (existingClaudeDirs.length > 0) {
    for (const claudeDir of existingClaudeDirs) {
      fs.rmSync(claudeDir, { recursive: true, force: true });
      p.log.success(`Claude config removed: ${claudeDir}`);
    }
  }

  p.outro(pc.green('Done'));
}

export async function rmAll() {
  p.intro(pc.cyan('清理所有沙箱'));

  // Stop all sandbox containers
  const containers = runSafe('docker', ['ps', '-a', '--filter', 'label=fit-sandbox', '--format', '{{.Names}}']);
  if (containers) {
    const s = p.spinner();
    s.start('Stopping all sandbox containers...');
    for (const name of containers.split('\n').filter(Boolean)) {
      runSafe('docker', ['stop', name]);
      runSafe('docker', ['rm', name]);
    }
    s.stop(pc.green('All containers removed'));
  } else {
    p.log.warn('No sandbox containers found');
  }

  // Clean worktrees
  if (fs.existsSync(WORKTREE_BASE) && fs.readdirSync(WORKTREE_BASE).length > 0) {
    const shouldRemove = await p.confirm({
      message: `Remove all worktrees in ${WORKTREE_BASE}?`,
      initialValue: false,
    });
    if (!p.isCancel(shouldRemove) && shouldRemove) {
      for (const entry of fs.readdirSync(WORKTREE_BASE)) {
        const dir = `${WORKTREE_BASE}/${entry}`;
        try {
          run('git', ['-C', MAIN_REPO, 'worktree', 'remove', dir, '--force']);
        } catch {
          fs.rmSync(dir, { recursive: true, force: true });
        }
        p.log.info(`  Removed worktree ${entry}`);
      }
      runSafe('git', ['-C', MAIN_REPO, 'worktree', 'prune']);
      p.log.success('All worktrees removed');
    }
  }

  // Clean claude sandbox configs
  if (fs.existsSync(CLAUDE_SANDBOX_BASE) && fs.readdirSync(CLAUDE_SANDBOX_BASE).length > 0) {
    fs.rmSync(CLAUDE_SANDBOX_BASE, { recursive: true, force: true });
    p.log.success('All Claude sandbox configs removed');
  }

  // Prune dangling images
  runSafe('docker', ['image', 'prune', '-f']);
  p.log.info('Dangling images cleaned');

  // Optionally remove image
  const shouldRmImage = await p.confirm({
    message: `Remove image ${IMAGE_NAME}?`,
    initialValue: false,
  });
  if (!p.isCancel(shouldRmImage) && shouldRmImage) {
    runSafe('docker', ['rmi', IMAGE_NAME]);
    p.log.success('Image removed');
  }

  // Optionally stop Colima
  const shouldStopColima = await p.confirm({
    message: 'Stop Colima VM?',
    initialValue: false,
  });
  if (!p.isCancel(shouldStopColima) && shouldStopColima) {
    runSafe('colima', ['stop']);
    p.log.success('Colima stopped');
  }

  p.outro(pc.green('All cleaned up'));
}
