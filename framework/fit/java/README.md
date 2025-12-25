## FIT 简介

FIT 函数平台是一套支持多语言融合编程、插件化开发、聚散部署和共享内存的解决方案，旨在提供一种灵活的服务开发范式。
- 多语言融合编程：FIT 在多种编程语言中采用统一的接口描述标准，分别进行实现，并进行相互调用，以达到多语言互相融合协作的目的。
- 插件化开发：FIT 为多种编程语言分别提供了插件开发标准，允许插件在不依赖其他插件的前提下调用其他插件所提供的服务。
- 聚散部署：FIT 为多种编程语言分别提供了运行时环境，允许插件在其运行时环境中既可以单个插件独立进程运行，也可以多个插件共享进程运行。
- 共享内存：FIT 支持插件之间共享内存，即使多个插件分散为多个进程部署，也可以以内存访问的效率来访问共享数据。

## 快速开始

以下是在`Java`中完整的`FIT`应用程序的简单说明。

所需要的环境：

* 编辑器，如 IntelliJ IDEA
* Java 17
* Maven，推荐版本 Maven 3.8.8+
* 对 fitframework 构建：在 framework/fit/java 目录下使用 Maven 对框架进行编译：`mvn clean install`
* 对 您的项目 构建：在您的项目下进行编译：`mvn clean install`
* 内存版注册中心启动：参考 `../registry-center.md`

#### 0. 依赖
在系统生成的 pom 文件中，添加 Web 相关的依赖，如下所示：

``` xml
<dependencies>
    <dependency>
        <groupId>org.fitframework</groupId>
        <artifactId>fit-starter</artifactId>
        <version>${fit.version}</version>
    </dependency>
    <dependency>
        <groupId>org.fitframework</groupId>
        <artifactId>fit-plugins-starter-web</artifactId>
        <version>${fit.version}</version>
    </dependency>
    <dependency>
        <groupId>org.fitframework</groupId>
        <artifactId>fit-api</artifactId>
        <version>${fit.version}</version>
    </dependency>
</dependencies>
```

#### 1. 创建启动器
通过 FitStarter 类的静态方法 start 去启动整个应用，使 FIT 框架对启动类进行识别，示例如下：
``` java
package modelengine.fit.demo1;
 
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.runtime.FitStarter;
 
public class DemoApplication {
    public static void main(String[] args) {
        FitStarter.start(DemoApplication.class, args);
    }
}
```

#### 2. 创建控制器
在该应用程序中，HTTP 请求由控制器处理。我们可以创建相应的控制器和方法来进行处理，示例如下：
``` java
package modelengine.fit.demo1.controller;
 
import modelengine.fit.http.annotation.GetMapping;
import modelengine.fitframework.annotation.Component;
 
/**
 * 控制器。
 */
@Component
public class Controller {
    @GetMapping(path = "/hello")
    public String hello() {
        return "hello world!";
    }
}
```

#### 3. 启动
当前应用启动有两种方式：
- 通过 IDEA 启动：您可以直接在 IDEA 运行 main 方法。
- 通过执行 JAR 文件：您可以使用 Maven 在命令行运行，构建一个包含所有必要依赖项、类和资源的单个可执行 JAR 文件，并运行该文件。

## 发布

进入 FIT for Java 根目录（即当前文件所处目录），执行命令 `mvn clean deploy`。

## 更多内容
### FIT 快速入门指南
- [01.构建基础Web应用](../../../docs/framework/fit/java/quick-start-guide/01.%20构建基础%20Web%20应用.md)
- [02.构建插件式 Web 应用](../../../docs/framework/fit/java/quick-start-guide/02.%20构建插件式%20Web%20应用.md)
- [03.使用插件的热插拔能力](../../../docs/framework/fit/java/quick-start-guide/03.%20使用插件的热插拔能力.md)
- [04.实现应用之间的服务调用](../../../docs/framework/fit/java/quick-start-guide/04.%20实现应用之间的服务调用.md)
- [05.常用注解和配置项](../../../docs/framework/fit/java/quick-start-guide/05.%20常用注解和配置项.md)
- [06.常见问题与解决方案](../../../docs/framework/fit/java/quick-start-guide/06.%20常见问题与解决方案.md)
- [07.工程目录最佳实践](../../../docs/framework/fit/java/quick-start-guide/07.%20工程目录最佳实践.md)

### FIT 用户指导手册

- [01.插件、loC 容器和 Bean](../../../docs/framework/fit/java/user-guide-book/01.%20插件%E3%80%81IoC%20容器和%20Bean.md)
- [02.AOP 编程](../../../docs/framework/fit/java/user-guide-book/02.%20AOP%20编程.md)
- [03.Conf 配置](../../../docs/framework/fit/java/user-guide-book/03.%20Conf%20配置.md)
- [04.Web MVC 能力](../../../docs/framework/fit/java/user-guide-book/04.%20Web%20MVC%20能力.md)
- [05.Genericable 面向接口的远程和本地方法调用能力](../../../docs/framework/fit/java/user-guide-book/05.%20Genericable%20面向接口的远程和本地方法调用能力.md)
- [06.插件化开发](../../../docs/framework/fit/java/user-guide-book/06.%20插件化开发.md)
- [07.数据访问](../../../docs/framework/fit/java/user-guide-book/07.%20数据访问.md)
- [08.日志](../../../docs/framework/fit/java/user-guide-book/08.%20日志.md)
- [09.测试框架](../../../docs/framework/fit/java/user-guide-book/09.%20测试框架.md)
- [10.流式功能](../../../docs/framework/fit/java/user-guide-book/10.%20流式功能.md)
- [11.FIT 框架的类加载](../../../docs/framework/fit/java/user-guide-book/11.%20FIT%20框架的类加载.md)
- [12.附加功能组件](../../../docs/framework/fit/java/user-guide-book/12.%20附加功能组件.md)

### FIT 模块设计

- [工具集设计](fit-util/README.md)
- [API设计](fit-api/README.md)
- [IoC容器设计](fit-ioc/README.md)
- [配置架构设计](fit-conf/README.md)
- [插件架构设计](fit-plugin/README.md)
- [运行时设计](fit-runtime/README.md)
- [启动程序设计](fit-launcher/README.md)
