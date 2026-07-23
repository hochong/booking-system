#!/usr/bin/env bash
# One-shot startup for the whole booking system: Kafka + DynamoDB Local via
# docker compose, then booking-api, booking-processor, and the frontend dev
# server, all in the background with their output tee'd to logs/. Ctrl+C
# stops everything, including the java/node processes Maven and npm spawn
# underneath - a plain `kill` on the wrapper process alone would leave those
# running.
set -euo pipefail # euo means e - exit on error, u - unset variable, or o - failed pipe command
cd "$(dirname "${BASH_SOURCE[0]}")"
source scripts/common.sh

if ! command -v node >/dev/null 2>&1 || ! command -v npm >/dev/null 2>&1; then # check if node js and npm are installed
  echo "Node.js/npm not found. Run ./install.sh first." >&2
  exit 1
fi

mkdir -p logs
PIDS=()
CLEANED_UP=0

# Kills a background job's PID and everything it spawned. On Windows/Git Bash,
# `kill $pid` only signals the MSYS wrapper process - the actual java.exe or
# node.exe it launched keeps running - so resolve the real Windows PID via
# /proc and use `taskkill /T` to kill the whole tree instead. Falls back to a
# plain POSIX kill on other platforms (or if that lookup fails).
kill_tree() {
  local pid="$1"
  if [[ -r "/proc/$pid/winpid" ]] && command -v taskkill >/dev/null 2>&1; then
    local winpid
    winpid="$(cat "/proc/$pid/winpid" 2>/dev/null || true)"
    if [[ -n "$winpid" ]] && taskkill //PID "$winpid" //T //F >/dev/null 2>&1; then
      return
    fi
  fi
  kill -TERM "$pid" 2>/dev/null || true
}

# Self-healing safety net: the Ctrl+C cleanup above only works if this script's
# own trap actually gets to run (closing the terminal window, an IDE's "kill
# terminal" button, or a crash can all bypass it). Rather than relying on that
# being perfect, reclaim our known ports from any leftover process on startup
# too, so a bad previous shutdown can never again block this run with "port
# already in use" - each ./run.sh becomes idempotent regardless of how the
# last one ended.
free_port() {
  local port="$1"
  if [[ "$(detect_os)" == "windows" ]]; then
    local pid
    for pid in $(netstat -ano 2>/dev/null | grep ":$port " | grep LISTENING | awk '{print $NF}' | sort -u); do
      echo "  Port $port was still in use by leftover process $pid - stopping it"
      taskkill //PID "$pid" //F >/dev/null 2>&1 || true
    done
  else
    local pid
    for pid in $(lsof -ti "tcp:$port" 2>/dev/null || true); do
      echo "  Port $port was still in use by leftover process $pid - stopping it"
      kill -9 "$pid" 2>/dev/null || true
    done
  fi
}

echo "==> Checking for leftover processes from a previous run"
for port in 8080 8090 8095 5173; do
  free_port "$port"
done

cleanup() {
  [[ "$CLEANED_UP" -eq 1 ]] && return
  CLEANED_UP=1
  echo ""
  echo "==> Stopping services..."
  for pid in "${PIDS[@]:-}"; do
    [[ -n "$pid" ]] && kill_tree "$pid"
  done
  sleep 1
  for pid in "${PIDS[@]:-}"; do
    kill -9 "$pid" 2>/dev/null || true
  done
}
trap cleanup EXIT INT TERM

echo "==> Starting Kafka + DynamoDB Local (docker compose)"
docker compose up -d

echo "==> Starting booking-api (logs/booking-api.log)"
(cd services/booking-api && mvn -q spring-boot:run) > logs/booking-api.log 2>&1 &
PIDS+=("$!")

echo "==> Starting booking-processor (logs/booking-processor.log)"
(cd services/booking-processor && mvn -q spring-boot:run) > logs/booking-processor.log 2>&1 &
PIDS+=("$!")

echo "==> Starting notification-service (logs/notification-service.log)"
(cd services/notification-service && mvn -q spring-boot:run) > logs/notification-service.log 2>&1 &
PIDS+=("$!")

echo "==> Starting frontend (logs/frontend.log)"
(cd frontend && npm run dev) > logs/frontend.log 2>&1 &
PIDS+=("$!")

cat <<'EOF'

All services starting:
  frontend             http://localhost:5173
  booking-api          http://localhost:8080
  booking-processor    http://localhost:8090  (no public API - background consumer)
  notification-service http://localhost:8095  (no public API - background consumer)
  kafka-ui             http://localhost:8081
  dynamodb-admin       http://localhost:8002
  mailpit (sent email) http://localhost:8025

Tailing logs/*.log below. Press Ctrl+C to stop everything.
EOF

tail -f logs/booking-api.log logs/booking-processor.log logs/notification-service.log logs/frontend.log &
PIDS+=("$!")

wait
