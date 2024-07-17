### 此项目是一个基于spring的桥接emq和kafka 高级应用支撑组件（如果觉的有意义给个star）
---
###### 闲来无事写了一个支撑组件，欢迎互相学习！
###### 桥接消息组件涉及（emq、kafka、websocket等）可支撑高级应用组件；
###### 功能目标就是希望为高级应用（业务）提供自定义或自适应的解决方案（其实还是为了程序解耦、方便应用维护）  
 

#### 项目组件介绍
 

---

-    ###### emq组件 
-           可支持多对多服务生成端自定义匹配
-    ###### exhook组件 
-           grpc方式的emq钩子组件可为高级应用钩子方式传递数据到外部系统（也支持webhook 这个可以看下emq官网相对比较简单 我也觉得比较不错 rest方式就能解决）
-    ###### kafka组件
-           自定义db+LB生成、消费端高级应用；自定义消费端高级应用监听；官方编程式生产消费支持
-    ###### websocket  
-           springwebsocket+netty方式 websocket 高级应用支持；  

#### 使用操作流程

---
 
 
1. maven 上传私有库 
1. 高级应用引入 
1. 配置端配置 

#### 开源中间件使用 
---
![image](https://github.com/user-attachments/assets/dd5331c3-e232-4a5d-8fc3-d0e7d2e91652)

---

![image](https://github.com/user-attachments/assets/eff6beec-5ae7-4917-9c75-9a62631d23f3)

---
 
#### 高级应用运维使用有空在写 略
