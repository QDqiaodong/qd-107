#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f .env ]; then
    echo "❌ 错误：.env 文件不存在"
    exit 1
fi

source .env

echo "=========================================="
echo "  运动打卡与健身记录空间 - 启动脚本"
echo "=========================================="
echo ""
echo "📋 端口配置："
echo "   - 前端端口: ${FRONTEND_PORT}"
echo "   - 后端端口: ${BACKEND_PORT}"
echo "   - MySQL端口: ${MYSQL_PORT}"
echo "   - Redis端口: ${REDIS_PORT}"
echo ""

echo "🔍 检查端口占用情况..."

check_port() {
    local port=$1
    local name=$2
    if lsof -nP -iTCP:${port} -sTCP:LISTEN > /dev/null 2>&1; then
        echo "❌ 端口 ${port} (${name}) 已被占用："
        lsof -nP -iTCP:${port} -sTCP:LISTEN
        return 1
    else
        echo "✅ 端口 ${port} (${name}) 可用"
        return 0
    fi
}

check_port ${FRONTEND_PORT} "前端" || exit 1
check_port ${BACKEND_PORT} "后端" || exit 1
check_port ${MYSQL_PORT} "MySQL" || exit 1
check_port ${REDIS_PORT} "Redis" || exit 1

echo ""
echo "🚀 开始构建并启动服务..."
echo ""

docker compose up --build -d

echo ""
echo "⏳ 等待服务启动（约30-60秒）..."
echo ""

sleep 10

echo "📊 服务状态："
docker compose ps

echo ""
echo "🔄 等待后端服务就绪..."

MAX_RETRIES=30
RETRY_COUNT=0
BACKEND_READY=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s http://127.0.0.1:${BACKEND_PORT}/api/sport-type/list > /dev/null 2>&1; then
        BACKEND_READY=1
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "   等待中... ($RETRY_COUNT/$MAX_RETRIES)"
    sleep 2
done

echo ""
echo "=========================================="
echo "  ✅ 项目启动完成！"
echo "=========================================="
echo ""
echo "🌐 前端访问地址："
echo "   http://localhost:${FRONTEND_PORT}"
echo "   http://127.0.0.1:${FRONTEND_PORT}"
echo ""
echo "🔌 后端API地址："
echo "   http://localhost:${BACKEND_PORT}"
echo "   http://127.0.0.1:${BACKEND_PORT}"
echo ""
echo "🗄️  数据库连接："
echo "   主机: 127.0.0.1"
echo "   端口: ${MYSQL_PORT}"
echo "   数据库: ${MYSQL_DATABASE}"
echo "   用户名: ${MYSQL_USER}"
echo "   密码: ${MYSQL_PASSWORD}"
echo ""
echo "📝 常用命令："
echo "   查看日志: docker compose logs -f"
echo "   停止服务: docker compose down"
echo "   重启服务: docker compose restart"
echo ""
