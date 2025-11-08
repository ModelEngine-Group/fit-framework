#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const readline = require('readline');

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
 * 初始化项目
 */
async function init(args) {
    const projectName = args.length > 1 ? args[1] : null;

    if (!projectName) {
        console.error('Error: Project name is required');
        console.error('Usage: fit init <project-name> [--group-id=<id>] [--artifact-id=<id>] [--package=<name>]');
        process.exit(1);
    }

    // 检查目录是否已存在
    const projectDir = path.join(currentDir, projectName);
    if (fs.existsSync(projectDir)) {
        console.error(`Error: Directory '${projectName}' already exists`);
        process.exit(1);
    }

    console.log(`Creating FIT project: ${projectName}\n`);

    // 解析命令行参数
    let groupId = 'com.example';
    let artifactId = projectName;
    let packageName = null;

    for (let i = 2; i < args.length; i++) {
        const arg = args[i];
        if (arg.startsWith('--group-id=')) {
            groupId = arg.substring('--group-id='.length);
        } else if (arg.startsWith('--artifact-id=')) {
            artifactId = arg.substring('--artifact-id='.length);
        } else if (arg.startsWith('--package=')) {
            packageName = arg.substring('--package='.length);
        }
    }

    // 如果没有指定 package，使用默认值
    if (!packageName) {
        packageName = `${groupId}.${artifactId.replace(/-/g, '.')}`;
    }

    // 如果是交互模式（没有提供参数）
    const isInteractive = args.length === 2;

    if (isInteractive && process.stdin.isTTY) {
        // 交互式获取项目信息
        const rl = readline.createInterface({
            input: process.stdin,
            output: process.stdout
        });

        const question = (prompt) => new Promise((resolve) => {
            rl.question(prompt, resolve);
        });

        try {
            const inputGroupId = await question(`Group ID (default: ${groupId}): `);
            groupId = inputGroupId || groupId;

            const inputArtifactId = await question(`Artifact ID (default: ${artifactId}): `);
            artifactId = inputArtifactId || artifactId;

            const defaultPackage = `${groupId}.${artifactId.replace(/-/g, '.')}`;
            const inputPackage = await question(`Package name (default: ${defaultPackage}): `);
            packageName = inputPackage || defaultPackage;

            rl.close();
        } catch (err) {
            rl.close();
            throw err;
        }
    }

    try {

        console.log('\nGenerating project structure...');

        // 创建项目目录结构
        const srcDir = path.join(projectDir, 'src', 'main', 'java');
        const packageDir = path.join(srcDir, ...packageName.split('.'));
        const controllerDir = path.join(packageDir, 'controller');
        const domainDir = path.join(packageDir, 'domain');

        fs.mkdirSync(controllerDir, { recursive: true });
        fs.mkdirSync(domainDir, { recursive: true });

        // 生成 pom.xml
        const pomContent = generatePomXml(groupId, artifactId);
        fs.writeFileSync(path.join(projectDir, 'pom.xml'), pomContent);
        console.log('  ✓ Created pom.xml');

        // 生成启动类
        const applicationClass = generateApplicationClass(packageName);
        fs.writeFileSync(path.join(packageDir, 'Application.java'), applicationClass);
        console.log('  ✓ Created Application.java');

        // 生成示例 Controller
        const controllerClass = generateControllerClass(packageName);
        fs.writeFileSync(path.join(controllerDir, 'HelloController.java'), controllerClass);
        console.log('  ✓ Created HelloController.java');

        // 生成示例 Domain 类
        const domainClass = generateDomainClass(packageName);
        fs.writeFileSync(path.join(domainDir, 'Message.java'), domainClass);
        console.log('  ✓ Created Message.java');

        // 生成 README.md
        const readmeContent = generateReadme(projectName, groupId, artifactId);
        fs.writeFileSync(path.join(projectDir, 'README.md'), readmeContent);
        console.log('  ✓ Created README.md');

        // 生成 .gitignore
        const gitignoreContent = generateGitignore();
        fs.writeFileSync(path.join(projectDir, '.gitignore'), gitignoreContent);
        console.log('  ✓ Created .gitignore');

        console.log('\n✨ Project created successfully!\n');
        console.log('Next steps:');
        console.log(`  1. cd ${projectName}`);
        console.log('  2. mvn clean install');
        console.log('  3. ./fit start (or java -jar target/*.jar)');
        console.log('\nVisit http://localhost:8080/hello after starting the application.');

    } catch (err) {
        console.error('Error during initialization:', err.message);
        process.exit(1);
    }
}

/**
 * 生成 pom.xml 文件
 */
