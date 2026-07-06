# AI-Jarvis

面向个人与企业的智能 AI 系统内核。

`AI-Jarvis` 的目标不是只做一个聊天机器人，而是沉淀一套可持续演进的 AI 操作系统能力底座。对个人场景，它强调高自由度、可扩展、可本地化部署；对企业场景，它强调权限、安全、审计、流程接入与组织协同。

当前我们把系统能力收敛为三个最核心的部分：

1. `知识记忆`
2. `执行工具 Agent / Skill`
3. `Function / MCP / 外部能力调用`

---

## 1. 项目定位

AI-Jarvis 是一个面向个人和企业的双栈 AI 系统：

- 面向个人：支持快速搭建自己的知识库、自动化工作流、插件化工具体系。
- 面向企业：支持多租户、权限隔离、审批中断、审计追踪、IM/业务系统集成。
- 面向工程实现：采用 `Java + Python` 协同开发，明确划分治理层与认知执行层。

---

## 2. 三大核心能力

### 2.1 知识记忆

知识记忆负责解决 AI 系统“记不住、记不准、记不久”的问题，核心包括：

- `短期记忆`：当前会话上下文、工作流运行状态、节点中间变量。
- `长期记忆`：用户知识、团队知识、文档知识、代码知识、业务知识。
- `混合检索`：向量检索、关键词检索、图谱检索、结构化元数据过滤。
- `记忆治理`：记忆写入策略、记忆召回策略、租户隔离、权限过滤、审计记录。

目标不是只做一个 RAG，而是形成一套统一记忆层，让工作流、Agent、函数调用都能基于同一份上下文运行。

### 2.2 执行工具 Agent / Skill

Agent / Skill 负责解决 AI 系统“如何思考并执行”的问题，核心包括：

- `工作流编排`：将复杂任务拆为节点、边、状态、条件分支。
- `技能系统`：把可复用的业务能力封装成 Skill，供 Agent 调度。
- `状态执行`：通过图工作流或状态机驱动多步任务。
- `人工介入`：在高风险节点支持审批、中断、恢复、回放。

我们希望 Skill 成为系统内的标准能力单元，既能服务个人自动化，也能服务企业流程化协作。

### 2.3 Function / MCP / 外部能力调用

Function 层负责解决 AI 系统“如何连接外部世界”的问题，核心包括：

- `MCP 接入`：统一接入外部工具、服务、插件和数据源。
- `函数调用`：支持标准 HTTP、内部服务调用、消息回调、任务调度。
- `协议适配`：适配 Web API、数据库、企业 IM、文件系统、第三方 SaaS。
- `安全控制`：参数校验、调用白名单、权限校验、调用审计、超时与重试。

这一层是 AI-Jarvis 的“执行总线”，负责把模型能力落到真实动作。

---

## 3. 设计原则

### 3.1 机制与策略分离

- 内核只负责通用机制：状态流转、记忆访问、工具调度、函数执行。
- 业务策略放在上层：审批、客服、知识助手、代码助手、运营自动化。

### 3.2 Java 与 Python 分层协作

- `Java` 负责企业级治理与系统接入，包括网关、权限、多租户、调度、审计、回调适配。
- `Python` 负责 AI 认知与执行，包括工作流编译、Skill 调度、模型调用、记忆检索、MCP 运行时。

### 3.3 统一状态与统一上下文

- 所有工作流节点共享统一状态对象。
- 记忆、Skill、Function 都从同一上下文读取和写入。
- 支持会话恢复、流程回放、异常补偿和人工续跑。

### 3.4 面向扩展的插件化能力

- 新增一个 Skill，不应影响内核主流程。
- 新增一个 Function / MCP 服务，应可注册后被统一调度。
- 新增一个知识源，应可纳入统一记忆体系。

---

## 4. 系统架构

### 4.1 架构分层

```text
用户 / 企业系统 / IM 平台
        |
        v
Java 接入与治理层
- API Gateway
- Auth / RBAC / Tenant
- Scheduler / Callback / Audit
- Workflow 管理与发布
        |
        v
Python 认知执行层
- LangGraph Workflow Runtime
- Skill Runtime
- LLM Gateway
- Function / MCP Runtime
- Memory Orchestrator
        |
        v
数据与基础设施层
- MySQL
- Redis
- Qdrant
- Neo4j
- Object Storage
```

### 4.2 双引擎职责划分

#### Java 侧

- 统一 API 入口
- 用户、组织、租户、权限体系
- 工作流发布与版本管理
- 定时调度与异步事件驱动
- 飞书、钉钉、企微等企业接口适配
- 审计日志、调用追踪、审批回调

#### Python 侧

- Workflow DSL 解析与执行
- LangGraph 状态机构建
- Prompt 渲染与模型路由
- Memory 检索、写回、压缩与总结
- Skill 注册、加载、执行
- Function / MCP 发现、调用、容错

---

## 5. 技术栈

### 5.1 Java

