@echo off
echo ========================================
echo 照片管理系统 - Docker 启动脚本
echo ========================================
echo.

echo 正在清理旧容器...
docker-compose down -v 2>nul

echo.
echo 正在构建并启动 Docker 服务...
docker-compose up -d --build

echo.
echo 等待服务启动（这可能需要1-2分钟）...
timeout /t 5 /nobreak >nul

echo.
echo ========================================
echo 服务启动中，请稍候...
echo ========================================
echo.
echo 前端地址: http://localhost
echo 后端API: http://localhost:8088
echo MySQL: localhost:3306
echo.
echo 查看服务状态: docker-compose ps
echo 查看日志: docker-compose logs -f
echo 查看后端日志: docker-compose logs -f backend
echo 停止服务: docker-compose down
echo.
echo 提示: 首次启动需要等待后端服务完全启动（约1-2分钟）
echo.
pause

