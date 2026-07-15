# NoteGather Nacos 配置说明

## 配置文件

- `common.yaml`：所有 Java 微服务共享配置，包含 Nacos 注册发现、Redis、JWT、Actuator 和公共日志级别。
- `ng-gateway.yaml`：网关私有配置，包含端口、路由、跨域、鉴权白名单和网关日志级别。
- `ng-admin.yaml`：用户与鉴权服务私有配置，包含端口、数据源、Redis 库号、MyBatis-Plus 和 Dubbo。
- `ng-biz.yaml`：知识库、文件和 AI 业务服务私有配置，包含端口、数据源、RocketMQ、Dubbo、MinIO、AI gRPC 和 Druid。

所有运行参数都直接维护在这四份 Nacos 配置中，不再引入 `secrets.yaml`、IDEA 环境变量或额外密钥 Data ID。

## Nacos 分组

- `COMMON_GROUP/common.yaml`
- `DEFAULT_GROUP/ng-gateway.yaml`
- `DEFAULT_GROUP/ng-admin.yaml`
- `DEFAULT_GROUP/ng-biz.yaml`

文件格式选择 `YAML`。

## 加载顺序

服务启动时先读取本地 `application.yml` 定位 Nacos，再加载 `COMMON_GROUP/common.yaml`，最后加载当前服务自己的 `${spring.application.name}.yaml`。

服务私有配置会覆盖共享配置中的同名键。例如 `common.yaml` 里 Redis 默认使用 `database: 0`，`ng-admin.yaml` 可以覆盖为 `database: 1`。

## 发布配置

在 `docs/nacos` 目录执行：

```powershell
.\publish.ps1
```

脚本默认连接：

- Nacos 地址：`10.142.244.123:8848`
- Nacos 用户名：`nacos`
- Nacos 密码：`nacos`
- Namespace：`public`

如需覆盖默认值，可以传参：

```powershell
.\publish.ps1 -NacosAddr "10.142.244.123:8848" -NacosUsername "nacos" -NacosPassword "nacos" -Namespace "public"
```

## 当前 Redis 报错定位

如果启动日志里出现：

```text
params=[${REDIS_PASSWORD}]
```

说明服务实际读取到的远端 `COMMON_GROUP/common.yaml` 仍然是旧配置，Redis 密码占位符没有被覆盖成真实值。

当前本地 `docs/nacos/common.yaml` 已经写死：

```yaml
spring:
  data:
    redis:
      host: 115.190.125.94
      port: 6379
      password: root
```

需要把 `docs/nacos/common.yaml` 覆盖发布到 Nacos 的 `COMMON_GROUP/common.yaml` 后再启动三个服务。

## RocketMQ 注意事项

`ng-biz.yaml` 的 NameServer 固定为 `10.142.244.123:9876`。RocketMQ Broker 还需要在 `broker.conf` 中设置：

```properties
brokerIP1=10.142.244.123
```

否则客户端从 NameServer 获取到的 Broker 地址可能是 Docker 内网地址，导致 Java 或 Python 客户端后续发送消息失败。

还需要确保宿主机防火墙允许开发机访问 `9876`、`10911`、`10912`。

## 服务边界与部署

- `ng-gateway`：统一入口，负责认证、用户上下文注入和路由，Nacos 服务名为 `ng-gateway`。
- `ng-admin`：用户与鉴权服务，Nacos 服务名为 `ng-admin`。
- `ng-biz`：知识库、文件和 AI 业务服务，Nacos 服务名为 `ng-biz`。

三个服务分别构建、分别启动、分别注册到 Nacos。任一服务都可以按自身负载部署多个实例。


M5：客户端
建议先做 Web，再做 Android：
Web 作为第一阶段的主要验收客户端。
登录、知识库树、笔记编辑、文件上传、解析状态、SSE 问答。
Android 等 REST 和 SSE 契约稳定后再接入，避免两个客户端同时放大后端接口变更成本。
需要马上处理的风险
[ng-biz application.yml (line 27)](D:/dev/App/AI-Jarvis/NoteGather/note-gather-server/ng-biz/ng-biz-app/src/main/resources/application.yml:27) 存在远程数据库地址和明文默认密码。
产品文档和技术方案对 OAuth、Milvus/Qdrant、WebFlux/MVC 存在不一致。
Java gRPC 只有运行时依赖，没有 proto 生成链路。
Python 解析服务的 MQ 回调仍未实现。
当前没有测试，后续至少应增加用户服务、目录树、解析状态机、RRF 检索和 SSE 流程测试。
网关 Redis 异常时当前逻辑会降级为仅校验 JWT，生产环境应考虑改为失败关闭。