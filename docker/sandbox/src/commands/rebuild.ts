import * as p from '@clack/prompts';
import pc from 'picocolors';
import { IMAGE_NAME, DOCKERFILE, SCRIPTS_DIR } from '../constants.js';
import { toolNpmPackagesArg } from '../tools.js';
import { run, runSafe, runVerbose } from '../shell.js';

interface RebuildOptions {
  quiet?: boolean;
}

export function rebuild(opts: RebuildOptions) {
  const quiet = opts.quiet ?? false;

  p.intro(pc.cyan('重建 Docker 镜像'));

  // Remove existing image
  if (quiet) {
    const s = p.spinner();
    s.start('Removing old image...');
    runSafe('docker', ['rmi', IMAGE_NAME]);
    s.stop('Old image removed');
  } else {
    p.log.step('Removing old image...');
    runSafe('docker', ['rmi', IMAGE_NAME]);
    p.log.success('Old image removed');
  }

  // Build new image
  const hostUid = run('id', ['-u']);
  const hostGid = run('id', ['-g']);
  const buildArgs = [
    'build', '-t', IMAGE_NAME,
    '--build-arg', `HOST_UID=${hostUid}`,
    '--build-arg', `HOST_GID=${hostGid}`,
    '--build-arg', `AI_TOOL_PACKAGES=${toolNpmPackagesArg()}`,
    '-f', DOCKERFILE, '.',
  ];

  try {
    if (quiet) {
      const s = p.spinner();
      s.start('Building image (this may take a few minutes)...');
      run('docker', buildArgs, { cwd: SCRIPTS_DIR });
      s.stop(pc.green('Image rebuilt successfully'));
    } else {
      p.log.step('Building image...');
      console.log('');
      runVerbose('docker', buildArgs, { cwd: SCRIPTS_DIR });
      console.log('');
      p.log.success(pc.green('Image rebuilt successfully'));
    }
  } catch (e) {
    p.log.error(pc.red('Build failed'));
    throw e;
  }

}
