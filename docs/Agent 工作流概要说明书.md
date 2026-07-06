# AI-Jarvis Agent 工作流概要说明书

## 1. 文档目标

本文档用于定义 `AI-Jarvis` 第一阶段的 Agent 工作流内核设计，重点回答三个问题：

1. 工作流如何被描述与编排
2. 工作流如何在运行期被执行与恢复
3. 工作流如何连接记忆、Skill 与 Function/MCP

本文档对应的实现目标，是为后续个人助手、企业审批、知识问答、业务自动化提供统一的流程执行底座。

---

## 2. 设计背景

大模型擅长理解、生成与推理，但单轮对话模式难以稳定承接以下场景：

- 多步骤任务拆解
- 带条件分支的业务流程
- 需要调用外部系统的自动化任务
- 需要人工审批或中断恢复的企业流程
- 需要长期记忆与上下文回溯的智能助手

因此，AI-Jarvis 不把 Agent 视为“连续聊天”，而是视为一个可编排、可追踪、可恢复的状态机执行系统。

在技术选型上，第一阶段采用 `LangGraph` 作为底层流程运行时，通过统一 DSL 将节点、边、状态和上下文组织成标准工作流。

---

## 3. 总体设计思路

### 3.1 核心原则

- `统一状态`：所有节点围绕同一个会话状态对象进行读写。
- `统一上下文`：记忆、Skill、Function 调用共享同一份上下文。
- `机制与策略分离`：内核负责执行机制，业务逻辑由上层工作流组合实现。
- `可恢复`：任意一次流程执行都应支持持久化、续跑和回放。
- `可治理`：流程执行必须具备审计、权限控制和人工中断能力。

### 3.2 Java 与 Python 职责

#### Java 侧职责

- 接收外部请求
- 管理用户、租户、权限、审批和审计
- 提供定时调度、消息回调和企业集成
- 管理工作流发布、版本与运行入口

#### Python 侧职责

- 解析工作流 DSL
- 编译为 LangGraph 状态图
- 执行 LLM、Skill、Function、Memory 节点
- 维护运行态状态与会话恢复逻辑

---

## 4. 工作流运行模型

### 4.1 工作流描述方式

前端或配置层输出一份标准 JSON DSL，作为工作流的唯一描述形式。该 DSL 不依赖具体编程语言，用于：

- 描述节点类型
- 描述节点参数
- 描述节点之间的边
- 描述条件分支
- 描述流程入口、出口与中断点

### 4.2 运行时机制

工作流运行时分为三个阶段：

1. `DSL 解析`
2. `状态图编译`
3. `按状态执行与持久化`

整体流程如下：

```text
外部请求 / 定时触发 / 回调信号
        |
        v
Java 发起流程运行请求
        |
        v
Python 读取 Workflow DSL
        |
        v
编译为 LangGraph StateGraph
        |
        v
按节点执行并持续写入 Session State
        |
        +----> 需要人工审批时挂起
        |
        +----> 收到信号后恢复执行
        |
        v
流程完成 / 失败 / 中断
```

### 4.3 无状态执行原则

内核服务本身尽量保持无状态，运行依赖通过外部状态定位：

- `session_id`：定位当前流程实例
- `workflow_version`：定位当前流程定义
- `context`：定位当前租户、用户、环境与权限范围

这样可以天然支持：

- 横向扩容
- 异常恢复
- 会话续跑
- 历史回放

---

## 5. 原生节点类型设计

为了让系统同时适用于个人场景与企业场景，第一阶段建议内置以下原生节点。

### 5.1 `start_node`

#### 作用

工作流入口节点，用于接收初始化输入。

#### 输入来源

- 手动触发
- API 调用
- 定时任务
- Webhook 回调
- 上游流程信号

#### 内核职责

- 校验输入参数是否满足 Schema
- 初始化运行上下文
- 写入初始状态对象

### 5.2 `llm_node`

#### 作用

负责大模型推理、规划、结构化生成和决策。

#### 内核职责

- 读取 Prompt 模板
- 使用变量渲染模板内容
- 调用指定模型或模型网关
- 对输出结果进行结构化校验

#### 典型输出

- 文本
- Markdown
- JSON
- 结构化决策结果

### 5.3 `memory_node`

