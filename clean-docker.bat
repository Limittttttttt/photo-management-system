@echo off
echo ========================================
echo 清理所有Docker容器、镜像和数据卷
echo ========================================
echo.

echo 停止并删除所有容器和数据卷...
docker-compose down -v --remove-orphans

echo.
echo 删除所有相关数据卷...
docker volume prune -f

echo.
echo 删除未使用的镜像...
docker image prune -f

echo.
echo 删除未使用的网络...
docker network prune -f

echo.
echo 清理完成！
echo.
pause

