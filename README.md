# 终身学习学分银行

终身学习学分银行是一个面向专业实训场景的学分认证与转换系统。项目以前后端分离方式实现，后端基于 Spring Boot，前端基于 Vue 3，并通过 Docker Compose 提供 MySQL、Redis、Nginx 和 Jenkins 等本地基础设施。

系统的核心业务闭环是：学习者提交学习成果认证申请，审核员审核通过后生成学分账户与流水，管理员维护转换规则并指派专家评审，学习者基于已认证成果提交转换申请，审核通过后完成冻结学分扣减、目标学分入账和转换交易记录。

## 功能亮点

- 用户体系：注册、密码登录、验证码登录、密码重置、个人资料、密码修改。
- 权限体系：角色、菜单权限、API 权限、用户角色分配和不同角色菜单隔离。
- 学习成果：成果目录管理、成果导览、认证申请、审核、撤回和批量审核。
- 学分账户：余额、冻结学分、累计学分、学分流水和类型/时间筛选。
- 成果转换：转换规则、专家评审、规则浏览、转换申请、转换审核和交易记录。
- 平台支撑：统计分析、站内信、公告、反馈、系统日志、数据字典和 TOS 文件上传。
- 工程支撑：Docker Compose 本地环境、Nginx 反向代理、Jenkins Pipeline 与构建产物归档。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.2.5、MyBatis-Plus 3.5.6、Spring Security 6、JWT 0.11.5 |
| 数据与缓存 | MySQL 8.0、Redis 7 |
| 工具库 | Hutool、Lombok、MapStruct、Springdoc OpenAPI |
| 前端 | Vue 3、Vite 5、TypeScript、Element Plus、Pinia、Vue Router、ECharts |
| 存储 | 火山引擎对象存储 TOS |
| 基础设施 | Docker Compose、Nginx、Jenkins |

## 环境要求

| 工具 | 建议版本 | 说明 |
| --- | --- | --- |
| JDK | 17+ | 后端运行和 Maven 构建 |
| Maven | 3.8+ | 后端依赖管理与打包 |
| Node.js | 18+ | 前端开发与构建 |
| npm | 9+ | 前端依赖安装 |
| Docker | 24+ | 本地容器环境 |
| Docker Compose | v2+ | 编排 MySQL、Redis、后端、Nginx、Jenkins |

MySQL 和 Redis 可以由 Docker Compose 提供，不需要额外手动安装。

## 快速启动

### 方式一：Docker Compose 启动整套后端环境

适合演示、联调和 Jenkins 截图素材准备。

```powershell
mvn -DskipTests package
$env:TOS_ACCESS_KEY_FILE="C:/Users/zhous/Desktop/develop/tos/AccessKey.txt"
docker compose pull
docker compose up -d --build
```

启动后检查：

```powershell
docker compose ps
curl http://localhost:8080/api/actuator/health
```

### 方式二：本地开发启动后端

如果使用 Docker Compose 中的 MySQL，宿主机端口是 `3307`，需要覆盖 `MYSQL_URL`：

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3307/llcb?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
mvn spring-boot:run
```

如果本机已有 `localhost:3306` 的 MySQL，并且数据库和账号与默认配置一致，可以直接运行：

```powershell
mvn spring-boot:run
```

### 方式三：本地开发启动前端

```powershell
cd credit-bank-frontend
npm install
npm run dev -- --host 127.0.0.1 --port 3000
```

前端 Vite 开发服务器会将 `/api` 请求代理到 `http://localhost:8080`。

## 服务地址

| 服务 | 地址 | 说明 |
| --- | --- | --- |
| 前端开发服务 | `http://127.0.0.1:3000` | Vite dev server |
| 后端 API | `http://localhost:8080/api` | Spring Boot，context path 为 `/api` |
| 后端健康检查 | `http://localhost:8080/api/actuator/health` | 返回 `UP` 表示后端可用 |
| Swagger UI | `http://localhost:8080/api/swagger-ui.html` | 接口文档 |
| OpenAPI JSON | `http://localhost:8080/api/v3/api-docs` | OpenAPI 3 描述 |
| Nginx 网关 | `http://localhost/api` | 代理到后端 `/api` |
| Jenkins | `http://localhost:8081` | 本地 CI |
| MySQL | `localhost:3307` | 宿主机端口，容器内为 `mysql:3306` |
| Redis | `localhost:6379` | 宿主机端口 |

## 默认账号

首次启动会自动初始化管理员账号：

| 用户名 | 密码 | 说明 |
| --- | --- | --- |
| `admin` | `Admin@123456` | 默认管理员账号，可通过环境变量覆盖 |

覆盖示例：

```powershell
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="your-password"
```

## 关键环境变量

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_URL` | `jdbc:mysql://localhost:3306/llcb...` | 后端数据库连接地址 |
| `MYSQL_USER` | `llcb` | 数据库用户名 |
| `MYSQL_PASSWORD` | `llcb123456` | 数据库密码 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `JWT_SECRET` | 开发默认值 | JWT 签名密钥，生产环境必须覆盖 |
| `DATA_CRYPTO_SECRET` | 开发默认值 | 敏感字段加密密钥，生产环境必须覆盖 |
| `TOS_ACCESS_KEY_FILE` | `C:/Users/zhous/Desktop/develop/tos/AccessKey.txt` | TOS 密钥文件路径 |
| `ADMIN_USERNAME` | `admin` | 初始化管理员用户名 |
| `ADMIN_PASSWORD` | `Admin@123456` | 初始化管理员密码 |

