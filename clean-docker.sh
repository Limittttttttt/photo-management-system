#!/bin/bash

echo "========================================"
echo "清理所有Docker容器和镜像"
echo "========================================"
echo ""

echo "停止并删除所有容器..."
docker-compose down -v

echo ""
echo "删除未使用的镜像..."
docker image prune -f

echo ""
echo "清理完成！"