#### 作用

负责从统一记忆系统中检索、写入或更新知识。

#### 适用场景

- 对话上下文召回
- 企业知识库检索
- 用户画像写入
- 代码知识图谱查询
- 长期记忆归档

#### 内核职责

- 根据上下文拼装检索条件
- 统一调用向量检索、图谱检索、关键词检索
- 将结果写回状态对象
- 根据策略执行记忆写入或压缩

### 5.4 `skill_node`

#### 作用

执行系统内部封装好的 Skill，是 Agent 的标准能力单元。

#### 适用场景

- 文档解析
- 表格处理
- 多步骤业务规则处理
- 摘要、分类、抽取等通用能力

#### 内核职责

- 根据 `skill_id` 加载目标 Skill
- 做入参映射与校验
- 执行 Skill 并收集输出
- 记录执行日志和耗时

### 5.5 `function_node`

#### 作用

调用外部函数、HTTP 服务或 MCP 能力，是系统连接现实世界的执行接口。

#### 适用场景

- 调用第三方 SaaS API
- 调用内部业务系统接口
- 调用 MCP Server 暴露的工具
- 发消息、写工单、改状态、创建任务

#### 内核职责

- 根据协议类型选择调用器
- 拼装请求参数
- 完成鉴权、超时、重试和错误处理
- 把调用结果回写到状态对象

### 5.6 `code_node`

#### 作用

提供可控的代码沙箱，补足流程中的轻量逻辑处理能力。

#### 使用原则

- 只承担轻量转换、清洗、拼装逻辑
- 不建议承担复杂业务主流程
- 需要受限运行、可审计、可超时终止

#### 约定入口

```python
def process(state: dict) -> dict:
    raw_text = state.get("node_1", {}).get("output", "")
    clean_text = raw_text.strip().replace("\n", " ")
    return {"result": clean_text}
```

### 5.7 `switch_node`

#### 作用

负责条件分支控制，决定工作流下一步流向。

#### 内核职责

- 解析条件表达式
- 读取当前状态变量
- 判定分支结果
- 路由到下一个节点

#### 示例表达式

```text
{{variables.intent}} == "approve"
{{variables.score}} > 0.8
{{nodes.review.status}} == "rejected"
```

### 5.8 `end_node`

#### 作用

标记流程正常结束，输出最终结果。

#### 内核职责

- 汇总最终输出
- 标记流程完成状态
- 触发后续通知、日志和审计写入

---

## 6. 工作流 DSL 建议结构

以下是一个简化后的 DSL 示例：

```json
{
  "workflowId": "leave-approval-v1",
  "version": "1.0.0",
  "nodes": [
    {
      "id": "start_1",
      "type": "start_node",
      "config": {
        "inputSchema": {
          "type": "object",
          "required": ["employeeName", "reason"]
        }
      }
    },
    {
      "id": "llm_1",
      "type": "llm_node",
      "config": {
        "promptTemplate": "请分析请假申请：{{input.reason}}",
        "outputParser": "json"
      }
    },
    {
      "id": "switch_1",
      "type": "switch_node",
      "config": {
        "expression": "{{nodes.llm_1.output.riskLevel}} == 'high'"
      }
    },
    {
      "id": "func_1",
      "type": "function_node",
      "config": {
        "protocol": "mcp",
        "functionId": "feishu.send_approval_card"
      }
    },
    {
      "id": "end_1",
      "type": "end_node"
    }
  ],
  "edges": [
    { "from": "start_1", "to": "llm_1" },
    { "from": "llm_1", "to": "switch_1" },
    { "from": "switch_1", "to": "func_1", "when": "true" },
    { "from": "switch_1", "to": "end_1", "when": "false" }
  ]
}
```

这个 DSL 的重点是：

- 节点定义标准化
- 运行参数配置化
- 连接关系显式化
- 支持版本化与回放

---

## 7. 统一状态设计

### 7.1 状态对象

运行期间，全局维护一份统一状态对象：

```python
from typing import Any, TypedDict


class AgentState(TypedDict):
    session_id: str
    workflow_id: str
    workflow_version: str
    tenant_id: str
    user_id: str
    input: dict
    variables: dict
    nodes: dict
    messages: list[Any]
    memory_refs: list[str]
    signals: list[dict]
    status: str
```

