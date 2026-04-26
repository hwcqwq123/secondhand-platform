# 二手交易平台部署说明 README

## 1. 项目简介

本项目是一个基于 **Spring Boot + Vue + MySQL** 的二手交易平台，整体分为三个主要部分：

- `backend`：后端服务，负责登录注册、商品管理、订单管理、文件上传、推荐、管理员接口等功能。
- `frontend`：普通用户前台页面，负责用户浏览商品、发布商品、下单、查看个人信息等功能。
- `admin-frontend`：管理员后台页面，负责管理员登录、查看商品、管理商品等功能。

项目运行时的访问关系如下：

```text
浏览器
  ├─ 用户前台：http://localhost:5173
  ├─ 管理后台：http://localhost:5174
  ↓
Vue 前端通过 /api 代理请求后端
  ↓
Spring Boot 后端：http://localhost:8080
  ↓
MySQL 数据库：localhost:3378/ick7778
```

---

## 2. 当前已修改/确认的数据库配置

本项目原始后端数据库端口是 `3306`，但当前电脑中新版 MySQL 使用的是 `3378` 端口，因此后端配置需要改成连接 `3378`。

配置文件位置：

```text
backend/src/main/resources/application.yml
```

当前应使用的数据库配置如下：

```yml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver

    # 数据库连接地址：
    # localhost:3378 表示连接本机 MySQL 的 3378 端口
    # ick7778 是本项目使用的数据库名称
    # 如果你的 MySQL 端口不是 3378，需要改成自己的端口，例如 3306
    url: jdbc:mysql://localhost:3378/ick7778?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai

    # MySQL 用户名，一般默认是 root
    username: root

    # MySQL 密码，需要改成安装 MySQL 8 时设置的 root 密码
    # 如果密码包含 @、#、:、! 等特殊符号，建议加英文双引号
    password: 你的新MySQL密码

  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false

  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 20MB

app:
  jwt:
    secret: "bWluaS1zZWNvbmQtaGFuZC1wbGF0Zm9ybS1qd3Qtc2VjcmV0LTEyMzQ1Njc4OTAxMjM0NTY3ODkw"
    expires-minutes: 120
```

注意：

```text
password: 你的新MySQL密码
```

这里必须替换成你自己 MySQL 8 的 root 密码，不能原样保留。

---

## 3. 环境要求

本项目需要以下环境：

| 环境 | 建议版本 | 作用 |
|---|---|---|
| JDK | 21 | 运行 Spring Boot 后端 |
| Maven | 3.9.x | 下载后端依赖、启动后端项目 |
| Node.js | 20.x 或以上 | 运行 Vue 前端项目 |
| npm | 10.x 或以上 | 安装前端依赖 |
| MySQL | 5.7 / 8.0 / 8.4 | 存储用户、商品、订单等数据 |

当前电脑已完成的情况：

```text
Maven：已配置成功
Node.js：v20.20.2，可用
npm：10.8.2，可用
MySQL：新版 MySQL 使用 3378 端口
```

检查命令：

```cmd
java -version
javac -version
mvn -version
node -v
npm -v
mysql --version
```

如果 `mysql --version` 仍然显示旧版本，不一定影响项目运行。因为当前新版 MySQL 使用的是 `3378` 端口，连接新版 MySQL 时应使用：

```cmd
mysql -h 127.0.0.1 -P 3378 -u root -p
```

---

## 4. 项目目录结构说明

项目推荐目录结构如下：

```text
secondhand-platform/
├─ backend/                       后端 Spring Boot 项目
│  ├─ pom.xml                     Maven 依赖配置文件
│  └─ src/main/
│     ├─ java/com/example/secondhand/
│     │  ├─ SecondhandApplication.java  后端启动入口
│     │  ├─ admin/                 管理员商品管理接口
│     │  ├─ auth/                  登录、注册、JWT、权限认证
│     │  ├─ behavior/              用户行为记录相关代码
│     │  ├─ config/                全局配置、跨域、静态资源、统一响应
│     │  ├─ file/                  图片/文件上传接口
│     │  ├─ item/                  商品发布、查询、编辑、上下架、删除
│     │  ├─ order/                 订单创建、支付、取消、查询
│     │  ├─ reco/                  推荐商品接口
│     │  └─ user/                  用户资料、收款信息、个人中心
│     └─ resources/
│        └─ application.yml        后端配置文件，包含端口、数据库、JWT 等配置
│
├─ frontend/                       用户前台 Vue 项目
│  ├─ package.json                 前台依赖和启动命令
│  ├─ vite.config.js               前台 Vite 配置，端口 5173，代理后端 8080
│  └─ src/
│     ├─ main.js                   前台入口文件
│     ├─ App.vue                   前台主页面
│     ├─ api.js                    前台接口请求封装
│     └─ components/               前台页面组件
│
├─ admin-frontend/                 管理后台 Vue 项目
│  ├─ package.json                 后台依赖和启动命令
│  ├─ vite.config.js               后台 Vite 配置，端口 5174，代理后端 8080
│  └─ src/
│     ├─ main.js                   管理后台入口文件
│     ├─ App.vue                   管理后台主页面
│     ├─ api.js                    管理后台接口请求封装
│     └─ styles/                   管理后台样式文件
│
├─ uploads/                        上传图片保存目录
│  └─ items/                       商品图片目录
│
└─ ick7778.sql                     数据库初始化脚本
```

