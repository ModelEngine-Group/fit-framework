#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const readline = require('readline');

// 定义版本号
const VERSION = '3.6.1-SNAPSHOT';

// 获取脚本所在的路径
const scriptDir = __dirname;
const currentDir = process.cwd();
const templatesDir = path.join(scriptDir, 'templates');

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
 * 读取模板文件
 */
function readTemplate(templateType, fileName) {
    const templatePath = path.join(templatesDir, templateType, fileName);
    try {
        return fs.readFileSync(templatePath, 'utf-8');
    } catch (err) {
        console.error(`Error reading template ${templatePath}:`, err.message);
        throw err;
    }
}

/**
 * 替换模板变量
 */
function replaceTemplateVars(template, vars) {
    let result = template;
    for (const [key, value] of Object.entries(vars)) {
        const regex = new RegExp(`\\{\\{${key}\\}\\}`, 'g');
        result = result.replace(regex, value);
    }
    return result;
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
    let projectName = args.length > 1 ? args[1] : null;

    // 解析命令行参数
    let projectType = null;
    let groupId = null;
    let artifactId = null;
    let packageName = null;
    let serviceName = null;
    let serviceGroupId = null;
    let serviceArtifactId = null;
    let serviceVersion = null;
    let servicePackage = null;

    for (let i = 2; i < args.length; i++) {
        const arg = args[i];
        if (arg.startsWith('--type=')) {
            projectType = arg.substring('--type='.length);
        } else if (arg.startsWith('--group-id=')) {
            groupId = arg.substring('--group-id='.length);
        } else if (arg.startsWith('--artifact-id=')) {
            artifactId = arg.substring('--artifact-id='.length);
        } else if (arg.startsWith('--package=')) {
            packageName = arg.substring('--package='.length);
        } else if (arg.startsWith('--service=')) {
            serviceName = arg.substring('--service='.length);
        } else if (arg.startsWith('--service-group-id=')) {
            serviceGroupId = arg.substring('--service-group-id='.length);
        } else if (arg.startsWith('--service-artifact-id=')) {
            serviceArtifactId = arg.substring('--service-artifact-id='.length);
        } else if (arg.startsWith('--service-version=')) {
            serviceVersion = arg.substring('--service-version='.length);
        } else if (arg.startsWith('--service-package=')) {
            servicePackage = arg.substring('--service-package='.length);
        }
    }

    // 如果在非交互式环境（如 CI/CD）且缺少必需参数，则报错
    const isInteractive = process.stdin.isTTY;

    if (!isInteractive) {
        if (!projectName) {
            console.error('Error: Project name is required');
            console.error('Usage: fit init <project-name> --type=<service|plugin> [options]');
            process.exit(1);
        }
        if (!projectType || (projectType !== 'service' && projectType !== 'plugin')) {
            console.error('Error: --type must be either "service" or "plugin"');
            process.exit(1);
        }
        if (projectType === 'plugin' && (!serviceGroupId || !serviceArtifactId)) {
            console.error('Error: Plugin type requires --service-group-id and --service-artifact-id');
            process.exit(1);
        }
    }

    // 交互式引导
    if (isInteractive) {
        const rl = readline.createInterface({
            input: process.stdin,
            output: process.stdout
        });

        const question = (prompt) => new Promise((resolve) => {
            rl.question(prompt, resolve);
        });

        try {
            console.log('Welcome to FIT Project Generator!\n');

            // 项目名称
            if (!projectName) {
                projectName = await question('Project name: ');
                if (!projectName) {
                    console.error('Error: Project name is required');
                    rl.close();
                    process.exit(1);
                }
            }

            // 检查目录是否已存在
            const projectDir = path.join(currentDir, projectName);
            if (fs.existsSync(projectDir)) {
                console.error(`Error: Directory '${projectName}' already exists`);
                rl.close();
                process.exit(1);
            }

            // 项目类型
            if (!projectType) {
                console.log('\nProject type:');
                console.log('  1) service  - Service interface (SPI)');
                console.log('  2) plugin   - Plugin implementation');
                const typeChoice = await question('Select type (1 or 2, default: 1): ');
                projectType = typeChoice === '2' ? 'plugin' : 'service';
            }

            // 验证项目类型
            if (projectType !== 'service' && projectType !== 'plugin') {
                console.error('Error: type must be either "service" or "plugin"');
                rl.close();
                process.exit(1);
            }

            console.log(`\nCreating FIT ${projectType === 'service' ? 'Service' : 'Plugin'}: ${projectName}\n`);

            // 通用参数
            const defaultGroupId = 'com.example';
            const inputGroupId = await question(`Group ID (default: ${defaultGroupId}): `);
            groupId = inputGroupId.trim() || defaultGroupId;

            const defaultArtifactId = projectName;
            const inputArtifactId = await question(`Artifact ID (default: ${defaultArtifactId}): `);
            artifactId = inputArtifactId.trim() || defaultArtifactId;

            const defaultPackage = `${groupId}.${artifactId.replace(/-/g, '.')}`;
            const inputPackage = await question(`Package name (default: ${defaultPackage}): `);
            packageName = inputPackage.trim() || defaultPackage;

            const defaultServiceName = 'Demo';
            const inputServiceName = await question(`Service name (default: ${defaultServiceName}): `);
            serviceName = inputServiceName.trim() || defaultServiceName;

            // Plugin 特有参数
            if (projectType === 'plugin') {
                console.log('\nPlugin depends on a service interface:');

                if (!serviceGroupId) {
                    const inputServiceGroupId = await question('Service Group ID: ');
                    serviceGroupId = inputServiceGroupId.trim();
                    if (!serviceGroupId) {
                        console.error('Error: Service Group ID is required for plugin');
                        rl.close();
                        process.exit(1);
                    }
                }

                if (!serviceArtifactId) {
                    const inputServiceArtifactId = await question('Service Artifact ID: ');
                    serviceArtifactId = inputServiceArtifactId.trim();
                    if (!serviceArtifactId) {
                        console.error('Error: Service Artifact ID is required for plugin');
                        rl.close();
                        process.exit(1);
                    }
                }

                const defaultServiceVersion = '1.0-SNAPSHOT';
                const inputServiceVersion = await question(`Service Version (default: ${defaultServiceVersion}): `);
                serviceVersion = inputServiceVersion.trim() || defaultServiceVersion;

                const defaultServicePackage = `${serviceGroupId}.${serviceArtifactId.replace(/-/g, '.')}`;
                const inputServicePackage = await question(`Service Package (default: ${defaultServicePackage}): `);
                servicePackage = inputServicePackage.trim() || defaultServicePackage;
            }

            rl.close();
        } catch (err) {
            rl.close();
            console.error('Error during interactive input:', err.message);
            process.exit(1);
        }
    } else {
        // 非交互式模式，检查目录
        const projectDir = path.join(currentDir, projectName);
        if (fs.existsSync(projectDir)) {
            console.error(`Error: Directory '${projectName}' already exists`);
            process.exit(1);
        }
        console.log(`Creating FIT ${projectType === 'service' ? 'Service' : 'Plugin'}: ${projectName}\n`);
    }

    // 设置默认值（如果用户没有提供）
    groupId = groupId || 'com.example';
    artifactId = artifactId || projectName;
    packageName = packageName || `${groupId}.${artifactId.replace(/-/g, '.')}`;
    serviceName = serviceName || 'Demo';
    serviceVersion = serviceVersion || '1.0-SNAPSHOT';

    // Plugin 的 service package 默认值
    if (projectType === 'plugin' && !servicePackage) {
        servicePackage = `${serviceGroupId}.${serviceArtifactId.replace(/-/g, '.')}`;
    }

    const projectDir = path.join(currentDir, projectName);

    try {
        console.log(`\nGenerating ${projectType} project structure...`);

        // 准备模板变量
        const serviceId = serviceName.toLowerCase();
        const packagePath = packageName.replace(/\./g, '/');
        const year = new Date().getFullYear();

        const templateVars = {
            GROUP_ID: groupId,
            ARTIFACT_ID: artifactId,
            PACKAGE: packageName,
            PACKAGE_PATH: packagePath,
            SERVICE_NAME: serviceName,
            SERVICE_ID: serviceId,
            FIT_VERSION: VERSION,
            YEAR: year.toString(),
            PROJECT_NAME: projectName,
            SERVICE_GROUP_ID: serviceGroupId || '',
            SERVICE_ARTIFACT_ID: serviceArtifactId || '',
            SERVICE_VERSION: serviceVersion || '',
            SERVICE_PACKAGE: servicePackage || ''
        };

        if (projectType === 'service') {
            // 生成 Service 项目
            const srcDir = path.join(projectDir, 'src', 'main', 'java');
            const packageDir = path.join(srcDir, ...packageName.split('.'));

            fs.mkdirSync(packageDir, { recursive: true });

            // 生成 pom.xml
            const pomTemplate = readTemplate('service', 'pom.xml.tpl');
            const pomContent = replaceTemplateVars(pomTemplate, templateVars);
            fs.writeFileSync(path.join(projectDir, 'pom.xml'), pomContent);
            console.log('  ✓ Created pom.xml');

            // 生成服务接口
            const serviceTemplate = readTemplate('service', 'Service.java.tpl');
            const serviceContent = replaceTemplateVars(serviceTemplate, templateVars);
            fs.writeFileSync(path.join(packageDir, `${serviceName}.java`), serviceContent);
            console.log(`  ✓ Created ${serviceName}.java`);

            // 生成 README.md
            const readmeTemplate = readTemplate('service', 'README.md.tpl');
            const readmeContent = replaceTemplateVars(readmeTemplate, templateVars);
            fs.writeFileSync(path.join(projectDir, 'README.md'), readmeContent);
            console.log('  ✓ Created README.md');

            // 生成 .gitignore
            const gitignoreTemplate = readTemplate('plugin', '.gitignore.tpl');
            const gitignoreContent = replaceTemplateVars(gitignoreTemplate, templateVars);
            fs.writeFileSync(path.join(projectDir, '.gitignore'), gitignoreContent);
            console.log('  ✓ Created .gitignore');

        } else if (projectType === 'plugin') {
            // 生成 Plugin 项目
            const srcDir = path.join(projectDir, 'src', 'main', 'java');
            const resourcesDir = path.join(projectDir, 'src', 'main', 'resources');
            const packageDir = path.join(srcDir, ...packageName.split('.'));

            fs.mkdirSync(packageDir, { recursive: true });
            fs.mkdirSync(resourcesDir, { recursive: true });

            // 生成 pom.xml
            const pomTemplate = readTemplate('plugin', 'pom.xml.tpl');
            const pomContent = replaceTemplateVars(pomTemplate, templateVars);
            fs.writeFileSync(path.join(projectDir, 'pom.xml'), pomContent);
            console.log('  ✓ Created pom.xml');

            // 生成服务实现
            const serviceImplTemplate = readTemplate('plugin', 'ServiceImpl.java.tpl');
            const serviceImplContent = replaceTemplateVars(serviceImplTemplate, templateVars);
            fs.writeFileSync(path.join(packageDir, `Default${serviceName}.java`), serviceImplContent);
            console.log(`  ✓ Created Default${serviceName}.java`);

            // 生成 application.yml
            const ymlTemplate = readTemplate('plugin', 'application.yml.tpl');
            const ymlContent = replaceTemplateVars(ymlTemplate, templateVars);
            fs.writeFileSync(path.join(resourcesDir, 'application.yml'), ymlContent);
            console.log('  ✓ Created application.yml');

            // 生成 README.md
            const readmeTemplate = readTemplate('plugin', 'README.md.tpl');
            const readmeContent = replaceTemplateVars(readmeTemplate, templateVars);
            fs.writeFileSync(path.join(projectDir, 'README.md'), readmeContent);
            console.log('  ✓ Created README.md');

            // 生成 .gitignore
            const gitignoreTemplate = readTemplate('plugin', '.gitignore.tpl');
            const gitignoreContent = replaceTemplateVars(gitignoreTemplate, templateVars);
            fs.writeFileSync(path.join(projectDir, '.gitignore'), gitignoreContent);
            console.log('  ✓ Created .gitignore');
        }

        // 显示成功信息
        console.log(`\n✨ FIT ${projectType === 'service' ? 'Service' : 'Plugin'} created successfully!\n`);

        if (projectType === 'service') {
            console.log('Service Structure:');
            console.log(`  ${projectName}/`);
            console.log(`  ├── pom.xml                          # Maven 配置`);
            console.log(`  ├── README.md                        # 服务文档`);
            console.log(`  └── src/main/java/${packagePath}/`);
            console.log(`      └── ${serviceName}.java          # 服务接口（SPI）`);
            console.log('\nNext steps:');
            console.log(`  1. cd ${projectName}`);
            console.log('  2. mvn clean install');
            console.log('  3. Create plugin implementations that depend on this service');
        } else {
            console.log('Plugin Structure:');
            console.log(`  ${projectName}/`);
            console.log(`  ├── pom.xml                          # Maven 配置`);
            console.log(`  ├── README.md                        # 插件文档`);
            console.log(`  └── src/main/`);
            console.log(`      ├── java/${packagePath}/`);
            console.log(`      │   └── Default${serviceName}.java     # 服务实现`);
            console.log(`      └── resources/`);
            console.log(`          └── application.yml          # FIT 配置`);
            console.log('\nNext steps:');
            console.log(`  1. cd ${projectName}`);
            console.log('  2. mvn clean install');
            console.log('  3. Deploy the plugin JAR to your FIT application');
        }

    } catch (err) {
        console.error('Error during initialization:', err.message);
        process.exit(1);
    }
}

