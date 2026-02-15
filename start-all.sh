#!/bin/bash

BASE_DIR="/Users/blackfruithouse/Documents/yimiao-manage"
LOG_DIR="$BASE_DIR/logs"

mkdir -p $LOG_DIR

echo "Starting Yimiao Vaccine Appointment System..."
echo "=============================================="

pkill -f "yimiao-" 2>/dev/null
sleep 2

echo "[1/6] Starting User Service (port 8083)..."
nohup java -jar $BASE_DIR/yimiao-user/target/yimiao-user-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false \
  --spring.cloud.nacos.config.enabled=false \
  --logging.level.com.yimiao=info \
  > $LOG_DIR/user.log 2>&1 &
echo "User Service PID: $!"
sleep 3

echo "[2/6] Starting Vaccine Service (port 8085)..."
nohup java -jar $BASE_DIR/yimiao-vaccine/target/yimiao-vaccine-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false \
  --spring.cloud.nacos.config.enabled=false \
  --logging.level.com.yimiao=info \
  > $LOG_DIR/vaccine.log 2>&1 &
echo "Vaccine Service PID: $!"
sleep 3

echo "[3/6] Starting Appointment Service (port 8087)..."
nohup java -jar $BASE_DIR/yimiao-appointment/target/yimiao-appointment-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false \
  --spring.cloud.nacos.config.enabled=false \
  --logging.level.com.yimiao=info \
  > $LOG_DIR/appointment.log 2>&1 &
echo "Appointment Service PID: $!"
sleep 3

echo "[4/6] Starting Payment Service (port 8089)..."
nohup java -jar $BASE_DIR/yimiao-payment/target/yimiao-payment-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false \
  --spring.cloud.nacos.config.enabled=false \
  --logging.level.com.yimiao=info \
  > $LOG_DIR/payment.log 2>&1 &
echo "Payment Service PID: $!"
sleep 3

echo "[5/6] Starting Admin Service (port 8081)..."
nohup java -jar $BASE_DIR/yimiao-admin/target/yimiao-admin-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false \
  --spring.cloud.nacos.config.enabled=false \
  --logging.level.com.yimiao=info \
  > $LOG_DIR/admin.log 2>&1 &
echo "Admin Service PID: $!"
sleep 3

echo "[6/6] Starting Gateway Service (port 9000)..."
nohup java -jar $BASE_DIR/yimiao-gateway/target/yimiao-gateway-1.0.0.jar \
  --spring.cloud.nacos.discovery.enabled=false \
  --spring.profiles.active=standalone \
  --logging.level.com.yimiao=info \
  > $LOG_DIR/gateway.log 2>&1 &
echo "Gateway Service PID: $!"
sleep 3

echo ""
echo "=============================================="
echo "All services started!"
echo ""
echo "Services:"
echo "  - Gateway:     http://localhost:9000"
echo "  - User:        http://localhost:8083/doc.html"
echo "  - Vaccine:     http://localhost:8085/doc.html"
echo "  - Appointment: http://localhost:8087/doc.html"
echo "  - Payment:     http://localhost:8089/doc.html"
echo "  - Admin:       http://localhost:8081/doc.html"
echo ""
echo "Logs directory: $LOG_DIR"
echo "To stop all services: pkill -f 'yimiao-'"