---

## 5. 主要文件作用说明

### 5.1 后端核心文件

| 文件/目录 | 作用 |
|---|---|
| `backend/pom.xml` | Maven 项目配置，包含 Spring Boot、JPA、Security、JWT、MySQL 驱动等依赖 |
| `SecondhandApplication.java` | Spring Boot 后端启动入口 |
| `application.yml` | 后端运行配置，包含端口、数据库连接、文件上传大小、JWT 配置 |
| `auth/` | 登录、注册、JWT 生成、JWT 过滤器、权限认证 |
| `item/` | 商品实体、商品接口、商品图片、商品状态、发布和编辑商品请求类 |
| `order/` | 订单实体、订单状态、创建订单、我的订单、出售订单等功能 |
| `user/` | 用户实体、用户资料修改、收款信息修改、个人中心数据 |
| `file/` | 商品图片上传接口 |
| `admin/` | 管理员商品管理接口，例如查看全部商品、删除商品、标记已售 |
| `reco/` | 推荐商品接口 |
| `behavior/` | 用户浏览、行为记录相关功能 |
| `config/` | 跨域配置、静态资源映射、全局异常处理、统一返回格式 |

### 5.2 用户前台核心文件

| 文件/目录 | 作用 |
|---|---|
| `frontend/package.json` | 前台依赖和启动命令，使用 Vue 3 + Vite |
| `frontend/vite.config.js` | 前台端口配置为 `5173`，并把 `/api` 和 `/uploads` 代理到后端 `8080` |
| `frontend/src/main.js` | 前台 Vue 应用入口 |
| `frontend/src/App.vue` | 前台主页面，整合页面逻辑 |
| `frontend/src/api.js` | 前台请求后端接口的统一封装 |
| `frontend/src/components/` | 商品列表、顶部导航、侧边栏、聊天组件等页面组件 |

### 5.3 管理后台核心文件

| 文件/目录 | 作用 |
|---|---|
| `admin-frontend/package.json` | 后台依赖和启动命令，使用 Vue 3 + Vite |
| `admin-frontend/vite.config.js` | 后台端口配置为 `5174`，并把 `/api` 代理到后端 `8080` |
| `admin-frontend/src/main.js` | 管理后台 Vue 应用入口 |
| `admin-frontend/src/App.vue` | 管理后台主页面 |
| `admin-frontend/src/api.js` | 管理后台请求后端接口的统一封装 |

### 5.4 数据库文件

| 文件 | 作用 |
|---|---|
| `ick7778.sql` | 项目数据库初始化脚本，包含用户表、商品表、订单表、商品图片表等结构和初始数据 |

---

## 6. 数据库部署步骤

### 6.1 连接新版 MySQL

由于新版 MySQL 当前使用 `3378` 端口，因此连接命令为：

```cmd
mysql -h 127.0.0.1 -P 3378 -u root -p
```

输入 MySQL 8 的 root 密码后，进入：

```text
mysql>
```

可以执行以下命令确认版本：

```sql
SELECT VERSION();
```

如果显示 `8.0.x` 或更高版本，说明连接的是新版 MySQL。

### 6.2 创建数据库

在 `mysql>` 中执行：

```sql
CREATE DATABASE ick7778 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

查看数据库是否创建成功：

```sql
SHOW DATABASES;
```

看到 `ick7778` 后退出：

```sql
exit;
```

### 6.3 导入 SQL 文件

SQL 文件当前路径为：

```text
E:\secondhand-platform\ick7778.sql
```

在 CMD 中执行：

```cmd
mysql -h 127.0.0.1 -P 3378 -u root -p ick7778 < "E:\secondhand-platform\ick7778.sql"
```

输入 root 密码后，如果没有报错，说明导入成功。

### 6.4 检查表是否导入成功

重新连接 MySQL：

```cmd
mysql -h 127.0.0.1 -P 3378 -u root -p
```

进入后执行：

```sql
USE ick7778;
SHOW TABLES;
```

如果看到商品表、用户表、订单表等内容，说明数据库导入成功。

---

## 7. 启动后端

进入后端目录：

```cmd
cd /d E:\secondhand-platform\backend
```

启动后端：

```cmd
mvn spring-boot:run
```

启动成功后，一般会看到：

```text
Tomcat started on port 8080
Started SecondhandApplication
```

浏览器访问：

```text
http://localhost:8080
```

如果页面显示后端运行提示，说明后端启动成功。

---

## 8. 启动用户前台

新开一个 CMD，进入前台目录：

```cmd
cd /d E:\secondhand-platform\frontend
```

安装依赖：

```cmd
npm install
```

启动前台：

```cmd
npm run dev
```

浏览器访问：

```text
http://localhost:5173
```

这是普通用户前台页面。

---

## 9. 启动管理后台

新开一个 CMD，进入管理后台目录：

```cmd
cd /d E:\secondhand-platform\admin-frontend
```

安装依赖：

```cmd
npm install
```

启动管理后台：

```cmd
npm run dev
```

浏览器访问：

```text
http://localhost:5174
```

这是管理员后台页面。

---

## 10. 推荐启动顺序

每次运行项目时，建议按照以下顺序：

```text
1. 确认 MySQL 8 服务已经启动，端口为 3378
2. 确认数据库 ick7778 已经创建并导入 SQL
3. 启动后端 backend，端口 8080
4. 启动用户前台 frontend，端口 5173
5. 启动管理后台 admin-frontend，端口 5174
```

对应命令：

```cmd
cd /d E:\secondhand-platform\backend
mvn spring-boot:run
```

```cmd
cd /d E:\secondhand-platform\frontend
npm run dev
```

```cmd
cd /d E:\secondhand-platform\admin-frontend
npm run dev
```

---

## 11. 前后端接口代理说明

### 11.1 用户前台代理

`frontend/vite.config.js` 中配置：

```js
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    },
    '/uploads': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

