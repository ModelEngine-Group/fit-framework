# FIT Python Nacos Registry 插件

## 简介

FIT Python Nacos Registry 插件是 FIT for Python 框架的服务注册中心插件，提供基于 Alibaba Nacos 的服务注册与发现功能。该插件实现了与 Java 版本兼容的 Nacos 服务协调机制，支持 Python 服务与 Java 服务的跨语言服务发现和调用。

## 重要说明

由于 `fit_py_nacos_registry` 插件的优先级默认高于内存版注册中心，只要该插件存在于 `plugin` 目录中，就会影响内存版注册中心的使用。因此：

### 默认状态
- 插件默认**不启用**，需要手动移动到 `plugin` 目录下才会生效
- 当不使用 Nacos 时，应将插件移动到非 `plugin` 目录，避免影响内存版注册中心

### 插件启用/禁用

#### 启用 Nacos 插件
```bash
# 进入 FIT Python 根目录
cd framework/fit/python

# 将插件从根目录移动到 plugin 目录
mv fit_py_nacos_registry plugin/
echo "Nacos 插件已启用"
```

#### 禁用 Nacos 插件
```bash
# 将插件移动到其他位置（建议移动到 plugin 目录的上级目录）
mv framework/fit/python/plugin/fit_py_nacos_registry framework/fit/python/
```

## 快速开始

### 1. Nacos 部署

#### 本地安装部署

1. 下载 Nacos Server

```bash
# 下载 Nacos 2.3.0
wget https://github.com/alibaba/nacos/releases/download/2.3.0/nacos-server-2.3.0.tar.gz

# 解压
tar -xvf nacos-server-2.3.0.tar.gz
cd nacos/bin
```

2. 启动 Nacos

```bash
# Linux/Mac
sh startup.sh -m standalone

# Windows
startup.cmd -m standalone
```

访问控制台：http://localhost:8848/nacos 

**默认登录凭据**：
- 用户名：`nacos`
- 密码：`nacos`

> ⚠️ **安全提醒**：默认凭据仅适用于开发环境。在生产环境中，请务必修改默认的用户名和密码以确保系统安全。

### 2. 插件配置

#### 依赖安装

确保已安装必要的 Python 依赖：

```bash
pip install nacos-sdk-python
pip install pyyaml
pip install requests
```

#### 配置文件

在 `application.yml` 中配置 Nacos 连接信息：

```yaml
# 基础配置示例
registry-center:
  server:
    mode: 'PROXY'  # 使用代理模式连接 Nacos
    addresses:
      - "localhost:8848"
    protocol: 2
    formats:
      - 1
      - 2

# Nacos 特定配置（可选）
nacos:
  username: 'nacos'          # Nacos 用户名
  password: 'nacos'          # Nacos 密码
  namespace: 'local'         # 命名空间
  weight: 1.0                # 服务权重
  isEphemeral: true          # 是否为临时实例
  heartBeatInterval: 5000    # 心跳间隔（毫秒）
  heartBeatTimeout: 15000    # 心跳超时（毫秒）
  async:
    timeout: 30              # 异步操作超时时间（秒）
```

## 配置参数说明

### 基础配置

| 参数                              | 类型    | 必需 | 默认值 | 说明                                    |
| --------------------------------- | ------- | ---- | ------ | --------------------------------------- |
| `registry-center.server.mode`     | String  | 是   | -      | 注册中心连接模式，使用 'PROXY' 连接 Nacos |
| `registry-center.server.addresses`| List    | 是   | -      | Nacos 服务器地址列表                     |
| `registry-center.server.protocol` | Integer | 是   | -      | 通信协议类型                            |
| `registry-center.server.formats`  | List    | 是   | -      | 支持的数据格式                          |

### Nacos 特定配置