### 7.2 状态内容说明

- `input`：流程原始输入
- `variables`：全局变量池
- `nodes`：每个节点的执行结果、状态、耗时、错误信息
- `messages`：与模型交互的消息历史
- `memory_refs`：本次流程关联的记忆引用
- `signals`：中断恢复过程中的外部信号
- `status`：流程状态，例如 `running`、`paused`、`failed`、`completed`

### 7.3 设计价值

统一状态对象可以保证：

- 节点之间的数据传递可追踪
- 会话恢复足够直接
- 历史排障有据可查
- 未来接入前端可视化调试更容易

---

## 8. 持久化设计

### 8.1 会话级快照

每一轮节点执行后，系统应自动保存当前 `AgentState` 快照。

建议保存内容：

- `session_id`
- `workflow_id`
- `workflow_version`
- `state_json`
- `current_node_id`
- `status`
- `updated_at`

### 8.2 节点执行历史

为了支持回放、审计和排障，需要单独记录节点执行历史。

建议记录内容：

- 节点 ID
- 节点类型
- 开始时间与结束时间
- 入参摘要
- 出参摘要
- 错误堆栈
- 重试次数

### 8.3 存储建议

- `MySQL`：会话快照、工作流定义、节点执行历史
- `Redis`：运行中热状态、锁、短期缓存
- `Qdrant / Neo4j`：长期记忆与知识关系

---

## 9. 异步控制与人工介入

这是企业场景中最关键的能力之一。

### 9.1 定时触发

#### 策略

Java 侧通过 `Quartz` 或 `Spring Task` 触发流程运行。

#### 流程

```text
定时器触发
  -> Java 生成运行请求
  -> Python 启动 start_node
  -> 工作流按图执行
  -> 输出结果或进入中断状态
```

### 9.2 人工审批中断

#### 场景

- 财务审批
- 代码发布
- 合同审核
- 高风险外部操作

#### 内核行为

1. 流程运行到人工审核节点
2. 内核将当前状态持久化
3. 流程状态改为 `paused`
4. 向 Java 层抛出等待信号
5. Java 适配企业 IM，推送审批卡片
6. 外部用户点击审批按钮
7. Java 将审批结果转成恢复信号
8. Python 内核反序列化会话并继续执行

### 9.3 恢复信号示例

```json
{
  "sessionId": "sess_20260706_001",
  "action": "approve",
  "operatorId": "manager_001",
  "comment": "同意执行"
}
```

---

## 10. 错误处理与治理要求

### 10.1 基本要求

- 节点失败要可定位
- 支持有限次数重试
- 支持失败后中断或补偿
- 高风险操作必须可审计

### 10.2 建议治理能力

- 参数校验
- 租户隔离
- 权限校验
- 调用超时
- 幂等控制
- 审计日志
- Trace / Span 链路追踪

---

## 11. 第一阶段实现边界

为了保证第一阶段可落地，建议先实现最小集合：

### 11.1 必做能力

- `start_node`
- `llm_node`
- `memory_node`
- `function_node`
- `switch_node`
- `end_node`
- `Session 持久化`
- `人工中断与恢复`

### 11.2 第二优先级

- `skill_node`
- `code_node`
- 可视化工作流编辑器
- 工作流版本对比
- 流程回放界面

---

## 12. 与系统三大核心的关系

第一部分工作流内核，本质上是三大核心能力的编排层：

- `知识记忆` 通过 `memory_node` 接入
- `Agent / Skill` 通过 `llm_node` 与 `skill_node` 承载
- `Function / MCP` 通过 `function_node` 承载

也就是说，工作流并不是第四套系统，而是把三大能力真正组织起来、运行起来的执行框架。

---

## 13. 总结

AI-Jarvis 的 Agent 工作流内核，不是单纯的“流程图执行器”，而是一套面向 AI 系统的统一运行框架。它需要同时具备：

- AI 推理能力
- 状态机执行能力
- 记忆接入能力
- 外部函数调用能力
- 企业级治理与中断恢复能力

第一阶段只要把这套最小内核做扎实，后续无论是个人助手、团队知识助理，还是企业审批与自动化系统，都可以在同一个底座上持续演进。
