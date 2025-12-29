#!/bin/bash

echo "========================================"
echo "照片管理系统 - Docker 启动脚本"
echo "========================================"
echo ""

echo "正在启动 Docker 服务..."
docker-compose up -d --build

echo ""
echo "等待服务启动..."
sleep 10

echo ""
echo "========================================"
echo "服务启动完成！"
echo "========================================"
echo ""
echo "前端地址: http://localhost"
echo "后端API: http://localhost:8088"
echo "MySQL: localhost:3306"
echo ""
echo "查看日志: docker-compose logs -f"
echo "停止服务: docker-compose down"
echo ""

