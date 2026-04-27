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


## 打算修改的逻辑
### 修改目标2  （如果完成了我会自己在后面✔）
一、用户端存在的逻辑漏洞

1. 并发下单问题√
当前系统中，多个用户可能同时点击同一个商品的“下单”按钮。如果后端只是简单判断商品状态是否为 AVAILABLE，然后创建订单，就可能出现多个用户同时认为该商品可以购买的情况。虽然数据库中可以通过订单唯一约束进行一定限制，但更合理的做法是在后端下单接口中加入事务控制和商品行锁，确保同一时间只有一个用户能成功下单。

2. 订单超时逻辑主要依赖前端√
系统前端可以显示订单倒计时，例如创建订单后一定时间内需要支付。但是如果超时判断只放在前端，用户可以通过刷新页面、关闭浏览器或直接调用接口绕过前端倒计时。因此，订单是否超时必须由后端接口再次判断，特别是在支付接口中，需要判断订单是否已经超过有效支付时间。

3. 支付流程过于简单
当前支付逻辑更接近模拟支付，用户点击“我已完成支付”后，系统可能直接将订单状态改为 PAID。真实交易中还应包含卖家确认收款、交易完成、退款或取消等环节。如果没有卖家确认机制，可能出现买家未实际付款但订单已被系统标记为支付完成的情况。（这个算了）

4. 取消订单后的商品状态处理需要明确√
用户取消订单后，商品应该如何处理需要有明确规则。合理情况下，订单取消后商品状态应从 RESERVED 恢复为 AVAILABLE，使其他用户可以继续购买。如果订单取消后商品仍然处于 RESERVED 或订单唯一约束阻止再次下单，就会导致商品无法继续交易。

5. 用户可能购买自己发布的商品√
如果后端没有判断当前登录用户是否为商品发布者，用户可能通过接口购买自己发布的商品。前端隐藏按钮并不安全，因为用户可以绕过前端直接请求后端接口。因此，下单接口中必须判断 buyer_id 和 seller_id 是否相同，如果相同则禁止下单。

6. 未登录状态下的接口访问控制需要后端兜底√
前端可以控制未登录用户点击下单时弹出登录框，但真正的权限控制必须放在后端。否则用户可以绕过前端，直接调用订单、发布商品、我的订单等接口。因此，商品浏览接口可以开放，但下单、发布、修改商品、查看个人订单等接口必须要求登录。

7. 商品状态显示与实际状态可能不同步
用户打开商品详情时看到商品是 AVAILABLE，但在他点击下单之前，商品可能已经被其他用户下单或买走。如果前端不重新获取最新状态，就可能给用户造成“明明显示可买却下单失败”的体验问题。更合理的做法是在下单失败后提示商品已被锁定或售出，并刷新商品详情状态。

8. 图片上传后可能产生无效文件
发布商品时通常是先上传图片，再提交商品信息。如果图片上传成功，但商品发布失败，上传目录中可能留下没有绑定任何商品的图片文件。长期使用后会产生大量无效文件。因此，系统应增加未绑定图片清理机制，或者在发布失败时删除刚上传的图片。

9. 商品删除逻辑可能与订单逻辑冲突
如果商品已经被下单或者已经支付完成，用户仍然可以删除商品，会影响买家的订单查看和交易记录。合理逻辑应该是：有未完成订单的商品不能删除；已完成订单的商品可以隐藏但不应直接物理删除；删除操作应尽量采用逻辑删除。

10. 推荐功能依赖浏览记录但缺少过滤规则
如果推荐功能只根据用户浏览行为推荐商品，可能会推荐已经下架、已删除、已售出或者用户自己发布的商品。更合理的推荐逻辑应该过滤掉不可购买商品、已删除商品、已售商品以及当前用户自己发布的商品。

二、管理员端存在的逻辑漏洞

1. 管理员权限粒度较粗
当前系统通常只区分 USER 和 ADMIN 两类角色。对于简单项目来说可以使用，但在真实平台中，管理员权限应该进一步细分。例如商品审核、用户管理、订单处理、系统配置等功能不一定都应该由同一个管理员角色操作。权限粒度过粗可能导致管理员误操作或权限过大。