| 参数                        | 类型    | 必需 | 默认值 | 说明                 |
| --------------------------- | ------- | ---- | ------ | -------------------- |
| `nacos.username`            | String  | 否   | -      | Nacos 用户名         |
| `nacos.password`            | String  | 否   | -      | Nacos 密码           |
| `nacos.accessKey`           | String  | 否   | -      | 访问密钥             |
| `nacos.secretKey`           | String  | 否   | -      | 秘密密钥             |
| `nacos.namespace`           | String  | 否   | ""     | 命名空间             |
| `nacos.weight`              | Float   | 否   | 1.0    | 服务权重             |
| `nacos.isEphemeral`         | Boolean | 否   | true   | 是否为临时实例       |
| `nacos.heartBeatInterval`   | Long    | 否   | 5000   | 心跳间隔（毫秒）     |
| `nacos.heartBeatTimeout`    | Long    | 否   | 15000  | 心跳超时（毫秒）     |
| `nacos.async.timeout`       | Integer | 否   | 30     | 异步操作超时（秒）   |

## 插件管理

### 目录结构

```
framework/fit/python/
├── plugin/                          # 活跃插件目录
│   ├── fit_py_nacos_registry/       # Nacos 插件（启用状态）
│   └── other_plugins/               # 其他插件
├── fit_py_nacos_registry/           # Nacos 插件（禁用状态）
└── other_directories/
```

### 管理命令

#### 启用 Nacos 插件

```bash
# 进入 FIT Python 根目录
cd framework/fit/python

# 如果插件在根目录，移动到 plugin 目录
if [ -d "fit_py_nacos_registry" ]; then
    mv fit_py_nacos_registry plugin/
    echo "Nacos 插件已启用"
fi
```

#### 禁用 Nacos 插件

```bash
# 进入 FIT Python 根目录
cd framework/fit/python

# 如果插件在 plugin 目录，移动到根目录
if [ -d "plugin/fit_py_nacos_registry" ]; then
    mv plugin/fit_py_nacos_registry ./
    echo "Nacos 插件已禁用，使用内存版注册中心"
fi
```

### 验证插件状态

#### 检查插件是否启用

```bash
# 检查 Nacos 插件是否在 plugin 目录
if [ -d "framework/fit/python/plugin/fit_py_nacos_registry" ]; then
    echo "✅ Nacos 插件已启用"
else
    echo "❌ Nacos 插件已禁用，使用内存版注册中心"
fi
```

## 使用示例

### 基本服务注册示例

```python
from fitframework import fitable
from fitframework.api.decorators import fit_service

@fit_service(service_id="weather_service")
class WeatherService:
    @fitable(fitable_id="get_weather")
    def get_weather(self, city: str) -> str:
        return f"Weather in {city}: Sunny, 25°C"
```

### 服务发现和调用示例

```python
from fitframework import inject_fit
from fitframework.api.decorators import fit_service

@fit_service(service_id="weather_client")
class WeatherClient:
    def __init__(self):
        # 通过 FIT 框架自动发现并注入远程服务
        self.weather_service = inject_fit("get_weather")
    
    def get_city_weather(self, city: str) -> str:
        # 调用远程服务（可能运行在其他 Python 或 Java 进程中）
        return self.weather_service(city)
```

### 配置文件示例

```yaml
# 完整的应用配置示例
app:
  name: 'Weather Service'

worker:
  id: "weather-service-001"
  protocol-priorities:
    - 'HTTP:JSON'
    - 'HTTP:CBOR'

worker-environment:
  env: 'local'
  env-seq: 'local,dev,test,prod'

http:
  server:
    enabled: true
    address:
      port: 9666
      protocol: 2
      formats: [1, 2]

registry-center:
  server:
    mode: 'PROXY'  # 使用 Nacos 代理模式
    addresses:
      - "localhost:8848"
    protocol: 2
    formats: [1, 2]

nacos:
  username: 'nacos'
  password: 'nacos'
  namespace: 'local'
  weight: 1.0
  isEphemeral: true
  heartBeatInterval: 5000
  heartBeatTimeout: 15000
```