不要把 `.env`、AccessKey、JWT 密钥或数据库真实密码提交到仓库。

## TOS 对象存储

项目使用火山引擎 TOS 存储图片和附件，代码不会硬编码 AccessKey。

| 配置项 | 值 |
| --- | --- |
| Bucket | `zhousheng1` |
| Bucket 域名 | `https://zhousheng1.tos-cn-beijing.volces.com` |
| 存储前缀 | `llcb/` |
| 容器内密钥路径 | `/run/secrets/tos_access_key` |

Docker Compose 的后端服务会将 `TOS_ACCESS_KEY_FILE` 指向的本机文件挂载到容器内：

```text
${TOS_ACCESS_KEY_FILE:-./AccessKey.txt}:/run/secrets/tos_access_key:ro
```

建议每次启动前显式设置真实密钥文件路径，避免 Docker 在项目根目录自动创建空的 `AccessKey.txt` 目录：

```powershell
$env:TOS_ACCESS_KEY_FILE="C:/Users/zhous/Desktop/develop/tos/AccessKey.txt"
```

密钥文件支持两种格式：

```text
AccessKeyId=xxx
SecretAccessKey=yyy
```

或前两行分别写 AccessKeyId 和 SecretAccessKey。

## Docker Compose 说明

`docker-compose.yml` 包含以下服务：

| 服务 | 镜像/构建方式 | 持久化 |
| --- | --- | --- |
| `mysql` | `mysql:8.0.33` | `mysql-data:/var/lib/mysql` |
| `redis` | `redis:7.2.4` | `redis-data:/data` |
| `backend` | 项目根目录 `Dockerfile` | 无业务数据卷 |
| `nginx` | `nginx:1.25.5` | 只读挂载 Nginx 配置 |
| `jenkins` | `jenkins/jenkins:2.440.1-lts-jdk17` | `jenkins-home:/var/jenkins_home` |

停止容器但保留数据：

```powershell
docker compose down
```

停止容器并删除 MySQL、Redis、Jenkins 数据卷：

```powershell
docker compose down -v
```

## Jenkins CI

Jenkins 使用官方 LTS 镜像，访问地址为 `http://localhost:8081`，数据保存在 `jenkins-home` volume 中。

项目根目录的 `Jenkinsfile` 包含以下阶段：

| 阶段 | 作用 |
| --- | --- |
| Checkout | 拉取仓库代码并输出最近一次提交 |
| Backend Package | 执行 `mvn -B -DskipTests package` |
| Docker Compose Evidence | 输出 `docker-compose.yml` 配置作为部署证据 |
| Archive Artifact | 归档 `target/*.jar` 构建产物 |

首次初始化 Jenkins 时，可在本机查看容器内初始密码。该密码只用于本地初始化，不应写入仓库或截图素材：

```powershell
docker exec llcb-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

## 常用验证命令

```powershell
# 后端测试
mvn test

# 后端打包
mvn -DskipTests package

# 前端构建
cd credit-bank-frontend
npm run build

# Docker 服务状态
docker compose ps

# 后端健康检查
curl http://localhost:8080/api/actuator/health
```

## 常见问题

### 1. 后端连接不上数据库

如果 MySQL 来自 Docker Compose，请确认后端本地启动时使用的是宿主机端口 `3307`：

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3307/llcb?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
```

### 2. 项目根目录出现空的 `AccessKey.txt` 目录

这是 Docker 在挂载不存在的默认路径 `./AccessKey.txt` 时自动创建的目录。删除该目录，并在启动前设置：

```powershell
$env:TOS_ACCESS_KEY_FILE="C:/Users/zhous/Desktop/develop/tos/AccessKey.txt"
```

### 3. Swagger 地址打不开

请优先检查后端健康状态：

```powershell
curl http://localhost:8080/api/actuator/health
```

健康检查通过后访问：

```text
http://localhost:8080/api/swagger-ui.html
```

### 4. 前端页面请求失败

确认后端已在 `8080` 端口启动，并且前端通过 Vite dev server 访问：

```text
http://127.0.0.1:3000
```

不要直接打开 `dist` 文件或源文件。

## 项目文档与素材

- `数据库结构.md`：数据库表结构说明。
- `功能需求.docx`：功能需求来源文档。
- `documents/`：实训相关过程文档。
- `素材/`：报告截图素材目录，已加入 `.gitignore`，不提交到仓库。

## 提交注意事项

- 不提交 `.env`、日志、构建产物、TOS AccessKey、JWT 密钥或数据库真实密码。
- 不提交 `documents/~$*.docx` 这类 Word 临时锁文件。
- 后端提交前建议运行 `mvn test` 或至少 `mvn -DskipTests package`。
- 前端提交前建议运行 `npm run build`。
