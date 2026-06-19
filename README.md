# 终身学习学分银行后端

## 本地构建

```bash
mvn -DskipTests package
```

## Docker 启动

项目提供 `docker-compose.yml`，包含 MySQL、Redis、后端服务、Nginx、Jenkins。

中间件与基础设施镜像均使用官方镜像：

- MySQL：`mysql:8.0.33`
- Redis：`redis:7.2.4`
- Nginx：`nginx:1.25.5`
- Jenkins：`jenkins/jenkins:2.440.1-lts-jdk17`

后端服务仍使用项目根目录下的 `Dockerfile` 构建，镜像直接复制本地 Maven 打包出的 jar，因此启动前需要先打包：

```bash
mvn -DskipTests package
docker compose pull
docker compose up -d --build
```

默认端口：

- 后端直连：`http://localhost:8080/api`
- Nginx 网关：`http://localhost/api`
- Swagger：`http://localhost:8080/api/swagger-ui/index.html`
- Jenkins：`http://localhost:8081`
- MySQL：`localhost:3307`（容器内仍为 `mysql:3306`）
- Redis：`localhost:6379`

## 默认账号

首次启动会自动初始化管理员账号：

- 用户名：`admin`
- 密码：`Admin@123456`

可通过环境变量覆盖：

```bash
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your-password
```

## 火山 TOS 配置

代码不会硬编码 AccessKey。Docker 默认将本机文件挂载到容器：

```text
C:/Users/zhous/Desktop/develop/tos/AccessKey.txt -> /run/secrets/tos_access_key
```

支持文件格式示例：

```text
AccessKeyId=xxx
SecretAccessKey=yyy
```

或前两行分别为 AccessKey、SecretKey。
