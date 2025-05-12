# ELSA 简介

## ✨ 功能亮点

### 核心框架 (framework/elsa)

- **统一的抽象数据结构**

    - Graph、Page、Shape结构通用于任意业务场景
    - 全链路JSON序列化能力，兼容任何持久化存储方案
- **拖拽式流程编排**

    - 支持图形拖拽和画布拖拽，并提供节点整理和一键显示所有节点的能力

### 前端应用 (elsa-react)

- **集成React能力**

    - 基于Context的上下文传递
    - 节点渲染缓存：React.memo + 自定义shouldComponentUpdate
- **集成Ant Design能力**

    - 基于Form组件的实时校验提示系统
    - 基于Tree组件封装的节点上下文观察者机制


## 🚀 核心架构概览
### 1. 分层架构

```markdown
├─ fit-elsa-react/    # React前端应用
│  └─ src/            # 前端源代码
└─ fit-elsa/          # 核心框架
   ├─ common/         # 通用工具
   ├─ core/           # 工作流运行时引擎
   └─ plugins/        # 可扩展插件系统
```

### 2. 技术栈

### **@fit-elsa/elsa-core（核心框架模块）**

#### **核心工具链**
| 类别        | 技术栈                                          |
| --------- | -------------------------------------------- |
| **构建工具** | Webpack 5 + Babel 7 + Cross-env（多环境配置）       |
| **代码转换** | `@babel/preset-env`+`core-js@3`（ES6+ 向下兼容） |
| **依赖管理** | npm scripts（支持 daily/debug/pro 多环境构建）        |

#### **关键依赖**
| 类别         | 技术栈                                            |
| ---------- | ---------------------------------------------- |
| **流程可视化** | `@dagrejs/dagre`（图形布局算法） +`echarts@5.4`（图表渲染） |
| **国际化**   | `i18next@21.6`（多语言支持）                          |
| **视频处理**  | `video.js@8.9`（视频播放器集成）                        |

### @fit-elsa/elsa-react（React 前端模块）
#### **核心框架**
| 类别          | 技术栈                                       |
| ----------- | ----------------------------------------- |
| **前端框架**   | React 18 + TypeScript（隐式依赖）               |
| **状态管理**   | 原生 React Hooks（未显式引入 Redux）               |
| **UI 组件库** | Ant Design@4.24 +`@ant-design/icons@5.3` |

#### **开发工具链**
| 类别        | 技术栈                                        |
| --------- | ------------------------------------------ |
| **构建工具** | Vite@5（替代 Webpack） +`vite-plugin-react@4` |
| **代码规范** | ESLint +`react-hooks`/`refresh`插件        |
| **特殊集成** | `vite-plugin-svgr`（SVG 转 React 组件）         |

#### **关键功能依赖**
| 类别            | 技术栈                                                                    |
| ------------- | ---------------------------------------------------------------------- |
| **编辑器**      | `monaco-editor@0.34`（代码编辑器） +`@tinymce/tinymce-react@4.3`（注释节点富文本编辑器） |
| **HTTP 客户端** | `axios@1.8`（API 请求）                                                    |
| **核心依赖**     | `@fit-elsa/elsa-core`（本地路径引用）                                          |

## 快速开始

所需要的环境：

* 编辑器，如 WebStorm
* node 推荐 v20.16.0

## 编译启动

```bash
# 从根目录开始
# 进入fit-elsa目录
cd ./framework/elsa/fit-elsa

# 安装依赖
npm i

# 编译构建
npm run build

# 进入fit-elsa-react目录
cd ../elsa-react

# 安装依赖
npm i

# 编译构建
npm run build

#启动 fit-elsa-react
npm run dev
```