2. 管理员账号生成方式不够规范
如果管理员账号需要通过数据库手动修改 role 字段获得，例如把普通用户的 role 改成 ADMIN，这种方式适合开发调试，但不适合作为正式系统逻辑。更合理的做法是系统初始化时创建默认管理员，或者由超级管理员在后台进行角色分配。

3. 后台商品删除缺少业务约束
管理员可以管理或删除商品，但如果商品已经产生订单，直接删除商品可能导致订单详情无法正常显示，也会影响买家和卖家的交易记录。合理做法是管理员删除商品时先判断商品是否有关联订单，如果有关联订单，应采用下架、隐藏或逻辑删除，而不是直接删除。

4. 后台缺少订单管理能力
管理员端如果只能管理商品，而不能查看和处理订单，就无法处理交易纠纷、异常订单、超时订单等问题。对于二手交易平台来说，订单管理是后台的重要部分，后续应增加订单查询、订单状态查看、异常订单处理等功能。

5. 后台缺少用户管理能力
管理员端如果不能查看用户列表、处理违规用户或修改用户状态，就无法对平台用户进行有效管理。例如恶意发布商品、频繁取消订单、发布违规内容等行为，都需要后台具备用户封禁、用户信息查看或用户状态管理功能。

6. 后台缺少操作日志
管理员删除商品、修改商品状态或处理订单时，如果系统不记录操作日志，后续很难追踪是谁在什么时候进行了什么操作。合理的后台管理系统应该记录管理员操作日志，包括操作人、操作时间、操作对象和操作内容。

7. 管理员操作缺少二次确认
删除商品、强制下架、修改订单状态等操作属于高风险操作。如果后台没有二次确认，管理员可能因为误点造成数据异常。因此，重要操作应增加确认弹窗，必要时还可以增加操作原因填写。

8. 后台数据展示不够完整
如果管理员只能看到商品标题、价格和状态，而看不到发布者、发布时间、订单关联状态、删除状态等信息，就不利于判断商品是否违规或是否可以删除。后台商品列表应展示更多管理字段，例如卖家、商品状态、是否删除、是否存在订单、发布时间等。

9. 后台接口必须严格校验 ADMIN 权限
前端后台页面只有管理员能看到并不代表接口安全。管理员相关接口必须在后端强制校验 ADMIN 权限，否则普通用户可能绕过前端直接调用后台接口。也就是说，后台权限控制必须以后端 SecurityConfig 或接口注解为准，不能只依赖前端页面控制。

10. 后台缺少数据统计与审核机制
作为平台管理端，后台除了商品管理，还应该具备一定的数据统计和审核能力。例如商品总数、用户总数、订单总数、已售商品数、违规商品数等。如果没有这些功能，后台更像是简单商品列表，而不是完整的平台管理系统。

### 修改思路之一  加入redis 基于docker容器化部署  方便缓存处理并发事务
## Redis 并发下单优化说明

### 修改目标

本次修改在原有 MySQL 事务和商品行锁的基础上，引入 Redis，用于优化多人同时下单同一商品的并发处理。

修改前，如果多个用户同时点击同一个商品的“下单”按钮，所有请求可能同时进入后端下单逻辑。虽然 MySQL 行锁可以保证最终只有一个用户下单成功，但大量并发请求仍然会进入数据库等待锁。

修改后，系统先使用 Redis 对商品进行前置加锁，再进入 MySQL 事务。这样可以减少同一商品的并发请求直接打到数据库，提高系统稳定性。

---

### Redis 启动方式

本项目使用 Docker 启动 Redis：

