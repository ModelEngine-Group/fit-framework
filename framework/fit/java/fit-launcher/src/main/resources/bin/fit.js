#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');

// 定义版本号
const VERSION = '3.5.5-SNAPSHOT';

// 获取脚本所在的路径
const scriptDir = __dirname;
const currentDir = process.cwd();

/**
 * 查找符合 fit-discrete-launcher-[version].jar 格式的文件
 */
function findJarFile(directory) {
    try {
        const files = fs.readdirSync(directory);
        const jarFile = files.find(file =>
            file.startsWith('fit-discrete-launcher-') && file.endsWith('.jar')
        );
        return jarFile || null;
    } catch (err) {
        console.error(`Error reading directory ${directory}:`, err.message);
        return null;
    }
}

/**
 * 启动应用
 */
function start(args) {
    const isDebugMode = args.length > 0 && args[0] === 'debug';

    // 初始化参数数组
    const javaArgs = [];
    const programArgs = [];

    // 如果是 debug 模式，添加 debug 参数
    if (isDebugMode) {
        javaArgs.push('-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005');
    }

    // 跳过第一个参数（start 或 debug）
    const remainingArgs = args.slice(1);

    // 分离 Java 参数和程序参数
    for (const arg of remainingArgs) {
        if (arg.startsWith('-')) {
            javaArgs.push(arg);
        } else {
            programArgs.push(arg);
        }
    }

    // 切换到脚本所在目录的上级目录
    const jarDir = path.join(scriptDir, '..');

    // 查找 JAR 文件
    const jarFile = findJarFile(jarDir);

    if (!jarFile) {
        console.error('No fit-discrete-launcher-[version].jar file found.');
        process.exit(1);
    }

    // 构造 Java 命令参数
    const javaCommand = 'java';
    const commandArgs = [
        ...javaArgs,
        `-Dsun.io.useCanonCaches=true`,
        `-Djdk.tls.client.enableSessionTicketExtension=false`,
        `-Dplugin.fit.dynamic.plugin.directory=${currentDir}`,
        '-jar',
        path.join(jarDir, jarFile),
        ...programArgs
    ];

    // 打印运行命令
    console.log(`Running command: ${javaCommand} ${commandArgs.join(' ')}`);

    // 执行 Java 命令
    const javaProcess = spawn(javaCommand, commandArgs, {
        stdio: 'inherit',
        cwd: jarDir
    });

    // 处理进程退出
    javaProcess.on('exit', (code) => {
        process.exit(code || 0);
    });

    // 处理错误
    javaProcess.on('error', (err) => {
        console.error('Failed to start Java process:', err.message);
        process.exit(1);
    });

    // 处理 SIGINT 和 SIGTERM 信号
    process.on('SIGINT', () => {
        javaProcess.kill('SIGINT');
    });

    process.on('SIGTERM', () => {
        javaProcess.kill('SIGTERM');
    });
}

/**
 * 显示版本号
 */
function version() {
    console.log(`Version: ${VERSION}`);
}

/**
 * 显示帮助信息
 */
function help() {
    console.log('Usage: fit <command> [arguments]');
    console.log('Commands:');
    console.log('  start    Start the application');
    console.log('  debug    Start the application in debug mode');
    console.log('  version  Display the version number');
    console.log('  help     Display this help message');
}

/**
 * 主函数
 */
function main() {
    const args = process.argv.slice(2);

    if (args.length === 0) {
        console.error('Unknown command: (no command provided)');
        console.error("Run 'fit help' for usage.");
        process.exit(1);
    }

    const command = args[0];

    switch (command) {
        case 'start':
            start(args);
            break;
        case 'debug':
            start(args);
            break;
        case 'version':
            version();
            break;
        case 'help':
            help();
            break;
        default:
            console.error(`Unknown command: ${command}`);
            console.error("Run 'fit help' for usage.");
            process.exit(1);
    }
}

// 执行主函数
main();
