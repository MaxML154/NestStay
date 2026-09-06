#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
FRONT_DIR="$ROOT/src/main/resources/front/front"
ADMIN_DIR="$ROOT/src/main/resources/admin/admin"
MODE="${1:-debug}"

cd "$ROOT"

echo "============================================================"
if [[ "$MODE" == "build" ]]; then
  echo "  NestStay Shell - production build + backend only"
else
  echo "  NestStay Shell - debug launcher"
fi
echo "============================================================"
echo "First-time setup: bash db/init_demo.sh"
echo

if [[ "$MODE" == "build" ]]; then
  command -v node >/dev/null || { echo "[ERROR] node not found"; exit 1; }
  command -v npm >/dev/null || { echo "[ERROR] npm not found"; exit 1; }
fi

free_port() {
  local p="$1"
  if command -v lsof >/dev/null; then
    local pids
    pids="$(lsof -ti:"$p" 2>/dev/null || true)"
    if [[ -n "$pids" ]]; then
      # shellcheck disable=SC2086
      kill -9 $pids 2>/dev/null || true
    fi
  fi
}

if [[ "$MODE" == "build" ]]; then
  echo "[WARN] Frontend rebuild still uses PowerShell (scripts/build-shell-frontends.ps1)."
  echo "       Dist is already in the repo; skip this and run: ./gradlew bootRun"
  if command -v pwsh >/dev/null; then
    pwsh -NoProfile -File "$ROOT/scripts/build-shell-frontends.ps1"
  elif command -v powershell >/dev/null; then
    powershell -NoProfile -File "$ROOT/scripts/build-shell-frontends.ps1"
  else
    echo "[ERROR] pwsh/powershell not found. Use ./gradlew bootRun with existing dist."
    exit 1
  fi
  free_port 8080
  ./gradlew classes -x test
  echo "Consumer: http://localhost:8080/neststay/front/index.html#/index/home"
  echo "Admin:    http://localhost:8080/neststay/admin/admin/dist/index.html#/login"
  ./gradlew bootRun
  exit 0
fi

[[ -f "$FRONT_DIR/dist/index.html" ]] || echo "[WARN] Consumer dist missing"
[[ -f "$ADMIN_DIR/dist/index.html" ]] || echo "[WARN] Admin dist missing"
free_port 8080
free_port 8081
free_port 8082
./gradlew classes -x test
./gradlew bootRun &
sleep 8
echo "Open http://localhost:8080/neststay/front/index.html#/index/home"
wait