function generatePomXml(groupId, artifactId) {
    return `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>${groupId}</groupId>
    <artifactId>${artifactId}</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>17</java.version>

        <!-- FIT version -->
        <fit.version>${VERSION}</fit.version>

        <!-- Maven plugin versions -->
        <maven.compiler.version>3.14.0</maven.compiler.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.fitframework</groupId>
            <artifactId>fit-starter</artifactId>
            <version>\${fit.version}</version>
        </dependency>
        <dependency>
            <groupId>org.fitframework</groupId>
            <artifactId>fit-plugins-starter-web</artifactId>
            <version>\${fit.version}</version>
        </dependency>
        <dependency>
            <groupId>org.fitframework</groupId>
            <artifactId>fit-api</artifactId>
            <version>\${fit.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>\${maven.compiler.version}</version>
                <configuration>
                    <source>\${java.version}</source>
                    <target>\${java.version}</target>
                    <encoding>\${project.build.sourceEncoding}</encoding>
                    <compilerArgs>
                        <arg>-parameters</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.fitframework</groupId>
                <artifactId>fit-build-maven-plugin</artifactId>
                <version>\${fit.version}</version>
                <executions>
                    <execution>
                        <id>package-app</id>
                        <goals>
                            <goal>package-app</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
`;
}

/**
 * 生成启动类
 */
function generateApplicationClass(packageName) {
    const year = new Date().getFullYear();
    return `/*---------------------------------------------------------------------------------------------
 *  Copyright (c) ${year} FIT Framework Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package ${packageName};

import modelengine.fitframework.runtime.FitStarter;

/**
 * 应用程序启动类
 */
public class Application {
    public static void main(String[] args) {
        FitStarter.start(Application.class, args);
    }
}
`;
}

/**
 * 生成示例 Controller
 */
function generateControllerClass(packageName) {
    const year = new Date().getFullYear();
    return `/*---------------------------------------------------------------------------------------------
 *  Copyright (c) ${year} FIT Framework Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package ${packageName}.controller;

import ${packageName}.domain.Message;
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.RequestParam;
import modelengine.fitframework.annotation.Component;

/**
 * Hello World 控制器
 */
@Component
public class HelloController {

    @GetMapping(path = "/hello")
    public Message hello(@RequestParam(value = "name", required = false) String name) {
        String greeting = name != null ? "Hello, " + name + "!" : "Hello, World!";
        return new Message(greeting, System.currentTimeMillis());
    }
}
`;
}

/**
 * 生成示例 Domain 类
 */
function generateDomainClass(packageName) {
    const year = new Date().getFullYear();
    return `/*---------------------------------------------------------------------------------------------
 *  Copyright (c) ${year} FIT Framework Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package ${packageName}.domain;

/**
 * 消息领域模型
 */
public class Message {
    private final String content;
    private final long timestamp;

    public Message(String content, long timestamp) {
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
`;
}

/**
 * 生成 README.md
 */
function generateReadme(projectName, groupId, artifactId) {
    return `# ${projectName}

A FIT Framework application.

## Requirements

- Java 17+
- Maven 3.8.8+
- Node.js 12+ (for fit command)

## Build

\`\`\`bash
mvn clean install
\`\`\`

## Run

Using fit command:
\`\`\`bash
./fit start
\`\`\`

Or using Java:
\`\`\`bash
java -jar target/${artifactId}-1.0-SNAPSHOT.jar
\`\`\`

## Test

Visit: http://localhost:8080/hello

With name parameter: http://localhost:8080/hello?name=YourName

## Project Information

- **Group ID**: ${groupId}
- **Artifact ID**: ${artifactId}
- **FIT Version**: ${VERSION}

## Documentation

For more information about FIT Framework, visit:
- [FIT Quick Start Guide](https://github.com/ModelEngine-Group/fit-framework/tree/main/docs)
- [FIT User Guide](https://github.com/ModelEngine-Group/fit-framework/tree/main/docs)
`;
}

/**
 * 生成 .gitignore
 */
function generateGitignore() {
    return `# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties

# IDE
.idea/
*.iml
.vscode/
.eclipse/
.settings/
.classpath
.project

# OS
.DS_Store
Thumbs.db

# Logs
*.log

# Build
build/
dist/
`;
}

/**
 * 显示帮助信息
 */
function help() {
    console.log('Usage: fit <command> [arguments]');
    console.log('Commands:');
    console.log('  init <name>  Initialize a new FIT project');
    console.log('  start        Start the application');
    console.log('  debug        Start the application in debug mode');
    console.log('  version      Display the version number');
    console.log('  help         Display this help message');
}

/**
 * 主函数
 */
async function main() {
    const args = process.argv.slice(2);

    if (args.length === 0) {
        console.error('Unknown command: (no command provided)');
        console.error("Run 'fit help' for usage.");
        process.exit(1);
    }

    const command = args[0];

    switch (command) {
        case 'init':
            await init(args);
            break;
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