/**
 * 显示帮助信息
 */
function help() {
    console.log('Usage: fit <command> [arguments]');
    console.log('\nCommands:');
    console.log('  init [name]  Initialize a new FIT service or plugin project');
    console.log('  start        Start the application');
    console.log('  debug        Start the application in debug mode');
    console.log('  version      Display the version number');
    console.log('  help         Display this help message');
    console.log('\nInit Usage:');
    console.log('  fit init                          # Interactive mode (recommended)');
    console.log('  fit init <name>                   # Interactive mode with project name');
    console.log('  fit init <name> [options]         # Non-interactive mode with options');
    console.log('\nInit Options:');
    console.log('  --type=<service|plugin>           Project type');
    console.log('  --group-id=<id>                   Maven Group ID');
    console.log('  --artifact-id=<id>                Maven Artifact ID');
    console.log('  --package=<name>                  Java package name');
    console.log('  --service=<name>                  Service name');
    console.log('\nPlugin-specific options:');
    console.log('  --service-group-id=<id>           Service Group ID');
    console.log('  --service-artifact-id=<id>        Service Artifact ID');
    console.log('  --service-version=<version>       Service Version');
    console.log('  --service-package=<package>       Service Package');
    console.log('\nExamples:');
    console.log('  # Interactive mode (easiest)');
    console.log('  fit init');
    console.log('  fit init my-project');
    console.log('\n  # Non-interactive mode - Create a service (SPI)');
    console.log('  fit init weather-service --type=service --service=Weather');
    console.log('\n  # Non-interactive mode - Create a plugin implementation');
    console.log('  fit init default-weather --type=plugin --service=Weather \\');
    console.log('      --service-group-id=com.example --service-artifact-id=weather-service');
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
