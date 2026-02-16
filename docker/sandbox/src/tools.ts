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
  /** npm package used to install the tool inside sandbox image */
  npmPackage: string;
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
  /** Additional host files to pre-seed into sandbox (e.g. settings, account info) */
  hostPreSeedFiles?: Array<{ hostPath: string; sandboxName: string }>;
  /** Shell commands to run inside the container after setup (e.g. symlink prompts) */
  postSetupCmds?: string[];
  /** Extra environment variables to pass to the container */
  envVars?: Record<string, string>;
  /** Host directories to recursively copy into sandbox (skipped if target already exists) */
  hostPreSeedDirs?: Array<{ hostDir: string; sandboxSubdir: string }>;
  /**
   * Files (relative to sandbox dir) that need host→container path rewriting after copy.
   * Rewrites: HOST_HOME → container home, HOST_PROJECT → /workspace.
   */
  pathRewriteFiles?: string[];
  /**
   * Host files to live-mount into container (bidirectional, always in sync with host).
   * Use for auth tokens that expire and need continuous refresh.
   * Overlays the sandbox directory mount — file-level bind takes precedence.
   */
  hostLiveMounts?: Array<{ hostPath: string; containerSubpath: string }>;
}

/** Validate descriptor consistency at startup — fail fast on misconfiguration. */
function validateTools(tools: readonly Readonly<AiTool>[]): void {
  const names = new Set<string>();
  for (const tool of tools) {
    if (names.has(tool.name)) {
      throw new Error(`AI_TOOLS: duplicate name '${tool.name}'`);
    }
    names.add(tool.name);

    if (!tool.npmPackage || tool.npmPackage.trim().length === 0) {
      throw new Error(`AI_TOOLS[${tool.name}]: npmPackage must be non-empty`);
    }

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
    npmPackage: '@anthropic-ai/claude-code',
    sandboxBase: path.join(HOME, '.claude-sandboxes'),
    containerMount: '/home/devuser/.claude',
    versionCmd: 'claude --version',
    noAuthHint: '首次使用需在容器内运行 claude 完成一次 OAuth 登录，之后免登录。',
    envVars: { CLAUDE_CONFIG_DIR: '/home/devuser/.claude' },
    hostPreSeedDirs: [
      { hostDir: path.join(HOME, '.claude', 'plugins'), sandboxSubdir: 'plugins' },
    ],
    pathRewriteFiles: [
      'plugins/installed_plugins.json',
      'plugins/known_marketplaces.json',
    ],
  },
  {
    name: 'Codex',
    npmPackage: '@openai/codex',
    sandboxBase: path.join(HOME, '.codex-sandboxes'),
    containerMount: '/home/devuser/.codex',
    versionCmd: 'codex --version',
    noAuthHint: '首次使用需在容器内运行 codex，按 Esc 选择 Device Code 方式登录。',
    hostLiveMounts: [
      { hostPath: path.join(HOME, '.codex', 'auth.json'), containerSubpath: 'auth.json' },
    ],
    postSetupCmds: [
      'test -d /workspace/.codex/commands && ln -sfn /workspace/.codex/commands /home/devuser/.codex/prompts || true',
    ],
  },
  {
    name: 'OpenCode',
    npmPackage: 'opencode-ai',
    sandboxBase: path.join(HOME, '.opencode-sandboxes'),
    containerMount: '/home/devuser/.local/share/opencode',
    versionCmd: 'opencode version',
    noAuthHint: '首次使用需在容器内配置认证凭据。',
    hostLiveMounts: [
      { hostPath: path.join(HOME, '.local', 'share', 'opencode', 'auth.json'), containerSubpath: 'auth.json' },
    ],
  },
  {
    name: 'Gemini CLI',
    npmPackage: '@google/gemini-cli',
    sandboxBase: path.join(HOME, '.gemini-sandboxes'),
    containerMount: '/home/devuser/.gemini',
    versionCmd: 'gemini --version',
    noAuthHint: '首次使用需在容器内运行 gemini 完成认证（支持 Google 登录、API Key、Vertex AI）。',
    hostLiveMounts: [
      { hostPath: path.join(HOME, '.gemini', 'oauth_creds.json'), containerSubpath: 'oauth_creds.json' },
    ],
    hostPreSeedFiles: [
      { hostPath: path.join(HOME, '.gemini', 'settings.json'), sandboxName: 'settings.json' },
      { hostPath: path.join(HOME, '.gemini', 'google_accounts.json'), sandboxName: 'google_accounts.json' },
    ],
  },
];

// Fail fast on startup if descriptors are misconfigured
validateTools(AI_TOOLS);

/** Per-branch config directory candidates for a tool (current + legacy naming) */
export function toolConfigDirCandidates(tool: Readonly<AiTool>, branch: string): string[] {
  return safeNameCandidates(branch).map((name) => path.join(tool.sandboxBase, name));
}

/** Build-arg value used by Dockerfile to install all registered AI tools. */
export function toolNpmPackagesArg(): string {
  return AI_TOOLS.map((tool) => tool.npmPackage).join(' ');
}
