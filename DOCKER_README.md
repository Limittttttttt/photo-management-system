# Docker 部署说明

本项目使用 Docker Compose 进行容器化部署，包含前端、后端和 MySQL 数据库三个服务。

## 前置要求

- Docker Desktop（Windows/Mac）或 Docker Engine + Docker Compose（Linux）
- 确保 80、3306、8088 端口未被占用

## 快速开始

### 0. 清理旧容器（如果之前有部署过）

**Windows:**
```bash
clean-docker.bat
```

**Linux/Mac:**
```bash
chmod +x clean-docker.sh
./clean-docker.sh
```

或者手动执行：
```bash
docker-compose down -v
docker image prune -f
```

### 1. 构建并启动所有服务

在项目根目录执行：

**Windows:**
```bash
start.bat
```

**Linux/Mac:**
```bash
chmod +x start.sh
./start.sh
```

或者手动执行：
```bash
docker-compose up -d --build
```

这个命令会：
- 构建前端和后端的 Docker 镜像
- 启动 MySQL 数据库并执行初始化脚本
- 启动后端 Spring Boot 服务
- 启动前端 Nginx 服务

### 2. 等待服务启动

首次启动需要等待：
- MySQL 初始化数据库：约 30 秒
- 后端 Spring Boot 启动：约 60-90 秒
- 前端 Nginx：立即启动

**查看服务状态：**
```bash
docker-compose ps
```

**查看后端启动日志（重要）：**
```bash
docker-compose logs -f backend
```

等待看到类似 `Started PicManagerApplication` 的日志，表示后端已启动。

### 3. 访问应用

- **前端地址**: http://localhost
- **后端API**: http://localhost:8088
- **MySQL**: localhost:3306 (用户名: root, 密码: root)

### 4. 查看日志

查看所有服务日志：
```bash
docker-compose logs -f
```

查看特定服务日志：
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### 5. 停止服务

```bash
docker-compose down
```

停止并删除数据卷（会清空数据库数据）：
```bash
docker-compose down -v
```

### 6. 完全清理（重新部署时使用）

**Windows:**
```bash
clean-docker.bat
```

**Linux/Mac:**
```bash
./clean-docker.sh
```

这会：
- 停止并删除所有容器
- 删除数据卷
- 清理未使用的镜像

## 服务说明

### MySQL 服务

- **容器名**: photo-mysql
- **端口**: 3306
- **数据库名**: mydb
- **用户名**: root
- **密码**: root
- **数据持久化**: 使用 Docker volume `mysql_data`
- **初始化脚本**: `mysql/init/init.sql` 和 `mysql/init/data.sql`

### 后端服务

- **容器名**: photo-backend
- **端口**: 8088
- **技术栈**: Spring Boot 4.0.1 + Java 25
- **数据库连接**: 自动连接到 MySQL 容器

### 前端服务

- **容器名**: photo-frontend
- **端口**: 80
- **技术栈**: Vue 3 + Vite + Nginx
- **API代理**: 自动代理 `/api/` 请求到后端

## 数据库初始化

数据库初始化脚本位于 `mysql/init/` 目录：

- `init.sql`: 创建数据库和表结构
- `data.sql`: 插入示例数据（可选）

首次启动时，MySQL 容器会自动执行这些脚本。

## 环境变量配置

可以通过修改 `docker-compose.yml` 中的环境变量来配置服务：

### MySQL 配置
```yaml
environment:
  MYSQL_ROOT_PASSWORD: root  # 修改root密码
  MYSQL_DATABASE: mydb       # 修改数据库名
```

### 后端配置
```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/mydb?...
  SPRING_DATASOURCE_USERNAME: root
  SPRING_DATASOURCE_PASSWORD: root
  SERVER_PORT: 8088
```

## 常见问题

### 1. 端口被占用

如果 80、3306 或 8088 端口被占用，可以修改 `docker-compose.yml` 中的端口映射：

```yaml
ports:
  - "8080:80"      # 前端改为8080端口
  - "3307:3306"    # MySQL改为3307端口
  - "8089:8088"    # 后端改为8089端口
```

### 2. 数据库连接失败

确保 MySQL 容器已完全启动（健康检查通过）。可以查看日志：

```bash
docker-compose logs mysql
```

### 3. 前端无法访问后端API

检查 `frontend/nginx.conf` 中的代理配置，确保后端服务名称为 `backend`。

### 4. 重新构建镜像

如果修改了代码，需要重新构建：

```bash
docker-compose up -d --build
```

或者只重建特定服务：

```bash
docker-compose up -d --build backend
```

### 5. 清除所有数据重新开始

```bash
docker-compose down -v
docker-compose up -d --build
```

## 开发模式

如果需要开发模式（代码热更新），可以：

1. 前端：在 `frontend` 目录运行 `npm run dev`
2. 后端：在 IDE 中运行 Spring Boot 应用
3. 数据库：使用 Docker 中的 MySQL 服务

```bash
# 只启动 MySQL
docker-compose up -d mysql
```

## 生产部署建议

1. 修改默认密码（MySQL root 密码、数据库密码等）
2. 使用环境变量文件（`.env`）管理敏感信息
3. 配置 HTTPS
4. 设置适当的资源限制
5. 配置日志轮转
6. 使用 Docker secrets 管理敏感数据

## 项目结构

```
photo-management-system/
├── docker-compose.yml          # Docker Compose 配置
├── .dockerignore               # Docker 忽略文件
├── DOCKER_README.md           # 本文档
├── mysql/
│   ├── init/
│   │   ├── init.sql           # 数据库初始化脚本
│   │   └── data.sql           # 示例数据脚本
│   └── data/                  # MySQL 数据目录（自动创建）
├── PicManager/                # 后端项目
│   ├── Dockerfile             # 后端 Dockerfile
│   └── .dockerignore
└── frontend/                  # 前端项目
    ├── Dockerfile             # 前端 Dockerfile
    ├── nginx.conf             # Nginx 配置
    └── .dockerignore
```

## 技术支持

如有问题，请检查：
1. Docker 和 Docker Compose 版本是否最新
2. 服务日志是否有错误信息
3. 端口是否被占用
4. 防火墙设置是否正确

