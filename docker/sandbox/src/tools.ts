import path from 'node:path';
import { safeNameCandidates } from './constants.js';

const HOME = process.env.HOME!;

/**
 * Declarative descriptor for an AI TUI tool supported by the sandbox.
 *
 * To add a new tool, append an entry to {@link AI_TOOLS} — no changes
 * needed in create / rm / ls commands.
 */
export interface AiTool {
  /** Display name shown in CLI output (e.g. "Claude Code") */
  name: string;
  /** Host-side sandbox config root (e.g. ~/.codex-sandboxes) */
  sandboxBase: string;
  /** Mount target inside container (e.g. /home/devuser/.codex) */
  containerMount: string;
  /** Shell command to verify installation (run inside container via bash -lc) */
  versionCmd: string;
  /** Host-side auth file to pre-seed into sandbox (omit if not applicable) */
  hostAuthFile?: string;
  /** Auth file name inside the per-branch sandbox dir (e.g. "auth.json") */
  authFileName?: string;
  /** Hint shown when auth is NOT pre-seeded */
  noAuthHint: string;
  /** Extra environment variables to pass to the container */
  envVars?: Record<string, string>;
}

/** Validate descriptor consistency at startup — fail fast on misconfiguration. */
function validateTools(tools: readonly Readonly<AiTool>[]): void {
  const names = new Set<string>();
  for (const tool of tools) {
    if (names.has(tool.name)) {
      throw new Error(`AI_TOOLS: duplicate name '${tool.name}'`);
    }
    names.add(tool.name);

    if (!tool.containerMount.startsWith('/')) {
      throw new Error(`AI_TOOLS[${tool.name}]: containerMount must be an absolute path`);
    }

    const hasHostAuth = tool.hostAuthFile !== undefined;
    const hasAuthName = tool.authFileName !== undefined;
    if (hasHostAuth !== hasAuthName) {
      throw new Error(`AI_TOOLS[${tool.name}]: hostAuthFile and authFileName must be specified together`);
    }
  }
}

export const AI_TOOLS: readonly Readonly<AiTool>[] = [
  {
    name: 'Claude Code',
    sandboxBase: path.join(HOME, '.claude-sandboxes'),
    containerMount: '/home/devuser/.claude',
    versionCmd: 'claude --version',
    noAuthHint: '首次使用需在容器内运行 claude 完成一次 OAuth 登录，之后免登录。',
    envVars: { CLAUDE_CONFIG_DIR: '/home/devuser/.claude' },
  },
  {
    name: 'Codex',
    sandboxBase: path.join(HOME, '.codex-sandboxes'),
    containerMount: '/home/devuser/.codex',
    versionCmd: 'codex --version',
    hostAuthFile: path.join(HOME, '.codex', 'auth.json'),
    authFileName: 'auth.json',
    noAuthHint: '首次使用需在容器内运行 codex，按 Esc 选择 Device Code 方式登录。',
  },
  {
    name: 'OpenCode',
    sandboxBase: path.join(HOME, '.opencode-sandboxes'),
    containerMount: '/home/devuser/.local/share/opencode',
    versionCmd: 'opencode version',
    hostAuthFile: path.join(HOME, '.local', 'share', 'opencode', 'auth.json'),
    authFileName: 'auth.json',
    noAuthHint: '首次使用需在容器内配置认证凭据。',
  },
];

// Fail fast on startup if descriptors are misconfigured
validateTools(AI_TOOLS);

/** Per-branch config directory candidates for a tool (current + legacy naming) */
export function toolConfigDirCandidates(tool: Readonly<AiTool>, branch: string): string[] {
  return safeNameCandidates(branch).map((name) => path.join(tool.sandboxBase, name));
}
