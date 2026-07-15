# NoteGather 三服务 DDD 重构方案

## 1. 服务边界

第一阶段保留三个独立部署和独立注册的 Java 服务：

```text
ng-gateway  -> 唯一公网入口、JWT 校验、可信用户上下文注入、服务路由
ng-admin    -> 身份与用户域：注册、登录、Token、用户资料、用户状态
ng-biz      -> 知识资产域：知识库、笔记、文件、解析任务、AI 编排
```

Nacos 服务名固定为 `ng-gateway`、`ng-admin`、`ng-biz`。每个服务可独立扩容；网关路由必须先匹配 `/api/v1/user/** -> ng-admin`，再匹配 `/api/v1/** -> ng-biz`。

## 2. DDD 模板

`ng-admin` 与 `ng-biz` 均采用同一依赖方向，bootstrap 是唯一可运行模块：

```text
<service>/
  <service>-domain/           聚合、实体、值对象、Repository Port、领域规则
  <service>-application/      Command/Query、用例服务、事务编排
  <service>-infrastructure/   MyBatis Repository、Redis、MinIO、MQ、外部客户端
  <service>-adapter/          REST Controller、Dubbo Facade、DTO/Assembler
  <service>-bootstrap/        Spring Boot 启动、Bean 装配、服务私有配置
```

依赖只允许 `adapter -> application -> domain` 与 `infrastructure -> domain`；`bootstrap` 负责组装 adapter、application、infrastructure。应用层只能依赖 Repository Port，不能依赖 Mapper、Redis、MinIO 或 RocketMQ 具体实现。

## 3. 网关安全边界

`JwtAuthFilter` 对非白名单请求执行：Bearer 提取、Access Token 类型/签名/过期校验、Redis JTI 黑名单校验、移除客户端伪造的 `X-User-*` 头、注入可信上下文。下游服务通过 `UserContextFilter` 读取该上下文，并在部署层禁止绕过网关直连内部服务。

## 4. 迁移顺序

1. 将用户域迁入 `ng-admin` 五层包结构，使用 `UserRepository` Port 隔离 MyBatis 实现。
2. 将 `ng-biz` 的知识库、文件、AI 按同一层次迁移；文件解析事件保留在 infrastructure，Controller 和 Dubbo Facade 留在 adapter。
3. 将现有启动模块收敛为各服务 bootstrap，删除旧功能聚合模块。
4. 用 Maven Enforcer/ArchUnit 补充依赖方向验证，并分别启动三个 bootstrap 验证 Nacos 注册与网关路由。

## 5. 当前落地状态（2026-07-15）

- `ng-gateway`、`ng-admin`、`ng-biz` 已作为三个独立 Spring Boot 启动模块和三个 Nacos 服务名保留；网关将 `/api/v1/user/**` 精确路由至 `ng-admin`，其余 `/api/v1/**` 路由至 `ng-biz`。
- `ng-admin` 的用户域已采用 `domain/user`、`application/user`、`infrastructure/persistence/user`、`adapter/web` 与 `bootstrap` 包结构，应用服务通过 `UserRepository` 访问持久化能力。
- `ng-biz` 的知识库和文件模块已采用相同分层。应用服务只依赖 `LibraryRepository`、`NoteRepository`、`FileRepository`、`ParseTaskRepository` 以及对象存储、文件上传事件端口；MyBatis、MinIO 和 RocketMQ 实现均留在 `infrastructure`。
- `module-ai` 当前尚无业务实现，不保留误导性的 Controller、DTO、Service 空包。AI 能力实际接入时按 `adapter -> application -> domain` 与 `infrastructure -> application port` 的方向增加代码，不预建空抽象。
- 配置中的 MySQL、Redis、JWT 和 MinIO 凭据只从环境变量读取，Nacos 中的共享配置和服务私有配置不再携带真实默认密码。
