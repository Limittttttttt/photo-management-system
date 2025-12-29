# 照片管理系统 (Photo Management System)

一个基于 Vue 3 + Spring Boot + MySQL 的照片管理系统，支持照片上传、管理、标签、AI搜索等功能。

## 技术栈

- **前端**: Vue 3 + Vite + Element Plus
- **后端**: Spring Boot 4.0.1 + MyBatis Plus
- **数据库**: MySQL 8.0
- **容器化**: Docker + Docker Compose

## 快速开始

### 使用 Docker

这是最简单的部署方式，适合快速启动和演示。

1. **确保已安装 Docker 和 Docker Compose**

2. **启动所有服务**

   Windows:
   ```bash
   start.bat
   ```
   
   Linux/Mac:
   ```bash
   chmod +x start.sh
   ./start.sh
   ```
   
   或手动执行:
   ```bash
   docker-compose up -d --build
   ```

3. **访问应用**

   - 前端: http://localhost
   - 后端API: http://localhost:8088
   - MySQL: localhost:3306 (用户名: root, 密码: root)

4. **查看日志**

   ```bash
   docker-compose logs -f
   ```

5. **停止服务**

   ```bash
   docker-compose down
   ```

详细的 Docker 使用说明请参考 [DOCKER_README.md](./DOCKER_README.md)

### 本地开发

#### 前端开发

```bash
cd frontend
npm install
npm run dev
```

前端将在 http://localhost:5173 启动

#### 后端开发

1. 确保 MySQL 已启动并创建数据库 `mydb`
2. 执行 `mysql/init/init.sql` 初始化数据库
3. 在 IDE 中运行 `PicManagerApplication.java`
4. 后端将在 http://localhost:8088 启动

## 项目结构

```
photo-management-system/
├── frontend/              # Vue 3 前端项目
├── PicManager/           # Spring Boot 后端项目
├── mysql/                # MySQL 初始化脚本
│   └── init/
│       ├── init.sql      # 数据库表结构
│       └── data.sql      # 示例数据
├── docker-compose.yml    # Docker Compose 配置
├── DOCKER_README.md      # Docker 详细说明
└── README.md             # 本文件
```

## 功能特性

- 用户注册/登录
- 照片上传和管理
- 照片标签管理
- AI 智能搜索
- 照片详情查看
- 照片轮播展示

## 注意事项

- 默认数据库密码为 `root`，生产环境请修改
- 首次启动可能需要几分钟来构建 Docker 镜像
- 确保端口 80、3306、8088 未被占用

## 许可证

课程作业项目