```powershell
docker run -d --name redis-secondhand -p 6379:6379 redis:7
如果容器已存在：

docker start redis-secondhand

测试 Redis：

docker exec -it redis-secondhand redis-cli ping

如果返回：PONG说明 Redis 启动成功。

后端 Redis 配置
Redis 配置位于：
backend/src/main/resources/application.yml
配置内容：
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      timeout: 3000ms

新增依赖
在 backend/pom.xml 中新增 Redis 依赖：
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

新增文件
本次新增以下文件：

backend/src/main/java/com/example/secondhand/config/RedisConfig.java
backend/src/main/java/com/example/secondhand/config/RedisTestController.java
backend/src/main/java/com/example/secondhand/order/RedisLockService.java
backend/src/main/java/com/example/secondhand/order/OrderService.java

其中：

RedisConfig.java：配置 StringRedisTemplate
RedisTestController.java：提供 /api/redis/ping 测试接口
RedisLockService.java：封装 Redis 加锁和解锁逻辑
OrderService.java：封装订单事务逻辑

修改文件
本次修改以下文件：

backend/src/main/java/com/example/secondhand/auth/SecurityConfig.java
backend/src/main/java/com/example/secondhand/item/ItemRepository.java
backend/src/main/java/com/example/secondhand/order/OrderRepository.java
backend/src/main/java/com/example/secondhand/order/TradeOrder.java
backend/src/main/java/com/example/secondhand/order/OrderController.java

并发下单流程
修改后的下单流程如下：

1. 用户点击下单；
2. 后端根据商品 id 获取 Redis 锁 lock:item:{itemId}；
3. 如果获取 Redis 锁失败，说明该商品正在被其他用户下单，直接返回失败提示；
4. 如果获取 Redis 锁成功，进入 OrderService 的事务方法；
5. 在 MySQL 中使用悲观写锁锁住商品记录；
6. 判断商品是否存在、是否删除、是否为 AVAILABLE；
7. 判断买家是否为商品发布者；
8. 判断商品是否已有未完成订单；
9. 创建订单；
10. 将商品状态修改为 RESERVED；
11. 提交 MySQL 事务；
12. 释放 Redis 锁。

Redis 和 MySQL 的分工
本次设计中，Redis 和 MySQL 的职责不同：
Redis：用于前置加锁，减少并发请求进入数据库；
MySQL：用于事务控制、商品状态修改和最终一致性保障。

因此，即使 Redis 只是辅助层，最终仍然通过 MySQL 行锁和事务保证同一件商品只能被一个用户下单成功。

订单表唯一索引调整
为了支持“取消订单后商品可以再次被其他用户下单”，需要将 orders.item_id 从唯一索引改为普通索引。

先查看索引：
USE ick7778;
SHOW INDEX FROM orders;
先添加普通索引：
ALTER TABLE orders ADD INDEX idx_orders_item_id (item_id);
再删除原来的唯一索引：
ALTER TABLE orders DROP INDEX UKrg95ddumc8608v80pqy2m43h0;
如果唯一索引名称不同，以 SHOW INDEX FROM orders; 显示的名称为准。

修改后的效果
多个用户同时下单同一个商品时：
第一个用户获取 Redis 锁成功，并进入 MySQL 事务；
其他用户如果同时点击，会因为 Redis 锁存在而直接失败；
如果 Redis 锁释放后再次请求，MySQL 会发现商品已变为 RESERVED，从而拒绝再次下单。

最终效果：同一个商品同一时间只能有一个用户成功下单。


## 用户端部分逻辑问题补充修复说明

### 一、本次修改目标

本次修改主要针对上一轮优化后仍处于“部分解决”状态的用户端逻辑问题进行补充完善，重点修复以下 3 个问题：

1. 商品状态显示与实际状态可能不同步；
2. 商品删除逻辑可能与订单逻辑冲突；
3. 推荐功能依赖浏览记录但缺少完整过滤规则。

修改后，系统在前端下单失败后的状态同步、后端商品删除/下架/重新上架规则、后端推荐结果过滤方面进行了增强。

---

### 二、修改文件

本次修改涉及以下文件：

```text
frontend/src/App.vue
backend/src/main/java/com/example/secondhand/item/ItemController.java
backend/src/main/java/com/example/secondhand/reco/RecommendationController.java