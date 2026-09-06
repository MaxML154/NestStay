#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
MYSQL="${MYSQL:-mysql}"
MYSQL_OPTS=(--default-character-set=utf8mb4)
DB="neststay_demo"
USER="${MYSQL_USER:-root}"
PASS="${MYSQL_PASSWORD:-123456}"
NESTSTAY_DB="$ROOT/../../NestStay/db"

run_sql() {
  local file="$1"
  [[ -f "$NESTSTAY_DB/$file" ]] || return 0
  echo "      Running $file"
  "$MYSQL" "${MYSQL_OPTS[@]}" -u"$USER" -p"$PASS" "$DB" < "$NESTSTAY_DB/$file" || echo "[WARN] $file non-zero exit"
}

echo "============================================================"
echo "  Initializing MySQL database: $DB"
echo "============================================================"

command -v "$MYSQL" >/dev/null || { echo "[ERROR] mysql client not found"; exit 1; }

echo "[1/3] Recreate database..."
"$MYSQL" "${MYSQL_OPTS[@]}" -u"$USER" -p"$PASS" -e "DROP DATABASE IF EXISTS \`$DB\`; CREATE DATABASE \`$DB\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

echo "[2/3] Load base schema..."
[[ -f "$NESTSTAY_DB/neststay.sql" ]] || { echo "[ERROR] Missing neststay.sql"; exit 1; }
sed 's/`neststay`/`neststay_demo`/g' "$NESTSTAY_DB/neststay.sql" | "$MYSQL" "${MYSQL_OPTS[@]}" -u"$USER" -p"$PASS" "$DB"

echo "[3/3] Apply migrations in order..."
for file in \
  migrate_round3.sql migrate_round4.sql migrate_round5.sql migrate_round6.sql \
  migrate_round7.sql migrate_round8.sql migrate_round9.sql migrate_round10.sql \
  migrate_round11.sql migrate_round12.sql migrate_round13.sql migrate_round14.sql \
  migrate_round15.sql migrate_round16.sql migrate_round17.sql migrate_round18.sql \
  migrate_round20.sql migrate_admin_permissions.sql migrate_core_workflows.sql \
  migrate_forum_hierarchy.sql migrate_home_display.sql migrate_news_comment_menu.sql \
  migrate_news_features.sql migrate_order_status.sql
do
  run_sql "$file"
done

if [[ -f "$ROOT/neststay_demo_seed.sql" ]]; then
  echo "[+] Apply demo seed ..."
  "$MYSQL" "${MYSQL_OPTS[@]}" -u"$USER" -p"$PASS" "$DB" < "$ROOT/neststay_demo_seed.sql"
fi

echo "Done. Database \`$DB\` is ready."
echo "Admin login: admin / admin"
