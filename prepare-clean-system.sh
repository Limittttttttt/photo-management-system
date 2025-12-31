#!/bin/bash

echo "========================================"
echo "准备干净系统 - 清理所有数据"
echo "========================================"
echo ""

echo "步骤1: 停止并清理所有Docker资源..."
docker-compose down -v --remove-orphans

echo ""
echo "步骤2: 删除所有相关数据卷..."
docker volume prune -f

echo ""
echo "步骤3: 删除未使用的镜像和网络..."
docker image prune -f
docker network prune -f

echo ""
echo "步骤4: 重新构建镜像（确保使用最新代码）..."
docker-compose build --no-cache

echo ""
echo "========================================"
echo "系统已清理完成！"
echo "现在可以安全地将项目发送给老师"
echo "系统将在下次启动时自动创建干净的数据库"
echo "========================================"