- `Spring Boot`：核心服务框架
- `Spring Cloud`：微服务协作与配置扩展
- `Spring Security`：认证鉴权
- `MyBatis / JPA`：元数据持久化
- `Quartz`：定时调度
- `WebSocket`：实时会话与流程状态推送

### 5.2 Python

- `FastAPI`：AI 内核 API
- `LangGraph`：工作流状态机
- `LangChain`：模型与工具编排辅助
- `Pydantic`：输入输出结构定义
- `Jinja2`：Prompt 模板渲染

### 5.3 存储与基础设施

- `MySQL 8+`：元数据、工作流定义、会话快照、审计记录
- `Redis 7+`：缓存、分布式锁、运行态状态
- `Qdrant`：向量记忆
- `Neo4j 5+`：图谱记忆、关系检索、代码知识图谱
- `Docker Compose`：本地开发环境编排

---

## 6. 项目结构规划

结合 `Java + Python` 双栈实现，建议项目结构如下：

```text
AI-Jarvis/
├── docs/                                # 设计文档、架构说明、方案沉淀
├── deploy/                              # 部署脚本与环境配置
│   ├── docker-compose.yml
│   └── env/
├── java/                                # Java 服务群：治理、接入、调度
│   ├── jarvis-gateway/                  # 统一 API 网关与请求入口
│   ├── jarvis-auth/                     # 用户、组织、租户、RBAC
│   ├── jarvis-workflow-manager/         # 工作流发布、版本、任务管理
│   ├── jarvis-integration/              # 飞书/钉钉/企微/Webhook 适配
│   └── jarvis-common/                   # 公共模型、工具类、基础组件
├── python/                              # Python 服务群：认知、记忆、执行
│   ├── jarvis-brain/                    # Agent 工作流内核
│   │   ├── app/
│   │   │   ├── api/                     # FastAPI 接口
│   │   │   ├── workflow/                # DSL 解析、图编译、运行时
│   │   │   ├── skills/                  # Skill 注册与执行
│   │   │   ├── functions/               # Function / MCP 调用封装
│   │   │   ├── memory/                  # 检索、写回、记忆治理
│   │   │   ├── llm/                     # 模型路由与统一调用
│   │   │   └── state/                   # Session / State 管理
│   │   ├── tests/
│   │   └── requirements.txt
│   └── jarvis-data-process/             # 文档解析、索引构建、离线任务
├── shared/                              # 跨语言共享协议与配置
│   ├── dsl/                             # 工作流 DSL 定义
│   ├── schemas/                         # JSON Schema / OpenAPI / DTO 协议
│   ├── prompts/                         # 通用 Prompt 模板
│   └── examples/                        # 示例工作流、示例配置
├── scripts/                             # 本地开发脚本、初始化脚本
├── .env.example
├── LICENSE
└── README.md
```

这个结构的重点是：

- `java/` 管治理、接入、调度、权限、组织能力。
- `python/` 管认知执行、记忆编排、Skill 与 Function 运行时。
- `shared/` 负责跨语言共享协议，避免 Java 和 Python 各自定义一套。

---

## 7. MVP 建设方向

第一阶段建议先把最小闭环跑通：

1. `统一工作流 DSL`
2. `Python Workflow Runtime`
3. `Java 网关 + 调度 + 审批回调`
4. `Memory 基础能力`
5. `Skill / Function / MCP 统一调用层`

只要这五部分先闭环，后续无论做个人助手、企业知识库、客服系统还是审批自动化，都可以在同一底座上扩展。

---

## 8. 本地开发环境

### 8.1 示例环境变量

```env
# --- Java / MySQL ---
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ai_jarvis
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# --- Redis ---
REDIS_HOST=localhost
REDIS_PORT=6379

# --- Qdrant ---
QDRANT_HOST=localhost
QDRANT_PORT=6333

# --- Neo4j ---
NEO4J_URI=bolt://localhost:7687
NEO4J_USER=neo4j
NEO4J_PASSWORD=your_password

# --- Python Brain ---
PYTHON_BRAIN_BASE_URL=http://localhost:8000

# --- LLM Router ---
CORE_LLM_PROVIDER=ollama
CORE_LLM_BASE_URL=http://localhost:11434
# CORE_LLM_API_KEY=

# --- Runtime Guard ---
MAX_AGENT_LOOP_STEPS=20
```

### 8.2 启动依赖

```bash
docker compose up -d mysql redis qdrant neo4j
```

---

## 9. 当前文档

- [Agent 工作流概要说明书](./docs/Agent%20%E5%B7%A5%E4%BD%9C%E6%B5%81%E6%A6%82%E8%A6%81%E8%AF%B4%E6%98%8E%E4%B9%A6.md)

---

## 10. 开源协议

当前仓库使用 [LICENSE](./LICENSE) 中定义的开源协议。

如果后续同时提供社区版与商业版，建议把以下边界单独补充到正式授权文档中：

- 社区版可用于个人学习、研究、二次开发与自部署。
- 企业生产使用、闭源分发、商业化打包交付，建议单独定义商业授权条款。
