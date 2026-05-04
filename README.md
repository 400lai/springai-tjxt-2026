# 天机学堂

天机学堂是一个基于 Spring Cloud 的在线教育微服务平台，提供课程管理、用户认证、交易支付、学习互动等完整的在线教育功能。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.3.5 | 基础框架 |
| Spring Cloud | 2023.0.3 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.3.2 | 阿里巴巴微服务组件 |
| MyBatis Plus | 3.5.9 | ORM 框架 |
| MySQL | 8.0.23 | 数据库 |
| Elasticsearch | 7.12.1 | 搜索引擎 |
| Redisson | 3.13.6 | Redis 客户端 |
| XXL-Job | 2.3.1 | 分布式任务调度 |
| Seata | 1.5.1 | 分布式事务 |
| Knife4j | 4.5.0 | API 文档 |
| Lombok | 1.18.36 | 简化代码 |
| Hutool | 5.8.36 | 工具类库 |

## 项目模块

| 模块 | 端口 | 服务名 | 说明 |
|------|------|--------|------|
| tj-gateway | 10010 | gateway-service | API 网关 |
| tj-auth | 8081 | auth-service | 认证授权服务 |
| tj-user | - | user-service | 用户服务 |
| tj-course | 8086 | course-service | 课程服务 |
| tj-learning | - | learning-service | 学习服务 |
| tj-media | - | media-service | 媒体服务 |
| tj-trade | - | trade-service | 交易服务 |
| tj-pay | - | pay-service | 支付服务 |
| tj-exam | - | exam-service | 考试服务 |
| tj-promotion | - | promotion-service | 营销服务 |
| tj-remark | - | remark-service | 评论服务 |
| tj-search | - | search-service | 搜索服务 |
| tj-message | - | message-service | 消息服务 |
| tj-data | - | data-service | 数据服务 |
| tj-common | - | - | 公共模块 |
| tj-api | - | - | API 接口定义 |

## 项目结构

```
tjxt-ai/
├── tj-api/              # API 接口定义
├── tj-auth/             # 认证授权模块
├── tj-common/           # 公共模块
├── tj-course/           # 课程模块
├── tj-data/             # 数据模块
├── tj-exam/             # 考试模块
├── tj-gateway/          # 网关模块
├── tj-learning/         # 学习模块
├── tj-media/            # 媒体模块
├── tj-message/          # 消息模块
├── tj-pay/              # 支付模块
├── tj-promotion/        # 营销模块
├── tj-remark/           # 评论模块
├── tj-search/           # 搜索模块
├── tj-trade/            # 交易模块
├── tj-user/             # 用户模块
├── pom.xml              # 父 POM
├── Dockerfile           # Docker 构建文件
└── startup.sh           # 启动脚本
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis
- Elasticsearch 7.12.1
- Nacos（服务注册与配置中心）

### 本地运行

1. 克隆项目

```bash
git clone <repository-url>
cd tjxt-ai
```

2. 配置数据库

创建数据库（参考各模块的 `tj.jdbc.database` 配置）：
- tj_auth
- tj_course
- ...

3. 修改配置文件

根据本地环境修改各模块 `src/main/resources/` 下的 `application-local.yml` 配置。

4. 编译项目

```bash
mvn clean install -DskipTests
```

5. 启动服务

按依赖顺序启动各个微服务：
1. 先启动 Nacos
2. 启动 tj-auth
3. 启动其他业务服务
4. 最后启动 tj-gateway

### Docker 部署

项目提供了 Dockerfile 和 startup.sh 用于容器化部署：

```bash
# 使用 startup.sh 脚本部署
./startup.sh -c <container-name> -n <project-name> -d <project-path> -p <port>
```

## 开发指南

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 使用 MyBatis Plus 简化数据库操作
- API 接口使用统一响应格式 `R<T>`

### 接口文档

项目集成了 Knife4j，启动服务后访问：

```
http://localhost:<port>/doc.html
```

### 环境配置

项目支持多环境配置：
- local：本地开发环境
- dev：开发环境
- test：测试环境
- prod：生产环境

通过 `spring.profiles.active` 切换环境。
