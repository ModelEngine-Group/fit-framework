# {{PROJECT_NAME}}

A FIT Framework Service (SPI).

## Project Information

- **Group ID**: {{GROUP_ID}}
- **Artifact ID**: {{ARTIFACT_ID}}
- **Package**: {{PACKAGE}}
- **FIT Version**: {{FIT_VERSION}}

## Build

```bash
mvn clean install
```

This will:
1. Build the service JAR file
2. Make it available for plugin implementations

## Service Structure

```
{{PROJECT_NAME}}/
└── src/main/java/
    └── {{PACKAGE_PATH}}/
        └── {{SERVICE_NAME}}.java    # 服务接口（使用 @Genericable）
```

## Key Annotations

- `@Genericable(id = "{{SERVICE_NAME}}")` - 标记服务接口，支持远程和本地调用

## Usage

### 1. 安装到本地仓库

```bash
mvn clean install
```

### 2. 创建插件实现

其他项目可以依赖此服务并创建插件实现：

```xml
<dependency>
    <groupId>{{GROUP_ID}}</groupId>
    <artifactId>{{ARTIFACT_ID}}</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

然后实现 {{SERVICE_NAME}} 接口：

```java
@Component
public class Default{{SERVICE_NAME}} implements {{SERVICE_NAME}} {
    @Override
    @Fitable(id = "default-{{SERVICE_ID}}")
    public String execute() {
        return "Implementation result";
    }
}
```

## Documentation

For more information about FIT Framework services, visit:
- [FIT Service Development Guide](https://github.com/ModelEngine-Group/fit-framework/tree/main/docs)
- [FIT Quick Start Guide](https://github.com/ModelEngine-Group/fit-framework/tree/main/docs)
