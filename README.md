# 终身学习学分银行后端

## 本地构建

```bash
mvn -DskipTests package
```

## Docker 启动

项目提供 `docker-compose.yml`，包含 MySQL、Redis、后端服务、Nginx、Jenkins。

```bash
mvn -DskipTests package
docker compose up -d --build
```

后端 Docker 镜像直接使用本地 `target/lifelong-learning-credit-bank-0.1.0-SNAPSHOT.jar`，这样构建容器时不需要再联网安装 Maven。

如果 Docker Hub 拉取 Jenkins 报 `UNAUTHORIZED authentication required`，当前配置已经不再拉取 `jenkins/jenkins` 镜像。Jenkins 会通过 [docker/jenkins/Dockerfile](docker/jenkins/Dockerfile) 在本地构建：使用 MCR OpenJDK 基础镜像，并从 Jenkins 官方 WAR 下载地址获取 `2.440.1`。

MySQL 默认使用 `public.ecr.aws/docker/library/mysql:8.0.33` 镜像缓存，避免 Docker Hub 直连失败。Redis 7.2.4、Nginx、Jenkins 使用本地 Dockerfile 构建。需要将 MySQL 切回官方源时可覆盖：

```bash
MYSQL_IMAGE=mysql:8.0.33 docker compose up -d --build
```

默认端口：

- 后端直连：`http://localhost:8080/api`
- Nginx 网关：`http://localhost/api`
- Swagger：`http://localhost:8080/api/swagger-ui.html`
- Jenkins：`http://localhost:8081`
- MySQL：`localhost:3307`（容器内仍为 `mysql:3306`）

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