含义：

```text
前台访问 /api 会自动转发到 http://localhost:8080/api
前台访问 /uploads 会自动转发到 http://localhost:8080/uploads
```

### 11.2 管理后台代理

`admin-frontend/vite.config.js` 中配置：

```js
server: {
  port: 5174,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

含义：

```text
管理后台访问 /api 会自动转发到 http://localhost:8080/api
```

---

## 12. 常用接口模块说明

| 接口模块 | 大致功能 |
|---|---|
| `/api/auth/register` | 用户注册 |
| `/api/auth/login` | 用户/管理员登录 |
| `/api/items` | 商品列表、商品发布、商品详情、商品编辑 |
| `/api/items/my` | 我的商品 |
| `/api/orders` | 创建订单 |
| `/api/orders/my` | 我的购买订单 |
| `/api/orders/my-sales` | 我的出售订单 |
| `/api/files/upload` | 图片上传 |
| `/api/recommendations` | 推荐商品 |
| `/api/users/me` | 当前用户个人资料 |
| `/api/admin/items` | 管理员商品管理 |

---

## 13. 常见问题与解决办法

### 13.1 `mvn` 不是内部或外部命令

说明 Maven 没有配置成功。需要确认 Maven 的 `bin` 目录已经加入环境变量 Path。

当前 Maven 真实目录示例：

```text
D:\apache-maven-3.9.15\apache-maven-3.9.15-bin\apache-maven-3.9.15
```

Path 中应包含：

```text
D:\apache-maven-3.9.15\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin
```

检查命令：

```cmd
mvn -version
```

### 13.2 `mysql --version` 还是显示 5.5.20

原因：电脑里同时存在旧 MySQL 5.5 和新 MySQL 8，CMD 默认调用了旧的 MySQL 客户端。

解决办法：连接新版 MySQL 时显式指定端口：

```cmd
mysql -h 127.0.0.1 -P 3378 -u root -p
```

### 13.3 后端连接数据库失败

重点检查 `application.yml`：

```yml
url: jdbc:mysql://localhost:3378/ick7778?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
username: root
password: 你的新MySQL密码
```

常见错误：

```text
端口写成了 3306，但新版 MySQL 实际是 3378
密码没有改成自己的 MySQL 密码
数据库 ick7778 没有创建
SQL 文件没有成功导入
```

### 13.4 端口被占用

检查端口：

```cmd
netstat -ano | findstr :8080
netstat -ano | findstr :5173
netstat -ano | findstr :5174
```

如果显示 `LISTENING`，说明端口被占用。可以关闭占用进程，或者修改对应配置文件中的端口。

### 13.5 前端打开后请求失败

一般是后端没有启动，或者后端端口不是 `8080`。

请先确认：

```text
http://localhost:8080
```

能正常访问，再启动前端。

---

## 14. 项目访问地址

| 模块 | 地址 |
|---|---|
| 后端接口 | http://localhost:8080 |
| 用户前台 | http://localhost:5173 |
| 管理后台 | http://localhost:5174 |
| MySQL 数据库 | localhost:3378/ick7778 |

---

## 15. 一句话部署流程

```text
安装 JDK、Maven、Node、MySQL → 创建 ick7778 数据库 → 导入 ick7778.sql → 修改 application.yml 的数据库端口和密码 → 启动 backend → 启动 frontend → 启动 admin-frontend
```
## 未登录可浏览商品、下单时再弹出登录说明

### 修改目标

本次修改将平台访问逻辑调整为：

1. 未登录用户也可以进入首页浏览已发布商品；
2. 未登录用户可以查看商品详情；
3. 未登录用户点击“下单”时，不会直接请求下单接口，而是先弹出登录/注册窗口；
4. 登录成功后，用户可以继续进行下单、发布商品、查看我的订单、查看我的发布、个人中心等需要登录的操作；
5. 后端同步放开商品列表和商品详情的 GET 接口，保证前端未登录时可以正常加载商品数据。

---
