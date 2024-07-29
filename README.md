# emq-kafka-integration 项目介绍

## 项目概述

`emq-kafka-integration` 主体是一个基于 Spring 的高级应用支撑组件，该项目通过桥接 EMQ、Kafka 等消息中间件，为高级应用提供自定义或自适应的解决方案，从而实现应用程序的解耦和方便维护。
（如果对你有帮助请给我点个star）
## 项目源码自定义组件介绍

### EMQ 组件

- **功能**：支持多对多服务生产端自定义匹配
- **特点**：灵活配置，满足各种场景的高级应用消息匹配需求

### Exhook 组件

- **方式**：通过 gRPC 方式的 EMQ 钩子组件，将数据传递给外部系统
- **扩展**：也支持 Webhook 方式，通过 RESTful 风格解决数据传递

### Kafka 组件

- **生产消费**：自定义 DB+LB 生产、消费端高级应用
- **监听**：自定义消费端高级应用监听
- **支持**：官方编程式生产消费

### Websocket 组件

- **技术栈**：基于 Spring Websocket 和 Netty 的 Websocket 高级应用支持
- **用途**：为 Websocket 提供了强大的技术支持

## 使用操作流程

1. **Maven 上传私有库**
   - 将项目打包并上传到您的私有 Maven 仓库中。

2. **高级应用引入**
   - 在您的高级应用项目中，添加对 `emq-kafka-integration` 项目的依赖。

3. **配置端配置**
   - 根据您的业务需求，配置 EMQ、Kafka、Websocket 等组件的相关参数。（（注意：请看下我另一个项目的yaml文件即可））

## 开源中间件代理使用

### 主要中间件

- **EMQ X Broker**
  - **简介**：开源的 MQTT 消息代理软件
  - **功能**：实现 MQTT 协议的消息发布/订阅、路由和持久化等

- **Apache Kafka**
  - **简介**：分布式流处理平台
  - **功能**：构建实时数据管道和流应用

- **Websocket**
  - **简介**：基于 netty 框架整合的 Websocket 组件
  - **功能**：构建实时通信及编程式配置化自定义 

## 高级应用运维使用（略）

（后续补充高级应用整体架构和运维使用的详细步骤和注意事项）
