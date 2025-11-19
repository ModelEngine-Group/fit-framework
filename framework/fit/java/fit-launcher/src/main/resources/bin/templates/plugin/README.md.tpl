# {{PROJECT_NAME}}

A FIT Framework Plugin (Service Implementation).

## Project Information

- **Group ID**: {{GROUP_ID}}
- **Artifact ID**: {{ARTIFACT_ID}}
- **Package**: {{PACKAGE}}
- **Service Dependency**: {{SERVICE_GROUP_ID}}:{{SERVICE_ARTIFACT_ID}}
- **FIT Version**: {{FIT_VERSION}}

## Build

```bash
mvn clean install
```

This will:
1. Build the plugin JAR file
2. Package the plugin with all dependencies

## Plugin Structure

```
{{PROJECT_NAME}}/
├── src/main/java/
│   └── {{PACKAGE_PATH}}/
│       └── Default{{SERVICE_NAME}}.java    # 服务实现（使用 @Component 和 @Fitable）
└── src/main/resources/
    └── application.yml                     # FIT 配置文件
```

## Key Annotations

- `@Component` - 标记为 FIT 组件，自动注册到 IoC 容器
- `@Fitable(id = "default-{{SERVICE_ID}}")` - 标记为可远程调用的实现

## Usage

### 1. 部署插件

将构建好的插件 JAR 包部署到 FIT 应用的插件目录：

```bash
cp target/{{ARTIFACT_ID}}-1.0-SNAPSHOT.jar /path/to/fit-app/plugins/
```

### 2. 在应用中使用

首先确保 service 项目已安装到本地仓库或远程仓库。

然后在应用中注入并使用服务：

```java
@Component
public class MyController {

    @FitBean
    private {{SERVICE_NAME}} {{SERVICE_ID}};

    public void usePlugin() {
        String result = {{SERVICE_ID}}.execute();
        System.out.println(result);
    }
}
```

## Documentation

For more information about FIT Framework plugins, visit:
- [FIT Plugin Development Guide](https://github.com/ModelEngine-Group/fit-framework/tree/main/docs)
- [FIT Quick Start Guide](https://github.com/ModelEngine-Group/fit-framework/tree/main/docs)
